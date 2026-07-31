package com.pedidos.mayorista.service;

import com.pedidos.mayorista.model.Usuario;
import com.pedidos.mayorista.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

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

        return User.builder()
                .username(usuario.getUsuario())
                .password(usuario.getPassword())
                .authorities(
                        new SimpleGrantedAuthority(
                                "ROLE_" + usuario.getRol().name()
                        )
                )
                .build();
    }
}