/* ==========================================
   Muestra el nombre del comercio del usuario logueado
   en todos los elementos con clase "js-comercio-nombre"
========================================== */

(async function () {

    try {

        const response = await fetch("/api/usuarios/me");

        if (!response.ok) {
            return;
        }

        const usuario = await response.json();

        if (!usuario.comercioNombre) {
            return;
        }

        document.querySelectorAll(".js-comercio-nombre").forEach(el => {
            el.textContent = usuario.comercioNombre;
        });

    } catch (error) {

        console.error(error);

    }

})();
