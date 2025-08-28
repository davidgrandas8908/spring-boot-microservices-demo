package com.microservices.inventario.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa el inventario de un producto en el sistema.
 * 
 * Esta entidad maneja la información de stock disponible para cada producto,
 * incluyendo metadatos de auditoría para seguimiento de cambios.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@Entity
@Table(name = "inventario")
@EntityListeners(AuditingEntityListener.class)
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del producto es obligatorio")
    @Column(name = "producto_id", nullable = false, unique = true)
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @PositiveOrZero(message = "La cantidad debe ser mayor o igual a cero")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima = 0;

    @Column(name = "cantidad_maxima")
    private Integer cantidadMaxima;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructores
    public Inventario() {}

    public Inventario(Long productoId, Integer cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public Inventario(Long productoId, Integer cantidad, Integer cantidadMinima, Integer cantidadMaxima) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.cantidadMinima = cantidadMinima;
        this.cantidadMaxima = cantidadMaxima;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    // Métodos de negocio
    public boolean tieneStockSuficiente(Integer cantidadSolicitada) {
        return this.cantidad >= cantidadSolicitada;
    }

    public boolean estaEnStockMinimo() {
        return this.cantidad <= this.cantidadMinima;
    }

    public boolean estaEnStockMaximo() {
        return this.cantidadMaxima != null && this.cantidad >= this.cantidadMaxima;
    }

    public void reducirStock(Integer cantidad) {
        if (cantidad > 0 && this.cantidad >= cantidad) {
            this.cantidad -= cantidad;
        } else {
            throw new IllegalArgumentException("No hay suficiente stock disponible");
        }
    }

    public void aumentarStock(Integer cantidad) {
        if (cantidad > 0) {
            if (this.cantidadMaxima != null && (this.cantidad + cantidad) > this.cantidadMaxima) {
                throw new IllegalArgumentException("La cantidad excede el stock máximo permitido");
            }
            this.cantidad += cantidad;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventario that = (Inventario) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Inventario{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", cantidadMinima=" + cantidadMinima +
                ", cantidadMaxima=" + cantidadMaxima +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                '}';
    }
}
