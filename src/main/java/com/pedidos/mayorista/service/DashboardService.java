package com.pedidos.mayorista.service;

import com.pedidos.mayorista.dto.DashboardDTO;
import com.pedidos.mayorista.dto.EstadosPedidosDTO;
import com.pedidos.mayorista.dto.ProductoMasVendidoDTO;
import com.pedidos.mayorista.dto.ResumenEstadosDTO;
import com.pedidos.mayorista.dto.UltimoPedidoDTO;
import com.pedidos.mayorista.model.Pedido;
import com.pedidos.mayorista.model.enums.EstadoPedido;
import com.pedidos.mayorista.repository.DetallePedidoRepository;
import com.pedidos.mayorista.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detalleRepository;

    public DashboardService(
            PedidoRepository pedidoRepository,
            DetallePedidoRepository detalleRepository) {

        this.pedidoRepository = pedidoRepository;
        this.detalleRepository = detalleRepository;
    }

// =====================================================
// DASHBOARD PRINCIPAL (Tarjetas superiores)
// =====================================================
public DashboardDTO obtenerDashboard() {

    LocalDate hoy = LocalDate.now();

    LocalDateTime inicio = hoy.atStartOfDay();

    LocalDateTime fin = hoy.atTime(23, 59, 59);

    return new DashboardDTO(

            pedidoRepository.ventasDelDia(inicio, fin),

            pedidoRepository.pedidosDelDia(inicio, fin),

            pedidoRepository.clientesDelDia(inicio, fin),

            detalleRepository.productosVendidos(inicio, fin)

    );

}


    // =====================================================
    // ÚLTIMOS PEDIDOS (Administrador)
    // =====================================================

    public List<UltimoPedidoDTO> obtenerUltimosPedidos() {

        List<Pedido> pedidos =
                pedidoRepository.findTop10ByOrderByFechaDesc();

        List<UltimoPedidoDTO> respuesta = new ArrayList<>();

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
    // RESUMEN DE ESTADOS
    // (Dashboard vendedor)
    // =====================================================

    public ResumenEstadosDTO obtenerResumenEstados() {

        return new ResumenEstadosDTO(

                pedidoRepository.countByEstado(
                        EstadoPedido.PENDIENTE_FACTURACION
                ),

                pedidoRepository.countByEstado(
                        EstadoPedido.ENVIADO_A_FACTURACION
                ),

                pedidoRepository.countByEstado(
                        EstadoPedido.FACTURADO
                ),

                pedidoRepository.countByEstado(
                        EstadoPedido.ANULADO
                )

        );

    }


    // =====================================================
    // PRODUCTOS MÁS VENDIDOS
    // =====================================================

    public List<ProductoMasVendidoDTO> productosMasVendidos() {

        List<Object[]> consulta =
                detalleRepository.productosMasVendidos();

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

    LocalDateTime fin = LocalDateTime.now();

    LocalDateTime inicio = fin.minusDays(6);

    List<Object[]> resultados =
            pedidoRepository.ventasPorDia(inicio, fin);

    Map<LocalDate, BigDecimal> mapa = new HashMap<>();

    for (Object[] fila : resultados) {

        LocalDate fecha = ((java.sql.Date) fila[0]).toLocalDate();

        BigDecimal total = (BigDecimal) fila[1];

        mapa.put(fecha, total);

    }

    List<String> labels = new ArrayList<>();

    List<Integer> ventas = new ArrayList<>();

    for (int i = 6; i >= 0; i--) {

        LocalDate dia = LocalDate.now().minusDays(i);

        labels.add(

                dia.getDayOfWeek()
                        .getDisplayName(
                                TextStyle.SHORT,
                                new Locale("es", "AR")
                        )

        );

        BigDecimal valor =
                mapa.getOrDefault(
                        dia,
                        BigDecimal.ZERO
                );

        ventas.add(valor.intValue());

    }

    Map<String, Object> response = new HashMap<>();

    response.put("labels", labels);

    response.put("data", ventas);

    return response;

}

}