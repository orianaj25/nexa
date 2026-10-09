package com.pedidos.mayorista.repository;

import com.pedidos.mayorista.dto.PedidoDetalleDTO;
import com.pedidos.mayorista.dto.PedidoHistorialDTO;
import com.pedidos.mayorista.model.DetallePedido;
import com.pedidos.mayorista.model.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("""
        SELECT new com.pedidos.mayorista.dto.PedidoDetalleDTO(
            p.id,
            p.numeroPedido,
            p.fecha,
            pr.nombre,
            d.cantidad,
            d.subtotal,
            p.total,
            p.metodoPago
        )
        FROM DetallePedido d
        JOIN d.pedido p
        JOIN d.producto pr
        WHERE p.comercioId = :comercioId
        ORDER BY p.fecha DESC
    """)
    List<PedidoDetalleDTO> listarDetalle(@Param("comercioId") Long comercioId);

    @Query("""
        SELECT COALESCE(SUM(d.cantidad),0)
        FROM DetallePedido d
        WHERE d.pedido.comercioId = :comercioId
          AND d.pedido.fecha BETWEEN :inicio AND :fin
    """)
    Integer productosVendidos(@Param("comercioId") Long comercioId,
                              @Param("inicio") LocalDateTime inicio,
                              @Param("fin") LocalDateTime fin);

    @Query("""
        SELECT new com.pedidos.mayorista.dto.PedidoHistorialDTO(
            p.id,
            p.numeroPedido,
            p.fecha,
            COUNT(d.id),
            p.total,
            p.metodoPago,
            CAST(p.estado AS string)
        )
        FROM DetallePedido d
        JOIN d.pedido p
        WHERE p.comercioId = :comercioId
          AND p.estado <> :estado
        GROUP BY
            p.id,
            p.numeroPedido,
            p.fecha,
            p.total,
            p.metodoPago,
            p.estado
        ORDER BY p.fecha DESC
    """)
    List<PedidoHistorialDTO> listarHistorial(@Param("comercioId") Long comercioId,
                                             @Param("estado") EstadoPedido estado);

    @Query("""
        SELECT new com.pedidos.mayorista.dto.PedidoHistorialDTO(
            p.id,
            p.numeroPedido,
            p.fecha,
            COUNT(d.id),
            p.total,
            p.metodoPago,
            CAST(p.estado AS string)
        )
        FROM DetallePedido d
        JOIN d.pedido p
        WHERE p.comercioId = :comercioId
          AND p.estado = :estado
        GROUP BY
            p.id,
            p.numeroPedido,
            p.fecha,
            p.total,
            p.metodoPago,
            p.estado
        ORDER BY p.fecha DESC
    """)
    List<PedidoHistorialDTO> listarAnulados(@Param("comercioId") Long comercioId,
                                            @Param("estado") EstadoPedido estado);

    @Query("""
        SELECT new com.pedidos.mayorista.dto.PedidoHistorialDTO(
            p.id,
            p.numeroPedido,
            p.fecha,
            COUNT(d.id),
            p.total,
            p.metodoPago,
            CAST(p.estado AS string)
        )
        FROM DetallePedido d
        JOIN d.pedido p
        WHERE p.comercioId = :comercioId
        GROUP BY
            p.id,
            p.numeroPedido,
            p.fecha,
            p.total,
            p.metodoPago,
            p.estado
        ORDER BY p.fecha DESC
    """)
    List<PedidoHistorialDTO> listarTodos(@Param("comercioId") Long comercioId);

    @Query("""
        SELECT
            d.producto.nombre,
            SUM(d.cantidad)
        FROM DetallePedido d
        WHERE d.pedido.comercioId = :comercioId
        GROUP BY d.producto.nombre
        ORDER BY SUM(d.cantidad) DESC
    """)
    List<Object[]> productosMasVendidos(@Param("comercioId") Long comercioId);

}
