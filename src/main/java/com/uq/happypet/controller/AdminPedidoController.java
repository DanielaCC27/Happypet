package com.uq.happypet.controller;

import com.uq.happypet.model.Pedido;
import com.uq.happypet.model.PedidoEstado;
import com.uq.happypet.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador encargado de la administración
 * de pedidos desde el panel administrativo.
 */
@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    private final OrderService orderService;

    public AdminPedidoController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Muestra el listado general de pedidos.
     */
    @GetMapping
    public String listar(Model model) {

        model.addAttribute("pedidos", orderService.listarTodosPedidos());

        // Configuración visual de la barra superior
        model.addAttribute("toolbarLayout", "pageTitle");
        model.addAttribute("toolbarActiveSection", "adminPedidos");

        // Información de encabezado para la vista
        model.addAttribute("catalogPageEyebrow", "Administración");
        model.addAttribute("catalogPageTitle", "Pedidos");
        model.addAttribute(
                "catalogPageHint",
                "Listado y gestión de pedidos de la tienda."
        );

        return "admin/pedidos";
    }

    /**
     * Muestra el detalle de un pedido específico.
     */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {

        Pedido pedido = orderService.obtenerPedidoAdmin(id);

        model.addAttribute("pedido", pedido);

        // Lista de estados disponibles para actualización
        model.addAttribute("estados", PedidoEstado.values());

        // Configuración visual de la vista
        model.addAttribute("toolbarLayout", "pageTitle");
        model.addAttribute("toolbarActiveSection", "adminPedidos");

        model.addAttribute("catalogPageEyebrow", "Pedido");
        model.addAttribute("catalogPageTitle", "Pedido #" + id);

        model.addAttribute(
                "catalogPageHint",
                "Cambiar estado, confirmar o eliminar."
        );

        return "admin/pedido-detalle";
    }

    /**
     * Actualiza el estado de un pedido.
     */
    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam PedidoEstado estado,
                                RedirectAttributes redirectAttributes) {

        try {

            orderService.actualizarEstadoAdmin(id, estado);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Estado actualizado."
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/admin/pedidos/" + id;
    }

    /**
     * Confirma un pedido desde el panel administrativo.
     */
    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {

        try {

            orderService.confirmarPedidoAdmin(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Pedido confirmado y notificación enviada."
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/admin/pedidos/" + id;
    }

    /**
     * Elimina un pedido del sistema.
     */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                           RedirectAttributes redirectAttributes) {

        try {

            orderService.eliminarPedidoAdmin(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Pedido eliminado."
            );

            return "redirect:/admin/pedidos";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/admin/pedidos/" + id;
        }
    }
}