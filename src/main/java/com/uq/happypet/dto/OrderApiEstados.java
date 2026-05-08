package com.uq.happypet.dto;

import com.uq.happypet.model.PedidoEstado;

import java.util.Locale;

/**
 * API order status strings (snake_case) mapped to persisted {@link PedidoEstado}.
 */
public final class OrderApiEstados {

    private OrderApiEstados() {
    }

    /**
     * @param raw JSON field {@code estado} (ADMIN only).
     */
    public static PedidoEstado toPedidoEstado(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(
                    "Estado obligatorio; valores permitidos: en_proceso, enviado, entregado.");
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "en_proceso" -> PedidoEstado.CONFIRMADO;
            case "enviado" -> PedidoEstado.ENVIADO;
            case "entregado" -> PedidoEstado.ENTREGADO;
            default -> throw new IllegalArgumentException(
                    "Estado no permitido; use uno de: en_proceso, enviado, entregado.");
        };
    }
}
