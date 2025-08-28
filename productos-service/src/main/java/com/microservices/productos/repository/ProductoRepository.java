package com.microservices.productos.repository;

import com.microservices.productos.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Producto.
 * 
 * Proporciona métodos para acceder y manipular datos de productos
 * en la base de datos, incluyendo consultas personalizadas.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su ID.
     * 
     * @param id ID del producto
     * @return Optional que contiene el producto si existe
     */
    Optional<Producto> findById(Long id);

    /**
     * Busca productos por nombre (búsqueda parcial, case-insensitive).
     * 
     * @param nombre Nombre o parte del nombre del producto
     * @return Lista de productos que coinciden con el nombre
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca productos por rango de precios.
     * 
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de productos dentro del rango de precios
     */
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax")
    List<Producto> findByPrecioBetween(@Param("precioMin") java.math.BigDecimal precioMin, 
                                      @Param("precioMax") java.math.BigDecimal precioMax);

    /**
     * Busca productos con paginación.
     * 
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    Page<Producto> findAll(Pageable pageable);

    /**
     * Verifica si existe un producto con el nombre especificado.
     * 
     * @param nombre Nombre del producto
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Cuenta el número total de productos.
     * 
     * @return Número total de productos
     */
    @Query("SELECT COUNT(p) FROM Producto p")
    long countProductos();

    /**
     * Obtiene el precio promedio de todos los productos.
     * 
     * @return Precio promedio
     */
    @Query("SELECT AVG(p.precio) FROM Producto p")
    java.math.BigDecimal getPrecioPromedio();
}
