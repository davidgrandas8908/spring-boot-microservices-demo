package com.microservices.inventario.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GlobalExceptionHandler.
 * 
 * Cubre todos los tipos de excepciones manejadas con casos exitosos
 * y edge cases para asegurar cobertura del 90%+.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        // Configuración inicial si es necesaria
    }

    @Test
    @DisplayName("Debería manejar IllegalArgumentException")
    void deberiaManejarIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Mensaje de error");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Mensaje de error", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar IllegalArgumentException con mensaje nulo")
    void deberiaManejarIllegalArgumentExceptionConMensajeNulo() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException();

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Argumento inválido", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar RuntimeException")
    void deberiaManejarRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Error interno del servidor");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error interno del servidor", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar RuntimeException con mensaje nulo")
    void deberiaManejarRuntimeExceptionConMensajeNulo() {
        // Given
        RuntimeException exception = new RuntimeException();

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error interno del servidor", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar MethodArgumentNotValidException")
    void deberiaManejarMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("object", "field1", "Error en campo 1");
        FieldError fieldError2 = new FieldError("object", "field2", "Error en campo 2");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error de validación", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getDetails());
        assertEquals(2, response.getBody().getDetails().size());
        assertTrue(response.getBody().getDetails().contains("field1: Error en campo 1"));
        assertTrue(response.getBody().getDetails().contains("field2: Error en campo 2"));
    }

    @Test
    @DisplayName("Debería manejar MethodArgumentNotValidException sin errores de campo")
    void deberiaManejarMethodArgumentNotValidExceptionSinErroresDeCampo() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error de validación", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getDetails());
        assertTrue(response.getBody().getDetails().isEmpty());
    }

    @Test
    @DisplayName("Debería manejar MethodArgumentTypeMismatchException")
    void deberiaManejarMethodArgumentTypeMismatchException() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        
        when(exception.getName()).thenReturn("id");
        when(exception.getValue()).thenReturn("invalid");
        when(exception.getRequiredType()).thenReturn((Class) Long.class);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleTypeMismatchException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tipo de argumento inválido para el parámetro 'id'", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar MethodArgumentTypeMismatchException con valores nulos")
    void deberiaManejarMethodArgumentTypeMismatchExceptionConValoresNulos() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        
        when(exception.getName()).thenReturn(null);
        when(exception.getValue()).thenReturn(null);
        when(exception.getRequiredType()).thenReturn(null);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleTypeMismatchException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tipo de argumento inválido", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción genérica")
    void deberiaManejarExcepcionGenerica() {
        // Given
        Exception exception = new Exception("Excepción genérica");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Excepción genérica", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción genérica con mensaje nulo")
    void deberiaManejarExcepcionGenericaConMensajeNulo() {
        // Given
        Exception exception = new Exception();

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error interno del servidor", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción con causa")
    void deberiaManejarExcepcionConCausa() {
        // Given
        Exception causa = new IllegalArgumentException("Causa raíz");
        Exception exception = new RuntimeException("Error con causa", causa);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error con causa", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción con múltiples causas")
    void deberiaManejarExcepcionConMultiplesCausas() {
        // Given
        Exception causa3 = new IllegalArgumentException("Causa más profunda");
        Exception causa2 = new RuntimeException("Causa intermedia", causa3);
        Exception exception = new RuntimeException("Error con múltiples causas", causa2);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error con múltiples causas", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción con mensaje muy largo")
    void deberiaManejarExcepcionConMensajeMuyLargo() {
        // Given
        String mensajeLargo = "A".repeat(1000);
        Exception exception = new IllegalArgumentException(mensajeLargo);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mensajeLargo, response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción con caracteres especiales")
    void deberiaManejarExcepcionConCaracteresEspeciales() {
        // Given
        String mensajeEspecial = "Error con caracteres especiales: áéíóú ñ ç € $ % &";
        Exception exception = new IllegalArgumentException(mensajeEspecial);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mensajeEspecial, response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción con mensaje vacío")
    void deberiaManejarExcepcionConMensajeVacio() {
        // Given
        Exception exception = new IllegalArgumentException("");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Argumento inválido", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción con mensaje que solo contiene espacios")
    void deberiaManejarExcepcionConMensajeSoloEspacios() {
        // Given
        Exception exception = new IllegalArgumentException("   ");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Argumento inválido", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción con mensaje que contiene solo tabulaciones")
    void deberiaManejarExcepcionConMensajeSoloTabulaciones() {
        // Given
        Exception exception = new IllegalArgumentException("\t\t\t");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Argumento inválido", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debería manejar excepción con mensaje que contiene saltos de línea")
    void deberiaManejarExcepcionConMensajeConSaltosDeLinea() {
        // Given
        Exception exception = new IllegalArgumentException("Error\ncon\nsaltos\nde\nlínea");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error\ncon\nsaltos\nde\nlínea", response.getBody().getMessage());
        assertEquals("BAD_REQUEST", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }
}

