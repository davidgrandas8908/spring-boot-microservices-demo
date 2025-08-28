package com.microservices.inventario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.inventario.dto.InventarioRequest;
import com.microservices.inventario.dto.InventarioResponse;
import com.microservices.inventario.entity.Inventario;
import com.microservices.inventario.service.InventarioService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para InventarioController.
 * 
 * Cubre todos los endpoints del controlador con casos exitosos,
 * errores y validaciones para asegurar cobertura del 90%+.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventarioController Tests")
class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private InventarioController inventarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Inventario inventario;
    private InventarioRequest inventarioRequest;
    private InventarioResponse inventarioResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventarioController).build();
        objectMapper = new ObjectMapper();

        // Configurar Inventario
        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);
        inventario.setCantidadMinima(2);
        inventario.setCantidadMaxima(100);

        // Configurar InventarioRequest
        inventarioRequest = new InventarioRequest();
        InventarioRequest.Data data = new InventarioRequest.Data();
        data.setType("inventario");
        InventarioRequest.Attributes attributes = new InventarioRequest.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidadDisponible(10);
        attributes.setCantidadMinima(2);
        attributes.setCantidadMaxima(100);
        data.setAttributes(attributes);
        inventarioRequest.setData(data);

        // Configurar InventarioResponse
        inventarioResponse = InventarioResponse.single(inventario);
    }

    @Test
    @DisplayName("Debería crear inventario exitosamente")
    void deberiaCrearInventarioExitosamente() throws Exception {
        // Given
        when(inventarioService.crearInventario(any(Inventario.class))).thenReturn(inventario);

        // When & Then
        mockMvc.perform(post("/api/v1/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventarioRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1))
                .andExpect(jsonPath("$.data.attributes.cantidadDisponible").value(10));

        verify(inventarioService).crearInventario(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería retornar 400 cuando request es inválido")
    void deberiaRetornar400CuandoRequestEsInvalido() throws Exception {
        // Given
        InventarioRequest requestInvalido = new InventarioRequest();
        // No se establece el campo data, lo que hace el request inválido

        // When & Then
        mockMvc.perform(post("/api/v1/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería obtener inventario por ID cuando existe")
    void deberiaObtenerInventarioPorIdCuandoExiste() throws Exception {
        // Given
        when(inventarioService.obtenerInventarioPorId(1L)).thenReturn(Optional.of(inventario));

        // When & Then
        mockMvc.perform(get("/api/v1/inventario/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"));

        verify(inventarioService).obtenerInventarioPorId(1L);
    }

    @Test
    @DisplayName("Debería retornar 404 cuando inventario no existe")
    void deberiaRetornar404CuandoInventarioNoExiste() throws Exception {
        // Given
        when(inventarioService.obtenerInventarioPorId(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/inventario/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(inventarioService).obtenerInventarioPorId(999L);
    }

    @Test
    @DisplayName("Debería obtener inventario por producto ID cuando existe")
    void deberiaObtenerInventarioPorProductoIdCuandoExiste() throws Exception {
        // Given
        when(inventarioService.obtenerInventarioPorProductoId(1L)).thenReturn(Optional.of(inventario));

        // When & Then
        mockMvc.perform(get("/api/v1/inventario/producto/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"));

        verify(inventarioService).obtenerInventarioPorProductoId(1L);
    }

    @Test
    @DisplayName("Debería retornar 404 cuando inventario por producto no existe")
    void deberiaRetornar404CuandoInventarioPorProductoNoExiste() throws Exception {
        // Given
        when(inventarioService.obtenerInventarioPorProductoId(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/inventario/producto/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(inventarioService).obtenerInventarioPorProductoId(999L);
    }

    @Test
    @DisplayName("Debería obtener todos los inventarios con paginación")
    void deberiaObtenerTodosLosInventariosConPaginacion() throws Exception {
        // Given
        List<Inventario> inventarios = Arrays.asList(inventario);
        Page<Inventario> inventariosPage = new PageImpl<>(inventarios, PageRequest.of(0, 10), 1);
        when(inventarioService.obtenerInventarios(0, 10)).thenReturn(inventariosPage);

        // When & Then
        mockMvc.perform(get("/api/v1/inventario")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("1"));

        verify(inventarioService).obtenerInventarios(0, 10);
    }

    @Test
    @DisplayName("Debería obtener inventarios con parámetros por defecto")
    void deberiaObtenerInventariosConParametrosPorDefecto() throws Exception {
        // Given
        List<Inventario> inventarios = Arrays.asList(inventario);
        Page<Inventario> inventariosPage = new PageImpl<>(inventarios, PageRequest.of(0, 10), 1);
        when(inventarioService.obtenerInventarios(0, 10)).thenReturn(inventariosPage);

        // When & Then
        mockMvc.perform(get("/api/v1/inventario")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(inventarioService).obtenerInventarios(0, 10);
    }

    @Test
    @DisplayName("Debería actualizar inventario exitosamente")
    void deberiaActualizarInventarioExitosamente() throws Exception {
        // Given
        Inventario inventarioActualizado = new Inventario();
        inventarioActualizado.setId(1L);
        inventarioActualizado.setProductoId(1L);
        inventarioActualizado.setCantidadDisponible(15);
        inventarioActualizado.setCantidadMinima(3);
        inventarioActualizado.setCantidadMaxima(150);

        when(inventarioService.actualizarInventario(eq(1L), any(Inventario.class)))
            .thenReturn(inventarioActualizado);

        // When & Then
        mockMvc.perform(put("/api/v1/inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventarioRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("1"));

        verify(inventarioService).actualizarInventario(eq(1L), any(Inventario.class));
    }

    @Test
    @DisplayName("Debería manejar excepción al actualizar inventario")
    void deberiaManejarExcepcionAlActualizarInventario() throws Exception {
        // Given
        when(inventarioService.actualizarInventario(eq(999L), any(Inventario.class)))
            .thenThrow(new IllegalArgumentException("Inventario no encontrado"));

        // When & Then
        mockMvc.perform(put("/api/v1/inventario/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventarioRequest)))
                .andExpect(status().isBadRequest());

        verify(inventarioService).actualizarInventario(eq(999L), any(Inventario.class));
    }

    @Test
    @DisplayName("Debería eliminar inventario exitosamente")
    void deberiaEliminarInventarioExitosamente() throws Exception {
        // Given
        doNothing().when(inventarioService).eliminarInventario(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/inventario/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(inventarioService).eliminarInventario(1L);
    }

    @Test
    @DisplayName("Debería manejar excepción al eliminar inventario")
    void deberiaManejarExcepcionAlEliminarInventario() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Inventario no encontrado"))
            .when(inventarioService).eliminarInventario(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/inventario/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(inventarioService).eliminarInventario(999L);
    }

    @Test
    @DisplayName("Debería verificar stock suficiente")
    void deberiaVerificarStockSuficiente() throws Exception {
        // Given
        when(inventarioService.verificarStockSuficiente(1L, 5)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/inventario/verificar-stock")
                .param("productoId", "1")
                .param("cantidad", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(true));

        verify(inventarioService).verificarStockSuficiente(1L, 5);
    }

    @Test
    @DisplayName("Debería verificar stock insuficiente")
    void deberiaVerificarStockInsuficiente() throws Exception {
        // Given
        when(inventarioService.verificarStockSuficiente(1L, 15)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/inventario/verificar-stock")
                .param("productoId", "1")
                .param("cantidad", "15")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(false));

        verify(inventarioService).verificarStockSuficiente(1L, 15);
    }

    @Test
    @DisplayName("Debería validar parámetros requeridos en verificación de stock")
    void deberiaValidarParametrosRequeridosEnVerificacionDeStock() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/inventario/verificar-stock")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).verificarStockSuficiente(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debería obtener productos con stock bajo")
    void deberiaObtenerProductosConStockBajo() throws Exception {
        // Given
        List<Inventario> inventariosBajoStock = Arrays.asList(inventario);
        when(inventarioService.obtenerProductosConStockBajo(5)).thenReturn(inventariosBajoStock);

        // When & Then
        mockMvc.perform(get("/api/v1/inventario/stock-bajo")
                .param("umbral", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("1"));

        verify(inventarioService).obtenerProductosConStockBajo(5);
    }

    @Test
    @DisplayName("Debería obtener productos con stock bajo con umbral por defecto")
    void deberiaObtenerProductosConStockBajoConUmbralPorDefecto() throws Exception {
        // Given
        List<Inventario> inventariosBajoStock = Arrays.asList(inventario);
        when(inventarioService.obtenerProductosConStockBajo(10)).thenReturn(inventariosBajoStock);

        // When & Then
        mockMvc.perform(get("/api/v1/inventario/stock-bajo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(inventarioService).obtenerProductosConStockBajo(10);
    }

    @Test
    @DisplayName("Debería obtener productos agotados")
    void deberiaObtenerProductosAgotados() throws Exception {
        // Given
        Inventario inventarioAgotado = new Inventario();
        inventarioAgotado.setId(2L);
        inventarioAgotado.setProductoId(2L);
        inventarioAgotado.setCantidadDisponible(0);
        inventarioAgotado.setCantidadMinima(2);

        List<Inventario> inventariosAgotados = Arrays.asList(inventarioAgotado);
        when(inventarioService.obtenerProductosAgotados()).thenReturn(inventariosAgotados);

        // When & Then
        mockMvc.perform(get("/api/v1/inventario/agotados")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("2"))
                .andExpect(jsonPath("$.data[0].attributes.cantidadDisponible").value(0));

        verify(inventarioService).obtenerProductosAgotados();
    }

    @Test
    @DisplayName("Debería reducir stock exitosamente")
    void deberiaReducirStockExitosamente() throws Exception {
        // Given
        doNothing().when(inventarioService).reducirStock(1L, 5);

        // When & Then
        mockMvc.perform(put("/api/v1/inventario/producto/1/reducir-stock")
                .param("cantidad", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(inventarioService).reducirStock(1L, 5);
    }

    @Test
    @DisplayName("Debería manejar excepción al reducir stock")
    void deberiaManejarExcepcionAlReducirStock() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Stock insuficiente"))
            .when(inventarioService).reducirStock(1L, 15);

        // When & Then
        mockMvc.perform(put("/api/v1/inventario/producto/1/reducir-stock")
                .param("cantidad", "15")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(inventarioService).reducirStock(1L, 15);
    }

    @Test
    @DisplayName("Debería validar parámetros requeridos en reducción de stock")
    void deberiaValidarParametrosRequeridosEnReduccionDeStock() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/inventario/producto/1/reducir-stock")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).reducirStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debería aumentar stock exitosamente")
    void deberiaAumentarStockExitosamente() throws Exception {
        // Given
        doNothing().when(inventarioService).aumentarStock(1L, 5);

        // When & Then
        mockMvc.perform(put("/api/v1/inventario/producto/1/aumentar-stock")
                .param("cantidad", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(inventarioService).aumentarStock(1L, 5);
    }

    @Test
    @DisplayName("Debería manejar excepción al aumentar stock")
    void deberiaManejarExcepcionAlAumentarStock() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Producto no encontrado"))
            .when(inventarioService).aumentarStock(999L, 5);

        // When & Then
        mockMvc.perform(put("/api/v1/inventario/producto/999/aumentar-stock")
                .param("cantidad", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(inventarioService).aumentarStock(999L, 5);
    }

    @Test
    @DisplayName("Debería validar parámetros requeridos en aumento de stock")
    void deberiaValidarParametrosRequeridosEnAumentoDeStock() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/inventario/producto/1/aumentar-stock")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).aumentarStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Debería manejar request con datos faltantes")
    void deberiaManejarRequestConDatosFaltantes() throws Exception {
        // Given
        InventarioRequest requestIncompleto = new InventarioRequest();
        InventarioRequest.Data data = new InventarioRequest.Data();
        data.setType("inventario");
        // No se establecen los attributes
        requestIncompleto.setData(data);

        // When & Then
        mockMvc.perform(post("/api/v1/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestIncompleto)))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería manejar request con cantidad negativa")
    void deberiaManejarRequestConCantidadNegativa() throws Exception {
        // Given
        InventarioRequest requestNegativo = new InventarioRequest();
        InventarioRequest.Data data = new InventarioRequest.Data();
        data.setType("inventario");
        InventarioRequest.Attributes attributes = new InventarioRequest.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidadDisponible(-5); // Cantidad negativa
        attributes.setCantidadMinima(2);
        attributes.setCantidadMaxima(100);
        data.setAttributes(attributes);
        requestNegativo.setData(data);

        // When & Then
        mockMvc.perform(post("/api/v1/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestNegativo)))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería manejar request con productoId nulo")
    void deberiaManejarRequestConProductoIdNulo() throws Exception {
        // Given
        InventarioRequest requestSinProducto = new InventarioRequest();
        InventarioRequest.Data data = new InventarioRequest.Data();
        data.setType("inventario");
        InventarioRequest.Attributes attributes = new InventarioRequest.Attributes();
        attributes.setProductoId(null); // ProductoId nulo
        attributes.setCantidadDisponible(10);
        attributes.setCantidadMinima(2);
        attributes.setCantidadMaxima(100);
        data.setAttributes(attributes);
        requestSinProducto.setData(data);

        // When & Then
        mockMvc.perform(post("/api/v1/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestSinProducto)))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(any(Inventario.class));
    }
}
