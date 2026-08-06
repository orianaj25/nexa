package com.pedidos.mayorista.repository;

import com.pedidos.mayorista.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoRepository  extends JpaRepository<Producto, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Producto> findByCodigo(String codigo);

}
