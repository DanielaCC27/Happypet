package com.uq.happypet.controller;

import com.uq.happypet.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

/**
 * Controlador principal encargado
 * de cargar la página de inicio.
 */
@Controller
public class HomeController {

    private final ProductoService productoService;

    public HomeController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Muestra la página principal con el listado
     * de productos disponibles.
     *
     * Permite aplicar filtros opcionales
     * por búsqueda y categoría.
     */
    @GetMapping({"/", "/home"})
    public String home(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "categoria", required = false) String categoria,
            Model model
    ) {

        // Obtiene productos aplicando filtros opcionales
        var lista = productoService.listarProductos(query, categoria);

        // Evita valores nulos en la vista
        model.addAttribute(
                "productos",
                lista != null ? lista : Collections.emptyList()
        );

        // Parámetros de búsqueda utilizados en la vista
        model.addAttribute("q", query);
        model.addAttribute("categoria", categoria);

        // Configuración visual de la barra superior
        model.addAttribute("toolbarLayout", "search");
        model.addAttribute("toolbarActiveSection", "home");

        return "home";
    }
}