# 🚀 Instrucciones de Ejecución - Microservicios de Productos e Inventario

## 📋 Prerrequisitos

Antes de comenzar, asegúrate de tener instalado:

- **Docker Desktop** (versión 20.10 o superior)
- **Docker Compose** (incluido con Docker Desktop)
- **Git** (para clonar el repositorio)

### Verificar instalaciones:
```bash
docker --version
docker-compose --version
git --version
```

## 🎯 Opción 1: Ejecución Rápida con Docker (Recomendado)

### Paso 1: Clonar el proyecto
```bash
git clone <URL_DEL_REPOSITORIO>
cd "prueba lider tecnico"
```

### Paso 2: Ejecutar todo el sistema
```bash
docker-compose up -d
```

### Paso 3: Verificar que todo esté funcionando
```bash
# Verificar servicios
docker-compose ps

# Verificar health checks
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

### Paso 4: Acceder a las APIs
- **API Productos**: http://localhost:8081/api/v1/productos
- **API Inventario**: http://localhost:8082/api/v1/inventario
- **Documentación Swagger Productos**: http://localhost:8081/swagger-ui.html
- **Documentación Swagger Inventario**: http://localhost:8082/swagger-ui.html

## 🧪 Opción 2: Ejecución para Testing

### Paso 1: Ejecutar solo las bases de datos
```bash
docker-compose up -d productos-db inventario-db
```

### Paso 2: Ejecutar tests del servicio de productos
```bash
cd productos-service
mvn clean test
```

### Paso 3: Ejecutar tests del servicio de inventario
```bash
cd inventario-service
mvn clean test
```

### Paso 4: Ver reportes de cobertura
```bash
# Productos Service
cd productos-service
mvn jacoco:report
# Abrir: target/site/jacoco/index.html

# Inventario Service
cd inventario-service
mvn jacoco:report
# Abrir: target/site/jacoco/index.html
```

## 🔧 Opción 3: Ejecución Individual de Servicios

### Servicio de Productos
```bash
# Ejecutar base de datos
docker-compose up -d productos-db

# Ejecutar servicio
cd productos-service
mvn spring-boot:run
```

### Servicio de Inventario
```bash
# Ejecutar base de datos
docker-compose up -d inventario-db

# Ejecutar servicio
cd inventario-service
mvn spring-boot:run
```

## 📊 Monitoreo y Logs

### Ver logs en tiempo real
```bash
# Todos los servicios
docker-compose logs -f

# Servicio específico
docker-compose logs -f productos-service
docker-compose logs -f inventario-service
```

### Métricas y Health Checks
```bash
# Health checks
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# Métricas
curl http://localhost:8081/actuator/metrics
curl http://localhost:8082/actuator/metrics
```

## 🛑 Comandos de Parada

### Parar todos los servicios
```bash
docker-compose down
```

### Parar y eliminar volúmenes (datos)
```bash
docker-compose down -v
```

### Parar servicios específicos
```bash
docker-compose stop productos-service
docker-compose stop inventario-service
```

## 🔍 Troubleshooting

### Problema: Puerto ya en uso
```bash
# Verificar qué está usando el puerto
netstat -ano | findstr :8081
netstat -ano | findstr :8082

# Parar proceso específico
taskkill /PID <PID> /F
```

### Problema: Docker no inicia
```bash
# Reiniciar Docker Desktop
# Verificar que Docker esté ejecutándose
docker info
```

### Problema: Base de datos no conecta
```bash
# Verificar logs de base de datos
docker-compose logs productos-db
docker-compose logs inventario-db

# Reiniciar servicios
docker-compose restart productos-db inventario-db
```

### Problema: Tests fallan
```bash
# Limpiar y reinstalar dependencias
mvn clean install

# Ejecutar tests con debug
mvn test -X
```

## 📱 Pruebas Rápidas con cURL

### Crear un producto
```bash
curl -X POST http://localhost:8081/api/v1/productos \
  -H "Content-Type: application/vnd.api+json" \
  -d '{
    "data": {
      "type": "productos",
      "attributes": {
        "nombre": "Laptop Gaming",
        "precio": 1299.99,
        "descripcion": "Laptop para gaming de alto rendimiento"
      }
    }
  }'
```

### Consultar productos
```bash
curl http://localhost:8081/api/v1/productos
```

### Crear inventario
```bash
curl -X POST "http://localhost:8082/api/v1/inventario?productoId=1&cantidad=100"
```

### Realizar compra
```bash
curl -X POST http://localhost:8082/api/v1/compras \
  -H "Content-Type: application/vnd.api+json" \
  -d '{
    "data": {
      "type": "compras",
      "attributes": {
        "productoId": 1,
        "cantidad": 2
      }
    }
  }'
```

## 🎯 URLs Importantes

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Productos API | http://localhost:8081/api/v1/productos | API REST de productos |
| Inventario API | http://localhost:8082/api/v1/inventario | API REST de inventario |
| Compras API | http://localhost:8082/api/v1/compras | API REST de compras |
| Swagger Productos | http://localhost:8081/swagger-ui.html | Documentación API productos |
| Swagger Inventario | http://localhost:8082/swagger-ui.html | Documentación API inventario |
| Health Productos | http://localhost:8081/actuator/health | Estado del servicio productos |
| Health Inventario | http://localhost:8082/actuator/health | Estado del servicio inventario |

## 📞 Soporte

Si encuentras algún problema:

1. **Revisar logs**: `docker-compose logs`
2. **Verificar health checks**: URLs de health mencionadas arriba
3. **Reiniciar servicios**: `docker-compose restart`
4. **Limpiar y reinstalar**: `docker-compose down -v && docker-compose up -d`

---

**¡Listo!** 🎉 Tu sistema de microservicios está ejecutándose correctamente.
