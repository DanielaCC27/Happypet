package com.uq.happypet.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class DeliveryScheduleUtil {

    public static final int ENTREGA_MIN_DIAS_DESDE_HOY = 4;
    public static final int ENTREGA_MAX_DIAS_DESDE_HOY = 6;

    private DeliveryScheduleUtil() {
    }

    public static LocalDate fechaMinima(LocalDate hoy) {
        return hoy.plusDays(ENTREGA_MIN_DIAS_DESDE_HOY);
    }

    public static LocalDate fechaMaxima(LocalDate hoy) {
        return hoy.plusDays(ENTREGA_MAX_DIAS_DESDE_HOY);
    }

    public static boolean fechaElegible(LocalDate elegida, LocalDate hoy) {
        if (elegida == null || hoy == null) {
            return false;
        }
        LocalDate min = fechaMinima(hoy);
        LocalDate max = fechaMaxima(hoy);
        return !elegida.isBefore(min) && !elegida.isAfter(max);
    }

    public static List<LocalDate> listarDiasConsecutivos(LocalDate desde, LocalDate hasta) {
        List<LocalDate> out = new ArrayList<>();
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            return out;
        }
        LocalDate d = desde;
        while (!d.isAfter(hasta)) {
            out.add(d);
            d = d.plusDays(1);
        }
        return out;
    }
}
