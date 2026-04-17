package com.uq.happypet.repository;

import com.uq.happypet.model.Carrito;
import com.uq.happypet.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByUsuario(Usuario usuario);

    Optional<Carrito> findByUsuarioAndActivoTrue(Usuario usuario);

    @Query("SELECT c FROM Carrito c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.producto "
            + "WHERE c.usuario = :usuario AND c.activo = true")
    Optional<Carrito> findActiveCartWithItems(@Param("usuario") Usuario usuario);
}