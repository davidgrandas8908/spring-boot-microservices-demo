package com.microservices.productos.controller;

import com.microservices.productos.dto.ProductoRequest;
import com.microservices.productos.dto.ProductoResponse;
import com.microservices.productos.entity.Producto;
import com.microservices.productos.service.ProductoService;
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

import java.util.List;

/**
 * Controlador REST para la gestión de productos.
 * 
 * Expone endpoints para operaciones CRUD de productos siguiendo
 * el estándar JSON API.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "API para gestión de productos")
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Crea un nuevo producto.
     * 
     * @param request DTO con los datos del producto
     * @return Producto creado en formato JSON API
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Producto con nombre duplicado")
    })
    public ResponseEntity<ProductoResponse> crearProducto(
            @Parameter(description = "Datos del producto a crear", required = true)
            @Valid @RequestBody ProductoRequest request) {
        
        logger.info("Recibida solicitud para crear producto: {}", request.getData().getAttributes().getNombre());
        
        try {
            Producto producto = productoService.crearProducto(request);
            ProductoResponse response = ProductoResponse.single(producto);
            
            logger.info("Producto creado exitosamente con ID: {}", producto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear producto: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene un producto por su ID.
     * 
     * @param id ID del producto
     * @return Producto en formato JSON API
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener producto por ID", description = "Recupera un producto específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long id) {
        
        logger.debug("Recibida solicitud para obtener producto con ID: {}", id);
        
        return productoService.obtenerProductoPorId(id)
                .map(producto -> {
                    ProductoResponse response = ProductoResponse.single(producto);
                    logger.debug("Producto encontrado con ID: {}", id);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Producto no encontrado con ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Obtiene todos los productos con paginación.
     * 
     * @param page Número de página (opcional, por defecto 0)
     * @param size Tamaño de página (opcional, por defecto 10)
     * @return Lista paginada de productos en formato JSON API
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar productos", description = "Obtiene una lista paginada de productos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class)))
    })
    public ResponseEntity<ProductoResponse> obtenerProductos(
            @Parameter(description = "Número de página (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página")
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Recibida solicitud para obtener productos - página: {}, tamaño: {}", page, size);
        
        Page<Producto> productosPage = productoService.obtenerProductos(page, size);
        ProductoResponse response = ProductoResponse.list(
                productosPage.getContent(),
                (int) productosPage.getTotalElements(),
                page,
                size
        );
        
        logger.debug("Retornando {} productos de un total de {}", 
                    productosPage.getContent().size(), productosPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Busca productos por nombre.
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de productos que coinciden con el nombre
     */
    @GetMapping(value = "/buscar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Buscar productos por nombre", description = "Busca productos cuyo nombre contenga el término especificado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class)))
    })
    public ResponseEntity<ProductoResponse> buscarProductosPorNombre(
            @Parameter(description = "Término de búsqueda", required = true)
            @RequestParam String nombre) {
        
        logger.debug("Recibida solicitud para buscar productos por nombre: {}", nombre);
        
        List<Producto> productos = productoService.buscarProductosPorNombre(nombre);
        ProductoResponse response = ProductoResponse.list(productos, productos.size(), 0, productos.size());
        
        logger.debug("Encontrados {} productos que coinciden con '{}'", productos.size(), nombre);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id ID del producto a actualizar
     * @param request DTO con los nuevos datos del producto
     * @return Producto actualizado en formato JSON API
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "409", description = "Producto con nombre duplicado")
    })
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @Parameter(description = "ID del producto a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del producto", required = true)
            @Valid @RequestBody ProductoRequest request) {
        
        logger.info("Recibida solicitud para actualizar producto con ID: {}", id);
        
        try {
            Producto producto = productoService.actualizarProducto(id, request);
            ProductoResponse response = ProductoResponse.single(producto);
            
            logger.info("Producto actualizado exitosamente con ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error al actualizar producto con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Elimina un producto por su ID.
     * 
     * @param id ID del producto a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto a eliminar", required = true)
            @PathVariable Long id) {
        
        logger.info("Recibida solicitud para eliminar producto con ID: {}", id);
        
        try {
            productoService.eliminarProducto(id);
            logger.info("Producto eliminado exitosamente con ID: {}", id);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar producto con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Verifica si un producto existe.
     * 
     * @param id ID del producto
     * @return true si existe, false en caso contrario
     */
    @GetMapping(value = "/{id}/existe", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verificar existencia de producto", description = "Verifica si un producto existe en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificación realizada exitosamente")
    })
    public ResponseEntity<Boolean> existeProducto(
            @Parameter(description = "ID del producto a verificar", required = true)
            @PathVariable Long id) {
        
        logger.debug("Recibida solicitud para verificar existencia de producto con ID: {}", id);
        
        boolean existe = productoService.existeProducto(id);
        logger.debug("Producto con ID {} existe: {}", id, existe);
        
        return ResponseEntity.ok(existe);
    }
}
