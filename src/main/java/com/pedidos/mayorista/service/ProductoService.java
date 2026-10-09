package com.pedidos.mayorista.service;

import com.pedidos.mayorista.model.Producto;
import com.pedidos.mayorista.repository.ProductoRepository;
import com.pedidos.mayorista.security.ComercioContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Cada comercio tiene su propio catálogo: sus productos, sus precios y su stock.
 */
@Service
public class ProductoService {

    private final ProductoRepository repo;

    private final ComercioContext comercio;

    public ProductoService(ProductoRepository repo,
                           ComercioContext comercio) {
        this.repo = repo;
        this.comercio = comercio;
    }

    // ==========================================
    // LISTAR
    // ==========================================

    public List<Producto> listar() {
        return repo.findAllByComercioId(comercio.id());
    }

    // ==========================================
    // GUARDAR
    // ==========================================

    public Producto guardar(Producto producto) {

        // Siempre se crea nuevo y siempre en el comercio de quien lo crea
        producto.setId(null);

        producto.setComercioId(comercio.id());

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
        return repo.findByIdAndComercioId(id, comercio.id());
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    public Producto actualizar(Long id, Producto nuevo) {

        return repo.findByIdAndComercioId(id, comercio.id())
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

        // Si el producto no es de este comercio, simplemente no se encuentra
        repo.findByIdAndComercioId(id, comercio.id())
                .ifPresent(repo::delete);
    }

}
