package com.microservices.inventario.controller;

import com.microservices.inventario.dto.InventarioResponse;
import com.microservices.inventario.entity.Inventario;
import com.microservices.inventario.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de inventario.
 * 
 * Expone endpoints para operaciones de inventario siguiendo
 * el estándar JSON API.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/inventario")
@Tag(name = "Inventario", description = "API para gestión de inventario")
public class InventarioController {

    private static final Logger logger = LoggerFactory.getLogger(InventarioController.class);

    private final InventarioService inventarioService;

    @Autowired
    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /**
     * Crea un nuevo registro de inventario.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad inicial
     * @param cantidadMinima Cantidad mínima (opcional)
     * @param cantidadMaxima Cantidad máxima (opcional)
     * @return Inventario creado en formato JSON API
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Crear inventario", description = "Crea un nuevo registro de inventario para un producto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Inventario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Ya existe inventario para el producto")
    })
    public ResponseEntity<InventarioResponse> crearInventario(
            @Parameter(description = "ID del producto", required = true)
            @RequestParam Long productoId,
            @Parameter(description = "Cantidad inicial", required = true)
            @RequestParam Integer cantidad,
            @Parameter(description = "Cantidad mínima")
            @RequestParam(required = false) Integer cantidadMinima,
            @Parameter(description = "Cantidad máxima")
            @RequestParam(required = false) Integer cantidadMaxima) {
        
        logger.info("Recibida solicitud para crear inventario - producto ID: {}, cantidad: {}", productoId, cantidad);
        
        try {
            Inventario inventario = inventarioService.crearInventario(productoId, cantidad, cantidadMinima, cantidadMaxima);
            InventarioResponse response = InventarioResponse.single(inventario);
            
            logger.info("Inventario creado exitosamente con ID: {}", inventario.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear inventario: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene el inventario de un producto por su ID.
     * 
     * @param productoId ID del producto
     * @return Inventario en formato JSON API
     */
    @GetMapping(value = "/producto/{productoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener inventario por producto", description = "Recupera el inventario de un producto específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventario encontrado",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<InventarioResponse> obtenerInventarioPorProducto(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId) {
        
        logger.debug("Recibida solicitud para obtener inventario del producto ID: {}", productoId);
        
        return inventarioService.obtenerInventarioPorProductoId(productoId)
                .map(inventario -> {
                    InventarioResponse response = InventarioResponse.single(inventario);
                    logger.debug("Inventario encontrado para producto ID: {}", productoId);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Inventario no encontrado para producto ID: {}", productoId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Obtiene un inventario por su ID.
     * 
     * @param id ID del inventario
     * @return Inventario en formato JSON API
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener inventario por ID", description = "Recupera un inventario específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventario encontrado",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<InventarioResponse> obtenerInventarioPorId(
            @Parameter(description = "ID del inventario", required = true)
            @PathVariable Long id) {
        
        logger.debug("Recibida solicitud para obtener inventario con ID: {}", id);
        
        return inventarioService.obtenerInventarioPorId(id)
                .map(inventario -> {
                    InventarioResponse response = InventarioResponse.single(inventario);
                    logger.debug("Inventario encontrado con ID: {}", id);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Inventario no encontrado con ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Obtiene todos los inventarios con paginación.
     * 
     * @param page Número de página (opcional, por defecto 0)
     * @param size Tamaño de página (opcional, por defecto 10)
     * @return Lista paginada de inventarios en formato JSON API
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar inventarios", description = "Obtiene una lista paginada de inventarios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de inventarios obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class)))
    })
    public ResponseEntity<InventarioResponse> obtenerInventarios(
            @Parameter(description = "Número de página (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página")
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Recibida solicitud para obtener inventarios - página: {}, tamaño: {}", page, size);
        
        Page<Inventario> inventariosPage = inventarioService.obtenerInventarios(page, size);
        InventarioResponse response = InventarioResponse.list(
                inventariosPage.getContent(),
                (int) inventariosPage.getTotalElements(),
                page,
                size
        );
        
        logger.debug("Retornando {} inventarios de un total de {}", 
                    inventariosPage.getContent().size(), inventariosPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene inventarios con stock bajo.
     * 
     * @return Lista de inventarios con stock bajo
     */
    @GetMapping(value = "/stock-bajo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener inventarios con stock bajo", description = "Obtiene inventarios que están en o por debajo de su cantidad mínima")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de inventarios con stock bajo obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class)))
    })
    public ResponseEntity<InventarioResponse> obtenerInventariosConStockBajo() {
        logger.debug("Recibida solicitud para obtener inventarios con stock bajo");
        
        List<Inventario> inventarios = inventarioService.obtenerInventariosConStockBajo();
        InventarioResponse response = InventarioResponse.list(inventarios, inventarios.size(), 0, inventarios.size());
        
        logger.debug("Retornando {} inventarios con stock bajo", inventarios.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza la cantidad de un inventario.
     * 
     * @param productoId ID del producto
     * @param nuevaCantidad Nueva cantidad
     * @return Inventario actualizado en formato JSON API
     */
    @PutMapping(value = "/producto/{productoId}/cantidad", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualizar cantidad", description = "Actualiza la cantidad disponible de un producto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cantidad actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<InventarioResponse> actualizarCantidad(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId,
            @Parameter(description = "Nueva cantidad", required = true)
            @RequestParam Integer nuevaCantidad) {
        
        logger.info("Recibida solicitud para actualizar cantidad - producto ID: {}, nueva cantidad: {}", 
                   productoId, nuevaCantidad);
        
        try {
            Inventario inventario = inventarioService.actualizarCantidad(productoId, nuevaCantidad);
            InventarioResponse response = InventarioResponse.single(inventario);
            
            logger.info("Cantidad actualizada exitosamente para producto ID: {}", productoId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error al actualizar cantidad: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Aumenta el stock de un producto.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad a aumentar
     * @return Inventario actualizado en formato JSON API
     */
    @PostMapping(value = "/producto/{productoId}/aumentar-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Aumentar stock", description = "Aumenta el stock disponible de un producto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock aumentado exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<InventarioResponse> aumentarStock(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId,
            @Parameter(description = "Cantidad a aumentar", required = true)
            @RequestParam Integer cantidad) {
        
        logger.info("Recibida solicitud para aumentar stock - producto ID: {}, cantidad: {}", productoId, cantidad);
        
        try {
            Inventario inventario = inventarioService.aumentarStock(productoId, cantidad);
            InventarioResponse response = InventarioResponse.single(inventario);
            
            logger.info("Stock aumentado exitosamente para producto ID: {}", productoId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error al aumentar stock: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Verifica si hay stock suficiente para un producto.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad solicitada
     * @return true si hay stock suficiente, false en caso contrario
     */
    @GetMapping(value = "/producto/{productoId}/verificar-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verificar stock", description = "Verifica si hay stock suficiente para un producto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificación realizada exitosamente")
    })
    public ResponseEntity<Boolean> verificarStock(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId,
            @Parameter(description = "Cantidad solicitada", required = true)
            @RequestParam Integer cantidad) {
        
        logger.debug("Recibida solicitud para verificar stock - producto ID: {}, cantidad solicitada: {}", 
                    productoId, cantidad);
        
        boolean tieneStock = inventarioService.verificarStockSuficiente(productoId, cantidad);
        logger.debug("Producto ID: {} tiene stock suficiente: {}", productoId, tieneStock);
        
        return ResponseEntity.ok(tieneStock);
    }

    /**
     * Obtiene estadísticas del inventario.
     * 
     * @return Estadísticas del inventario
     */
    @GetMapping(value = "/estadisticas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener estadísticas", description = "Obtiene estadísticas generales del inventario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    })
    public ResponseEntity<Object[]> obtenerEstadisticas() {
        logger.debug("Recibida solicitud para obtener estadísticas de inventario");
        
        Object[] estadisticas = inventarioService.obtenerEstadisticas();
        logger.debug("Estadísticas obtenidas: totalInventarios={}, totalStock={}, inventariosConStockBajo={}", 
                    estadisticas[0], estadisticas[1], estadisticas[2]);
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Desactiva un inventario.
     * 
     * @param productoId ID del producto
     * @return Respuesta sin contenido
     */
    @DeleteMapping(value = "/producto/{productoId}")
    @Operation(summary = "Desactivar inventario", description = "Desactiva el inventario de un producto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Inventario desactivado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<Void> desactivarInventario(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId) {
        
        logger.info("Recibida solicitud para desactivar inventario del producto ID: {}", productoId);
        
        try {
            inventarioService.desactivarInventario(productoId);
            logger.info("Inventario desactivado exitosamente para producto ID: {}", productoId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error al desactivar inventario: {}", e.getMessage());
            throw e;
        }
    }
}
