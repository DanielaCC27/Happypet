package com.uq.happypet.util;

public final class PasswordPolicies {

    /**
     * Requiere mayúscula ASCII, minúscula ASCII, dígito y al menos un carácter que no sea letra Unicode ni número Unicode
     * (cualquier símbolo o puntuación, p. ej. * ( ) - _ . ? etc.), no solo un subconjunto fijo.
     */
    public static final String PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\p{L}\\p{N}]).{8,}$";

    public static final String MESSAGE =
            "La contrasena debe tener al menos 8 caracteres e incluir mayuscula, minuscula, numero y un simbolo.";

    private PasswordPolicies() {
    }
}