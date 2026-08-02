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
   ADMINISTRADOR
========================================== */

async function mostrarDashboardAdministrador() {

    document.getElementById("dashboardAdministrador").style.display = "block";
    document.getElementById("dashboardVendedor").style.display = "none";

    document.getElementById("menuArqueo").style.display = "";
    document.getElementById("menuHistorialCajas").style.display = "";
    document.getElementById("menuUsuarios").style.display = "";

    await cargarDashboard();

    await crearGraficoVentas();

}


/* ==========================================
   VENDEDOR
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
   DASHBOARD ADMIN
========================================== */

async function cargarDashboard() {

    try {

        const response = await fetch("/api/dashboard");

        if (!response.ok)
            throw new Error("Error cargando dashboard");

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


/* ==========================================
   DASHBOARD VENDEDOR
========================================== */

async function cargarDashboardVendedor() {

    try {

        /*
            Lo conectaremos cuando hagamos el endpoint.

            Por ahora dejamos valores temporales.
        */

        document.getElementById("pendientesDia").innerText = "-";
        document.getElementById("pagadosDia").innerText = "-";
        document.getElementById("entregadosDia").innerText = "-";
        document.getElementById("anuladosDia").innerText = "-";

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

    const fecha = hoy.toLocaleDateString("es-AR", opciones);

    document.getElementById("fechaActual").innerHTML =
        `<i class="bi bi-calendar3"></i> ${fecha}`;

}


/* ==========================================
   FORMATO NUMEROS
========================================== */

function formatearNumero(numero) {

    return new Intl.NumberFormat("es-AR").format(numero);

}


/* ==========================================
   GRAFICO VENTAS
========================================== */

async function crearGraficoVentas() {

    try {

        const response = await fetch("/ventas-semana");

        if (!response.ok)
            throw new Error("Error cargando ventas");

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