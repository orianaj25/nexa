package com.pedidos.mayorista.dto;

import com.pedidos.mayorista.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos del usuario logueado (sin password) + el comercio donde trabaja. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeDTO {

    private Long id;

    private String nombre;

    private String apellido;

    private String usuario;

    private Rol rol;

    private Boolean activo;

    // null para el SUPER_ADMIN
    private Long comercioId;

    private String comercioNombre;

}
