package com.uq.happypet.repository;

import com.uq.happypet.model.Carrito;
import com.uq.happypet.model.ItemCarrito;
import com.uq.happypet.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio encargado de la gestión
 * de los productos contenidos en el carrito.
 */
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    /**
     * Busca un producto específico dentro de un carrito.
     */
    Optional<ItemCarrito> findByCarritoAndProducto(Carrito carrito, Producto producto);

    /**
     * Busca un item del carrito utilizando
     * su identificador y el carrito asociado.
     */
    Optional<ItemCarrito> findByIdAndCarrito(Long id, Carrito carrito);

    /**
     * Elimina todos los registros de carrito
     * asociados a un producto determinado.
     *
     * Este método se utiliza antes de eliminar
     * un producto del sistema.
     */
    void deleteByProducto_Id(Long productoId);
}