package com.microservices.inventario.service;

import com.microservices.inventario.client.ProductosClient;
import com.microservices.inventario.dto.ProductoResponse;
import com.microservices.inventario.entity.Inventario;
import com.microservices.inventario.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el servicio de inventario.
 * 
 * Verifica la lógica de negocio del servicio de inventario,
 * incluyendo operaciones CRUD y validaciones.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductosClient productosClient;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventario;
    private ProductoResponse productoResponse;

    @BeforeEach
    void setUp() {
        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProductoId(1L);
        inventario.setCantidad(100);
        inventario.setCantidadMinima(10);
        inventario.setCantidadMaxima(1000);
        inventario.setActivo(true);

        // Configurar ProductoResponse
        ProductoResponse.Data productoData = new ProductoResponse.Data();
        productoData.setId("1");
        productoData.setType("productos");
        
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Laptop Gaming");
        attributes.setDescripcion("Laptop para gaming");
        attributes.setPrecio(new BigDecimal("1299.99"));
        
        productoData.setAttributes(attributes);
        
        productoResponse = new ProductoResponse();
        productoResponse.setData(productoData);
    }

    @Test
    void crearInventario_DebeCrearInventarioExitosamente() {
        // Arrange
        Long productoId = 1L;
        Integer cantidad = 100;
        Integer cantidadMinima = 10;
        Integer cantidadMaxima = 1000;

        when(productosClient.obtenerProductoPorId(eq(productoId), anyString()))
                .thenReturn(ResponseEntity.ok(productoResponse));
        when(inventarioRepository.existsByProductoId(productoId)).thenReturn(false);
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // Act
        Inventario resultado = inventarioService.crearInventario(productoId, cantidad, cantidadMinima, cantidadMaxima);

        // Assert
        assertNotNull(resultado);
        assertEquals(productoId, resultado.getProductoId());
        assertEquals(cantidad, resultado.getCantidad());
        assertEquals(cantidadMinima, resultado.getCantidadMinima());
        assertEquals(cantidadMaxima, resultado.getCantidadMaxima());
        
        verify(productosClient).obtenerProductoPorId(eq(productoId), anyString());
        verify(inventarioRepository).existsByProductoId(productoId);
        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    void crearInventario_ProductoNoExiste_DebeLanzarExcepcion() {
        // Arrange
        Long productoId = 999L;
        Integer cantidad = 100;

        when(productosClient.obtenerProductoPorId(eq(productoId), anyString()))
                .thenThrow(new RuntimeException("Producto no encontrado"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            inventarioService.crearInventario(productoId, cantidad, 10, 1000);
        });

        verify(productosClient).obtenerProductoPorId(eq(productoId), anyString());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_InventarioYaExiste_DebeLanzarExcepcion() {
        // Arrange
        Long productoId = 1L;
        Integer cantidad = 100;

        when(productosClient.obtenerProductoPorId(eq(productoId), anyString()))
                .thenReturn(ResponseEntity.ok(productoResponse));
        when(inventarioRepository.existsByProductoId(productoId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.crearInventario(productoId, cantidad, 10, 1000);
        });

        assertEquals("Ya existe inventario para el producto con ID: " + productoId, exception.getMessage());
        
        verify(productosClient).obtenerProductoPorId(eq(productoId), anyString());
        verify(inventarioRepository).existsByProductoId(productoId);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void obtenerInventarioPorProductoId_DebeRetornarInventario() {
        // Arrange
        Long productoId = 1L;
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventario));

        // Act
        Optional<Inventario> resultado = inventarioService.obtenerInventarioPorProductoId(productoId);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(inventario, resultado.get());
        verify(inventarioRepository).findByProductoId(productoId);
    }

    @Test
    void obtenerInventarioPorProductoId_NoExiste_DebeRetornarVacio() {
        // Arrange
        Long productoId = 999L;
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.empty());

        // Act
        Optional<Inventario> resultado = inventarioService.obtenerInventarioPorProductoId(productoId);

        // Assert
        assertFalse(resultado.isPresent());
        verify(inventarioRepository).findByProductoId(productoId);
    }

    @Test
    void obtenerInventarioPorId_DebeRetornarInventario() {
        // Arrange
        Long id = 1L;
        when(inventarioRepository.findById(id)).thenReturn(Optional.of(inventario));

        // Act
        Optional<Inventario> resultado = inventarioService.obtenerInventarioPorId(id);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(inventario, resultado.get());
        verify(inventarioRepository).findById(id);
    }

    @Test
    void obtenerInventarios_DebeRetornarListaPaginada() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Inventario> inventarios = Arrays.asList(inventario);
        Page<Inventario> page = new PageImpl<>(inventarios, pageable, 1);
        
        when(inventarioRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Inventario> resultado = inventarioService.obtenerInventarios(0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals(inventario, resultado.getContent().get(0));
        verify(inventarioRepository).findAll(pageable);
    }

    @Test
    void actualizarCantidad_DebeActualizarCantidadExitosamente() {
        // Arrange
        Long productoId = 1L;
        Integer nuevaCantidad = 150;
        
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // Act
        Inventario resultado = inventarioService.actualizarCantidad(productoId, nuevaCantidad);

        // Assert
        assertNotNull(resultado);
        assertEquals(nuevaCantidad, inventario.getCantidad());
        verify(inventarioRepository).findByProductoId(productoId);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void actualizarCantidad_InventarioNoExiste_DebeLanzarExcepcion() {
        // Arrange
        Long productoId = 999L;
        Integer nuevaCantidad = 150;
        
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.actualizarCantidad(productoId, nuevaCantidad);
        });

        verify(inventarioRepository).findByProductoId(productoId);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void aumentarStock_DebeAumentarStockExitosamente() {
        // Arrange
        Long productoId = 1L;
        Integer cantidadAumentar = 50;
        Integer cantidadInicial = inventario.getCantidad();
        
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // Act
        Inventario resultado = inventarioService.aumentarStock(productoId, cantidadAumentar);

        // Assert
        assertNotNull(resultado);
        assertEquals(cantidadInicial + cantidadAumentar, inventario.getCantidad());
        verify(inventarioRepository).findByProductoId(productoId);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void verificarStockSuficiente_StockSuficiente_DebeRetornarTrue() {
        // Arrange
        Long productoId = 1L;
        Integer cantidadSolicitada = 50;
        
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventario));

        // Act
        boolean resultado = inventarioService.verificarStockSuficiente(productoId, cantidadSolicitada);

        // Assert
        assertTrue(resultado);
        verify(inventarioRepository).findByProductoId(productoId);
    }

    @Test
    void verificarStockSuficiente_StockInsuficiente_DebeRetornarFalse() {
        // Arrange
        Long productoId = 1L;
        Integer cantidadSolicitada = 150; // Más que el stock disponible (100)
        
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventario));

        // Act
        boolean resultado = inventarioService.verificarStockSuficiente(productoId, cantidadSolicitada);

        // Assert
        assertFalse(resultado);
        verify(inventarioRepository).findByProductoId(productoId);
    }

    @Test
    void obtenerInventariosConStockBajo_DebeRetornarLista() {
        // Arrange
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioRepository.findInventariosConStockBajo()).thenReturn(inventarios);

        // Act
        List<Inventario> resultado = inventarioService.obtenerInventariosConStockBajo();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(inventario, resultado.get(0));
        verify(inventarioRepository).findInventariosConStockBajo();
    }

    @Test
    void desactivarInventario_DebeDesactivarExitosamente() {
        // Arrange
        Long productoId = 1L;
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // Act
        inventarioService.desactivarInventario(productoId);

        // Assert
        assertFalse(inventario.getActivo());
        verify(inventarioRepository).findByProductoId(productoId);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void obtenerEstadisticas_DebeRetornarEstadisticas() {
        // Arrange
        when(inventarioRepository.count()).thenReturn(10L);
        when(inventarioRepository.getStockTotal()).thenReturn(1000L);
        when(inventarioRepository.countInventariosConStockBajo()).thenReturn(3L);

        // Act
        Object[] estadisticas = inventarioService.obtenerEstadisticas();

        // Assert
        assertNotNull(estadisticas);
        assertEquals(3, estadisticas.length);
        assertEquals(10L, estadisticas[0]); // totalInventarios
        assertEquals(1000L, estadisticas[1]); // totalStock
        assertEquals(3L, estadisticas[2]); // inventariosConStockBajo
        
        verify(inventarioRepository).count();
        verify(inventarioRepository).getStockTotal();
        verify(inventarioRepository).countInventariosConStockBajo();
    }
}
