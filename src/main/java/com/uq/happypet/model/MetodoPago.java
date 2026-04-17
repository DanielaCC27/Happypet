package com.uq.happypet.model;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia bancaria");

    private final String etiqueta;

    MetodoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}