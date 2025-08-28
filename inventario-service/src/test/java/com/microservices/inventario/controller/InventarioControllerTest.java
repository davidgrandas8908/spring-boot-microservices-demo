package com.microservices.inventario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.inventario.dto.InventarioResponse;
import com.microservices.inventario.entity.Inventario;
import com.microservices.inventario.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para el controlador de inventario.
 * 
 * Verifica los endpoints REST del controlador de inventario,
 * incluyendo validaciones de entrada y respuestas HTTP.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private InventarioController inventarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventarioController).build();
        objectMapper = new ObjectMapper();
        
        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProductoId(1L);
        inventario.setCantidad(100);
        inventario.setCantidadMinima(10);
        inventario.setCantidadMaxima(1000);
        inventario.setActivo(true);
    }

    @Test
    void obtenerInventarios_DebeRetornarListaPaginada() throws Exception {
        // Arrange
        List<Inventario> inventarios = Arrays.asList(inventario);
        Page<Inventario> page = new PageImpl<>(inventarios, PageRequest.of(0, 10), 1);
        
        when(inventarioService.obtenerInventarios(0, 10)).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("1"))
                .andExpect(jsonPath("$.data[0].type").value("inventario"))
                .andExpect(jsonPath("$.data[0].attributes.productoId").value(1))
                .andExpect(jsonPath("$.data[0].attributes.cantidad").value(100));

        verify(inventarioService).obtenerInventarios(0, 10);
    }

    @Test
    void obtenerInventarioPorId_DebeRetornarInventario() throws Exception {
        // Arrange
        when(inventarioService.obtenerInventarioPorId(1L)).thenReturn(Optional.of(inventario));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(100));

        verify(inventarioService).obtenerInventarioPorId(1L);
    }

    @Test
    void obtenerInventarioPorId_NoExiste_DebeRetornarNotFound() throws Exception {
        // Arrange
        when(inventarioService.obtenerInventarioPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/999"))
                .andExpect(status().isNotFound());

        verify(inventarioService).obtenerInventarioPorId(999L);
    }

    @Test
    void obtenerInventarioPorProductoId_DebeRetornarInventario() throws Exception {
        // Arrange
        when(inventarioService.obtenerInventarioPorProductoId(1L)).thenReturn(Optional.of(inventario));

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/producto/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(100));

        verify(inventarioService).obtenerInventarioPorProductoId(1L);
    }

    @Test
    void obtenerInventarioPorProductoId_NoExiste_DebeRetornarNotFound() throws Exception {
        // Arrange
        when(inventarioService.obtenerInventarioPorProductoId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/producto/999"))
                .andExpect(status().isNotFound());

        verify(inventarioService).obtenerInventarioPorProductoId(999L);
    }

    @Test
    void crearInventario_DebeCrearExitosamente() throws Exception {
        // Arrange
        when(inventarioService.crearInventario(eq(1L), eq(100), eq(10), eq(1000)))
                .thenReturn(inventario);

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventario")
                .param("productoId", "1")
                .param("cantidad", "100")
                .param("cantidadMinima", "10")
                .param("cantidadMaxima", "1000"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(100));

        verify(inventarioService).crearInventario(1L, 100, 10, 1000);
    }

    @Test
    void crearInventario_ParametrosInvalidos_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/inventario")
                .param("productoId", "-1")
                .param("cantidad", "-10"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(anyLong(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void crearInventario_ProductoNoExiste_DebeRetornarBadRequest() throws Exception {
        // Arrange
        when(inventarioService.crearInventario(eq(999L), eq(100), eq(10), eq(1000)))
                .thenThrow(new IllegalArgumentException("Producto no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/inventario")
                .param("productoId", "999")
                .param("cantidad", "100")
                .param("cantidadMinima", "10")
                .param("cantidadMaxima", "1000"))
                .andExpect(status().isBadRequest());

        verify(inventarioService).crearInventario(999L, 100, 10, 1000);
    }

    @Test
    void actualizarCantidad_DebeActualizarExitosamente() throws Exception {
        // Arrange
        when(inventarioService.actualizarCantidad(1L, 150)).thenReturn(inventario);

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventario/1/cantidad")
                .param("cantidad", "150"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(100));

        verify(inventarioService).actualizarCantidad(1L, 150);
    }

    @Test
    void actualizarCantidad_InventarioNoExiste_DebeRetornarNotFound() throws Exception {
        // Arrange
        when(inventarioService.actualizarCantidad(999L, 150))
                .thenThrow(new IllegalArgumentException("Inventario no encontrado"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventario/999/cantidad")
                .param("cantidad", "150"))
                .andExpect(status().isBadRequest());

        verify(inventarioService).actualizarCantidad(999L, 150);
    }

    @Test
    void aumentarStock_DebeAumentarExitosamente() throws Exception {
        // Arrange
        when(inventarioService.aumentarStock(1L, 50)).thenReturn(inventario);

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventario/1/aumentar")
                .param("cantidad", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"));

        verify(inventarioService).aumentarStock(1L, 50);
    }

    @Test
    void reducirStock_DebeReducirExitosamente() throws Exception {
        // Arrange
        when(inventarioService.reducirStock(1L, 20)).thenReturn(inventario);

        // Act & Assert
        mockMvc.perform(put("/api/v1/inventario/1/reducir")
                .param("cantidad", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"));

        verify(inventarioService).reducirStock(1L, 20);
    }

    @Test
    void verificarStockSuficiente_StockSuficiente_DebeRetornarTrue() throws Exception {
        // Arrange
        when(inventarioService.verificarStockSuficiente(1L, 50)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/1/verificar-stock")
                .param("cantidad", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stockSuficiente").value(true));

        verify(inventarioService).verificarStockSuficiente(1L, 50);
    }

    @Test
    void verificarStockSuficiente_StockInsuficiente_DebeRetornarFalse() throws Exception {
        // Arrange
        when(inventarioService.verificarStockSuficiente(1L, 150)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/1/verificar-stock")
                .param("cantidad", "150"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stockSuficiente").value(false));

        verify(inventarioService).verificarStockSuficiente(1L, 150);
    }

    @Test
    void obtenerInventariosConStockBajo_DebeRetornarLista() throws Exception {
        // Arrange
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioService.obtenerInventariosConStockBajo()).thenReturn(inventarios);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/stock-bajo"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("1"))
                .andExpect(jsonPath("$.data[0].type").value("inventario"));

        verify(inventarioService).obtenerInventariosConStockBajo();
    }

    @Test
    void desactivarInventario_DebeDesactivarExitosamente() throws Exception {
        // Arrange
        doNothing().when(inventarioService).desactivarInventario(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/inventario/1"))
                .andExpect(status().isNoContent());

        verify(inventarioService).desactivarInventario(1L);
    }

    @Test
    void obtenerEstadisticas_DebeRetornarEstadisticas() throws Exception {
        // Arrange
        Object[] estadisticas = {10L, 1000L, 3L};
        when(inventarioService.obtenerEstadisticas()).thenReturn(estadisticas);

        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalInventarios").value(10))
                .andExpect(jsonPath("$.totalStock").value(1000))
                .andExpect(jsonPath("$.inventariosConStockBajo").value(3));

        verify(inventarioService).obtenerEstadisticas();
    }

    @Test
    void obtenerInventarioPorId_ParametroInvalido_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/abc"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).obtenerInventarioPorId(anyLong());
    }

    @Test
    void obtenerInventarioPorProductoId_ParametroInvalido_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/producto/abc"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).obtenerInventarioPorProductoId(anyLong());
    }

    @Test
    void actualizarCantidad_ParametroInvalido_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/inventario/1/cantidad")
                .param("cantidad", "abc"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).actualizarCantidad(anyLong(), anyInt());
    }

    @Test
    void aumentarStock_ParametroInvalido_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/inventario/1/aumentar")
                .param("cantidad", "abc"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).aumentarStock(anyLong(), anyInt());
    }

    @Test
    void reducirStock_ParametroInvalido_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/inventario/1/reducir")
                .param("cantidad", "abc"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).reducirStock(anyLong(), anyInt());
    }

    @Test
    void verificarStockSuficiente_ParametroInvalido_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/inventario/1/verificar-stock")
                .param("cantidad", "abc"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).verificarStockSuficiente(anyLong(), anyInt());
    }

    @Test
    void crearInventario_ParametrosFaltantes_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/inventario")
                .param("productoId", "1"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(anyLong(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void crearInventario_CantidadNegativa_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/inventario")
                .param("productoId", "1")
                .param("cantidad", "-10")
                .param("cantidadMinima", "10")
                .param("cantidadMaxima", "1000"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(anyLong(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void crearInventario_ProductoIdNegativo_DebeRetornarBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/inventario")
                .param("productoId", "-1")
                .param("cantidad", "100")
                .param("cantidadMinima", "10")
                .param("cantidadMaxima", "1000"))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(anyLong(), anyInt(), anyInt(), anyInt());
    }
}
