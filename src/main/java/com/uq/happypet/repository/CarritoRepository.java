package com.uq.happypet.repository;

import com.uq.happypet.model.Carrito;
import com.uq.happypet.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio encargado de las operaciones de acceso
 * a datos relacionadas con la entidad Carrito.
 */
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    /**
     * Obtiene todos los carritos asociados a un usuario.
     */
    List<Carrito> findByUsuario(Usuario usuario);

    /**
     * Busca el carrito activo de un usuario.
     */
    Optional<Carrito> findByUsuarioAndActivoTrue(Usuario usuario);

    /**
     * Obtiene el carrito activo junto con sus items y productos asociados.
     *
     * Se utiliza LEFT JOIN FETCH para evitar problemas de carga perezosa
     * (LazyInitializationException) al acceder a los elementos del carrito.
     */
    @Query("SELECT c FROM Carrito c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.producto "
            + "WHERE c.usuario = :usuario AND c.activo = true")
    Optional<Carrito> findActiveCartWithItems(@Param("usuario") Usuario usuario);
}