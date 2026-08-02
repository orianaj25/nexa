package com.pedidos.mayorista.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UltimoPedidoVendedorDTO {

    private Long numeroPedido;

    private String cliente;

    private String estado;

    private String hora;

}