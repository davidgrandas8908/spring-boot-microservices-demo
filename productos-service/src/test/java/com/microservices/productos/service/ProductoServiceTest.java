package com.microservices.productos.service;

import com.microservices.productos.dto.ProductoRequest;
import com.microservices.productos.entity.Producto;
import com.microservices.productos.repository.ProductoRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el servicio de productos.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoRequest request;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop Gaming");
        producto.setPrecio(new BigDecimal("1299.99"));
        producto.setDescripcion("Laptop para gaming de alto rendimiento");
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaActualizacion(LocalDateTime.now());

        request = new ProductoRequest();
        ProductoRequest.Data data = new ProductoRequest.Data();
        ProductoRequest.Attributes attributes = new ProductoRequest.Attributes();
        attributes.setNombre("Laptop Gaming");
        attributes.setPrecio(new BigDecimal("1299.99"));
        attributes.setDescripcion("Laptop para gaming de alto rendimiento");
        data.setAttributes(attributes);
        data.setType("productos");
        request.setData(data);
    }

    @Test
    void crearProducto_DeberiaCrearProductoExitosamente() {
        // Arrange
        when(productoRepository.existsByNombre("Laptop Gaming")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        Producto resultado = productoService.crearProducto(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Laptop Gaming", resultado.getNombre());
        assertEquals(new BigDecimal("1299.99"), resultado.getPrecio());
        verify(productoRepository).existsByNombre("Laptop Gaming");
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crearProducto_DeberiaLanzarExcepcionSiNombreExiste() {
        // Arrange
        when(productoRepository.existsByNombre("Laptop Gaming")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.crearProducto(request)
        );

        assertEquals("Ya existe un producto con el nombre: Laptop Gaming", exception.getMessage());
        verify(productoRepository).existsByNombre("Laptop Gaming");
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void obtenerProductoPorId_DeberiaRetornarProductoSiExiste() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        Optional<Producto> resultado = productoService.obtenerProductoPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(producto, resultado.get());
        verify(productoRepository).findById(1L);
    }

    @Test
    void obtenerProductoPorId_DeberiaRetornarEmptySiNoExiste() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Producto> resultado = productoService.obtenerProductoPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(productoRepository).findById(999L);
    }

    @Test
    void obtenerProductos_DeberiaRetornarPaginaDeProductos() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        Page<Producto> pagina = new PageImpl<>(productos);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(productoRepository.findAll(pageable)).thenReturn(pagina);

        // Act
        Page<Producto> resultado = productoService.obtenerProductos(0, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals(producto, resultado.getContent().get(0));
        verify(productoRepository).findAll(pageable);
    }

    @Test
    void buscarProductosPorNombre_DeberiaRetornarProductosCoincidentes() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findByNombreContainingIgnoreCase("Laptop")).thenReturn(productos);

        // Act
        List<Producto> resultado = productoService.buscarProductosPorNombre("Laptop");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(producto, resultado.get(0));
        verify(productoRepository).findByNombreContainingIgnoreCase("Laptop");
    }

    @Test
    void actualizarProducto_DeberiaActualizarProductoExitosamente() {
        // Arrange
        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setNombre("Laptop Antiguo");
        productoExistente.setPrecio(new BigDecimal("999.99"));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsByNombre("Laptop Gaming")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        Producto resultado = productoService.actualizarProducto(1L, request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Laptop Gaming", resultado.getNombre());
        verify(productoRepository).findById(1L);
        verify(productoRepository).existsByNombre("Laptop Gaming");
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_DeberiaLanzarExcepcionSiProductoNoExiste() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.actualizarProducto(999L, request)
        );

        assertEquals("Producto no encontrado con ID: 999", exception.getMessage());
        verify(productoRepository).findById(999L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_DeberiaLanzarExcepcionSiNombreDuplicado() {
        // Arrange
        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setNombre("Laptop Antiguo");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsByNombre("Laptop Gaming")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.actualizarProducto(1L, request)
        );

        assertEquals("Ya existe un producto con el nombre: Laptop Gaming", exception.getMessage());
        verify(productoRepository).findById(1L);
        verify(productoRepository).existsByNombre("Laptop Gaming");
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void eliminarProducto_DeberiaEliminarProductoExitosamente() {
        // Arrange
        when(productoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(1L);

        // Act
        productoService.eliminarProducto(1L);

        // Assert
        verify(productoRepository).existsById(1L);
        verify(productoRepository).deleteById(1L);
    }

    @Test
    void eliminarProducto_DeberiaLanzarExcepcionSiProductoNoExiste() {
        // Arrange
        when(productoRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.eliminarProducto(999L)
        );

        assertEquals("Producto no encontrado con ID: 999", exception.getMessage());
        verify(productoRepository).existsById(999L);
        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    void existeProducto_DeberiaRetornarTrueSiExiste() {
        // Arrange
        when(productoRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean resultado = productoService.existeProducto(1L);

        // Assert
        assertTrue(resultado);
        verify(productoRepository).existsById(1L);
    }

    @Test
    void existeProducto_DeberiaRetornarFalseSiNoExiste() {
        // Arrange
        when(productoRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean resultado = productoService.existeProducto(999L);

        // Assert
        assertFalse(resultado);
        verify(productoRepository).existsById(999L);
    }

    @Test
    void obtenerEstadisticas_DeberiaRetornarEstadisticasCorrectas() {
        // Arrange
        when(productoRepository.countProductos()).thenReturn(5L);
        when(productoRepository.getPrecioPromedio()).thenReturn(new BigDecimal("1000.00"));

        // Act
        Object[] resultado = productoService.obtenerEstadisticas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.length);
        assertEquals(5L, resultado[0]);
        assertEquals(new BigDecimal("1000.00"), resultado[1]);
        verify(productoRepository).countProductos();
        verify(productoRepository).getPrecioPromedio();
    }
}
