package com.pedidos.mayorista.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UltimoPedidoDTO {

    private String numeroPedido;

    private String dniCliente;

    private String estado;

    private BigDecimal total;

}