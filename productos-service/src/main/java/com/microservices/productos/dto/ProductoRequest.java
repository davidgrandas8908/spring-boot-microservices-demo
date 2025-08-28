package com.microservices.productos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para las solicitudes de productos siguiendo el estándar JSON API.
 * 
 * Este DTO encapsula la estructura de datos requerida para crear
 * y actualizar productos según la especificación JSON API.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
public class ProductoRequest {

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
        private String nombre;
        private java.math.BigDecimal precio;
        private String descripcion;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public java.math.BigDecimal getPrecio() {
            return precio;
        }

        public void setPrecio(java.math.BigDecimal precio) {
            this.precio = precio;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
