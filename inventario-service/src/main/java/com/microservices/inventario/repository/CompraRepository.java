package com.microservices.inventario.repository;

import com.microservices.inventario.entity.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Compra.
 * 
 * Proporciona métodos para acceder y manipular datos de compras
 * en la base de datos, incluyendo consultas personalizadas.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    /**
     * Busca una compra por su ID.
     * 
     * @param id ID de la compra
     * @return Optional que contiene la compra si existe
     */
    Optional<Compra> findById(Long id);

    /**
     * Busca compras por ID del producto.
     * 
     * @param productoId ID del producto
     * @return Lista de compras del producto
     */
    List<Compra> findByProductoId(Long productoId);

    /**
     * Busca compras por ID del producto con paginación.
     * 
     * @param productoId ID del producto
     * @param pageable Configuración de paginación
     * @return Página de compras del producto
     */
    Page<Compra> findByProductoId(Long productoId, Pageable pageable);

    /**
     * Busca compras por estado.
     * 
     * @param estado Estado de la compra
     * @return Lista de compras con el estado especificado
     */
    List<Compra> findByEstado(Compra.EstadoCompra estado);

    /**
     * Busca compras por estado con paginación.
     * 
     * @param estado Estado de la compra
     * @param pageable Configuración de paginación
     * @return Página de compras con el estado especificado
     */
    Page<Compra> findByEstado(Compra.EstadoCompra estado, Pageable pageable);

    /**
     * Busca compras realizadas en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de compras en el rango de fechas
     */
    @Query("SELECT c FROM Compra c WHERE c.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    List<Compra> findByFechaCompraBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                         @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca compras por rango de precios totales.
     * 
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de compras en el rango de precios
     */
    @Query("SELECT c FROM Compra c WHERE c.precioTotal BETWEEN :precioMin AND :precioMax")
    List<Compra> findByPrecioTotalBetween(@Param("precioMin") BigDecimal precioMin, 
                                         @Param("precioMax") BigDecimal precioMax);

    /**
     * Cuenta el número total de compras.
     * 
     * @return Número total de compras
     */
    @Query("SELECT COUNT(c) FROM Compra c")
    long countCompras();

    /**
     * Cuenta el número de compras por estado.
     * 
     * @param estado Estado de la compra
     * @return Número de compras con el estado especificado
     */
    long countByEstado(Compra.EstadoCompra estado);

    /**
     * Obtiene el total de ventas (suma de precios totales de compras completadas).
     * 
     * @return Total de ventas
     */
    @Query("SELECT SUM(c.precioTotal) FROM Compra c WHERE c.estado = 'COMPLETADA'")
    BigDecimal getTotalVentas();

    /**
     * Obtiene el total de ventas por producto.
     * 
     * @param productoId ID del producto
     * @return Total de ventas del producto
     */
    @Query("SELECT SUM(c.precioTotal) FROM Compra c WHERE c.productoId = :productoId AND c.estado = 'COMPLETADA'")
    BigDecimal getTotalVentasPorProducto(@Param("productoId") Long productoId);

    /**
     * Busca las compras más recientes.
     * 
     * @param limit Número máximo de compras a retornar
     * @return Lista de compras más recientes
     */
    @Query("SELECT c FROM Compra c ORDER BY c.fechaCompra DESC")
    List<Compra> findComprasRecientes(Pageable pageable);

    /**
     * Busca compras por cantidad (exacta).
     * 
     * @param cantidad Cantidad exacta
     * @return Lista de compras con la cantidad especificada
     */
    List<Compra> findByCantidad(Integer cantidad);

    /**
     * Busca compras por cantidad mayor o igual.
     * 
     * @param cantidad Cantidad mínima
     * @return Lista de compras con cantidad mayor o igual
     */
    @Query("SELECT c FROM Compra c WHERE c.cantidad >= :cantidad")
    List<Compra> findByCantidadGreaterThanEqual(@Param("cantidad") Integer cantidad);
}
