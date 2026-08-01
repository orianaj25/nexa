document.addEventListener("DOMContentLoaded", async () => {

    actualizarFecha();

    await cargarUsuario();

});


/* =========================
   USUARIO LOGUEADO
========================= */

async function cargarUsuario() {

    try {

        const response = await fetch("/api/usuarios/me");

        if (!response.ok) {
            throw new Error("No se pudo obtener el usuario.");
        }

        const usuario = await response.json();

        // Bienvenida
        document.querySelector(".topbar-left h3").innerText =
            `¡Bienvenido, ${usuario.nombre}!`;

        // Según el rol cargamos un dashboard distinto
        if (usuario.rol === "ADMINISTRADOR") {

            cargarDashboardAdministrador();

        } else {

            configurarDashboardVendedor();

        }

    } catch (error) {

        console.error(error);

    }

}


/* =========================
   DASHBOARD ADMINISTRADOR
========================= */

async function cargarDashboardAdministrador() {

    mostrarOpcionesAdministrador();

    await cargarDashboard();

    await crearGraficoVentas();

}


/* =========================
   DASHBOARD VENDEDOR
========================= */

function configurarDashboardVendedor() {

    ocultarOpcionesAdministrador();

    // Cambiar títulos de las tarjetas
    document.querySelector("#ventasDia")
        .parentElement
        .querySelector(".card-title")
        .innerText = "Pedidos Pendientes";

    document.querySelector("#pedidosDia")
        .parentElement
        .querySelector(".card-title")
        .innerText = "Pedidos Entregados";

    document.querySelector("#clientesDia")
        .parentElement
        .querySelector(".card-title")
        .innerText = "Pedidos Anulados";

    document.querySelector("#productosDia")
        .parentElement
        .querySelector(".card-title")
        .innerText = "Pedidos Totales";

    // Valores temporales
    document.getElementById("ventasDia").innerText = "-";
    document.getElementById("pedidosDia").innerText = "-";
    document.getElementById("clientesDia").innerText = "-";
    document.getElementById("productosDia").innerText = "-";

    // Ocultar gráfico por ahora
    document.querySelector(".chart-card").style.display = "none";

}


/* =========================
   OCULTAR OPCIONES ADMIN
========================= */

function ocultarOpcionesAdministrador() {

    document.querySelector('a[href="arqueo.html"]')
        .parentElement.style.display = "none";

    document.querySelector('a[href="historial-cajas.html"]')
        .parentElement.style.display = "none";

}


/* =========================
   MOSTRAR OPCIONES ADMIN
========================= */

function mostrarOpcionesAdministrador() {

    document.querySelector('a[href="arqueo.html"]')
        .parentElement.style.display = "";

    document.querySelector('a[href="historial-cajas.html"]')
        .parentElement.style.display = "";

}


/* =========================
   DASHBOARD TARJETAS
========================= */

async function cargarDashboard() {

    try {

        const response = await fetch("/api/dashboard");

        if (!response.ok) {
            throw new Error("Error al cargar dashboard");
        }

        const data = await response.json();

        document.getElementById("ventasDia").innerText =
            "$" + formatearNumero(data.ventasDelDia);

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


/* =========================
   FECHA
========================= */

function actualizarFecha() {

    const hoy = new Date();

    const opciones = {

        weekday: "long",
        day: "numeric",
        month: "long",
        year: "numeric"

    };

    const fecha = hoy.toLocaleDateString("es-AR", opciones);

    document.getElementById("fechaActual").innerHTML =
        `<i class="bi bi-calendar3"></i> ${fecha}`;

}


/* =========================
   FORMATO NUMEROS
========================= */

function formatearNumero(numero) {

    return new Intl.NumberFormat("es-AR").format(numero);

}


/* =========================
   GRAFICO
========================= */

async function crearGraficoVentas() {

    try {

        const response = await fetch("/ventas-semana");

        if (!response.ok) {
            throw new Error("Error al cargar ventas semana");
        }

        const result = await response.json();

        const ctx = document.getElementById("ventasChart");

        if (!ctx) return;

        new Chart(ctx, {

            type: "line",

            data: {

                labels: result.labels,

                datasets: [{

                    label: "Ventas últimos 7 días",

                    data: result.data,

                    borderColor: "#3b82f6",

                    backgroundColor: "rgba(59,130,246,.15)",

                    fill: true,

                    tension: .4,

                    pointBackgroundColor: "#3b82f6",

                    pointRadius: 4

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