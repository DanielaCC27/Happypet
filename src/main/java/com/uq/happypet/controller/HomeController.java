package com.uq.happypet.controller;

import com.uq.happypet.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

@Controller
public class HomeController {

    private final ProductoService productoService;

    public HomeController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping({"/", "/home"})
    public String home(@RequestParam(value = "q", required = false) String query,
                       @RequestParam(value = "categoria", required = false) String categoria,
                       Model model) {
        var lista = productoService.listarProductos(query, categoria);
        model.addAttribute("productos", lista != null ? lista : Collections.emptyList());
        model.addAttribute("q", query);
        model.addAttribute("categoria", categoria);
        model.addAttribute("toolbarLayout", "search");
        model.addAttribute("toolbarActiveSection", "home");
        return "home";
    }
}