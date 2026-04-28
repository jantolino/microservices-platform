#!/bin/bash
# bootstrap.sh
# Script de bootstrap para desplegar la plataforma base (Foundation, Redis Operator, Redis Standalone, ArgoCD)
# Ejecutar desde la raíz del repositorio: ./k8s/bootstrap/bootstrap.sh

set -e  # Detener el script si cualquier comando falla

# ============================================
# Configuración de rutas y namespaces
# ============================================
REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo "$(pwd)")"
cd "$REPO_ROOT"

FOUNDATION_PATH="k8s/bootstrap/00-foundation"
REDIS_OP_VALUES="k8s/bootstrap/01-redis/redis-operator-values.yaml"
REDIS_STANDALONE_VALUES="k8s/bootstrap/01-redis/redis-standalone-values.yaml"
ARGOCD_VALUES="k8s/bootstrap/02-argocd/argocd-values.yaml"

NAMESPACE_DATA="data-services"
NAMESPACE_ARGOCD="argocd"

# Colores para mensajes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================
# Funciones auxiliares
# ============================================
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

# ============================================
# Validación de herramientas previas
# ============================================
validate_tools() {
    log_info "Verificando herramientas necesarias..."

    # Verificar kubectl
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl no está instalado o no está en el PATH."
        exit 1
    fi
    log_success "kubectl encontrado: $(kubectl version --client --short 2>/dev/null || echo 'versión desconocida')"

    # Verificar helm
    if ! command -v helm &> /dev/null; then
        log_error "helm no está instalado o no está en el PATH."
        exit 1
    fi
    log_success "helm encontrado: $(helm version --short 2>/dev/null || echo 'versión desconocida')"

    # Verificar cluster Kubernetes (opcional, pero recomendado)
    if ! kubectl cluster-info &> /dev/null; then
        log_error "No se puede conectar al cluster Kubernetes."
        exit 1
    fi
    log_success "Cluster Kubernetes accesible: $(kubectl cluster-info | head -1)"
}

# Función para verificar existencia de un namespace
namespace_exists() {
    kubectl get namespace "$1" &>/dev/null
    return $?
}

# Función para verificar que el operador de Redis esté listo antes de instalar el standalone
wait_for_redis_operator() {
    log_info "Esperando a que el operador de Redis esté completamente operativo..."

    # Esperar a que el deployment del operador esté disponible
    kubectl wait --for=condition=available --timeout=120s -n "$NAMESPACE_DATA" deployment/redis-operator 2>/dev/null || {
        log_error "El operador de Redis no se volvió disponible en el tiempo esperado."
        log_info "Intentando obtener información de depuración:"
        kubectl get pods -n "$NAMESPACE_DATA" -l app.kubernetes.io/name=redis-operator
        kubectl describe pods -n "$NAMESPACE_DATA" -l app.kubernetes.io/name=redis-operator
        exit 1
    }

    # Verificar que el CRD esté instalado
    CRD_NAME=$(kubectl get crd | grep "redis.*opstreelabs" | awk '{print $1}' | head -1)
    if [ -z "$CRD_NAME" ]; then
        log_error "No se encontró ningún CRD de Redis Operator."
        log_info "Puede que el operador aún no haya registrado el CRD. Esperando unos segundos..."
        sleep 10
        CRD_NAME=$(kubectl get crd | grep "redis.*opstreelabs" | awk '{print $1}' | head -1)
        if [ -z "$CRD_NAME" ]; then
            log_error "El CRD no apareció. La instalación del operador puede haber fallado."
            exit 1
        fi
    fi

    log_success "Operador de Redis listo y CRD disponible."
}

# Función para verificar que Redis está listo
wait_for_redis_ready() {
    log_info "Esperando a que Redis Standalone esté listo..."

    kubectl wait --for=condition=ready --timeout=180s -n "$NAMESPACE_DATA" pod/redis-0 2>/dev/null || {
        log_error "Redis no se volvió listo en el tiempo esperado."
        log_info "Intentando obtener información de depuración:"
        kubectl get pods -n "$NAMESPACE_DATA" -l app.kubernetes.io/name=redis
        kubectl describe pods -n "$NAMESPACE_DATA" -l app.kubernetes.io/name=redis
        exit 1
    }

    log_success "Redis Standalone está listo."
}

# Llamar a la validación al inicio del script (justo después de las configuraciones iniciales)
validate_tools

# ============================================
# Inicio del script
# ============================================
echo ""
echo "========================================="
echo "  🚀 PLATAFORM BOOTSTRAP"
echo "========================================="
echo ""

# Verificar que kubectl está disponible
if ! command -v kubectl &> /dev/null; then
    log_error "kubectl no está instalado o no está en el PATH."
    exit 1
fi

# Verificar que helm está disponible
if ! command -v helm &> /dev/null; then
    log_error "helm no está instalado o no está en el PATH."
    exit 1
fi

# Verificar que los archivos necesarios existen
if [ ! -d "$FOUNDATION_PATH" ]; then
    log_error "No se encontró el directorio de Foundation: $FOUNDATION_PATH"
    exit 1
fi

if [ ! -f "$REDIS_OP_VALUES" ]; then
    log_error "No se encontró el archivo de valores para Redis Operator: $REDIS_OP_VALUES"
    exit 1
fi

if [ ! -f "$REDIS_STANDALONE_VALUES" ]; then
    log_error "No se encontró el archivo de valores para Redis Standalone: $REDIS_STANDALONE_VALUES"
    exit 1
fi

if [ ! -f "$ARGOCD_VALUES" ]; then
    log_error "No se encontró el archivo de valores para ArgoCD: $ARGOCD_VALUES"
    exit 1
fi

# ============================================
# Fase 1: Foundation (Kustomize)
# ============================================
log_info "=== FASE 1: Aplicando Foundation (Namespaces, NetworkPolicies, StorageClass, etc.) ==="
kubectl apply -k "$FOUNDATION_PATH"
log_success "Foundation aplicada correctamente."

# Verificar que el namespace data-services existe
if ! namespace_exists "$NAMESPACE_DATA"; then
    log_error "El namespace $NAMESPACE_DATA no fue creado por Foundation. Verifica la configuración."
    exit 1
fi

# ============================================
# Fase 2: Redis Operator
# ============================================
log_info "=== FASE 2: Instalando Redis Operator ==="

# Agregar repositorio ot-helm si no está presente
if ! helm repo list | grep -q "ot-helm"; then
    log_info "Agregando repositorio ot-helm..."
    helm repo add ot-helm https://ot-container-kit.github.io/helm-charts/
    helm repo update
fi

log_info "Ejecutando: helm upgrade --install redis-operator ot-helm/redis-operator --namespace $NAMESPACE_DATA -f $REDIS_OP_VALUES --wait"
helm upgrade --install redis-operator ot-helm/redis-operator \
    --namespace "$NAMESPACE_DATA" \
    -f "$REDIS_OP_VALUES" \
    --wait --debug

log_success "Redis Operator instalado correctamente."

# Verificar que el operador esté realmente operativo
wait_for_redis_operator

# ============================================
# Fase 3: Redis Standalone
# ============================================
log_info "=== FASE 3: Instalando Redis Standalone ==="

log_info "Ejecutando: helm upgrade --install redis ot-helm/redis --namespace $NAMESPACE_DATA -f $REDIS_STANDALONE_VALUES --wait"
helm upgrade --install redis ot-helm/redis \
    --namespace "$NAMESPACE_DATA" \
    -f "$REDIS_STANDALONE_VALUES" \
    --wait --debug

log_success "Redis Standalone instalado correctamente."

# Verificar que Redis esté listo
wait_for_redis_ready

# ============================================
# Fase 4: ArgoCD
# ============================================
log_info "=== FASE 4: Instalando ArgoCD ==="

# Agregar repositorio de ArgoCD si no está presente
if ! helm repo list | grep -q "argo"; then
    log_info "Agregando repositorio argo..."
    helm repo add argo https://argoproj.github.io/argo-helm
    helm repo update
fi

# Asegurar que el namespace argocd existe
if ! namespace_exists "$NAMESPACE_ARGOCD"; then
    log_info "Creando namespace $NAMESPACE_ARGOCD..."
    kubectl create namespace "$NAMESPACE_ARGOCD"
fi

log_info "Ejecutando: helm upgrade --install argocd argo/argo-cd --namespace $NAMESPACE_ARGOCD -f $ARGOCD_VALUES --wait"
helm upgrade --install argocd argo/argo-cd \
    --namespace "$NAMESPACE_ARGOCD" \
    -f "$ARGOCD_VALUES" \
    --wait --debug

log_success "ArgoCD instalado correctamente."

# Esperar a que el pod de ArgoCD esté listo
log_info "Esperando a que el servidor de ArgoCD esté listo..."
kubectl wait --for=condition=ready --timeout=180s -n "$NAMESPACE_ARGOCD" pod -l app.kubernetes.io/name=argocd-server 2>/dev/null || {
    log_warning "No se pudo verificar el pod de ArgoCD, pero la instalación debería estar funcionando."
}

# ============================================
# Finalización
# ============================================
echo ""
echo "========================================="
log_success "🚀 BOOTSTRAP COMPLETADO EXITOSAMENTE"
echo "========================================="
echo ""

# Obtener la contraseña inicial de ArgoCD
log_info "Obteniendo la contraseña inicial de ArgoCD..."
ARGOCD_PASSWORD=$(kubectl get secret argocd-initial-admin-secret -n "$NAMESPACE_ARGOCD" -o jsonpath="{.data.password}" 2>/dev/null | base64 -d)

if [ -n "$ARGOCD_PASSWORD" ]; then
    echo ""
    echo "🔐 CREDENCIALES DE ARGOCD:"
    echo "   Usuario: admin"
    echo "   Contraseña: $ARGOCD_PASSWORD"
    echo ""
else
    log_warning "No se pudo obtener la contraseña de ArgoCD. Puedes recuperarla con:"
    echo "   kubectl get secret argocd-initial-admin-secret -n $NAMESPACE_ARGOCD -o jsonpath=\"{.data.password}\" | base64 -d"
fi

echo ""
echo "🌐 ACCESO A ARGOCD:"
echo "   kubectl port-forward -n $NAMESPACE_ARGOCD svc/argocd-server 8080:80"
echo "   Luego abre http://localhost:8080"
echo ""
echo "📁 PARA CONTINUAR CON GITOPS:"
echo "   Aplica la App of Apps para desplegar el resto de componentes:"
echo "   kubectl apply -f k8s/argocd/apps/app-of-apps.yaml"
echo ""

log_success "¡Listo! La plataforma base está funcionando y ArgoCD está listo para gestionar el resto de la infraestructura."