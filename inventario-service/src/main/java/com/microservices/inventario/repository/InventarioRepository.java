package com.microservices.inventario.repository;

import com.microservices.inventario.entity.Inventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Inventario.
 * 
 * Proporciona métodos para acceder y manipular datos de inventario
 * en la base de datos, incluyendo consultas personalizadas.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    /**
     * Busca un inventario por su ID.
     * 
     * @param id ID del inventario
     * @return Optional que contiene el inventario si existe
     */
    Optional<Inventario> findById(Long id);

    /**
     * Busca un inventario por el ID del producto.
     * 
     * @param productoId ID del producto
     * @return Optional que contiene el inventario si existe
     */
    Optional<Inventario> findByProductoId(Long productoId);

    /**
     * Busca inventarios con stock bajo (cantidad <= cantidad mínima).
     * 
     * @return Lista de inventarios con stock bajo
     */
    @Query("SELECT i FROM Inventario i WHERE i.cantidad <= i.cantidadMinima AND i.activo = true")
    List<Inventario> findInventariosConStockBajo();

    /**
     * Busca inventarios activos.
     * 
     * @return Lista de inventarios activos
     */
    List<Inventario> findByActivoTrue();

    /**
     * Busca inventarios con paginación.
     * 
     * @param pageable Configuración de paginación
     * @return Página de inventarios
     */
    Page<Inventario> findAll(Pageable pageable);

    /**
     * Busca inventarios activos con paginación.
     * 
     * @param pageable Configuración de paginación
     * @return Página de inventarios activos
     */
    Page<Inventario> findByActivoTrue(Pageable pageable);

    /**
     * Verifica si existe un inventario para un producto específico.
     * 
     * @param productoId ID del producto
     * @return true si existe, false en caso contrario
     */
    boolean existsByProductoId(Long productoId);

    /**
     * Cuenta el número total de inventarios activos.
     * 
     * @return Número total de inventarios activos
     */
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.activo = true")
    long countInventariosActivos();

    /**
     * Cuenta el número de inventarios con stock bajo.
     * 
     * @return Número de inventarios con stock bajo
     */
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.cantidad <= i.cantidadMinima AND i.activo = true")
    long countInventariosConStockBajo();

    /**
     * Obtiene el stock total de todos los productos activos.
     * 
     * @return Stock total
     */
    @Query("SELECT SUM(i.cantidad) FROM Inventario i WHERE i.activo = true")
    Long getStockTotal();

    /**
     * Busca inventarios por rango de cantidad.
     * 
     * @param cantidadMin Cantidad mínima
     * @param cantidadMax Cantidad máxima
     * @return Lista de inventarios dentro del rango
     */
    @Query("SELECT i FROM Inventario i WHERE i.cantidad BETWEEN :cantidadMin AND :cantidadMax AND i.activo = true")
    List<Inventario> findByCantidadBetween(@Param("cantidadMin") Integer cantidadMin, 
                                          @Param("cantidadMax") Integer cantidadMax);
}
