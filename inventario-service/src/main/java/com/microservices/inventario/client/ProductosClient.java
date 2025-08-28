package com.microservices.inventario.client;

import com.microservices.inventario.dto.ProductoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente Feign para comunicación con el microservicio de productos.
 * 
 * Este cliente permite realizar llamadas HTTP al microservicio de productos
 * de forma declarativa y con manejo automático de errores.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@FeignClient(
    name = "productos-service",
    url = "${productos.service.url}",
    configuration = ProductosClientConfig.class
)
public interface ProductosClient {

    /**
     * Obtiene un producto por su ID.
     * 
     * @param id ID del producto
     * @param apiKey API key para autenticación
     * @return Respuesta con la información del producto
     */
    @GetMapping("/api/v1/productos/{id}")
    ResponseEntity<ProductoResponse> obtenerProductoPorId(
            @PathVariable("id") Long id,
            @RequestHeader("X-API-Key") String apiKey
    );

    /**
     * Verifica si un producto existe.
     * 
     * @param id ID del producto
     * @param apiKey API key para autenticación
     * @return true si existe, false en caso contrario
     */
    @GetMapping("/api/v1/productos/{id}/existe")
    ResponseEntity<Boolean> existeProducto(
            @PathVariable("id") Long id,
            @RequestHeader("X-API-Key") String apiKey
    );
}
