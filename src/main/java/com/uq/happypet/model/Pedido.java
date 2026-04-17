package com.uq.happypet.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PedidoEstado estado;

    @Column(nullable = false, length = 1000)
    private String direccionEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private MetodoPago metodoPago;

    @Column(nullable = false)
    private LocalDate fechaEntregaPreferida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private HorarioEntrega horarioEntrega;

    /** Eligible delivery date range at order time (calendar days +4 to +6 from order date). */
    @Column(nullable = false)
    private LocalDate ventanaEntregaDesde;

    @Column(nullable = false)
    private LocalDate ventanaEntregaHasta;

    @Column(nullable = false, length = 200)
    private String facturacionNombre;

    @Column(length = 200)
    private String facturacionApellidos;

    @Column(length = 32)
    private String facturacionTipoDocumento;

    @Column(nullable = false, length = 32)
    private String facturacionDocumento;

    @Column(nullable = false, length = 1000)
    private String facturacionDireccion;

    @Column(nullable = false, length = 255)
    private String facturacionEmail;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
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
}