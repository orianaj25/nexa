# NEXA multi-comercio

Un mismo despliegue (misma app, misma base Postgres en Render) sirve a varios comercios.
Cada comercio ve y modifica **solo** lo suyo.

```
SUPER_ADMIN
 ├── Comercio A ── Admin ── Vendedores ── productos, pedidos, caja
 ├── Comercio B ── Admin ── Vendedores ── productos, pedidos, caja
 └── Comercio C ── Admin ── Vendedores ── productos, pedidos, caja
```

## Roles

| Rol | Qué hace | Pantalla de inicio |
|---|---|---|
| `SUPER_ADMIN` | Crea, edita, suspende comercios; crea usuarios y resetea contraseñas. No opera ventas. | `/superadmin.html` |
| `ADMINISTRADOR` | Todo lo de su comercio + gestiona sus usuarios (`/usuarios.html`). | `/dashboard.html` |
| `VENDEDOR` | Operación de su comercio (pedidos, productos, caja), igual que antes. | `/dashboard.html` |

Los nombres de rol `ADMINISTRADOR` y `VENDEDOR` no cambiaron, así que el front y los datos existentes siguen funcionando.

## Variables de entorno nuevas (Render → Environment)

| Variable | Para qué | Default |
|---|---|---|
| `SUPERADMIN_USUARIO` | Usuario del super admin | `superadmin` |
| `SUPERADMIN_PASSWORD` | Contraseña del super admin | si falta, se genera una y se imprime **una sola vez** en los logs |
| `COMERCIO_INICIAL` | Nombre del comercio que recibe tus datos actuales | `Volga` |

Recomendado: definir `SUPERADMIN_PASSWORD` antes del primer deploy.

## Qué pasa en el primer arranque (automático, idempotente)

1. Hibernate agrega la tabla `comercio` y las columnas `comercio_id` (productos, pedidos, cajas, usuarios).
2. Se actualiza el constraint del rol en `usuario` para aceptar `SUPER_ADMIN`.
3. **Todos tus datos actuales** (productos, pedidos, cajas, usuarios) se asignan al comercio inicial (`Volga`).
4. Se crea el super admin si no existe.

Resultado: tus usuarios actuales entran igual que antes, ahora dentro del comercio "Volga".

## Alquilar a un comercio nuevo

1. Entrar con el super admin → **Nuevo comercio** → completar nombre, rubro y datos del administrador.
2. Pasarle al cliente su usuario y contraseña.
3. El cliente crea sus vendedores desde **Usuarios**, carga su catálogo (manual o Excel) y abre su caja.

Para cortar el servicio a un comercio: **Suspender**. Sus usuarios quedan afuera de inmediato y los datos se conservan; **Reactivar** lo devuelve.

## Cómo se garantiza el aislamiento

- El comercio se toma de la **sesión del usuario logueado**, nunca de un parámetro que mande el navegador (`security/ComercioContext`).
- Todas las consultas filtran por `comercio_id`, y las búsquedas por id usan `findByIdAndComercioId`: pedir el id de un registro ajeno devuelve "no encontrado".
- La caja abierta, los códigos de producto y los cierres son por comercio.
- Un administrador no puede crear usuarios `SUPER_ADMIN` ni mover usuarios a otro comercio.

## Qué cambió en el código

- **Nuevo:** `model/Comercio`, `repository/ComercioRepository`, `security/*`, `service/ComercioService`, `controller/SuperAdminController`, `config/InicializacionMultiComercio`, DTOs de comercio, `superadmin.html`, `usuarios.html`, `js/comercio.js`.
- **Modificado:** modelos (`comercioId`), todos los repositorios y servicios (filtro por comercio), `SecurityConfig`, `UsuarioController` (`/me` ahora incluye el comercio), ticket PDF (muestra el nombre del comercio en vez de "VOLGA"), pantallas (nombre del comercio en la barra).
- **Corregido de paso:** el "hoy" del dashboard usaba la zona horaria del servidor (UTC en Render) en lugar de la argentina, y "ventas últimos 7 días" perdía las ventas de la mañana del día más antiguo.

## Pendientes recomendados (no incluidos)

- Hacer un backup/snapshot de la base de Render antes del primer deploy.
- Numeración de pedidos por comercio (hoy `PED-000123` usa el id global).
- Descuento de stock al vender (el proyecto original tampoco lo hacía).
