package com.uq.happypet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CheckoutFacturacionForm {

    @NotBlank(message = "Tipo de documento es obligatorio")
    @Size(max = 32)
    private String facturacionTipoDocumento;

    @NotBlank(message = "Numero de documento es obligatorio")
    @Size(max = 32)
    private String facturacionDocumento;

    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 100)
    private String facturacionNombre;

    @NotBlank(message = "Apellidos son obligatorios")
    @Size(max = 100)
    private String facturacionApellidos;

    @NotBlank(message = "Departamento es obligatorio")
    @Size(max = 120)
    private String departamentoEnvio;

    @NotBlank(message = "Municipio es obligatorio")
    @Size(max = 120)
    private String municipioEnvio;

    /** Calle, número, barrio y referencias (sin departamento ni municipio). */
    @NotBlank(message = "La dirección detallada es obligatoria")
    @Size(max = 800, message = "Máximo 800 caracteres")
    private String direccionEnvio;

    @NotBlank(message = "Correo es obligatorio")
    @Email(message = "Correo no valido")
    @Size(max = 255)
    private String facturacionEmail;

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

    public String getDepartamentoEnvio() {
        return departamentoEnvio;
    }

    public void setDepartamentoEnvio(String departamentoEnvio) {
        this.departamentoEnvio = departamentoEnvio;
    }

    public String getMunicipioEnvio() {
        return municipioEnvio;
    }

    public void setMunicipioEnvio(String municipioEnvio) {
        this.municipioEnvio = municipioEnvio;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getFacturacionEmail() {
        return facturacionEmail;
    }

    public void setFacturacionEmail(String facturacionEmail) {
        this.facturacionEmail = facturacionEmail;
    }
}
