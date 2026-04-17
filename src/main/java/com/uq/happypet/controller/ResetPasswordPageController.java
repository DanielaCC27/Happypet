package com.uq.happypet.controller;

import com.uq.happypet.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResetPasswordPageController {

    private final UsuarioService usuarioService;

    public ResetPasswordPageController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/reset-password")
    public String showResetPage(@RequestParam(required = false) String token, Model model) {
        String t = token != null ? token.trim() : "";
        model.addAttribute("token", t);
        boolean valid = !t.isEmpty() && usuarioService.tokenRestablecimientoValido(t);
        model.addAttribute("tokenValid", valid);
        return "reset-password";
    }
}
