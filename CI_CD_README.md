# 🐳 CI/CD y Dockerización - Microservicios

## Descripción General

Este documento describe la implementación completa de **CI/CD (Continuous Integration/Continuous Deployment)** y **Dockerización** para los microservicios de Productos e Inventario.

## 🏗️ Arquitectura de Containerización

### Dockerfiles Optimizados

#### Características Principales
- **Multi-stage builds** para reducir tamaño de imágenes
- **Usuario no-root** para seguridad
- **Health checks** integrados
- **Optimizaciones de JVM** para producción
- **Caché de dependencias** para builds más rápidos

#### Estructura de Dockerfile
```dockerfile
# Etapa de construcción
FROM maven:3.9.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:17-jre-slim
RUN apt-get update && apt-get install -y curl
RUN groupadd -r appuser && useradd -r -g appuser appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser
EXPOSE 8081
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Configuraciones por Entorno

#### Development
- Hot-reload habilitado
- Logging detallado
- Configuración local de base de datos

#### Staging
- Configuración de pruebas
- Monitoreo básico
- Datos de prueba

#### Production
- Alta disponibilidad
- Load balancing
- Monitoreo completo
- Optimizaciones de rendimiento

## 🔄 Pipeline de CI/CD

### GitHub Actions Workflow

#### Trigger Events
```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
```

#### Jobs del Pipeline

1. **test-and-analyze**
   - Testing unitario e integración
   - Análisis de cobertura
   - Análisis estático de código

2. **security-scan**
   - Escaneo de vulnerabilidades
   - Análisis de dependencias
   - Verificación de seguridad

3. **build-and-push**
   - Construcción de imágenes Docker
   - Push a registry
   - Versionado automático

4. **deploy-staging**
   - Deployment automático a staging
   - Smoke tests
   - Verificación de health checks

5. **deploy-production**
   - Deployment manual a producción
   - Rollback automático
   - Monitoreo post-deployment

### Etapas Detalladas

#### 1. Testing y Análisis
```yaml
- name: Test Productos Service
  working-directory: ./productos-service
  run: |
    mvn clean test jacoco:report
    mvn sonar:sonar \
      -Dsonar.projectKey=productos-service \
      -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }} \
      -Dsonar.login=${{ secrets.SONAR_TOKEN }}
```

#### 2. Security Scanning
```yaml
- name: Run Trivy vulnerability scanner
  uses: aquasecurity/trivy-action@master
  with:
    scan-type: 'fs'
    scan-ref: '.'
    format: 'sarif'
    output: 'trivy-results.sarif'
```

#### 3. Build y Push
```yaml
- name: Build and push Productos Service
  uses: docker/build-push-action@v5
  with:
    context: ./productos-service
    push: true
    tags: |
      ${{ env.REGISTRY }}/${{ env.IMAGE_NAME_PRODUCTOS }}:latest
      ${{ env.REGISTRY }}/${{ env.IMAGE_NAME_PRODUCTOS }}:${{ github.sha }}
    cache-from: type=gha
    cache-to: type=gha,mode=max
    platforms: linux/amd64,linux/arm64
```

## 🐳 Orquestación con Docker Compose

### Entorno de Staging

#### Características
- Configuración de pruebas
- Monitoreo básico
- Datos de prueba
- Redis para caché

#### Servicios Incluidos
- `productos-service-staging`
- `inventario-service-staging`
- `productos-db-staging`
- `inventario-db-staging`
- `nginx-staging`
- `redis-staging`

### Entorno de Producción

#### Características
- Alta disponibilidad
- Load balancing
- Monitoreo completo
- Backup automático

#### Servicios Incluidos
- `productos-service-prod` (2 réplicas)
- `inventario-service-prod` (2 réplicas)
- `productos-db-prod`
- `inventario-db-prod`
- `nginx-prod` (2 réplicas)
- `redis-prod`
- `prometheus`
- `grafana`
- `jaeger`

## 🔄 Load Balancing y Reverse Proxy

### Configuración de Nginx

#### Upstream Configuration
```nginx
upstream productos_backend {
    least_conn;
    server productos-service-prod:8081 max_fails=3 fail_timeout=30s;
    server productos-service-prod:8081 max_fails=3 fail_timeout=30s;
    keepalive 32;
}
```

#### Rate Limiting
```nginx
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

location /api/v1/productos {
    limit_req zone=api burst=20 nodelay;
    proxy_pass http://productos_backend;
}
```

#### SSL/TLS Configuration
```nginx
ssl_protocols TLSv1.2 TLSv1.3;
ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
ssl_prefer_server_ciphers off;
ssl_session_cache shared:SSL:10m;
```

## 📊 Monitoreo y Observabilidad

### Stack de Monitoreo

#### Prometheus
- Recolección de métricas de aplicaciones
- Métricas de infraestructura
- Alertas configurables

#### Grafana
- Dashboards personalizados
- Visualización de métricas
- Alertas y notificaciones

#### Jaeger
- Distributed tracing
- Análisis de performance
- Debugging de microservicios

### Métricas Recolectadas

#### Application Metrics
- Request rate
- Response time
- Error rate
- JVM metrics

#### Infrastructure Metrics
- CPU usage
- Memory usage
- Disk I/O
- Network traffic

#### Database Metrics
- Connection pools
- Query performance
- Lock contention
- Transaction rate

## 🔧 Scripts de Automatización

### Deployment Script

#### Uso
```bash
./scripts/deploy.sh [staging|production] [productos|inventario|all]
```

#### Ejemplos
```bash
# Deploy todos los servicios a staging
./scripts/deploy.sh staging all

# Deploy solo productos a producción
./scripts/deploy.sh production productos

# Deploy con logs
./scripts/deploy.sh staging all --logs
```

#### Características
- Verificación de prerequisitos
- Backup automático
- Rollback en caso de fallo
- Health checks
- Limpieza de recursos

### Backup Script

#### Uso
```bash
./scripts/backup.sh [full|incremental] [productos|inventario|all]
```

#### Ejemplos
```bash
# Backup completo de todas las bases de datos
./scripts/backup.sh full all

# Backup incremental de productos
./scripts/backup.sh incremental productos
```

#### Características
- Backup comprimido
- Verificación de integridad
- Limpieza automática
- Reportes detallados
- Notificaciones

## 🔒 Seguridad y Configuración

### Variables de Entorno

#### Separación por Entorno
- `.env.development` - Desarrollo local
- `.env.staging` - Entorno de pruebas
- `.env.production` - Producción

#### Secrets Management
- GitHub Secrets para producción
- Variables de entorno encriptadas
- Rotación automática de credenciales

### Configuraciones de Seguridad

#### Rate Limiting
- Protección contra DDoS
- Configuración por endpoint
- Burst handling

#### SSL/TLS
- Certificados automáticos
- HSTS headers
- Perfect Forward Secrecy

#### Container Security
- Usuario no-root
- Imágenes minimalistas
- Escaneo de vulnerabilidades
- Actualizaciones automáticas

## 🚀 Ejecución de Entornos

### Desarrollo Local
```bash
# Ejecutar con Docker Compose
docker-compose up -d

# Verificar servicios
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# Ver logs
docker-compose logs -f
```

### Staging
```bash
# Deploy a staging
./scripts/deploy.sh staging all

# Verificar deployment
docker-compose -f docker-compose.staging.yml ps

# Acceder a servicios
curl https://staging.example.com/api/v1/productos
```

### Producción
```bash
# Deploy a producción
./scripts/deploy.sh production all

# Monitorear deployment
docker-compose -f docker-compose.production.yml logs -f

# Verificar métricas
curl http://localhost:9090/api/v1/targets
```

## 📋 URLs de Acceso

### Staging
- **API Productos**: https://staging.example.com/api/v1/productos
- **API Inventario**: https://staging.example.com/api/v1/inventario
- **Swagger Productos**: https://staging.example.com/productos/docs
- **Swagger Inventario**: https://staging.example.com/inventario/docs
- **Grafana**: http://staging.example.com:3000

### Producción
- **API Productos**: https://api.example.com/api/v1/productos
- **API Inventario**: https://api.example.com/api/v1/inventario
- **Swagger Productos**: https://api.example.com/productos/docs
- **Swagger Inventario**: https://api.example.com/inventario/docs
- **Grafana**: https://monitoring.example.com
- **Jaeger**: https://tracing.example.com
- **Prometheus**: https://monitoring.example.com:9090

## 🔍 Troubleshooting

### Problemas Comunes

#### Servicios no inician
```bash
# Verificar logs
docker-compose logs [service-name]

# Verificar health checks
docker-compose ps

# Reiniciar servicio
docker-compose restart [service-name]
```

#### Problemas de conectividad
```bash
# Verificar red
docker network ls
docker network inspect microservices-network

# Verificar DNS
docker exec [container] nslookup [service]
```

#### Problemas de recursos
```bash
# Verificar uso de recursos
docker stats

# Limpiar recursos no utilizados
docker system prune -f
```

### Logs y Debugging

#### Ver logs en tiempo real
```bash
# Todos los servicios
docker-compose logs -f

# Servicio específico
docker-compose logs -f productos-service
```

#### Debugging de contenedores
```bash
# Entrar al contenedor
docker exec -it [container-name] /bin/bash

# Verificar configuración
docker exec [container-name] env
```

## 📈 Métricas y Alertas

### Dashboards de Grafana

#### Application Dashboard
- Request rate por servicio
- Response time percentiles
- Error rate
- Throughput

#### Infrastructure Dashboard
- CPU y memoria por contenedor
- Disk I/O
- Network traffic
- Container health

#### Database Dashboard
- Connection pools
- Query performance
- Lock contention
- Transaction rate

### Alertas Configuradas

#### Application Alerts
- Error rate > 5%
- Response time > 2s
- Service down
- High memory usage

#### Infrastructure Alerts
- CPU > 80%
- Memory > 90%
- Disk space > 85%
- Container restart

## 🔄 Rollback y Recovery

### Rollback Automático
- Detección de fallos en deployment
- Rollback automático a versión anterior
- Notificaciones de rollback
- Logs de rollback

### Recovery Procedures
- Backup restoration
- Service recovery
- Data consistency checks
- Post-recovery validation

## 📚 Recursos Adicionales

### Documentación
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)

### Herramientas
- [Trivy Vulnerability Scanner](https://aquasecurity.github.io/trivy/)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)
- [SonarQube](https://www.sonarqube.org/)

### Comunidad
- [Docker Community](https://community.docker.com/)
- [GitHub Actions Community](https://github.com/actions/community)
- [Prometheus Community](https://prometheus.io/community/)

