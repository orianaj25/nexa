package com.pedidos.mayorista.service;

import com.pedidos.mayorista.dto.MeDTO;
import com.pedidos.mayorista.model.Comercio;
import com.pedidos.mayorista.model.Usuario;
import com.pedidos.mayorista.model.enums.Rol;
import com.pedidos.mayorista.repository.ComercioRepository;
import com.pedidos.mayorista.repository.UsuarioRepository;
import com.pedidos.mayorista.security.ComercioContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComercioRepository comercioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ComercioContext comercio;

    // ==========================
    // LISTAR (solo usuarios de MI comercio)
    // ==========================

    public List<Usuario> listar() {

        return usuarioRepository.findAllByComercioId(comercio.id());

    }

    // ==========================
    // BUSCAR POR ID (solo de MI comercio)
    // ==========================

    public Usuario buscarPorId(Long id) {

        return usuarioRepository.findByIdAndComercioId(id, comercio.id())
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

    }

    // ==========================
    // CREAR (en mi comercio)
    // ==========================

    public Usuario crear(Usuario usuario) {

        return crearEnComercio(usuario, comercio.id());

    }

    /**
     * Alta de un usuario dentro de un comercio puntual.
     * Lo usa el administrador (su propio comercio) y el SUPER_ADMIN (cualquier comercio).
     */
    public Usuario crearEnComercio(Usuario usuario, Long comercioId) {

        if (usuario.getUsuario() == null ||
                usuario.getUsuario().isBlank()) {

            throw new RuntimeException(
                    "El nombre de usuario es obligatorio."
            );
        }

        if (usuario.getNombre() == null ||
                usuario.getNombre().isBlank() ||
                usuario.getApellido() == null ||
                usuario.getApellido().isBlank()) {

            throw new RuntimeException(
                    "El nombre y el apellido son obligatorios."
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

        // Nadie puede crear un SUPER_ADMIN desde la API
        controlarRolPermitido(usuario.getRol());

        usuario.setUsuario(usuario.getUsuario().trim());

        if (usuarioRepository.existsByUsuario(usuario.getUsuario())) {

            throw new RuntimeException(
                    "El nombre de usuario ya existe."
            );
        }

        // Siempre es un alta nueva y siempre dentro del comercio indicado por el servidor
        usuario.setId(null);

        usuario.setComercioId(comercioId);

        usuario.setFechaCreacion(null);

        if (usuario.getActivo() == null) {

            usuario.setActivo(true);

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

        if (datos.getRol() != null) {

            controlarRolPermitido(datos.getRol());

            usuario.setRol(datos.getRol());

        }

        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
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

    public MeDTO obtenerMe(String username) {

        Usuario u = buscarPorUsuario(username);

        String nombreComercio = null;

        if (u.getComercioId() != null) {

            nombreComercio = comercioRepository
                    .findById(u.getComercioId())
                    .map(Comercio::getNombre)
                    .orElse(null);

        }

        return new MeDTO(
                u.getId(),
                u.getNombre(),
                u.getApellido(),
                u.getUsuario(),
                u.getRol(),
                u.getActivo(),
                u.getComercioId(),
                nombreComercio
        );

    }

    // ==========================
    // SUPER_ADMIN: restablecer contraseña de cualquier usuario de comercio
    // ==========================

    public void restablecerPassword(Long usuarioId, String nuevaPassword) {

        if (nuevaPassword == null || nuevaPassword.isBlank()) {

            throw new RuntimeException("La contraseña es obligatoria.");

        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.SUPER_ADMIN) {

            throw new RuntimeException(
                    "No se puede modificar este usuario desde el panel."
            );

        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));

        usuarioRepository.save(usuario);

    }

    // ==========================
    // REGLAS DE ROL
    // ==========================

    private void controlarRolPermitido(Rol rol) {

        if (rol == Rol.SUPER_ADMIN) {

            throw new RuntimeException(
                    "No se puede asignar el rol SUPER_ADMIN."
            );

        }

    }

}
