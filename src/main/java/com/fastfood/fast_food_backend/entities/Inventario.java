package com.fastfood.fast_food_backend.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventarios")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_stock_bajo")
    private LocalDateTime fechaStockBajo;

    @Column(name = "fecha_agotado")
    private LocalDateTime fechaAgotado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LocalDateTime getFechaStockBajo() {
        return fechaStockBajo;
    }

    public void setFechaStockBajo(LocalDateTime fechaStockBajo) {
        this.fechaStockBajo = fechaStockBajo;
    }

    public LocalDateTime getFechaAgotado() {
        return fechaAgotado;
    }

    public void setFechaAgotado(LocalDateTime fechaAgotado) {
        this.fechaAgotado = fechaAgotado;
    }
}
