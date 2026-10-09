package com.pedidos.mayorista.controller;

import com.pedidos.mayorista.dto.ComercioRequest;
import com.pedidos.mayorista.dto.ComercioResumenDTO;
import com.pedidos.mayorista.dto.CrearComercioRequest;
import com.pedidos.mayorista.dto.PasswordRequest;
import com.pedidos.mayorista.model.Comercio;
import com.pedidos.mayorista.model.Usuario;
import com.pedidos.mayorista.service.ComercioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Panel del dueño de la plataforma.
 * Solo accesible con rol SUPER_ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final ComercioService comercioService;

    public SuperAdminController(ComercioService comercioService) {

        this.comercioService = comercioService;

    }

    // ==========================
    // COMERCIOS
    // ==========================

    @GetMapping("/comercios")
    public List<ComercioResumenDTO> listar() {

        return comercioService.listar();

    }

    @PostMapping("/comercios")
    public Comercio crear(@RequestBody CrearComercioRequest request) {

        return comercioService.crear(request);

    }

    @PutMapping("/comercios/{id}")
    public Comercio actualizar(
            @PathVariable Long id,
            @RequestBody ComercioRequest request) {

        return comercioService.actualizar(id, request);

    }

    @PutMapping("/comercios/{id}/estado")
    public Comercio cambiarEstado(@PathVariable Long id) {

        return comercioService.cambiarEstado(id);

    }

    // ==========================
    // USUARIOS DE UN COMERCIO
    // ==========================

    @GetMapping("/comercios/{id}/usuarios")
    public List<Usuario> usuarios(@PathVariable Long id) {

        return comercioService.listarUsuarios(id);

    }

    @PostMapping("/comercios/{id}/usuarios")
    public Usuario crearUsuario(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        return comercioService.crearUsuario(id, usuario);

    }

    @PutMapping("/usuarios/{id}/password")
    public ResponseEntity<String> restablecerPassword(
            @PathVariable Long id,
            @RequestBody PasswordRequest request) {

        comercioService.restablecerPassword(id, request.getPassword());

        return ResponseEntity.ok("Contraseña actualizada correctamente.");

    }

    // ==========================
    // ERRORES: mensaje claro para el panel
    // ==========================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> manejarError(RuntimeException e) {

        if (e instanceof ResponseStatusException) {

            ResponseStatusException rse = (ResponseStatusException) e;

            return ResponseEntity
                    .status(rse.getStatusCode())
                    .body(rse.getReason());

        }

        return ResponseEntity.badRequest().body(e.getMessage());

    }

}
