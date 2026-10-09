package com.pedidos.mayorista.service;

import com.pedidos.mayorista.model.Caja;
import com.pedidos.mayorista.model.MovimientoCaja;
import com.pedidos.mayorista.repository.CajaRepository;
import com.pedidos.mayorista.repository.MovimientoCajaRepository;
import com.pedidos.mayorista.security.ComercioContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class MovimientoCajaService {

    private final MovimientoCajaRepository movimientoRepository;
    private final CajaRepository cajaRepository;
    private final ComercioContext comercio;

    public MovimientoCajaService(
            MovimientoCajaRepository movimientoRepository,
            CajaRepository cajaRepository,
            ComercioContext comercio) {

        this.movimientoRepository = movimientoRepository;
        this.cajaRepository = cajaRepository;
        this.comercio = comercio;
    }

    /*
     * Caja abierta del comercio actual
     */
    private Caja cajaAbiertaActual() {

        return cajaRepository
                .findByEstadoAndComercioId("ABIERTA", comercio.id())
                .orElseThrow(() ->
                        new RuntimeException("No existe una caja abierta"));
    }

    /*
     * ===============================
     * REGISTRAR INGRESO
     * ===============================
     */
    @Transactional
    public MovimientoCaja registrarIngreso(
            BigDecimal monto,
            String motivo,
            String usuario) {

        Caja caja = cajaAbiertaActual();

        MovimientoCaja movimiento = new MovimientoCaja();

        movimiento.setCaja(caja);
        movimiento.setFecha(
                LocalDateTime.now(
                        ZoneId.of("America/Argentina/Buenos_Aires"))
        );

        movimiento.setTipo("INGRESO");
        movimiento.setMonto(monto);
        movimiento.setMotivo(motivo);
        movimiento.setUsuario(usuario);

        caja.setIngresos(
                caja.getIngresos().add(monto)
        );

        recalcularCaja(caja);

        cajaRepository.save(caja);

        return movimientoRepository.save(movimiento);
    }

    /*
     * ===============================
     * REGISTRAR RETIRO
     * ===============================
     */
    @Transactional
    public MovimientoCaja registrarRetiro(
            BigDecimal monto,
            String motivo,
            String usuario) {

        Caja caja = cajaAbiertaActual();

        MovimientoCaja movimiento = new MovimientoCaja();

        movimiento.setCaja(caja);
        movimiento.setFecha(
                LocalDateTime.now(
                        ZoneId.of("America/Argentina/Buenos_Aires"))
        );

        movimiento.setTipo("RETIRO");
        movimiento.setMonto(monto);
        movimiento.setMotivo(motivo);
        movimiento.setUsuario(usuario);

        caja.setRetiros(
                caja.getRetiros().add(monto)
        );

        recalcularCaja(caja);

        cajaRepository.save(caja);

        return movimientoRepository.save(movimiento);
    }

    /*
     * ===============================
     * LISTAR MOVIMIENTOS
     * ===============================
     */
    public List<MovimientoCaja> listarMovimientos() {

        Caja caja = cajaAbiertaActual();

        return movimientoRepository.findByCajaOrderByFechaDesc(caja);
    }

    /*
     * ===============================
     * RECALCULAR EFECTIVO ESPERADO
     * ===============================
     */
    private void recalcularCaja(Caja caja) {

        BigDecimal esperado =
                caja.getCajaInicial()
                        .add(caja.getVentasEfectivo())
                        .add(caja.getIngresos())
                        .subtract(caja.getRetiros());

        caja.setEfectivoEsperado(esperado);

    }

    public List<MovimientoCaja> listarPorCaja(Long cajaId) {

        // La caja tiene que ser del comercio actual
        cajaRepository.findByIdAndComercioId(cajaId, comercio.id())
                .orElseThrow(() ->
                        new RuntimeException("Caja no encontrada"));

        return movimientoRepository.findByCajaIdOrderByFechaAsc(cajaId);

    }

}