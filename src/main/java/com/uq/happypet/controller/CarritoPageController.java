package com.uq.happypet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CarritoPageController {

    @GetMapping("/carrito")
    public String carrito(Model model) {
        model.addAttribute("toolbarLayout", "search");
        model.addAttribute("toolbarActiveSection", "cart");
        model.addAttribute("q", "");
        model.addAttribute("categoria", null);
        return "carrito";
    }
}