package com.uq.happypet.model;

public enum HorarioEntrega {
    /**
     * Valores legacy: deben existir para deserializar sesiones antiguas y filas en BD.
     * El flujo actual solo usa {@link #JORNADA_8_18}.
     */
    @Deprecated
    MANANA_9_12("9:00 - 12:00"),
    @Deprecated
    TARDE_14_18("14:00 - 18:00"),
    @Deprecated
    NOCHE_18_20("18:00 - 20:00"),
    JORNADA_8_18("8:00 - 18:00");

    private final String etiqueta;

    HorarioEntrega(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
