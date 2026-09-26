package com.fastfood.fast_food_backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.services.ProductoService;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(productoService))
                .build();
    }

    @Test
    void debeResponderOkAlListarProductos() throws Exception {
        when(productoService.listar()).thenReturn(List.of(producto()));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Hamburguesa"));
    }

    @Test
    void debeResponderOkAlBuscarProductoPorId() throws Exception {
        when(productoService.buscarPorId(1L)).thenReturn(producto());

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void debeResponderCreatedAlRegistrarProducto() throws Exception {
        when(productoService.registrar(any(Producto.class))).thenReturn(producto());

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Hamburguesa",
                                  "precio": 12.50,
                                  "stock": 10,
                                  "categoria": "HAMBURGUESAS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Hamburguesa"));
    }

    private Producto producto() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Hamburguesa");
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setStock(10);
        producto.setCategoria("HAMBURGUESAS");
        return producto;
    }
}
