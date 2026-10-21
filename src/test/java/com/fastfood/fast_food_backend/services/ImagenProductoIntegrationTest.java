package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;
import com.fastfood.fast_food_backend.repositories.ImagenProductoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;

@SpringBootTest
@Transactional
class ImagenProductoIntegrationTest {

    private static final byte[] PNG = new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 1};

    @Autowired private ImagenProductoService imagenes;
    @Autowired private ImagenProductoRepository repositorioImagenes;
    @Autowired private CategoriaRepository categorias;
    @Autowired private ProductoRepository productos;
    @Autowired private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void preparar() {
        Categoria categoria = new Categoria();
        categoria.setNombre("FOTOS_PRUEBA");
        categoria.setDescripcion("Pruebas de fotos");
        categoria.setActivo(true);
        categoria = categorias.save(categoria);

        producto = new Producto();
        producto.setNombre("Hamburguesa de prueba");
        producto.setDescripcion("Sin cambiar stock");
        producto.setPrecio(BigDecimal.TEN);
        producto.setStock(5);
        producto.setCategoria(categoria.getNombre());
        producto.setCategoriaReferencia(categoria);
        producto = productos.save(producto);
    }

    @Test
    void guardaReemplazaYQuitaImagenSinModificarProducto() throws Exception {
        imagenes.guardar(producto.getId(), archivo(PNG, "image/png"));
        assertArrayEquals(PNG, imagenes.buscar(producto.getId()).getDatos());
        assertEquals(5, productos.findById(producto.getId()).orElseThrow().getStock());

        byte[] segunda = new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 2};
        imagenes.guardar(producto.getId(), archivo(segunda, "image/png"));
        assertArrayEquals(segunda, imagenes.buscar(producto.getId()).getDatos());
        assertEquals(1, repositorioImagenes.count());

        imagenes.quitar(producto.getId());
        assertThrows(RecursoNoEncontradoException.class, () -> imagenes.buscar(producto.getId()));
    }

    @Test
    void rechazaTipoFalsoYArchivoVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> imagenes.guardar(producto.getId(), archivo(PNG, "image/jpeg")));
        assertThrows(IllegalArgumentException.class,
                () -> imagenes.guardar(producto.getId(), archivo(new byte[0], "image/png")));
        assertTrue(repositorioImagenes.findById(producto.getId()).isEmpty());
    }

    @Test
    void eliminarProductoEliminaSuImagen() throws Exception {
        imagenes.guardar(producto.getId(), archivo(PNG, "image/png"));
        productoService.eliminar(producto.getId());
        assertTrue(repositorioImagenes.findById(producto.getId()).isEmpty());
    }

    private MockMultipartFile archivo(byte[] datos, String tipo) {
        return new MockMultipartFile("archivo", "foto.png", tipo, datos);
    }
}
