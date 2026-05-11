package com.uq.happypet.repository;

import com.uq.happypet.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    boolean existsByProducto_Id(Long productoId);
}