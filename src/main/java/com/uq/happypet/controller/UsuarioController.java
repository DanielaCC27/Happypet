package com.uq.happypet.controller;

import com.uq.happypet.model.Usuario;
import com.uq.happypet.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.security.Principal;

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

    @GetMapping("/recuperar")
    public String mostrarRecuperarPassword() {
        return "usuarios/recuperar";
    }

    @PostMapping("/verificarCorreo")
    public String verificarCorreo(@RequestParam String correo, Model model) {

        Usuario usuario = usuarioService.buscarPorCorreo(correo);

        if (usuario == null) {
            model.addAttribute("error", "El correo no está registrado");
            return "usuarios/recuperar";
        }

        model.addAttribute("usuarioId", usuario.getId());
        return "usuarios/cambiarPassword";
    }

    @PostMapping("/cambiarPassword")
    public String cambiarPassword(@RequestParam Long id,
                                  @RequestParam String password) {

        usuarioService.actualizarPassword(id, password);

        return "redirect:/login";
    }
}