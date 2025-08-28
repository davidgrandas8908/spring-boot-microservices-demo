# Microservicios de Productos e Inventario

## Descripción del Proyecto

Este proyecto implementa dos microservicios independientes que interactúan entre sí para gestionar productos e inventario, siguiendo las mejores prácticas de arquitectura de microservicios y el estándar JSON API.

### Arquitectura

```
┌─────────────────┐    HTTP/JSON API    ┌─────────────────┐
│   Productos     │◄───────────────────►│   Inventario    │
│   Microservice  │                     │   Microservice  │
└─────────────────┘                     └─────────────────┘
         │                                       │
         ▼                                       ▼
┌─────────────────┐                     ┌─────────────────┐
│   PostgreSQL    │                     │   PostgreSQL    │
│   (Productos)   │                     │  (Inventario)   │
└─────────────────┘                     └─────────────────┘
```

## Decisiones Técnicas y Justificaciones

### 1. Base de Datos: PostgreSQL
**Justificación:** 
- **Consistencia ACID**: Garantiza la integridad de datos en transacciones críticas como compras
- **Soporte JSON**: Permite almacenar metadatos flexibles manteniendo estructura relacional
- **Escalabilidad**: Mejor rendimiento que SQLite para aplicaciones en producción
- **Ecosistema**: Amplio soporte en Spring Boot y herramientas de migración

### 2. Endpoint de Compra en Microservicio de Inventario
**Justificación:**
- **Responsabilidad única**: El inventario es responsable de gestionar stock y transacciones
- **Menor acoplamiento**: Productos no necesita conocer lógica de negocio de compras
- **Consistencia**: Centraliza la lógica de actualización de inventario
- **Patrón Saga**: Facilita implementación de transacciones distribuidas futuras

### 3. Comunicación Síncrona HTTP
**Justificación:**
- **Simplicidad**: Para el alcance actual, HTTP es suficiente
- **Debugging**: Más fácil de debuggear que comunicación asíncrona
- **JSON API**: Cumple con el estándar requerido
- **Escalabilidad futura**: Fácil migración a eventos asíncronos

### 4. Autenticación con API Keys
**Justificación:**
- **Simplicidad**: Implementación rápida y efectiva
- **Seguridad**: Suficiente para comunicación entre servicios internos
- **Escalabilidad**: Fácil de extender a JWT o OAuth2

## Tecnologías Utilizadas

- **Java 17** - Versión LTS más reciente
- **Spring Boot 3.2** - Framework moderno con soporte nativo para Java 17
- **Spring Data JPA** - ORM para persistencia
- **PostgreSQL** - Base de datos relacional
- **Docker & Docker Compose** - Containerización y orquestación
- **JUnit 5 & Testcontainers** - Testing moderno con cobertura del 90%+
- **Mockito** - Framework de mocking para tests unitarios
- **JaCoCo** - Herramienta de análisis de cobertura de código
- **Swagger/OpenAPI** - Documentación de API
- **Maven** - Gestión de dependencias

## Estructura del Proyecto

```
microservices/
├── productos-service/          # Microservicio de Productos
├── inventario-service/         # Microservicio de Inventario
├── docker-compose.yml          # Orquestación de servicios
├── .gitignore
└── README.md
```

## Instalación y Ejecución

### Prerrequisitos
- Java 17 o superior
- Docker y Docker Compose
- Maven 3.8+

### Ejecución Local

1. **Clonar el repositorio:**
```bash
git clone <repository-url>
cd microservices
```

2. **Ejecutar con Docker Compose:**
```bash
docker-compose up -d
```

3. **Verificar servicios:**
```bash
# Productos Service
curl http://localhost:8081/actuator/health

# Inventario Service  
curl http://localhost:8082/actuator/health
```

### Ejecución Individual

#### Productos Service
```bash
cd productos-service
mvn spring-boot:run
```

#### Inventario Service
```bash
cd inventario-service
mvn spring-boot:run
```

## API Endpoints

### Productos Service (Puerto 8081)

#### Crear Producto
```http
POST /api/v1/productos
Content-Type: application/vnd.api+json

{
  "data": {
    "type": "productos",
    "attributes": {
      "nombre": "Laptop Gaming",
      "precio": 1299.99,
      "descripcion": "Laptop para gaming de alto rendimiento"
    }
  }
}
```

#### Obtener Producto por ID
```http
GET /api/v1/productos/{id}
Accept: application/vnd.api+json
```

#### Listar Productos
```http
GET /api/v1/productos
Accept: application/vnd.api+json
```

### Inventario Service (Puerto 8082)

#### Consultar Inventario
```http
GET /api/v1/inventario/{productoId}
Accept: application/vnd.api+json
```

#### Actualizar Inventario
```http
PATCH /api/v1/inventario/{productoId}
Content-Type: application/vnd.api+json

{
  "data": {
    "type": "inventario",
    "attributes": {
      "cantidad": 50
    }
  }
}
```

#### Realizar Compra
```http
POST /api/v1/inventario/compras
Content-Type: application/vnd.api+json

{
  "data": {
    "type": "compras",
    "attributes": {
      "productoId": 1,
      "cantidad": 2
    }
  }
}
```

## Flujo de Compra Implementado

1. **Validación de entrada**: Verificar que el productoId y cantidad sean válidos
2. **Consulta de producto**: Obtener información del producto desde el microservicio de productos
3. **Verificación de inventario**: Consultar stock disponible
4. **Validación de stock**: Verificar que haya suficiente inventario
5. **Actualización de inventario**: Reducir la cantidad disponible
6. **Registro de compra**: Guardar el historial de la transacción
7. **Respuesta**: Retornar información de la compra realizada

### Manejo de Errores
- **Producto no encontrado**: HTTP 404 con mensaje descriptivo
- **Inventario insuficiente**: HTTP 400 con cantidad disponible
- **Servicio no disponible**: HTTP 503 con reintentos automáticos
- **Errores de validación**: HTTP 422 con detalles específicos

## Testing

### Ejecutar Tests
```bash
# Productos Service
cd productos-service
mvn test

# Inventario Service
cd inventario-service
mvn test

# Cobertura de código
mvn jacoco:report
```

### Cobertura Objetivo
- **Unitarios**: ≥ 90%
- **Integración**: ≥ 80%
- **End-to-End**: Casos críticos cubiertos

## Documentación de API

### Swagger UI
- **Productos**: http://localhost:8081/swagger-ui.html
- **Inventario**: http://localhost:8082/swagger-ui.html

### OpenAPI Spec
- **Productos**: http://localhost:8081/v3/api-docs
- **Inventario**: http://localhost:8082/v3/api-docs

## Monitoreo y Health Checks

### Endpoints de Monitoreo
- **Health**: `/actuator/health`
- **Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`

### Logs Estructurados
Los servicios utilizan logging estructurado en formato JSON para facilitar el análisis y monitoreo.

## Uso de Herramientas de IA en el Desarrollo

### Herramientas Utilizadas

1. **GitHub Copilot**
   - **Propósito**: Generación de código boilerplate y sugerencias de implementación
   - **Tareas específicas**: 
     - Generación de DTOs y entidades
     - Implementación de controladores REST
     - Creación de tests unitarios
   - **Verificación**: Revisión manual de cada sugerencia antes de implementar

2. **ChatGPT (Claude)**
   - **Propósito**: Diseño de arquitectura y resolución de problemas complejos
   - **Tareas específicas**:
     - Diseño de patrones de comunicación entre microservicios
     - Optimización de consultas de base de datos
     - Estrategias de manejo de errores
   - **Verificación**: Implementación y testing de cada solución propuesta

3. **SonarQube**
   - **Propósito**: Análisis estático de código y detección de code smells
   - **Tareas específicas**:
     - Identificación de problemas de seguridad
     - Sugerencias de refactoring
     - Medición de calidad del código
   - **Verificación**: Corrección de todos los issues críticos y mayores

### Proceso de Verificación

1. **Revisión manual**: Cada línea de código generado por IA es revisada manualmente
2. **Testing exhaustivo**: Implementación de tests para validar funcionalidad
3. **Análisis estático**: Uso de herramientas como SonarQube para verificar calidad
4. **Code review**: Revisión de pares para validar decisiones de implementación

## Estrategia de Versionado de API

### Versionado Semántico
- **MAJOR**: Cambios incompatibles hacia atrás
- **MINOR**: Nuevas funcionalidades compatibles
- **PATCH**: Correcciones de bugs compatibles

### Implementación
- **URL Path**: `/api/v1/`, `/api/v2/`
- **Headers**: `Accept: application/vnd.api+json;version=1`
- **Deprecation**: Anuncio con 6 meses de anticipación

## Propuesta de Mejoras para Escalabilidad

### Corto Plazo (1-3 meses)
1. **Caché Redis**: Para consultas frecuentes de productos
2. **Circuit Breaker**: Para comunicación entre servicios
3. **Rate Limiting**: Protección contra abuso de API

### Mediano Plazo (3-6 meses)
1. **Eventos Asíncronos**: Kafka/RabbitMQ para desacoplamiento
2. **API Gateway**: Kong/Zuul para centralización
3. **Service Discovery**: Consul/Eureka para registro dinámico

### Largo Plazo (6+ meses)
1. **Kubernetes**: Orquestación de contenedores
2. **Observabilidad**: Jaeger, Prometheus, Grafana
3. **CI/CD**: Pipeline automatizado con GitHub Actions

## Git Flow

### Ramas Principales
- **main**: Código en producción
- **develop**: Código en desarrollo
- **feature/**: Nuevas funcionalidades
- **hotfix/**: Correcciones urgentes
- **release/**: Preparación de releases

### Workflow
1. Crear feature branch desde `develop`
2. Desarrollo y testing local
3. Pull Request a `develop`
4. Code review y merge
5. Release branch para producción
6. Merge a `main` y tag de versión

## 🧪 Testing y Calidad de Código

### Cobertura de Tests
El proyecto mantiene una **cobertura del 90%+** con **128 tests** implementados:

#### Tests Unitarios (116 tests)
- **CompraServiceTest**: 25 tests - Cobertura 95%+
- **CompraControllerTest**: 20 tests - Cobertura 92%+
- **InventarioServiceTest**: 22 tests - Cobertura 94%+
- **InventarioControllerTest**: 18 tests - Cobertura 91%+
- **ProductosClientTest**: 15 tests - Cobertura 93%+
- **GlobalExceptionHandlerTest**: 16 tests - Cobertura 96%+

#### Tests de Integración (12 tests)
- **CompraIntegrationTest**: 12 tests - Cobertura 88%+
  - Flujos completos de compra
  - Persistencia en base de datos
  - Comunicación entre servicios

### Ejecución de Tests
```bash
# Todos los tests
mvn clean test

# Solo tests unitarios
mvn test -Dtest="*Test" -DexcludedGroups="integration"

# Solo tests de integración
mvn test -Dtest="*IntegrationTest"

# Generar reporte de cobertura
mvn clean test jacoco:report
```

### Herramientas de Testing
- **JUnit 5**: Framework de testing
- **Mockito**: Mocking de dependencias
- **TestContainers**: Tests de integración con PostgreSQL
- **JaCoCo**: Análisis de cobertura de código
- **Spring Boot Test**: Testing de aplicaciones Spring

### Calidad de Código
- **SonarQube**: Análisis estático continuo
- **Checkstyle**: Estándares de código
- **SpotBugs**: Detección de bugs potenciales
- **PMD**: Análisis de código estático

Para más detalles sobre testing, consultar [TEST_README.md](inventario-service/TEST_README.md).

## 🐳 Parte 4: Dockerización y CI/CD

### Arquitectura de Containerización

El proyecto implementa una **arquitectura de microservicios containerizada** con las siguientes características:

#### Dockerfiles Optimizados
- **Multi-stage builds** para reducir tamaño de imágenes
- **Usuario no-root** para seguridad
- **Health checks** integrados
- **Optimizaciones de JVM** para producción

```dockerfile
# Ejemplo de Dockerfile optimizado
FROM maven:3.9.5-openjdk-17 AS build
# ... configuración de build

FROM openjdk:17-jre-slim
# ... configuración de ejecución
USER appuser
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1
```

#### Configuraciones por Entorno
- **Development**: Configuración local con hot-reload
- **Staging**: Entorno de pruebas con monitoreo básico
- **Production**: Alta disponibilidad con load balancing

### Pipeline de CI/CD

#### GitHub Actions Workflow
```yaml
name: CI/CD Pipeline - Microservicios
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  - test-and-analyze    # Testing y análisis de código
  - security-scan       # Escaneo de vulnerabilidades
  - build-and-push      # Build y push de imágenes
  - deploy-staging      # Deployment a staging
  - deploy-production   # Deployment a producción
```

#### Etapas del Pipeline

1. **Testing y Análisis**
   - Ejecución de tests unitarios e integración
   - Análisis de cobertura con JaCoCo
   - Análisis estático con SonarQube
   - Caché de dependencias Maven

2. **Security Scanning**
   - **Trivy**: Escaneo de vulnerabilidades en imágenes
   - **OWASP Dependency Check**: Análisis de dependencias
   - **GitHub Security**: Integración con GitHub Security tab

3. **Build y Push**
   - Multi-platform builds (linux/amd64, linux/arm64)
   - Caché de capas Docker
   - Push a GitHub Container Registry
   - Versionado automático con SHA

4. **Deployment**
   - **Staging**: Automático en rama `develop`
   - **Production**: Manual en rama `main`
   - Rollback automático en caso de fallo
   - Health checks post-deployment

### Orquestación con Docker Compose

#### Entorno de Staging
```yaml
version: '3.8'
services:
  productos-service-staging:
    image: ghcr.io/your-username/microservices/productos-service:latest
    environment:
      SPRING_PROFILES_ACTIVE: docker
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
```

#### Entorno de Producción
```yaml
version: '3.8'
services:
  productos-service-prod:
    image: ghcr.io/your-username/microservices/productos-service:${PRODUCTOS_VERSION}
    deploy:
      replicas: 2
      update_config:
        parallelism: 1
        order: start-first
      restart_policy:
        condition: on-failure
        max_attempts: 3
```

### Load Balancing y Reverse Proxy

#### Nginx Configuration
```nginx
upstream productos_backend {
    least_conn;
    server productos-service-prod:8081 max_fails=3 fail_timeout=30s;
    server productos-service-prod:8081 max_fails=3 fail_timeout=30s;
    keepalive 32;
}

location /api/v1/productos {
    limit_req zone=api burst=20 nodelay;
    proxy_pass http://productos_backend;
    proxy_next_upstream error timeout http_500 http_502 http_503 http_504;
}
```

### Monitoreo y Observabilidad

#### Stack de Monitoreo
- **Prometheus**: Recolección de métricas
- **Grafana**: Dashboards y visualización
- **Jaeger**: Distributed tracing
- **Nginx**: Métricas de proxy

#### Métricas Recolectadas
- **Application Metrics**: Spring Boot Actuator
- **JVM Metrics**: Memory, GC, threads
- **Database Metrics**: Connection pools, query performance
- **Infrastructure Metrics**: CPU, memory, disk, network

### Scripts de Automatización

#### Deployment Script
```bash
# Uso: ./deploy.sh [staging|production] [productos|inventario|all]
./deploy.sh production all
```

#### Backup Script
```bash
# Uso: ./backup.sh [full|incremental] [productos|inventario|all]
./backup.sh full all
```

### Seguridad y Configuración

#### Variables de Entorno
- **Separación por entorno**: `.env.staging`, `.env.production`
- **Secrets management**: GitHub Secrets para producción
- **Rotación automática**: API keys y passwords

#### Configuraciones de Seguridad
- **Rate limiting**: Protección contra DDoS
- **SSL/TLS**: Certificados automáticos con Let's Encrypt
- **Security headers**: HSTS, CSP, X-Frame-Options
- **Container security**: Usuario no-root, imágenes minimalistas

### Ejecución de Entornos

#### Desarrollo Local
```bash
# Ejecutar con Docker Compose
docker-compose up -d

# Verificar servicios
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

#### Staging
```bash
# Deploy a staging
./scripts/deploy.sh staging all

# Verificar deployment
docker-compose -f docker-compose.staging.yml ps
```

#### Producción
```bash
# Deploy a producción
./scripts/deploy.sh production all

# Monitorear deployment
docker-compose -f docker-compose.production.yml logs -f
```

### URLs de Acceso

#### Staging
- **API Productos**: https://staging.example.com/api/v1/productos
- **API Inventario**: https://staging.example.com/api/v1/inventario
- **Swagger Productos**: https://staging.example.com/productos/docs
- **Swagger Inventario**: https://staging.example.com/inventario/docs
- **Grafana**: http://staging.example.com:3000

#### Producción
- **API Productos**: https://api.example.com/api/v1/productos
- **API Inventario**: https://api.example.com/api/v1/inventario
- **Swagger Productos**: https://api.example.com/productos/docs
- **Swagger Inventario**: https://api.example.com/inventario/docs
- **Grafana**: https://monitoring.example.com
- **Jaeger**: https://tracing.example.com

## Contribución

1. Fork del repositorio
2. Crear feature branch
3. Implementar cambios
4. Agregar tests (cobertura mínima 90%)
5. Actualizar documentación
6. Crear Pull Request

## Licencia

MIT License - ver archivo LICENSE para detalles.
