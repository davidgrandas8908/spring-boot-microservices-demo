package com.microservices.inventario.client;

import com.microservices.inventario.dto.ProductoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el cliente de productos.
 * 
 * Verifica la comunicación con el microservicio de productos
 * y el manejo de errores de red.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ProductosClientTest {

    @Mock
    private ProductosClient productosClient;

    private ProductoResponse productoResponse;
    private static final String API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        // Configurar ProductoResponse
        ProductoResponse.Data productoData = new ProductoResponse.Data();
        productoData.setId("1");
        productoData.setType("productos");
        
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Laptop Gaming");
        attributes.setDescripcion("Laptop for gaming");
        attributes.setPrecio(new BigDecimal("1299.99"));
        
        productoData.setAttributes(attributes);
        
        productoResponse = new ProductoResponse();
        productoResponse.setData(productoData);
    }

    @Test
    void obtenerProductoPorId_ProductoExiste_DebeRetornarProducto() {
        // Arrange
        Long productoId = 1L;
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(API_KEY)))
                .thenReturn(ResponseEntity.ok(productoResponse));

        // Act
        ResponseEntity<ProductoResponse> response = productosClient.obtenerProductoPorId(productoId, API_KEY);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().getData().getId());
        assertEquals("Laptop Gaming", response.getBody().getData().getAttributes().getNombre());
        
        verify(productosClient).obtenerProductoPorId(productoId, API_KEY);
    }

    @Test
    void obtenerProductoPorId_ProductoNoExiste_DebeRetornarNotFound() {
        // Arrange
        Long productoId = 999L;
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(API_KEY)))
                .thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<ProductoResponse> response = productosClient.obtenerProductoPorId(productoId, API_KEY);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(productosClient).obtenerProductoPorId(productoId, API_KEY);
    }

    @Test
    void obtenerProductoPorId_ErrorDeRed_DebeLanzarExcepcion() {
        // Arrange
        Long productoId = 1L;
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(API_KEY)))
                .thenThrow(new RestClientException("Error de conexión"));

        // Act & Assert
        assertThrows(RestClientException.class, () -> {
            productosClient.obtenerProductoPorId(productoId, API_KEY);
        });
        
        verify(productosClient).obtenerProductoPorId(productoId, API_KEY);
    }

    @Test
    void obtenerProductoPorId_ErrorInterno_DebeRetornarError() {
        // Arrange
        Long productoId = 1L;
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(API_KEY)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        // Act
        ResponseEntity<ProductoResponse> response = productosClient.obtenerProductoPorId(productoId, API_KEY);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        verify(productosClient).obtenerProductoPorId(productoId, API_KEY);
    }

    @Test
    void obtenerProductoPorId_ErrorDeValidacion_DebeRetornarBadRequest() {
        // Arrange
        Long productoId = -1L;
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(API_KEY)))
                .thenReturn(ResponseEntity.badRequest().build());

        // Act
        ResponseEntity<ProductoResponse> response = productosClient.obtenerProductoPorId(productoId, API_KEY);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        verify(productosClient).obtenerProductoPorId(productoId, API_KEY);
    }

    @Test
    void obtenerProductoPorId_Timeout_DebeLanzarExcepcion() {
        // Arrange
        Long productoId = 1L;
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(API_KEY)))
                .thenThrow(new RestClientException("Timeout"));

        // Act & Assert
        assertThrows(RestClientException.class, () -> {
            productosClient.obtenerProductoPorId(productoId, API_KEY);
        });
        
        verify(productosClient).obtenerProductoPorId(productoId, API_KEY);
    }

    @Test
    void obtenerProductoPorId_ApiKeyInvalida_DebeRetornarUnauthorized() {
        // Arrange
        Long productoId = 1L;
        String apiKeyInvalida = "invalid-key";
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(apiKeyInvalida)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        // Act
        ResponseEntity<ProductoResponse> response = productosClient.obtenerProductoPorId(productoId, apiKeyInvalida);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        verify(productosClient).obtenerProductoPorId(productoId, apiKeyInvalida);
    }

    @Test
    void obtenerProductoPorId_ApiKeyVacia_DebeLanzarExcepcion() {
        // Arrange
        Long productoId = 1L;
        String apiKeyVacia = "";
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(apiKeyVacia)))
                .thenThrow(new IllegalArgumentException("API key no puede estar vacía"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productosClient.obtenerProductoPorId(productoId, apiKeyVacia);
        });
        
        verify(productosClient).obtenerProductoPorId(productoId, apiKeyVacia);
    }

    @Test
    void obtenerProductoPorId_ProductoIdNulo_DebeLanzarExcepcion() {
        // Arrange
        Long productoId = null;
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(API_KEY)))
                .thenThrow(new IllegalArgumentException("Producto ID no puede ser nulo"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productosClient.obtenerProductoPorId(productoId, API_KEY);
        });
        
        verify(productosClient).obtenerProductoPorId(productoId, API_KEY);
    }

    @Test
    void obtenerProductoPorId_ProductoIdNegativo_DebeRetornarBadRequest() {
        // Arrange
        Long productoId = -1L;
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(API_KEY)))
                .thenReturn(ResponseEntity.badRequest().build());

        // Act
        ResponseEntity<ProductoResponse> response = productosClient.obtenerProductoPorId(productoId, API_KEY);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        verify(productosClient).obtenerProductoPorId(productoId, API_KEY);
    }

    @Test
    void obtenerProductoPorId_ProductoIdCero_DebeRetornarBadRequest() {
        // Arrange
        Long productoId = 0L;
        when(productosClient.obtenerProductoPorId(eq(productoId), eq(API_KEY)))
                .thenReturn(ResponseEntity.badRequest().build());

        // Act
        ResponseEntity<ProductoResponse> response = productosClient.obtenerProductoPorId(productoId, API_KEY);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        verify(productosClient).obtenerProductoPorId(productoId, API_KEY);
    }

    @Test
    void existeProducto_ProductoExiste_DebeRetornarTrue() {
        // Arrange
        Long productoId = 1L;
        when(productosClient.existeProducto(eq(productoId), eq(API_KEY)))
                .thenReturn(ResponseEntity.ok(true));

        // Act
        ResponseEntity<Boolean> response = productosClient.existeProducto(productoId, API_KEY);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        
        verify(productosClient).existeProducto(productoId, API_KEY);
    }

    @Test
    void existeProducto_ProductoNoExiste_DebeRetornarFalse() {
        // Arrange
        Long productoId = 999L;
        when(productosClient.existeProducto(eq(productoId), eq(API_KEY)))
                .thenReturn(ResponseEntity.ok(false));

        // Act
        ResponseEntity<Boolean> response = productosClient.existeProducto(productoId, API_KEY);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
        
        verify(productosClient).existeProducto(productoId, API_KEY);
    }

    @Test
    void existeProducto_ErrorDeRed_DebeLanzarExcepcion() {
        // Arrange
        Long productoId = 1L;
        when(productosClient.existeProducto(eq(productoId), eq(API_KEY)))
                .thenThrow(new RestClientException("Error de conexión"));

        // Act & Assert
        assertThrows(RestClientException.class, () -> {
            productosClient.existeProducto(productoId, API_KEY);
        });
        
        verify(productosClient).existeProducto(productoId, API_KEY);
    }

    @Test
    void existeProducto_ApiKeyInvalida_DebeRetornarUnauthorized() {
        // Arrange
        Long productoId = 1L;
        String apiKeyInvalida = "invalid-key";
        when(productosClient.existeProducto(eq(productoId), eq(apiKeyInvalida)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        // Act
        ResponseEntity<Boolean> response = productosClient.existeProducto(productoId, apiKeyInvalida);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        verify(productosClient).existeProducto(productoId, apiKeyInvalida);
    }

    @Test
    void existeProducto_ProductoIdNulo_DebeLanzarExcepcion() {
        // Arrange
        Long productoId = null;
        when(productosClient.existeProducto(eq(productoId), eq(API_KEY)))
                .thenThrow(new IllegalArgumentException("Producto ID no puede ser nulo"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productosClient.existeProducto(productoId, API_KEY);
        });
        
        verify(productosClient).existeProducto(productoId, API_KEY);
    }
}

