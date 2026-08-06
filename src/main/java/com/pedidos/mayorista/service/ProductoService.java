package com.pedidos.mayorista.service;

import com.pedidos.mayorista.model.Producto;
import com.pedidos.mayorista.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository repo;

    public ProductoService(ProductoRepository repo) {
        this.repo = repo;
    }

    // ==========================================
    // LISTAR
    // ==========================================

    public List<Producto> listar() {
        return repo.findAll();
    }

    // ==========================================
    // GUARDAR
    // ==========================================

    public Producto guardar(Producto producto) {

        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }

        if (producto.getStock() == null) {
            producto.setStock(0);
        }

        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(0);
        }

        return repo.save(producto);
    }

    // ==========================================
    // BUSCAR
    // ==========================================

    public Optional<Producto> buscarPorId(Long id) {
        return repo.findById(id);
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    public Producto actualizar(Long id, Producto nuevo) {

        return repo.findById(id)
                .map(producto -> {

                    producto.setCodigo(nuevo.getCodigo());
                    producto.setNombre(nuevo.getNombre());
                    producto.setCosto(nuevo.getCosto());
                    producto.setPrecioVenta(nuevo.getPrecioVenta());
                    producto.setTipoVenta(nuevo.getTipoVenta());
                    producto.setStock(nuevo.getStock());
                    producto.setStockMinimo(nuevo.getStockMinimo());
                    producto.setActivo(nuevo.getActivo());

                    return repo.save(producto);

                })
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado"));

    }

    // ==========================================
    // ELIMINAR
    // ==========================================

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}