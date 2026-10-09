package com.pedidos.mayorista.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "producto", indexes = @Index(name = "idx_producto_comercio", columnList = "comercio_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Comercio dueño del producto (cada comercio tiene su propio catálogo y precios)
    @JsonIgnore
    @Column(name = "comercio_id")
    private Long comercioId;

    private String nombre;
    private Double costo;
    private BigDecimal precioVenta;
    private String tipoVenta;
    private String codigo;

    private Integer stock;

    private Integer stockMinimo;

    private Boolean activo;
}