// =====================================================
// VOLGA - Productos
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

                tabla.innerHTML += `
                    <tr>

                        <td>${producto.nombre}</td>

                        <td>${producto.costo}</td>

                        <td>${producto.precioVenta}</td>

                        <td>${tipoTexto}</td>

                        <td>

                            <button
                                class="btn btn-sm btn-warning"
                                onclick="cargarEdicion(
                                    ${producto.id},
                                    '${producto.nombre}',
                                    ${producto.costo},
                                    ${producto.precioVenta},
                                    '${producto.tipoVenta}'
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

function guardar() {

    const producto = {

        nombre: document.getElementById("nombre").value,

        costo: document.getElementById("costo").value,

        precioVenta: document.getElementById("precio").value,

        tipoVenta: document.getElementById("tipoVenta").value

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

        axios.put(API + "/" + editandoId, producto)

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

function cargarEdicion(id, nombre, costo, precio, tipoVenta) {

    editandoId = id;

    document.getElementById("nombre").value = nombre;

    document.getElementById("costo").value = costo;

    document.getElementById("precio").value = precio;

    document.getElementById("tipoVenta").value = tipoVenta;

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

        const nombre =
            fila.children[0]
                .innerText
                .toLowerCase();

        fila.style.display =
            nombre.includes(texto)
                ? ""
                : "none";

    });

}

// =====================================================
// LIMPIAR
// =====================================================

function limpiar() {

    document.getElementById("nombre").value = "";

    document.getElementById("costo").value = "";

    document.getElementById("precio").value = "";

    document.getElementById("tipoVenta").value = "UNIDAD";

    cargar();

}



// ===============================
// IMPORTAR EXCEL
// ===============================

async function importarExcel(){



    const archivo =
        document.getElementById("archivoExcel")
        .files[0];



    if(!archivo){

        alert("Seleccione un archivo Excel");

        return;

    }



    const formData =
        new FormData();



    formData.append(
        "archivo",
        archivo
    );





    try{


        const respuesta =
        await axios.post(

            `${API}/importar`,

            formData,

            {
                headers:{
                    "Content-Type":
                    "multipart/form-data"
                }
            }

        );




        mostrarResultadoImportacion(
            respuesta.data
        );



cargar();




    }catch(error){



        console.error(error);


        document.getElementById(
            "resultadoImportacion"
        )
        .innerHTML = `

        <div class="alert alert-danger">

        Error importando archivo:
        ${error.response?.data || error.message}

        </div>

        `;


    }



}








// ===============================
// RESULTADO IMPORTACION
// ===============================

function mostrarResultadoImportacion(data){



    document.getElementById(
        "resultadoImportacion"
    )
    .innerHTML = `


    <div class="alert alert-success">


        Importación finalizada.


        <br>


        Productos importados:
        ${data.importados ?? 0}


        <br>


        Errores:
        ${data.errores ?? 0}


    </div>


    `;


}







// ===============================
// PLANTILLA EXCEL
// ===============================

function descargarPlantilla(){


    const datos = [


        {
            Nombre:"Coca Cola 2.25",
            Costo:1800,
            PrecioVenta:2500,
            TipoVenta:"UNIDAD"
        },


        {
            Nombre:"Carne Picada",
            Costo:8500,
            PrecioVenta:12000,
            TipoVenta:"KILOGRAMO"
        },


        {
            Nombre:"Pan Francés",
            Costo:900,
            PrecioVenta:1400,
            TipoVenta:"UNIDAD"
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