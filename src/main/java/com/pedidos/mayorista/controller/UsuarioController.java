package com.pedidos.mayorista.controller;

import com.pedidos.mayorista.model.Usuario;
import com.pedidos.mayorista.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // ==========================
    // LISTAR USUARIOS
    // ==========================

    @GetMapping
    public List<Usuario> listar() {

        return usuarioService.listar();

    }

    // ==========================
    // BUSCAR POR ID
    // ==========================

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {

        return usuarioService.buscarPorId(id);

    }

    // ==========================
    // CREAR USUARIO
    // ==========================

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {

        try {

            return ResponseEntity.ok(
                    usuarioService.crear(usuario)
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        }

    }

    // ==========================
    // ACTUALIZAR USUARIO
    // ==========================

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        try {

            return ResponseEntity.ok(
                    usuarioService.actualizar(id, usuario)
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        }

    }

    // ==========================
    // ACTIVAR / DESACTIVAR
    // ==========================

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id) {

        try {

            usuarioService.cambiarEstado(id);

            return ResponseEntity.ok(
                    "Estado del usuario actualizado correctamente."
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        }

    }

    // ==========================
// USUARIO LOGUEADO
// ==========================

    @GetMapping("/me")
    public ResponseEntity<Usuario> usuarioLogueado(
            Principal principal) {

        Usuario usuario = usuarioService.buscarPorUsuario(
                principal.getName()
        );

        usuario.setPassword(null);

        return ResponseEntity.ok(usuario);

    }

}