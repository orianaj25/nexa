package com.pedidos.mayorista.service;

import com.pedidos.mayorista.dto.PedidoDetalleDTO;
import com.pedidos.mayorista.dto.PedidoHistorialDTO;
import com.pedidos.mayorista.model.enums.EstadoPedido;
import com.pedidos.mayorista.repository.DetallePedidoRepository;
import com.pedidos.mayorista.security.ComercioContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoDetalleService {

    @Autowired
    private DetallePedidoRepository detalleRepo;

    @Autowired
    private ComercioContext comercio;

    public List<PedidoDetalleDTO> listarDetalle() {
        return detalleRepo.listarDetalle(comercio.id());
    }

    public List<PedidoHistorialDTO> listarHistorial() {
        return detalleRepo.listarHistorial(comercio.id(), EstadoPedido.ANULADO);
    }

    public List<PedidoHistorialDTO> listarAnulados() {
        return detalleRepo.listarAnulados(comercio.id(), EstadoPedido.ANULADO);
    }
    public List<PedidoHistorialDTO> listarTodos() {
        return detalleRepo.listarTodos(comercio.id());
    }
}
