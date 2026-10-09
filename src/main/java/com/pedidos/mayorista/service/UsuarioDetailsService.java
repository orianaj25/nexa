package com.pedidos.mayorista.service;

import com.pedidos.mayorista.model.Comercio;
import com.pedidos.mayorista.model.Usuario;
import com.pedidos.mayorista.model.enums.Rol;
import com.pedidos.mayorista.repository.ComercioRepository;
import com.pedidos.mayorista.repository.UsuarioRepository;
import com.pedidos.mayorista.security.UsuarioPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComercioRepository comercioRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByUsuario(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        if (!usuario.getActivo()) {

            throw new UsernameNotFoundException(
                    "El usuario está inactivo"
            );

        }

        // El SUPER_ADMIN no pertenece a ningún comercio.
        // Cualquier otro usuario necesita un comercio existente y activo.
        boolean habilitado = true;

        if (usuario.getRol() != Rol.SUPER_ADMIN) {

            if (usuario.getComercioId() == null) {

                throw new UsernameNotFoundException(
                        "El usuario no pertenece a ningún comercio"
                );

            }

            habilitado = comercioRepository
                    .findById(usuario.getComercioId())
                    .map(Comercio::getActivo)
                    .orElse(false);

        }

        return new UsuarioPrincipal(
                usuario.getUsuario(),
                usuario.getPassword(),
                habilitado,
                Collections.singletonList(
                        new SimpleGrantedAuthority(
                                "ROLE_" + usuario.getRol().name()
                        )
                ),
                usuario.getComercioId(),
                usuario.getRol()
        );
    }
}
