package com.microservices.inventario.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.inventario.dto.CompraRequest;
import com.microservices.inventario.entity.Compra;
import com.microservices.inventario.entity.Inventario;
import com.microservices.inventario.repository.CompraRepository;
import com.microservices.inventario.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para el microservicio de inventario.
 * 
 * Utiliza TestContainers con PostgreSQL para probar flujos completos
 * de compra, incluyendo persistencia y comunicación entre servicios.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("Compra Integration Tests")
class CompraIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("inventario_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Limpiar datos de prueba
        compraRepository.deleteAll();
        inventarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Debería procesar compra completa exitosamente")
    void deberiaProcesarCompraCompletaExitosamente() throws Exception {
        // Given - Crear inventario inicial
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        // Crear request de compra
        CompraRequest request = crearCompraRequest(1L, 5);

        // When & Then - Procesar compra
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.type").value("compras"))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(5))
                .andExpect(jsonPath("$.data.attributes.estado").value("COMPLETADA"));

        // Verificar que la compra se guardó en la base de datos
        assertEquals(1, compraRepository.count());
        Compra compraGuardada = compraRepository.findAll().get(0);
        assertEquals(1L, compraGuardada.getProductoId());
        assertEquals(5, compraGuardada.getCantidad());
        assertEquals(Compra.EstadoCompra.COMPLETADA, compraGuardada.getEstado());

        // Verificar que el inventario se actualizó
        Inventario inventarioActualizado = inventarioRepository.findByProductoId(1L).orElse(null);
        assertNotNull(inventarioActualizado);
        assertEquals(5, inventarioActualizado.getCantidadDisponible()); // 10 - 5 = 5
    }

    @Test
    @DisplayName("Debería fallar cuando no hay stock suficiente")
    void deberiaFallarCuandoNoHayStockSuficiente() throws Exception {
        // Given - Crear inventario con stock insuficiente
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(3);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        // Crear request de compra con cantidad mayor al stock
        CompraRequest request = crearCompraRequest(1L, 10);

        // When & Then - Intentar procesar compra
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verificar que no se creó ninguna compra
        assertEquals(0, compraRepository.count());

        // Verificar que el inventario no cambió
        Inventario inventarioSinCambios = inventarioRepository.findByProductoId(1L).orElse(null);
        assertNotNull(inventarioSinCambios);
        assertEquals(3, inventarioSinCambios.getCantidadDisponible());
    }

    @Test
    @DisplayName("Debería cancelar compra y restaurar stock")
    void deberiaCancelarCompraYRestaurarStock() throws Exception {
        // Given - Crear inventario y compra
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        CompraRequest request = crearCompraRequest(1L, 5);
        
        // Procesar compra inicial
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Verificar stock después de la compra
        Inventario inventarioDespuesCompra = inventarioRepository.findByProductoId(1L).orElse(null);
        assertEquals(5, inventarioDespuesCompra.getCantidadDisponible());

        // When & Then - Cancelar compra
        Compra compra = compraRepository.findAll().get(0);
        mockMvc.perform(put("/api/v1/compras/" + compra.getId() + "/cancelar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.estado").value("CANCELADA"));

        // Verificar que la compra se canceló
        Compra compraCancelada = compraRepository.findById(compra.getId()).orElse(null);
        assertNotNull(compraCancelada);
        assertEquals(Compra.EstadoCompra.CANCELADA, compraCancelada.getEstado());

        // Verificar que el stock se restauró
        Inventario inventarioRestaurado = inventarioRepository.findByProductoId(1L).orElse(null);
        assertEquals(10, inventarioRestaurado.getCantidadDisponible()); // 5 + 5 = 10
    }

    @Test
    @DisplayName("Debería obtener compra por ID")
    void deberiaObtenerCompraPorId() throws Exception {
        // Given - Crear inventario y compra
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        CompraRequest request = crearCompraRequest(1L, 5);
        
        // Procesar compra
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Compra compra = compraRepository.findAll().get(0);

        // When & Then - Obtener compra por ID
        mockMvc.perform(get("/api/v1/compras/" + compra.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(compra.getId().toString()))
                .andExpect(jsonPath("$.data.type").value("compras"))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(5));
    }

    @Test
    @DisplayName("Debería retornar 404 para compra inexistente")
    void deberiaRetornar404ParaCompraInexistente() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/compras/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería obtener compras por producto con paginación")
    void deberiaObtenerComprasPorProductoConPaginacion() throws Exception {
        // Given - Crear inventario y múltiples compras
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(100);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        // Crear 3 compras para el mismo producto
        for (int i = 0; i < 3; i++) {
            CompraRequest request = crearCompraRequest(1L, 5);
            mockMvc.perform(post("/api/v1/compras")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Obtener compras por producto
        mockMvc.perform(get("/api/v1/compras/producto/1")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.meta.totalElements").value(3))
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.size").value(10));
    }

    @Test
    @DisplayName("Debería obtener todas las compras con paginación")
    void deberiaObtenerTodasLasComprasConPaginacion() throws Exception {
        // Given - Crear inventario y compras
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(100);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        // Crear 2 compras
        for (int i = 0; i < 2; i++) {
            CompraRequest request = crearCompraRequest(1L, 5);
            mockMvc.perform(post("/api/v1/compras")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Obtener todas las compras
        mockMvc.perform(get("/api/v1/compras")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.meta.totalElements").value(2));
    }

    @Test
    @DisplayName("Debería obtener compras por estado")
    void deberiaObtenerComprasPorEstado() throws Exception {
        // Given - Crear inventario y compra
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        CompraRequest request = crearCompraRequest(1L, 5);
        
        // Procesar compra
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When & Then - Obtener compras completadas
        mockMvc.perform(get("/api/v1/compras/estado/COMPLETADA")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].attributes.estado").value("COMPLETADA"));
    }

    @Test
    @DisplayName("Debería obtener compras recientes")
    void deberiaObtenerComprasRecientes() throws Exception {
        // Given - Crear inventario y compras
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(100);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        // Crear 3 compras
        for (int i = 0; i < 3; i++) {
            CompraRequest request = crearCompraRequest(1L, 5);
            mockMvc.perform(post("/api/v1/compras")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Obtener compras recientes
        mockMvc.perform(get("/api/v1/compras/recientes")
                .param("limit", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("Debería obtener estadísticas de compras")
    void deberiaObtenerEstadisticasDeCompras() throws Exception {
        // Given - Crear inventario y compras
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(100);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        // Crear 2 compras completadas
        for (int i = 0; i < 2; i++) {
            CompraRequest request = crearCompraRequest(1L, 5);
            mockMvc.perform(post("/api/v1/compras")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Obtener estadísticas
        mockMvc.perform(get("/api/v1/compras/estadisticas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(2)) // totalCompras
                .andExpect(jsonPath("$[2]").value(2)) // comprasCompletadas
                .andExpect(jsonPath("$[3]").value(0)); // comprasCanceladas
    }

    @Test
    @DisplayName("Debería obtener total de ventas por producto")
    void deberiaObtenerTotalVentasPorProducto() throws Exception {
        // Given - Crear inventario y compras
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(100);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        // Crear 2 compras con precio unitario 10.00
        for (int i = 0; i < 2; i++) {
            CompraRequest request = crearCompraRequest(1L, 5);
            mockMvc.perform(post("/api/v1/compras")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Obtener total de ventas
        mockMvc.perform(get("/api/v1/compras/producto/1/total-ventas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(100.00)); // 2 compras * 5 unidades * 10.00 = 100.00
    }

    @Test
    @DisplayName("Debería verificar si se puede procesar compra")
    void deberiaVerificarSiSePuedeProcesarCompra() throws Exception {
        // Given - Crear inventario
        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);
        inventario.setCantidadMinima(2);
        inventarioRepository.save(inventario);

        // When & Then - Verificar compra posible
        mockMvc.perform(get("/api/v1/compras/verificar")
                .param("productoId", "1")
                .param("cantidad", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        // Verificar compra imposible
        mockMvc.perform(get("/api/v1/compras/verificar")
                .param("productoId", "1")
                .param("cantidad", "15")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @DisplayName("Debería manejar request inválido")
    void deberiaManejarRequestInvalido() throws Exception {
        // Given - Request sin datos requeridos
        CompraRequest requestInvalido = new CompraRequest();
        // No se establece el campo data

        // When & Then
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debería manejar cantidad negativa")
    void deberiaManejarCantidadNegativa() throws Exception {
        // Given - Request con cantidad negativa
        CompraRequest request = crearCompraRequest(1L, -5);

        // When & Then
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debería manejar producto inexistente")
    void deberiaManejarProductoInexistente() throws Exception {
        // Given - Request para producto que no existe en inventario
        CompraRequest request = crearCompraRequest(999L, 5);

        // When & Then
        mockMvc.perform(post("/api/v1/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private CompraRequest crearCompraRequest(Long productoId, Integer cantidad) {
        CompraRequest request = new CompraRequest();
        CompraRequest.Data data = new CompraRequest.Data();
        data.setType("compras");
        CompraRequest.Attributes attributes = new CompraRequest.Attributes();
        attributes.setProductoId(productoId);
        attributes.setCantidad(cantidad);
        data.setAttributes(attributes);
        request.setData(data);
        return request;
    }
}
