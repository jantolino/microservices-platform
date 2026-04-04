[![ArgoCD](https://img.shields.io/badge/ArgoCD-v3.3.6-blue)](https://argoproj.github.io/cd/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-9.4.17-orange)](https://github.com/argoproj/argo-helm)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)


# ArgoCD – Gestión GitOps de despliegues en Kubernetes

ArgoCD es una herramienta de entrega continua declarativa (GitOps) que sincroniza el estado deseado de las aplicaciones definido en repositorios Git con el estado real del clúster de Kubernetes. En este proyecto se despliega en el namespace `devops-tools` con una configuración minimalista, utilizando Redis externo y exponiendo tanto la interfaz web como el CLI a través de Traefik.

## 📖 Tabla de Contenidos

- [1. Introducción](#1-introducción)
- [2. Documentación Oficial](#2-documentación-oficial)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación](#4-instalación)
- [5. Detalles de Implementación](#5-detalles-de-implementación)
- [6. Integración con el Ecosistema](#6-integración-con-el-ecosistema)
- [7. Acceso](#7-acceso)
- [8. Guía de Operaciones Kubernetes](#8-guía-de-operaciones-kubernetes)
- [9. Desinstalación](#9-desinstalación)

---

## 1. Introducción

ArgoCD implementa el patrón **GitOps** donde los manifiestos de Kubernetes se almacenan en un repositorio Git (por ejemplo, GitLab). El controlador de ArgoCD monitoriza continuamente el repositorio y aplica los cambios automáticamente en el clúster. Los componentes principales son:

- **argocd-server**: Interfaz web y API REST.
- **argocd-repo-server**: Clona repositorios y genera los manifiestos.
- **argocd-application-controller**: Orquesta la sincronización de aplicaciones.
- **argocd-redis**: Caché en memoria (se reutiliza un Redis externo para ahorrar recursos).

La instalación se realiza con el chart oficial `argo/argo-cd`, desactivando componentes opcionales (ApplicationSet, Notifications, Dex) y reduciendo los recursos para adaptarse al clúster de desarrollo.

---

## 2. Documentación Oficial

- [ArgoCD Helm Chart](https://github.com/argoproj/argo-helm/tree/main/charts/argo-cd)
- [Documentación de ArgoCD](https://argo-cd.readthedocs.io/)
- [GitOps con ArgoCD](https://argo-cd.readthedocs.io/en/stable/operator-manual/declarative-setup/)

---

## 3. Requisitos Previos

| Recurso | Versión / Detalle |
|---------|-------------------|
| Kubernetes | v1.35.1 (probado en WSL2/Ubuntu con Minikube v1.38.1) |
| kubectl | v1.34.1+ configurado con acceso al clúster |
| Helm | v3.18.3+ |
| Namespace `devops-tools` | Debe existir (se crea automáticamente con `--create-namespace`) |
| Redis externo | Servicio accesible en `redis.event-management.svc.cluster.local:6379` (sin autenticación) |
| Traefik | Ingress controller activo (para exponer la interfaz web y CLI) |
| Archivos `argocd-values.yaml` y `argocd-ingress-route.yaml` | Disponibles en el directorio de trabajo |

---

## 4. Instalación

### 4.1 Agregar repositorio de ArgoCD

```bash
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update
```

### 4.2 Validar configuración (dry-run)

```bash
helm upgrade --install argocd argo/argo-cd \
  --namespace devops-tools \
  -f argocd-sonarqube-values.yaml \
  --dry-run --debug
```

### 4.3 Instalar ArgoCD

```bash
helm upgrade --install argocd argo/argo-cd \
  --namespace devops-tools \
  -f argocd-sonarqube-values.yaml
```

### 4.4 Verificar la instalación

```bash
kubectl get pods -n devops-tools -w
```

Esperar a que los pods argocd-server, argocd-repo-server y argocd-application-controller estén en estado Running.

## 5. Detalles de Implementación

El archivo argocd-values.yaml personaliza la instalación para el entorno de desarrollo. A continuación se explican los bloques principales.

### 5.1 Etiquetado global

```yaml
global:
  additionalLabels:
    part-of: microservices-platform
```

Aplica la etiqueta part-of: microservices-platform a todos los recursos creados por el chart (pods, servicios, etc.), facilitando la identificación en el clúster.

### 5.2 Configuración del servidor

```yaml
server:
  ingress:
    enabled: false
  service:
    type: ClusterIP
  extraArgs:
    - --insecure
  resources:
    requests:
      cpu: 100m
      memory: 256Mi
    limits:
      cpu: 200m
      memory: 512Mi
```

Se desactiva el Ingress automático para delegar la exposición en Traefik.

El servicio se mantiene como ClusterIP; Traefik enrutará el tráfico externo.

El flag --insecure permite que el servidor acepte conexiones HTTP (sin TLS) ya que la terminación TLS se realiza en Traefik (si se configura) o se usa HTTP directamente.

Los recursos se ajustan a valores reducidos para evitar sobrecarga del clúster.

### 5.3 Redis externo

```yaml
externalRedis:
  host: redis.event-management.svc.cluster.local
  port: 6379
redis:
  enabled: false
```

Se desactiva el Redis interno del chart y se apunta a la instancia de Redis ya existente en el namespace event-management. Esto ahorra recursos y centraliza la gestión.

Nota: Si el Redis externo tuviera autenticación, sería necesario crear un secret y referenciarlo en externalRedis.existingSecret.

### 5.4 Componentes desactivados

```yaml
applicationSet:
  enabled: false
notifications:
  enabled: false
dex:
  enabled: false
```

ApplicationSet: No se necesita para gestionar aplicaciones de forma masiva.

Notifications: No se requieren alertas por ahora.

Dex: Se usa autenticación local (usuario admin), no SSO.

### 5.5 Recursos de los controladores

```yaml
controller:
  resources:
    requests:
      cpu: 250m
      memory: 512Mi
    limits:
      cpu: 500m
      memory: 1Gi
repo-server:
  resources:
    requests:
      cpu: 200m
      memory: 256Mi
    limits:
      cpu: 500m
      memory: 512Mi
```

Valores ajustados para un clúster de 28 GB, suficientes para operaciones normales de sincronización.

### 5.6 IngressRoute para Traefik

El archivo argocd-ingress-route.yaml expone ArgoCD externamente:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: argocd-server
  namespace: devops-tools
  labels:
    part-of: microservices-platform
spec:
  entryPoints:
    - web                     # Puerto 80 (sin TLS)
  routes:
    - kind: Rule
      match: Host(`argocd.desarrollo`)
      priority: 10
      services:
        - name: argocd-server
          port: 80
    - kind: Rule
      match: Host(`argocd.desarrollo`) && Headers(`Content-Type`, `application/grpc`)
      priority: 11
      services:
        - name: argocd-server
          port: 80
          scheme: h2c         # Necesario para tráfico gRPC (CLI)
```

Se utilizan dos reglas:

La primera (prioridad 10) maneja el tráfico web/API (HTTP/1.1).

La segunda (prioridad 11) identifica peticiones con cabecera Content-Type: application/grpc (gRPC) y las enruta con scheme: h2c (HTTP/2 sin TLS), permitiendo que el CLI de ArgoCD funcione correctamente.

No se incluye TLS porque el Traefik actual no tiene configurado un certResolver. Para habilitar HTTPS, sería necesario añadir un bloque tls y definir el certResolver en Traefik.

## 6. Integración con el Ecosistema

### 6.1 Traefik (Ingress)

Una vez aplicado el IngressRoute, se puede acceder a ArgoCD a través de http://argocd.desarrollo (previo ajuste de DNS local). El CLI también se conecta mediante gRPC sin configuración adicional.

### 6.2 Prometheus

El chart de ArgoCD expone métricas en los puertos 8082 (servidor) y 8084 (controlador). Para integrar con Prometheus, se puede crear un ServiceMonitor:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: argocd-server
  namespace: devops-tools
  labels:
    release: prometheus
    part-of: microservices-platform
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: argocd-server
  endpoints:
    - port: metrics
      interval: 30s
```

### 6.3 ELK (Logs)

ArgoCD escribe logs en stdout; Filebeat los recoge y envía a Elasticsearch. Se pueden consultar en Kibana filtrando por kubernetes.namespace=devops-tools y kubernetes.container.name.

### 6.4 Redis externo

La conexión a Redis se realiza mediante el nombre de servicio redis.event-management.svc.cluster.local. Asegurarse de que el Redis esté funcionando y sea accesible desde el namespace devops-tools.

## 7. Acceso

### 7.1 Obtener la contraseña de administrador

El usuario para acceder desde la interfaz web es  `admin`
La contraseña inicial se genera automáticamente y se almacena en un secret:

```bash
kubectl get secret argocd-initial-admin-secret -n devops-tools -o jsonpath="{.data.password}" | base64 --decode
```

### 7.2 Port-forward (acceso local rápido)

```bash
kubectl port-forward -n devops-tools svc/argocd-server 8080:80
```

Acceder a http://localhost:8080 con usuario admin y la contraseña obtenida.

### 7.3 Ingress con Traefik

Una vez aplicado el IngressRoute, acceder a http://argocd.desarrollo. Credenciales: admin / contraseña del secret.

### 7.4 Acceso mediante CLI

Instalar el CLI de ArgoCD desde https://github.com/argoproj/argo-cd/releases. Luego iniciar sesión:

```bash
argocd login argocd.desarrollo --username admin --password $(kubectl get secret argocd-initial-admin-secret -n devops-tools -o jsonpath="{.data.password}" | base64 --decode)
```

## 8. Guía de Operaciones Kubernetes

### 8.1 Verificar estado

```bash
kubectl get pods -n devops-tools -l app.kubernetes.io/name=argocd-server
kubectl get svc -n devops-tools argocd-server
```

### 8.2 Logs

```bash
# Logs del servidor
kubectl logs -n devops-tools deployment/argocd-server

# Logs del controlador
kubectl logs -n devops-tools deployment/argocd-application-controller
```

### 8.3 Reiniciar un componente

```bash
kubectl rollout restart deployment argocd-server -n devops-tools
```

### 8.4 Solución de problemas comunes

**No se puede iniciar sesión en la web:** Verificar que la contraseña se haya obtenido correctamente del secret. Si el secret no existe, puede deberse a que la instalación no se completó; revisar los logs de argocd-server.

**El CLI no se conecta:** Asegurar que la segunda regla del IngressRoute esté activa y que scheme: h2c esté presente. Probar conectividad gRPC con argocd login --grpc-web.

**Error de conexión a Redis:** Comprobar que el servicio redis.event-management.svc.cluster.local exista y sea accesible desde el namespace devops-tools:

```bash
kubectl run -it --rm test --image=busybox -n devops-tools -- nslookup redis.event-management.svc.cluster.local
```

**Alto consumo de recursos:** Si el clúster está saturado, reducir aún más los requests en argocd-values.yaml (por ejemplo, cpu: 50m para el servidor).

## 9. Desinstalación

### 9.1 Eliminar el release de Helm

```bash
helm uninstall argocd -n devops-tools
```

### 9.2 Eliminar recursos residuales

```bash
kubectl delete pvc -n devops-tools -l app.kubernetes.io/name=argocd --ignore-not-found
kubectl delete secret argocd-initial-admin-secret -n devops-tools --ignore-not-found
kubectl delete ingressroute argocd-server -n devops-tools --ignore-not-found
```
