package com.uq.happypet.dto;

import com.uq.happypet.model.HorarioEntrega;
import com.uq.happypet.model.MetodoPago;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Checkout wizard state stored in HTTP session.
 */
public class CheckoutSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate fechaEntregaPreferida;
    private HorarioEntrega horarioEntrega;
    private MetodoPago metodoPago;

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

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public boolean hasEntrega() {
        return fechaEntregaPreferida != null && horarioEntrega != null;
    }

    public boolean hasPago() {
        return metodoPago != null;
    }
}
