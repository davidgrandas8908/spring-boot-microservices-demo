package com.microservices.productos.service;

import com.microservices.productos.dto.ProductoRequest;
import com.microservices.productos.entity.Producto;
import com.microservices.productos.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de productos.
 * 
 * Contiene la lógica de negocio para operaciones CRUD de productos,
 * incluyendo validaciones y transformaciones de datos.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@Service
@Transactional
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Crea un nuevo producto.
     * 
     * @param request DTO con los datos del producto a crear
     * @return Producto creado
     * @throws IllegalArgumentException si el producto ya existe
     */
    public Producto crearProducto(ProductoRequest request) {
        logger.info("Creando nuevo producto: {}", request.getData().getAttributes().getNombre());
        
        ProductoRequest.Attributes attributes = request.getData().getAttributes();
        
        // Validar que no exista un producto con el mismo nombre
        if (productoRepository.existsByNombre(attributes.getNombre())) {
            logger.warn("Intento de crear producto con nombre duplicado: {}", attributes.getNombre());
            throw new IllegalArgumentException("Ya existe un producto con el nombre: " + attributes.getNombre());
        }

        Producto producto = new Producto();
        producto.setNombre(attributes.getNombre());
        producto.setPrecio(attributes.getPrecio());
        producto.setDescripcion(attributes.getDescripcion());

        Producto productoGuardado = productoRepository.save(producto);
        logger.info("Producto creado exitosamente con ID: {}", productoGuardado.getId());
        
        return productoGuardado;
    }

    /**
     * Obtiene un producto por su ID.
     * 
     * @param id ID del producto
     * @return Optional que contiene el producto si existe
     */
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        logger.debug("Buscando producto con ID: {}", id);
        return productoRepository.findById(id);
    }

    /**
     * Obtiene todos los productos con paginación.
     * 
     * @param page Número de página (base 0)
     * @param size Tamaño de la página
     * @return Página de productos
     */
    @Transactional(readOnly = true)
    public Page<Producto> obtenerProductos(int page, int size) {
        logger.debug("Obteniendo productos - página: {}, tamaño: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return productoRepository.findAll(pageable);
    }

    /**
     * Obtiene todos los productos sin paginación.
     * 
     * @return Lista de todos los productos
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        logger.debug("Obteniendo todos los productos");
        return productoRepository.findAll();
    }

    /**
     * Busca productos por nombre.
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de productos que coinciden con el nombre
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarProductosPorNombre(String nombre) {
        logger.debug("Buscando productos por nombre: {}", nombre);
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id ID del producto a actualizar
     * @param request DTO con los nuevos datos del producto
     * @return Producto actualizado
     * @throws IllegalArgumentException si el producto no existe
     */
    public Producto actualizarProducto(Long id, ProductoRequest request) {
        logger.info("Actualizando producto con ID: {}", id);
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        ProductoRequest.Attributes attributes = request.getData().getAttributes();
        
        // Validar que no exista otro producto con el mismo nombre (excluyendo el actual)
        if (!producto.getNombre().equals(attributes.getNombre()) && 
            productoRepository.existsByNombre(attributes.getNombre())) {
            logger.warn("Intento de actualizar producto con nombre duplicado: {}", attributes.getNombre());
            throw new IllegalArgumentException("Ya existe un producto con el nombre: " + attributes.getNombre());
        }

        producto.setNombre(attributes.getNombre());
        producto.setPrecio(attributes.getPrecio());
        producto.setDescripcion(attributes.getDescripcion());

        Producto productoActualizado = productoRepository.save(producto);
        logger.info("Producto actualizado exitosamente con ID: {}", productoActualizado.getId());
        
        return productoActualizado;
    }

    /**
     * Elimina un producto por su ID.
     * 
     * @param id ID del producto a eliminar
     * @throws IllegalArgumentException si el producto no existe
     */
    public void eliminarProducto(Long id) {
        logger.info("Eliminando producto con ID: {}", id);
        
        if (!productoRepository.existsById(id)) {
            logger.warn("Intento de eliminar producto inexistente con ID: {}", id);
            throw new IllegalArgumentException("Producto no encontrado con ID: " + id);
        }

        productoRepository.deleteById(id);
        logger.info("Producto eliminado exitosamente con ID: {}", id);
    }

    /**
     * Verifica si un producto existe.
     * 
     * @param id ID del producto
     * @return true si existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean existeProducto(Long id) {
        return productoRepository.existsById(id);
    }

    /**
     * Obtiene estadísticas básicas de productos.
     * 
     * @return Array con [totalProductos, precioPromedio]
     */
    @Transactional(readOnly = true)
    public Object[] obtenerEstadisticas() {
        logger.debug("Obteniendo estadísticas de productos");
        long totalProductos = productoRepository.countProductos();
        java.math.BigDecimal precioPromedio = productoRepository.getPrecioPromedio();
        return new Object[]{totalProductos, precioPromedio};
    }
}
