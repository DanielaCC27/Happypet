package com.uq.happypet.dto;

import com.uq.happypet.model.PedidoEstado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderApiEstadosTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "en_proceso|CONFIRMADO",
            "EN_PROCESO|CONFIRMADO",
            " enviado |ENVIADO",
            "ENVIADO|ENVIADO",
            "entregado|ENTREGADO",
            "ENTREGADO|ENTREGADO"
    })
    void toPedidoEstado_valoresApiPermitidos_mapeaDominio(String raw, String esperadoNombre) {
        assertEquals(PedidoEstado.valueOf(esperadoNombre), OrderApiEstados.toPedidoEstado(raw));
    }

    @Test
    void toPedidoEstado_null_lanza() {
        assertThrows(IllegalArgumentException.class, () -> OrderApiEstados.toPedidoEstado(null));
    }

    @Test
    void toPedidoEstado_blank_lanza() {
        assertThrows(IllegalArgumentException.class, () -> OrderApiEstados.toPedidoEstado("   "));
    }

    @Test
    void toPedidoEstado_valorDesconocido_lanza() {
        assertThrows(IllegalArgumentException.class, () -> OrderApiEstados.toPedidoEstado("cancelado"));
    }
}