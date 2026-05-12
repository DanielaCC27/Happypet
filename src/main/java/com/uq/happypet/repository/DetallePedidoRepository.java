package com.uq.happypet.repository;

import com.uq.happypet.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio encargado de las operaciones
 * relacionadas con los detalles de pedidos.
 */
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    /**
     * Verifica si existe al menos un detalle de pedido
     * asociado a un producto específico.
     *
     * Este método se utiliza para evitar la eliminación
     * de productos que ya han sido incluidos en pedidos.
     */
    boolean existsByProducto_Id(Long productoId);
}