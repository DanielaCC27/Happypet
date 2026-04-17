package com.uq.happypet.service;

import com.uq.happypet.dto.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ProductoRepository productoRepository;

    public CartService(UsuarioRepository usuarioRepository,
                       CarritoRepository carritoRepository,
                       ItemCarritoRepository itemCarritoRepository,
                       ProductoRepository productoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String username) {
        Usuario usuario = requireUsuario(username);
        return carritoRepository.findActiveCartWithItems(usuario)
                .map(this::toCartResponse)
                .orElseGet(this::emptyCartResponse);
    }

    @Transactional
    public CartResponse add(String username, CartAddRequest request) {
        Usuario usuario = requireUsuario(username);
        Carrito carrito = getOrCreateActiveCart(usuario);
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado."));

        int cantidadNueva = request.getCantidad();
        ItemCarrito existente = itemCarritoRepository.findByCarritoAndProducto(carrito, producto).orElse(null);
        int cantidadTotal = existente != null ? existente.getCantidad() + cantidadNueva : cantidadNueva;

        if (producto.getStock() < cantidadTotal) {
            throw new ProductOutOfStockException(
                    "Stock insuficiente para \"" + producto.getNombre() + "\". Disponible: "
                            + producto.getStock() + ", requerido en carrito: " + cantidadTotal);
        }

        if (existente != null) {
            existente.setCantidad(cantidadTotal);
        } else {
            ItemCarrito line = new ItemCarrito(carrito, producto, cantidadNueva);
            carrito.getItems().add(line);
        }
        carritoRepository.save(carrito);
        return carritoRepository.findActiveCartWithItems(usuario)
                .map(this::toCartResponse)
                .orElseGet(this::emptyCartResponse);
    }

    @Transactional
    public CartResponse updateQuantity(String username, CartUpdateRequest request) {
        Usuario usuario = requireUsuario(username);
        Carrito carrito = carritoRepository.findByUsuarioAndActivoTrue(usuario)
                .orElseThrow(() -> new CartEmptyException("No hay un carrito activo."));
        ItemCarrito line = itemCarritoRepository.findByIdAndCarrito(request.getItemId(), carrito)
                .orElseThrow(() -> new CartItemNotFoundException("Linea de carrito no encontrada."));

        Producto producto = line.getProducto();
        if (producto.getStock() < request.getCantidad()) {
            throw new ProductOutOfStockException(
                    "Stock insuficiente para \"" + producto.getNombre() + "\". Disponible: "
                            + producto.getStock() + ", solicitado: " + request.getCantidad());
        }
        line.setCantidad(request.getCantidad());
        carritoRepository.save(carrito);
        return carritoRepository.findActiveCartWithItems(usuario)
                .map(this::toCartResponse)
                .orElseGet(this::emptyCartResponse);
    }

    @Transactional
    public CartResponse removeItem(String username, Long itemId) {
        Usuario usuario = requireUsuario(username);
        Carrito carrito = carritoRepository.findByUsuarioAndActivoTrue(usuario)
                .orElseThrow(() -> new CartEmptyException("No hay un carrito activo."));
        ItemCarrito line = itemCarritoRepository.findByIdAndCarrito(itemId, carrito)
                .orElseThrow(() -> new CartItemNotFoundException("Linea de carrito no encontrada."));
        carrito.getItems().remove(line);
        itemCarritoRepository.delete(line);
        carritoRepository.save(carrito);
        return carritoRepository.findActiveCartWithItems(usuario)
                .map(this::toCartResponse)
                .orElseGet(this::emptyCartResponse);
    }

    private Usuario requireUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado."));
    }

    private Carrito getOrCreateActiveCart(Usuario usuario) {
        return carritoRepository.findByUsuarioAndActivoTrue(usuario).orElseGet(() -> {
            Carrito c = new Carrito(usuario, true);
            return carritoRepository.save(c);
        });
    }

    private CartResponse emptyCartResponse() {
        CartResponse r = new CartResponse();
        r.setCarritoId(null);
        r.setItems(new ArrayList<>());
        r.setTotal(0);
        return r;
    }

    private CartResponse toCartResponse(Carrito carrito) {
        CartResponse dto = new CartResponse();
        dto.setCarritoId(carrito.getId());
        double total = 0;
        List<CartItemResponse> lines = new ArrayList<>();
        for (ItemCarrito line : carrito.getItems()) {
            Producto p = line.getProducto();
            double precio = p.getPrecio();
            double subtotal = precio * line.getCantidad();
            total += subtotal;
            lines.add(new CartItemResponse(
                    line.getId(),
                    p.getId(),
                    p.getNombre(),
                    p.getImagenUrl(),
                    line.getCantidad(),
                    precio,
                    subtotal
            ));
        }
        dto.setItems(lines);
        dto.setTotal(total);
        return dto;
    }
}