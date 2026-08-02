document.addEventListener("DOMContentLoaded", async () => {

    actualizarFecha();

    await cargarUsuario();

});


/* ==========================================
   USUARIO LOGUEADO
========================================== */

async function cargarUsuario() {

    try {

        const response = await fetch("/api/usuarios/me");

        if (!response.ok) {

            throw new Error("No se pudo obtener el usuario.");

        }

        const usuario = await response.json();

        document.getElementById("saludoUsuario").innerText =
            `¡Bienvenido, ${usuario.nombre}!`;

        document.getElementById("rolUsuario").innerText =
            usuario.rol;

        if (usuario.rol === "ADMINISTRADOR") {

            mostrarDashboardAdministrador();

        } else {

            mostrarDashboardVendedor();

        }

    } catch (error) {

        console.error(error);

    }

}


/* ==========================================
   DASHBOARD ADMINISTRADOR
========================================== */

async function mostrarDashboardAdministrador() {

    document.getElementById("dashboardAdministrador").style.display = "block";
    document.getElementById("dashboardVendedor").style.display = "none";

    document.getElementById("menuArqueo").style.display = "";
    document.getElementById("menuHistorialCajas").style.display = "";
    document.getElementById("menuUsuarios").style.display = "";

    await cargarDashboardAdministrador();

}


/* ==========================================
   DASHBOARD VENDEDOR
========================================== */

async function mostrarDashboardVendedor() {

    document.getElementById("dashboardAdministrador").style.display = "none";
    document.getElementById("dashboardVendedor").style.display = "block";

    document.getElementById("menuArqueo").style.display = "none";
    document.getElementById("menuHistorialCajas").style.display = "none";
    document.getElementById("menuUsuarios").style.display = "none";

    await cargarDashboardVendedor();

}
/* ==========================================
   DASHBOARD ADMINISTRADOR
========================================== */

async function cargarDashboardAdministrador() {

    await Promise.all([

        cargarTarjetasAdministrador(),
        crearGraficoVentas(),
        cargarUltimosPedidos(),
        cargarProductosMasVendidos(),
        cargarResumenEstados()

    ]);

}


/* ==========================================
   TARJETAS
========================================== */

async function cargarTarjetasAdministrador() {

    try {

        const response = await fetch("/api/dashboard");

        if (!response.ok) {

            throw new Error("Error cargando dashboard");

        }

        const data = await response.json();

        document.getElementById("ventasDia").innerText =
            "$ " + formatearNumero(data.ventasDelDia);

        document.getElementById("pedidosDia").innerText =
            data.pedidosDelDia;

        document.getElementById("clientesDia").innerText =
            data.clientesAtendidos;

        document.getElementById("productosDia").innerText =
            data.productosVendidos;

    } catch (error) {

        console.error(error);

    }

}


/* ==========================================
   ULTIMOS PEDIDOS
========================================== */

async function cargarUltimosPedidos() {

    try {

        const response = await fetch("/api/dashboard/ultimos-pedidos");

        if (!response.ok) {

            throw new Error("Error cargando últimos pedidos");

        }

        const pedidos = await response.json();

        const tbody =
            document.getElementById("tablaUltimosPedidos");

        tbody.innerHTML = "";

        pedidos.forEach(pedido => {

            tbody.innerHTML += `

                <tr>

                    <td>${pedido.numeroPedido}</td>

                    <td>${pedido.dniCliente}</td>

                    <td>${badgeEstado(pedido.estado)}</td>

                    <td>$ ${formatearNumero(pedido.total)}</td>

                </tr>

            `;

        });

    } catch (error) {

        console.error(error);

    }

}


/* ==========================================
   PRODUCTOS MAS VENDIDOS
========================================== */

async function cargarProductosMasVendidos() {

    try {

        const response =
            await fetch("/api/dashboard/productos-mas-vendidos");

        if (!response.ok) {

            throw new Error("Error cargando productos");

        }

        const productos = await response.json();

        const tbody =
            document.getElementById("tablaProductosVendidos");

        tbody.innerHTML = "";

        productos.forEach(producto => {

            tbody.innerHTML += `

                <tr>

                    <td>${producto.nombre}</td>

                    <td>${producto.cantidadVendida}</td>

                </tr>

            `;

        });

    } catch (error) {

        console.error(error);

    }

}


/* ==========================================
   RESUMEN POR ESTADO
========================================== */

async function cargarResumenEstados() {

    try {

        const response =
            await fetch("/api/dashboard/resumen-estados");

        if (!response.ok) {

            throw new Error("Error cargando estados");

        }

        const estados = await response.json();

        const contenedor =
            document.getElementById("resumenEstados");

        contenedor.innerHTML = "";

        estados.forEach(item => {

            contenedor.innerHTML += `

                <div class="estado-card">

                    ${badgeEstado(item.estado)}

                    <h2>${item.cantidad}</h2>

                </div>

            `;

        });

    } catch (error) {

        console.error(error);

    }

}
/* ==========================================
   DASHBOARD VENDEDOR
========================================== */

async function cargarDashboardVendedor() {

    try {

        const response =
            await fetch("/api/dashboard/dashboard-vendedor");

        if (!response.ok) {

            throw new Error("Error cargando dashboard vendedor");

        }

        const data = await response.json();

        document.getElementById("pendientesDia").innerText =
            data.pendientes;

        document.getElementById("facturacionDia").innerText =
            data.facturacion;

        document.getElementById("facturadosDia").innerText =
            data.facturados;

        document.getElementById("anuladosDia").innerText =
            data.anulados;

        cargarTablaPedidosVendedor(data.ultimosPedidos);

        cargarResumenEstadosVendedor(data.resumenEstados);

    } catch (error) {

        console.error(error);

    }

}


/* ==========================================
   TABLA ULTIMOS PEDIDOS VENDEDOR
========================================== */

function cargarTablaPedidosVendedor(pedidos) {

    const tbody =
        document.getElementById("tablaPedidosVendedor");

    tbody.innerHTML = "";

    pedidos.forEach(pedido => {

        tbody.innerHTML += `

            <tr>

                <td>${pedido.numeroPedido}</td>

                <td>${pedido.dniCliente}</td>

                <td>${badgeEstado(pedido.estado)}</td>

                <td>$ ${formatearNumero(pedido.total)}</td>

            </tr>

        `;

    });

}


/* ==========================================
   RESUMEN ESTADOS VENDEDOR
========================================== */

function cargarResumenEstadosVendedor(estados) {

    const contenedor =
        document.getElementById("resumenEstadosVendedor");

    contenedor.innerHTML = "";

    estados.forEach(item => {

        contenedor.innerHTML += `

            <div class="estado-card">

                ${badgeEstado(item.estado)}

                <h2>${item.cantidad}</h2>

            </div>

        `;

    });

}

/* ==========================================
   BADGES DE ESTADO
========================================== */

function badgeEstado(estado) {

    let clase = "";

    switch (estado) {

        case "PENDIENTE_FACTURACION":

            clase = "badge bg-warning text-dark";
            estado = "Pendiente";

            break;

        case "ENVIADO_A_FACTURACION":

            clase = "badge bg-primary";
            estado = "Enviado";

            break;

        case "FACTURADO":

            clase = "badge bg-success";
            estado = "Facturado";

            break;

        case "ANULADO":

            clase = "badge bg-danger";
            estado = "Anulado";

            break;

        default:

            clase = "badge bg-secondary";

    }

    return `<span class="${clase}">${estado}</span>`;

}


/* ==========================================
   GRAFICO VENTAS
========================================== */

async function crearGraficoVentas() {

    try {

        const response =
            await fetch("/api/dashboard/ventas-semana");

        if (!response.ok) {

            throw new Error("Error cargando gráfico");

        }

        const result = await response.json();

        const ctx =
            document.getElementById("ventasChart");

        if (!ctx) return;

        new Chart(ctx, {

            type: "line",

            data: {

                labels: result.labels,

                datasets: [{

                    label: "Ventas últimos 7 días",

                    data: result.data,

                    borderColor: "#3b82f6",

                    backgroundColor:
                        "rgba(59,130,246,.15)",

                    fill: true,

                    tension: .4,

                    pointRadius: 4,

                    pointBackgroundColor:
                        "#3b82f6"

                }]

            },

            options: {

                responsive: true,

                plugins: {

                    legend: {

                        display: true

                    }

                }

            }

        });

    } catch (error) {

        console.error(error);

    }

}


/* ==========================================
   FECHA
========================================== */

function actualizarFecha() {

    const hoy = new Date();

    const opciones = {

        weekday: "long",
        day: "numeric",
        month: "long",
        year: "numeric"

    };

    document.getElementById("fechaActual").innerHTML =
        `<i class="bi bi-calendar3"></i> ${
            hoy.toLocaleDateString("es-AR", opciones)
        }`;

}


/* ==========================================
   FORMATO NUMEROS
========================================== */

function formatearNumero(numero) {

    return new Intl.NumberFormat("es-AR")
        .format(numero);

}

/* ==========================================
   MENSAJES CUANDO NO HAY DATOS
========================================== */

function mostrarSinDatos(idTabla, columnas, mensaje) {

    const tabla = document.getElementById(idTabla);

    if (!tabla) return;

    tabla.innerHTML = `

        <tr>

            <td colspan="${columnas}"
                style="text-align:center;color:#888;padding:25px;">

                ${mensaje}

            </td>

        </tr>

    `;

}


/* ==========================================
   LOADER SIMPLE
========================================== */

function mostrarCargando(idTabla, columnas) {

    const tabla = document.getElementById(idTabla);

    if (!tabla) return;

    tabla.innerHTML = `

        <tr>

            <td colspan="${columnas}"
                style="text-align:center;padding:20px;">

                Cargando...

            </td>

        </tr>

    `;

}


/* ==========================================
   VARIABLE GLOBAL DEL GRAFICO
========================================== */

let graficoVentas = null;


/* ==========================================
   DESTRUIR GRAFICO SI YA EXISTE
========================================== */

function destruirGrafico() {

    if (graficoVentas != null) {

        graficoVentas.destroy();

        graficoVentas = null;

    }

}

/* ==========================================
   ACTUALIZAR DASHBOARD AUTOMÁTICAMENTE
========================================== */

setInterval(async () => {

    try {

        const rol = document.getElementById("rolUsuario").innerText;

        if (rol === "ADMINISTRADOR") {

            await cargarDashboardAdministrador();

        } else {

            await cargarDashboardVendedor();

        }

    } catch (error) {

        console.error(error);

    }

}, 30000);