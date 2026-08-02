package com.pedidos.mayorista.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVendedorDTO {

    private Long pendientes;

    private Long pagados;

    private Long entregados;

    private Long anulados;

}