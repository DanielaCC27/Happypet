package com.uq.happypet.controller;

import com.uq.happypet.model.Pedido;
import com.uq.happypet.model.PedidoEstado;
import com.uq.happypet.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    private final OrderService orderService;

    public AdminPedidoController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", orderService.listarTodosPedidos());
        model.addAttribute("toolbarLayout", "pageTitle");
        model.addAttribute("toolbarActiveSection", "adminPedidos");
        model.addAttribute("catalogPageEyebrow", "Administraci\u00f3n");
        model.addAttribute("catalogPageTitle", "Pedidos");
        model.addAttribute("catalogPageHint", "Listado y gesti\u00f3n de pedidos de la tienda.");
        return "admin/pedidos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Pedido p = orderService.obtenerPedidoAdmin(id);
        model.addAttribute("pedido", p);
        model.addAttribute("estados", PedidoEstado.values());
        model.addAttribute("toolbarLayout", "pageTitle");
        model.addAttribute("toolbarActiveSection", "adminPedidos");
        model.addAttribute("catalogPageEyebrow", "Pedido");
        model.addAttribute("catalogPageTitle", "Pedido #" + id);
        model.addAttribute("catalogPageHint", "Cambiar estado, confirmar o eliminar.");
        return "admin/pedido-detalle";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam PedidoEstado estado,
                                RedirectAttributes redirectAttributes) {
        try {
            orderService.actualizarEstadoAdmin(id, estado);
            redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.confirmarPedidoAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Pedido confirmado y notificaci\u00f3n enviada.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.eliminarPedidoAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pedido eliminado.");
            return "redirect:/admin/pedidos";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/pedidos/" + id;
        }
    }
}