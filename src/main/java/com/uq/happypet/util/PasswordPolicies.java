package com.uq.happypet.util;

public final class PasswordPolicies {

    public static final String PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

    public static final String MESSAGE =
            "Password must be 8+ chars with upper, lower, digit and symbol (@#$%^&+=!).";

    private PasswordPolicies() {
    }
}