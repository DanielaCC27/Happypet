package com.uq.happypet.service;

import com.uq.happypet.dto.CheckoutRequest;
import com.uq.happypet.dto.OrderItemResponse;
import com.uq.happypet.dto.OrderResponse;
import com.uq.happypet.exception.CartEmptyException;
import com.uq.happypet.exception.ProductNotFoundException;
import com.uq.happypet.exception.ProductOutOfStockException;
import com.uq.happypet.model.*;
import com.uq.happypet.repository.CarritoRepository;
import com.uq.happypet.repository.PedidoRepository;
import com.uq.happypet.repository.ProductoRepository;
import com.uq.happypet.repository.UsuarioRepository;
import com.uq.happypet.util.DeliveryScheduleUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final EmailService emailService;

    public OrderService(UsuarioRepository usuarioRepository,
                        CarritoRepository carritoRepository,
                        ProductoRepository productoRepository,
                        PedidoRepository pedidoRepository,
                        EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.emailService = emailService;
    }

    @Transactional
    public OrderResponse checkout(String username, CheckoutRequest req) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado."));

        LocalDate hoy = LocalDate.now();
        LocalDate min = DeliveryScheduleUtil.fechaMinima(hoy);
        LocalDate max = DeliveryScheduleUtil.fechaMaxima(hoy);
        LocalDate elegida = req.getFechaEntregaPreferida();
        if (!DeliveryScheduleUtil.fechaElegible(elegida, hoy)) {
            throw new IllegalArgumentException(
                    "El d\u00eda de entrega debe estar entre " + min + " y " + max + " (inclusive).");
        }

        Carrito carrito = carritoRepository.findActiveCartWithItems(usuario)
                .orElseThrow(() -> new CartEmptyException("El carrito esta vacio o no existe."));
        if (carrito.getItems().isEmpty()) {
            throw new CartEmptyException("El carrito esta vacio.");
        }

        List<ItemCarrito> sorted = carrito.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getProducto().getId()))
                .toList();

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(PedidoEstado.CREADO);
        pedido.setDireccionEnvio(req.getDireccionEnvio().trim());
        pedido.setMetodoPago(req.getMetodoPago());
        pedido.setFechaEntregaPreferida(elegida);
        pedido.setHorarioEntrega(req.getHorarioEntrega());
        pedido.setVentanaEntregaDesde(min);
        pedido.setVentanaEntregaHasta(max);
        pedido.setFacturacionNombre(req.getFacturacionNombre().trim());
        pedido.setFacturacionApellidos(req.getFacturacionApellidos().trim());
        pedido.setFacturacionTipoDocumento(req.getFacturacionTipoDocumento().trim());
        pedido.setFacturacionDocumento(req.getFacturacionDocumento().trim());
        pedido.setFacturacionDireccion(req.getFacturacionDireccion().trim());
        pedido.setFacturacionEmail(req.getFacturacionEmail().trim());

        double total = 0;
        for (ItemCarrito line : sorted) {
            Long productId = line.getProducto().getId();
            Producto p = productoRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado."));
            int qty = line.getCantidad();
            if (p.getStock() < qty) {
                throw new ProductOutOfStockException(
                        "Stock insuficiente para \"" + p.getNombre() + "\". Disponible: "
                                + p.getStock() + ", solicitado: " + qty);
            }
            p.setStock(p.getStock() - qty);
            double precioUnitario = p.getPrecio();
            total += precioUnitario * qty;
            DetallePedido det = new DetallePedido(pedido, p, qty, precioUnitario);
            pedido.getDetalles().add(det);
        }
        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);

        carrito.getItems().clear();
        carrito.setActivo(false);
        carritoRepository.save(carrito);

        emailService.enviarPedidoRecibidoCliente(guardado);

        return toOrderResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .map(pedidoRepository::findByUsuarioOrderByFechaDesc)
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarTodosPedidos() {
        return pedidoRepository.findAllByOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPedidoAdmin(Long id) {
        return pedidoRepository.findFetchedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPedidoUsuario(String username, Long id) {
        Pedido p = pedidoRepository.findFetchedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));
        if (!p.getUsuario().getUsername().equals(username)) {
            throw new IllegalArgumentException("No autorizado.");
        }
        return p;
    }

    @Transactional
    public void cancelarPedidoUsuario(String username, Long id) {
        Pedido p = pedidoRepository.findFetchedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));
        if (!p.getUsuario().getUsername().equals(username)) {
            throw new IllegalArgumentException("No autorizado.");
        }
        if (p.getEstado() != PedidoEstado.CREADO) {
            throw new IllegalStateException("Solo se pueden cancelar pedidos en estado creado.");
        }
        devolverStockAlInventario(p);
        p.setEstado(PedidoEstado.CANCELADO);
        pedidoRepository.save(p);
    }

    /**
     * Devuelve al inventario las unidades reservadas por un pedido (cancelaci\u00f3n o eliminaci\u00f3n).
     */
    private void devolverStockAlInventario(Pedido p) {
        for (DetallePedido linea : p.getDetalles()) {
            Long pid = linea.getProducto().getId();
            Producto prod = productoRepository.findByIdForUpdate(pid)
                    .orElseThrow(() -> new IllegalStateException("Producto no encontrado: " + pid));
            prod.setStock(prod.getStock() + linea.getCantidad());
        }
    }

    @Transactional
    public void confirmarPedidoAdmin(Long id) {
        actualizarEstadoAdmin(id, PedidoEstado.CONFIRMADO);
    }

    @Transactional
    public void eliminarPedidoAdmin(Long id) {
        Pedido p = pedidoRepository.findFetchedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));
        if (p.getEstado() != PedidoEstado.CANCELADO) {
            devolverStockAlInventario(p);
        }
        pedidoRepository.delete(p);
    }

    @Transactional
    public void actualizarEstadoAdmin(Long id, PedidoEstado nuevo) {
        Pedido p = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));
        if (nuevo == null) {
            throw new IllegalArgumentException("Estado inv\u00e1lido.");
        }
        if (p.getEstado() == PedidoEstado.CANCELADO && nuevo != PedidoEstado.CANCELADO) {
            throw new IllegalStateException("No se puede modificar un pedido cancelado.");
        }
        PedidoEstado anterior = p.getEstado();
        p.setEstado(nuevo);
        pedidoRepository.save(p);
        Pedido fetched = pedidoRepository.findFetchedById(id).orElse(p);
        if (anterior != PedidoEstado.CONFIRMADO && nuevo == PedidoEstado.CONFIRMADO) {
            emailService.enviarPedidoConfirmadoCliente(fetched);
        }
        if (anterior != PedidoEstado.ENVIADO && nuevo == PedidoEstado.ENVIADO) {
            emailService.enviarPedidoEnviadoCliente(fetched);
        }
    }

    private OrderResponse toOrderResponse(Pedido pedido) {
        OrderResponse dto = new OrderResponse();
        dto.setId(pedido.getId());
        dto.setFecha(pedido.getFecha());
        dto.setTotal(pedido.getTotal());
        dto.setEstado(pedido.getEstado());
        for (DetallePedido d : pedido.getDetalles()) {
            Producto p = d.getProducto();
            double subtotal = d.getPrecioUnitario() * d.getCantidad();
            dto.getItems().add(new OrderItemResponse(
                    p.getId(),
                    p.getNombre(),
                    d.getCantidad(),
                    d.getPrecioUnitario(),
                    subtotal
            ));
        }
        return dto;
    }
}
