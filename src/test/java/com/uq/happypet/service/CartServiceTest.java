package com.uq.happypet.service;

import com.uq.happypet.dto.CartAddRequest;
import com.uq.happypet.dto.CartResponse;
import com.uq.happypet.dto.CartUpdateRequest;
import com.uq.happypet.exception.CartEmptyException;
import com.uq.happypet.exception.CartItemNotFoundException;
import com.uq.happypet.exception.ProductNotFoundException;
import com.uq.happypet.exception.ProductOutOfStockException;
import com.uq.happypet.model.Carrito;
import com.uq.happypet.model.ItemCarrito;
import com.uq.happypet.model.Producto;
import com.uq.happypet.model.Usuario;
import com.uq.happypet.repository.CarritoRepository;
import com.uq.happypet.repository.ItemCarritoRepository;
import com.uq.happypet.repository.ProductoRepository;
import com.uq.happypet.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private ItemCarritoRepository itemCarritoRepository;
    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CartService cartService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Ana", "ana@test.com", "ana", "hash", "ROLE_USER");
        ReflectionTestUtils.setField(usuario, "id", 1L);
    }

    @Test
    void getCart_sinCarritoActivo_devuelveVacio() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findActiveCartWithItems(usuario)).thenReturn(Optional.empty());

        CartResponse r = cartService.getCart("ana");

        assertNull(r.getCarritoId());
        assertTrue(r.getItems().isEmpty());
        assertEquals(0, r.getTotal());
    }

    @Test
    void getCart_sinUsuario_lanza() {
        when(usuarioRepository.findByUsername("x")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> cartService.getCart("x"));
    }

    @Test
    void getCart_multiplesLineas_totalEsSumaSubtotales() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        Carrito carrito = new Carrito(usuario, true);
        ReflectionTestUtils.setField(carrito, "id", 55L);

        Producto p1 = new Producto("Producto A", "d", 15000, 10, "a.jpg");
        ReflectionTestUtils.setField(p1, "id", 11L);
        Producto p2 = new Producto("Producto B", "d", 2500, 10, "b.jpg");
        ReflectionTestUtils.setField(p2, "id", 12L);

        ItemCarrito linea1 = new ItemCarrito(carrito, p1, 1);
        ItemCarrito linea2 = new ItemCarrito(carrito, p2, 3);
        carrito.getItems().add(linea1);
        carrito.getItems().add(linea2);

        when(carritoRepository.findActiveCartWithItems(usuario)).thenReturn(Optional.of(carrito));

        CartResponse r = cartService.getCart("ana");

        assertEquals(2, r.getItems().size());
        assertEquals(22500.0, r.getTotal(), 1e-6);
        assertEquals(15000.0, r.getItems().get(0).getSubtotal(), 1e-6);
        assertEquals(7500.0, r.getItems().get(1).getSubtotal(), 1e-6);
    }

    @Test
    void add_nuevaLinea_creaCarritoYGuarda() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioAndActivoTrue(usuario)).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> {
            Carrito c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 10L);
            return c;
        });

        Producto p = new Producto("Croquetas", "Desc", 10000, 5, "img.jpg");
        ReflectionTestUtils.setField(p, "id", 20L);
        when(productoRepository.findById(20L)).thenReturn(Optional.of(p));
        when(itemCarritoRepository.findByCarritoAndProducto(any(), eq(p))).thenReturn(Optional.empty());

        Carrito guardado = new Carrito(usuario, true);
        ReflectionTestUtils.setField(guardado, "id", 10L);
        ItemCarrito linea = new ItemCarrito(guardado, p, 2);
        ReflectionTestUtils.setField(linea, "id", 30L);
        guardado.getItems().add(linea);

        when(carritoRepository.findActiveCartWithItems(usuario)).thenReturn(Optional.of(guardado));

        CartAddRequest req = new CartAddRequest();
        req.setProductoId(20L);
        req.setCantidad(2);

        CartResponse r = cartService.add("ana", req);

        verify(carritoRepository, atLeastOnce()).save(any(Carrito.class));
        assertEquals(10L, r.getCarritoId());
        assertEquals(1, r.getItems().size());
        assertEquals(20000.0, r.getTotal(), 1e-6);
    }

    @Test
    void add_productoInexistente_lanzaProductNotFound() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        Carrito c = new Carrito(usuario, true);
        ReflectionTestUtils.setField(c, "id", 1L);
        when(carritoRepository.findByUsuarioAndActivoTrue(usuario)).thenReturn(Optional.of(c));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        CartAddRequest req = new CartAddRequest();
        req.setProductoId(99L);
        req.setCantidad(1);

        assertThrows(ProductNotFoundException.class, () -> cartService.add("ana", req));
    }

    @Test
    void add_stockInsuficiente_lanzaProductOutOfStock() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        Carrito c = new Carrito(usuario, true);
        when(carritoRepository.findByUsuarioAndActivoTrue(usuario)).thenReturn(Optional.of(c));

        Producto p = new Producto("P", "d", 1, 1, "i");
        ReflectionTestUtils.setField(p, "id", 2L);
        when(productoRepository.findById(2L)).thenReturn(Optional.of(p));
        when(itemCarritoRepository.findByCarritoAndProducto(any(), eq(p))).thenReturn(Optional.empty());

        CartAddRequest req = new CartAddRequest();
        req.setProductoId(2L);
        req.setCantidad(2);

        assertThrows(ProductOutOfStockException.class, () -> cartService.add("ana", req));
    }

    @Test
    void updateQuantity_sinCarritoActivo_lanzaCartEmpty() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioAndActivoTrue(usuario)).thenReturn(Optional.empty());

        CartUpdateRequest req = new CartUpdateRequest();
        req.setItemId(1L);
        req.setCantidad(3);

        assertThrows(CartEmptyException.class, () -> cartService.updateQuantity("ana", req));
    }

    @Test
    void removeItem_itemAjeno_lanzaCartItemNotFound() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        Carrito c = new Carrito(usuario, true);
        when(carritoRepository.findByUsuarioAndActivoTrue(usuario)).thenReturn(Optional.of(c));
        when(itemCarritoRepository.findByIdAndCarrito(5L, c)).thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class, () -> cartService.removeItem("ana", 5L));
    }

    @Test
    void add_productoExistente_actualizaCantidadYTotal() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        Carrito carrito = new Carrito(usuario, true);
        ReflectionTestUtils.setField(carrito, "id", 22L);
        when(carritoRepository.findByUsuarioAndActivoTrue(usuario)).thenReturn(Optional.of(carrito));

        Producto producto = new Producto("Arena", "desc", 12000, 10, "img");
        ReflectionTestUtils.setField(producto, "id", 3L);
        when(productoRepository.findById(3L)).thenReturn(Optional.of(producto));

        ItemCarrito existente = new ItemCarrito(carrito, producto, 2);
        ReflectionTestUtils.setField(existente, "id", 40L);
        when(itemCarritoRepository.findByCarritoAndProducto(carrito, producto)).thenReturn(Optional.of(existente));
        carrito.setItems(List.of(existente));
        when(carritoRepository.findActiveCartWithItems(usuario)).thenReturn(Optional.of(carrito));

        CartAddRequest req = new CartAddRequest();
        req.setProductoId(3L);
        req.setCantidad(1);

        CartResponse response = cartService.add("ana", req);

        assertEquals(3, existente.getCantidad());
        assertEquals(36000.0, response.getTotal(), 1e-6);
        verify(carritoRepository).save(carrito);
    }

    @Test
    void removeItem_exitoso_devuelveCarritoVacio() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        Carrito carrito = new Carrito(usuario, true);
        ReflectionTestUtils.setField(carrito, "id", 23L);
        when(carritoRepository.findByUsuarioAndActivoTrue(usuario)).thenReturn(Optional.of(carrito));

        Producto p = new Producto("Snack", "desc", 5000, 8, "img");
        ReflectionTestUtils.setField(p, "id", 6L);
        ItemCarrito line = new ItemCarrito(carrito, p, 1);
        ReflectionTestUtils.setField(line, "id", 88L);
        carrito.getItems().add(line);
        when(itemCarritoRepository.findByIdAndCarrito(88L, carrito)).thenReturn(Optional.of(line));
        when(carritoRepository.findActiveCartWithItems(usuario)).thenReturn(Optional.of(carrito));

        CartResponse response = cartService.removeItem("ana", 88L);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0.0, response.getTotal(), 1e-6);
        verify(itemCarritoRepository).delete(line);
    }

    @Test
    void removeItem_quedaOtroProducto_recalculaTotal() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        Carrito carrito = new Carrito(usuario, true);
        ReflectionTestUtils.setField(carrito, "id", 99L);
        when(carritoRepository.findByUsuarioAndActivoTrue(usuario)).thenReturn(Optional.of(carrito));

        Producto p1 = new Producto("Snack", "d", 5000, 10, "i1.jpg");
        ReflectionTestUtils.setField(p1, "id", 1L);
        Producto p2 = new Producto("Juguete", "d", 3000, 10, "i2.jpg");
        ReflectionTestUtils.setField(p2, "id", 2L);

        ItemCarrito lineaRemove = new ItemCarrito(carrito, p1, 2);
        ReflectionTestUtils.setField(lineaRemove, "id", 101L);
        ItemCarrito lineaQueda = new ItemCarrito(carrito, p2, 1);
        ReflectionTestUtils.setField(lineaQueda, "id", 102L);

        carrito.getItems().clear();
        carrito.getItems().add(lineaRemove);
        carrito.getItems().add(lineaQueda);

        when(itemCarritoRepository.findByIdAndCarrito(101L, carrito)).thenReturn(Optional.of(lineaRemove));
        when(carritoRepository.findActiveCartWithItems(usuario)).thenReturn(Optional.of(carrito));

        CartResponse r = cartService.removeItem("ana", 101L);

        assertEquals(1, r.getItems().size());
        assertEquals(3000.0, r.getTotal(), 1e-6);
        verify(itemCarritoRepository).delete(lineaRemove);
        verify(carritoRepository).save(carrito);
    }
}
