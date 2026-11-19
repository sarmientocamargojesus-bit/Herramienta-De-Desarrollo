package com.fastfood.fast_food_backend.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fastfood.fast_food_backend.entities.Inventario;
import com.fastfood.fast_food_backend.entities.Pago;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.services.InventarioService;
import com.fastfood.fast_food_backend.services.PagoService;
import com.fastfood.fast_food_backend.services.PedidoService;

import jakarta.persistence.EntityManagerFactory;

/**
 * Los listados del panel deben salir en una sola consulta: con una base remota (Supabase) cada consulta
 * extra cuesta cerca de 180 ms, y una consulta por fila hacía que Inventario tardara casi 4 segundos.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class ConsultasListadoIntegrationTest {

    private static final int FILAS = 5;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManagerFactory emf;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProductoRepository productos;
    @Autowired private InventarioRepository inventarios;
    @Autowired private PedidoRepository pedidos;
    @Autowired private PagoRepository pagos;
    @Autowired private InventarioService inventarioService;
    @Autowired private PedidoService pedidoService;
    @Autowired private PagoService pagoService;

    private Statistics estadisticas;

    @BeforeEach
    void preparar() {
        for (String tabla : List.of("pagos", "carrito_items", "detalles_pedido", "imagenes_producto",
                "inventarios", "pedidos", "productos", "usuarios")) {
            jdbc.update("DELETE FROM " + tabla);
        }
        // Un cliente distinto y un producto distinto por fila: es el caso en que antes había una consulta por fila.
        for (int i = 0; i < FILAS; i++) {
            Usuario cliente = new Usuario();
            cliente.setNombre("Cliente " + i);
            cliente.setCorreo("cliente" + i + "@consultas.test");
            cliente.setContrasena("hash");
            cliente.setTelefono("999111222");
            cliente.setRol("CLIENTE");
            cliente.setActivo(true);
            cliente = usuarios.save(cliente);

            Producto producto = new Producto();
            producto.setNombre("Producto " + i);
            producto.setPrecio(new BigDecimal("10.00"));
            producto.setStock(i);
            producto.setCategoria("HAMBURGUESAS");
            producto = productos.save(producto);

            Inventario inventario = new Inventario();
            inventario.setProducto(producto);
            inventario.setStockMinimo(2);
            inventario.setFechaActualizacion(LocalDateTime.now());
            inventarios.save(inventario);

            Pedido pedido = new Pedido();
            pedido.setUsuario(cliente);
            pedido.setFecha(LocalDateTime.now());
            pedido.setFechaActualizacion(LocalDateTime.now());
            pedido.setEstado("CONFIRMADO");
            pedido.setSubtotal(new BigDecimal("10.00"));
            pedido.setCostoEntrega(BigDecimal.ZERO);
            pedido.setTotal(new BigDecimal("10.00"));
            pedido.setTipoEntrega("RECOJO");
            pedido = pedidos.save(pedido);

            Pago pago = new Pago();
            pago.setPedido(pedido);
            pago.setMetodo("YAPE");
            pago.setEstado("APROBADO");
            pago.setMonto(new BigDecimal("10.00"));
            pago.setFecha(LocalDateTime.now());
            pagos.save(pago);
        }
        estadisticas = emf.unwrap(SessionFactory.class).getStatistics();
        estadisticas.clear();
    }

    @Test
    void elInventarioSeListaEnUnaSolaConsulta() {
        List<Inventario> lista = inventarioService.listar();

        assertEquals(FILAS, lista.size());
        lista.forEach(i -> assertNotNull(i.getProducto().getNombre()));
        assertEquals(1, estadisticas.getPrepareStatementCount());
    }

    @Test
    void elInventarioBajoMinimoSeListaEnUnaSolaConsulta() {
        // Stock 0, 1 y 2 quedan en o bajo el mínimo 2.
        assertEquals(3, inventarioService.listarStockBajo().size());
        assertEquals(1, estadisticas.getPrepareStatementCount());
    }

    @Test
    void losPedidosSeListanConSuClienteEnUnaSolaConsulta() {
        List<Pedido> lista = pedidoService.listar();

        assertEquals(FILAS, lista.size());
        lista.forEach(p -> assertNotNull(p.getUsuario().getNombre()));
        assertEquals(1, estadisticas.getPrepareStatementCount());
    }

    @Test
    void losPagosSeListanConSuPedidoYClienteEnUnaSolaConsulta() {
        List<Pago> lista = pagoService.listar();

        assertEquals(FILAS, lista.size());
        lista.forEach(p -> assertNotNull(p.getPedido().getUsuario().getNombre()));
        assertEquals(1, estadisticas.getPrepareStatementCount());
    }
}
