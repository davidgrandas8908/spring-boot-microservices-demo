package com.microservices.inventario.controller;

import com.microservices.inventario.dto.CompraRequest;
import com.microservices.inventario.dto.CompraResponse;
import com.microservices.inventario.entity.Compra;
import com.microservices.inventario.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para la gestión de compras.
 * 
 * Expone endpoints para operaciones de compra siguiendo
 * el estándar JSON API.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/compras")
@Tag(name = "Compras", description = "API para gestión de compras")
public class CompraController {

    private static final Logger logger = LoggerFactory.getLogger(CompraController.class);

    private final CompraService compraService;

    @Autowired
    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    /**
     * Procesa una compra de productos.
     * 
     * @param request Solicitud de compra en formato JSON API
     * @return Compra procesada en formato JSON API
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Procesar compra", description = "Procesa una compra de productos, verificando stock y actualizando inventario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Compra procesada exitosamente",
                    content = @Content(schema = @Schema(implementation = CompraResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    })
    public ResponseEntity<CompraResponse> procesarCompra(
            @Parameter(description = "Solicitud de compra", required = true)
            @Valid @RequestBody CompraRequest request) {
        
        logger.info("Recibida solicitud para procesar compra - producto ID: {}, cantidad: {}", 
                   request.getData().getAttributes().getProductoId(), 
                   request.getData().getAttributes().getCantidad());
        
        try {
            Compra compra = compraService.procesarCompra(request);
            CompraResponse response = CompraResponse.single(compra);
            
            logger.info("Compra procesada exitosamente con ID: {}", compra.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error al procesar compra: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene una compra por su ID.
     * 
     * @param id ID de la compra
     * @return Compra en formato JSON API
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener compra por ID", description = "Recupera una compra específica por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Compra encontrada",
                    content = @Content(schema = @Schema(implementation = CompraResponse.class))),
        @ApiResponse(responseCode = "404", description = "Compra no encontrada")
    })
    public ResponseEntity<CompraResponse> obtenerCompraPorId(
            @Parameter(description = "ID de la compra", required = true)
            @PathVariable Long id) {
        
        logger.debug("Recibida solicitud para obtener compra con ID: {}", id);
        
        return compraService.obtenerCompraPorId(id)
                .map(compra -> {
                    CompraResponse response = CompraResponse.single(compra);
                    logger.debug("Compra encontrada con ID: {}", id);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Compra no encontrada con ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Obtiene compras por ID del producto.
     * 
     * @param productoId ID del producto
     * @param page Número de página (opcional, por defecto 0)
     * @param size Tamaño de página (opcional, por defecto 10)
     * @return Lista paginada de compras en formato JSON API
     */
    @GetMapping(value = "/producto/{productoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener compras por producto", description = "Obtiene todas las compras de un producto específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de compras obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = CompraResponse.class)))
    })
    public ResponseEntity<CompraResponse> obtenerComprasPorProducto(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId,
            @Parameter(description = "Número de página (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página")
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Recibida solicitud para obtener compras del producto ID: {} - página: {}, tamaño: {}", 
                    productoId, page, size);
        
        Page<Compra> comprasPage = compraService.obtenerComprasPorProducto(productoId, page, size);
        CompraResponse response = CompraResponse.list(
                comprasPage.getContent(),
                (int) comprasPage.getTotalElements(),
                page,
                size
        );
        
        logger.debug("Retornando {} compras del producto ID: {} de un total de {}", 
                    comprasPage.getContent().size(), productoId, comprasPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todas las compras con paginación.
     * 
     * @param page Número de página (opcional, por defecto 0)
     * @param size Tamaño de página (opcional, por defecto 10)
     * @return Lista paginada de compras en formato JSON API
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar compras", description = "Obtiene una lista paginada de todas las compras")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de compras obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = CompraResponse.class)))
    })
    public ResponseEntity<CompraResponse> obtenerCompras(
            @Parameter(description = "Número de página (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página")
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Recibida solicitud para obtener compras - página: {}, tamaño: {}", page, size);
        
        Page<Compra> comprasPage = compraService.obtenerCompras(page, size);
        CompraResponse response = CompraResponse.list(
                comprasPage.getContent(),
                (int) comprasPage.getTotalElements(),
                page,
                size
        );
        
        logger.debug("Retornando {} compras de un total de {}", 
                    comprasPage.getContent().size(), comprasPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene compras por estado.
     * 
     * @param estado Estado de la compra
     * @param page Número de página (opcional, por defecto 0)
     * @param size Tamaño de página (opcional, por defecto 10)
     * @return Lista paginada de compras en formato JSON API
     */
    @GetMapping(value = "/estado/{estado}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener compras por estado", description = "Obtiene compras filtradas por estado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de compras obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = CompraResponse.class)))
    })
    public ResponseEntity<CompraResponse> obtenerComprasPorEstado(
            @Parameter(description = "Estado de la compra", required = true)
            @PathVariable Compra.EstadoCompra estado,
            @Parameter(description = "Número de página (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página")
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Recibida solicitud para obtener compras con estado: {} - página: {}, tamaño: {}", 
                    estado, page, size);
        
        Page<Compra> comprasPage = compraService.obtenerComprasPorEstado(estado, page, size);
        CompraResponse response = CompraResponse.list(
                comprasPage.getContent(),
                (int) comprasPage.getTotalElements(),
                page,
                size
        );
        
        logger.debug("Retornando {} compras con estado {} de un total de {}", 
                    comprasPage.getContent().size(), estado, comprasPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene las compras más recientes.
     * 
     * @param limit Número máximo de compras a retornar (opcional, por defecto 10)
     * @return Lista de compras más recientes
     */
    @GetMapping(value = "/recientes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener compras recientes", description = "Obtiene las compras más recientes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de compras recientes obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = CompraResponse.class)))
    })
    public ResponseEntity<CompraResponse> obtenerComprasRecientes(
            @Parameter(description = "Número máximo de compras")
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.debug("Recibida solicitud para obtener {} compras más recientes", limit);
        
        List<Compra> compras = compraService.obtenerComprasRecientes(limit);
        CompraResponse response = CompraResponse.list(compras, compras.size(), 0, compras.size());
        
        logger.debug("Retornando {} compras más recientes", compras.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cancela una compra.
     * 
     * @param id ID de la compra
     * @return Compra cancelada en formato JSON API
     */
    @PutMapping(value = "/{id}/cancelar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancelar compra", description = "Cancela una compra y restaura el stock si es necesario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Compra cancelada exitosamente",
                    content = @Content(schema = @Schema(implementation = CompraResponse.class))),
        @ApiResponse(responseCode = "400", description = "La compra ya está cancelada"),
        @ApiResponse(responseCode = "404", description = "Compra no encontrada")
    })
    public ResponseEntity<CompraResponse> cancelarCompra(
            @Parameter(description = "ID de la compra", required = true)
            @PathVariable Long id) {
        
        logger.info("Recibida solicitud para cancelar compra con ID: {}", id);
        
        try {
            Compra compra = compraService.cancelarCompra(id);
            CompraResponse response = CompraResponse.single(compra);
            
            logger.info("Compra cancelada exitosamente con ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error al cancelar compra: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene estadísticas de compras.
     * 
     * @return Estadísticas de compras
     */
    @GetMapping(value = "/estadisticas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener estadísticas de compras", description = "Obtiene estadísticas generales de compras")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    })
    public ResponseEntity<Object[]> obtenerEstadisticas() {
        logger.debug("Recibida solicitud para obtener estadísticas de compras");
        
        Object[] estadisticas = compraService.obtenerEstadisticas();
        logger.debug("Estadísticas obtenidas: totalCompras={}, totalVentas={}, comprasCompletadas={}, comprasCanceladas={}", 
                    estadisticas[0], estadisticas[1], estadisticas[2], estadisticas[3]);
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene el total de ventas por producto.
     * 
     * @param productoId ID del producto
     * @return Total de ventas del producto
     */
    @GetMapping(value = "/producto/{productoId}/total-ventas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener total de ventas por producto", description = "Obtiene el total de ventas de un producto específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total de ventas obtenido exitosamente")
    })
    public ResponseEntity<BigDecimal> obtenerTotalVentasPorProducto(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId) {
        
        logger.debug("Recibida solicitud para obtener total de ventas del producto ID: {}", productoId);
        
        BigDecimal totalVentas = compraService.obtenerTotalVentasPorProducto(productoId);
        logger.debug("Total de ventas del producto ID: {} = {}", productoId, totalVentas);
        
        return ResponseEntity.ok(totalVentas);
    }

    /**
     * Verifica si una compra puede ser procesada.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad solicitada
     * @return true si la compra puede ser procesada, false en caso contrario
     */
    @GetMapping(value = "/verificar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verificar si se puede procesar compra", description = "Verifica si una compra puede ser procesada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificación realizada exitosamente")
    })
    public ResponseEntity<Boolean> verificarCompra(
            @Parameter(description = "ID del producto", required = true)
            @RequestParam Long productoId,
            @Parameter(description = "Cantidad solicitada", required = true)
            @RequestParam Integer cantidad) {
        
        logger.debug("Recibida solicitud para verificar compra - producto ID: {}, cantidad: {}", productoId, cantidad);
        
        boolean puedeProcesar = compraService.puedeProcesarCompra(productoId, cantidad);
        logger.debug("Compra para producto ID: {} puede ser procesada: {}", productoId, puedeProcesar);
        
        return ResponseEntity.ok(puedeProcesar);
    }
}
