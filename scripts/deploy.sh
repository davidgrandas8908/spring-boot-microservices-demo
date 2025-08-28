#!/bin/bash

# Script de Deployment para Microservicios
# Uso: ./deploy.sh [staging|production] [productos|inventario|all]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
ENVIRONMENT=${1:-staging}
SERVICE=${2:-all}
REGISTRY="ghcr.io"
REPOSITORY="your-username/microservices"

# Funciones de logging
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Validación de parámetros
if [[ ! "$ENVIRONMENT" =~ ^(staging|production)$ ]]; then
    log_error "Entorno inválido. Use 'staging' o 'production'"
    exit 1
fi

if [[ ! "$SERVICE" =~ ^(productos|inventario|all)$ ]]; then
    log_error "Servicio inválido. Use 'productos', 'inventario' o 'all'"
    exit 1
fi

# Configuración por entorno
case $ENVIRONMENT in
    staging)
        DOCKER_COMPOSE_FILE="docker-compose.staging.yml"
        DOMAIN="staging.example.com"
        ;;
    production)
        DOCKER_COMPOSE_FILE="docker-compose.production.yml"
        DOMAIN="api.example.com"
        ;;
esac

log_info "Iniciando deployment en entorno: $ENVIRONMENT"
log_info "Servicio(s) a desplegar: $SERVICE"

# Función para verificar prerequisitos
check_prerequisites() {
    log_info "Verificando prerequisitos..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker no está instalado"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose no está instalado"
        exit 1
    fi
    
    log_success "Prerequisitos verificados"
}

# Función para hacer backup de la versión actual
backup_current_version() {
    log_info "Creando backup de la versión actual..."
    
    if [ -f "$DOCKER_COMPOSE_FILE" ]; then
        cp "$DOCKER_COMPOSE_FILE" "${DOCKER_COMPOSE_FILE}.backup.$(date +%Y%m%d_%H%M%S)"
        log_success "Backup creado"
    fi
}

# Función para actualizar imágenes
update_images() {
    log_info "Actualizando imágenes Docker..."
    
    case $SERVICE in
        productos)
            docker pull "$REGISTRY/$REPOSITORY/productos-service:latest"
            ;;
        inventario)
            docker pull "$REGISTRY/$REPOSITORY/inventario-service:latest"
            ;;
        all)
            docker pull "$REGISTRY/$REPOSITORY/productos-service:latest"
            docker pull "$REGISTRY/$REPOSITORY/inventario-service:latest"
            ;;
    esac
    
    log_success "Imágenes actualizadas"
}

# Función para deployment con rollback
deploy_with_rollback() {
    log_info "Iniciando deployment..."
    
    # Hacer deployment
    if docker-compose -f "$DOCKER_COMPOSE_FILE" up -d; then
        log_success "Deployment iniciado correctamente"
    else
        log_error "Error en el deployment"
        rollback_deployment
        exit 1
    fi
    
    # Esperar a que los servicios estén listos
    wait_for_services
    
    # Verificar health checks
    if check_health; then
        log_success "Deployment completado exitosamente"
    else
        log_error "Health checks fallaron"
        rollback_deployment
        exit 1
    fi
}

# Función para esperar a que los servicios estén listos
wait_for_services() {
    log_info "Esperando a que los servicios estén listos..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if check_health; then
            log_success "Servicios listos"
            return 0
        fi
        
        log_info "Intento $attempt/$max_attempts - Esperando 10 segundos..."
        sleep 10
        ((attempt++))
    done
    
    log_error "Timeout esperando servicios"
    return 1
}

# Función para verificar health checks
check_health() {
    local productos_health=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8081/actuator/health" || echo "000")
    local inventario_health=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8082/actuator/health" || echo "000")
    
    if [ "$productos_health" = "200" ] && [ "$inventario_health" = "200" ]; then
        return 0
    else
        return 1
    fi
}

# Función para rollback
rollback_deployment() {
    log_warning "Iniciando rollback..."
    
    if [ -f "${DOCKER_COMPOSE_FILE}.backup" ]; then
        cp "${DOCKER_COMPOSE_FILE}.backup" "$DOCKER_COMPOSE_FILE"
        docker-compose -f "$DOCKER_COMPOSE_FILE" up -d
        log_success "Rollback completado"
    else
        log_error "No se encontró backup para rollback"
    fi
}

# Función para limpiar recursos no utilizados
cleanup() {
    log_info "Limpiando recursos no utilizados..."
    
    docker system prune -f
    docker image prune -f
    
    log_success "Limpieza completada"
}

# Función para mostrar logs
show_logs() {
    log_info "Mostrando logs de los servicios..."
    
    case $SERVICE in
        productos)
            docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f productos-service
            ;;
        inventario)
            docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f inventario-service
            ;;
        all)
            docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f
            ;;
    esac
}

# Función principal
main() {
    log_info "=== Script de Deployment de Microservicios ==="
    
    check_prerequisites
    backup_current_version
    update_images
    deploy_with_rollback
    cleanup
    
    log_success "Deployment completado exitosamente en $ENVIRONMENT"
    
    # Mostrar información del deployment
    echo ""
    log_info "Información del deployment:"
    echo "  - Entorno: $ENVIRONMENT"
    echo "  - Servicios: $SERVICE"
    echo "  - URLs:"
    echo "    - Productos: http://$DOMAIN:8081"
    echo "    - Inventario: http://$DOMAIN:8082"
    echo "    - Swagger Productos: http://$DOMAIN:8081/swagger-ui.html"
    echo "    - Swagger Inventario: http://$DOMAIN:8082/swagger-ui.html"
    echo ""
    log_info "Para ver logs: ./deploy.sh $ENVIRONMENT $SERVICE --logs"
}

# Manejo de argumentos adicionales
case "${3:-}" in
    --logs)
        show_logs
        ;;
    *)
        main
        ;;
esac

