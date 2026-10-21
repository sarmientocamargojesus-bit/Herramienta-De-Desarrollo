package com.fastfood.fast_food_backend.entities;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Guarda la imagen aparte para que el catálogo no cargue sus bytes. */
@Entity
@Table(name = "imagenes_producto")
public class ImagenProducto {

    @Id
    @Column(name = "producto_id")
    private Long productoId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "tipo_contenido", nullable = false, length = 30)
    private String tipoContenido;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false, length = 2_000_000)
    private byte[] datos;

    public Long getProductoId() { return productoId; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getTipoContenido() { return tipoContenido; }
    public void setTipoContenido(String tipoContenido) { this.tipoContenido = tipoContenido; }
    public byte[] getDatos() { return datos; }
    public void setDatos(byte[] datos) { this.datos = datos; }
}
