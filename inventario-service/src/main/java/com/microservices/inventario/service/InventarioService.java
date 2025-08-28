package com.microservices.inventario.service;

import com.microservices.inventario.client.ProductosClient;
import com.microservices.inventario.dto.ProductoResponse;
import com.microservices.inventario.entity.Inventario;
import com.microservices.inventario.repository.InventarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de inventario.
 * 
 * Contiene la lógica de negocio para operaciones de inventario,
 * incluyendo validaciones y comunicación con el microservicio de productos.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@Service
@Transactional
public class InventarioService {

    private static final Logger logger = LoggerFactory.getLogger(InventarioService.class);

    private final InventarioRepository inventarioRepository;
    private final ProductosClient productosClient;

    @Value("${productos.service.api-key}")
    private String productosApiKey;

    @Autowired
    public InventarioService(InventarioRepository inventarioRepository, ProductosClient productosClient) {
        this.inventarioRepository = inventarioRepository;
        this.productosClient = productosClient;
    }

    /**
     * Crea un nuevo registro de inventario para un producto.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad inicial
     * @param cantidadMinima Cantidad mínima (opcional)
     * @param cantidadMaxima Cantidad máxima (opcional)
     * @return Inventario creado
     * @throws IllegalArgumentException si el producto no existe o ya tiene inventario
     */
    public Inventario crearInventario(Long productoId, Integer cantidad, Integer cantidadMinima, Integer cantidadMaxima) {
        logger.info("Creando inventario para producto ID: {} con cantidad: {}", productoId, cantidad);
        
        // Verificar que el producto existe
        verificarProductoExiste(productoId);
        
        // Verificar que no existe inventario para este producto
        if (inventarioRepository.existsByProductoId(productoId)) {
            logger.warn("Ya existe inventario para el producto ID: {}", productoId);
            throw new IllegalArgumentException("Ya existe inventario para el producto con ID: " + productoId);
        }

        Inventario inventario = new Inventario(productoId, cantidad, cantidadMinima, cantidadMaxima);
        Inventario inventarioGuardado = inventarioRepository.save(inventario);
        
        logger.info("Inventario creado exitosamente con ID: {} para producto ID: {}", 
                   inventarioGuardado.getId(), productoId);
        
        return inventarioGuardado;
    }

    /**
     * Obtiene el inventario de un producto por su ID.
     * 
     * @param productoId ID del producto
     * @return Optional que contiene el inventario si existe
     */
    @Transactional(readOnly = true)
    public Optional<Inventario> obtenerInventarioPorProductoId(Long productoId) {
        logger.debug("Buscando inventario para producto ID: {}", productoId);
        return inventarioRepository.findByProductoId(productoId);
    }

    /**
     * Obtiene un inventario por su ID.
     * 
     * @param id ID del inventario
     * @return Optional que contiene el inventario si existe
     */
    @Transactional(readOnly = true)
    public Optional<Inventario> obtenerInventarioPorId(Long id) {
        logger.debug("Buscando inventario con ID: {}", id);
        return inventarioRepository.findById(id);
    }

    /**
     * Obtiene todos los inventarios con paginación.
     * 
     * @param page Número de página (base 0)
     * @param size Tamaño de la página
     * @return Página de inventarios
     */
    @Transactional(readOnly = true)
    public Page<Inventario> obtenerInventarios(int page, int size) {
        logger.debug("Obteniendo inventarios - página: {}, tamaño: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return inventarioRepository.findByActivoTrue(pageable);
    }

    /**
     * Obtiene todos los inventarios activos.
     * 
     * @return Lista de inventarios activos
     */
    @Transactional(readOnly = true)
    public List<Inventario> obtenerTodosLosInventarios() {
        logger.debug("Obteniendo todos los inventarios activos");
        return inventarioRepository.findByActivoTrue();
    }

    /**
     * Obtiene inventarios con stock bajo.
     * 
     * @return Lista de inventarios con stock bajo
     */
    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventariosConStockBajo() {
        logger.debug("Obteniendo inventarios con stock bajo");
        return inventarioRepository.findInventariosConStockBajo();
    }

    /**
     * Actualiza la cantidad de un inventario.
     * 
     * @param productoId ID del producto
     * @param nuevaCantidad Nueva cantidad
     * @return Inventario actualizado
     * @throws IllegalArgumentException si el inventario no existe
     */
    public Inventario actualizarCantidad(Long productoId, Integer nuevaCantidad) {
        logger.info("Actualizando cantidad para producto ID: {} a: {}", productoId, nuevaCantidad);
        
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado para el producto con ID: " + productoId));

        inventario.setCantidad(nuevaCantidad);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        
        logger.info("Cantidad actualizada exitosamente para producto ID: {}", productoId);
        
        return inventarioActualizado;
    }

    /**
     * Aumenta el stock de un producto.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad a aumentar
     * @return Inventario actualizado
     * @throws IllegalArgumentException si el inventario no existe
     */
    public Inventario aumentarStock(Long productoId, Integer cantidad) {
        logger.info("Aumentando stock para producto ID: {} en: {}", productoId, cantidad);
        
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado para el producto con ID: " + productoId));

        inventario.aumentarStock(cantidad);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        
        logger.info("Stock aumentado exitosamente para producto ID: {}", productoId);
        
        return inventarioActualizado;
    }

    /**
     * Reduce el stock de un producto.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad a reducir
     * @return Inventario actualizado
     * @throws IllegalArgumentException si el inventario no existe o no hay suficiente stock
     */
    public Inventario reducirStock(Long productoId, Integer cantidad) {
        logger.info("Reduciendo stock para producto ID: {} en: {}", productoId, cantidad);
        
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado para el producto con ID: " + productoId));

        inventario.reducirStock(cantidad);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        
        logger.info("Stock reducido exitosamente para producto ID: {}", productoId);
        
        return inventarioActualizado;
    }

    /**
     * Verifica si hay stock suficiente para un producto.
     * 
     * @param productoId ID del producto
     * @param cantidadSolicitada Cantidad solicitada
     * @return true si hay stock suficiente, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean verificarStockSuficiente(Long productoId, Integer cantidadSolicitada) {
        logger.debug("Verificando stock suficiente para producto ID: {}, cantidad solicitada: {}", 
                    productoId, cantidadSolicitada);
        
        Optional<Inventario> inventarioOpt = inventarioRepository.findByProductoId(productoId);
        if (inventarioOpt.isEmpty()) {
            logger.warn("Inventario no encontrado para producto ID: {}", productoId);
            return false;
        }

        Inventario inventario = inventarioOpt.get();
        boolean tieneStock = inventario.tieneStockSuficiente(cantidadSolicitada);
        
        logger.debug("Producto ID: {} tiene stock suficiente: {}", productoId, tieneStock);
        
        return tieneStock;
    }

    /**
     * Obtiene estadísticas del inventario.
     * 
     * @return Array con [totalInventarios, totalStock, inventariosConStockBajo]
     */
    @Transactional(readOnly = true)
    public Object[] obtenerEstadisticas() {
        logger.debug("Obteniendo estadísticas de inventario");
        
        long totalInventarios = inventarioRepository.countInventariosActivos();
        Long totalStock = inventarioRepository.getStockTotal();
        long inventariosConStockBajo = inventarioRepository.countInventariosConStockBajo();
        
        return new Object[]{totalInventarios, totalStock != null ? totalStock : 0L, inventariosConStockBajo};
    }

    /**
     * Desactiva un inventario.
     * 
     * @param productoId ID del producto
     * @throws IllegalArgumentException si el inventario no existe
     */
    public void desactivarInventario(Long productoId) {
        logger.info("Desactivando inventario para producto ID: {}", productoId);
        
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado para el producto con ID: " + productoId));

        inventario.setActivo(false);
        inventarioRepository.save(inventario);
        
        logger.info("Inventario desactivado exitosamente para producto ID: {}", productoId);
    }

    /**
     * Verifica que un producto existe en el microservicio de productos.
     * 
     * @param productoId ID del producto
     * @throws IllegalArgumentException si el producto no existe
     */
    private void verificarProductoExiste(Long productoId) {
        try {
            var response = productosClient.existeProducto(productoId, productosApiKey);
            if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody())) {
                logger.debug("Producto ID: {} existe en el microservicio de productos", productoId);
            } else {
                logger.warn("Producto ID: {} no existe en el microservicio de productos", productoId);
                throw new IllegalArgumentException("Producto no encontrado con ID: " + productoId);
            }
        } catch (Exception e) {
            logger.error("Error al verificar existencia del producto ID: {}", productoId, e);
            throw new IllegalArgumentException("Error al verificar la existencia del producto con ID: " + productoId, e);
        }
    }

    /**
     * Obtiene información de un producto desde el microservicio de productos.
     * 
     * @param productoId ID del producto
     * @return Información del producto
     * @throws IllegalArgumentException si el producto no existe
     */
    public ProductoResponse.Data obtenerInformacionProducto(Long productoId) {
        try {
            var response = productosClient.obtenerProductoPorId(productoId, productosApiKey);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Información del producto ID: {} obtenida exitosamente", productoId);
                return response.getBody().getData();
            } else {
                logger.warn("No se pudo obtener información del producto ID: {}", productoId);
                throw new IllegalArgumentException("No se pudo obtener información del producto con ID: " + productoId);
            }
        } catch (Exception e) {
            logger.error("Error al obtener información del producto ID: {}", productoId, e);
            throw new IllegalArgumentException("Error al obtener información del producto con ID: " + productoId, e);
        }
    }
}
