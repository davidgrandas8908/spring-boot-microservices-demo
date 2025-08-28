package com.microservices.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal del microservicio de inventario.
 * 
 * Este microservicio es responsable de la gestión de inventario,
 * incluyendo operaciones de stock, compras y comunicación con
 * el microservicio de productos.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class InventarioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioServiceApplication.class, args);
    }
}
