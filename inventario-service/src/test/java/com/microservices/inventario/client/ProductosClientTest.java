package com.microservices.inventario.client;

import com.microservices.inventario.dto.ProductoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductosClient.
 * 
 * Cubre todos los métodos del cliente con casos exitosos,
 * errores y timeouts para asegurar cobertura del 90%+.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductosClient Tests")
class ProductosClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductosClient productosClient;

    private ProductoResponse productoResponse;

    @BeforeEach
    void setUp() {
        // Configurar ProductoResponse
        productoResponse = new ProductoResponse();
        ProductoResponse.Data productoData = new ProductoResponse.Data();
        productoData.setId("1");
        productoData.setType("productos");
        ProductoResponse.Attributes productoAttributes = new ProductoResponse.Attributes();
        productoAttributes.setNombre("Producto Test");
        productoAttributes.setPrecio(new BigDecimal("10.00"));
        productoAttributes.setDescripcion("Descripción del producto");
        productoData.setAttributes(productoAttributes);
        productoResponse.setData(productoData);
    }

    @Test
    @DisplayName("Debería obtener producto exitosamente")
    void deberiaObtenerProductoExitosamente() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(productoResponse));

        // When
        ProductoResponse resultado = productosClient.obtenerProducto(1L);

        // Then
        assertNotNull(resultado);
        assertEquals("1", resultado.getData().getId());
        assertEquals("productos", resultado.getData().getType());
        assertEquals("Producto Test", resultado.getData().getAttributes().getNombre());
        assertEquals(new BigDecimal("10.00"), resultado.getData().getAttributes().getPrecio());
        
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando producto no existe")
    void deberiaLanzarExcepcionCuandoProductoNoExiste() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.notFound().build());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(999L));
        
        assertEquals("Producto no encontrado con ID: 999", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando hay error de servidor")
    void deberiaLanzarExcepcionCuandoHayErrorDeServidor() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.internalServerError().build());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Error al obtener producto con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando hay error de red")
    void deberiaLanzarExcepcionCuandoHayErrorDeRed() {
        // Given
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenThrow(new RuntimeException("Connection timeout"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Error al obtener producto con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con datos nulos")
    void deberiaManejarRespuestaConDatosNulos() {
        // Given
        ProductoResponse responseNulo = new ProductoResponse();
        responseNulo.setData(null);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseNulo));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con atributos nulos")
    void deberiaManejarRespuestaConAtributosNulos() {
        // Given
        ProductoResponse responseSinAtributos = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType("productos");
        data.setAttributes(null);
        responseSinAtributos.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseSinAtributos));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con precio nulo")
    void deberiaManejarRespuestaConPrecioNulo() {
        // Given
        ProductoResponse responseSinPrecio = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType("productos");
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Producto Test");
        attributes.setPrecio(null);
        attributes.setDescripcion("Descripción del producto");
        data.setAttributes(attributes);
        responseSinPrecio.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseSinPrecio));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con nombre nulo")
    void deberiaManejarRespuestaConNombreNulo() {
        // Given
        ProductoResponse responseSinNombre = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType("productos");
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre(null);
        attributes.setPrecio(new BigDecimal("10.00"));
        attributes.setDescripcion("Descripción del producto");
        data.setAttributes(attributes);
        responseSinNombre.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseSinNombre));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con ID nulo")
    void deberiaManejarRespuestaConIdNulo() {
        // Given
        ProductoResponse responseSinId = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId(null);
        data.setType("productos");
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Producto Test");
        attributes.setPrecio(new BigDecimal("10.00"));
        attributes.setDescripcion("Descripción del producto");
        data.setAttributes(attributes);
        responseSinId.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseSinId));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con tipo incorrecto")
    void deberiaManejarRespuestaConTipoIncorrecto() {
        // Given
        ProductoResponse responseTipoIncorrecto = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType("categoria"); // Tipo incorrecto
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Producto Test");
        attributes.setPrecio(new BigDecimal("10.00"));
        attributes.setDescripcion("Descripción del producto");
        data.setAttributes(attributes);
        responseTipoIncorrecto.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseTipoIncorrecto));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con precio negativo")
    void deberiaManejarRespuestaConPrecioNegativo() {
        // Given
        ProductoResponse responsePrecioNegativo = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType("productos");
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Producto Test");
        attributes.setPrecio(new BigDecimal("-10.00")); // Precio negativo
        attributes.setDescripcion("Descripción del producto");
        data.setAttributes(attributes);
        responsePrecioNegativo.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responsePrecioNegativo));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con precio cero")
    void deberiaManejarRespuestaConPrecioCero() {
        // Given
        ProductoResponse responsePrecioCero = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType("productos");
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Producto Test");
        attributes.setPrecio(BigDecimal.ZERO); // Precio cero
        attributes.setDescripcion("Descripción del producto");
        data.setAttributes(attributes);
        responsePrecioCero.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responsePrecioCero));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con nombre vacío")
    void deberiaManejarRespuestaConNombreVacio() {
        // Given
        ProductoResponse responseNombreVacio = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType("productos");
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre(""); // Nombre vacío
        attributes.setPrecio(new BigDecimal("10.00"));
        attributes.setDescripcion("Descripción del producto");
        data.setAttributes(attributes);
        responseNombreVacio.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseNombreVacio));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con ID vacío")
    void deberiaManejarRespuestaConIdVacio() {
        // Given
        ProductoResponse responseIdVacio = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId(""); // ID vacío
        data.setType("productos");
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Producto Test");
        attributes.setPrecio(new BigDecimal("10.00"));
        attributes.setDescripcion("Descripción del producto");
        data.setAttributes(attributes);
        responseIdVacio.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseIdVacio));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con tipo vacío")
    void deberiaManejarRespuestaConTipoVacio() {
        // Given
        ProductoResponse responseTipoVacio = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType(""); // Tipo vacío
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Producto Test");
        attributes.setPrecio(new BigDecimal("10.00"));
        attributes.setDescripcion("Descripción del producto");
        data.setAttributes(attributes);
        responseTipoVacio.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseTipoVacio));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con descripción nula")
    void deberiaManejarRespuestaConDescripcionNula() {
        // Given
        ProductoResponse responseSinDescripcion = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType("productos");
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Producto Test");
        attributes.setPrecio(new BigDecimal("10.00"));
        attributes.setDescripcion(null); // Descripción nula
        data.setAttributes(attributes);
        responseSinDescripcion.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseSinDescripcion));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }

    @Test
    @DisplayName("Debería manejar respuesta con descripción vacía")
    void deberiaManejarRespuestaConDescripcionVacia() {
        // Given
        ProductoResponse responseDescripcionVacia = new ProductoResponse();
        ProductoResponse.Data data = new ProductoResponse.Data();
        data.setId("1");
        data.setType("productos");
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Producto Test");
        attributes.setPrecio(new BigDecimal("10.00"));
        attributes.setDescripcion(""); // Descripción vacía
        data.setAttributes(attributes);
        responseDescripcionVacia.setData(data);
        
        when(restTemplate.getForEntity(anyString(), eq(ProductoResponse.class)))
            .thenReturn(ResponseEntity.ok(responseDescripcionVacia));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> productosClient.obtenerProducto(1L));
        
        assertEquals("Producto no encontrado con ID: 1", exception.getMessage());
        verify(restTemplate).getForEntity(anyString(), eq(ProductoResponse.class));
    }
}

