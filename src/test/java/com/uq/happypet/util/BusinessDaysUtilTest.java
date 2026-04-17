package com.uq.happypet.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BusinessDaysUtilTest {

    @Test
    void addBusinessDays_ceroOmenor_devuelveLaMismaFecha() {
        LocalDate lunes = LocalDate.of(2026, 4, 13);
        assertEquals(lunes, BusinessDaysUtil.addBusinessDays(lunes, 0));
        assertEquals(lunes, BusinessDaysUtil.addBusinessDays(lunes, -3));
    }

    @Test
    void addBusinessDays_omiteFinesDeSemana() {
        // Friday 2026-04-17 + 1 business day -> Monday 2026-04-20 (skips Sat/Sun)
        LocalDate viernes = LocalDate.of(2026, 4, 17);
        LocalDate esperado = LocalDate.of(2026, 4, 20);
        assertEquals(esperado, BusinessDaysUtil.addBusinessDays(viernes, 1));
    }

    @Test
    void isWeekend_detectaSabadoYDomingo() {
        assertTrue(BusinessDaysUtil.isWeekend(LocalDate.of(2026, 4, 18)));
        assertTrue(BusinessDaysUtil.isWeekend(LocalDate.of(2026, 4, 19)));
        assertFalse(BusinessDaysUtil.isWeekend(LocalDate.of(2026, 4, 20)));
    }

    @Test
    void isWithinBusinessDayRange_rechazaFueraFinDeSemanaONulo() {
        LocalDate min = LocalDate.of(2026, 4, 13);
        LocalDate max = LocalDate.of(2026, 4, 17);
        assertFalse(BusinessDaysUtil.isWithinBusinessDayRange(
                LocalDate.of(2026, 4, 18), min, max));
        assertFalse(BusinessDaysUtil.isWithinBusinessDayRange(null, min, max));
        assertFalse(BusinessDaysUtil.isWithinBusinessDayRange(min, null, max));
    }

    @Test
    void listBusinessDaysInclusive_excluyeFinesDeSemana() {
        // Lunes 13 — domingo 19: incluye solo lun–vie
        LocalDate from = LocalDate.of(2026, 4, 13);
        LocalDate to = LocalDate.of(2026, 4, 19);
        List<LocalDate> list = BusinessDaysUtil.listBusinessDaysInclusive(from, to);
        assertEquals(5, list.size());
        assertFalse(list.stream().anyMatch(BusinessDaysUtil::isWeekend));
    }

    @Test
    void listBusinessDaysInclusive_vacioSiRangoInvalido() {
        assertTrue(BusinessDaysUtil.listBusinessDaysInclusive(null, LocalDate.now()).isEmpty());
        assertTrue(BusinessDaysUtil.listBusinessDaysInclusive(
                LocalDate.of(2026, 2, 5),
                LocalDate.of(2026, 2, 1)).isEmpty());
    }
}
