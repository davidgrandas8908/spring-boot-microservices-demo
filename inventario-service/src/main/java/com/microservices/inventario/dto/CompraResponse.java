package com.microservices.inventario.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.microservices.inventario.entity.Compra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para las respuestas de compra siguiendo el estándar JSON API.
 * 
 * Este DTO encapsula la estructura de respuesta requerida para
 * compras según la especificación JSON API.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompraResponse {

    private Data data;
    private Included included;

    public static class Data {
        private String type;
        private String id;
        private Attributes attributes;
        private Relationships relationships;

        public Data() {}

        public Data(Compra compra) {
            this.type = "compras";
            this.id = compra.getId().toString();
            this.attributes = new Attributes(compra);
            this.relationships = new Relationships(compra);
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Attributes getAttributes() {
            return attributes;
        }

        public void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        public Relationships getRelationships() {
            return relationships;
        }

        public void setRelationships(Relationships relationships) {
            this.relationships = relationships;
        }
    }

    public static class Attributes {
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal precioTotal;
        private String estado;
        private String observaciones;
        private LocalDateTime fechaCompra;

        public Attributes() {}

        public Attributes(Compra compra) {
            this.cantidad = compra.getCantidad();
            this.precioUnitario = compra.getPrecioUnitario();
            this.precioTotal = compra.getPrecioTotal();
            this.estado = compra.getEstado().name();
            this.observaciones = compra.getObservaciones();
            this.fechaCompra = compra.getFechaCompra();
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
        }

        public BigDecimal getPrecioTotal() {
            return precioTotal;
        }

        public void setPrecioTotal(BigDecimal precioTotal) {
            this.precioTotal = precioTotal;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }

        public LocalDateTime getFechaCompra() {
            return fechaCompra;
        }

        public void setFechaCompra(LocalDateTime fechaCompra) {
            this.fechaCompra = fechaCompra;
        }
    }

    public static class Relationships {
        private ProductoReference producto;

        public Relationships() {}

        public Relationships(Compra compra) {
            this.producto = new ProductoReference(compra.getProductoId());
        }

        public ProductoReference getProducto() {
            return producto;
        }

        public void setProducto(ProductoReference producto) {
            this.producto = producto;
        }
    }

    public static class ProductoReference {
        private Data data;

        public ProductoReference() {}

        public ProductoReference(Long productoId) {
            this.data = new Data();
            this.data.setType("productos");
            this.data.setId(productoId.toString());
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public static class Data {
            private String type;
            private String id;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }
        }
    }

    public static class Included {
        private ProductoResponse.Data producto;

        public ProductoResponse.Data getProducto() {
            return producto;
        }

        public void setProducto(ProductoResponse.Data producto) {
            this.producto = producto;
        }
    }

    // Constructores estáticos para facilitar la creación
    public static CompraResponse single(Compra compra) {
        CompraResponse response = new CompraResponse();
        response.data = new Data(compra);
        return response;
    }

    public static CompraResponse withProducto(Compra compra, ProductoResponse.Data producto) {
        CompraResponse response = single(compra);
        response.included = new Included();
        response.included.setProducto(producto);
        return response;
    }

    public static CompraResponse list(List<Compra> compras, int totalElements, int page, int size) {
        CompraResponse response = new CompraResponse();
        response.data = new Data();
        response.data.setType("compras");
        response.data.setId("list");
        
        // Crear lista de datos
        List<Data> dataList = compras.stream()
                .map(Data::new)
                .collect(Collectors.toList());
        
        // Crear atributos de lista
        Attributes listAttributes = new Attributes();
        listAttributes.setCantidad(totalElements);
        listAttributes.setPrecioTotal(BigDecimal.valueOf(page));
        listAttributes.setPrecioUnitario(BigDecimal.valueOf(size));
        
        response.data.setAttributes(listAttributes);
        return response;
    }

    // Getters y Setters
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Included getIncluded() {
        return included;
    }

    public void setIncluded(Included included) {
        this.included = included;
    }
}
