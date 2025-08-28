package com.microservices.inventario.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.microservices.inventario.entity.Inventario;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para las respuestas de inventario siguiendo el estándar JSON API.
 * 
 * Este DTO encapsula la estructura de respuesta requerida para
 * inventario según la especificación JSON API.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventarioResponse {

    private Data data;
    private List<Data> dataList;
    private Meta meta;
    private Links links;
    private Included included;

    public static class Data {
        private String type;
        private String id;
        private Attributes attributes;
        private Relationships relationships;
        private Links links;

        public Data() {}

        public Data(Inventario inventario) {
            this.type = "inventario";
            this.id = inventario.getId().toString();
            this.attributes = new Attributes(inventario);
            this.relationships = new Relationships(inventario);
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

        public Links getLinks() {
            return links;
        }

        public void setLinks(Links links) {
            this.links = links;
        }
    }

    public static class Attributes {
        private Integer cantidad;
        private Integer cantidadMinima;
        private Integer cantidadMaxima;
        private Boolean activo;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaActualizacion;

        public Attributes() {}

        public Attributes(Inventario inventario) {
            this.cantidad = inventario.getCantidad();
            this.cantidadMinima = inventario.getCantidadMinima();
            this.cantidadMaxima = inventario.getCantidadMaxima();
            this.activo = inventario.getActivo();
            this.fechaCreacion = inventario.getFechaCreacion();
            this.fechaActualizacion = inventario.getFechaActualizacion();
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public Integer getCantidadMinima() {
            return cantidadMinima;
        }

        public void setCantidadMinima(Integer cantidadMinima) {
            this.cantidadMinima = cantidadMinima;
        }

        public Integer getCantidadMaxima() {
            return cantidadMaxima;
        }

        public void setCantidadMaxima(Integer cantidadMaxima) {
            this.cantidadMaxima = cantidadMaxima;
        }

        public Boolean getActivo() {
            return activo;
        }

        public void setActivo(Boolean activo) {
            this.activo = activo;
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

    public static class Relationships {
        private ProductoReference producto;

        public Relationships() {}

        public Relationships(Inventario inventario) {
            this.producto = new ProductoReference(inventario.getProductoId());
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
    public static InventarioResponse single(Inventario inventario) {
        InventarioResponse response = new InventarioResponse();
        response.data = new Data(inventario);
        return response;
    }

    public static InventarioResponse list(List<Inventario> inventarios, int total, int page, int size) {
        InventarioResponse response = new InventarioResponse();
        response.dataList = inventarios.stream()
                .map(Data::new)
                .toList();
        response.meta = new Meta(total, page, size);
        return response;
    }

    public static InventarioResponse withProducto(Inventario inventario, ProductoResponse.Data producto) {
        InventarioResponse response = single(inventario);
        response.included = new Included();
        response.included.setProducto(producto);
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

    public Included getIncluded() {
        return included;
    }

    public void setIncluded(Included included) {
        this.included = included;
    }
}
