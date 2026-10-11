package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.dto.CarritoItemRequest;
import com.fastfood.fast_food_backend.dto.CheckoutRequest;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.CarritoItemRepository;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.PagoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;

@SpringBootTest
@Transactional
class CheckoutCarritoIntegrationTest {

    @Autowired private CarritoService carrito;
    @Autowired private CheckoutService checkout;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProductoRepository productos;
    @Autowired private CarritoItemRepository items;
    @Autowired private PedidoRepository pedidos;
    @Autowired private DetallePedidoRepository detalles;
    @Autowired private PagoRepository pagos;

    @Test
    void seleccionInvitadaSeAsociaYLaCompraSeConfirmaEnUnaOperacion() {
        Usuario cliente = usuarios.save(cliente("compra@ejemplo.com"));
        Producto producto = productos.save(producto(5));
        LocalDateTime agregado = LocalDateTime.now().minusHours(2).withNano(0);
        long pedidosAntes = pedidos.count();

        var seleccion = carrito.importar(cliente.getCorreo(),
                List.of(new CarritoItemRequest(producto.getId(), 2, agregado)));
        assertEquals(agregado, seleccion.getFirst().fechaAgregado());
        assertEquals(2, carrito.listar(cliente.getCorreo()).getFirst().cantidad());
        carrito.importar(cliente.getCorreo(),
                List.of(new CarritoItemRequest(producto.getId(), 2, agregado)));
        assertEquals(2, carrito.listar(cliente.getCorreo()).getFirst().cantidad());
        assertEquals(pedidosAntes, pedidos.count());
        assertEquals(5, productos.findById(producto.getId()).orElseThrow().getStock());

        var resultado = checkout.confirmar(cliente.getCorreo(), solicitud("TARJETA"));

        assertEquals("CONFIRMADO", resultado.estadoPedido());
        assertEquals("APROBADO", resultado.estadoPago());
        assertEquals(new BigDecimal("25.00"), resultado.total());
        assertEquals(pedidosAntes + 1, pedidos.count());
        assertEquals(1, detalles.buscarPorPedidoId(resultado.pedidoId()).size());
        assertEquals("TARJETA", pagos.findByPedidoId(resultado.pedidoId()).orElseThrow().getMetodo());
        assertEquals(3, productos.findById(producto.getId()).orElseThrow().getStock());
        assertTrue(items.findByUsuarioIdOrderByFechaAgregadoAsc(cliente.getId()).isEmpty());
    }

    @Test
    void stockInsuficienteConservaCarritoYNoCreaPedidoNiPago() {
        Usuario cliente = usuarios.save(cliente("agotado@ejemplo.com"));
        Producto producto = productos.save(producto(2));
        carrito.establecerCantidad(cliente.getCorreo(), producto.getId(), 2);
        producto.setStock(1); // Otro pedido consumió una unidad después de llenar el carrito.
        productos.save(producto);
        long pedidosAntes = pedidos.count();
        long pagosAntes = pagos.count();

        assertThrows(ReglaNegocioException.class,
                () -> checkout.confirmar(cliente.getCorreo(), solicitud("YAPE")));

        assertEquals(pedidosAntes, pedidos.count());
        assertEquals(pagosAntes, pagos.count());
        assertEquals(1, productos.findById(producto.getId()).orElseThrow().getStock());
        assertEquals(2, carrito.listar(cliente.getCorreo()).getFirst().cantidad());
    }

    @Test
    void noAceptaDatosRealesComoReferenciaDePago() {
        Usuario cliente = usuarios.save(cliente("referencia@ejemplo.com"));
        Producto producto = productos.save(producto(3));
        carrito.establecerCantidad(cliente.getCorreo(), producto.getId(), 1);
        assertThrows(IllegalArgumentException.class,
                () -> checkout.confirmar(cliente.getCorreo(),
                        new CheckoutRequest("RECOJO", null, null, "TARJETA", "4111111111111111")));
        assertEquals(3, productos.findById(producto.getId()).orElseThrow().getStock());
    }

    private CheckoutRequest solicitud(String metodo) {
        return new CheckoutRequest("RECOJO", null, null, metodo, "DEMO-APROBADO");
    }

    private Usuario cliente(String correo) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Cliente de prueba");
        usuario.setCorreo(correo);
        usuario.setContrasena("hash-prueba");
        usuario.setTelefono("999111222");
        usuario.setRol("CLIENTE");
        usuario.setActivo(true);
        return usuario;
    }

    private Producto producto(int stock) {
        Producto producto = new Producto();
        producto.setNombre("Burger de prueba");
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setStock(stock);
        producto.setCategoria("HAMBURGUESAS");
        return producto;
    }
}
