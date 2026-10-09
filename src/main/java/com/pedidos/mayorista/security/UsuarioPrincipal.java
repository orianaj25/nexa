package com.pedidos.mayorista.security;

import com.pedidos.mayorista.model.enums.Rol;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Usuario autenticado + el comercio al que pertenece.
 * Se guarda en la sesión al hacer login, así cada request conoce su comercio
 * sin consultar la base de datos y sin confiar en nada que mande el navegador.
 */
public class UsuarioPrincipal extends User {

    private final Long comercioId;

    private final Rol rol;

    public UsuarioPrincipal(String username,
                            String password,
                            boolean enabled,
                            Collection<? extends GrantedAuthority> authorities,
                            Long comercioId,
                            Rol rol) {

        super(username, password, enabled, true, true, true, authorities);

        this.comercioId = comercioId;
        this.rol = rol;
    }

    // null para el SUPER_ADMIN
    public Long getComercioId() {
        return comercioId;
    }

    public Rol getRol() {
        return rol;
    }
}
