package com.uq.happypet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador encargado de mostrar
 * la vista principal del carrito de compras.
 */
@Controller
public class CarritoPageController {

    /**
     * Carga la página del carrito y configura
     * los atributos necesarios para la interfaz.
     */
    @GetMapping("/carrito")
    public String carrito(Model model) {

        // Configuración de la barra superior
        model.addAttribute("toolbarLayout", "search");
        model.addAttribute("toolbarActiveSection", "cart");

        // Valores iniciales para filtros y búsqueda
        model.addAttribute("q", "");
        model.addAttribute("categoria", null);

        return "carrito";
    }
}