package com.fastfood.fast_food_backend.controllers;

import java.io.IOException;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fastfood.fast_food_backend.entities.ImagenProducto;
import com.fastfood.fast_food_backend.services.ImagenProductoService;

@RestController
@RequestMapping("/api/productos/{productoId}/imagen")
public class ImagenProductoController {

    private final ImagenProductoService servicio;

    public ImagenProductoController(ImagenProductoService servicio) { this.servicio = servicio; }

    @GetMapping
    public ResponseEntity<byte[]> obtener(@PathVariable Long productoId) {
        ImagenProducto imagen = servicio.buscar(productoId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(imagen.getTipoContenido()))
                .body(imagen.getDatos());
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> guardar(@PathVariable Long productoId,
            @RequestPart("archivo") MultipartFile archivo) throws IOException {
        servicio.guardar(productoId, archivo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> quitar(@PathVariable Long productoId) {
        servicio.quitar(productoId);
        return ResponseEntity.noContent().build();
    }
}
