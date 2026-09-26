package com.fastfood.fast_food_backend.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fastfood.fast_food_backend.entities.Producto;

@SpringBootTest
class ProductoJpqlIntegrationTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void prepararProductos() {
        detallePedidoRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();

        productoRepository.save(producto("Hamburguesa clásica", "12.50", 10));
        productoRepository.save(producto("Hamburguesa doble", "20.00", 0));
        productoRepository.save(producto("Gaseosa", "5.00", 20));
    }

    @Test
    void debeEjecutarConsultaJpqlPorNombre() {
        List<Producto> resultado = productoRepository.buscarPorNombre("hamburguesa");

        assertEquals(2, resultado.size());
    }

    @Test
    void debeEjecutarConsultaJpqlDeStockDisponible() {
        List<Producto> resultado = productoRepository.buscarConStockDisponible();

        assertEquals(2, resultado.size());
    }

    @Test
    void debeEjecutarConsultaJpqlConRangoYMultiplesCondiciones() {
        List<Producto> resultado = productoRepository.buscarPorRangoDePrecios(
                new BigDecimal("10.00"), new BigDecimal("25.00"));

        assertEquals(1, resultado.size());
        assertEquals("Hamburguesa clásica", resultado.get(0).getNombre());
    }

    @Test
    void debeOrdenarProductosPorPrecioDescendente() {
        List<Producto> resultado = productoRepository.ordenarPorPrecioDescendente();

        assertEquals("20.00", resultado.get(0).getPrecio().toPlainString());
        assertEquals("5.00", resultado.get(2).getPrecio().toPlainString());
    }

    private Producto producto(String nombre, String precio, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        producto.setCategoria("COMIDA");
        return producto;
    }
}
