[![Redis Operator](https://img.shields.io/badge/Redis%20Operator-2.1.0-blue)](https://github.com/OT-CONTAINER-KIT/redis-operator)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# Redis – Almacenamiento en caché (standalone)

Redis es un almacén de estructuras de datos en memoria utilizado como caché distribuido, broker de mensajes y base de datos NoSQL. En este proyecto se despliega en modo **standalone**, gestionado por el **Redis Operator comunitario** y el chart Helm asociado, garantizando una configuración simple y compatible con aplicaciones que no soportan Redis Cluster (por ejemplo, GitLab).

## 📖 Tabla de Contenidos

- [1. Introducción y decisión técnica](#1-introducción-y-decisión-técnica)
- [2. Documentación Oficial](#2-documentación-oficial)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación del operador](#4-instalación-del-operador)
- [5. Creación del Redis standalone](#5-creación-del-redis-standalone)
- [6. Detalles de Implementación](#6-detalles-de-implementación)
- [7. Integración con el Ecosistema](#7-integración-con-el-ecosistema)
- [8. Acceso](#8-acceso)
- [9. Guía de Operaciones Kubernetes](#9-guía-de-operaciones-kubernetes)
- [10. Desinstalación](#10-desinstalación)

---

## 1. Introducción y decisión técnica

**Redis Operator** (mantenido por `OT-CONTAINER-KIT`) permite gestionar instancias de Redis mediante recursos personalizados (`Redis`). Se eligió esta aproximación frente al clúster (`RedisCluster`) porque:

- **GitLab no es compatible con Redis Cluster**: las operaciones multi‑clave (sesiones, caché) fallan con el error `CROSSSLOT`.
- **Modo standalone**: más sencillo, consume menos recursos y es suficiente para entornos de desarrollo.
- **Flexibilidad**: el operador permite escalar a clúster más adelante si es necesario, pero con la configuración actual se despliega un único nodo sin réplicas.

---

## 2. Documentación Oficial

- [Redis Operator en GitHub](https://github.com/OT-CONTAINER-KIT/redis-operator)
- [Documentación de instalación](https://redis-operator.opstree.dev/docs/installation/)
- [redis_exporter](https://github.com/oliver006/redis_exporter)

---

## 3. Requisitos Previos

| Recurso | Versión / Detalle |
|---------|-------------------|
| Kubernetes | 1.19+ (probado en WSL2/Ubuntu) |
| kubectl | Configurado con acceso al clúster |
| Helm | 3.2+ |
| Namespace | `event-management` (donde residirá el Redis) |
| StorageClass | Debe existir uno (ej. `standard` en Minikube) |
| Prometheus Operator | para el ServiceMonitor (opcional) |

---

## 4. Instalación del operador

### 4.1 Agregar el repositorio Helm

```bash
helm repo add ot-helm https://ot-container-kit.github.io/helm-charts/
helm repo update
```

### 4.2 Validar e instalar el operador

Crea el archivo redis-operator-values.yaml (puedes usar el que ya tienes):

```yaml
# Etiquetas globales
commonLabels:
  part-of: microservices-platform

# Etiquetas para el pod del operador
podLabels:
  part-of: microservices-platform

# Etiquetas para el servicio (opcional)
serviceLabels:
  part-of: microservices-platform

replicaCount: 1

resources:
  requests:
    cpu: 100m
    memory: 128Mi
  limits:
    cpu: 200m
    memory: 256Mi

featureGates:
  GenerateConfigInInitContainer: true
```

Instala:

```bash

# Valida
helm upgrade --install redis-operator ot-helm/redis-operator \
  --namespace event-management \
  --create-namespace \
  -f redis-operator-sonarqube-values.yaml \
  --debug --dry-run
  
# Instala
helm upgrade --install redis-operator ot-helm/redis-operator \
  --namespace event-management \
  --create-namespace \
  -f redis-operator-sonarqube-values.yaml --debug
```


### 4.3 Verificar la instalación

```bash
kubectl get pods -n event-management -l app.kubernetes.io/name=redis-operator
```

## 5. Creación del Redis standalone

El repositorio `ot-helm` también proporciona un chart que crea directamente Redis standalone.

### 5.1 Crear archivo de valores personalizado

Crea un archivo `redis-values.yaml` con el siguiente contenido (ajustado a tu proyecto):

```yaml
# Etiquetas globales
labels:
  part-of: microservices-platform

# Configuración del recurso Redis (standalone)
redisStandalone:
  image: redis:7.0.5
  imagePullPolicy: IfNotPresent
  resources:
    requests:
      cpu: 100m
      memory: 256Mi
    limits:
      cpu: 250m
      memory: 500Mi
  persistence:
    enabled: true
    storageClassName: standard
    storageSize: 3Gi
  # Desactivar autenticación (opcional, para simplificar)
  redisSecret:
    secretName: ""

# ServiceMonitor para Prometheus
serviceMonitor:
  enabled: true
  labels:
    release: prometheus
  interval: 30s
```

### 5.2 Instalar Redis

```bash
# Valida
helm upgrade --install redis ot-helm/redis \
  --namespace event-management \
  -f redis-sonarqube-values.yaml --debug --dry-run
  
# Instala
helm upgrade --install redis ot-helm/redis \
  --namespace event-management \
  -f redis-sonarqube-values.yaml --debug
```

### 5.3 Verificar la instalación

```bash
kubectl get redis -n event-management
kubectl get pods -n event-management -l app.kubernetes.io/name=redis
```

## 6. Detalles de Implementación

### 6.1 Recurso RedisCluster
- `Autenticación`: desactivada (redisSecret.secretName: "")
- `storage`: PVC de 3Gi con StorageClass `standard`.
- `resources`: límites y solicitudes ajustados al clúster de 28GB.
- `serviceMonitor`: habilita ServiceMonitor automático para Prometheus.

### 6.2 Métricas Prometheus

El chart crea automáticamente un ServiceMonitor con la etiqueta `release: prometheus`. Esto permite que Prometheus Operator descubra las métricas de Redis.

### 6.2 Servicios expuestos

- `6379`: puerto Redis
- `9121`: puerto de métricas del exporter (solo para recolección)

## 7. Integración con el Ecosistema

### 7.1 Prometheus

Verifica que el ServiceMonitor esté presente:

```bash
kubectl get servicemonitor -n event-management
```

Confirma que las métricas llegan a Prometheus:

```bash
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090
```

Abre `http://localhost:9090/targets` y busca un target con el nombre `event-management/redis-cluster/0`. Debe estar `UP`.

Consulta métricas como `redis_connected_clients` o `redis_memory_used_bytes`.

### 7.2 Traefik (Ingress)

Redis no tiene interfaz web nativa. Si se necesita una UI de administración, se puede desplegar `Redis Insight` aparte y exponerla con IngressRoute. Ejemplo de despliegue rápido:

```bash
kubectl run redis-insight --image=redislabs/redisinsight --port=5540 -n event-management
kubectl expose pod redis-insight --port=5540 --target-port=5540 -n event-management
```

Luego crear IngressRoute:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: redis-insight-route
  namespace: event-management
  labels:
    part-of: microservices-platform
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`redis-insight.desarrollo`)
      kind: Rule
      services:
        - name: redis-insight
          port: 5540
```

## 8. Acceso

### 8.1 Port-forward

```bash
kubectl port-forward -n event-management svc/redis-cluster 6379:6379
```

### 8.2 Ingress con Traefik

No se expone Redis directamente por Ingress. Si se despliega Redis Insight, se accede por `http://redis-insight.desarrollo` (después de configurar DNS o `/etc/hosts`).

## 9. Guía de Operaciones Kubernetes

### 9.1 Verificar estado

```bash
kubectl get redisclusters -n event-management
kubectl get pods -n event-management -l app.kubernetes.io/name=redis-cluster
kubectl get pvc -n event-management -l app.kubernetes.io/name=redis-cluster
```

### 9.2 Logs

```bash
kubectl logs -n event-management redis-0
kubectl logs -n event-management deployment/redis-operator
```

### 9.3 Reiniciar Redis

```bash
kubectl delete pod -n event-management redis-0

# NOTA: El operador lo recreará automáticamente.
```

### 9.4 Solución de problemas comunes

```bash
kubectl delete pod -n event-management -l app.kubernetes.io/name=redis-cluster
```

### 9.5 Solución de problemas comunes

**PVC en estado Pending**: Verificar StorageClass (`kubectl get storageclass`). En Minikube, activar `minikube addons enable storage-provisioner`.

**Métricas no aparecen**: Comprobar que el ServiceMonitor tiene la etiqueta `release: prometheus` y que el puerto `metrics` está expuesto en el servicio.

**Pod no arranca**: Revisar logs y eventos (`kubectl describe pod`). Puede ser falta de recursos o configuración errónea.

**¿Por qué no usar Redis Cluster?**: GitLab no es compatible con Redis Cluster (`operaciones multi‑clave como sesiones y caché fallan con error CROSSSLOT`). Por eso se optó por un nodo standalone.

Si en el futuro se desea un clúster con alta disponibilidad: Se puede instalar el chart `ot-helm/redis-cluster` con la configuración adecuada, pero teniendo en cuenta que GitLab no funcionará correctamente con él.

## 10. Desinstalación

### 10.1 Eliminar Redis

```bash
helm uninstall redis -n event-management

```

### 10.2 Eliminar el operador

```bash
helm uninstall redis-operator -n event-management
```

### 10.3 Limpiar ServiceMonitor e IngressRoute (si existen)

```bash
kubectl delete servicemonitor redis -n event-management --ignore-not-found
kubectl delete ingressroute redis-insight-route -n event-management --ignore-not-found
kubectl delete svc redis-insight -n event-management --ignore-not-found
```

---