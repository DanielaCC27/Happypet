package com.uq.happypet.controller;

import com.uq.happypet.model.Producto;
import com.uq.happypet.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private Model model;

    @InjectMocks
    private ProductoController productoController;

    @Test
    void deberiaListarProductos() {

        Producto producto = new Producto(
                "Croquetas",
                "Comida para perro",
                20000,
                10,
                "imagen.jpg"
        );

        when(productoService.listarProductos(null, null)).thenReturn(List.of(producto));

        String vista = productoController.listarProductos(null, null, model);

        verify(model).addAttribute("productos", List.of(producto));
        verify(model).addAttribute("q", null);
        verify(model).addAttribute("categoria", null);

        assertEquals("productos/lista", vista);
    }
}