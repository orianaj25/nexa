/* ==========================================
   PANEL SUPER ADMIN - alta y administración de comercios
========================================== */

const peso = new Intl.NumberFormat("es-AR", {style: "currency", currency: "ARS", maximumFractionDigits: 0});

let comercios = [];
let comercioUsuariosId = null;

const modal = id => bootstrap.Modal.getOrCreateInstance(document.getElementById(id));

/* Escapa texto antes de meterlo en innerHTML */
function esc(valor) {
    const d = document.createElement("div");
    d.textContent = valor == null ? "" : String(valor);
    return d.innerHTML;
}

async function api(url, opciones) {

    const response = await fetch(url, opciones);

    if (!response.ok) {
        const texto = await response.text();
        throw new Error(texto || "Error " + response.status);
    }

    const tipo = response.headers.get("content-type") || "";

    return tipo.includes("application/json") ? response.json() : response.text();
}

function jsonOpts(metodo, cuerpo) {
    return {
        method: metodo,
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(cuerpo || {})
    };
}

function mostrarError(id, mensaje) {
    const el = document.getElementById(id);
    el.textContent = mensaje;
    el.classList.remove("d-none");
}

function ocultar(...ids) {
    ids.forEach(id => document.getElementById(id).classList.add("d-none"));
}

/* ==========================================
   LISTADO
========================================== */

async function cargarComercios() {

    try {

        comercios = await api("/api/superadmin/comercios");

        renderTabla();

        renderKpis();

    } catch (e) {

        document.getElementById("alerta").innerHTML =
            `<div class="alert alert-danger">${esc(e.message)}</div>`;

    }
}

function renderKpis() {

    document.getElementById("kpiComercios").textContent = comercios.length;

    document.getElementById("kpiActivos").textContent =
        comercios.filter(c => c.activo).length;

    document.getElementById("kpiPedidos").textContent =
        comercios.reduce((s, c) => s + (c.pedidosHoy || 0), 0);

    document.getElementById("kpiVentas").textContent =
        peso.format(comercios.reduce((s, c) => s + Number(c.ventasHoy || 0), 0));
}

function renderTabla() {

    const tbody = document.getElementById("tablaComercios");

    if (comercios.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center text-muted py-4">
            Todavía no hay comercios. Creá el primero con “Nuevo comercio”.</td></tr>`;
        return;
    }

    tbody.innerHTML = comercios.map(c => `
        <tr class="${c.activo ? "" : "suspendido"}">
            <td><strong>${esc(c.nombre)}</strong></td>
            <td>${esc(c.rubro || "—")}</td>
            <td>${esc(c.administrador)}</td>
            <td class="text-end">${c.usuarios}</td>
            <td class="text-end">${c.productos}</td>
            <td class="text-end">${c.pedidosHoy}</td>
            <td class="text-end">${peso.format(Number(c.ventasHoy || 0))}</td>
            <td>${c.activo
                ? '<span class="badge text-bg-success">Activo</span>'
                : '<span class="badge text-bg-secondary">Suspendido</span>'}</td>
            <td class="text-end text-nowrap">
                <button class="btn btn-outline-primary btn-sm" title="Usuarios" onclick="abrirUsuarios(${c.id})"><i class="bi bi-people"></i></button>
                <button class="btn btn-outline-secondary btn-sm" title="Editar" onclick="abrirEditar(${c.id})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm ${c.activo ? "btn-outline-danger" : "btn-outline-success"}"
                        title="${c.activo ? "Suspender" : "Reactivar"}" onclick="cambiarEstado(${c.id})">
                    <i class="bi ${c.activo ? "bi-pause-circle" : "bi-play-circle"}"></i></button>
            </td>
        </tr>`).join("");
}

/* ==========================================
   NUEVO COMERCIO
========================================== */

function abrirNuevo() {

    ["nNombre", "nRubro", "nAdminNombre", "nAdminApellido", "nAdminUsuario", "nAdminPassword"]
        .forEach(id => document.getElementById(id).value = "");

    ocultar("errNuevo");

    modal("modalNuevo").show();
}

async function crearComercio() {

    ocultar("errNuevo");

    const valor = id => document.getElementById(id).value.trim();

    try {

        await api("/api/superadmin/comercios", jsonOpts("POST", {
            nombre: valor("nNombre"),
            rubro: valor("nRubro"),
            adminNombre: valor("nAdminNombre"),
            adminApellido: valor("nAdminApellido"),
            adminUsuario: valor("nAdminUsuario"),
            adminPassword: document.getElementById("nAdminPassword").value
        }));

        modal("modalNuevo").hide();

        await cargarComercios();

    } catch (e) {

        mostrarError("errNuevo", e.message);

    }
}

/* ==========================================
   EDITAR / SUSPENDER
========================================== */

function abrirEditar(id) {

    const c = comercios.find(x => x.id === id);

    document.getElementById("eId").value = c.id;
    document.getElementById("eNombre").value = c.nombre;
    document.getElementById("eRubro").value = c.rubro || "";

    ocultar("errEditar");

    modal("modalEditar").show();
}

async function guardarEdicion() {

    ocultar("errEditar");

    try {

        await api("/api/superadmin/comercios/" + document.getElementById("eId").value,
            jsonOpts("PUT", {
                nombre: document.getElementById("eNombre").value,
                rubro: document.getElementById("eRubro").value
            }));

        modal("modalEditar").hide();

        await cargarComercios();

    } catch (e) {

        mostrarError("errEditar", e.message);

    }
}

async function cambiarEstado(id) {

    const c = comercios.find(x => x.id === id);

    const mensaje = c.activo
        ? `¿Suspender "${c.nombre}"? Sus usuarios no van a poder ingresar (los datos se conservan).`
        : `¿Reactivar "${c.nombre}"?`;

    if (!confirm(mensaje)) {
        return;
    }

    try {

        await api(`/api/superadmin/comercios/${id}/estado`, jsonOpts("PUT"));

        await cargarComercios();

    } catch (e) {

        alert(e.message);

    }
}

/* ==========================================
   USUARIOS DE UN COMERCIO
========================================== */

async function abrirUsuarios(id) {

    comercioUsuariosId = id;

    const c = comercios.find(x => x.id === id);

    document.getElementById("tituloUsuarios").textContent = "Usuarios de " + c.nombre;

    ["uNombre", "uApellido", "uUsuario", "uPassword"]
        .forEach(campo => document.getElementById(campo).value = "");

    ocultar("errUsuarios", "okUsuarios");

    modal("modalUsuarios").show();

    await cargarUsuarios();
}

async function cargarUsuarios() {

    const tbody = document.getElementById("tablaUsuarios");

    try {

        const usuarios = await api(`/api/superadmin/comercios/${comercioUsuariosId}/usuarios`);

        tbody.innerHTML = usuarios.map(u => `
            <tr>
                <td>${esc(u.nombre)} ${esc(u.apellido)}</td>
                <td>${esc(u.usuario)}</td>
                <td>${u.rol === "ADMINISTRADOR" ? "Administrador" : "Vendedor"}</td>
                <td>${u.activo
                    ? '<span class="badge text-bg-success">Activo</span>'
                    : '<span class="badge text-bg-secondary">Inactivo</span>'}</td>
                <td class="text-end">
                    <button class="btn btn-outline-secondary btn-sm"
                            onclick="abrirPassword(${u.id}, '${esc(u.usuario).replace(/'/g, "")}')">
                        <i class="bi bi-key"></i></button>
                </td>
            </tr>`).join("");

    } catch (e) {

        mostrarError("errUsuarios", e.message);

    }
}

async function crearUsuario() {

    ocultar("errUsuarios", "okUsuarios");

    try {

        await api(`/api/superadmin/comercios/${comercioUsuariosId}/usuarios`, jsonOpts("POST", {
            nombre: document.getElementById("uNombre").value,
            apellido: document.getElementById("uApellido").value,
            usuario: document.getElementById("uUsuario").value,
            password: document.getElementById("uPassword").value,
            rol: document.getElementById("uRol").value
        }));

        ["uNombre", "uApellido", "uUsuario", "uPassword"]
            .forEach(campo => document.getElementById(campo).value = "");

        await cargarUsuarios();

        await cargarComercios();

    } catch (e) {

        mostrarError("errUsuarios", e.message);

    }
}

/* ==========================================
   RESTABLECER CONTRASEÑA
========================================== */

function abrirPassword(id, nombre) {

    document.getElementById("pUsuarioId").value = id;
    document.getElementById("pUsuarioNombre").textContent = "Usuario: " + nombre;
    document.getElementById("pPassword").value = "";

    modal("modalPassword").show();
}

async function guardarPassword() {

    try {

        await api(`/api/superadmin/usuarios/${document.getElementById("pUsuarioId").value}/password`,
            jsonOpts("PUT", {password: document.getElementById("pPassword").value}));

        modal("modalPassword").hide();

        const ok = document.getElementById("okUsuarios");
        ok.textContent = "Contraseña actualizada.";
        ok.classList.remove("d-none");

    } catch (e) {

        alert(e.message);

    }
}

document.addEventListener("DOMContentLoaded", cargarComercios);
