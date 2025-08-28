package com.microservices.inventario.service;

import com.microservices.inventario.dto.CompraRequest;
import com.microservices.inventario.dto.ProductoResponse;
import com.microservices.inventario.entity.Compra;
import com.microservices.inventario.entity.Inventario;
import com.microservices.inventario.repository.CompraRepository;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CompraService.
 * 
 * Cubre todos los métodos del servicio con casos exitosos,
 * errores y edge cases para asegurar cobertura del 90%+.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompraService Tests")
class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private CompraService compraService;

    private CompraRequest compraRequest;
    private Compra compra;
    private ProductoResponse productoResponse;
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        // Configurar CompraRequest
        compraRequest = new CompraRequest();
        CompraRequest.Data data = new CompraRequest.Data();
        data.setType("compras");
        CompraRequest.Attributes attributes = new CompraRequest.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidad(5);
        data.setAttributes(attributes);
        compraRequest.setData(data);

        // Configurar Compra
        compra = new Compra(1L, 5, new BigDecimal("10.00"));
        compra.setId(1L);
        compra.setEstado(Compra.EstadoCompra.COMPLETADA);
        compra.setObservaciones("Compra procesada exitosamente");
        compra.setFechaCompra(LocalDateTime.now());

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

        // Configurar Inventario
        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);
        inventario.setCantidadMinima(2);
    }

    @Test
    @DisplayName("Debería procesar compra exitosamente cuando hay stock suficiente")
    void deberiaProcesarCompraExitosamente() {
        // Given
        when(inventarioService.obtenerInformacionProducto(1L)).thenReturn(productoResponse);
        when(inventarioService.verificarStockSuficiente(1L, 5)).thenReturn(true);
        when(compraRepository.save(any(Compra.class))).thenReturn(compra);
        doNothing().when(inventarioService).reducirStock(1L, 5);

        // When
        Compra resultado = compraService.procesarCompra(compraRequest);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(Compra.EstadoCompra.COMPLETADA, resultado.getEstado());
        assertEquals("Compra procesada exitosamente", resultado.getObservaciones());
        assertEquals(new BigDecimal("50.00"), resultado.getPrecioTotal());

        verify(inventarioService).obtenerInformacionProducto(1L);
        verify(inventarioService).verificarStockSuficiente(1L, 5);
        verify(compraRepository).save(any(Compra.class));
        verify(inventarioService).reducirStock(1L, 5);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando no hay stock suficiente")
    void deberiaLanzarExcepcionCuandoNoHayStockSuficiente() {
        // Given
        when(inventarioService.obtenerInformacionProducto(1L)).thenReturn(productoResponse);
        when(inventarioService.verificarStockSuficiente(1L, 5)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> compraService.procesarCompra(compraRequest));
        
        assertEquals("Stock insuficiente para el producto con ID: 1", exception.getMessage());
        
        verify(inventarioService).obtenerInformacionProducto(1L);
        verify(inventarioService).verificarStockSuficiente(1L, 5);
        verify(compraRepository, never()).save(any(Compra.class));
        verify(inventarioService, never()).reducirStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debería obtener compra por ID cuando existe")
    void deberiaObtenerCompraPorIdCuandoExiste() {
        // Given
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        // When
        Optional<Compra> resultado = compraService.obtenerCompraPorId(1L);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(compraRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar Optional vacío cuando compra no existe")
    void deberiaRetornarOptionalVacioCuandoCompraNoExiste() {
        // Given
        when(compraRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Compra> resultado = compraService.obtenerCompraPorId(999L);

        // Then
        assertFalse(resultado.isPresent());
        verify(compraRepository).findById(999L);
    }

    @Test
    @DisplayName("Debería obtener compras por producto con paginación")
    void deberiaObtenerComprasPorProductoConPaginacion() {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        Page<Compra> comprasPage = new PageImpl<>(compras, PageRequest.of(0, 10), 1);
        when(compraRepository.findByProductoId(1L, PageRequest.of(0, 10))).thenReturn(comprasPage);

        // When
        Page<Compra> resultado = compraService.obtenerComprasPorProducto(1L, 0, 10);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        verify(compraRepository).findByProductoId(1L, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Debería obtener todas las compras con paginación")
    void deberiaObtenerTodasLasComprasConPaginacion() {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        Page<Compra> comprasPage = new PageImpl<>(compras, PageRequest.of(0, 10), 1);
        when(compraRepository.findAll(PageRequest.of(0, 10))).thenReturn(comprasPage);

        // When
        Page<Compra> resultado = compraService.obtenerCompras(0, 10);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        verify(compraRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Debería obtener compras por estado")
    void deberiaObtenerComprasPorEstado() {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        Page<Compra> comprasPage = new PageImpl<>(compras, PageRequest.of(0, 10), 1);
        when(compraRepository.findByEstado(Compra.EstadoCompra.COMPLETADA, PageRequest.of(0, 10)))
            .thenReturn(comprasPage);

        // When
        Page<Compra> resultado = compraService.obtenerComprasPorEstado(Compra.EstadoCompra.COMPLETADA, 0, 10);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        verify(compraRepository).findByEstado(Compra.EstadoCompra.COMPLETADA, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Debería obtener compras recientes")
    void deberiaObtenerComprasRecientes() {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        when(compraRepository.findComprasRecientes(PageRequest.of(0, 5))).thenReturn(compras);

        // When
        List<Compra> resultado = compraService.obtenerComprasRecientes(5);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(compraRepository).findComprasRecientes(PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("Debería cancelar compra exitosamente cuando está completada")
    void deberiaCancelarCompraExitosamenteCuandoEstaCompletada() {
        // Given
        Compra compraCompletada = new Compra(1L, 5, new BigDecimal("10.00"));
        compraCompletada.setId(1L);
        compraCompletada.setEstado(Compra.EstadoCompra.COMPLETADA);
        
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compraCompletada));
        when(compraRepository.save(any(Compra.class))).thenReturn(compraCompletada);
        doNothing().when(inventarioService).aumentarStock(1L, 5);

        // When
        Compra resultado = compraService.cancelarCompra(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(Compra.EstadoCompra.CANCELADA, resultado.getEstado());
        assertEquals("Compra cancelada", resultado.getObservaciones());
        
        verify(compraRepository).findById(1L);
        verify(inventarioService).aumentarStock(1L, 5);
        verify(compraRepository).save(any(Compra.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando compra no existe al cancelar")
    void deberiaLanzarExcepcionCuandoCompraNoExisteAlCancelar() {
        // Given
        when(compraRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> compraService.cancelarCompra(999L));
        
        assertEquals("Compra no encontrada con ID: 999", exception.getMessage());
        verify(compraRepository).findById(999L);
        verify(inventarioService, never()).aumentarStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando compra ya está cancelada")
    void deberiaLanzarExcepcionCuandoCompraYaEstaCancelada() {
        // Given
        Compra compraCancelada = new Compra(1L, 5, new BigDecimal("10.00"));
        compraCancelada.setId(1L);
        compraCancelada.setEstado(Compra.EstadoCompra.CANCELADA);
        
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compraCancelada));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> compraService.cancelarCompra(1L));
        
        assertEquals("La compra ya está cancelada", exception.getMessage());
        verify(compraRepository).findById(1L);
        verify(inventarioService, never()).aumentarStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debería obtener estadísticas de compras")
    void deberiaObtenerEstadisticasDeCompras() {
        // Given
        when(compraRepository.countCompras()).thenReturn(10L);
        when(compraRepository.getTotalVentas()).thenReturn(new BigDecimal("1000.00"));
        when(compraRepository.countByEstado(Compra.EstadoCompra.COMPLETADA)).thenReturn(8L);
        when(compraRepository.countByEstado(Compra.EstadoCompra.CANCELADA)).thenReturn(2L);

        // When
        Object[] resultado = compraService.obtenerEstadisticas();

        // Then
        assertNotNull(resultado);
        assertEquals(4, resultado.length);
        assertEquals(10L, resultado[0]);
        assertEquals(new BigDecimal("1000.00"), resultado[1]);
        assertEquals(8L, resultado[2]);
        assertEquals(2L, resultado[3]);
        
        verify(compraRepository).countCompras();
        verify(compraRepository).getTotalVentas();
        verify(compraRepository).countByEstado(Compra.EstadoCompra.COMPLETADA);
        verify(compraRepository).countByEstado(Compra.EstadoCompra.CANCELADA);
    }

    @Test
    @DisplayName("Debería obtener estadísticas con valores nulos")
    void deberiaObtenerEstadisticasConValoresNulos() {
        // Given
        when(compraRepository.countCompras()).thenReturn(0L);
        when(compraRepository.getTotalVentas()).thenReturn(null);
        when(compraRepository.countByEstado(Compra.EstadoCompra.COMPLETADA)).thenReturn(0L);
        when(compraRepository.countByEstado(Compra.EstadoCompra.CANCELADA)).thenReturn(0L);

        // When
        Object[] resultado = compraService.obtenerEstadisticas();

        // Then
        assertNotNull(resultado);
        assertEquals(4, resultado.length);
        assertEquals(0L, resultado[0]);
        assertEquals(BigDecimal.ZERO, resultado[1]);
        assertEquals(0L, resultado[2]);
        assertEquals(0L, resultado[3]);
    }

    @Test
    @DisplayName("Debería obtener total de ventas por producto")
    void deberiaObtenerTotalVentasPorProducto() {
        // Given
        when(compraRepository.getTotalVentasPorProducto(1L)).thenReturn(new BigDecimal("500.00"));

        // When
        BigDecimal resultado = compraService.obtenerTotalVentasPorProducto(1L);

        // Then
        assertEquals(new BigDecimal("500.00"), resultado);
        verify(compraRepository).getTotalVentasPorProducto(1L);
    }

    @Test
    @DisplayName("Debería retornar cero cuando no hay ventas para el producto")
    void deberiaRetornarCeroCuandoNoHayVentasParaElProducto() {
        // Given
        when(compraRepository.getTotalVentasPorProducto(1L)).thenReturn(null);

        // When
        BigDecimal resultado = compraService.obtenerTotalVentasPorProducto(1L);

        // Then
        assertEquals(BigDecimal.ZERO, resultado);
        verify(compraRepository).getTotalVentasPorProducto(1L);
    }

    @Test
    @DisplayName("Debería verificar que se puede procesar compra cuando hay stock suficiente")
    void deberiaVerificarQueSePuedeProcesarCompraCuandoHayStockSuficiente() {
        // Given
        when(inventarioService.obtenerInformacionProducto(1L)).thenReturn(productoResponse);
        when(inventarioService.verificarStockSuficiente(1L, 5)).thenReturn(true);

        // When
        boolean resultado = compraService.puedeProcesarCompra(1L, 5);

        // Then
        assertTrue(resultado);
        verify(inventarioService).obtenerInformacionProducto(1L);
        verify(inventarioService).verificarStockSuficiente(1L, 5);
    }

    @Test
    @DisplayName("Debería verificar que no se puede procesar compra cuando no hay stock suficiente")
    void deberiaVerificarQueNoSePuedeProcesarCompraCuandoNoHayStockSuficiente() {
        // Given
        when(inventarioService.obtenerInformacionProducto(1L)).thenReturn(productoResponse);
        when(inventarioService.verificarStockSuficiente(1L, 5)).thenReturn(false);

        // When
        boolean resultado = compraService.puedeProcesarCompra(1L, 5);

        // Then
        assertFalse(resultado);
        verify(inventarioService).obtenerInformacionProducto(1L);
        verify(inventarioService).verificarStockSuficiente(1L, 5);
    }

    @Test
    @DisplayName("Debería verificar que no se puede procesar compra cuando producto no existe")
    void deberiaVerificarQueNoSePuedeProcesarCompraCuandoProductoNoExiste() {
        // Given
        when(inventarioService.obtenerInformacionProducto(999L))
            .thenThrow(new IllegalArgumentException("Producto no encontrado"));

        // When
        boolean resultado = compraService.puedeProcesarCompra(999L, 5);

        // Then
        assertFalse(resultado);
        verify(inventarioService).obtenerInformacionProducto(999L);
        verify(inventarioService, never()).verificarStockSuficiente(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debería manejar cantidad cero en verificación de compra")
    void deberiaManejarCantidadCeroEnVerificacionDeCompra() {
        // Given
        when(inventarioService.obtenerInformacionProducto(1L)).thenReturn(productoResponse);
        when(inventarioService.verificarStockSuficiente(1L, 0)).thenReturn(false);

        // When
        boolean resultado = compraService.puedeProcesarCompra(1L, 0);

        // Then
        assertFalse(resultado);
        verify(inventarioService).obtenerInformacionProducto(1L);
        verify(inventarioService).verificarStockSuficiente(1L, 0);
    }

    @Test
    @DisplayName("Debería manejar cantidad negativa en verificación de compra")
    void deberiaManejarCantidadNegativaEnVerificacionDeCompra() {
        // Given
        when(inventarioService.obtenerInformacionProducto(1L)).thenReturn(productoResponse);
        when(inventarioService.verificarStockSuficiente(1L, -5)).thenReturn(false);

        // When
        boolean resultado = compraService.puedeProcesarCompra(1L, -5);

        // Then
        assertFalse(resultado);
        verify(inventarioService).obtenerInformacionProducto(1L);
        verify(inventarioService).verificarStockSuficiente(1L, -5);
    }
}
