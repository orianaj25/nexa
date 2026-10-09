package com.pedidos.mayorista.repository;

import com.pedidos.mayorista.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByComercioId(Long comercioId);

    Optional<Producto> findByIdAndComercioId(Long id, Long comercioId);

    // El código se repite entre comercios: es único solo dentro de cada uno
    Optional<Producto> findByCodigoAndComercioId(String codigo, Long comercioId);

    boolean existsByNombreIgnoreCaseAndComercioId(String nombre, Long comercioId);

    long countByComercioId(Long comercioId);

}
