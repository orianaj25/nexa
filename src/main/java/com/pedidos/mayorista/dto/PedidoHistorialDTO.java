package com.pedidos.mayorista.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoHistorialDTO {

    public Long pedidoId;          // interno

    public String numeroPedido;    // visible

    public LocalDateTime fecha;

    public Long cantidadProductos;

    public BigDecimal total;

    public String metodoPago;

    public String estado;

    public PedidoHistorialDTO(
            Long pedidoId,
            String numeroPedido,
            LocalDateTime fecha,
            Long cantidadProductos,
            BigDecimal total,
            String metodoPago,
            String estado) {

        this.pedidoId = pedidoId;
        this.numeroPedido = numeroPedido;
        this.fecha = fecha;
        this.cantidadProductos = cantidadProductos;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

}