// =====================================================
// NEXA - Productos
// =====================================================

const API = "/api/productos";

let editandoId = null;

// =====================================================
// CARGAR PRODUCTOS
// =====================================================

function cargar() {

    axios.get(API)
        .then(response => {

            const tabla = document.getElementById("tabla");

            tabla.innerHTML = "";

            response.data.forEach(producto => {

                const tipoTexto =
                    producto.tipoVenta === "KILOGRAMO"
                        ? "Por kilogramo"
                        : "Por unidad";

                const estadoBadge =
                    producto.activo
                        ? `<span class="badge bg-success">Activo</span>`
                        : `<span class="badge bg-danger">Inactivo</span>`;

                tabla.innerHTML += `

                    <tr>

                        <td>${producto.codigo ?? ""}</td>

                        <td>${producto.nombre}</td>

                        <td>$ ${producto.costo}</td>

                        <td>$ ${producto.precioVenta}</td>

                        <td>${tipoTexto}</td>

                        <td>${producto.stock}</td>

                        <td>${producto.stockMinimo}</td>

                        <td>${estadoBadge}</td>

                        <td>

                            <button
                                class="btn btn-sm btn-warning"
                                onclick="cargarEdicion(

                                    ${producto.id},

                                    '${(producto.codigo ?? "").replace(/'/g,"\\'")}',

                                    '${producto.nombre.replace(/'/g,"\\'")}',

                                    ${producto.costo},

                                    ${producto.precioVenta},

                                    '${producto.tipoVenta}',

                                    ${producto.stock},

                                    ${producto.stockMinimo},

                                    ${producto.activo}

                                )">

                                Editar

                            </button>

                            <button
                                class="btn btn-sm btn-danger ms-1"
                                onclick="eliminar(${producto.id})">

                                Borrar

                            </button>

                        </td>

                    </tr>

                `;

            });

        })
        .catch(error => {

            console.error("Error cargando productos", error);

        });

}

// =====================================================
// GUARDAR
// =====================================================

// =====================================================
// GUARDAR
// =====================================================

function guardar() {

    const producto = {

        codigo:
            document.getElementById("codigo").value,

        nombre:
            document.getElementById("nombre").value,

        costo:
            Number(
                document.getElementById("costo").value
            ),

        precioVenta:
            Number(
                document.getElementById("precio").value
            ),

        tipoVenta:
            document.getElementById("tipoVenta").value,

        stock:
            Number(
                document.getElementById("stock").value
            ),

        stockMinimo:
            Number(
                document.getElementById("stockMinimo").value
            ),

        activo:
            document.getElementById("activo").checked

    };

    if (editandoId == null) {

        axios.post(API, producto)

            .then(() => {

                limpiar();

            })

            .catch(error => {

                console.error(error);

            });

    } else {

        axios.put(

            API + "/" + editandoId,

            producto

        )

        .then(() => {

            editandoId = null;

            limpiar();

            document.getElementById("tituloForm").innerText =
                "Nuevo Producto";

            document
                .getElementById("cancelarBtn")
                .classList.add("d-none");

        })

        .catch(error => {

            console.error(error);

        });

    }

}
// =====================================================
// EDITAR
// =====================================================

function cargarEdicion(

    id,
    codigo,
    nombre,
    costo,
    precio,
    tipoVenta,
    stock,
    stockMinimo,
    activo

) {

    editandoId = id;

    document.getElementById("codigo").value = codigo;

    document.getElementById("nombre").value = nombre;

    document.getElementById("costo").value = costo;

    document.getElementById("precio").value = precio;

    document.getElementById("tipoVenta").value = tipoVenta;

    document.getElementById("stock").value = stock;

    document.getElementById("stockMinimo").value = stockMinimo;

    document.getElementById("activo").checked = activo;

    document.getElementById("tituloForm").innerText =
        "Editar Producto";

    document
        .getElementById("cancelarBtn")
        .classList.remove("d-none");

}


// =====================================================
// CANCELAR
// =====================================================

function cancelar() {

    editandoId = null;

    limpiar();

    document.getElementById("tituloForm").innerText =
        "Nuevo Producto";

    document
        .getElementById("cancelarBtn")
        .classList.add("d-none");

}

// =====================================================
// ELIMINAR
// =====================================================

function eliminar(id) {

    if (!confirm("¿Seguro que desea eliminar este producto?")) {

        return;

    }

    axios.delete(API + "/" + id)

        .then(() => {

            cargar();

        })

        .catch(error => {

            console.error(error);

            alert("No se pudo eliminar el producto.");

        });

}

// =====================================================
// FILTRAR
// =====================================================

function filtrarProductos() {

    const texto =
        document
            .getElementById("filtro")
            .value
            .toLowerCase();

    const filas =
        document.querySelectorAll("#tabla tr");

    filas.forEach(fila => {

        const codigo =
            fila.children[0]
                .innerText
                .toLowerCase();

        const nombre =
            fila.children[1]
                .innerText
                .toLowerCase();

        fila.style.display =

            codigo.includes(texto) ||

            nombre.includes(texto)

                ? ""

                : "none";

    });

}


// =====================================================
// LIMPIAR
// =====================================================

function limpiar() {

    document.getElementById("codigo").value = "";

    document.getElementById("nombre").value = "";

    document.getElementById("costo").value = "";

    document.getElementById("precio").value = "";

    document.getElementById("tipoVenta").value = "UNIDAD";

    document.getElementById("stock").value = "";

    document.getElementById("stockMinimo").value = "";

    document.getElementById("activo").checked = true;

    cargar();

}

// ===============================
// IMPORTAR EXCEL
// ===============================

async function importarExcel() {

    const archivo =
        document.getElementById("archivoExcel").files[0];

    if (!archivo) {

        alert("Seleccione un archivo Excel");

        return;

    }

    const formData = new FormData();

    formData.append("archivo", archivo);

    try {

        const respuesta = await axios.post(

            `${API}/importar`,

            formData,

            {

                headers: {

                    "Content-Type":
                        "multipart/form-data"

                }

            }

        );

        mostrarResultadoImportacion(respuesta.data);

        cargar();

    } catch (error) {

        console.error(error);

        document.getElementById(
            "resultadoImportacion"
        ).innerHTML = `

            <div class="alert alert-danger">

                Error importando archivo.

                <br>

                ${error.response?.data || error.message}

            </div>

        `;

    }

}


// ===============================
// RESULTADO IMPORTACION
// ===============================

function mostrarResultadoImportacion(data) {

    document.getElementById(
        "resultadoImportacion"
    ).innerHTML = `

        <div class="alert alert-success">

            <strong>Importación finalizada</strong>

            <hr>

            Productos importados:
            <b>${data.importados ?? 0}</b>

            <br>

            Errores:
            <b>${data.errores ?? 0}</b>

        </div>

    `;

}


// ===============================
// PLANTILLA EXCEL
// ===============================

function descargarPlantilla() {

    const datos = [

        {

            Codigo: "1001",

            Nombre: "Coca Cola 2.25",

            Costo: 1800,

            PrecioVenta: 2500,

            TipoVenta: "UNIDAD",

            Stock: 25,

            StockMinimo: 5,

            Activo: true

        },

        {

            Codigo: "2001",

            Nombre: "Carne Picada",

            Costo: 8500,

            PrecioVenta: 12000,

            TipoVenta: "KILOGRAMO",

            Stock: 40,

            StockMinimo: 8,

            Activo: true

        },

        {

            Codigo: "3001",

            Nombre: "Pan Francés",

            Costo: 900,

            PrecioVenta: 1400,

            TipoVenta: "UNIDAD",

            Stock: 70,

            StockMinimo: 15,

            Activo: true

        }

    ];

    const hoja =
        XLSX.utils.json_to_sheet(datos);

    const libro =
        XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(

        libro,

        hoja,

        "Productos"

    );

    XLSX.writeFile(

        libro,

        "Plantilla_Productos_NEXA.xlsx"

    );

}


// =====================================================
// INICIO
// =====================================================

document.addEventListener("DOMContentLoaded", () => {

    cargar();

});