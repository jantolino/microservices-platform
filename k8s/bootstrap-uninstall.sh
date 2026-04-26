#!/bin/bash
# bootstrap-uninstall.sh
# Script para desinstalar los componentes instalados por bootstrap.sh
# Ejecutar desde la raíz del repositorio: ./k8s/bootstrap/bootstrap-uninstall.sh
# Sigue el orden correcto de desinstalación respetando dependencias:
# ArgoCD -> depende de Redis Standalone
# Redis Standalone -> depende de Redis Operator
# Por lo tanto: 1. ArgoCD, 2. Redis Standalone, 3. Redis Operator

set -euo pipefail

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }

# Namespaces creados por Foundation (orden de eliminación: primero los que tienen componentes)
NAMESPACES=(
    "argocd"           # Contiene ArgoCD
    "data-services"    # Contiene Redis Operator y Redis Standalone
    "cert-manager"
    "devops-tools"
    "monitoring"
    "networking"
    "repositories"
    "security"
    "vault"
)

# Funciones auxiliares
namespace_exists() { kubectl get namespace "$1" &>/dev/null; }
resource_exists() { kubectl get "$1" -n "$2" --ignore-not-found &>/dev/null; }

# Eliminar finalizers de un recurso específico
remove_finalizers() {
    local resource_type=$1
    local resource_name=$2
    local namespace=$3

    if kubectl get "$resource_type" "$resource_name" -n "$namespace" &>/dev/null; then
        log_info "Eliminando finalizers de $resource_type/$resource_name en namespace $namespace"
        kubectl patch "$resource_type" "$resource_name" -n "$namespace" \
            -p '{"metadata":{"finalizers":[]}}' --type=merge 2>/dev/null || true
    fi
}

# Limpiar recursos huérfanos de Redis (CRDs, finalizers, etc.)
cleanup_redis_orphans() {
    log_step "Limpiando recursos huérfanos de Redis Operator..."

    # Eliminar finalizers de cualquier recurso Redis que haya quedado
    for crd in $(kubectl get crd 2>/dev/null | grep -E "redis.*opstreelabs" | awk '{print $1}'); do
        local resource_type=$(echo "$crd" | cut -d. -f1)
        for resource in $(kubectl get "$resource_type" --all-namespaces -o name 2>/dev/null | head -1); do
            if [ -n "$resource" ]; then
                local ns=$(echo "$resource" | cut -d/ -f2)
                local name=$(echo "$resource" | cut -d/ -f3)
                remove_finalizers "$resource_type" "$name" "$ns"
                kubectl delete "$resource_type" "$name" -n "$ns" --force --grace-period=0 2>/dev/null || true
            fi
        done
    done
}

# Esperar a que un namespace esté completamente limpio
wait_for_namespace_deletion() {
    local ns=$1
    local timeout=120
    local elapsed=0

    while kubectl get namespace "$ns" &>/dev/null; do
        if [ $elapsed -ge $timeout ]; then
            log_warning "Timeout esperando eliminación de namespace $ns"
            return 1
        fi
        sleep 2
        elapsed=$((elapsed + 2))
    done
    return 0
}

# Forzar eliminación de namespace atascado
force_delete_namespace() {
    local ns=$1
    log_warning "Forzando eliminación del namespace $ns..."

    # Eliminar finalizers del namespace
    kubectl get namespace "$ns" -o json | jq '.spec.finalizers = []' | kubectl replace --raw "/api/v1/namespaces/$ns/finalize" -f - 2>/dev/null || \
    kubectl patch namespace "$ns" -p '{"metadata":{"finalizers":[]}}' --type=merge 2>/dev/null || true

    # Forzar eliminación
    kubectl delete namespace "$ns" --force --grace-period=0 2>/dev/null || true
}

# ============================================
# Inicio del script
# ============================================
echo ""
echo "========================================="
echo "  🗑️  PLATAFORM UNINSTALL"
echo "========================================="
echo ""

# Confirmar desinstalación
read -p "⚠️  Esta acción eliminará TODOS los namespaces de la plataforma y sus recursos. ¿Continuar? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_info "Desinstalación cancelada."
    exit 0
fi

# ============================================
# FASE 1: Desinstalar componentes con Helm (orden respetando dependencias)
# ============================================
log_step "FASE 1: Desinstalando componentes con Helm (orden correcto)..."

# 1.1 ArgoCD (depende de Redis, pero ArgoCD se desinstala primero porque no afecta a Redis)
if namespace_exists "argocd"; then
    log_info "Desinstalando ArgoCD..."
    helm uninstall argocd -n argocd --wait 2>/dev/null && log_success "ArgoCD desinstalado." || log_warning "ArgoCD no estaba instalado"
else
    log_warning "Namespace argocd no existe, omitiendo ArgoCD."
fi

# 1.2 Redis Standalone (depende del operador)
if namespace_exists "data-services"; then
    if helm list -n data-services | grep -q "^redis "; then
        log_info "Desinstalando Redis Standalone..."
        helm uninstall redis -n data-services --wait 2>/dev/null && log_success "Redis Standalone desinstalado." || log_warning "Redis no estaba instalado"
    else
        log_warning "Redis Standalone no estaba instalado."
    fi
else
    log_warning "Namespace data-services no existe, omitiendo Redis."
fi

# 1.3 Redis Operator (debe desinstalarse después de Redis Standalone)
if namespace_exists "data-services"; then
    if helm list -n data-services | grep -q "^redis-operator"; then
        log_info "Desinstalando Redis Operator..."
        helm uninstall redis-operator -n data-services --wait 2>/dev/null && log_success "Redis Operator desinstalado." || log_warning "Redis Operator no estaba instalado"
    else
        log_warning "Redis Operator no estaba instalado."
    fi
fi

# ============================================
# FASE 2: Esperar a que los recursos se terminen de eliminar
# ============================================
log_step "FASE 2: Verificando que los recursos se hayan eliminado correctamente..."
sleep 5

# ============================================
# FASE 3: Limpiar residuos a nivel de clúster (CRDs, finalizers, etc.)
# ============================================
log_step "FASE 3: Limpiando residuos a nivel de clúster..."

# 3.1 Limpiar recursos huérfanos de Redis
cleanup_redis_orphans

# 3.2 Eliminar CRDs de Redis (preguntar)
read -p "¿Eliminar también los CRDs de Redis Operator? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    for crd in $(kubectl get crd 2>/dev/null | grep -E "redis.*opstreelabs" | awk '{print $1}'); do
        kubectl delete crd "$crd" 2>/dev/null && log_info "CRD $crd eliminado." || log_warning "No se pudo eliminar $crd"
    done
else
    log_info "CRDs de Redis conservados."
fi

# 3.3 Eliminar ClusterRoles y ClusterRoleBindings de ArgoCD
log_info "Eliminando recursos de clúster de ArgoCD..."
for role in argocd-application-controller argocd-server argocd-repo-server; do
    kubectl delete clusterrole "$role" 2>/dev/null && log_info "ClusterRole $role eliminado." || true
    kubectl delete clusterrolebinding "$role" 2>/dev/null && log_info "ClusterRoleBinding $role eliminado." || true
done

# ============================================
# FASE 4: Eliminar namespaces (orden: argocd, data-services, luego el resto)
# ============================================
log_step "FASE 4: Eliminando namespaces..."

# Primero los namespaces con componentes
for ns in "argocd" "data-services"; do
    if namespace_exists "$ns"; then
        log_info "Eliminando namespace: $ns"
        kubectl delete namespace "$ns" --timeout=120s --wait=true 2>/dev/null || {
            log_warning "Timeout o error al eliminar $ns. Verificando estado..."
            if kubectl get namespace "$ns" -o json | grep -q "finalizers"; then
                force_delete_namespace "$ns"
            fi
        }
        wait_for_namespace_deletion "$ns" || true
    else
        log_warning "Namespace $ns no existe, omitiendo."
    fi
done

# Luego el resto de namespaces
for ns in "${NAMESPACES[@]}"; do
    if [[ "$ns" == "argocd" || "$ns" == "data-services" ]]; then
        continue
    fi
    if namespace_exists "$ns"; then
        log_info "Eliminando namespace: $ns"
        kubectl delete namespace "$ns" --timeout=90s --wait=true 2>/dev/null || {
            log_warning "Timeout al eliminar $ns. Continuando..."
        }
    else
        log_warning "Namespace $ns no existe, omitiendo."
    fi
done

# Esperar a que todos los namespaces se hayan eliminado
sleep 5

# Verificar namespaces atascados
for ns in "${NAMESPACES[@]}"; do
    if namespace_exists "$ns"; then
        status=$(kubectl get namespace "$ns" -o jsonpath='{.status.phase}')
        if [ "$status" == "Terminating" ]; then
            log_error "Namespace $ns atascado en estado Terminating. Forzando eliminación..."
            force_delete_namespace "$ns"
        fi
    fi
done

# ============================================
# FASE 5: StorageClasses (opcional - preguntar)
# ============================================
log_step "FASE 5: Limpieza de StorageClasses..."
read -p "¿Eliminar los StorageClasses 'local-default' y 'nfs' creados por Foundation? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    for sc in local-default nfs; do
        if kubectl get storageclass "$sc" &>/dev/null; then
            kubectl delete storageclass "$sc" 2>/dev/null && log_info "StorageClass $sc eliminado." || log_warning "No se pudo eliminar $sc"
        else
            log_warning "StorageClass $sc no encontrado."
        fi
    done
else
    log_info "StorageClasses conservados."
fi

# ============================================
# FASE 6: Verificación final
# ============================================
log_step "FASE 6: Verificación final..."

echo ""
log_info "=== NAMESPACES RESIDUALES ==="
kubectl get namespaces | grep -E "$(IFS=\|; echo "${NAMESPACES[*]}")" || echo "   No se encontraron namespaces residuales."

echo ""
log_info "=== CRDs RESIDUALES ==="
kubectl get crd | grep -E "(redis|argocd)" || echo "   No se encontraron CRDs residuales."

echo ""
log_info "=== PVCs RESIDUALES ==="
kubectl get pvc -A | grep -E "(data-services|argocd)" || echo "   No se encontraron PVCs residuales."

echo ""
log_success "Desinstalación completada."
log_warning "Si algún PV quedó huérfano, revísalo con: kubectl get pv"