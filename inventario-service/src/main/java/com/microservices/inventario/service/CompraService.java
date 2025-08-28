package com.microservices.inventario.service;

import com.microservices.inventario.dto.CompraRequest;
import com.microservices.inventario.entity.Compra;
import com.microservices.inventario.entity.Inventario;
import com.microservices.inventario.repository.CompraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de compras.
 * 
 * Contiene la lógica de negocio para el proceso de compra,
 * incluyendo validaciones de stock y actualización de inventario.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@Service
@Transactional
public class CompraService {

    private static final Logger logger = LoggerFactory.getLogger(CompraService.class);

    private final CompraRepository compraRepository;
    private final InventarioService inventarioService;

    @Autowired
    public CompraService(CompraRepository compraRepository, InventarioService inventarioService) {
        this.compraRepository = compraRepository;
        this.inventarioService = inventarioService;
    }

    /**
     * Procesa una compra de productos.
     * 
     * @param request Solicitud de compra
     * @return Compra procesada
     * @throws IllegalArgumentException si no hay stock suficiente o el producto no existe
     */
    public Compra procesarCompra(CompraRequest request) {
        Long productoId = request.getData().getAttributes().getProductoId();
        Integer cantidad = request.getData().getAttributes().getCantidad();
        
        logger.info("Procesando compra para producto ID: {} con cantidad: {}", productoId, cantidad);
        
        // Obtener información del producto
        var productoData = inventarioService.obtenerInformacionProducto(productoId);
        BigDecimal precioUnitario = productoData.getAttributes().getPrecio();
        
        // Verificar stock suficiente
        if (!inventarioService.verificarStockSuficiente(productoId, cantidad)) {
            logger.warn("Stock insuficiente para producto ID: {}, cantidad solicitada: {}", productoId, cantidad);
            throw new IllegalArgumentException("Stock insuficiente para el producto con ID: " + productoId);
        }
        
        // Crear la compra
        Compra compra = new Compra(productoId, cantidad, precioUnitario);
        compra.setEstado(Compra.EstadoCompra.COMPLETADA);
        compra.setObservaciones("Compra procesada exitosamente");
        
        Compra compraGuardada = compraRepository.save(compra);
        
        // Reducir el stock
        inventarioService.reducirStock(productoId, cantidad);
        
        logger.info("Compra procesada exitosamente con ID: {} para producto ID: {}", 
                   compraGuardada.getId(), productoId);
        
        return compraGuardada;
    }

    /**
     * Obtiene una compra por su ID.
     * 
     * @param id ID de la compra
     * @return Optional que contiene la compra si existe
     */
    @Transactional(readOnly = true)
    public Optional<Compra> obtenerCompraPorId(Long id) {
        logger.debug("Buscando compra con ID: {}", id);
        return compraRepository.findById(id);
    }

    /**
     * Obtiene compras por ID del producto.
     * 
     * @param productoId ID del producto
     * @param page Número de página (base 0)
     * @param size Tamaño de la página
     * @return Página de compras del producto
     */
    @Transactional(readOnly = true)
    public Page<Compra> obtenerComprasPorProducto(Long productoId, int page, int size) {
        logger.debug("Obteniendo compras para producto ID: {} - página: {}, tamaño: {}", productoId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return compraRepository.findByProductoId(productoId, pageable);
    }

    /**
     * Obtiene todas las compras con paginación.
     * 
     * @param page Número de página (base 0)
     * @param size Tamaño de la página
     * @return Página de compras
     */
    @Transactional(readOnly = true)
    public Page<Compra> obtenerCompras(int page, int size) {
        logger.debug("Obteniendo compras - página: {}, tamaño: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return compraRepository.findAll(pageable);
    }

    /**
     * Obtiene compras por estado.
     * 
     * @param estado Estado de la compra
     * @param page Número de página (base 0)
     * @param size Tamaño de la página
     * @return Página de compras con el estado especificado
     */
    @Transactional(readOnly = true)
    public Page<Compra> obtenerComprasPorEstado(Compra.EstadoCompra estado, int page, int size) {
        logger.debug("Obteniendo compras con estado: {} - página: {}, tamaño: {}", estado, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return compraRepository.findByEstado(estado, pageable);
    }

    /**
     * Obtiene las compras más recientes.
     * 
     * @param limit Número máximo de compras a retornar
     * @return Lista de compras más recientes
     */
    @Transactional(readOnly = true)
    public List<Compra> obtenerComprasRecientes(int limit) {
        logger.debug("Obteniendo {} compras más recientes", limit);
        Pageable pageable = PageRequest.of(0, limit);
        return compraRepository.findComprasRecientes(pageable);
    }

    /**
     * Cancela una compra.
     * 
     * @param id ID de la compra
     * @return Compra cancelada
     * @throws IllegalArgumentException si la compra no existe o ya está cancelada
     */
    public Compra cancelarCompra(Long id) {
        logger.info("Cancelando compra con ID: {}", id);
        
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada con ID: " + id));
        
        if (compra.getEstado() == Compra.EstadoCompra.CANCELADA) {
            logger.warn("La compra con ID: {} ya está cancelada", id);
            throw new IllegalArgumentException("La compra ya está cancelada");
        }
        
        // Solo se puede cancelar compras completadas
        if (compra.getEstado() == Compra.EstadoCompra.COMPLETADA) {
            // Restaurar el stock
            inventarioService.aumentarStock(compra.getProductoId(), compra.getCantidad());
            logger.info("Stock restaurado para producto ID: {}", compra.getProductoId());
        }
        
        compra.setEstado(Compra.EstadoCompra.CANCELADA);
        compra.setObservaciones("Compra cancelada");
        
        Compra compraCancelada = compraRepository.save(compra);
        
        logger.info("Compra cancelada exitosamente con ID: {}", id);
        
        return compraCancelada;
    }

    /**
     * Obtiene estadísticas de compras.
     * 
     * @return Array con [totalCompras, totalVentas, comprasCompletadas, comprasCanceladas]
     */
    @Transactional(readOnly = true)
    public Object[] obtenerEstadisticas() {
        logger.debug("Obteniendo estadísticas de compras");
        
        long totalCompras = compraRepository.countCompras();
        BigDecimal totalVentas = compraRepository.getTotalVentas();
        long comprasCompletadas = compraRepository.countByEstado(Compra.EstadoCompra.COMPLETADA);
        long comprasCanceladas = compraRepository.countByEstado(Compra.EstadoCompra.CANCELADA);
        
        return new Object[]{
            totalCompras, 
            totalVentas != null ? totalVentas : BigDecimal.ZERO, 
            comprasCompletadas, 
            comprasCanceladas
        };
    }

    /**
     * Obtiene el total de ventas por producto.
     * 
     * @param productoId ID del producto
     * @return Total de ventas del producto
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalVentasPorProducto(Long productoId) {
        logger.debug("Obteniendo total de ventas para producto ID: {}", productoId);
        
        BigDecimal totalVentas = compraRepository.getTotalVentasPorProducto(productoId);
        return totalVentas != null ? totalVentas : BigDecimal.ZERO;
    }

    /**
     * Verifica si una compra puede ser procesada.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad solicitada
     * @return true si la compra puede ser procesada, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean puedeProcesarCompra(Long productoId, Integer cantidad) {
        logger.debug("Verificando si se puede procesar compra para producto ID: {}, cantidad: {}", 
                    productoId, cantidad);
        
        // Verificar que el producto existe
        try {
            inventarioService.obtenerInformacionProducto(productoId);
        } catch (Exception e) {
            logger.warn("Producto ID: {} no existe", productoId);
            return false;
        }
        
        // Verificar stock suficiente
        boolean tieneStock = inventarioService.verificarStockSuficiente(productoId, cantidad);
        
        logger.debug("Compra para producto ID: {} puede ser procesada: {}", productoId, tieneStock);
        
        return tieneStock;
    }
}
