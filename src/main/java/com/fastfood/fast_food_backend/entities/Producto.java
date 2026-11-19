package com.fastfood.fast_food_backend.entities;

import java.math.BigDecimal;

import org.hibernate.annotations.Formula;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private int stock;

    private String categoria;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoriaReferencia;

    // Se calcula al leer el producto; permite que Angular no pida una foto que no existe.
    @Formula("(case when exists (select 1 from imagenes_producto i where i.producto_id = id) then true else false end)")
    private boolean tieneImagen;

    // Indica si el JSON recibido traía "stock"; si no lo trae, al editar se conserva el stock actual.
    @Transient
    @JsonIgnore
    private boolean stockAsignado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isTieneImagen() {
        return tieneImagen;
    }

    public void setStock(int stock) {
        this.stock = stock;
        this.stockAsignado = true;
    }

    public boolean stockFueEnviado() {
        return stockAsignado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Categoria getCategoriaReferencia() {
        return categoriaReferencia;
    }

    public void setCategoriaReferencia(Categoria categoriaReferencia) {
        this.categoriaReferencia = categoriaReferencia;
    }

    // Un producto de una categoría inactiva sigue visible en el panel, pero no se puede agregar al carrito ni comprar.
    public boolean categoriaDisponible() {
        return categoriaReferencia == null || Boolean.TRUE.equals(categoriaReferencia.getActivo());
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getCategoriaId() {
        return categoriaReferencia != null ? categoriaReferencia.getId() : null;
    }
}
