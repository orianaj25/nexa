package com.pedidos.mayorista.repository;

import com.pedidos.mayorista.model.Pedido;
import com.pedidos.mayorista.model.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByIdAndComercioId(Long id, Long comercioId);

    List<Pedido> findAllByComercioId(Long comercioId);

    // Total vendido en el día
    @Query("""
        SELECT COALESCE(SUM(p.total),0)
        FROM Pedido p
        WHERE p.comercioId = :comercioId
          AND p.fecha BETWEEN :inicio AND :fin
    """)
    BigDecimal ventasDelDia(@Param("comercioId") Long comercioId,
                            @Param("inicio") LocalDateTime inicio,
                            @Param("fin") LocalDateTime fin);


    // Cantidad de pedidos
    @Query("""
        SELECT COUNT(p)
        FROM Pedido p
        WHERE p.comercioId = :comercioId
          AND p.fecha BETWEEN :inicio AND :fin
    """)
    Long pedidosDelDia(@Param("comercioId") Long comercioId,
                       @Param("inicio") LocalDateTime inicio,
                       @Param("fin") LocalDateTime fin);


    // Clientes distintos
    @Query("""
        SELECT COUNT(DISTINCT p.dniCliente)
        FROM Pedido p
        WHERE p.comercioId = :comercioId
          AND p.fecha BETWEEN :inicio AND :fin
    """)
    Long clientesDelDia(@Param("comercioId") Long comercioId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

    @Query("""
        SELECT
            FUNCTION('DATE', p.fecha),
            SUM(p.total)
        FROM Pedido p
        WHERE p.comercioId = :comercioId
          AND p.fecha BETWEEN :inicio AND :fin
        GROUP BY FUNCTION('DATE', p.fecha)
        ORDER BY FUNCTION('DATE', p.fecha)
    """)
    List<Object[]> ventasPorDia(@Param("comercioId") Long comercioId,
                                @Param("inicio") LocalDateTime inicio,
                                @Param("fin") LocalDateTime fin);

    // ULTIMOS PEDIDOS
    List<Pedido> findTop10ByComercioIdOrderByFechaDesc(Long comercioId);

    // PEDIDOS POR ESTADO
    Long countByEstadoAndComercioId(EstadoPedido estado, Long comercioId);

}
