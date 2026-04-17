package com.uq.happypet.controller;

import com.uq.happypet.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    @Test
    void listarProductos_deberiaRedirigirAlInicio() {
        String vista = productoController.listarProductos(null, null);

        verifyNoInteractions(productoService);
        assertTrue(vista.startsWith("redirect:/home"));
    }
}