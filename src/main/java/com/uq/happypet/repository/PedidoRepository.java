package com.uq.happypet.repository;

import com.uq.happypet.model.Pedido;
import com.uq.happypet.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio encargado de las operaciones
 * de acceso a datos relacionadas con los pedidos.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Obtiene todos los pedidos asociados a un usuario.
     */
    List<Pedido> findByUsuario(Usuario usuario);

    /**
     * Obtiene los pedidos de un usuario ordenados
     * por fecha descendente.
     *
     * Se utiliza EntityGraph para cargar de manera anticipada
     * los detalles y productos asociados al pedido.
     */
    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    List<Pedido> findByUsuarioOrderByFechaDesc(Usuario usuario);

    /**
     * Obtiene todos los pedidos registrados en el sistema,
     * ordenados por fecha descendente.
     *
     * Incluye información del usuario y detalles del pedido.
     */
    @EntityGraph(attributePaths = {
            "detalles",
            "detalles.producto",
            "usuario"
    })
    List<Pedido> findAllByOrderByFechaDesc();

    /**
     * Busca un pedido específico incluyendo
     * toda la información relacionada.
     */
    @EntityGraph(attributePaths = {
            "detalles",
            "detalles.producto",
            "usuario"
    })
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findFetchedById(@Param("id") Long id);
}