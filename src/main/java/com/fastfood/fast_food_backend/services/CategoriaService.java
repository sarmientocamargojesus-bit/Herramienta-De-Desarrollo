package com.fastfood.fast_food_backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivoTrueOrderByNombreAsc();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada: " + id));
    }

    public Categoria registrar(Categoria categoria) {
        validar(categoria);
        // Registrar siempre crea una categoría nueva: un id enviado en el JSON no puede reemplazar a otra.
        categoria.setId(null);
        validarNombreDisponible(categoria.getNombre(), null);
        if (categoria.getActivo() == null) {
            categoria.setActivo(true);
        }
        categoria.setNombre(categoria.getNombre().trim().toUpperCase());
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria datos) {
        Categoria categoria = buscarPorId(id);
        validar(datos);
        validarNombreDisponible(datos.getNombre(), id);
        categoria.setNombre(datos.getNombre().trim().toUpperCase());
        categoria.setDescripcion(datos.getDescripcion());
        if (datos.getActivo() != null) {
            categoria.setActivo(datos.getActivo());
        }
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        if (productoRepository.existsByCategoriaReferenciaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar una categoría que tiene productos");
        }
        categoriaRepository.delete(categoria);
    }

    private void validar(Categoria categoria) {
        if (categoria == null || categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
    }

    private void validarNombreDisponible(String nombre, Long idActual) {
        categoriaRepository.findByNombreIgnoreCase(nombre.trim()).ifPresent(existente -> {
            if (idActual == null || !existente.getId().equals(idActual)) {
                throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + nombre);
            }
        });
    }
}
