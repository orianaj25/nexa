document.addEventListener("DOMContentLoaded", async () => {

    actualizarFecha();

    await cargarUsuario();

});


/* ==========================================
   VARIABLE GLOBAL DEL GRAFICO
========================================== */

let graficoVentas = null;


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

            await mostrarDashboardAdministrador();

        } else {

            await mostrarDashboardVendedor();

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
   CARGAR DASHBOARD ADMINISTRADOR
========================================== */

async function cargarDashboardAdministrador() {

    await Promise.all([

        cargarTarjetasAdministrador(),

        cargarUltimosPedidos(),

        cargarProductosMasVendidos(),

        crearGraficoVentas()

    ]);

}


/* ==========================================
   TARJETAS ADMINISTRADOR
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

        const response =
            await fetch("/api/dashboard/ultimos-pedidos");

        if (!response.ok) {

            throw new Error("Error cargando últimos pedidos");

        }

        const pedidos = await response.json();

        const tbody =
            document.getElementById("tablaUltimosPedidos");

        tbody.innerHTML = "";

        if (pedidos.length === 0) {

            tbody.innerHTML = `

                <tr>

                    <td colspan="4"
                        style="text-align:center;padding:20px;color:#888;">

                        No existen pedidos registrados.

                    </td>

                </tr>

            `;

            return;

        }

        pedidos.forEach(pedido => {

            tbody.innerHTML += `

                <tr>

                    <td>${pedido.numeroPedido}</td>

                    <td>${pedido.dniCliente ?? "-"}</td>

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
   PRODUCTOS MÁS VENDIDOS
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

        if (productos.length === 0) {

            tbody.innerHTML = `

                <tr>

                    <td colspan="2"
                        style="text-align:center;padding:20px;color:#888;">

                        Aún no existen ventas.

                    </td>

                </tr>

            `;

            return;

        }

        productos.forEach(producto => {

            tbody.innerHTML += `

                <tr>

                  <td>${producto.producto}</td>
                  <td>${producto.cantidadVendida}</td>

                </tr>

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

        const response = await fetch("/api/dashboard/vendedor");

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

        await cargarUltimosPedidosVendedor();

    } catch (error) {

        console.error(error);

    }

}


/* ==========================================
   TABLA ULTIMOS PEDIDOS VENDEDOR
========================================== */

async function cargarUltimosPedidosVendedor() {

    try {

        const response =
            await fetch("/api/dashboard/ultimos-pedidos");

        if (!response.ok) {

            throw new Error("Error cargando pedidos");

        }

        const pedidos = await response.json();

        const tbody =
            document.getElementById("tablaPedidosVendedor");

        tbody.innerHTML = "";

        if (pedidos.length === 0) {

            tbody.innerHTML = `

                <tr>

                    <td colspan="4"
                        style="text-align:center;padding:20px;color:#888;">

                        No existen pedidos registrados.

                    </td>

                </tr>

            `;

            return;

        }

        pedidos.forEach(pedido => {

            tbody.innerHTML += `

                <tr>

                    <td>${pedido.numeroPedido}</td>

                    <td>${pedido.dniCliente ?? "-"}</td>

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
   DASHBOARD VENDEDOR
========================================== */

async function cargarDashboardVendedor() {

    try {

        const response =
            await fetch("/api/dashboard/vendedor");

        if (!response.ok) {

            throw new Error("Error cargando dashboard vendedor");

        }

        const data = await response.json();

        document.getElementById("pendientesDia").innerText =
            formatearNumero(data.pendientes);

        document.getElementById("pagadosDia").innerText =
            formatearNumero(data.pagados);

        document.getElementById("entregadosDia").innerText =
            formatearNumero(data.entregados);

        document.getElementById("anuladosDia").innerText =
            formatearNumero(data.anulados);

        await cargarUltimosPedidosVendedor();

    } catch (error) {

        console.error(error);

    }

}


/* ==========================================
   ÚLTIMOS PEDIDOS VENDEDOR
========================================== */

async function cargarUltimosPedidosVendedor() {

    try {

        const response =
            await fetch("/api/dashboard/ultimos-pedidos");

        if (!response.ok) {

            throw new Error("Error cargando pedidos");

        }

        const pedidos = await response.json();

        const tbody =
            document.getElementById("tablaPedidosVendedor");

        tbody.innerHTML = "";

        if (pedidos.length === 0) {

            tbody.innerHTML = `

                <tr>

                    <td colspan="4"
                        style="text-align:center;padding:25px;color:#888;">

                        No existen pedidos registrados.

                    </td>

                </tr>

            `;

            return;

        }

        pedidos.forEach(pedido => {

            tbody.innerHTML += `

                <tr>

                    <td>${pedido.numeroPedido}</td>

                    <td>${pedido.dniCliente ?? "-"}</td>

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
   BADGES ESTADOS
========================================== */

function badgeEstado(estado) {

    switch (estado) {

        case "PENDIENTE_FACTURACION":

            return `
                <span class="badge bg-warning text-dark">
                    Pendiente
                </span>
            `;

        case "ENVIADO_A_FACTURACION":

            return `
                <span class="badge bg-primary">
                    Enviado a Facturación
                </span>
            `;

        case "FACTURADO":

            return `
                <span class="badge bg-success">
                    Facturado
                </span>
            `;

        case "ANULADO":

            return `
                <span class="badge bg-danger">
                    Anulado
                </span>
            `;

        default:

            return `
                <span class="badge bg-secondary">
                    ${estado}
                </span>
            `;

    }

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

        const canvas =
            document.getElementById("ventasChart");

        if (!canvas) return;

        // Evita el error de Chart.js al refrescar

        if (graficoVentas) {

            graficoVentas.destroy();

            graficoVentas = null;

        }

        const ctx = canvas.getContext("2d");

        graficoVentas = new Chart(ctx, {

            type: "line",

            data: {

                labels: result.labels,

                datasets: [

                    {

                        label: "Ventas últimos 7 días",

                        data: result.data,

                        borderColor: "#2563eb",

                        backgroundColor: "rgba(37,99,235,.15)",

                        fill: true,

                        tension: .35,

                        pointRadius: 4,

                        pointHoverRadius: 6,

                        pointBackgroundColor: "#2563eb"

                    }

                ]

            },

            options: {

                responsive: true,

                maintainAspectRatio: false,

                plugins: {

                    legend: {

                        display: true

                    }

                },

                scales: {

                    y: {

                        beginAtZero: true

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
        `<i class="bi bi-calendar3"></i> ${hoy.toLocaleDateString("es-AR", opciones)}`;

}


/* ==========================================
   FORMATEAR NÚMEROS
========================================== */

function formatearNumero(numero) {

    if (numero == null) {

        return "0";

    }

    return new Intl.NumberFormat("es-AR").format(numero);

}


/* ==========================================
   MENSAJE SIN DATOS
========================================== */

function mostrarSinDatos(idTabla, columnas, mensaje) {

    const tabla = document.getElementById(idTabla);

    if (!tabla) return;

    tabla.innerHTML = `

        <tr>

            <td colspan="${columnas}"
                style="text-align:center;padding:25px;color:#888;">

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
   ACTUALIZACIÓN AUTOMÁTICA
========================================== */

setInterval(async () => {

    try {

        const rol =
            document.getElementById("rolUsuario").innerText;

        if (rol === "ADMINISTRADOR") {

            await cargarDashboardAdministrador();

        } else {

            await cargarDashboardVendedor();

        }

    } catch (error) {

        console.error(error);

    }

}, 30000);