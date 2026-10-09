package com.pedidos.mayorista.repository;

import com.pedidos.mayorista.dto.CajaHistorialDTO;
import com.pedidos.mayorista.model.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {

    // Cada comercio tiene su propia caja abierta
    Optional<Caja> findByEstadoAndComercioId(String estado, Long comercioId);

    Optional<Caja> findByIdAndComercioId(Long id, Long comercioId);

    @Query("""
            SELECT new com.pedidos.mayorista.dto.CajaHistorialDTO(
                c.id,
                c.usuario,
                c.fechaApertura,
                c.fechaCierre,
                c.estado,
                c.cajaInicial,
                c.ventasEfectivo,
                c.ventasDigitales,
                c.ingresos,
                c.retiros,
                c.efectivoEsperado,
                c.efectivoContado,
                c.diferencia,
                c.observaciones
            )
            FROM Caja c
            WHERE c.comercioId = :comercioId
            ORDER BY c.fechaApertura DESC
            """)
    List<CajaHistorialDTO> listarHistorial(@Param("comercioId") Long comercioId);
}
