package com.pedidos.mayorista.service;

import com.pedidos.mayorista.dto.ComercioRequest;
import com.pedidos.mayorista.dto.ComercioResumenDTO;
import com.pedidos.mayorista.dto.CrearComercioRequest;
import com.pedidos.mayorista.model.Comercio;
import com.pedidos.mayorista.model.Usuario;
import com.pedidos.mayorista.model.enums.Rol;
import com.pedidos.mayorista.repository.ComercioRepository;
import com.pedidos.mayorista.repository.PedidoRepository;
import com.pedidos.mayorista.repository.ProductoRepository;
import com.pedidos.mayorista.repository.UsuarioRepository;
import com.pedidos.mayorista.security.ComercioActivoFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones del SUPER_ADMIN sobre los comercios.
 */
@Service
public class ComercioService {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final ComercioRepository comercioRepository;

    private final UsuarioRepository usuarioRepository;

    private final ProductoRepository productoRepository;

    private final PedidoRepository pedidoRepository;

    private final UsuarioService usuarioService;

    private final ComercioActivoFilter comercioActivoFilter;

    public ComercioService(
            ComercioRepository comercioRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            UsuarioService usuarioService,
            ComercioActivoFilter comercioActivoFilter) {

        this.comercioRepository = comercioRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioService = usuarioService;
        this.comercioActivoFilter = comercioActivoFilter;

    }

    // ==========================
    // LISTAR (con métricas)
    // ==========================

    public List<ComercioResumenDTO> listar() {

        LocalDate hoy = LocalDate.now(ZONA);

        LocalDateTime inicio = hoy.atStartOfDay();

        LocalDateTime fin = hoy.atTime(23, 59, 59);

        List<ComercioResumenDTO> respuesta = new ArrayList<>();

        for (Comercio c : comercioRepository.findAll()) {

            String administrador = usuarioRepository
                    .findFirstByComercioIdAndRolOrderByIdAsc(
                            c.getId(), Rol.ADMINISTRADOR)
                    .map(Usuario::getUsuario)
                    .orElse("—");

            BigDecimal ventas =
                    pedidoRepository.ventasDelDia(c.getId(), inicio, fin);

            respuesta.add(new ComercioResumenDTO(
                    c.getId(),
                    c.getNombre(),
                    c.getRubro(),
                    c.getActivo(),
                    c.getFechaCreacion(),
                    administrador,
                    usuarioRepository.countByComercioId(c.getId()),
                    productoRepository.countByComercioId(c.getId()),
                    pedidoRepository.pedidosDelDia(c.getId(), inicio, fin),
                    ventas
            ));

        }

        return respuesta;

    }

    // ==========================
    // CREAR COMERCIO + SU ADMINISTRADOR
    // ==========================

    @Transactional
    public Comercio crear(CrearComercioRequest request) {

        String nombre = textoObligatorio(request.getNombre(), "El nombre del comercio es obligatorio.");

        if (comercioRepository.existsByNombreIgnoreCase(nombre)) {

            throw new RuntimeException("Ya existe un comercio con ese nombre.");

        }

        Comercio comercio = new Comercio();

        comercio.setNombre(nombre);

        comercio.setRubro(request.getRubro() == null ? null : request.getRubro().trim());

        comercio.setActivo(true);

        comercio = comercioRepository.save(comercio);

        Usuario admin = new Usuario();

        admin.setNombre(request.getAdminNombre());
        admin.setApellido(request.getAdminApellido());
        admin.setUsuario(request.getAdminUsuario());
        admin.setPassword(request.getAdminPassword());
        admin.setRol(Rol.ADMINISTRADOR);

        // Si algo falla acá (usuario repetido, datos faltantes) se deshace también el comercio
        usuarioService.crearEnComercio(admin, comercio.getId());

        return comercio;

    }

    // ==========================
    // EDITAR
    // ==========================

    public Comercio actualizar(Long id, ComercioRequest request) {

        Comercio comercio = buscar(id);

        String nombre = textoObligatorio(request.getNombre(), "El nombre del comercio es obligatorio.");

        if (comercioRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {

            throw new RuntimeException("Ya existe un comercio con ese nombre.");

        }

        comercio.setNombre(nombre);

        comercio.setRubro(request.getRubro() == null ? null : request.getRubro().trim());

        return comercioRepository.save(comercio);

    }

    // ==========================
    // SUSPENDER / REACTIVAR
    // ==========================

    public Comercio cambiarEstado(Long id) {

        Comercio comercio = buscar(id);

        comercio.setActivo(!comercio.getActivo());

        comercio = comercioRepository.save(comercio);

        // La suspensión se aplica enseguida, también a quienes ya tienen la sesión abierta
        comercioActivoFilter.invalidar(id);

        return comercio;

    }

    // ==========================
    // USUARIOS DE UN COMERCIO
    // ==========================

    public List<Usuario> listarUsuarios(Long comercioId) {

        buscar(comercioId);

        return usuarioRepository.findAllByComercioId(comercioId);

    }

    public Usuario crearUsuario(Long comercioId, Usuario usuario) {

        buscar(comercioId);

        return usuarioService.crearEnComercio(usuario, comercioId);

    }

    public void restablecerPassword(Long usuarioId, String password) {

        usuarioService.restablecerPassword(usuarioId, password);

    }

    // ==========================
    // AUXILIARES
    // ==========================

    private Comercio buscar(Long id) {

        return comercioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Comercio no encontrado"));

    }

    private String textoObligatorio(String valor, String mensaje) {

        if (valor == null || valor.isBlank()) {

            throw new RuntimeException(mensaje);

        }

        return valor.trim();

    }

}
