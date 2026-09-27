package com.fastfood.fast_food_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProcesarPedidoRequest {

    private Long usuarioId;
    private String tipoEntrega;
    private String direccionEntrega;
    private String observaciones;
    private BigDecimal costoEntrega;
    private List<DetallePedidoRequest> detalles;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getCostoEntrega() {
        return costoEntrega;
    }

    public void setCostoEntrega(BigDecimal costoEntrega) {
        this.costoEntrega = costoEntrega;
    }

    public List<DetallePedidoRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoRequest> detalles) {
        this.detalles = detalles;
    }
}
