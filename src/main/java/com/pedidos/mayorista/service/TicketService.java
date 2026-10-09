package com.pedidos.mayorista.service;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.pedidos.mayorista.model.DetallePedido;
import com.pedidos.mayorista.model.Pedido;
import com.pedidos.mayorista.repository.ComercioRepository;
import com.pedidos.mayorista.repository.PedidoRepository;
import com.pedidos.mayorista.security.ComercioContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class TicketService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ComercioRepository comercioRepository;

    @Autowired
    private ComercioContext comercio;

    public byte[] generarTicket(Long pedidoId) {

        try {

            // Solo se pueden imprimir tickets de pedidos del propio comercio
            Long comercioId = comercio.id();

            Pedido pedido = pedidoRepository.findByIdAndComercioId(pedidoId, comercioId)
                    .orElseThrow(() ->
                            new RuntimeException("Pedido no encontrado"));

            String nombreComercio = comercioRepository.findById(comercioId)
                    .map(c -> c.getNombre())
                    .orElse("NEXA");

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            // TAMAÑO TICKET
            Document document =
                    new Document(PageSize.A7, 10, 10, 10, 10);

            PdfWriter.getInstance(document, out);

            document.open();

            /* =========================
               FUENTES
            ========================= */

            Font titulo =
                    new Font(Font.HELVETICA, 14, Font.BOLD);

            Font normal =
                    new Font(Font.HELVETICA, 9);

            Font bold =
                    new Font(Font.HELVETICA, 10, Font.BOLD);

            /* =========================
               EMPRESA
            ========================= */

            Paragraph empresa =
                    new Paragraph(nombreComercio.toUpperCase(), titulo);

            empresa.setAlignment(Element.ALIGN_CENTER);

            document.add(empresa);

            Paragraph subtitulo =
                    new Paragraph("Consumidor Final", normal);

            subtitulo.setAlignment(Element.ALIGN_CENTER);

            document.add(subtitulo);

            document.add(new Paragraph(" "));

            /* =========================
               DATOS PEDIDO
            ========================= */

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy HH:mm"
                    );

            document.add(new Paragraph(
                    "Pedido #: " + pedido.getId(),
                    normal
            ));

            document.add(new Paragraph(
                    "Fecha: " +
                            pedido.getFecha().format(formatter),
                    normal
            ));

            document.add(new Paragraph(
                    "Pago: " + pedido.getMetodoPago(),
                    normal
            ));

            /* =========================
               DNI OPCIONAL
            ========================= */

            if (pedido.getDniCliente() != null &&
                    !pedido.getDniCliente().isBlank()) {

                document.add(new Paragraph(
                        "DNI: " + pedido.getDniCliente(),
                        normal
                ));
            }

            document.add(new Paragraph(
                    "--------------------------------"
            ));

            /* =========================
               PRODUCTOS
            ========================= */

            for (DetallePedido d : pedido.getDetalles()) {

                document.add(new Paragraph(
                        d.getProducto().getNombre(),
                        bold
                ));

                document.add(new Paragraph(
                        d.getCantidad()
                                + " x $"
                                + d.getProducto().getPrecioVenta(),
                        normal
                ));

                document.add(new Paragraph(
                        "Subtotal: $" + d.getSubtotal(),
                        normal
                ));

                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph(
                    "--------------------------------"
            ));

            /* =========================
               TOTAL
            ========================= */

            Paragraph total =
                    new Paragraph(
                            "TOTAL: $" + pedido.getTotal(),
                            titulo
                    );

            total.setAlignment(Element.ALIGN_RIGHT);

            document.add(total);

            document.add(new Paragraph(" "));

            /* =========================
               MENSAJE FINAL
            ========================= */

            Paragraph gracias =
                    new Paragraph(
                            "Gracias por su compra",
                            normal
                    );

            gracias.setAlignment(Element.ALIGN_CENTER);

            document.add(gracias);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }
}