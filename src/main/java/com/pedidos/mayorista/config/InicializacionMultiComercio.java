package com.pedidos.mayorista.config;

import com.pedidos.mayorista.model.Comercio;
import com.pedidos.mayorista.model.Usuario;
import com.pedidos.mayorista.model.enums.Rol;
import com.pedidos.mayorista.repository.ComercioRepository;
import com.pedidos.mayorista.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;

/**
 * Se ejecuta en cada arranque (es idempotente) y deja la base lista para multi-comercio:
 *
 *  1. Permite el rol SUPER_ADMIN en la base existente (Hibernate no actualiza constraints viejos).
 *  2. Si hay datos de antes de esta versión (sin comercio), los asigna al comercio inicial.
 *     Así lo que hoy tenés en producción queda intacto, dentro de un comercio.
 *  3. Crea el SUPER_ADMIN si todavía no existe.
 */
@Component
public class InicializacionMultiComercio implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(InicializacionMultiComercio.class);

    private final JdbcTemplate jdbc;

    private final ComercioRepository comercioRepository;

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${nexa.superadmin.usuario:superadmin}")
    private String superAdminUsuario;

    @Value("${nexa.superadmin.password:}")
    private String superAdminPassword;

    @Value("${nexa.comercio-inicial:Volga}")
    private String nombreComercioInicial;

    public InicializacionMultiComercio(
            JdbcTemplate jdbc,
            ComercioRepository comercioRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.jdbc = jdbc;
        this.comercioRepository = comercioRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public void run(ApplicationArguments args) {

        permitirRolSuperAdmin();

        migrarDatosExistentes();

        crearSuperAdmin();

    }

    // =====================================================
    // 1. CONSTRAINT DEL ROL
    // =====================================================

    /*
     * Hibernate crea en Postgres un CHECK (rol IN ('ADMINISTRADOR','VENDEDOR')) y con
     * ddl-auto=update no lo modifica. Sin este paso, guardar un SUPER_ADMIN fallaría.
     */
    private void permitirRolSuperAdmin() {

        try {

            List<String> viejos = jdbc.queryForList(
                    """
                    SELECT conname
                    FROM pg_constraint
                    WHERE conrelid = 'usuario'::regclass
                      AND contype = 'c'
                      AND pg_get_constraintdef(oid) LIKE '%rol%'
                      AND pg_get_constraintdef(oid) NOT LIKE '%SUPER_ADMIN%'
                    """,
                    String.class
            );

            for (String nombre : viejos) {

                jdbc.execute("ALTER TABLE usuario DROP CONSTRAINT \"" + nombre + "\"");

            }

            if (!viejos.isEmpty()) {

                jdbc.execute(
                        "ALTER TABLE usuario ADD CONSTRAINT usuario_rol_check " +
                        "CHECK (rol IN ('SUPER_ADMIN','ADMINISTRADOR','VENDEDOR'))"
                );

                log.info("Constraint de rol actualizado para permitir SUPER_ADMIN.");

            }

        } catch (Exception e) {

            log.warn("No se pudo revisar el constraint del rol: {}", e.getMessage());

        }

    }

    // =====================================================
    // 2. DATOS PREVIOS -> COMERCIO INICIAL
    // =====================================================

    private void migrarDatosExistentes() {

        long huerfanos =
                contar("SELECT COUNT(*) FROM producto WHERE comercio_id IS NULL")
                + contar("SELECT COUNT(*) FROM pedido WHERE comercio_id IS NULL")
                + contar("SELECT COUNT(*) FROM caja WHERE comercio_id IS NULL")
                + contar("SELECT COUNT(*) FROM usuario WHERE comercio_id IS NULL AND rol <> 'SUPER_ADMIN'");

        if (huerfanos == 0) {
            return;
        }

        Comercio inicial = comercioRepository
                .findFirstByNombreIgnoreCase(nombreComercioInicial)
                .orElseGet(() -> {

                    Comercio nuevo = new Comercio();

                    nuevo.setNombre(nombreComercioInicial);
                    nuevo.setRubro("Mayorista");
                    nuevo.setActivo(true);

                    return comercioRepository.save(nuevo);
                });

        Long id = inicial.getId();

        jdbc.update("UPDATE producto SET comercio_id = ? WHERE comercio_id IS NULL", id);
        jdbc.update("UPDATE pedido SET comercio_id = ? WHERE comercio_id IS NULL", id);
        jdbc.update("UPDATE caja SET comercio_id = ? WHERE comercio_id IS NULL", id);
        jdbc.update("UPDATE usuario SET comercio_id = ? WHERE comercio_id IS NULL AND rol <> 'SUPER_ADMIN'", id);

        log.info("Migración multi-comercio: {} registros asignados al comercio '{}' (id {}).",
                huerfanos, inicial.getNombre(), id);

    }

    private long contar(String sql) {

        Long total = jdbc.queryForObject(sql, Long.class);

        return total == null ? 0 : total;

    }

    // =====================================================
    // 3. SUPER ADMIN
    // =====================================================

    private void crearSuperAdmin() {

        if (usuarioRepository.existsByRol(Rol.SUPER_ADMIN)) {
            return;
        }

        if (usuarioRepository.existsByUsuario(superAdminUsuario)) {

            log.warn("No se creó el SUPER_ADMIN: el usuario '{}' ya existe con otro rol. " +
                    "Definí SUPERADMIN_USUARIO con otro nombre.", superAdminUsuario);

            return;
        }

        boolean generada = superAdminPassword == null || superAdminPassword.isBlank();

        String password = generada ? generarPassword() : superAdminPassword;

        Usuario superAdmin = new Usuario();

        superAdmin.setNombre("Super");
        superAdmin.setApellido("Admin");
        superAdmin.setUsuario(superAdminUsuario);
        superAdmin.setPassword(passwordEncoder.encode(password));
        superAdmin.setRol(Rol.SUPER_ADMIN);
        superAdmin.setActivo(true);
        superAdmin.setComercioId(null);

        usuarioRepository.save(superAdmin);

        if (generada) {

            log.warn("==================================================================");
            log.warn(" SUPER_ADMIN creado. Usuario: '{}'  Contraseña: '{}'", superAdminUsuario, password);
            log.warn(" Guardala ahora y cambiala. Para fijarla: variable SUPERADMIN_PASSWORD.");
            log.warn("==================================================================");

        } else {

            log.info("SUPER_ADMIN '{}' creado con la contraseña de SUPERADMIN_PASSWORD.", superAdminUsuario);

        }

    }

    private String generarPassword() {

        String letras = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 16; i++) {

            sb.append(letras.charAt(random.nextInt(letras.length())));

        }

        return sb.toString();

    }

}
