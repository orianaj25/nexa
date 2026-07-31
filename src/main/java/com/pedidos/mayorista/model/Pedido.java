package com.pedidos.mayorista.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pedidos.mayorista.model.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", unique = true)
    private String numeroPedido;

    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "America/Argentina/Buenos_Aires"
    )
    private LocalDateTime fecha;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name = "metodo_pago", nullable = false)
    private String metodoPago;

    @Column(name = "dni_cliente")
    private String dniCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER
    )
    @JsonIgnore
    private List<DetallePedido> detalles;

}