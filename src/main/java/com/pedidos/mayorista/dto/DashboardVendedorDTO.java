package com.pedidos.mayorista.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVendedorDTO {


    private Long pendientes;

    private Long facturacion;

    private Long facturados;

    private Long anulados;

}