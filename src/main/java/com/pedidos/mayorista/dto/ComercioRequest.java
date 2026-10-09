package com.pedidos.mayorista.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComercioRequest {

    private String nombre;

    private String rubro;

}
