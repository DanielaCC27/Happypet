package com.uq.happypet.service;

import com.uq.happypet.model.Producto;
import com.uq.happypet.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarProductos(String query, String categoria) {
        boolean tieneQuery = query != null && !query.trim().isEmpty();
        boolean tieneCategoria = categoria != null && !categoria.trim().isEmpty();

        if (!tieneQuery && !tieneCategoria) {
            return listarProductos();
        }

        if (tieneQuery && !tieneCategoria) {
            String termino = query.trim();
            return productoRepository
                    .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(termino, termino);
        }

        if (!tieneQuery) {
            return productoRepository.findByCategoriaIgnoreCase(categoria.trim());
        }

        // Ambos filtros: primero por categoría y luego por texto en memoria
        String termino = query.trim().toLowerCase();
        return productoRepository.findByCategoriaIgnoreCase(categoria.trim())
                .stream()
                .filter(p ->
                        (p.getNombre() != null && p.getNombre().toLowerCase().contains(termino)) ||
                        (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(termino))
                )
                .toList();
    }

    public Producto guardarProducto(Producto producto) {

        // Si el producto tiene ID significa que es una edición
        if (producto.getId() != null) {

            Producto productoExistente = productoRepository
                    .findById(producto.getId())
                    .orElse(null);

            if (productoExistente != null) {
                productoExistente.setNombre(producto.getNombre());
                productoExistente.setDescripcion(producto.getDescripcion());
                productoExistente.setPrecio(producto.getPrecio());
                productoExistente.setStock(producto.getStock());
                productoExistente.setCategoria(producto.getCategoria());
                productoExistente.setImagenUrl(producto.getImagenUrl());

                return productoRepository.save(productoExistente);
            }
        }

        // Si no tiene ID es un producto nuevo
        return productoRepository.save(producto);
    }

    public Producto obtenerProductoPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }
}