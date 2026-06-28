## Este caso se deja como ejemplo practico para cuando se necesite desplegar el 
## dashboard de forma separada, para este proyecto no es necesario ya que se
## trabaja con el dashboard de minikube.

# Desplegar Kubernetes Dashboard usando Helm según la documentación oficial
echo "Desplegando Kubernetes Dashboard usando Helm..."
helm repo add kubernetes-dashboard https://kubernetes.github.io/dashboard/
helm upgrade --install kubernetes-dashboard kubernetes-dashboard/kubernetes-dashboard --namespace kubernetes-dashboard

# Obtener el directorio actual donde se encuentra el script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Crear usuario administrador para el Dashboard
echo "Creando usuario administrador para el Dashboard..."
kubectl apply -f "$SCRIPT_DIR/dashboard-01-service-account.yaml"
kubectl apply -f "$SCRIPT_DIR/dashboard-02-secret.yaml"

# Guardar el token de acceso en un archivo
echo "Generando token de acceso y guardándolo en dashboard-user-token.txt..."
TOKEN=$(kubectl get secret k8s-admin -n kubernetes-dashboard -o jsonpath="{.data.token}" | base64 -d)
echo "$TOKEN" > "$SCRIPT_DIR/dashboard-user-token.txt"

# Iniciar el proxy
echo "Iniciando el proxy..."
kubectl -n kubernetes-dashboard port-forward svc/kubernetes-dashboard-kong-proxy 8443:443