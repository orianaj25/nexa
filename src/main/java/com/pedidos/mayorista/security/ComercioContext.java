package com.pedidos.mayorista.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Punto único para saber "en qué comercio estoy parado".
 * Todos los servicios de negocio usan id() para filtrar lo que leen y escriben.
 */
@Component
public class ComercioContext {

    public UsuarioPrincipal principal() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UsuarioPrincipal)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Sesión no válida."
            );

        }

        return (UsuarioPrincipal) auth.getPrincipal();
    }

    /**
     * Id del comercio del usuario logueado.
     * El SUPER_ADMIN no tiene comercio: si llega acá se rechaza.
     */
    public Long id() {

        Long comercioId = principal().getComercioId();

        if (comercioId == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Esta operación solo está disponible dentro de un comercio."
            );

        }

        return comercioId;
    }
}
