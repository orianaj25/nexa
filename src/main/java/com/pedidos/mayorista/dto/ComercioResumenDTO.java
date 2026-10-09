package com.pedidos.mayorista.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Fila del panel del SUPER_ADMIN: datos del comercio + métricas rápidas. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComercioResumenDTO {

    private Long id;

    private String nombre;

    private String rubro;

    private Boolean activo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Argentina/Buenos_Aires")
    private LocalDateTime fechaCreacion;

    // usuario del primer administrador
    private String administrador;

    private Long usuarios;

    private Long productos;

    private Long pedidosHoy;

    private BigDecimal ventasHoy;

}
