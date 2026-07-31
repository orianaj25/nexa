package com.pedidos.mayorista.service;

import com.pedidos.mayorista.dto.PedidoRequest;
import com.pedidos.mayorista.model.DetallePedido;
import com.pedidos.mayorista.model.Pedido;
import com.pedidos.mayorista.model.Producto;
import com.pedidos.mayorista.model.enums.EstadoPedido;
import com.pedidos.mayorista.repository.PedidoRepository;
import com.pedidos.mayorista.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private PedidoRepository pedidoRepo;

    @Autowired
    private CajaService cajaService;

    public Pedido crearPedido(PedidoRequest request) {

        Pedido pedido = new Pedido();

        pedido.setFecha(
                LocalDateTime.now(
                        ZoneId.of("America/Argentina/Buenos_Aires"))
        );

        pedido.setMetodoPago(request.metodoPago);
        pedido.setDniCliente(request.dniCliente);

        // Estado inicial del pedido
        pedido.setEstado(EstadoPedido.ENVIADO_A_FACTURACION);

        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PedidoRequest.ItemPedido item : request.items) {

            Producto p = productoRepo.findById(item.productoId)
                    .orElseThrow(() ->
                            new RuntimeException("Producto no existe"));

            BigDecimal subtotal = p.getPrecioVenta()
                    .multiply(item.getCantidad());

            DetallePedido d = new DetallePedido();

            d.setPedido(pedido);
            d.setProducto(p);
            d.setCantidad(item.cantidad);
            d.setSubtotal(subtotal);

            detalles.add(d);

            total = total.add(subtotal);
        }

        pedido.setTotal(total);
        pedido.setDetalles(detalles);

        // Guarda el pedido para obtener el ID
        Pedido pedidoGuardado = pedidoRepo.save(pedido);

        // Genera el número de pedido
        pedidoGuardado.setNumeroPedido(
                String.format("PED-%06d", pedidoGuardado.getId())
        );

        // Guarda nuevamente con el número generado
        pedidoGuardado = pedidoRepo.save(pedidoGuardado);

        // Registra la venta en caja (si existe una caja abierta)
        cajaService.registrarVenta(pedidoGuardado);

        return pedidoGuardado;
    }

    public List<Pedido> listar() {
        return pedidoRepo.findAll();
    }

    @Transactional
    public void anularPedido(Long id) {

        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(EstadoPedido.ANULADO);

        pedidoRepo.save(pedido);
    }

    @Transactional
    public void restaurarPedido(Long id) {

        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(EstadoPedido.ENVIADO_A_FACTURACION);

        pedidoRepo.save(pedido);
    }

}