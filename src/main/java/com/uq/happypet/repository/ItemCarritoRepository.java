package com.uq.happypet.repository;

import com.uq.happypet.model.Carrito;
import com.uq.happypet.model.ItemCarrito;
import com.uq.happypet.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    Optional<ItemCarrito> findByCarritoAndProducto(Carrito carrito, Producto producto);

    Optional<ItemCarrito> findByIdAndCarrito(Long id, Carrito carrito);
}