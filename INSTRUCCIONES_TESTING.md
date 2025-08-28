# 🧪 Instrucciones de Testing - Microservicios de Productos e Inventario

## 📋 Prerrequisitos para Testing

Antes de ejecutar los tests, asegúrate de tener:

- **Java 17** o superior
- **Maven 3.8+**
- **Docker Desktop** (para bases de datos de test)

### Verificar instalaciones:
```bash
java --version
mvn --version
docker --version
```

## 🎯 Ejecución de Tests

### Opción 1: Tests con Docker (Recomendado)

#### Paso 1: Ejecutar bases de datos de test
```bash
# Ejecutar solo las bases de datos
docker-compose up -d productos-db inventario-db

# Verificar que estén funcionando
docker-compose ps
```

#### Paso 2: Ejecutar tests del servicio de productos
```bash
cd productos-service
mvn clean test
```

#### Paso 3: Ejecutar tests del servicio de inventario
```bash
cd inventario-service
mvn clean test
```

### Opción 2: Tests con TestContainers (Automático)

Los tests están configurados para usar TestContainers, que automáticamente:
- Inicia contenedores de PostgreSQL
- Ejecuta los tests
- Limpia los contenedores al finalizar

```bash
# Productos Service
cd productos-service
mvn clean test

# Inventario Service
cd inventario-service
mvn clean test
```

## 📊 Reportes de Cobertura

### Generar reportes de cobertura
```bash
# Productos Service
cd productos-service
mvn clean test jacoco:report

# Inventario Service
cd inventario-service
mvn clean test jacoco:report
```

### Ver reportes
- **Productos Service**: Abrir `productos-service/target/site/jacoco/index.html`
- **Inventario Service**: Abrir `inventario-service/target/site/jacoco/index.html`

## 🎯 Tipos de Tests

### Tests Unitarios
```bash
# Solo tests unitarios
mvn test -Dtest="*Test" -DexcludedGroups="integration"
```

### Tests de Integración
```bash
# Solo tests de integración
mvn test -Dtest="*IntegrationTest"
```

### Tests Específicos
```bash
# Test específico
mvn test -Dtest=ProductoServiceTest

# Método específico
mvn test -Dtest=ProductoServiceTest#crearProducto
```

## 📈 Métricas de Cobertura

### Cobertura Objetivo
- **Cobertura Total**: ≥ 90%
- **Tests Unitarios**: ≥ 95%
- **Tests de Integración**: ≥ 80%

### Verificar cobertura actual
```bash
# Productos Service
cd productos-service
mvn clean test jacoco:report
echo "Cobertura Productos:"
cat target/site/jacoco/jacoco.csv | grep "TOTAL"

# Inventario Service
cd inventario-service
mvn clean test jacoco:report
echo "Cobertura Inventario:"
cat target/site/jacoco/jacoco.csv | grep "TOTAL"
```

## 🔧 Configuración de Tests

### Variables de entorno para tests
```bash
# Configuración automática en application-test.yml
SPRING_PROFILES_ACTIVE=test
```

### Configuración de TestContainers
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:tc:postgresql:15-alpine:///testdb
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
```

## 🚀 Tests de Rendimiento

### Ejecutar tests de rendimiento
```bash
# Tests de carga (si están configurados)
mvn test -Dtest="*PerformanceTest"
```

### Métricas de rendimiento
- **Tiempo de respuesta**: < 200ms
- **Throughput**: > 1000 req/s
- **Memoria**: < 512MB

## 🐛 Debugging de Tests

### Ejecutar tests con debug
```bash
mvn test -X
```

### Ver logs detallados
```bash
mvn test -Dspring.profiles.active=test -Dlogging.level.com.microservices=DEBUG
```

### Ejecutar test específico con debug
```bash
mvn test -Dtest=ProductoServiceTest#crearProducto -X
```

## 📋 Checklist de Testing

### Antes de ejecutar tests
- [ ] Docker Desktop está ejecutándose
- [ ] Puertos 5432 y 5433 están libres
- [ ] Maven está configurado correctamente
- [ ] Java 17 está instalado

### Durante la ejecución
- [ ] Todos los tests pasan (verde)
- [ ] Cobertura ≥ 90%
- [ ] No hay warnings críticos
- [ ] Tests de integración funcionan

### Después de la ejecución
- [ ] Reportes de cobertura generados
- [ ] Logs sin errores críticos
- [ ] Contenedores de test limpiados

## 🔍 Troubleshooting de Tests

### Problema: Tests fallan por conexión a base de datos
```bash
# Verificar que Docker esté ejecutándose
docker info

# Reiniciar contenedores de test
docker-compose down
docker-compose up -d productos-db inventario-db

# Esperar que las bases de datos estén listas
sleep 10
```

### Problema: Tests de integración fallan
```bash
# Limpiar y reinstalar dependencias
mvn clean install

# Ejecutar con más memoria
mvn test -Xmx2g

# Verificar configuración de TestContainers
mvn test -Dtestcontainers.reuse.enable=true
```

### Problema: Cobertura baja
```bash
# Verificar qué clases no están cubiertas
cat target/site/jacoco/jacoco.csv | grep ",0,"

# Ejecutar tests específicos para clases no cubiertas
mvn test -Dtest="*ServiceTest,*ControllerTest"
```

### Problema: Tests lentos
```bash
# Usar TestContainers con reutilización
mvn test -Dtestcontainers.reuse.enable=true

# Ejecutar tests en paralelo
mvn test -T 1C
```

## 📊 Comandos Útiles

### Ver resumen de tests
```bash
# Resumen rápido
mvn test -q

# Con detalles
mvn test -X
```

### Limpiar y reinstalar
```bash
# Limpiar todo
mvn clean

# Reinstalar dependencias
mvn clean install
```

### Ejecutar tests en modo offline
```bash
mvn test -o
```

## 🎯 Tests Específicos por Servicio

### Productos Service
```bash
cd productos-service

# Tests unitarios
mvn test -Dtest="*ServiceTest"
mvn test -Dtest="*ControllerTest"

# Tests de integración
mvn test -Dtest="*IntegrationTest"

# Tests de cliente
mvn test -Dtest="*ClientTest"
```

### Inventario Service
```bash
cd inventario-service

# Tests unitarios
mvn test -Dtest="*ServiceTest"
mvn test -Dtest="*ControllerTest"

# Tests de integración
mvn test -Dtest="*IntegrationTest"

# Tests de cliente
mvn test -Dtest="*ClientTest"
```

## 📈 Monitoreo de Tests

### Ver estadísticas de tests
```bash
# Ver reporte de tests
cat target/surefire-reports/TEST-*.xml

# Ver cobertura
cat target/site/jacoco/jacoco.csv
```

### Métricas importantes
- **Tests ejecutados**: Total de tests
- **Tests exitosos**: Tests que pasan
- **Tests fallidos**: Tests que fallan
- **Tiempo de ejecución**: Tiempo total
- **Cobertura de líneas**: Porcentaje de líneas cubiertas
- **Cobertura de ramas**: Porcentaje de ramas cubiertas

---

**¡Listo!** 🎉 Tus tests están ejecutándose correctamente y tienes una cobertura del 90%+.
