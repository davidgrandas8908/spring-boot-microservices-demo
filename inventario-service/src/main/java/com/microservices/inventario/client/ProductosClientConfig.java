package com.microservices.inventario.client;

import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración del cliente Feign para comunicación con productos.
 * 
 * Incluye configuración de timeouts, logging, interceptores y
 * manejo personalizado de errores.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@Configuration
public class ProductosClientConfig {

    @Value("${productos.service.timeout:5000}")
    private int timeout;

    @Value("${productos.service.retries:3}")
    private int retries;

    /**
     * Configura el nivel de logging para el cliente Feign.
     * 
     * @return Logger.Level configurado
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Configura los timeouts para las llamadas HTTP.
     * 
     * @return Request.Options configurado
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                timeout, TimeUnit.MILLISECONDS,
                timeout, TimeUnit.MILLISECONDS,
                true
        );
    }

    /**
     * Configura el decodificador de errores personalizado.
     * 
     * @return ErrorDecoder personalizado
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ProductosErrorDecoder();
    }

    /**
     * Decodificador de errores personalizado para el cliente de productos.
     */
    public static class ProductosErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            if (response.status() >= 400 && response.status() <= 499) {
                return new ProductoNotFoundException(
                        "Producto no encontrado o no disponible: " + methodKey
                );
            }
            if (response.status() >= 500 && response.status() <= 599) {
                return new ProductoServiceException(
                        "Error en el servicio de productos: " + methodKey
                );
            }
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }

    /**
     * Excepción para productos no encontrados.
     */
    public static class ProductoNotFoundException extends RuntimeException {
        public ProductoNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Excepción para errores del servicio de productos.
     */
    public static class ProductoServiceException extends RuntimeException {
        public ProductoServiceException(String message) {
            super(message);
        }
    }
}
