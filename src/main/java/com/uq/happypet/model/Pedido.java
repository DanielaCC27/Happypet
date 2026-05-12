package com.uq.happypet.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un pedido realizado por un usuario.
 *
 * Contiene información relacionada con:
 * - datos de compra
 * - entrega
 * - facturación
 * - productos asociados al pedido
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que realizó el pedido.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Fecha y hora en que se registró el pedido.
     */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /**
     * Valor total de la compra.
     */
    @Column(nullable = false)
    private double total;

    /**
     * Estado actual del pedido.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PedidoEstado estado;

    /**
     * Dirección de entrega registrada por el usuario.
     */
    @Column(nullable = false, length = 1000)
    private String direccionEnvio;

    /**
     * Método de pago utilizado para la compra.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private MetodoPago metodoPago;

    /**
     * Fecha de entrega seleccionada por el cliente.
     */
    @Column(nullable = false)
    private LocalDate fechaEntregaPreferida;

    /**
     * Franja horaria preferida para la entrega.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private HorarioEntrega horarioEntrega;

    /**
     * Fecha mínima disponible para la entrega.
     */
    @Column(nullable = false)
    private LocalDate ventanaEntregaDesde;

    /**
     * Fecha máxima disponible para la entrega.
     */
    @Column(nullable = false)
    private LocalDate ventanaEntregaHasta;

    /**
     * Nombre utilizado para facturación.
     */
    @Column(nullable = false, length = 200)
    private String facturacionNombre;

    /**
     * Apellidos utilizados para facturación.
     */
    @Column(length = 200)
    private String facturacionApellidos;

    /**
     * Tipo de documento de facturación.
     */
    @Column(length = 32)
    private String facturacionTipoDocumento;

    /**
     * Número de documento de facturación.
     */
    @Column(nullable = false, length = 32)
    private String facturacionDocumento;

    /**
     * Dirección de facturación.
     */
    @Column(nullable = false, length = 1000)
    private String facturacionDireccion;

    /**
     * Correo electrónico asociado a la facturación.
     */
    @Column(nullable = false, length = 255)
    private String facturacionEmail;

    /**
     * Fecha y hora en que el cliente confirmó
     * la recepción del pedido.
     */
    @Column(name = "fecha_confirmacion_entrega")
    private LocalDateTime fechaConfirmacionEntrega;

    /**
     * Productos asociados al pedido.
     *
     * orphanRemoval elimina automáticamente
     * los detalles desvinculados del pedido.
     */
    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public PedidoEstado getEstado() {
        return estado;
    }

    public void setEstado(PedidoEstado estado) {
        this.estado = estado;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDate getFechaEntregaPreferida() {
        return fechaEntregaPreferida;
    }

    public void setFechaEntregaPreferida(LocalDate fechaEntregaPreferida) {
        this.fechaEntregaPreferida = fechaEntregaPreferida;
    }

    public HorarioEntrega getHorarioEntrega() {
        return horarioEntrega;
    }

    public void setHorarioEntrega(HorarioEntrega horarioEntrega) {
        this.horarioEntrega = horarioEntrega;
    }

    public LocalDate getVentanaEntregaDesde() {
        return ventanaEntregaDesde;
    }

    public void setVentanaEntregaDesde(LocalDate ventanaEntregaDesde) {
        this.ventanaEntregaDesde = ventanaEntregaDesde;
    }

    public LocalDate getVentanaEntregaHasta() {
        return ventanaEntregaHasta;
    }

    public void setVentanaEntregaHasta(LocalDate ventanaEntregaHasta) {
        this.ventanaEntregaHasta = ventanaEntregaHasta;
    }

    public String getFacturacionNombre() {
        return facturacionNombre;
    }

    public void setFacturacionNombre(String facturacionNombre) {
        this.facturacionNombre = facturacionNombre;
    }

    public String getFacturacionApellidos() {
        return facturacionApellidos;
    }

    public void setFacturacionApellidos(String facturacionApellidos) {
        this.facturacionApellidos = facturacionApellidos;
    }

    public String getFacturacionTipoDocumento() {
        return facturacionTipoDocumento;
    }

    public void setFacturacionTipoDocumento(String facturacionTipoDocumento) {
        this.facturacionTipoDocumento = facturacionTipoDocumento;
    }

    public String getFacturacionDocumento() {
        return facturacionDocumento;
    }

    public void setFacturacionDocumento(String facturacionDocumento) {
        this.facturacionDocumento = facturacionDocumento;
    }

    public String getFacturacionDireccion() {
        return facturacionDireccion;
    }

    public void setFacturacionDireccion(String facturacionDireccion) {
        this.facturacionDireccion = facturacionDireccion;
    }

    public String getFacturacionEmail() {
        return facturacionEmail;
    }

    public void setFacturacionEmail(String facturacionEmail) {
        this.facturacionEmail = facturacionEmail;
    }

    public LocalDateTime getFechaConfirmacionEntrega() {
        return fechaConfirmacionEntrega;
    }

    public void setFechaConfirmacionEntrega(LocalDateTime fechaConfirmacionEntrega) {
        this.fechaConfirmacionEntrega = fechaConfirmacionEntrega;
    }
}