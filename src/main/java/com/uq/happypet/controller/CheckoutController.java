package com.uq.happypet.controller;

import com.uq.happypet.dto.CartResponse;
import com.uq.happypet.dto.CheckoutFacturacionForm;
import com.uq.happypet.dto.CheckoutRequest;
import com.uq.happypet.dto.CheckoutSession;
import com.uq.happypet.dto.OrderResponse;
import com.uq.happypet.model.HorarioEntrega;
import com.uq.happypet.model.MetodoPago;
import com.uq.happypet.model.Usuario;
import com.uq.happypet.service.CartService;
import com.uq.happypet.service.ColombiaUbicacionesService;
import com.uq.happypet.service.OrderService;
import com.uq.happypet.service.UsuarioService;
import com.uq.happypet.util.DeliveryScheduleUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class CheckoutController {

    /** Nuevo nombre de atributo para no reutilizar objetos de sesion corruptos de versiones viejas. */
    public static final String SESSION_CHECKOUT = "checkoutWizardV2";

    private final OrderService orderService;
    private final UsuarioService usuarioService;
    private final CartService cartService;
    private final ColombiaUbicacionesService colombiaUbicacionesService;

    public CheckoutController(OrderService orderService,
                                UsuarioService usuarioService,
                                CartService cartService,
                                ColombiaUbicacionesService colombiaUbicacionesService) {
        this.orderService = orderService;
        this.usuarioService = usuarioService;
        this.cartService = cartService;
        this.colombiaUbicacionesService = colombiaUbicacionesService;
    }

    /**
     * Lista departamento/municipios (Colombia) en JSON UTF-8.
     * Evita depender del bloque incrustado en HTML, que a veces corrompe el parseo en el navegador.
     */
    @GetMapping(value = "/api/ubicaciones/colombia", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ResponseBody
    public String colombiaUbicacionesApi() {
        return colombiaUbicacionesService.getJsonIncrustado();
    }

    @GetMapping("/checkout")
    public String checkoutRoot() {
        return "redirect:/checkout/entrega";
    }

    @GetMapping("/checkout/entrega")
    public String entregaGet(Principal principal, HttpSession session, Model model) {
        CartResponse cart = cartService.getCart(principal.getName());
        if (cart.getItems().isEmpty()) {
            return "redirect:/carrito";
        }
        CheckoutSession cs = getOrCreateCheckout(session);
        addToolbar(model);
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutSession", cs);
        addFechasModel(model);
        model.addAttribute("horarioEntregaFijo", HorarioEntrega.JORNADA_8_18);
        model.addAttribute("step", 1);
        return "checkout-entrega";
    }

    @PostMapping("/checkout/entrega")
    public String entregaPost(Principal principal,
                              @RequestParam LocalDate fechaEntregaPreferida,
                              HttpSession session,
                              RedirectAttributes ra) {
        CartResponse cart = cartService.getCart(principal.getName());
        if (cart.getItems().isEmpty()) {
            return "redirect:/carrito";
        }
        LocalDate hoy = LocalDate.now();
        LocalDate min = DeliveryScheduleUtil.fechaMinima(hoy);
        LocalDate max = DeliveryScheduleUtil.fechaMaxima(hoy);
        if (!DeliveryScheduleUtil.fechaElegible(fechaEntregaPreferida, hoy)) {
            ra.addFlashAttribute("errorCheckout",
                    "El dia elegido debe estar entre " + min + " y " + max + " (inclusive).");
            return "redirect:/checkout/entrega";
        }
        CheckoutSession cs = getOrCreateCheckout(session);
        cs.setFechaEntregaPreferida(fechaEntregaPreferida);
        cs.setHorarioEntrega(HorarioEntrega.JORNADA_8_18);
        session.setAttribute(SESSION_CHECKOUT, cs);
        return "redirect:/checkout/pago";
    }

    @GetMapping("/checkout/resumen")
    public String resumenGet() {
        return "redirect:/checkout/pago";
    }

    @PostMapping("/checkout/resumen")
    public String resumenPost() {
        return "redirect:/checkout/pago";
    }

    @GetMapping("/checkout/pago")
    public String pagoGet(Principal principal, HttpSession session, Model model) {
        CartResponse cart = cartService.getCart(principal.getName());
        if (cart.getItems().isEmpty()) {
            session.removeAttribute(SESSION_CHECKOUT);
            return "redirect:/carrito";
        }
        CheckoutSession cs = requireCheckoutWithEntrega(session);
        if (cs == null) {
            return "redirect:/checkout/entrega";
        }
        addToolbar(model);
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutSession", cs);
        addFechasModel(model);
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("step", 2);
        return "checkout-pago";
    }

    @PostMapping("/checkout/pago")
    public String pagoPost(@RequestParam MetodoPago metodoPago, HttpSession session) {
        CheckoutSession cs = requireCheckoutWithEntrega(session);
        if (cs == null) {
            return "redirect:/checkout/entrega";
        }
        cs.setMetodoPago(metodoPago);
        session.setAttribute(SESSION_CHECKOUT, cs);
        return "redirect:/checkout/facturacion";
    }

    @GetMapping("/checkout/facturacion")
    public String facturacionGet(Principal principal, HttpSession session, Model model) {
        CartResponse cart = cartService.getCart(principal.getName());
        if (cart.getItems().isEmpty()) {
            session.removeAttribute(SESSION_CHECKOUT);
            return "redirect:/carrito";
        }
        CheckoutSession cs = requireCheckoutWithEntrega(session);
        if (cs == null) {
            return "redirect:/checkout/entrega";
        }
        if (!cs.hasPago()) {
            return "redirect:/checkout/pago";
        }
        if (!model.containsAttribute("facturacion")) {
            CheckoutFacturacionForm form = new CheckoutFacturacionForm();
            Usuario u = usuarioService.buscarPorUsername(principal.getName());
            if (u != null) {
                form.setFacturacionNombre(u.getNombre() != null ? u.getNombre() : "");
                form.setFacturacionEmail(u.getCorreo() != null ? u.getCorreo() : "");
            }
            model.addAttribute("facturacion", form);
        }
        addToolbar(model);
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutSession", cs);
        addFechasModel(model);
        model.addAttribute("tiposDocumento", tiposDocumento());
        model.addAttribute("departamentosColombia", colombiaUbicacionesService.listarDepartamentos());
        model.addAttribute("colombiaUbicacionesJson", colombiaUbicacionesService.getJsonIncrustado());
        model.addAttribute("step", 3);
        return "checkout-facturacion";
    }

    @PostMapping("/checkout/facturacion")
    public String facturacionPost(@Valid @ModelAttribute("facturacion") CheckoutFacturacionForm form,
                                  BindingResult binding,
                                  Principal principal,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes ra) {
        CartResponse cart = cartService.getCart(principal.getName());
        if (cart.getItems().isEmpty()) {
            session.removeAttribute(SESSION_CHECKOUT);
            return "redirect:/carrito";
        }
        CheckoutSession cs = requireCheckoutWithEntrega(session);
        if (cs == null) {
            return "redirect:/checkout/entrega";
        }
        if (!cs.hasPago()) {
            return "redirect:/checkout/pago";
        }
        if (binding.hasErrors()) {
            facturacionModelCommons(model, cart, cs);
            return "checkout-facturacion";
        }
        if (!colombiaUbicacionesService.esParValido(form.getDepartamentoEnvio(), form.getMunicipioEnvio())) {
            binding.rejectValue("municipioEnvio", "ubicacion.invalida",
                    "Selecciona un municipio que corresponda al departamento.");
        }
        if (binding.hasErrors()) {
            facturacionModelCommons(model, cart, cs);
            return "checkout-facturacion";
        }
        String direccionCompleta = colombiaUbicacionesService.construirDireccionCompleta(
                form.getDepartamentoEnvio(),
                form.getMunicipioEnvio(),
                form.getDireccionEnvio());
        CheckoutRequest req = new CheckoutRequest();
        req.setDireccionEnvio(direccionCompleta);
        req.setMetodoPago(cs.getMetodoPago());
        req.setFechaEntregaPreferida(cs.getFechaEntregaPreferida());
        req.setHorarioEntrega(cs.getHorarioEntrega());
        req.setFacturacionTipoDocumento(form.getFacturacionTipoDocumento().trim());
        req.setFacturacionDocumento(form.getFacturacionDocumento().trim());
        req.setFacturacionNombre(form.getFacturacionNombre().trim());
        req.setFacturacionApellidos(form.getFacturacionApellidos().trim());
        req.setFacturacionDireccion(direccionCompleta);
        req.setFacturacionEmail(form.getFacturacionEmail().trim());
        try {
            OrderResponse order = orderService.checkout(principal.getName(), req);
            session.removeAttribute(SESSION_CHECKOUT);
            ra.addAttribute("pedidoId", order.getId());
            return "redirect:/checkout/exito";
        } catch (RuntimeException e) {
            model.addAttribute("errorCheckout", e.getMessage());
            facturacionModelCommons(model, cart, cs);
            return "checkout-facturacion";
        }
    }

    private void facturacionModelCommons(Model model, CartResponse cart, CheckoutSession cs) {
        addToolbar(model);
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutSession", cs);
        addFechasModel(model);
        model.addAttribute("tiposDocumento", tiposDocumento());
        model.addAttribute("departamentosColombia", colombiaUbicacionesService.listarDepartamentos());
        model.addAttribute("colombiaUbicacionesJson", colombiaUbicacionesService.getJsonIncrustado());
        model.addAttribute("step", 3);
    }

    @GetMapping("/checkout/exito")
    public String exito(@RequestParam Long pedidoId, Model model) {
        model.addAttribute("pedidoId", pedidoId);
        addToolbar(model);
        return "checkout-exito";
    }

    private void addFechasModel(Model model) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaMin = DeliveryScheduleUtil.fechaMinima(hoy);
        LocalDate fechaMax = DeliveryScheduleUtil.fechaMaxima(hoy);
        model.addAttribute("fechaMin", fechaMin);
        model.addAttribute("fechaMax", fechaMax);
        model.addAttribute("fechasElegibles", DeliveryScheduleUtil.listarDiasConsecutivos(fechaMin, fechaMax));
    }

    private void addToolbar(Model model) {
        model.addAttribute("toolbarActiveSection", "checkout");
        model.addAttribute("toolbarLayout", "pageTitle");
        model.addAttribute("catalogPageTitle", "Finalizar compra");
    }

    private CheckoutSession getOrCreateCheckout(HttpSession session) {
        Object o = session.getAttribute(SESSION_CHECKOUT);
        if (o instanceof CheckoutSession s) {
            migraHorarioLegacy(s);
            return s;
        }
        CheckoutSession created = new CheckoutSession();
        session.setAttribute(SESSION_CHECKOUT, created);
        return created;
    }

    private CheckoutSession requireCheckoutWithEntrega(HttpSession session) {
        Object o = session.getAttribute(SESSION_CHECKOUT);
        if (o instanceof CheckoutSession s && s.hasEntrega()) {
            migraHorarioLegacy(s);
            return s;
        }
        return null;
    }

    /** Unifica franja 8-18 si venia de sesiones o datos previos con slots viejos. */
    private static void migraHorarioLegacy(CheckoutSession s) {
        if (s.getHorarioEntrega() != null && s.getHorarioEntrega() != HorarioEntrega.JORNADA_8_18) {
            s.setHorarioEntrega(HorarioEntrega.JORNADA_8_18);
        }
    }

    private static List<String> tiposDocumento() {
        return Arrays.asList("CC", "CE", "NIT", "Pasaporte", "Otro");
    }
}
