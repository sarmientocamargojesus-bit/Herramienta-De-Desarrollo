package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fastfood.fast_food_backend.dto.DetallePedidoRequest;
import com.fastfood.fast_food_backend.dto.ProcesarPedidoRequest;
import com.fastfood.fast_food_backend.entities.Cliente;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.ClienteRepository;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;

@SpringBootTest
class PedidoTransaccionIntegrationTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        detallePedidoRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void debeConfirmarPedidoYDescontarStock() {
        Cliente cliente = clienteRepository.save(cliente());
        Producto producto = productoRepository.save(producto("Hamburguesa", "12.50", 10));

        Pedido pedido = pedidoService.procesarPedido(solicitud(cliente.getId(), detalle(producto.getId(), 2)));

        assertEquals(new BigDecimal("25.00"), pedido.getTotal());
        assertEquals(1, pedidoRepository.count());
        assertEquals(1, detallePedidoRepository.count());
        assertEquals(1, detallePedidoRepository.buscarPorPedidoId(pedido.getId()).size());
        assertEquals(new BigDecimal("25.00"), pedidoRepository.sumarTotalPorEstado("PENDIENTE"));
        assertEquals(8, productoRepository.findById(producto.getId()).orElseThrow().getStock());
    }

    @Test
    void debeRevertirTodoElPedidoCuandoUnProductoNoTieneStock() {
        Cliente cliente = clienteRepository.save(cliente());
        Producto disponible = productoRepository.save(producto("Hamburguesa", "12.50", 10));
        Producto sinStock = productoRepository.save(producto("Gaseosa", "5.00", 1));

        ProcesarPedidoRequest solicitud = solicitud(
                cliente.getId(),
                detalle(disponible.getId(), 2),
                detalle(sinStock.getId(), 3));

        assertThrows(ReglaNegocioException.class, () -> pedidoService.procesarPedido(solicitud));

        assertEquals(0, pedidoRepository.count());
        assertEquals(0, detallePedidoRepository.count());
        assertEquals(10, productoRepository.findById(disponible.getId()).orElseThrow().getStock());
        assertEquals(1, productoRepository.findById(sinStock.getId()).orElseThrow().getStock());
    }

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setNombres("Ana");
        cliente.setApellidos("Pérez");
        cliente.setTelefono("999111222");
        cliente.setCorreo("ana.transaccion@correo.com");
        cliente.setDireccion("Av. Principal 123");
        cliente.setActivo(true);
        return cliente;
    }

    private Producto producto(String nombre, String precio, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        producto.setCategoria("COMIDA");
        return producto;
    }

    private DetallePedidoRequest detalle(Long productoId, int cantidad) {
        DetallePedidoRequest detalle = new DetallePedidoRequest();
        detalle.setProductoId(productoId);
        detalle.setCantidad(cantidad);
        return detalle;
    }

    private ProcesarPedidoRequest solicitud(Long clienteId, DetallePedidoRequest... detalles) {
        ProcesarPedidoRequest solicitud = new ProcesarPedidoRequest();
        solicitud.setClienteId(clienteId);
        solicitud.setTipoEntrega("RECOJO");
        solicitud.setObservaciones("Prueba transaccional");
        solicitud.setDetalles(List.of(detalles));
        return solicitud;
    }
}
