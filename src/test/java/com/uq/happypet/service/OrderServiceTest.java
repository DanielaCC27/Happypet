package com.uq.happypet.service;

import com.uq.happypet.dto.CheckoutRequest;
import com.uq.happypet.dto.OrderResponse;
import com.uq.happypet.exception.CartEmptyException;
import com.uq.happypet.exception.PedidoNoEncontradoException;
import com.uq.happypet.exception.ProductNotFoundException;
import com.uq.happypet.exception.ProductOutOfStockException;
import com.uq.happypet.model.*;
import com.uq.happypet.repository.CarritoRepository;
import com.uq.happypet.repository.PedidoRepository;
import com.uq.happypet.repository.ProductoRepository;
import com.uq.happypet.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderService orderService;

    private Usuario cliente;
    private LocalDate hoy;

    @BeforeEach
    void setUp() {
        cliente = new Usuario("Luis", "luis@test.com", "luis", "x", "ROLE_USER");
        ReflectionTestUtils.setField(cliente, "id", 1L);
        hoy = LocalDate.now();
    }

    @Test
    void checkout_usuarioNoRegistrado_lanzaIllegalState() {
        when(usuarioRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> orderService.checkout("ghost", checkoutValido(hoy.plusDays(5))));
        verify(carritoRepository, never()).findActiveCartWithItems(any());
    }

    @Test
    void checkout_fechaEntregaFueraDeVentana_lanzaIllegalArgument() {
        when(usuarioRepository.findByUsername("luis")).thenReturn(Optional.of(cliente));
        CheckoutRequest req = checkoutValido(hoy.plusDays(2));

        assertThrows(IllegalArgumentException.class, () -> orderService.checkout("luis", req));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void checkout_sinCarrito_lanzaCartEmpty() {
        when(usuarioRepository.findByUsername("luis")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findActiveCartWithItems(cliente)).thenReturn(Optional.empty());
        CheckoutRequest req = checkoutValido(hoy.plusDays(4));

        assertThrows(CartEmptyException.class, () -> orderService.checkout("luis", req));
    }

    @Test
    void checkout_carritoSinItems_lanzaCartEmpty() {
        when(usuarioRepository.findByUsername("luis")).thenReturn(Optional.of(cliente));
        Carrito vacio = new Carrito(cliente, true);
        vacio.setItems(new ArrayList<>());
        when(carritoRepository.findActiveCartWithItems(cliente)).thenReturn(Optional.of(vacio));
        CheckoutRequest req = checkoutValido(hoy.plusDays(4));

        assertThrows(CartEmptyException.class, () -> orderService.checkout("luis", req));
    }

    @Test
    void checkout_exito_guardaPedidoEnviaCorreoYcierraCarrito() {
        when(usuarioRepository.findByUsername("luis")).thenReturn(Optional.of(cliente));

        Producto prod = new Producto("Kit", "x", 50.0, 10, "img");
        ReflectionTestUtils.setField(prod, "id", 7L);

        Carrito carrito = new Carrito(cliente, true);
        ReflectionTestUtils.setField(carrito, "id", 2L);
        ItemCarrito linea = new ItemCarrito(carrito, prod, 2);
        carrito.getItems().add(linea);

        when(carritoRepository.findActiveCartWithItems(cliente)).thenReturn(Optional.of(carrito));
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(prod));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 99L);
            return p;
        });

        CheckoutRequest req = checkoutValido(hoy.plusDays(5));
        OrderResponse out = orderService.checkout("luis", req);

        assertEquals(99L, out.getId());
        assertEquals(100.0, out.getTotal(), 1e-6);
        verify(emailService).enviarPedidoRecibidoCliente(any(Pedido.class));
        verify(carritoRepository).save(carrito);
        assertFalse(carrito.isActivo());
        assertEquals(8, prod.getStock());
    }

    @Test
    void checkout_stockInsuficiente_lanzaProductOutOfStock() {
        when(usuarioRepository.findByUsername("luis")).thenReturn(Optional.of(cliente));

        Producto prod = new Producto("Kit", "x", 50.0, 1, "img");
        ReflectionTestUtils.setField(prod, "id", 7L);

        Carrito carrito = new Carrito(cliente, true);
        ItemCarrito linea = new ItemCarrito(carrito, prod, 3);
        carrito.getItems().add(linea);

        when(carritoRepository.findActiveCartWithItems(cliente)).thenReturn(Optional.of(carrito));
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(prod));

        assertThrows(ProductOutOfStockException.class,
                () -> orderService.checkout("luis", checkoutValido(hoy.plusDays(5))));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void checkout_productoNoEncontradoEnCatalogo_lanzaProductNotFound() {
        when(usuarioRepository.findByUsername("luis")).thenReturn(Optional.of(cliente));

        Producto prod = new Producto("Kit", "x", 50.0, 10, "img");
        ReflectionTestUtils.setField(prod, "id", 7L);

        Carrito carrito = new Carrito(cliente, true);
        ItemCarrito linea = new ItemCarrito(carrito, prod, 1);
        carrito.getItems().add(linea);

        when(carritoRepository.findActiveCartWithItems(cliente)).thenReturn(Optional.of(carrito));
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> orderService.checkout("luis", checkoutValido(hoy.plusDays(5))));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void obtenerPedidoUsuario_pedidoInexistente_lanzaPedidoNoEncontrado() {
        when(pedidoRepository.findFetchedById(999L)).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoException.class,
                () -> orderService.obtenerPedidoUsuario("luis", 999L));
    }

    @Test
    void listarTodosPedidos_delegaEnRepositorio() {
        Pedido p = new Pedido();
        p.setEstado(PedidoEstado.CREADO);
        when(pedidoRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(p));

        List<Pedido> todos = orderService.listarTodosPedidos();

        assertEquals(1, todos.size());
        verify(pedidoRepository).findAllByOrderByFechaDesc();
    }

    @Test
    void obtenerPedidoUsuario_otroUsername_lanza() {
        Usuario otro = new Usuario("Otro", "o@t.com", "otro", "x", "ROLE_USER");
        Pedido pedido = new Pedido();
        pedido.setUsuario(otro);
        when(pedidoRepository.findFetchedById(3L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> orderService.obtenerPedidoUsuario("luis", 3L));
    }

    @Test
    void cancelarPedidoUsuario_estadoDistintoDeCreado_lanza() {
        Pedido pedido = new Pedido();
        pedido.setUsuario(cliente);
        pedido.setEstado(PedidoEstado.CONFIRMADO);
        when(pedidoRepository.findFetchedById(8L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> orderService.cancelarPedidoUsuario("luis", 8L));
    }

    @Test
    void actualizarEstadoAdmin_desdeCanceladoANoCancelado_lanza() {
        Pedido pedido = new Pedido();
        pedido.setEstado(PedidoEstado.CANCELADO);
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class,
                () -> orderService.actualizarEstadoAdmin(5L, PedidoEstado.CONFIRMADO));
    }

    @Test
    void actualizarEstadoPedidoAdministracion_enProceso_mapeaConfirmado() {
        Pedido pedido = new Pedido();
        ReflectionTestUtils.setField(pedido, "id", 1L);
        pedido.setUsuario(cliente);
        pedido.setEstado(PedidoEstado.CREADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.findFetchedById(1L)).thenReturn(Optional.of(pedido));

        OrderResponse r = orderService.actualizarEstadoPedidoAdministracion(1L, "en_proceso");

        assertEquals(PedidoEstado.CONFIRMADO, r.getEstado());
        verify(pedidoRepository).save(pedido);
        verify(emailService).notificarClienteCambioEstadoPedido(
                pedido, PedidoEstado.CREADO, PedidoEstado.CONFIRMADO);
    }

    @Test
    void actualizarEstadoPedidoAdministracion_enviado_mapeaEnviado() {
        Pedido pedido = new Pedido();
        ReflectionTestUtils.setField(pedido, "id", 2L);
        pedido.setUsuario(cliente);
        pedido.setEstado(PedidoEstado.CONFIRMADO);
        when(pedidoRepository.findById(2L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.findFetchedById(2L)).thenReturn(Optional.of(pedido));

        OrderResponse r = orderService.actualizarEstadoPedidoAdministracion(2L, "enviado");

        assertEquals(PedidoEstado.ENVIADO, r.getEstado());
        verify(emailService).notificarClienteCambioEstadoPedido(
                pedido, PedidoEstado.CONFIRMADO, PedidoEstado.ENVIADO);
    }

    @Test
    void actualizarEstadoPedidoAdministracion_entregado_mapeaEntregado() {
        Pedido pedido = new Pedido();
        ReflectionTestUtils.setField(pedido, "id", 4L);
        pedido.setUsuario(cliente);
        pedido.setEstado(PedidoEstado.ENVIADO);
        when(pedidoRepository.findById(4L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.findFetchedById(4L)).thenReturn(Optional.of(pedido));

        OrderResponse r = orderService.actualizarEstadoPedidoAdministracion(4L, "entregado");

        assertEquals(PedidoEstado.ENTREGADO, r.getEstado());
        verify(emailService).notificarClienteCambioEstadoPedido(
                pedido, PedidoEstado.ENVIADO, PedidoEstado.ENTREGADO);
    }

    @Test
    void cancelarPedidoUsuario_exito_enviaNotificacion() {
        Pedido pedido = new Pedido();
        ReflectionTestUtils.setField(pedido, "id", 3L);
        pedido.setUsuario(cliente);
        pedido.setEstado(PedidoEstado.CREADO);
        when(pedidoRepository.findFetchedById(3L)).thenReturn(Optional.of(pedido));

        orderService.cancelarPedidoUsuario("luis", 3L);

        verify(emailService).notificarClienteCambioEstadoPedido(
                pedido, PedidoEstado.CREADO, PedidoEstado.CANCELADO);
    }

    @Test
    void actualizarEstadoPedidoAdministracion_pedidoInexistente_lanza() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoException.class,
                () -> orderService.actualizarEstadoPedidoAdministracion(99L, "enviado"));
    }

    @Test
    void listarPedidosUsuario_usuarioInexistente_devuelveVacio() {
        when(usuarioRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        List<Pedido> lista = orderService.listarPedidosUsuario("ghost");

        assertTrue(lista.isEmpty());
        verify(pedidoRepository, never()).findByUsuarioOrderByFechaDesc(any());
    }

    @Test
    void listarPedidosUsuario_usuarioValido_consultaRepositorio() {
        Pedido p = new Pedido();
        p.setUsuario(cliente);
        p.setEstado(PedidoEstado.CREADO);
        when(usuarioRepository.findByUsername("luis")).thenReturn(Optional.of(cliente));
        when(pedidoRepository.findByUsuarioOrderByFechaDesc(cliente)).thenReturn(List.of(p));

        List<Pedido> pedidos = orderService.listarPedidosUsuario("luis");

        assertEquals(1, pedidos.size());
        assertEquals(PedidoEstado.CREADO, pedidos.get(0).getEstado());
        verify(pedidoRepository).findByUsuarioOrderByFechaDesc(cliente);
    }

    @Test
    void confirmarEntregaCliente_pedidoEntregado_guardaTimestamp() {
        Pedido pedido = new Pedido();
        ReflectionTestUtils.setField(pedido, "id", 20L);
        pedido.setUsuario(cliente);
        pedido.setEstado(PedidoEstado.ENTREGADO);
        when(pedidoRepository.findFetchedById(20L)).thenReturn(Optional.of(pedido));

        OrderResponse r = orderService.confirmarEntregaCliente("luis", 20L);

        assertNotNull(r.getFechaConfirmacionEntrega());
        assertEquals(PedidoEstado.ENTREGADO, r.getEstado());
        verify(pedidoRepository).save(pedido);
        assertEquals(r.getFechaConfirmacionEntrega(), pedido.getFechaConfirmacionEntrega());
    }

    @Test
    void confirmarEntregaCliente_estadoDistintoEntregado_lanza() {
        Pedido pedido = new Pedido();
        pedido.setUsuario(cliente);
        pedido.setEstado(PedidoEstado.ENVIADO);
        when(pedidoRepository.findFetchedById(21L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> orderService.confirmarEntregaCliente("luis", 21L));
        verify(pedidoRepository, never()).save(pedido);
    }

    @Test
    void confirmarEntregaCliente_yaConfirmado_lanza() {
        Pedido pedido = new Pedido();
        pedido.setUsuario(cliente);
        pedido.setEstado(PedidoEstado.ENTREGADO);
        pedido.setFechaConfirmacionEntrega(LocalDateTime.now().minusDays(1));
        when(pedidoRepository.findFetchedById(22L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> orderService.confirmarEntregaCliente("luis", 22L));
        verify(pedidoRepository, never()).save(pedido);
    }

    @Test
    void confirmarEntregaCliente_otroUsuario_lanza() {
        Usuario otro = new Usuario("Otro", "o@t.com", "otro", "x", "ROLE_USER");
        Pedido pedido = new Pedido();
        pedido.setUsuario(otro);
        pedido.setEstado(PedidoEstado.ENTREGADO);
        when(pedidoRepository.findFetchedById(23L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> orderService.confirmarEntregaCliente("luis", 23L));
    }

    @Test
    void confirmarEntregaCliente_pedidoInexistente_lanza() {
        when(pedidoRepository.findFetchedById(24L)).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoException.class, () -> orderService.confirmarEntregaCliente("luis", 24L));
    }

    @Test
    void actualizarEstadoAdmin_estadoNulo_lanzaBadRequest() {
        Pedido pedido = new Pedido();
        pedido.setEstado(PedidoEstado.CREADO);
        when(pedidoRepository.findById(44L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> orderService.actualizarEstadoAdmin(44L, null));
    }

    private CheckoutRequest checkoutValido(LocalDate fechaEntrega) {
        CheckoutRequest r = new CheckoutRequest();
        r.setDireccionEnvio("Calle 1");
        r.setMetodoPago(MetodoPago.EFECTIVO);
        r.setFechaEntregaPreferida(fechaEntrega);
        r.setHorarioEntrega(HorarioEntrega.JORNADA_8_18);
        r.setFacturacionNombre("Nom");
        r.setFacturacionApellidos("Ape");
        r.setFacturacionTipoDocumento("CC");
        r.setFacturacionDocumento("123");
        r.setFacturacionDireccion("Dir");
        r.setFacturacionEmail("a@b.co");
        return r;
    }
}
