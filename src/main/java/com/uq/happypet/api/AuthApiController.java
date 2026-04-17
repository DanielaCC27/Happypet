package com.uq.happypet.api;

import com.uq.happypet.dto.ApiMessageResponse;
import com.uq.happypet.dto.RegisterRequest;
import com.uq.happypet.dto.ResetPasswordRequest;
import com.uq.happypet.service.UsuarioService;
import com.uq.happypet.service.VerifyEmailOutcome;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UsuarioService usuarioService;

    public AuthApiController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiMessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return switch (usuarioService.register(request)) {
            case SUCCESS -> ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiMessageResponse(
                            "Registro exitoso. Revisa tu correo para verificar tu cuenta."));
            case EMAIL_EXISTS -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiMessageResponse("EMAIL_EXISTS", "Este correo ya esta registrado."));
            case USERNAME_EXISTS -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiMessageResponse("USERNAME_EXISTS", "Este nombre de usuario ya existe."));
        };
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiMessageResponse> verify(@RequestParam(required = false) String token) {
        VerifyEmailOutcome outcome = usuarioService.verificarEmailPorToken(token);
        return switch (outcome) {
            case SUCCESS -> ResponseEntity.ok(new ApiMessageResponse("Cuenta verificada correctamente."));
            case ALREADY_VERIFIED -> ResponseEntity.ok(new ApiMessageResponse("Already verified"));
            case INVALID -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiMessageResponse("INVALID_TOKEN", "El enlace de verificacion no es valido."));
            case EXPIRED -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiMessageResponse("TOKEN_EXPIRED", "El enlace de verificacion ha caducado."));
        };
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiMessageResponse> forgotPassword(@RequestBody(required = false) Map<String, String> body) {
        String email = body != null ? body.get("email") : null;
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiMessageResponse("EMAIL_REQUIRED", "Correo requerido."));
        }
        usuarioService.solicitarRecuperacionPassword(email.trim());
        return ResponseEntity.ok(new ApiMessageResponse(
                "Si el correo esta registrado, recibiras un enlace para restablecer tu contrasena."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiMessageResponse("MISMATCH", "Las contrasenas no coinciden."));
        }
        String resultado = usuarioService.restablecerPassword(request.getToken(), request.getPassword());
        if ("OK".equals(resultado)) {
            return ResponseEntity.ok(new ApiMessageResponse("Contrasena actualizada correctamente"));
        }
        if ("EXPIRADO".equals(resultado) || "INVALIDO".equals(resultado)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiMessageResponse("INVALID_RESET",
                            "El enlace ha expirado o es invalido"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiMessageResponse("ERROR", "No se pudo actualizar la contrasena."));
    }
}