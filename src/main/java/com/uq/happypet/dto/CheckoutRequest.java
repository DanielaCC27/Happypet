package com.uq.happypet.dto;

import com.uq.happypet.model.HorarioEntrega;
import com.uq.happypet.model.MetodoPago;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CheckoutRequest {

    @NotBlank(message = "Shipping address is required")
    @Size(max = 1000)
    private String direccionEnvio;

    @NotNull(message = "Payment method is required")
    private MetodoPago metodoPago;

    @NotNull(message = "Delivery date is required")
    private LocalDate fechaEntregaPreferida;

    @NotNull(message = "Delivery time window is required")
    private HorarioEntrega horarioEntrega;

    @NotBlank(message = "Billing name is required")
    @Size(max = 200)
    private String facturacionNombre;

    @NotBlank(message = "Billing surnames are required")
    @Size(max = 200)
    private String facturacionApellidos;

    @NotBlank(message = "Document type is required")
    @Size(max = 32)
    private String facturacionTipoDocumento;

    @NotBlank(message = "Billing document is required")
    @Size(max = 32)
    private String facturacionDocumento;

    @NotBlank(message = "Billing address is required")
    @Size(max = 1000)
    private String facturacionDireccion;

    @NotBlank(message = "Billing email is required")
    @Email(message = "Invalid billing email")
    @Size(max = 255)
    private String facturacionEmail;

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