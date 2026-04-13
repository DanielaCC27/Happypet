package com.uq.happypet.controller;

import com.uq.happypet.dto.RegisterRequest;
import com.uq.happypet.exception.DuplicateAccountException;
import com.uq.happypet.model.Usuario;
import com.uq.happypet.service.AuthService;
import com.uq.happypet.service.UsuarioService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final Validator validator;

    public UsuarioController(UsuarioService usuarioService, AuthService authService, Validator validator) {
        this.usuarioService = usuarioService;
        this.authService = authService;
        this.validator = validator;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "usuarios/register";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        RegisterRequest request = new RegisterRequest(nombre, correo, username, password);
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            model.addAttribute("error", violations.iterator().next().getMessage());
            return "usuarios/register";
        }

        try {
            authService.register(request);
        } catch (DuplicateAccountException e) {
            model.addAttribute("error", e.getMessage());
            return "usuarios/register";
        } catch (MailException e) {
            model.addAttribute(
                    "error",
                    "No se pudo enviar el correo de verificación. Comprueba la configuración SMTP o inténtalo más tarde.");
            return "usuarios/register";
        }

        model.addAttribute(
                "success",
                "Cuenta creada. Revisa tu correo y haz clic en el enlace de verificación antes de iniciar sesión.");
        return "usuarios/register";
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal) {

        String username = principal.getName();

        Usuario usuario = usuarioService.buscarPorUsername(username);

        model.addAttribute("usuario", usuario);

        return "usuarios/perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarUsuario(@RequestParam Long id, @RequestParam String nombre, @RequestParam String correo) {

        usuarioService.actualizar(id, nombre, correo);

        return "redirect:/usuarios/perfil";
    }

    @GetMapping("/recuperar")
    public String mostrarRecuperarPassword() {
        return "usuarios/recuperar";
    }
}
