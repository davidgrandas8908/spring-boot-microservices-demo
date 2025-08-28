package com.microservices.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal del microservicio de productos.
 * 
 * Este microservicio es responsable de la gestión de productos,
 * incluyendo operaciones CRUD básicas y exposición de APIs REST
 * siguiendo el estándar JSON API.
 * 
 * @author Microservices Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class ProductosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductosServiceApplication.class, args);
    }
}

