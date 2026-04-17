package com.uq.happypet.repository;

import com.uq.happypet.model.Pedido;
import com.uq.happypet.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuario(Usuario usuario);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    List<Pedido> findByUsuarioOrderByFechaDesc(Usuario usuario);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    List<Pedido> findAllByOrderByFechaDesc();

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findFetchedById(@Param("id") Long id);
}