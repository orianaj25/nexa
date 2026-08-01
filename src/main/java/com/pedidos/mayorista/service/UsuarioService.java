package com.pedidos.mayorista.service;

import com.pedidos.mayorista.model.Usuario;
import com.pedidos.mayorista.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================
    // LISTAR
    // ==========================

    public List<Usuario> listar() {

        return usuarioRepository.findAll();

    }

    // ==========================
    // BUSCAR POR ID
    // ==========================

    public Usuario buscarPorId(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

    }

    // ==========================
    // CREAR
    // ==========================

    public Usuario crear(Usuario usuario) {

        if (usuario.getUsuario() == null ||
                usuario.getUsuario().isBlank()) {

            throw new RuntimeException(
                    "El nombre de usuario es obligatorio."
            );
        }

        if (usuario.getPassword() == null ||
                usuario.getPassword().isBlank()) {

            throw new RuntimeException(
                    "La contraseña es obligatoria."
            );
        }

        if (usuario.getRol() == null) {

            throw new RuntimeException(
                    "El rol es obligatorio."
            );
        }

        if (usuarioRepository.existsByUsuario(usuario.getUsuario())) {

            throw new RuntimeException(
                    "El nombre de usuario ya existe."
            );
        }

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        return usuarioRepository.save(usuario);
    }

    // ==========================
    // ACTUALIZAR
    // ==========================

    public Usuario actualizar(Long id, Usuario datos) {

        Usuario usuario = buscarPorId(id);

        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setRol(datos.getRol());
        usuario.setActivo(datos.getActivo());

        if (datos.getPassword() != null &&
                !datos.getPassword().isBlank()) {

            usuario.setPassword(
                    passwordEncoder.encode(datos.getPassword())
            );

        }

        return usuarioRepository.save(usuario);

    }

    // ==========================
    // ACTIVAR / DESACTIVAR
    // ==========================

    public void cambiarEstado(Long id) {

        Usuario usuario = buscarPorId(id);

        usuario.setActivo(!usuario.getActivo());

        usuarioRepository.save(usuario);

    }

    // ==========================
// USUARIO LOGUEADO
// ==========================

    public Usuario buscarPorUsuario(String username) {

        return usuarioRepository.findByUsuario(username)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

    }

}