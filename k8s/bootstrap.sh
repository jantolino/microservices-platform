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
REDIS_NETPOL="k8s/bootstrap/01-redis/redis-network-policy.yaml"
ARGOCD_VALUES="k8s/bootstrap/02-argocd/argocd-values.yaml"
ARGOCD_NETPOL="k8s/bootstrap/02-argocd/argocd-network-policy.yaml"
ROOT_APP="k8s/bootstrap/03-platform-orchestration/root-app-of-apps.yaml"

NAMESPACE_DATA="data-services"
NAMESPACE_ARGOCD="argocd"
NAMESPACE_VAULT="secrets-manager"

# Contraseña de Redis (se genera una sola vez; nunca se versiona)
REDIS_SECRET_NAME="redis-auth"
ARGOCD_REDIS_SECRET_NAME="argocd-redis-auth"

# Vault: init/unseal/seed del auth (Fase 5)
VAULT_POD="vault-0"
VAULT_KEY_SHARES=5
VAULT_KEY_THRESHOLD=3
SECRETS_DIR="k8s/.secrets"
VAULT_INIT_FILE="$SECRETS_DIR/vault-init.json"

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

    # Verificar Cilium (CNI requerido por las políticas zero-trust)
    if ! kubectl get crd ciliumnetworkpolicies.cilium.io &> /dev/null; then
        log_error "Cilium no está instalado (falta el CRD ciliumnetworkpolicies.cilium.io)."
        log_info "En Minikube: minikube start --cni=cilium"
        exit 1
    fi
    log_success "Cilium detectado."
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
#wait_for_redis_ready() {
#    log_info "Esperando a que Redis Standalone esté listo..."
#
#    kubectl wait --for=condition=ready --timeout=180s -n "$NAMESPACE_DATA" pod/redis-0 2>/dev/null || {
#        log_error "Redis no se volvió listo en el tiempo esperado."
#        log_info "Intentando obtener información de depuración:"
#        kubectl get pods -n "$NAMESPACE_DATA" -l app.kubernetes.io/name=redis
#        kubectl describe pods -n "$NAMESPACE_DATA" -l app.kubernetes.io/name=redis
#        exit 1
#    }
#
#    log_success "Redis Standalone está listo."
#}

wait_for_redis_ready() {
    log_info "Esperando a que Redis Standalone esté listo..."

    # kubectl wait falla al instante sobre un pod inexistente:
    # primero esperar a que el operador lo cree
    local timeout=180 elapsed=0
    until kubectl get pod redis-0 -n "$NAMESPACE_DATA" &>/dev/null; do
        if (( elapsed >= timeout )); then
            log_error "El operador no creó el pod redis-0 en ${timeout}s."
            break
        fi
        sleep 5
        (( elapsed += 5 ))
    done

    if ! kubectl wait --for=condition=ready --timeout=180s -n "$NAMESPACE_DATA" pod/redis-0 2>/dev/null; then
        log_error "Redis no se volvió listo en el tiempo esperado. Estado real:"
        echo "--- CR Redis ---"
        kubectl get redis -n "$NAMESPACE_DATA" 2>/dev/null || echo "CR Redis no existe (el operador no lo reconcilió o el chart no lo creó)"
        kubectl describe redis -n "$NAMESPACE_DATA" 2>/dev/null | tail -15 || true
        echo "--- Recursos creados ---"
        kubectl get statefulset,pod,pvc,secret -n "$NAMESPACE_DATA"
        echo "--- Eventos recientes ---"
        kubectl get events -n "$NAMESPACE_DATA" --sort-by=.lastTimestamp | tail -15
        echo "--- Logs del operador ---"
        kubectl logs deploy/redis-operator -n "$NAMESPACE_DATA" --tail=40 || true
        exit 1
    fi

    log_success "Redis Standalone está listo."
}

# ============================================
# Funciones de Vault (Fase 5)
# ============================================
vault_cmd() {
    kubectl exec -n "$NAMESPACE_VAULT" "$VAULT_POD" -c vault -- "$@"
}

# Codigo de `vault status`: 0 = desellado, 2 = sellado, 1 = error/no disponible
vault_status_code() {
    local rc=0
    vault_cmd vault status >/dev/null 2>&1 || rc=$?
    echo "$rc"
}

wait_for_vault_pod() {
    log_info "Esperando a que ArgoCD despliegue $VAULT_POD (puede tardar varios minutos)..."
    local timeout=900
    local elapsed=0
    until [ "$(kubectl get pod "$VAULT_POD" -n "$NAMESPACE_VAULT" -o jsonpath='{.status.phase}' 2>/dev/null)" == "Running" ]; do
        if [ $elapsed -ge $timeout ]; then
            log_error "$VAULT_POD no llegó a Running en ${timeout}s."
            kubectl get applications -n "$NAMESPACE_ARGOCD" 2>/dev/null || true
            exit 1
        fi
        sleep 5
        elapsed=$((elapsed + 5))
    done

    # El pod no queda Ready mientras esté sellado; esperamos a que la API responda.
    elapsed=0
    until [ "$(vault_status_code)" != "1" ]; do
        if [ $elapsed -ge 300 ]; then
            log_error "La API de Vault no respondió en 300s."
            exit 1
        fi
        sleep 5
        elapsed=$((elapsed + 5))
    done
    log_success "Vault responde."
}

vault_is_initialized() {
    vault_cmd vault status -format=json 2>/dev/null | grep -q '"initialized": *true'
}

read_unseal_keys() {
    sed -n '/"unseal_keys_b64"/,/\]/p' "$VAULT_INIT_FILE" | grep -o '"[^"]*"' | grep -v 'unseal_keys_b64' | tr -d '"'
}

read_root_token() {
    grep -o '"root_token"[[:space:]]*:[[:space:]]*"[^"]*"' "$VAULT_INIT_FILE" | sed 's/.*"\([^"]*\)"$/\1/'
}

vault_init() {
    mkdir -p "$SECRETS_DIR"
    chmod 700 "$SECRETS_DIR" 2>/dev/null || true

    # Un archivo previo pertenece a una instalación anterior: se conserva renombrado.
    if [ -f "$VAULT_INIT_FILE" ]; then
        local backup="$SECRETS_DIR/vault-init-$(date +%Y%m%d-%H%M%S).old.json"
        mv "$VAULT_INIT_FILE" "$backup"
        log_warning "Archivo de init previo movido a: $backup"
    fi

    log_info "Inicializando Vault (${VAULT_KEY_SHARES} claves, umbral ${VAULT_KEY_THRESHOLD})..."
    (umask 077; vault_cmd vault operator init \
        -key-shares="$VAULT_KEY_SHARES" \
        -key-threshold="$VAULT_KEY_THRESHOLD" \
        -format=json > "$VAULT_INIT_FILE.tmp")
    mv "$VAULT_INIT_FILE.tmp" "$VAULT_INIT_FILE"
    chmod 600 "$VAULT_INIT_FILE" 2>/dev/null || true
    log_success "Vault inicializado. Claves y root token guardados en: $VAULT_INIT_FILE"
}

vault_unseal() {
    if [ "$(vault_status_code)" == "0" ]; then
        log_success "Vault ya está desellado."
        return 0
    fi

    local keys=()
    mapfile -t keys < <(read_unseal_keys)
    if [ ${#keys[@]} -lt "$VAULT_KEY_THRESHOLD" ]; then
        log_error "No se encontraron suficientes unseal keys en $VAULT_INIT_FILE."
        exit 1
    fi

    log_info "Desellando Vault..."
    local key
    for key in "${keys[@]}"; do
        [ "$(vault_status_code)" == "0" ] && break
        vault_cmd vault operator unseal "$key" >/dev/null
    done

    if [ "$(vault_status_code)" != "0" ]; then
        log_error "Vault sigue sellado. Verifica que $VAULT_INIT_FILE corresponda a esta instalación."
        exit 1
    fi
    log_success "Vault desellado."
}

# Semilla mínima para que el VCO pueda autenticarse; el resto lo crea el VCO desde Git.
vault_seed_auth() {
    local token
    token="$(read_root_token)"
    if [ -z "$token" ]; then
        log_error "No se encontró el root token en $VAULT_INIT_FILE."
        exit 1
    fi

    log_info "Configurando auth/kubernetes y el rol vault-config-operator..."
    kubectl exec -i -n "$NAMESPACE_VAULT" "$VAULT_POD" -c vault -- env VAULT_TOKEN="$token" sh -s >/dev/null <<'EOF'
set -e
vault auth list | grep -q '^kubernetes/' || vault auth enable kubernetes
vault write auth/kubernetes/config kubernetes_host="https://$KUBERNETES_SERVICE_HOST:443"
vault policy write vault-config-operator - <<'POLICY'
path "sys/mounts/*"           { capabilities = ["create","read","update","delete","list"] }
path "sys/mounts"             { capabilities = ["read","list"] }
path "sys/policies/acl/*"     { capabilities = ["create","read","update","delete","list"] }
path "auth/kubernetes/role/*" { capabilities = ["create","read","update","delete","list"] }
path "sys/audit"              { capabilities = ["read","list","sudo"] }
path "sys/audit/*"            { capabilities = ["create","read","update","delete","list","sudo"] }
POLICY
vault write auth/kubernetes/role/vault-config-operator \
  bound_service_account_names=default \
  bound_service_account_namespaces=secrets-manager \
  policies=vault-config-operator \
  ttl=1h
EOF
    log_success "Auth de Vault configurado."
}

# Semillas de credenciales de componentes (idempotente: solo crea el path si NO existe;
# un bootstrap repetido NO rota contraseñas ya establecidas).
# Requiere el motor KV v2 'secret/', que crea el VCO (SecretEngineMount): se espera a que exista.
# Cada path se escribe en UNA sola operación (kv put reemplaza el documento completo).
vault_seed_kv() {
    local token
    token="$(read_root_token)"
    if [ -z "$token" ]; then
        log_error "No se encontró el root token en $VAULT_INIT_FILE."
        exit 1
    fi

    log_info "Sembrando credenciales de componentes en Vault (solo si no existen)..."
    kubectl exec -i -n "$NAMESPACE_VAULT" "$VAULT_POD" -c vault -- env VAULT_TOKEN="$token" sh -s >/dev/null <<'EOF'
set -e
i=0
until vault secrets list | grep -q '^secret/'; do
  i=$((i+1)); [ "$i" -gt 120 ] && { echo "El motor KV 'secret/' no apareció en 600s (revisa el VCO / SecretEngineMount)." >&2; exit 1; }
  sleep 5
done

# Aleatorio generado por el propio Vault (no depende de openssl en la imagen)
rand() { vault write -field=random_bytes sys/tools/random/32 format=hex; }

# seed <path> clave=valor [clave=valor ...]: escribe todas las claves juntas si el path no existe
seed() {
  p="$1"; shift
  if vault kv get "$p" >/dev/null 2>&1; then
    return 0
  fi
  for kv in "$@"; do
    [ -n "${kv#*=}" ] || { echo "Valor vacío para '${kv%%=*}' en $p" >&2; exit 1; }
  done
  vault kv put "$p" "$@" >/dev/null
}

seed secret/platform/databases/postgres    password="$(rand)"
seed secret/platform/tools/pgadmin         password="$(rand)"
seed secret/platform/tools/grafana         admin-user=admin admin-password="$(rand)"
seed secret/platform/tools/jenkins         jenkins-admin-user=admin jenkins-admin-password="$(rand)"
seed secret/platform/databases/sonarqube   password="$(rand)"
seed secret/platform/tools/sonarqube       monitoringPasscode="$(rand)"
seed secret/platform/databases/artifactory password="$(rand)"
seed secret/platform/tools/artifactory     masterKey="$(rand)" joinKey="$(rand)" adminPassword="$(rand)"
EOF
    log_success "Credenciales de componentes sembradas en Vault."
}

# El VCO arranca justo después del unseal y puede quedar en backoff si intentó
# autenticarse antes de la semilla: se reinicia para que reconcilie de inmediato.
restart_vault_config_operator() {
    log_info "Esperando al Vault Config Operator..."
    local timeout=600
    local elapsed=0
    until kubectl get deployment vault-config-operator -n "$NAMESPACE_VAULT" &>/dev/null; do
        if [ $elapsed -ge $timeout ]; then
            log_warning "El VCO no apareció en ${timeout}s; reconciliará al arrancar."
            return 0
        fi
        sleep 5
        elapsed=$((elapsed + 5))
    done

    kubectl rollout restart deployment vault-config-operator -n "$NAMESPACE_VAULT" >/dev/null
    kubectl rollout status deployment vault-config-operator -n "$NAMESPACE_VAULT" --timeout=300s || {
        log_warning "El VCO no completó el rollout; revísalo en ArgoCD."
        return 0
    }
    log_success "Vault Config Operator reiniciado."
}

# Crea la contraseña de Redis si no existe y la replica en el namespace de ArgoCD.
# Idempotente: si ya existe se reutiliza (regenerarla dejaría a Redis y ArgoCD desincronizados).
ensure_redis_secrets() {
    local password
    if kubectl get secret "$REDIS_SECRET_NAME" -n "$NAMESPACE_DATA" &>/dev/null; then
        log_info "Secret $REDIS_SECRET_NAME ya existe; se reutiliza."
        password="$(kubectl get secret "$REDIS_SECRET_NAME" -n "$NAMESPACE_DATA" -o jsonpath='{.data.password}' | base64 -d)"
    else
        password="$(openssl rand -base64 32 | tr -d '/+=\n')"
        kubectl create secret generic "$REDIS_SECRET_NAME" -n "$NAMESPACE_DATA" \
            --from-literal=password="$password"
        kubectl label secret "$REDIS_SECRET_NAME" -n "$NAMESPACE_DATA" part-of=microservices-platform --overwrite >/dev/null
        log_success "Secret $REDIS_SECRET_NAME creado."
    fi

    if ! namespace_exists "$NAMESPACE_ARGOCD"; then
        kubectl create namespace "$NAMESPACE_ARGOCD"
    fi
    kubectl create secret generic "$ARGOCD_REDIS_SECRET_NAME" -n "$NAMESPACE_ARGOCD" \
        --from-literal=redis-password="$password" \
        --dry-run=client -o yaml | kubectl apply -f - >/dev/null
    kubectl label secret "$ARGOCD_REDIS_SECRET_NAME" -n "$NAMESPACE_ARGOCD" part-of=microservices-platform --overwrite >/dev/null
    log_success "Secret $ARGOCD_REDIS_SECRET_NAME sincronizado en $NAMESPACE_ARGOCD."
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

for netpol in "$REDIS_NETPOL" "$ARGOCD_NETPOL"; do
    if [ ! -f "$netpol" ]; then
        log_error "No se encontró la política de red: $netpol"
        exit 1
    fi
done

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

# Políticas de red mínimas de Redis y ArgoCD antes de instalarlos (arrancan ya sellados)
log_info "Aplicando políticas de red de arranque (Redis y ArgoCD)..."
kubectl apply -f "$REDIS_NETPOL"
kubectl apply -f "$ARGOCD_NETPOL"
log_success "Políticas de red de arranque aplicadas."

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

ensure_redis_secrets

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
# Fase 5: Plataforma GitOps + Vault
# ============================================
log_info "=== FASE 5: Aplicando App of Apps e inicializando Vault ==="

kubectl apply -f "$ROOT_APP"
log_success "App of Apps aplicada. ArgoCD desplegará los componentes por waves."

wait_for_vault_pod

if vault_is_initialized; then
    log_info "Vault ya estaba inicializado; se usarán las claves de $VAULT_INIT_FILE."
    if [ ! -f "$VAULT_INIT_FILE" ]; then
        log_error "Vault está inicializado pero no existe $VAULT_INIT_FILE. Sin las claves no es posible desellarlo."
        exit 1
    fi
else
    vault_init
fi

vault_unseal
vault_seed_auth
restart_vault_config_operator
vault_seed_kv

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
echo "🔑 VAULT:"
echo "   Unseal keys y root token guardados en: $VAULT_INIT_FILE"
echo "   Haz un resguardo de ese archivo fuera del proyecto (está excluido de Git)."
echo "   Si Vault se reinicia y queda sellado, vuelve a ejecutar este script: es idempotente."
echo ""

log_success "¡Listo! La plataforma base está funcionando y ArgoCD está listo para gestionar el resto de la infraestructura."