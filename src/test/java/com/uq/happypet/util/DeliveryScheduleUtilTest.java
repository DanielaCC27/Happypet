package com.uq.happypet.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryScheduleUtilTest {

    @Test
    void fechaMinima_sumaCuatroDias_alDiaDeHoy() {
        LocalDate hoy = LocalDate.of(2026, 4, 10);
        assertEquals(hoy.plusDays(4), DeliveryScheduleUtil.fechaMinima(hoy));
    }

    @Test
    void fechaMaxima_sumaSeisDias_alDiaDeHoy() {
        LocalDate hoy = LocalDate.of(2026, 4, 10);
        assertEquals(hoy.plusDays(6), DeliveryScheduleUtil.fechaMaxima(hoy));
    }

    @Test
    void fechaElegible_incluyeVentanaBorde() {
        LocalDate hoy = LocalDate.of(2026, 1, 1);
        LocalDate min = hoy.plusDays(4);
        LocalDate max = hoy.plusDays(6);
        assertTrue(DeliveryScheduleUtil.fechaElegible(min, hoy));
        assertTrue(DeliveryScheduleUtil.fechaElegible(max, hoy));
    }

    @Test
    void fechaElegible_rechazaFueraDeRangoONulos() {
        LocalDate hoy = LocalDate.of(2026, 1, 1);
        assertFalse(DeliveryScheduleUtil.fechaElegible(hoy.plusDays(3), hoy));
        assertFalse(DeliveryScheduleUtil.fechaElegible(hoy.plusDays(7), hoy));
        assertFalse(DeliveryScheduleUtil.fechaElegible(null, hoy));
        assertFalse(DeliveryScheduleUtil.fechaElegible(hoy.plusDays(4), null));
    }

    @Test
    void listarDiasConsecutivos_generaListaInclusiva() {
        LocalDate desde = LocalDate.of(2026, 3, 1);
        LocalDate hasta = LocalDate.of(2026, 3, 3);
        List<LocalDate> dias = DeliveryScheduleUtil.listarDiasConsecutivos(desde, hasta);
        assertEquals(3, dias.size());
        assertEquals(desde, dias.get(0));
        assertEquals(hasta, dias.get(2));
    }

    @Test
    void listarDiasConsecutivos_vacioSiArgumentosInvalidos() {
        assertTrue(DeliveryScheduleUtil.listarDiasConsecutivos(null, LocalDate.now()).isEmpty());
        assertTrue(DeliveryScheduleUtil.listarDiasConsecutivos(LocalDate.now(), null).isEmpty());
        assertTrue(DeliveryScheduleUtil.listarDiasConsecutivos(
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 4)).isEmpty());
    }
}
