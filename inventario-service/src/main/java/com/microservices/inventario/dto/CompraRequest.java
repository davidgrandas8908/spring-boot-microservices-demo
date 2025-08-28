package com.microservices.inventario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para las solicitudes de compra siguiendo el estándar JSON API.
 * 
 * Este DTO encapsula la estructura de datos requerida para realizar
 * compras según la especificación JSON API.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
public class CompraRequest {

    @NotNull(message = "El campo 'data' es obligatorio")
    @Valid
    private Data data;

    public static class Data {
        @NotNull(message = "El tipo es obligatorio")
        private String type;

        @Valid
        private Attributes attributes;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Attributes getAttributes() {
            return attributes;
        }

        public void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    public static class Attributes {
        private Long productoId;
        private Integer cantidad;

        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
