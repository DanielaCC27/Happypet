package com.uq.happypet.service;

import com.uq.happypet.model.Producto;
import com.uq.happypet.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void deberiaListarProductos() {

        Producto p1 = new Producto("Croquetas", "Comida para perro", 20000, 10, "img");
        Producto p2 = new Producto("Arena", "Arena para gato", 15000, 5, "img");

        when(productoRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Producto> productos = productoService.listarProductos();

        assertEquals(2, productos.size());
        verify(productoRepository).findAll();
    }

    @Test
    void deberiaGuardarProductoNuevo() {

        Producto producto = new Producto("Croquetas", "Comida", 20000, 10, "img");

        productoService.guardarProducto(producto);

        verify(productoRepository).save(producto);
    }

    @Test
    void deberiaObtenerProductoPorId() {

        Producto producto = new Producto("Croquetas", "Comida", 20000, 10, "img");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.obtenerProductoPorId(1L);

        assertNotNull(resultado);
        verify(productoRepository).findById(1L);
    }

    @Test
    void deberiaEliminarProducto() {

        productoService.eliminarProducto(1L);

        verify(productoRepository).deleteById(1L);
    }
}