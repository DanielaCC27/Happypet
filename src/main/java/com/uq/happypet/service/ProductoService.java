package com.uq.happypet.service;

import com.uq.happypet.model.Producto;
import com.uq.happypet.repository.DetallePedidoRepository;
import com.uq.happypet.repository.ItemCarritoRepository;
import com.uq.happypet.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           ItemCarritoRepository itemCarritoRepository,
                           DetallePedidoRepository detallePedidoRepository) {
        this.productoRepository = productoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    /**
     * Obtiene la lista completa de productos registrados.
     */
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    /**
     * Lista productos aplicando filtros opcionales
     * de búsqueda por texto y categoría.
     */
    public List<Producto> listarProductos(String query, String categoria) {

        boolean tieneQuery = query != null && !query.trim().isEmpty();
        boolean tieneCategoria = categoria != null && !categoria.trim().isEmpty();

        // Si no existen filtros, retorna todos los productos
        if (!tieneQuery && !tieneCategoria) {
            return listarProductos();
        }

        // Filtrado únicamente por texto
        if (tieneQuery && !tieneCategoria) {

            String termino = query.trim();

            return productoRepository
                    .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                            termino,
                            termino
                    );
        }

        // Filtrado únicamente por categoría
        if (!tieneQuery) {
            return productoRepository.findByCategoriaIgnoreCase(categoria.trim());
        }

        /*
         * Aplicación de ambos filtros:
         * primero por categoría y luego filtrado por texto en memoria.
         */
        String termino = query.trim().toLowerCase();

        return productoRepository.findByCategoriaIgnoreCase(categoria.trim())
                .stream()
                .filter(producto ->

                        (producto.getNombre() != null &&
                                producto.getNombre().toLowerCase().contains(termino))

                                ||

                        (producto.getDescripcion() != null &&
                                producto.getDescripcion().toLowerCase().contains(termino))
                )
                .toList();
    }

    /**
     * Guarda un producto nuevo o actualiza uno existente.
     */
    public Producto guardarProducto(Producto producto) {

        // Si el producto tiene ID significa que es una edición
        if (producto.getId() != null) {

            Producto productoExistente = productoRepository
                    .findById(producto.getId())
                    .orElse(null);

            if (productoExistente != null) {

                // Actualización de datos del producto existente
                productoExistente.setNombre(producto.getNombre());
                productoExistente.setDescripcion(producto.getDescripcion());
                productoExistente.setPrecio(producto.getPrecio());
                productoExistente.setStock(producto.getStock());
                productoExistente.setCategoria(producto.getCategoria());
                productoExistente.setImagenUrl(producto.getImagenUrl());

                return productoRepository.save(productoExistente);
            }
        }

        // Si no tiene ID se registra un nuevo producto
        return productoRepository.save(producto);
    }

    /**
     * Busca un producto por su identificador.
     */
    public Producto obtenerProductoPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    /**
     * Elimina un producto si no está asociado
     * a pedidos existentes.
     */
    public void eliminarProducto(Long id) {

        // Verifica si el producto existe
        if (!productoRepository.existsById(id)) {
            return;
        }

        // Evita eliminar productos asociados a pedidos
        if (detallePedidoRepository.existsByProducto_Id(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el producto porque consta en uno o más pedidos.");
        }

        // Elimina referencias del carrito antes de eliminar el producto
        itemCarritoRepository.deleteByProducto_Id(id);

        productoRepository.deleteById(id);
    }
}