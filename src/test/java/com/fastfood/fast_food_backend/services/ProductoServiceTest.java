package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void debeRegistrarUnProductoValido() {
        Producto producto = producto("Hamburguesa", "12.50", 10);
        when(categoriaRepository.findByNombreIgnoreCase("HAMBURGUESAS"))
                .thenReturn(Optional.of(categoria()));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto resultado = productoService.registrar(producto);

        assertSame(producto, resultado);
        verify(productoRepository).save(producto);
    }

    @Test
    void debeRechazarUnProductoSinNombre() {
        Producto producto = producto(" ", "12.50", 10);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> productoService.registrar(producto));

        assertEquals("El nombre del producto es obligatorio", error.getMessage());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void debeRechazarUnProductoConPrecioCeroONegativo() {
        Producto producto = producto("Hamburguesa", "0.00", 10);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> productoService.registrar(producto));

        assertEquals("El precio debe ser mayor que cero", error.getMessage());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void debeBuscarUnProductoPorId() {
        Producto producto = producto("Hamburguesa", "12.50", 10);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.buscarPorId(1L);

        assertSame(producto, resultado);
    }

    @Test
    void debeInformarCuandoElProductoNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.buscarPorId(99L));
    }

    @Test
    void debeBuscarProductosPorRangoDePrecios() {
        Producto producto = producto("Hamburguesa", "12.50", 10);
        BigDecimal minimo = new BigDecimal("10.00");
        BigDecimal maximo = new BigDecimal("20.00");
        when(productoRepository.buscarPorRangoDePrecios(minimo, maximo)).thenReturn(List.of(producto));

        List<Producto> resultado = productoService.buscarPorRangoDePrecios(minimo, maximo);

        assertEquals(1, resultado.size());
        assertSame(producto, resultado.get(0));
    }

    @Test
    void debeDescontarStockDisponible() {
        Producto producto = producto("Hamburguesa", "12.50", 10);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.descontarStock(1L, 3);

        assertEquals(7, resultado.getStock());
        verify(productoRepository).save(producto);
    }

    @Test
    void debeRechazarDescuentoConStockInsuficiente() {
        Producto producto = producto("Hamburguesa", "12.50", 2);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(ReglaNegocioException.class, () -> productoService.descontarStock(1L, 3));

        verify(productoRepository, never()).save(any());
    }

    private Producto producto(String nombre, String precio, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        producto.setCategoria("HAMBURGUESAS");
        return producto;
    }

    private Categoria categoria() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("HAMBURGUESAS");
        categoria.setActivo(true);
        return categoria;
    }
}
