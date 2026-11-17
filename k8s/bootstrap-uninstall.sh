#!/bin/bash
# bootstrap-uninstall.sh
# Script para desinstalar los componentes instalados por bootstrap.sh
# Ejecutar desde la raíz del repositorio: ./k8s/bootstrap-uninstall.sh
# Orden de desinstalación (inverso a la instalación):
# 1. Applications de ArgoCD en orden inverso de sync-wave (ArgoCD aún activo borra sus recursos)
# 2. ArgoCD, Redis Standalone y Redis Operator (Helm)
# 3. Webhooks e instancias de CRDs con finalizers
# 4. Namespaces de Foundation
# 5. Volúmenes persistentes y datos residuales
# 6. CRDs, RBAC de clúster y StorageClasses

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo "$(pwd)")"
cd "$REPO_ROOT"

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

# Namespaces creados por Foundation (00-foundation/kustomization.yaml)
NAMESPACES=(
    "argocd"
    "cert-manager"
    "data-services"
    "devops-tools"
    "headlamp"
    "microservices"
    "monitoring"
    "networking"
    "repositories"
    "security"
    "secrets-manager"
)

# StorageClasses creadas por Foundation
STORAGE_CLASSES=("local-default" "nfs")

# Nombres de ClusterRoles, ClusterRoleBindings y Webhooks creados por bootstrap.sh y las apps de ArgoCD
CLUSTER_PATTERN="argocd|cert-manager|external-secrets|externalsecret|secretstore|vault|redis"

# Grupos de API de los CRDs instalados (ArgoCD, cert-manager, ESO, VCO, Redis Operator)
CRD_GROUP_PATTERN="argoproj\.io$|cert-manager\.io$|external-secrets\.io$|redhatcop\.redhat\.io$|opstreelabs\.in$"

ROOT_APP_NAME="platform-root"
SECRETS_DIR="k8s/.secrets"

# Funciones auxiliares
namespace_exists() { kubectl get namespace "$1" &>/dev/null; }

# Quitar finalizers de un recurso ($3 vacío = recurso de clúster)
strip_finalizers() {
    local kind=$1
    local name=$2
    local ns=${3:-}
    if [ -n "$ns" ]; then
        kubectl patch "$kind" "$name" -n "$ns" --type=merge -p '{"metadata":{"finalizers":null}}' >/dev/null 2>&1 || true
    else
        kubectl patch "$kind" "$name" --type=merge -p '{"metadata":{"finalizers":null}}' >/dev/null 2>&1 || true
    fi
}

list_platform_crds() {
    kubectl get crd -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' 2>/dev/null | grep -E "$CRD_GROUP_PATTERN" || true
}

# Elimina todas las instancias de los CRDs de la plataforma quitando antes sus finalizers
# (sus operadores ya no existen para procesarlos y bloquearían namespaces y CRDs).
purge_custom_resources() {
    log_step "Eliminando instancias residuales de CRDs de la plataforma..."
    local crd item ns name
    for crd in $(list_platform_crds); do
        for item in $(kubectl get "$crd" -A -o jsonpath='{range .items[*]}{.metadata.namespace}{"/"}{.metadata.name}{"\n"}{end}' 2>/dev/null || true); do
            ns="${item%%/*}"
            name="${item#*/}"
            strip_finalizers "$crd" "$name" "$ns"
            if [ -n "$ns" ]; then
                kubectl delete "$crd" "$name" -n "$ns" --ignore-not-found --wait=false >/dev/null 2>&1 || true
            else
                kubectl delete "$crd" "$name" --ignore-not-found --wait=false >/dev/null 2>&1 || true
            fi
            log_info "$crd $item eliminado"
        done
    done
}

# Elimina las Applications mientras ArgoCD sigue activo para que borre sus recursos gestionados
delete_argocd_applications() {
    if ! namespace_exists "argocd" || ! kubectl get crd applications.argoproj.io &>/dev/null; then
        log_warning "ArgoCD no está presente; se omiten las Applications."
        return 0
    fi

    # Root App sin cascada: evita que vuelva a crear las apps hijas mientras se eliminan
    if kubectl get application "$ROOT_APP_NAME" -n argocd &>/dev/null; then
        log_info "Eliminando $ROOT_APP_NAME (sin cascada)..."
        strip_finalizers application "$ROOT_APP_NAME" argocd
        kubectl delete application "$ROOT_APP_NAME" -n argocd --timeout=60s >/dev/null 2>&1 || true
    fi

    # Apps hijas en orden inverso de sync-wave (cluster-secret-store -> ... -> cert-manager)
    local app
    for app in $(kubectl get applications -n argocd \
            -o jsonpath='{range .items[*]}{.metadata.annotations.argocd\.argoproj\.io/sync-wave}{"|"}{.metadata.name}{"\n"}{end}' 2>/dev/null \
            | awk -F'|' '{ print ($1 == "" ? 0 : $1) "|" $2 }' \
            | sort -t'|' -k1,1 -n -r \
            | cut -d'|' -f2 || true); do
        log_info "Eliminando Application $app y sus recursos..."
        if kubectl delete application "$app" -n argocd --timeout=180s >/dev/null 2>&1; then
            log_success "Application $app eliminada."
        else
            log_warning "Application $app no terminó en 180s; se fuerza y sus residuos se limpian después."
            strip_finalizers application "$app" argocd
            kubectl delete application "$app" -n argocd --ignore-not-found --wait=false >/dev/null 2>&1 || true
        fi
    done
}

# Elimina recursos de clúster cuyo nombre coincide con CLUSTER_PATTERN
delete_cluster_resources_by_name() {
    local kind=$1
    local item
    for item in $(kubectl get "$kind" -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' 2>/dev/null | grep -E "$CLUSTER_PATTERN" || true); do
        if kubectl delete "$kind" "$item" --ignore-not-found >/dev/null 2>&1; then
            log_info "$kind $item eliminado"
        else
            log_warning "No se pudo eliminar $kind $item"
        fi
    done
}

delete_platform_crds() {
    local crd
    for crd in $(list_platform_crds); do
        if kubectl delete crd "$crd" --ignore-not-found --timeout=60s >/dev/null 2>&1; then
            log_info "CRD $crd eliminado"
        else
            log_warning "CRD $crd atascado; quitando finalizers..."
            strip_finalizers crd "$crd"
            kubectl delete crd "$crd" --ignore-not-found --wait=false >/dev/null 2>&1 || true
        fi
    done
}

# Elimina PVs que pertenecían a los namespaces de la plataforma y sus datos en Minikube
delete_platform_volumes() {
    local item name claim_ns ns elapsed=0

    # Dar tiempo al provisioner para reclamar (reclaimPolicy: Delete) y borrar los datos del host
    while kubectl get pv -o jsonpath='{range .items[*]}{.spec.claimRef.namespace}{"\n"}{end}' 2>/dev/null \
            | grep -qxE "$(IFS='|'; echo "${NAMESPACES[*]}")"; do
        if [ $elapsed -ge 60 ]; then
            break
        fi
        sleep 5
        elapsed=$((elapsed + 5))
    done

    for item in $(kubectl get pv -o jsonpath='{range .items[*]}{.metadata.name}{"|"}{.spec.claimRef.namespace}{"\n"}{end}' 2>/dev/null || true); do
        name="${item%%|*}"
        claim_ns="${item#*|}"
        for ns in "${NAMESPACES[@]}"; do
            if [ "$claim_ns" == "$ns" ]; then
                strip_finalizers pv "$name"
                kubectl delete pv "$name" --ignore-not-found --wait=false >/dev/null 2>&1 || true
                log_info "PV $name ($claim_ns) eliminado"
            fi
        done
    done

    # Datos del storage-provisioner de Minikube: si sobreviven, Vault arrancaría
    # ya inicializado con claves de la instalación anterior.
    if command -v minikube &>/dev/null && [ "$(kubectl config current-context 2>/dev/null)" == "minikube" ]; then
        log_info "Eliminando datos residuales de /tmp/hostpath-provisioner en Minikube..."
        for ns in "${NAMESPACES[@]}"; do
            minikube ssh -- "sudo rm -rf /tmp/hostpath-provisioner/$ns" >/dev/null 2>&1 || true
        done
    fi
}

# Esperar a que un namespace esté completamente limpio
wait_for_namespace_deletion() {
    local ns=$1
    local timeout=${2:-120}
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

# Forzar eliminación de namespace atascado: vacía spec.finalizers vía la API /finalize (sin jq)
force_delete_namespace() {
    local ns=$1
    log_warning "Forzando eliminación del namespace $ns..."
    kubectl get namespace "$ns" -o json 2>/dev/null \
        | sed 's/"kubernetes"//' \
        | kubectl replace --raw "/api/v1/namespaces/$ns/finalize" -f - >/dev/null 2>&1 || true
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
# FASE 1: Applications de ArgoCD (ArgoCD aún activo elimina lo que gestiona)
# ============================================
log_step "FASE 1: Eliminando Applications de ArgoCD en orden inverso de sync-wave..."
delete_argocd_applications

# ============================================
# FASE 2: Componentes instalados con Helm por bootstrap.sh
# ============================================
log_step "FASE 2: Desinstalando componentes con Helm (orden correcto)..."

# 2.1 ArgoCD (depende de Redis, pero ArgoCD se desinstala primero porque no afecta a Redis)
if namespace_exists "argocd"; then
    log_info "Desinstalando ArgoCD..."
    helm uninstall argocd -n argocd --wait 2>/dev/null && log_success "ArgoCD desinstalado." || log_warning "ArgoCD no estaba instalado"
else
    log_warning "Namespace argocd no existe, omitiendo ArgoCD."
fi

# 2.2 Redis Standalone (depende del operador)
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

# 2.3 Redis Operator (debe desinstalarse después de Redis Standalone)
if namespace_exists "data-services"; then
    if helm list -n data-services | grep -q "^redis-operator"; then
        log_info "Desinstalando Redis Operator..."
        helm uninstall redis-operator -n data-services --wait 2>/dev/null && log_success "Redis Operator desinstalado." || log_warning "Redis Operator no estaba instalado"
    else
        log_warning "Redis Operator no estaba instalado."
    fi
fi

# ============================================
# FASE 3: Webhooks e instancias de CRDs
# ============================================
log_step "FASE 3: Eliminando webhooks e instancias de CRDs..."

# Webhooks huérfanos apuntan a servicios inexistentes y bloquean la creación y el borrado de recursos
delete_cluster_resources_by_name validatingwebhookconfiguration
delete_cluster_resources_by_name mutatingwebhookconfiguration
purge_custom_resources

# ============================================
# FASE 4: Eliminar namespaces de Foundation
# ============================================
log_step "FASE 4: Eliminando namespaces..."

for ns in "${NAMESPACES[@]}"; do
    if namespace_exists "$ns"; then
        log_info "Eliminando namespace: $ns"
        kubectl delete namespace "$ns" --wait=false >/dev/null 2>&1 || true
    else
        log_warning "Namespace $ns no existe, omitiendo."
    fi
done

for ns in "${NAMESPACES[@]}"; do
    if namespace_exists "$ns" && ! wait_for_namespace_deletion "$ns" 180; then
        log_error "Namespace $ns atascado en Terminating. Limpiando residuos y forzando..."
        purge_custom_resources
        force_delete_namespace "$ns"
        wait_for_namespace_deletion "$ns" 30 || log_error "Namespace $ns sigue presente; revísalo con: kubectl get namespace $ns -o yaml"
    fi
done

# ============================================
# FASE 5: Volúmenes persistentes y datos residuales
# ============================================
log_step "FASE 5: Eliminando volúmenes persistentes de la plataforma..."
delete_platform_volumes

# ============================================
# FASE 6: CRDs, RBAC de clúster y StorageClasses
# ============================================
log_step "FASE 6: Eliminando CRDs, RBAC de clúster y StorageClasses..."

purge_custom_resources
delete_platform_crds
delete_cluster_resources_by_name clusterrolebinding
delete_cluster_resources_by_name clusterrole
delete_cluster_resources_by_name validatingwebhookconfiguration
delete_cluster_resources_by_name mutatingwebhookconfiguration

for sc in "${STORAGE_CLASSES[@]}"; do
    if kubectl get storageclass "$sc" &>/dev/null; then
        if kubectl delete storageclass "$sc" >/dev/null 2>&1; then
            log_info "StorageClass $sc eliminado."
        else
            log_warning "No se pudo eliminar StorageClass $sc"
        fi
    fi
done

# ============================================
# FASE 7: Verificación final
# ============================================
log_step "FASE 7: Verificación final..."

NS_REGEX="$(IFS='|'; echo "${NAMESPACES[*]}")"

echo ""
log_info "=== NAMESPACES RESIDUALES ==="
kubectl get namespaces --no-headers 2>/dev/null | grep -E "^($NS_REGEX)[[:space:]]" || echo "   Ninguno."

echo ""
log_info "=== CRDs RESIDUALES ==="
list_platform_crds | grep . || echo "   Ninguno."

echo ""
log_info "=== RBAC Y WEBHOOKS RESIDUALES ==="
kubectl get clusterrole,clusterrolebinding,validatingwebhookconfiguration,mutatingwebhookconfiguration -o name 2>/dev/null \
    | grep -E "$CLUSTER_PATTERN" || echo "   Ninguno."

echo ""
log_info "=== PVs RESIDUALES ==="
kubectl get pv -o jsonpath='{range .items[*]}{.metadata.name}{" "}{.spec.claimRef.namespace}{"\n"}{end}' 2>/dev/null \
    | grep -E "[[:space:]]($NS_REGEX)$" || echo "   Ninguno."

echo ""
log_success "Desinstalación completada."

if ls "$SECRETS_DIR"/vault-init*.json &>/dev/null; then
    log_warning "Las claves de Vault en $SECRETS_DIR pertenecen a la instalación eliminada y ya no sirven."
    log_warning "bootstrap.sh las conservará renombradas (.old.json) al reinicializar Vault; bórralas cuando ya no las necesites."
fi