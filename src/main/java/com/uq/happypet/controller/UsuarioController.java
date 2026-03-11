package com.uq.happypet.controller;

import com.uq.happypet.model.Usuario;
import com.uq.happypet.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro() {
        return "usuarios/register";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(@RequestParam String nombre,
                                   @RequestParam String correo,
                                   @RequestParam String username,
                                   @RequestParam String password,
                                   Model model) {

        Usuario usuario = new Usuario(nombre, correo, username, password, "ROLE_CLIENTE");

        String resultado = usuarioService.registrar(usuario);

        if (resultado.equals("USERNAME_EXISTE")) {
            model.addAttribute("error", "El nombre de usuario ya existe.");
            return "usuarios/register";
        }

        if (resultado.equals("CORREO_EXISTE")) {
            model.addAttribute("error", "El correo ya está registrado.");
            return "usuarios/register";
        }

        model.addAttribute("success", "Usuario registrado correctamente.");
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
    public String actualizarUsuario(@RequestParam Long id,
                                    @RequestParam String nombre,
                                    @RequestParam String correo) {

        usuarioService.actualizar(id, nombre, correo);

        return "redirect:/usuarios/perfil";
    }

    private static final String SESSION_ATTR_RECUPERAR_USUARIO_ID = "recuperarPasswordUsuarioId";

    @GetMapping("/recuperar")
    public String mostrarRecuperarPassword() {
        return "usuarios/recuperar";
    }

    @PostMapping("/verificarCorreo")
    public Object verificarCorreo(@RequestParam String correo, Model model,
                                  HttpServletRequest request, HttpSession session) {
        Usuario usuario = usuarioService.buscarPorCorreo(correo);
        boolean ajax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        if (usuario == null) {
            if (ajax) {
                return ResponseEntity.ok(Map.of("success", false, "error", "El correo no está registrado"));
            }
            model.addAttribute("error", "El correo no está registrado");
            return "usuarios/recuperar";
        }

        if (ajax) {
            session.setAttribute(SESSION_ATTR_RECUPERAR_USUARIO_ID, usuario.getId());
            String redirectUrl = request.getContextPath() + "/usuarios/cambiarPassword";
            return ResponseEntity.ok(Map.of("success", true, "redirectUrl", redirectUrl));
        }
        model.addAttribute("usuarioId", usuario.getId());
        return "usuarios/cambiarPassword";
    }

    @GetMapping("/cambiarPassword")
    public String mostrarCambiarPassword(Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute(SESSION_ATTR_RECUPERAR_USUARIO_ID);
        if (usuarioId == null) {
            return "redirect:/usuarios/recuperar";
        }
        model.addAttribute("usuarioId", usuarioId);
        return "usuarios/cambiarPassword";
    }

    @PostMapping("/cambiarPassword")
    public String cambiarPassword(@RequestParam Long id,
                                  @RequestParam String password,
                                  HttpSession session) {
        usuarioService.actualizarPassword(id, password);
        session.removeAttribute(SESSION_ATTR_RECUPERAR_USUARIO_ID);
        return "redirect:/login";
    }
}