package com.pedidos.mayorista.service;

import com.pedidos.mayorista.dto.DashboardDTO;
import com.pedidos.mayorista.dto.DashboardVendedorDTO;
import com.pedidos.mayorista.dto.ProductoMasVendidoDTO;
import com.pedidos.mayorista.dto.UltimoPedidoDTO;
import com.pedidos.mayorista.model.Pedido;
import com.pedidos.mayorista.model.enums.EstadoPedido;
import com.pedidos.mayorista.repository.DetallePedidoRepository;
import com.pedidos.mayorista.repository.PedidoRepository;
import com.pedidos.mayorista.security.ComercioContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class DashboardService {

    // El servidor (Render) corre en UTC: el "hoy" del comercio se calcula siempre en hora argentina
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final PedidoRepository pedidoRepository;

    private final DetallePedidoRepository detalleRepository;

    private final ComercioContext comercio;

    public DashboardService(
            PedidoRepository pedidoRepository,
            DetallePedidoRepository detalleRepository,
            ComercioContext comercio) {

        this.pedidoRepository = pedidoRepository;
        this.detalleRepository = detalleRepository;
        this.comercio = comercio;

    }

    // =====================================================
    // DASHBOARD PRINCIPAL
    // =====================================================

    public DashboardDTO obtenerDashboard() {

        Long comercioId = comercio.id();

        LocalDate hoy = LocalDate.now(ZONA);

        LocalDateTime inicio = hoy.atStartOfDay();

        LocalDateTime fin = hoy.atTime(23,59,59);

        return new DashboardDTO(

                pedidoRepository.ventasDelDia(comercioId, inicio, fin),

                pedidoRepository.pedidosDelDia(comercioId, inicio, fin),

                pedidoRepository.clientesDelDia(comercioId, inicio, fin),

                detalleRepository.productosVendidos(comercioId, inicio, fin)

        );

    }

    // =====================================================
    // ÚLTIMOS PEDIDOS
    // =====================================================

    public List<UltimoPedidoDTO> obtenerUltimosPedidos() {

        List<Pedido> pedidos =
                pedidoRepository.findTop10ByComercioIdOrderByFechaDesc(comercio.id());

        List<UltimoPedidoDTO> respuesta =
                new ArrayList<>();

        for (Pedido pedido : pedidos) {

            respuesta.add(

                    new UltimoPedidoDTO(

                            pedido.getNumeroPedido(),

                            pedido.getDniCliente(),

                            pedido.getEstado().name(),

                            pedido.getTotal()

                    )

            );

        }

        return respuesta;

    }


    // =====================================================
    // DASHBOARD VENDEDOR
    // =====================================================

    public DashboardVendedorDTO obtenerDashboardVendedor() {

        Long comercioId = comercio.id();

        return new DashboardVendedorDTO(

                pedidoRepository.countByEstadoAndComercioId(
                        EstadoPedido.PENDIENTE_FACTURACION,
                        comercioId
                ),

                pedidoRepository.countByEstadoAndComercioId(
                        EstadoPedido.ENVIADO_A_FACTURACION,
                        comercioId
                ),

                pedidoRepository.countByEstadoAndComercioId(
                        EstadoPedido.FACTURADO,
                        comercioId
                ),

                pedidoRepository.countByEstadoAndComercioId(
                        EstadoPedido.ANULADO,
                        comercioId
                )

        );

    }
    // =====================================================
    // PRODUCTOS MÁS VENDIDOS
    // =====================================================

    public List<ProductoMasVendidoDTO> productosMasVendidos() {

        List<Object[]> consulta =
                detalleRepository.productosMasVendidos(comercio.id());

        List<ProductoMasVendidoDTO> respuesta =
                new ArrayList<>();

        for (Object[] fila : consulta) {

            respuesta.add(

                    new ProductoMasVendidoDTO(

                            (String) fila[0],

                            ((Number) fila[1]).longValue()

                    )

            );

        }

        return respuesta;

    }

    // =====================================================
    // VENTAS ÚLTIMOS 7 DÍAS
    // =====================================================

    public Map<String, Object> ventasUltimos7Dias() {

        LocalDate hoy = LocalDate.now(ZONA);

        // Desde las 00:00 del día más antiguo, para no perder ventas de ese día
        LocalDateTime inicio = hoy.minusDays(6).atStartOfDay();

        LocalDateTime fin = hoy.atTime(23,59,59);

        List<Object[]> resultados =
                pedidoRepository.ventasPorDia(comercio.id(), inicio, fin);

        Map<LocalDate, BigDecimal> mapa =
                new HashMap<>();

        for (Object[] fila : resultados) {

            LocalDate fecha =
                    ((java.sql.Date) fila[0]).toLocalDate();

            BigDecimal total =
                    (BigDecimal) fila[1];

            mapa.put(fecha, total);

        }

        List<String> labels =
                new ArrayList<>();

        List<Integer> data =
                new ArrayList<>();

        for (int i = 6; i >= 0; i--) {

            LocalDate dia =
                    hoy.minusDays(i);

            labels.add(

                    dia.getDayOfWeek()
                            .getDisplayName(
                                    TextStyle.SHORT,
                                    new Locale("es", "AR")
                            )

            );

            BigDecimal total =
                    mapa.getOrDefault(
                            dia,
                            BigDecimal.ZERO
                    );

            data.add(total.intValue());

        }

        Map<String, Object> response =
                new HashMap<>();

        response.put("labels", labels);

        response.put("data", data);

        return response;

    }

}
