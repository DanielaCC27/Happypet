package com.uq.happypet.repository;

import com.uq.happypet.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String nombre, String descripcion);
    List<Producto> findByCategoriaIgnoreCase(String categoria);
    boolean existsByNombreIgnoreCase(String nombre);
}