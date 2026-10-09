package com.pedidos.mayorista.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Alta de un comercio junto con su primer administrador. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearComercioRequest {

    private String nombre;

    private String rubro;

    private String adminNombre;

    private String adminApellido;

    private String adminUsuario;

    private String adminPassword;

}
