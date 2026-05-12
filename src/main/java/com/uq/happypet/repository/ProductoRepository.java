package com.uq.happypet.repository;

import com.uq.happypet.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio encargado de las operaciones
 * relacionadas con la entidad Producto.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos por coincidencia en nombre o descripción,
     * ignorando diferencias entre mayúsculas y minúsculas.
     */
    List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
            String nombre,
            String descripcion
    );

    /**
     * Obtiene productos pertenecientes a una categoría específica.
     */
    List<Producto> findByCategoriaIgnoreCase(String categoria);

    /**
     * Verifica si ya existe un producto registrado
     * con el mismo nombre.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Obtiene un producto aplicando bloqueo pesimista.
     *
     * El bloqueo PESSIMISTIC_WRITE evita modificaciones concurrentes
     * sobre el mismo registro durante transacciones críticas,
     * especialmente en operaciones relacionadas con stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);
}