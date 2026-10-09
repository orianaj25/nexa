package com.pedidos.mayorista.security;

import com.pedidos.mayorista.model.Comercio;
import com.pedidos.mayorista.repository.ComercioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Si el SUPER_ADMIN suspende un comercio, sus usuarios que ya tenían la sesión
 * abierta quedan afuera en el siguiente request (no hace falta esperar a que venza la sesión).
 * El estado se cachea 60 segundos para no consultar la base en cada request.
 */
@Component
public class ComercioActivoFilter extends OncePerRequestFilter {

    private static final long TTL_MS = 60_000;

    private record Estado(boolean activo, long vence) {
    }

    private final ComercioRepository comercioRepository;

    private final Map<Long, Estado> cache = new ConcurrentHashMap<>();

    public ComercioActivoFilter(ComercioRepository comercioRepository) {
        this.comercioRepository = comercioRepository;
    }

    /** Lo llama el SUPER_ADMIN al suspender/reactivar para que el cambio sea inmediato. */
    public void invalidar(Long comercioId) {
        cache.remove(comercioId);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal) {

            Long comercioId =
                    ((UsuarioPrincipal) auth.getPrincipal()).getComercioId();

            if (comercioId != null && !estaActivo(comercioId)) {

                SecurityContextHolder.clearContext();

                HttpSession session = request.getSession(false);

                if (session != null) {
                    session.invalidate();
                }

                if (request.getRequestURI().startsWith("/api/")) {

                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "El comercio se encuentra suspendido."
                    );

                } else {

                    response.sendRedirect("/login");

                }

                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean estaActivo(Long comercioId) {

        long ahora = System.currentTimeMillis();

        Estado estado = cache.get(comercioId);

        if (estado == null || estado.vence() < ahora) {

            boolean activo = comercioRepository.findById(comercioId)
                    .map(Comercio::getActivo)
                    .orElse(false);

            estado = new Estado(activo, ahora + TTL_MS);

            cache.put(comercioId, estado);
        }

        return estado.activo();
    }
}
