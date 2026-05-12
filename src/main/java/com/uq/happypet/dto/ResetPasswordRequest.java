package com.uq.happypet.dto;

import com.uq.happypet.util.PasswordPolicies;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "La contrasena es obligatoria")
    @Pattern(regexp = PasswordPolicies.PATTERN, message = PasswordPolicies.MESSAGE)
    @Size(max = 128)
    private String password;

    @NotBlank(message = "Confirma la contrasena")
    private String confirmPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}