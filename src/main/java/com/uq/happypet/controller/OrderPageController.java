package com.uq.happypet.controller;

import com.uq.happypet.model.Pedido;
import com.uq.happypet.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
public class OrderPageController {

    private final OrderService orderService;

    public OrderPageController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/pedidos")
    public String misPedidos(Model model, Principal principal) {
        List<Pedido> pedidos = Collections.emptyList();
        try {
            if (principal != null && principal.getName() != null) {
                pedidos = orderService.listarPedidosUsuario(principal.getName());
            }
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage",
                    "No pudimos cargar tus pedidos en este momento. Intenta nuevamente.");
        }
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("toolbarLayout", "pageTitle");
        model.addAttribute("toolbarActiveSection", "pedidos");
        model.addAttribute("catalogPageEyebrow", "Tu historial");
        model.addAttribute("catalogPageTitle", "Mis pedidos");
        model.addAttribute("catalogPageHint", "Envio, pago y facturacion de cada pedido.");
        return "pedidos/mis-pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String detallePedido(@PathVariable Long id, Model model, Principal principal) {
        Pedido pedido = orderService.obtenerPedidoUsuario(principal.getName(), id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("toolbarLayout", "pageTitle");
        model.addAttribute("toolbarActiveSection", "pedidos");
        model.addAttribute("catalogPageEyebrow", "Pedido");
        model.addAttribute("catalogPageTitle", "Detalle #" + id);
        model.addAttribute("catalogPageHint", "Resumen de tu compra.");
        return "pedidos/pedido-detalle";
    }

    @PostMapping("/pedidos/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelarPedidoUsuario(principal.getName(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Pedido cancelado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/pedidos";
    }
}