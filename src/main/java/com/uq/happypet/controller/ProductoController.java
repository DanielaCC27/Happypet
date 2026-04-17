package com.uq.happypet.controller;

import com.uq.happypet.model.Producto;
import com.uq.happypet.service.ProductoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String listarProductos(@RequestParam(value = "q", required = false) String query,
                                  @RequestParam(value = "categoria", required = false) String categoria) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/home");
        if (query != null && !query.isBlank()) {
            uri.queryParam("q", query.trim());
        }
        if (categoria != null && !categoria.isBlank()) {
            uri.queryParam("categoria", categoria.trim());
        }
        return "redirect:" + uri.build().encode().toUriString();
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Producto producto = productoService.obtenerProductoPorId(id);
        if (producto == null) {
            return "redirect:/home";
        }
        model.addAttribute("producto", producto);
        model.addAttribute("toolbarLayout", "search");
        model.addAttribute("q", "");
        model.addAttribute("categoria", null);
        return "productos/producto-detalle";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {

        model.addAttribute("producto", new Producto());
        toolbarCatalogo(model);

        return "productos/formulario";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {

        Producto producto = productoService.obtenerProductoPorId(id);

        if(producto == null){
            return "redirect:/home";
        }

        model.addAttribute("producto", producto);
        toolbarCatalogo(model);

        return "productos/formulario";
    }

    /** Atributos de la barra superior (misma experiencia que el catálogo). */
    private static void toolbarCatalogo(Model model) {
        model.addAttribute("toolbarLayout", "search");
        model.addAttribute("q", "");
        model.addAttribute("categoria", null);
        model.addAttribute("toolbarActiveSection", "home");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardar")
    public String guardarProducto(
            @RequestParam(required = false) Long id,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam double precio,
            @RequestParam int stock,
            @RequestParam String categoria,
            @RequestParam(required = false) String imagenUrl
    ) {

        System.out.println("ID recibido: " + id);

        Producto producto;

        if (id != null) {
            producto = productoService.obtenerProductoPorId(id);
        } else {
            producto = new Producto();
        }

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setCategoria(categoria);
        producto.setImagenUrl(imagenUrl);

        productoService.guardarProducto(producto);

        return "redirect:/home";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {

        productoService.eliminarProducto(id);

        return "redirect:/home";
    }
}