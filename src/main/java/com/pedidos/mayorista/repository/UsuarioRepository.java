package com.pedidos.mayorista.repository;

import com.pedidos.mayorista.model.Usuario;
import com.pedidos.mayorista.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // El login es global: el nombre de usuario es único en todo el sistema
    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);

    boolean existsByRol(Rol rol);

    // Siempre acotado al comercio
    List<Usuario> findAllByComercioId(Long comercioId);

    Optional<Usuario> findByIdAndComercioId(Long id, Long comercioId);

    long countByComercioId(Long comercioId);

    Optional<Usuario> findFirstByComercioIdAndRolOrderByIdAsc(Long comercioId, Rol rol);

}
