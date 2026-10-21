package com.fastfood.fast_food_backend.services;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fastfood.fast_food_backend.entities.ImagenProducto;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.repositories.ImagenProductoRepository;

@Service
public class ImagenProductoService {

    public static final int TAMANO_MAXIMO = 2_000_000;

    private final ImagenProductoRepository imagenes;
    private final ProductoService productos;

    public ImagenProductoService(ImagenProductoRepository imagenes, ProductoService productos) {
        this.imagenes = imagenes;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public ImagenProducto buscar(Long productoId) {
        return imagenes.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Este producto no tiene imagen"));
    }

    @Transactional
    public void guardar(Long productoId, MultipartFile archivo) throws IOException {
        Producto producto = productos.buscarPorId(productoId);
        if (archivo == null || archivo.isEmpty() || archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("La imagen debe tener entre 1 byte y 2 MB");
        }
        byte[] datos = archivo.getBytes();
        String tipo = detectarTipo(datos);
        if (tipo == null || !tipo.equals(archivo.getContentType())) {
            throw new IllegalArgumentException("Solo se aceptan imágenes PNG, JPEG o WebP válidas");
        }
        ImagenProducto imagen = imagenes.findById(productoId).orElseGet(ImagenProducto::new);
        imagen.setProducto(producto);
        imagen.setTipoContenido(tipo);
        imagen.setDatos(datos);
        imagenes.save(imagen);
    }

    @Transactional
    public void quitar(Long productoId) {
        productos.buscarPorId(productoId);
        imagenes.deleteById(productoId);
    }

    private String detectarTipo(byte[] bytes) {
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P'
                && bytes[2] == 'N' && bytes[3] == 'G' && (bytes[4] & 0xff) == 0x0d
                && (bytes[5] & 0xff) == 0x0a && (bytes[6] & 0xff) == 0x1a
                && (bytes[7] & 0xff) == 0x0a) return "image/png";
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) return "image/jpeg";
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I'
                && bytes[2] == 'F' && bytes[3] == 'F' && bytes[8] == 'W'
                && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') return "image/webp";
        return null;
    }
}
