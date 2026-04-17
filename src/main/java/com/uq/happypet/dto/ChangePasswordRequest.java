package com.uq.happypet.dto;

import com.uq.happypet.util.PasswordPolicies;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String passwordActual;

    @NotBlank(message = "New password is required")
    @Pattern(regexp = PasswordPolicies.PATTERN, message = PasswordPolicies.MESSAGE)
    private String passwordNueva;

    public String getPasswordActual() {
        return passwordActual;
    }

    public void setPasswordActual(String passwordActual) {
        this.passwordActual = passwordActual;
    }

    public String getPasswordNueva() {
        return passwordNueva;
    }

    public void setPasswordNueva(String passwordNueva) {
        this.passwordNueva = passwordNueva;
    }
}