package com.uq.happypet.controller;

import com.uq.happypet.dto.ChangePasswordRequest;
import com.uq.happypet.dto.RegisterRequest;
import com.uq.happypet.model.Usuario;
import com.uq.happypet.service.RegistrationOutcome;
import com.uq.happypet.service.UsuarioService;
import com.uq.happypet.service.VerifyEmailOutcome;
import com.uq.happypet.util.PasswordPolicies;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Set;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final Validator validator;

    public UsuarioController(UsuarioService usuarioService, Validator validator) {
        this.usuarioService = usuarioService;
        this.validator = validator;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        toolbarCatalogo(model);
        return "usuarios/register";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(@RequestParam String nombre,
                                   @RequestParam String correo,
                                   @RequestParam String username,
                                   @RequestParam String password,
                                   Model model) {
        toolbarCatalogo(model);

        RegisterRequest request = new RegisterRequest(nombre, correo, username, password);
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            model.addAttribute("error", violations.iterator().next().getMessage());
            return "usuarios/register";
        }

        RegistrationOutcome outcome = usuarioService.register(request);

        if (outcome == RegistrationOutcome.USERNAME_EXISTS) {
            model.addAttribute("error", "El nombre de usuario ya existe.");
            return "usuarios/register";
        }

        if (outcome == RegistrationOutcome.EMAIL_EXISTS) {
            model.addAttribute("error", "Este correo ya est\u00e1 registrado.");
            return "usuarios/register";
        }

        model.addAttribute("success",
                "Cuenta creada. Revisa tu correo y abre el enlace para verificar tu cuenta antes de iniciar sesi\u00f3n.");
        return "usuarios/register";
    }

    private void toolbarCatalogo(Model model) {
        model.addAttribute("toolbarLayout", "search");
        model.addAttribute("q", "");
        model.addAttribute("categoria", null);
    }

    @GetMapping("/verificar-email")
    public String verificarEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        VerifyEmailOutcome resultado = usuarioService.verificarEmailPorToken(token);
        if (resultado == VerifyEmailOutcome.SUCCESS) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Correo verificado. Ya puedes iniciar sesi\u00f3n.");
        } else if (resultado == VerifyEmailOutcome.ALREADY_VERIFIED) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tu cuenta ya estaba verificada. Puedes iniciar sesi\u00f3n.");
        } else if (resultado == VerifyEmailOutcome.EXPIRED) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "El enlace ha caducado. Solicita un nuevo registro o contacta soporte.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "El enlace no es v\u00e1lido.");
        }
        return "redirect:/login";
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal) {

        String username = principal.getName();

        Usuario usuario = usuarioService.buscarPorUsername(username);

        model.addAttribute("usuario", usuario);
        model.addAttribute("toolbarLayout", "search");
        model.addAttribute("q", "");
        model.addAttribute("categoria", null);

        return "usuarios/perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarUsuario(@RequestParam Long id,
                                    @RequestParam String nombre,
                                    @RequestParam String correo) {

        usuarioService.actualizar(id, nombre, correo);

        return "redirect:/usuarios/perfil";
    }

    @PostMapping("/eliminar-cuenta")
    public String eliminarCuenta(@RequestParam Long id,
                                 Principal principal,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        usuarioService.eliminarCuenta(id, principal.getName());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?cuentaEliminada";
    }

    @GetMapping("/recuperar")
    public String mostrarRecuperarPassword() {
        return "usuarios/recuperar";
    }

    @PostMapping("/solicitar-recuperacion")
    public String solicitarRecuperacion(@RequestParam String correo,
                                        RedirectAttributes redirectAttributes) {
        usuarioService.solicitarRecuperacionPassword(correo);
        redirectAttributes.addFlashAttribute("infoMessage",
                "Si el correo est\u00e1 registrado, recibir\u00e1s un enlace para restablecer tu contrase\u00f1a.");
        return "redirect:/usuarios/recuperar";
    }

    @GetMapping("/restablecer-password")
    public String mostrarRestablecerPassword(@RequestParam(required = false) String token) {
        if (token == null || token.isBlank()) {
            return "redirect:/reset-password";
        }
        return "redirect:/reset-password?token=" + URLEncoder.encode(token.trim(), StandardCharsets.UTF_8);
    }

    @PostMapping("/restablecer-password")
    public String restablecerPassword(@RequestParam String token,
                                     @RequestParam String password,
                                     RedirectAttributes redirectAttributes) {
        String resultado = usuarioService.restablecerPassword(token, password);
        if ("OK".equals(resultado)) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Contrase\u00f1a actualizada. Ya puedes iniciar sesi\u00f3n.");
            return "redirect:/login";
        }
        if ("DEBIL".equals(resultado)) {
            redirectAttributes.addFlashAttribute("errorMessage", PasswordPolicies.MESSAGE);
            return "redirect:/reset-password?token=" + URLEncoder.encode(token.trim(), StandardCharsets.UTF_8);
        }
        if ("EXPIRADO".equals(resultado)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "El enlace ha caducado. Solicita uno nuevo.");
            return "redirect:/usuarios/recuperar";
        }
        redirectAttributes.addFlashAttribute("errorMessage",
                "No se pudo restablecer la contrase\u00f1a.");
        return "redirect:/usuarios/recuperar";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String passwordActual,
                                  @RequestParam String passwordNueva,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setPasswordActual(passwordActual);
        req.setPasswordNueva(passwordNueva);
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            redirectAttributes.addFlashAttribute("passwordError",
                    violations.iterator().next().getMessage());
            return "redirect:/usuarios/perfil";
        }
        try {
            usuarioService.cambiarPassword(principal.getName(), passwordActual, passwordNueva);
            redirectAttributes.addFlashAttribute("successMessage", "Contraseña actualizada correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/usuarios/perfil";
    }
}