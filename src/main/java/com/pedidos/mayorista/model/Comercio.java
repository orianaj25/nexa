package com.pedidos.mayorista.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Un comercio = un cliente que alquila el sistema.
 * Todo el negocio (productos, pedidos, cajas, usuarios) cuelga de un comercio.
 */
@Entity
@Table(name = "comercio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comercio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 80)
    private String rubro;

    // false = comercio suspendido: sus usuarios no pueden ingresar
    @Column(nullable = false)
    private Boolean activo = true;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Argentina/Buenos_Aires")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        }

        if (activo == null) {
            activo = true;
        }
    }
}
