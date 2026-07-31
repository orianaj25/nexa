const API_PEDIDOS = "/api/pedidos";

let historialGlobal = [];
let detalleGlobal = [];
let estadoActual = "ENVIADO_A_FACTURACION";

/* =========================
   FORMATEAR FECHA
========================= */

function formatearFecha(fechaIso) {

    const f = new Date(fechaIso);

    return f.toLocaleString("es-AR", {
        timeZone: "America/Argentina/Buenos_Aires",
        day: "2-digit",
        month: "long",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });

}

/* =========================
   ESTADOS
========================= */

function obtenerEstado(estado){

    switch(estado){

        case "ENVIADO_A_FACTURACION":
            return {
                texto:"Enviado",
                badge:"bg-primary"
            };

        case "PENDIENTE_FACTURACION":
            return {
                texto:"Pendiente",
                badge:"bg-warning text-dark"
            };

        case "FACTURADO":
            return {
                texto:"Facturado",
                badge:"bg-success"
            };

        case "ANULADO":
            return {
                texto:"Anulado",
                badge:"bg-danger"
            };

        default:
            return {
                texto:estado,
                badge:"bg-secondary"
            };

    }

}

/* =========================
   CAMBIAR ESTADO
========================= */

function cambiarEstado(){

    estadoActual =
        document.getElementById("filtroEstado").value;

    cargarDetalle();

}

/* =========================
   CARGAR HISTORIAL
========================= */

function cargarDetalle(){

    let endpoint="/historial";

    if(estadoActual==="ANULADO"){

        endpoint="/anulados";

    }else if(estadoActual==="TODOS"){

        endpoint="/todos";

    }

    Promise.all([

        axios.get(API_PEDIDOS+endpoint),

        axios.get(API_PEDIDOS+"/detalle")

    ])

    .then(([historial,detalle])=>{

        historialGlobal=historial.data;

        detalleGlobal=detalle.data;

        renderDetalle(historialGlobal);

    })

    .catch(error=>{

        console.error(error);

    });

}

/* =========================
   RENDER
========================= */

function renderDetalle(lista){

    const tabla =
        document.getElementById("tablaDetalle");

    tabla.innerHTML="";

    lista.forEach(pedido=>{

        const estado =
            obtenerEstado(pedido.estado);

        let botones=`

            <button
                class="btn btn-sm btn-primary"
                onclick="verPedido(${pedido.pedidoId})">

                Ver

            </button>

        `;

        if(pedido.estado!=="ANULADO"){

            botones+=`

                <button
                    class="btn btn-sm btn-success"
                    onclick="descargarTicket(${pedido.pedidoId})">

                    Ticket

                </button>

                <button
                    class="btn btn-sm btn-danger"
                    onclick="anularPedido(${pedido.pedidoId})">

                    Anular

                </button>

            `;

        }else{

            botones+=`

                <button
                    class="btn btn-sm btn-warning"
                    onclick="restaurarPedido(${pedido.pedidoId})">

                    Restaurar

                </button>

            `;

        }

        tabla.innerHTML+=`

            <tr>

                <td>${pedido.numeroPedido}</td>

                <td>${formatearFecha(pedido.fecha)}</td>

                <td>${pedido.cantidadProductos}</td>

                <td>$${pedido.total}</td>

                <td>${pedido.metodoPago}</td>

                <td>

                    <span class="badge ${estado.badge}">

                        ${estado.texto}

                    </span>

                </td>

                <td>

                    ${botones}

                </td>

            </tr>

        `;

    });

}

/* =========================
   FILTRAR
========================= */

function filtrarDetalle(){

    const texto =
        document
            .getElementById("filtro")
            .value
            .toLowerCase();

    const filtrados = historialGlobal.filter(p=>

        p.numeroPedido.toLowerCase().includes(texto)

        ||

        formatearFecha(p.fecha)
            .toLowerCase()
            .includes(texto)

        ||

        p.metodoPago.toLowerCase().includes(texto)

    );

    renderDetalle(filtrados);

}

/* =========================
   VER PEDIDO
========================= */

function verPedido(id){

    const pedido =
        detalleGlobal.filter(
            d=>d.pedidoId===id
        );

    if(pedido.length===0) return;

    document.getElementById("modalId").innerText =
        pedido[0].numeroPedido;

    document.getElementById("modalFecha").innerText =
        formatearFecha(pedido[0].fecha);

    document.getElementById("modalTotal").innerText =
        pedido[0].totalPedido;

    document.getElementById("modalPago").innerText =
        pedido[0].metodoPago;

    const historial =
        historialGlobal.find(
            p=>p.pedidoId===id
        );

    document.getElementById("modalEstado").innerText =
        obtenerEstado(historial.estado).texto;

    const tabla =
        document.getElementById("modalItems");

    tabla.innerHTML="";

    pedido.forEach(item=>{

        tabla.innerHTML+=`

            <tr>

                <td>${item.producto}</td>

                <td>${item.cantidad}</td>

                <td>$${(item.subtotal/item.cantidad).toFixed(2)}</td>

                <td>$${item.subtotal}</td>

            </tr>

        `;

    });

    new bootstrap.Modal(
        document.getElementById("modalPedido")
    ).show();

}

/* =========================
   TICKET
========================= */

function descargarTicket(id){

    window.open(`/tickets/${id}`,"_blank");

}

/* =========================
   ANULAR
========================= */

function anularPedido(id){

    if(!confirm("¿Desea anular este pedido?")) return;

    axios.put(API_PEDIDOS+"/"+id+"/anular")

        .then(()=>{

            cargarDetalle();

        })

        .catch(()=>{

            alert("No se pudo anular el pedido.");

        });

}

/* =========================
   RESTAURAR
========================= */

function restaurarPedido(id){

    if(!confirm("¿Desea restaurar este pedido?")) return;

    axios.put(API_PEDIDOS+"/"+id+"/restaurar")

        .then(()=>{

            cargarDetalle();

        })

        .catch(()=>{

            alert("No se pudo restaurar el pedido.");

        });

}

/* =========================
   INICIO
========================= */

document.addEventListener("DOMContentLoaded",()=>{

    cargarDetalle();

});