package com.uq.happypet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OrderStatusUpdateRequest {

    @NotBlank(message = "El campo estado es obligatorio.")
    @Pattern(
            regexp = "^(en_proceso|enviado|entregado)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Estado no permitido; use: en_proceso, enviado o entregado."
    )
    private String estado;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
