package com.microservices.productos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microservices.productos.entity.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para las respuestas de productos siguiendo el estándar JSON API.
 * 
 * Este DTO encapsula la estructura de respuesta requerida para
 * productos según la especificación JSON API.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductoResponse {

    private Data data;
    private List<Data> dataList;
    private Meta meta;
    private Links links;

    public static class Data {
        private String type;
        private String id;
        private Attributes attributes;
        private Links links;

        public Data() {}

        public Data(Producto producto) {
            this.type = "productos";
            this.id = producto.getId().toString();
            this.attributes = new Attributes(producto);
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

        public Links getLinks() {
            return links;
        }

        public void setLinks(Links links) {
            this.links = links;
        }
    }

    public static class Attributes {
        private String nombre;
        private BigDecimal precio;
        private String descripcion;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaActualizacion;

        public Attributes() {}

        public Attributes(Producto producto) {
            this.nombre = producto.getNombre();
            this.precio = producto.getPrecio();
            this.descripcion = producto.getDescripcion();
            this.fechaCreacion = producto.getFechaCreacion();
            this.fechaActualizacion = producto.getFechaActualizacion();
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public BigDecimal getPrecio() {
            return precio;
        }

        public void setPrecio(BigDecimal precio) {
            this.precio = precio;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public LocalDateTime getFechaCreacion() {
            return fechaCreacion;
        }

        public void setFechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
        }

        public LocalDateTime getFechaActualizacion() {
            return fechaActualizacion;
        }

        public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
            this.fechaActualizacion = fechaActualizacion;
        }
    }

    public static class Links {
        private String self;
        private String next;
        private String prev;

        public String getSelf() {
            return self;
        }

        public void setSelf(String self) {
            this.self = self;
        }

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public String getPrev() {
            return prev;
        }

        public void setPrev(String prev) {
            this.prev = prev;
        }
    }

    public static class Meta {
        private int total;
        private int page;
        private int size;

        public Meta(int total, int page, int size) {
            this.total = total;
            this.page = page;
            this.size = size;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    // Constructores estáticos para facilitar la creación
    public static ProductoResponse single(Producto producto) {
        ProductoResponse response = new ProductoResponse();
        response.data = new Data(producto);
        return response;
    }

    public static ProductoResponse list(List<Producto> productos, int total, int page, int size) {
        ProductoResponse response = new ProductoResponse();
        response.dataList = productos.stream()
                .map(Data::new)
                .toList();
        response.meta = new Meta(total, page, size);
        return response;
    }

    // Getters y Setters
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }
}
