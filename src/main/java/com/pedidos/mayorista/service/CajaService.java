package com.pedidos.mayorista.service;

import com.pedidos.mayorista.dto.CajaHistorialDTO;
import com.pedidos.mayorista.model.Caja;
import com.pedidos.mayorista.model.Pedido;
import com.pedidos.mayorista.repository.CajaRepository;
import com.pedidos.mayorista.security.ComercioContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class CajaService {

    private final CajaRepository cajaRepository;

    private final ComercioContext comercio;

    public CajaService(CajaRepository cajaRepository,
                       ComercioContext comercio) {
        this.cajaRepository = cajaRepository;
        this.comercio = comercio;
    }

    /*
     * Obtiene la caja abierta DEL COMERCIO ACTUAL.
     * Cada comercio maneja su propia caja de forma independiente.
     */
    public Optional<Caja> obtenerCajaAbierta() {

        return cajaAbierta(comercio.id());

    }

    private Optional<Caja> cajaAbierta(Long comercioId) {

        return cajaRepository.findByEstadoAndComercioId("ABIERTA", comercioId);

    }

    /*
     * Lista todas las cajas del comercio.
     */
    public List<CajaHistorialDTO> listar() {

        return cajaRepository.listarHistorial(comercio.id());

    }

    /*
     * Registrar automáticamente una venta.
     * Es llamado desde PedidoService.
     */
    @Transactional
    public void registrarVenta(Pedido pedido) {

        Optional<Caja> cajaOpt = cajaAbierta(pedido.getComercioId());

        if (cajaOpt.isEmpty()) {
            return;
        }

        Caja caja = cajaOpt.get();

        if ("EFECTIVO".equalsIgnoreCase(pedido.getMetodoPago())) {

            caja.setVentasEfectivo(
                    caja.getVentasEfectivo().add(pedido.getTotal())
            );

        } else {

            caja.setVentasDigitales(
                    caja.getVentasDigitales().add(pedido.getTotal())
            );

        }

        recalcularCaja(caja);

        cajaRepository.save(caja);

    }

    /*
     * Recalcula el efectivo esperado.
     */
    private void recalcularCaja(Caja caja) {

        BigDecimal esperado =
                caja.getCajaInicial()
                        .add(caja.getVentasEfectivo())
                        .add(caja.getIngresos())
                        .subtract(caja.getRetiros());

        caja.setEfectivoEsperado(esperado);

    }

    /*
     * Abrir una caja.
     */
    @Transactional
    public Caja abrirCaja(BigDecimal cajaInicial,
                          String usuario) {

        if (obtenerCajaAbierta().isPresent()) {

            throw new RuntimeException(
                    "Ya existe una caja abierta."
            );

        }

        Caja caja = new Caja();

        caja.setComercioId(comercio.id());

        caja.setFechaApertura(
                LocalDateTime.now(
                        ZoneId.of("America/Argentina/Buenos_Aires"))
        );

        caja.setUsuario(usuario);

        caja.setEstado("ABIERTA");

        caja.setCajaInicial(cajaInicial);

        caja.setVentasEfectivo(BigDecimal.ZERO);

        caja.setVentasDigitales(BigDecimal.ZERO);

        caja.setIngresos(BigDecimal.ZERO);

        caja.setRetiros(BigDecimal.ZERO);

        caja.setEfectivoEsperado(cajaInicial);

        caja.setEfectivoContado(BigDecimal.ZERO);

        caja.setDiferencia(BigDecimal.ZERO);

        caja.setObservaciones("");

        return cajaRepository.save(caja);

    }

    /*
     * Cerrar caja.
     */
    @Transactional
    public Caja cerrarCaja(BigDecimal efectivoContado,
                           String observaciones) {

        Caja caja = obtenerCajaAbierta()
                .orElseThrow(() ->
                        new RuntimeException("No existe una caja abierta."));

        caja.setFechaCierre(
                LocalDateTime.now(
                        ZoneId.of("America/Argentina/Buenos_Aires"))
        );

        caja.setEstado("CERRADA");

        caja.setEfectivoContado(efectivoContado);

        BigDecimal diferencia =
                efectivoContado.subtract(
                        caja.getEfectivoEsperado());

        caja.setDiferencia(diferencia);

        caja.setObservaciones(observaciones);

        return cajaRepository.save(caja);

    }

}
