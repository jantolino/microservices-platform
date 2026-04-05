[![RabbitMQ Cluster Operator](https://img.shields.io/badge/RabbitMQ%20Cluster%20Operator-2.16.1-blue)](https://www.rabbitmq.com/kubernetes/operator/operator-overview)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)


# RabbitMQ Cluster Operator – Broker de mensajería para microservicios

## 📖 Tabla de Contenidos

- [1. Introducción y decisión técnica](#1-introducción-y-decisión-técnica)
- [2. Documentación Oficial](#2-documentación-oficial)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación](#4-instalación)
- [5. Detalles de Implementación](#5-detalles-de-implementación)
- [6. Integración con el Ecosistema](#6-integración-con-el-ecosistema)
- [7. Acceso](#7-acceso)
- [8. Guía de Operaciones Kubernetes](#8-guía-de-operaciones-kubernetes)
- [9. Desinstalación](#9-desinstalación)

---

## 1. Introducción y decisión técnica

**RabbitMQ Cluster Operator** es la solución oficial para desplegar y gestionar clústeres de RabbitMQ en Kubernetes. El operador está mantenido por el equipo de RabbitMQ.

### ¿Por qué no usar el chart de Bitnami?
Durante la evaluación se presentaron múltiples problemas con `bitnami/rabbitmq` :

| Problema | Impacto |
|----------|---------|
| Fallos constantes de imagen (`manifest unknown`) | Imposibilidad de desplegar |
| Sincronización deficiente entre chart e imágenes | Pérdida de tiempo en debugging |

El **operador oficial** resuelve estos inconvenientes: imágenes estables, actualizaciones seguras, y está recomendado por los propios desarrolladores de RabbitMQ.

---

## 2. Documentación Oficial

- [RabbitMQ Cluster Operator en GitHub](https://github.com/rabbitmq/cluster-operator)
- [Documentación oficial del operador](https://www.rabbitmq.com/kubernetes/operator/operator-overview)
- [Recurso RabbitmqCluster](https://www.rabbitmq.com/kubernetes/operator/using-operator)
- [Métricas Prometheus](https://www.rabbitmq.com/prometheus.html)

---

## 3. Requisitos Previos

| Recurso | Versión / Detalle |
|---------|-------------------|
| Kubernetes | 1.19+ (probado en WSL2/Ubuntu) |
| kubectl | Configurado con acceso al clúster |
| Namespace `event-management` | Para el operador (se crea automáticamente) |
| Namespace de destino | `event-management` (o el que se elija) |
| StorageClass | Debe existir uno (ej. `standard` en Minikube) |
| Prometheus Operator | Opcional, para el ServiceMonitor |

---

## 4. Instalación

### 4.1 Instalar el operador

```bash
kubectl apply -f https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml

# NOTA: para este caso se descargo el cluster-operator.yml y se modifico para que se instalara en el namespace event-management
```

Verifica que el operador esté funcionando:

```bash
kubectl get pods -n event-management
```

### 4.2 Crear un clúster de RabbitMQ

Define el archivo `rabbitmq-cluster.yaml`:

```yaml
apiVersion: rabbitmq.com/v1beta1
kind: RabbitmqCluster
metadata:
  name: rabbitmq
  namespace: event-management
  labels:
    part-of: microservices-platform
spec:
  replicas: 1   # Para desarrollo; usa 3 en producción
  image: rabbitmq:4.1.0-management
  resources:
    requests:
      cpu: 500m
      memory: 1Gi
    limits:
      cpu: 2
      memory: 2Gi
  persistence:
    storageClassName: standard
    storage: 10Gi
  rabbitmq:
    additionalConfig: |
      default_user = admin
      default_pass = changeme
      default_user_tags.administrator = true
      prometheus.return_per_object_metrics = true
  service:
    type: ClusterIP
```

Aplica:

```bash
kubectl apply -f rabbitmq-cluster.yaml
```

### 4.3 Verificar la instalación

```bash
kubectl get rabbitmqclusters -n event-management
kubectl get pods -n event-management -l app.kubernetes.io/name=rabbitmq
```

## 5. Detalles de Implementación

### 5.1 Recurso RabbitmqCluster

- `replicas`: número de nodos (1 para desarrollo)
- `image`: imagen oficial; usar `-management` para UI.
- `persistence`: PVC de 10Gi con StorageClass `standard`.
- `additionalConfig`: configuración del broker (usuarios, plugins, etc.)

### 5.2 Métricas Prometheus

El operador expone métricas en el puerto `15692`. Para que Prometheus las recolecte, se crea un ServiceMonitor (ver sección 6.1).

### 5.3 Servicios expuestos

- `5672`: AMQP (aplicaciones)
- `15672`: UI de gestión
- `15692`: métricas Prometheus

## 6. Integración con el Ecosistema

### 6.1 Prometheus

Crea `rabbitmq-service-monitor.yaml`:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: rabbitmq
  namespace: event-management
  labels:
    release: prometheus
    part-of: microservices-platform
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: rabbitmq
  endpoints:
    - port: prometheus
      interval: 30s
```

Aplica:

```bash
kubectl apply -f rabbitmq-service-monitor.yaml
```

### 6.2 Traefik (Ingress)

Crea `rabbitmq-ingress-route.yaml`:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: rabbitmq-ui-route
  namespace: event-management
  labels:
    part-of: microservices-platform
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`rabbitmq.desarrollo`)
      kind: Rule
      services:
        - name: rabbitmq
          port: 15672
```

Aplica y agrega la entrada en `/etc/hosts` si usas Minikube:

```bash
echo "$(minikube ip) rabbitmq.desarrollo" | sudo tee -a /etc/hosts
```

## 7. Acceso

### 7.1 Port-forward

```bash
kubectl port-forward -n event-management svc/rabbitmq 15672:15672
```

Abre `http://localhost:15672` con usuario `admin` y la contraseña definida.

### 7.2 Ingress con Traefik

Accede a `http://rabbitmq.desarrollo`.

## 8. Guía de Operaciones Kubernetes

### 8.1 Verificar estado

```bash
kubectl get rabbitmqclusters -n event-management
kubectl get pods -n event-management -l app.kubernetes.io/name=rabbitmq
kubectl get pvc -n event-management -l app.kubernetes.io/name=rabbitmq
```

### 8.2 Logs

```bash
kubectl logs -n event-management -l app.kubernetes.io/name=rabbitmq
kubectl logs -n event-management deployment/rabbitmq-cluster-operator
```

### 8.3 Escalar el clúster

```bash
kubectl edit rabbitmqcluster rabbitmq -n event-management
# Cambia spec.replicas y guarda
```

### 8.4 Reiniciar el clúster

```bash
kubectl delete pod -n event-management -l app.kubernetes.io/name=rabbitmq
```

### 8.5 Solución de problemas comunes

**PVC en estado Pending**: Verifica que el StorageClass existe y que el provisioner está activo.

**Pod no arranca**: Revisa logs y eventos con `kubectl describe pod`.

**Métricas no aparecen**: Comprueba que el ServiceMonitor tenga la etiqueta `release: prometheus`.

## 9. Desinstalación

### 9.1 Eliminar el clúster

```bash
kubectl delete rabbitmqcluster rabbitmq -n event-management
kubectl delete pvc -n event-management -l app.kubernetes.io/name=rabbitmq
```

### 9.2 Eliminar el operador

```bash
kubectl delete -f https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml

# Nota para hacer el delete si utiliza la versión modificada se debe apuntar al archivo cluster-operator.yml
```

### 9.3 Limpiar recursos adicionales

```bash
kubectl delete servicemonitor rabbitmq -n event-management
kubectl delete ingressroute rabbitmq-ui-route -n event-management
```

---

**Proyecto: microservices-architecture | Entorno: Kubernetes (WSL2/Ubuntu) con RabbitMQ Cluster Operator**
