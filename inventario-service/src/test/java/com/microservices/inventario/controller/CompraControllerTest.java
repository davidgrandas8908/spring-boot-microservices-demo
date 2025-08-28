package com.microservices.inventario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.inventario.dto.CompraRequest;
import com.microservices.inventario.dto.CompraResponse;
import com.microservices.inventario.entity.Compra;
import com.microservices.inventario.service.CompraService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para CompraController.
 * 
 * Cubre todos los endpoints del controlador con casos exitosos,
 * errores y validaciones para asegurar cobertura del 90%+.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompraController Tests")
class CompraControllerTest {

    @Mock
    private CompraService compraService;

    @InjectMocks
    private CompraController compraController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CompraRequest compraRequest;
    private Compra compra;
    private CompraResponse compraResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(compraController).build();
        objectMapper = new ObjectMapper();

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

        // Configurar CompraResponse
        compraResponse = CompraResponse.single(compra);
    }

    @Test
    @DisplayName("Debería procesar compra exitosamente")
    void deberiaProcesarCompraExitosamente() throws Exception {
        // Given
        when(compraService.procesarCompra(any(CompraRequest.class))).thenReturn(compra);

        // When & Then
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("compras"))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(5));

        verify(compraService).procesarCompra(any(CompraRequest.class));
    }

    @Test
    @DisplayName("Debería retornar 400 cuando request es inválido")
    void deberiaRetornar400CuandoRequestEsInvalido() throws Exception {
        // Given
        CompraRequest requestInvalido = new CompraRequest();
        // No se establece el campo data, lo que hace el request inválido

        // When & Then
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

        verify(compraService, never()).procesarCompra(any(CompraRequest.class));
    }

    @Test
    @DisplayName("Debería obtener compra por ID cuando existe")
    void deberiaObtenerCompraPorIdCuandoExiste() throws Exception {
        // Given
        when(compraService.obtenerCompraPorId(1L)).thenReturn(Optional.of(compra));

        // When & Then
        mockMvc.perform(get("/api/v1/compras/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("compras"));

        verify(compraService).obtenerCompraPorId(1L);
    }

    @Test
    @DisplayName("Debería retornar 404 cuando compra no existe")
    void deberiaRetornar404CuandoCompraNoExiste() throws Exception {
        // Given
        when(compraService.obtenerCompraPorId(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/compras/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(compraService).obtenerCompraPorId(999L);
    }

    @Test
    @DisplayName("Debería obtener compras por producto con paginación")
    void deberiaObtenerComprasPorProductoConPaginacion() throws Exception {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        Page<Compra> comprasPage = new PageImpl<>(compras, PageRequest.of(0, 10), 1);
        when(compraService.obtenerComprasPorProducto(1L, 0, 10)).thenReturn(comprasPage);

        // When & Then
        mockMvc.perform(get("/api/v1/compras/producto/1")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("1"));

        verify(compraService).obtenerComprasPorProducto(1L, 0, 10);
    }

    @Test
    @DisplayName("Debería obtener compras por producto con parámetros por defecto")
    void deberiaObtenerComprasPorProductoConParametrosPorDefecto() throws Exception {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        Page<Compra> comprasPage = new PageImpl<>(compras, PageRequest.of(0, 10), 1);
        when(compraService.obtenerComprasPorProducto(1L, 0, 10)).thenReturn(comprasPage);

        // When & Then
        mockMvc.perform(get("/api/v1/compras/producto/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(compraService).obtenerComprasPorProducto(1L, 0, 10);
    }

    @Test
    @DisplayName("Debería obtener todas las compras con paginación")
    void deberiaObtenerTodasLasComprasConPaginacion() throws Exception {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        Page<Compra> comprasPage = new PageImpl<>(compras, PageRequest.of(0, 10), 1);
        when(compraService.obtenerCompras(0, 10)).thenReturn(comprasPage);

        // When & Then
        mockMvc.perform(get("/api/v1/compras")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray());

        verify(compraService).obtenerCompras(0, 10);
    }

    @Test
    @DisplayName("Debería obtener compras por estado")
    void deberiaObtenerComprasPorEstado() throws Exception {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        Page<Compra> comprasPage = new PageImpl<>(compras, PageRequest.of(0, 10), 1);
        when(compraService.obtenerComprasPorEstado(Compra.EstadoCompra.COMPLETADA, 0, 10))
            .thenReturn(comprasPage);

        // When & Then
        mockMvc.perform(get("/api/v1/compras/estado/COMPLETADA")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray());

        verify(compraService).obtenerComprasPorEstado(Compra.EstadoCompra.COMPLETADA, 0, 10);
    }

    @Test
    @DisplayName("Debería obtener compras recientes")
    void deberiaObtenerComprasRecientes() throws Exception {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        when(compraService.obtenerComprasRecientes(5)).thenReturn(compras);

        // When & Then
        mockMvc.perform(get("/api/v1/compras/recientes")
                .param("limit", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray());

        verify(compraService).obtenerComprasRecientes(5);
    }

    @Test
    @DisplayName("Debería obtener compras recientes con límite por defecto")
    void deberiaObtenerComprasRecientesConLimitePorDefecto() throws Exception {
        // Given
        List<Compra> compras = Arrays.asList(compra);
        when(compraService.obtenerComprasRecientes(10)).thenReturn(compras);

        // When & Then
        mockMvc.perform(get("/api/v1/compras/recientes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(compraService).obtenerComprasRecientes(10);
    }

    @Test
    @DisplayName("Debería cancelar compra exitosamente")
    void deberiaCancelarCompraExitosamente() throws Exception {
        // Given
        Compra compraCancelada = new Compra(1L, 5, new BigDecimal("10.00"));
        compraCancelada.setId(1L);
        compraCancelada.setEstado(Compra.EstadoCompra.CANCELADA);
        compraCancelada.setObservaciones("Compra cancelada");
        
        when(compraService.cancelarCompra(1L)).thenReturn(compraCancelada);

        // When & Then
        mockMvc.perform(put("/api/v1/compras/1/cancelar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.attributes.estado").value("CANCELADA"));

        verify(compraService).cancelarCompra(1L);
    }

    @Test
    @DisplayName("Debería obtener estadísticas de compras")
    void deberiaObtenerEstadisticasDeCompras() throws Exception {
        // Given
        Object[] estadisticas = {10L, new BigDecimal("1000.00"), 8L, 2L};
        when(compraService.obtenerEstadisticas()).thenReturn(estadisticas);

        // When & Then
        mockMvc.perform(get("/api/v1/compras/estadisticas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value(10))
                .andExpect(jsonPath("$[1]").value(1000.00));

        verify(compraService).obtenerEstadisticas();
    }

    @Test
    @DisplayName("Debería obtener total de ventas por producto")
    void deberiaObtenerTotalVentasPorProducto() throws Exception {
        // Given
        BigDecimal totalVentas = new BigDecimal("500.00");
        when(compraService.obtenerTotalVentasPorProducto(1L)).thenReturn(totalVentas);

        // When & Then
        mockMvc.perform(get("/api/v1/compras/producto/1/total-ventas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(500.00));

        verify(compraService).obtenerTotalVentasPorProducto(1L);
    }

    @Test
    @DisplayName("Debería verificar que se puede procesar compra")
    void deberiaVerificarQueSePuedeProcesarCompra() throws Exception {
        // Given
        when(compraService.puedeProcesarCompra(1L, 5)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/compras/verificar")
                .param("productoId", "1")
                .param("cantidad", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(true));

        verify(compraService).puedeProcesarCompra(1L, 5);
    }

    @Test
    @DisplayName("Debería verificar que no se puede procesar compra")
    void deberiaVerificarQueNoSePuedeProcesarCompra() throws Exception {
        // Given
        when(compraService.puedeProcesarCompra(1L, 100)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/compras/verificar")
                .param("productoId", "1")
                .param("cantidad", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(false));

        verify(compraService).puedeProcesarCompra(1L, 100);
    }

    @Test
    @DisplayName("Debería manejar excepción al procesar compra")
    void deberiaManejarExcepcionAlProcesarCompra() throws Exception {
        // Given
        when(compraService.procesarCompra(any(CompraRequest.class)))
            .thenThrow(new IllegalArgumentException("Stock insuficiente"));

        // When & Then
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isBadRequest());

        verify(compraService).procesarCompra(any(CompraRequest.class));
    }

    @Test
    @DisplayName("Debería manejar excepción al cancelar compra")
    void deberiaManejarExcepcionAlCancelarCompra() throws Exception {
        // Given
        when(compraService.cancelarCompra(999L))
            .thenThrow(new IllegalArgumentException("Compra no encontrada"));

        // When & Then
        mockMvc.perform(put("/api/v1/compras/999/cancelar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(compraService).cancelarCompra(999L);
    }

    @Test
    @DisplayName("Debería validar parámetros requeridos en verificación")
    void deberiaValidarParametrosRequeridosEnVerificacion() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/compras/verificar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(compraService, never()).puedeProcesarCompra(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debería manejar request con datos faltantes")
    void deberiaManejarRequestConDatosFaltantes() throws Exception {
        // Given
        CompraRequest requestIncompleto = new CompraRequest();
        CompraRequest.Data data = new CompraRequest.Data();
        data.setType("compras");
        // No se establecen los attributes
        requestIncompleto.setData(data);

        // When & Then
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestIncompleto)))
                .andExpect(status().isBadRequest());

        verify(compraService, never()).procesarCompra(any(CompraRequest.class));
    }

    @Test
    @DisplayName("Debería manejar request con cantidad negativa")
    void deberiaManejarRequestConCantidadNegativa() throws Exception {
        // Given
        CompraRequest requestNegativo = new CompraRequest();
        CompraRequest.Data data = new CompraRequest.Data();
        data.setType("compras");
        CompraRequest.Attributes attributes = new CompraRequest.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidad(-5); // Cantidad negativa
        data.setAttributes(attributes);
        requestNegativo.setData(data);

        // When & Then
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestNegativo)))
                .andExpect(status().isBadRequest());

        verify(compraService, never()).procesarCompra(any(CompraRequest.class));
    }

    @Test
    @DisplayName("Debería manejar request con productoId nulo")
    void deberiaManejarRequestConProductoIdNulo() throws Exception {
        // Given
        CompraRequest requestSinProducto = new CompraRequest();
        CompraRequest.Data data = new CompraRequest.Data();
        data.setType("compras");
        CompraRequest.Attributes attributes = new CompraRequest.Attributes();
        attributes.setProductoId(null); // ProductoId nulo
        attributes.setCantidad(5);
        data.setAttributes(attributes);
        requestSinProducto.setData(data);

        // When & Then
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestSinProducto)))
                .andExpect(status().isBadRequest());

        verify(compraService, never()).procesarCompra(any(CompraRequest.class));
    }
}
