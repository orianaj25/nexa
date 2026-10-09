package com.pedidos.mayorista.repository;

import com.pedidos.mayorista.model.Comercio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComercioRepository extends JpaRepository<Comercio, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    Optional<Comercio> findFirstByNombreIgnoreCase(String nombre);

}
