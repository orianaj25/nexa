package com.pedidos.mayorista.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoMasVendidoDTO {

    private String producto;

    private Long cantidadVendida;

}