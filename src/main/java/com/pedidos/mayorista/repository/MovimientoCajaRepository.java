package com.pedidos.mayorista.repository;

import com.pedidos.mayorista.model.Caja;
import com.pedidos.mayorista.model.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Los movimientos no llevan comercio propio: pertenecen a una Caja, y la Caja es del comercio.
 * Por eso los servicios siempre validan primero la caja contra el comercio actual.
 */
@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findByCajaIdOrderByFechaAsc(Long cajaId);

    List<MovimientoCaja> findByCajaOrderByFechaDesc(Caja caja);

}
