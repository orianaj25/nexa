package com.pedidos.mayorista.controller;

import com.pedidos.mayorista.dto.DashboardDTO;
import com.pedidos.mayorista.dto.DashboardVendedorDTO;
import com.pedidos.mayorista.dto.ProductoMasVendidoDTO;
import com.pedidos.mayorista.dto.UltimoPedidoDTO;
import com.pedidos.mayorista.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {

        this.dashboardService = dashboardService;

    }

    // =====================================================
    // DASHBOARD ADMINISTRADOR
    // =====================================================

    @GetMapping("/api/dashboard")
    public DashboardDTO dashboard() {

        return dashboardService.obtenerDashboard();

    }

    // =====================================================
    // DASHBOARD VENDEDOR
    // =====================================================

    @GetMapping("/api/dashboard/vendedor")
    public DashboardVendedorDTO dashboardVendedor() {

        return dashboardService.obtenerDashboardVendedor();

    }

    // =====================================================
    // ÚLTIMOS PEDIDOS
    // =====================================================

    @GetMapping("/api/dashboard/ultimos-pedidos")
    public List<UltimoPedidoDTO> ultimosPedidos() {

        return dashboardService.obtenerUltimosPedidos();

    }

    // =====================================================
    // PRODUCTOS MÁS VENDIDOS
    // =====================================================

    @GetMapping("/api/dashboard/productos-mas-vendidos")
    public List<ProductoMasVendidoDTO> productosMasVendidos() {

        return dashboardService.productosMasVendidos();

    }

    // =====================================================
    // GRÁFICO VENTAS ÚLTIMOS 7 DÍAS
    // =====================================================

    @GetMapping("/api/dashboard/ventas-semana")
    public Map<String, Object> ventasSemana() {

        return dashboardService.ventasUltimos7Dias();

    }

}