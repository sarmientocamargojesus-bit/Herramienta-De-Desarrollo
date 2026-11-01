package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fastfood.fast_food_backend.entities.Inventario;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.repositories.InventarioRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;

@SpringBootTest
class InventarioFaltanteIntegrationTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ProductoRepository productos;
    @Autowired private InventarioRepository inventarios;
    @Autowired private InventarioService inventarioService;

    @BeforeEach
    void limpiar() {
        for (String tabla : List.of("pagos", "carrito_items", "detalles_pedido", "imagenes_producto",
                "inventarios", "pedidos", "productos")) {
            jdbc.update("DELETE FROM " + tabla);
        }
    }

    @Test
    void completaSoloLosProductosSinInventarioConElMinimoIndicado() {
        Producto conStock = productos.save(producto("Hamburguesa clásica", 20));
        Producto stockBajo = productos.save(producto("Papas medianas", 3));
        Producto agotado = productos.save(producto("Inca Kola", 0));
        Producto configurado = productos.save(producto("Combo familiar", 1));
        LocalDateTime fechaOriginal = LocalDateTime.of(2026, 10, 1, 12, 0);
        Inventario existente = new Inventario();
        existente.setProducto(configurado);
        existente.setStockMinimo(2);
        existente.setFechaActualizacion(fechaOriginal);
        existente.setFechaStockBajo(fechaOriginal);
        inventarios.save(existente);

        int creados = inventarioService.completarFaltantes(5);

        assertEquals(3, creados);
        assertEquals(4, inventarios.count());

        Inventario normal = inventarios.findByProductoId(conStock.getId()).orElseThrow();
        assertEquals(5, normal.getStockMinimo());
        assertNull(normal.getFechaStockBajo());
        assertNull(normal.getFechaAgotado());

        Inventario bajo = inventarios.findByProductoId(stockBajo.getId()).orElseThrow();
        assertEquals(5, bajo.getStockMinimo());
        assertNotNull(bajo.getFechaStockBajo());
        assertNull(bajo.getFechaAgotado());

        Inventario sinStock = inventarios.findByProductoId(agotado.getId()).orElseThrow();
        assertNotNull(sinStock.getFechaStockBajo());
        assertNotNull(sinStock.getFechaAgotado());

        Inventario conservado = inventarios.findByProductoId(configurado.getId()).orElseThrow();
        assertEquals(2, conservado.getStockMinimo());
        assertEquals(fechaOriginal, conservado.getFechaActualizacion());
        assertEquals(fechaOriginal, conservado.getFechaStockBajo());

        assertEquals(20, productos.findById(conStock.getId()).orElseThrow().getStock());
        assertEquals(0, productos.findById(agotado.getId()).orElseThrow().getStock());
    }

    @Test
    void ejecutarloOtraVezNoDuplicaInventarios() {
        productos.save(producto("Hamburguesa clásica", 20));
        productos.save(producto("Inca Kola", 0));

        assertEquals(2, inventarioService.completarFaltantes(5));
        assertEquals(0, inventarioService.completarFaltantes(5));
        assertEquals(2, inventarios.count());
    }

    private Producto producto(String nombre, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal("10.00"));
        producto.setStock(stock);
        producto.setCategoria("HAMBURGUESAS");
        return producto;
    }
}
