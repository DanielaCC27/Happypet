package com.uq.happypet.controller;

import com.uq.happypet.dto.ForgotPasswordRequest;
import com.uq.happypet.dto.MessageResponse;
import com.uq.happypet.dto.RegisterRequest;
import com.uq.happypet.dto.ResetPasswordRequest;
import com.uq.happypet.exception.InvalidTokenException;
import com.uq.happypet.exception.TokenExpiredException;
import com.uq.happypet.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest body) {
        authService.register(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new MessageResponse(
                                "Registro recibido. Revisa tu correo para verificar la cuenta."));
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token) {
        try {
            authService.verifyToken(token);
            return "redirect:/login?verified=true";
        } catch (InvalidTokenException | TokenExpiredException e) {
            return "redirect:/login?error=invalid_token";
        }
    }

    @PostMapping(value = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest body) {
        authService.requestPasswordReset(body.email());
        return ResponseEntity.ok(
                new MessageResponse(
                        "Si el correo está registrado, recibirás instrucciones para restablecer la contraseña."));
    }

    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<MessageResponse> resetPasswordJson(@Valid @RequestBody ResetPasswordRequest body) {
        authService.resetPassword(body.token(), body.newPassword());
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada. Ya puedes iniciar sesión."));
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("error", "Enlace no válido o incompleto.");
        } else {
            model.addAttribute("token", token);
        }
        return "auth/reset-password";
    }

    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String resetPasswordForm(
            @RequestParam String token,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            if (password == null || password.length() < 6) {
                redirectAttributes.addFlashAttribute(
                        "error", "La contraseña debe tener al menos 6 caracteres.");
                return "redirect:/auth/reset-password?token=" + token;
            }
            authService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("success", "Contraseña actualizada. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (InvalidTokenException | TokenExpiredException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }
}
