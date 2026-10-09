/* ==========================================
   USUARIOS DEL COMERCIO (solo ADMINISTRADOR)
========================================== */

let usuarios = [];

const modalUsuario = () =>
    bootstrap.Modal.getOrCreateInstance(document.getElementById("modalUsuario"));

function esc(valor) {
    const d = document.createElement("div");
    d.textContent = valor == null ? "" : String(valor);
    return d.innerHTML;
}

async function api(url, opciones) {

    const response = await fetch(url, opciones);

    if (!response.ok) {
        throw new Error((await response.text()) || "Error " + response.status);
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

async function cargar() {

    try {

        usuarios = await api("/api/usuarios");

        render();

    } catch (e) {

        document.getElementById("alerta").innerHTML =
            `<div class="alert alert-danger">${esc(e.message)}</div>`;

    }
}

function render() {

    document.getElementById("tablaUsuarios").innerHTML = usuarios.map(u => `
        <tr>
            <td>${esc(u.nombre)} ${esc(u.apellido)}</td>
            <td>${esc(u.usuario)}</td>
            <td>${u.rol === "ADMINISTRADOR" ? "Administrador" : "Vendedor"}</td>
            <td>${u.activo
                ? '<span class="badge text-bg-success">Activo</span>'
                : '<span class="badge text-bg-secondary">Inactivo</span>'}</td>
            <td class="text-end text-nowrap">
                <button class="btn btn-outline-secondary btn-sm" onclick="abrirEditar(${u.id})">Editar</button>
                <button class="btn btn-sm ${u.activo ? "btn-outline-danger" : "btn-outline-success"}"
                        onclick="cambiarEstado(${u.id})">${u.activo ? "Desactivar" : "Activar"}</button>
            </td>
        </tr>`).join("");
}

function limpiarError() {
    document.getElementById("errUsuario").classList.add("d-none");
}

function abrirNuevo() {

    limpiarError();

    document.getElementById("tituloModal").textContent = "Nuevo usuario";
    document.getElementById("uId").value = "";

    ["uNombre", "uApellido", "uUsuario", "uPassword"]
        .forEach(id => document.getElementById(id).value = "");

    document.getElementById("uRol").value = "VENDEDOR";
    document.getElementById("uUsuario").disabled = false;
    document.getElementById("ayudaPassword").textContent = "";

    modalUsuario().show();
}

function abrirEditar(id) {

    limpiarError();

    const u = usuarios.find(x => x.id === id);

    document.getElementById("tituloModal").textContent = "Editar usuario";
    document.getElementById("uId").value = u.id;
    document.getElementById("uNombre").value = u.nombre;
    document.getElementById("uApellido").value = u.apellido;
    document.getElementById("uUsuario").value = u.usuario;
    document.getElementById("uUsuario").disabled = true;
    document.getElementById("uPassword").value = "";
    document.getElementById("uRol").value = u.rol;
    document.getElementById("ayudaPassword").textContent = "(vacía = no cambia)";

    modalUsuario().show();
}

async function guardar() {

    limpiarError();

    const id = document.getElementById("uId").value;

    const cuerpo = {
        nombre: document.getElementById("uNombre").value,
        apellido: document.getElementById("uApellido").value,
        usuario: document.getElementById("uUsuario").value,
        password: document.getElementById("uPassword").value,
        rol: document.getElementById("uRol").value,
        activo: true
    };

    try {

        if (id) {

            const actual = usuarios.find(x => x.id === Number(id));

            cuerpo.activo = actual.activo;

            await api("/api/usuarios/" + id, jsonOpts("PUT", cuerpo));

        } else {

            await api("/api/usuarios", jsonOpts("POST", cuerpo));

        }

        modalUsuario().hide();

        await cargar();

    } catch (e) {

        const el = document.getElementById("errUsuario");
        el.textContent = e.message;
        el.classList.remove("d-none");

    }
}

async function cambiarEstado(id) {

    try {

        await api(`/api/usuarios/${id}/estado`, jsonOpts("PUT"));

        await cargar();

    } catch (e) {

        alert(e.message);

    }
}

document.addEventListener("DOMContentLoaded", cargar);
