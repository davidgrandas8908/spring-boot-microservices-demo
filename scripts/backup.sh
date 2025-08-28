#!/bin/bash

# Script de Backup Automatizado para Microservicios
# Uso: ./backup.sh [full|incremental] [productos|inventario|all]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
BACKUP_TYPE=${1:-full}
DATABASE=${2:-all}
BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=${BACKUP_RETENTION_DAYS:-30}

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
if [[ ! "$BACKUP_TYPE" =~ ^(full|incremental)$ ]]; then
    log_error "Tipo de backup inválido. Use 'full' o 'incremental'"
    exit 1
fi

if [[ ! "$DATABASE" =~ ^(productos|inventario|all)$ ]]; then
    log_error "Base de datos inválida. Use 'productos', 'inventario' o 'all'"
    exit 1
fi

# Crear directorio de backup si no existe
mkdir -p "$BACKUP_DIR"

log_info "Iniciando backup tipo: $BACKUP_TYPE"
log_info "Base(s) de datos: $DATABASE"
log_info "Directorio de backup: $BACKUP_DIR"

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

# Función para backup de Productos
backup_productos() {
    log_info "Iniciando backup de base de datos Productos..."
    
    local backup_file="$BACKUP_DIR/productos_db_${BACKUP_TYPE}_${DATE}.sql"
    
    if docker exec productos-db-prod pg_dump -U productos_user_prod productos_db_prod > "$backup_file"; then
        log_success "Backup de Productos completado: $backup_file"
        
        # Comprimir backup
        gzip "$backup_file"
        log_success "Backup comprimido: ${backup_file}.gz"
        
        # Verificar integridad
        if gunzip -t "${backup_file}.gz"; then
            log_success "Integridad del backup verificada"
        else
            log_error "Error en la integridad del backup"
            exit 1
        fi
    else
        log_error "Error en el backup de Productos"
        exit 1
    fi
}

# Función para backup de Inventario
backup_inventario() {
    log_info "Iniciando backup de base de datos Inventario..."
    
    local backup_file="$BACKUP_DIR/inventario_db_${BACKUP_TYPE}_${DATE}.sql"
    
    if docker exec inventario-db-prod pg_dump -U inventario_user_prod inventario_db_prod > "$backup_file"; then
        log_success "Backup de Inventario completado: $backup_file"
        
        # Comprimir backup
        gzip "$backup_file"
        log_success "Backup comprimido: ${backup_file}.gz"
        
        # Verificar integridad
        if gunzip -t "${backup_file}.gz"; then
            log_success "Integridad del backup verificada"
        else
            log_error "Error en la integridad del backup"
            exit 1
        fi
    else
        log_error "Error en el backup de Inventario"
        exit 1
    fi
}

# Función para limpiar backups antiguos
cleanup_old_backups() {
    log_info "Limpiando backups antiguos (más de $RETENTION_DAYS días)..."
    
    local deleted_count=0
    
    # Encontrar y eliminar backups antiguos
    while IFS= read -r -d '' file; do
        if [[ -f "$file" ]]; then
            rm "$file"
            ((deleted_count++))
            log_info "Eliminado: $(basename "$file")"
        fi
    done < <(find "$BACKUP_DIR" -name "*.sql.gz" -mtime +$RETENTION_DAYS -print0)
    
    log_success "Limpieza completada. $deleted_count archivos eliminados"
}

# Función para verificar espacio en disco
check_disk_space() {
    log_info "Verificando espacio en disco..."
    
    local available_space=$(df "$BACKUP_DIR" | awk 'NR==2 {print $4}')
    local required_space=1048576  # 1GB en KB
    
    if [ "$available_space" -lt "$required_space" ]; then
        log_warning "Espacio en disco insuficiente. Disponible: ${available_space}KB, Requerido: ${required_space}KB"
        cleanup_old_backups
    else
        log_success "Espacio en disco suficiente"
    fi
}

# Función para generar reporte de backup
generate_backup_report() {
    log_info "Generando reporte de backup..."
    
    local report_file="$BACKUP_DIR/backup_report_${DATE}.txt"
    
    cat > "$report_file" << EOF
=== REPORTE DE BACKUP ===
Fecha: $(date)
Tipo: $BACKUP_TYPE
Base de datos: $DATABASE
Directorio: $BACKUP_DIR

=== ARCHIVOS CREADOS ===
$(find "$BACKUP_DIR" -name "*${DATE}*" -type f -exec ls -lh {} \; 2>/dev/null || echo "No se encontraron archivos")

=== RESUMEN DE BACKUPS ===
$(find "$BACKUP_DIR" -name "*.sql.gz" -type f -exec ls -lh {} \; 2>/dev/null | wc -l) backups totales
$(du -sh "$BACKUP_DIR" 2>/dev/null || echo "0B") tamaño total

=== ÚLTIMOS BACKUPS ===
$(find "$BACKUP_DIR" -name "*.sql.gz" -type f -exec ls -lt {} \; 2>/dev/null | head -10)

EOF
    
    log_success "Reporte generado: $report_file"
}

# Función para enviar notificación
send_notification() {
    local status=$1
    local message=$2
    
    log_info "Enviando notificación: $status"
    
    # Aquí podrías integrar con servicios como:
    # - Slack webhook
    # - Email
    # - SMS
    # - Teams webhook
    
    echo "Backup $status: $message" >> "$BACKUP_DIR/backup.log"
}

# Función principal
main() {
    log_info "=== Script de Backup de Microservicios ==="
    
    check_prerequisites
    check_disk_space
    
    # Ejecutar backups según parámetros
    case $DATABASE in
        productos)
            backup_productos
            ;;
        inventario)
            backup_inventario
            ;;
        all)
            backup_productos
            backup_inventario
            ;;
    esac
    
    cleanup_old_backups
    generate_backup_report
    
    log_success "Backup completado exitosamente"
    send_notification "SUCCESS" "Backup $BACKUP_TYPE completado para $DATABASE"
    
    # Mostrar información del backup
    echo ""
    log_info "Información del backup:"
    echo "  - Tipo: $BACKUP_TYPE"
    echo "  - Base de datos: $DATABASE"
    echo "  - Fecha: $DATE"
    echo "  - Directorio: $BACKUP_DIR"
    echo "  - Retención: $RETENTION_DAYS días"
    echo ""
    log_info "Para restaurar: ./restore.sh <archivo_backup>"
}

# Ejecutar función principal
main

