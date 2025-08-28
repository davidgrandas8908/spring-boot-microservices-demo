package com.microservices.inventario.service;

import com.microservices.inventario.client.ProductosClient;
import com.microservices.inventario.dto.ProductoResponse;
import com.microservices.inventario.entity.Inventario;
import com.microservices.inventario.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para InventarioService.
 * 
 * Cubre todos los métodos del servicio con casos exitosos,
 * errores y edge cases para asegurar cobertura del 90%+.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventarioService Tests")
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
        // Configurar Inventario
        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);
        inventario.setCantidadMinima(2);
        inventario.setCantidadMaxima(100);

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
    @DisplayName("Debería obtener inventario por ID cuando existe")
    void deberiaObtenerInventarioPorIdCuandoExiste() {
        // Given
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        // When
        Optional<Inventario> resultado = inventarioService.obtenerInventarioPorId(1L);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        assertEquals(1L, resultado.get().getProductoId());
        verify(inventarioRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar Optional vacío cuando inventario no existe")
    void deberiaRetornarOptionalVacioCuandoInventarioNoExiste() {
        // Given
        when(inventarioRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Inventario> resultado = inventarioService.obtenerInventarioPorId(999L);

        // Then
        assertFalse(resultado.isPresent());
        verify(inventarioRepository).findById(999L);
    }

    @Test
    @DisplayName("Debería obtener inventario por producto ID cuando existe")
    void deberiaObtenerInventarioPorProductoIdCuandoExiste() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // When
        Optional<Inventario> resultado = inventarioService.obtenerInventarioPorProductoId(1L);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getProductoId());
        verify(inventarioRepository).findByProductoId(1L);
    }

    @Test
    @DisplayName("Debería retornar Optional vacío cuando inventario por producto no existe")
    void deberiaRetornarOptionalVacioCuandoInventarioPorProductoNoExiste() {
        // Given
        when(inventarioRepository.findByProductoId(999L)).thenReturn(Optional.empty());

        // When
        Optional<Inventario> resultado = inventarioService.obtenerInventarioPorProductoId(999L);

        // Then
        assertFalse(resultado.isPresent());
        verify(inventarioRepository).findByProductoId(999L);
    }

    @Test
    @DisplayName("Debería obtener todos los inventarios con paginación")
    void deberiaObtenerTodosLosInventariosConPaginacion() {
        // Given
        List<Inventario> inventarios = Arrays.asList(inventario);
        Page<Inventario> inventariosPage = new PageImpl<>(inventarios, PageRequest.of(0, 10), 1);
        when(inventarioRepository.findAll(PageRequest.of(0, 10))).thenReturn(inventariosPage);

        // When
        Page<Inventario> resultado = inventarioService.obtenerInventarios(0, 10);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        verify(inventarioRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Debería crear inventario exitosamente")
    void deberiaCrearInventarioExitosamente() {
        // Given
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // When
        Inventario resultado = inventarioService.crearInventario(inventario);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getProductoId());
        verify(inventarioRepository).save(inventario);
    }

    @Test
    @DisplayName("Debería actualizar inventario exitosamente")
    void deberiaActualizarInventarioExitosamente() {
        // Given
        Inventario inventarioActualizado = new Inventario();
        inventarioActualizado.setId(1L);
        inventarioActualizado.setProductoId(1L);
        inventarioActualizado.setCantidadDisponible(15);
        inventarioActualizado.setCantidadMinima(3);
        inventarioActualizado.setCantidadMaxima(150);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioActualizado);

        // When
        Inventario resultado = inventarioService.actualizarInventario(1L, inventarioActualizado);

        // Then
        assertNotNull(resultado);
        assertEquals(15, resultado.getCantidadDisponible());
        assertEquals(3, resultado.getCantidadMinima());
        verify(inventarioRepository).findById(1L);
        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar inventario inexistente")
    void deberiaLanzarExcepcionAlActualizarInventarioInexistente() {
        // Given
        when(inventarioRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.actualizarInventario(999L, inventario));
        
        assertEquals("Inventario no encontrado con ID: 999", exception.getMessage());
        verify(inventarioRepository).findById(999L);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería eliminar inventario exitosamente")
    void deberiaEliminarInventarioExitosamente() {
        // Given
        when(inventarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(inventarioRepository).deleteById(1L);

        // When
        inventarioService.eliminarInventario(1L);

        // Then
        verify(inventarioRepository).existsById(1L);
        verify(inventarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar inventario inexistente")
    void deberiaLanzarExcepcionAlEliminarInventarioInexistente() {
        // Given
        when(inventarioRepository.existsById(999L)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.eliminarInventario(999L));
        
        assertEquals("Inventario no encontrado con ID: 999", exception.getMessage());
        verify(inventarioRepository).existsById(999L);
        verify(inventarioRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debería verificar stock suficiente cuando hay stock")
    void deberiaVerificarStockSuficienteCuandoHayStock() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // When
        boolean resultado = inventarioService.verificarStockSuficiente(1L, 5);

        // Then
        assertTrue(resultado);
        verify(inventarioRepository).findByProductoId(1L);
    }

    @Test
    @DisplayName("Debería verificar stock insuficiente cuando no hay stock")
    void deberiaVerificarStockInsuficienteCuandoNoHayStock() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // When
        boolean resultado = inventarioService.verificarStockSuficiente(1L, 15);

        // Then
        assertFalse(resultado);
        verify(inventarioRepository).findByProductoId(1L);
    }

    @Test
    @DisplayName("Debería verificar stock insuficiente cuando producto no existe")
    void deberiaVerificarStockInsuficienteCuandoProductoNoExiste() {
        // Given
        when(inventarioRepository.findByProductoId(999L)).thenReturn(Optional.empty());

        // When
        boolean resultado = inventarioService.verificarStockSuficiente(999L, 5);

        // Then
        assertFalse(resultado);
        verify(inventarioRepository).findByProductoId(999L);
    }

    @Test
    @DisplayName("Debería reducir stock exitosamente")
    void deberiaReducirStockExitosamente() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // When
        inventarioService.reducirStock(1L, 5);

        // Then
        assertEquals(5, inventario.getCantidadDisponible()); // 10 - 5 = 5
        verify(inventarioRepository).findByProductoId(1L);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    @DisplayName("Debería lanzar excepción al reducir stock de producto inexistente")
    void deberiaLanzarExcepcionAlReducirStockDeProductoInexistente() {
        // Given
        when(inventarioRepository.findByProductoId(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.reducirStock(999L, 5));
        
        assertEquals("Producto no encontrado en inventario con ID: 999", exception.getMessage());
        verify(inventarioRepository).findByProductoId(999L);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al reducir stock insuficiente")
    void deberiaLanzarExcepcionAlReducirStockInsuficiente() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.reducirStock(1L, 15));
        
        assertEquals("Stock insuficiente para el producto con ID: 1", exception.getMessage());
        verify(inventarioRepository).findByProductoId(1L);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería aumentar stock exitosamente")
    void deberiaAumentarStockExitosamente() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // When
        inventarioService.aumentarStock(1L, 5);

        // Then
        assertEquals(15, inventario.getCantidadDisponible()); // 10 + 5 = 15
        verify(inventarioRepository).findByProductoId(1L);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    @DisplayName("Debería lanzar excepción al aumentar stock de producto inexistente")
    void deberiaLanzarExcepcionAlAumentarStockDeProductoInexistente() {
        // Given
        when(inventarioRepository.findByProductoId(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.aumentarStock(999L, 5));
        
        assertEquals("Producto no encontrado en inventario con ID: 999", exception.getMessage());
        verify(inventarioRepository).findByProductoId(999L);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería obtener información del producto exitosamente")
    void deberiaObtenerInformacionDelProductoExitosamente() {
        // Given
        when(productosClient.obtenerProducto(1L)).thenReturn(productoResponse);

        // When
        ProductoResponse resultado = inventarioService.obtenerInformacionProducto(1L);

        // Then
        assertNotNull(resultado);
        assertEquals("1", resultado.getData().getId());
        assertEquals("productos", resultado.getData().getType());
        assertEquals("Producto Test", resultado.getData().getAttributes().getNombre());
        verify(productosClient).obtenerProducto(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando producto no existe")
    void deberiaLanzarExcepcionCuandoProductoNoExiste() {
        // Given
        when(productosClient.obtenerProducto(999L))
            .thenThrow(new IllegalArgumentException("Producto no encontrado"));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.obtenerInformacionProducto(999L));
        
        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productosClient).obtenerProducto(999L);
    }

    @Test
    @DisplayName("Debería obtener productos con stock bajo")
    void deberiaObtenerProductosConStockBajo() {
        // Given
        List<Inventario> inventariosBajoStock = Arrays.asList(inventario);
        when(inventarioRepository.findByCantidadDisponibleLessThanEqual(5)).thenReturn(inventariosBajoStock);

        // When
        List<Inventario> resultado = inventarioService.obtenerProductosConStockBajo(5);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(inventarioRepository).findByCantidadDisponibleLessThanEqual(5);
    }

    @Test
    @DisplayName("Debería obtener productos agotados")
    void deberiaObtenerProductosAgotados() {
        // Given
        Inventario inventarioAgotado = new Inventario();
        inventarioAgotado.setId(2L);
        inventarioAgotado.setProductoId(2L);
        inventarioAgotado.setCantidadDisponible(0);
        inventarioAgotado.setCantidadMinima(2);

        List<Inventario> inventariosAgotados = Arrays.asList(inventarioAgotado);
        when(inventarioRepository.findByCantidadDisponible(0)).thenReturn(inventariosAgotados);

        // When
        List<Inventario> resultado = inventarioService.obtenerProductosAgotados();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(0, resultado.get(0).getCantidadDisponible());
        verify(inventarioRepository).findByCantidadDisponible(0);
    }

    @Test
    @DisplayName("Debería manejar cantidad cero en verificación de stock")
    void deberiaManejarCantidadCeroEnVerificacionDeStock() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // When
        boolean resultado = inventarioService.verificarStockSuficiente(1L, 0);

        // Then
        assertTrue(resultado); // Cero unidades siempre es suficiente
        verify(inventarioRepository).findByProductoId(1L);
    }

    @Test
    @DisplayName("Debería manejar cantidad negativa en verificación de stock")
    void deberiaManejarCantidadNegativaEnVerificacionDeStock() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // When
        boolean resultado = inventarioService.verificarStockSuficiente(1L, -5);

        // Then
        assertFalse(resultado); // Cantidad negativa nunca es suficiente
        verify(inventarioRepository).findByProductoId(1L);
    }

    @Test
    @DisplayName("Debería manejar cantidad cero en reducción de stock")
    void deberiaManejarCantidadCeroEnReduccionDeStock() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // When
        inventarioService.reducirStock(1L, 0);

        // Then
        assertEquals(10, inventario.getCantidadDisponible()); // No cambia
        verify(inventarioRepository).findByProductoId(1L);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    @DisplayName("Debería manejar cantidad cero en aumento de stock")
    void deberiaManejarCantidadCeroEnAumentoDeStock() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // When
        inventarioService.aumentarStock(1L, 0);

        // Then
        assertEquals(10, inventario.getCantidadDisponible()); // No cambia
        verify(inventarioRepository).findByProductoId(1L);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    @DisplayName("Debería manejar cantidad negativa en aumento de stock")
    void deberiaManejarCantidadNegativaEnAumentoDeStock() {
        // Given
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // When
        inventarioService.aumentarStock(1L, -5);

        // Then
        assertEquals(5, inventario.getCantidadDisponible()); // 10 + (-5) = 5
        verify(inventarioRepository).findByProductoId(1L);
        verify(inventarioRepository).save(inventario);
    }
}
