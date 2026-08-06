package com.pedidos.mayorista.service;

import com.pedidos.mayorista.dto.ImportacionProductosDTO;
import com.pedidos.mayorista.model.Producto;
import com.pedidos.mayorista.repository.ProductoRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ExcelProductoService {

    private final ProductoRepository productoRepository;

    public ExcelProductoService(ProductoRepository productoRepository) {

        this.productoRepository = productoRepository;

    }

    public ImportacionProductosDTO importar(MultipartFile archivo) throws IOException {

        ImportacionProductosDTO resultado = new ImportacionProductosDTO();

        Workbook workbook =
                WorkbookFactory.create(archivo.getInputStream());

        Sheet hoja = workbook.getSheetAt(0);

        for (int i = 1; i <= hoja.getLastRowNum(); i++) {

            Row fila = hoja.getRow(i);

            if (fila == null) {
                continue;
            }

            try {

                String codigo =
                        obtenerTexto(fila.getCell(0));

                String nombre =
                        obtenerTexto(fila.getCell(1));

                Double costo =
                        obtenerDouble(fila.getCell(2));

                BigDecimal precioVenta =
                        obtenerBigDecimal(fila.getCell(3));

                String tipoVenta =
                        obtenerTexto(fila.getCell(4)).toUpperCase();

                Integer stock =
                        obtenerInteger(fila.getCell(5));

                Integer stockMinimo =
                        obtenerInteger(fila.getCell(6));

                Boolean activo =
                        obtenerBoolean(fila.getCell(7));

                if (codigo.isBlank()) {

                    throw new RuntimeException("Código vacío");

                }

                if (nombre.isBlank()) {

                    throw new RuntimeException("Nombre vacío");

                }

                if (!tipoVenta.equals("UNIDAD")
                        && !tipoVenta.equals("KILOGRAMO")) {

                    throw new RuntimeException(
                            "Tipo de venta inválido"
                    );

                }
                Producto producto;

                Optional<Producto> existente =
                        productoRepository.findByCodigo(codigo);

                if (existente.isPresent()) {

                    producto = existente.get();

                } else {

                    producto = new Producto();

                    producto.setCodigo(codigo);

                }

                producto.setNombre(nombre);

                producto.setCosto(costo);

                producto.setPrecioVenta(precioVenta);

                producto.setTipoVenta(tipoVenta);

                producto.setStock(stock);

                producto.setStockMinimo(stockMinimo);

                producto.setActivo(activo);

                productoRepository.save(producto);

                resultado.setImportados(
                        resultado.getImportados() + 1
                );

            } catch (Exception e) {

                resultado.setErrores(
                        resultado.getErrores() + 1
                );

                resultado.getDetalleErrores().add(
                        "Fila " + (i + 1) + ": " + e.getMessage()
                );

            }

        }

        workbook.close();

        return resultado;

    }
    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    private String obtenerTexto(Cell cell) {

        if (cell == null) {
            return "";
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            double valor = cell.getNumericCellValue();

            if (valor == (long) valor) {

                return String.valueOf((long) valor);

            }

            return String.valueOf(valor);

        }

        if (cell.getCellType() == CellType.BOOLEAN) {

            return String.valueOf(cell.getBooleanCellValue());

        }

        return cell.getStringCellValue().trim();

    }

    private Double obtenerDouble(Cell cell) {

        if (cell == null) {
            return 0.0;
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            return cell.getNumericCellValue();

        }

        String valor = cell.getStringCellValue()
                .replace(",", ".")
                .trim();

        if (valor.isBlank()) {

            return 0.0;

        }

        return Double.parseDouble(valor);

    }

    private BigDecimal obtenerBigDecimal(Cell cell) {

        if (cell == null) {

            return BigDecimal.ZERO;

        }

        if (cell.getCellType() == CellType.NUMERIC) {

            return BigDecimal.valueOf(
                    cell.getNumericCellValue()
            );

        }

        String valor = cell.getStringCellValue()
                .replace(",", ".")
                .trim();

        if (valor.isBlank()) {

            return BigDecimal.ZERO;

        }

        return new BigDecimal(valor);

    }

    private Boolean obtenerBoolean(Cell cell) {

        if (cell == null) {

            return true;

        }

        if (cell.getCellType() == CellType.BOOLEAN) {

            return cell.getBooleanCellValue();

        }

        String valor =
                obtenerTexto(cell).trim().toUpperCase();

        return valor.equals("TRUE")
                || valor.equals("VERDADERO")
                || valor.equals("SI")
                || valor.equals("SÍ")
                || valor.equals("1");

    }

    private Integer obtenerInteger(Cell cell) {

        if (cell == null) {
            return 0;
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            return (int) cell.getNumericCellValue();

        }

        String valor = cell.getStringCellValue()
                .trim();

        if (valor.isBlank()) {

            return 0;

        }

        return Integer.parseInt(valor);

    }
}