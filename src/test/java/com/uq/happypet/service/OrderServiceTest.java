package com.uq.happypet.service;

import com.uq.happypet.dto.CheckoutRequest;
import com.uq.happypet.dto.OrderResponse;
import com.uq.happypet.exception.CartEmptyException;
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
    void listarPedidosUsuario_usuarioInexistente_devuelveVacio() {
        when(usuarioRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        List<Pedido> lista = orderService.listarPedidosUsuario("ghost");

        assertTrue(lista.isEmpty());
        verify(pedidoRepository, never()).findByUsuarioOrderByFechaDesc(any());
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
