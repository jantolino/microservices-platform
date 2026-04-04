[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18.5.15-blue)](https://www.postgresql.org/)
[![pgAdmin](https://img.shields.io/badge/pgAdmin-8.14-blue)](https://www.pgadmin.org/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-18.5.15-orange)](https://github.com/bitnami/charts/tree/main/bitnami/postgresql)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1-326CE5)](https://kubernetes.io/)

# PostgreSQL y pgAdmin – Base de datos y administración gráfica

Este directorio contiene las configuraciones para desplegar **PostgreSQL** (usando el chart de Bitnami con imágenes `bitnamilegacy`) y **pgAdmin** (interfaz gráfica de administración) dentro del clúster de Kubernetes. Ambos componentes se integran con el ecosistema de microservicios, proporcionando almacenamiento persistente y una herramienta visual para gestionar las bases de datos de GitLab, Nexus y otras aplicaciones.

## 📖 Tabla de Contenidos

- [1. Introducción](#1-introducción)
- [2. Documentación Oficial](#2-documentación-oficial)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación de PostgreSQL](#4-instalación-de-postgresql)
- [5. Instalación de pgAdmin](#5-instalación-de-pgadmin)
- [6. Detalles de Implementación](#6-detalles-de-implementación)
- [7. Integración con el Ecosistema](#7-integración-con-el-ecosistema)
- [8. Acceso](#8-acceso)
- [9. Guía de Operaciones Kubernetes](#9-guía-de-operaciones-kubernetes)
- [10. Desinstalación](#10-desinstalación)

---

## 1. Introducción

**PostgreSQL** es el sistema de base de datos relacional utilizado para almacenar los datos de aplicaciones como GitLab y Nexus. Se despliega en modo **standalone** (un solo nodo) con persistencia, utilizando la imagen `bitnamilegacy/postgresql` para evitar problemas de licencia. **pgAdmin** proporciona una interfaz web para administrar las bases de datos, conectándose al servicio PostgreSQL interno.

La configuración incluye la creación de dos bases de datos y dos usuarios (`nexus`  y `gitlab`) para aislar los datos de cada aplicación, así como recursos ajustados al clúster de 28 GB.

---

## 2. Documentación Oficial

- [PostgreSQL Helm Chart (Bitnami)](https://github.com/bitnami/charts/tree/main/bitnami/postgresql)
- [Imagen bitnamilegacy/postgresql](https://hub.docker.com/r/bitnamilegacy/postgresql)
- [pgAdmin4 Helm Chart (runix)](https://github.com/runix-charts/pgadmin4)
- [Documentación de PostgreSQL](https://www.postgresql.org/docs/)
- [Documentación de pgAdmin](https://www.pgadmin.org/docs/)

---

## 3. Requisitos Previos

| Recurso | Versión / Detalle |
|---------|-------------------|
| Kubernetes | v1.35.1 (probado en WSL2/Ubuntu con Minikube v1.38.1) |
| kubectl | v1.34.1+ configurado con acceso al clúster |
| Helm | v3.18.3+ |
| Namespace `devops-tools` | Debe existir (se crea automáticamente con `--create-namespace`) |
| StorageClass | Debe existir uno (ej. `standard` en Minikube) |
| Archivos `postgresql-values.yaml` y `pgadmin-values.yaml` | Disponibles en el directorio de trabajo |

---

## 4. Instalación de PostgreSQL

### 4.1 Agregar repositorio de Bitnami

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

### 4.2 Crear archivo postgresql-values.yaml

```yaml
# postgresql-values.yaml
# PostgreSQL para GitLab y Nexus con imagen bitnamilegacy

commonLabels:
  part-of: microservices-platform

image:
  registry: docker.io
  repository: bitnamilegacy/postgresql
  tag: 17.6.0-debian-12-r0   # Ajustar según disponibilidad en Docker Hub
  pullPolicy: IfNotPresent

auth:
  postgresPassword: "postgres123"
  username: "nexus"
  password: "nexus456"
  database: "nexus"

# Script adicional para crear usuario y base de datos de GitLab
initdbScripts:
  create-gitlab-db.sql: |
    CREATE USER gitlab WITH PASSWORD 'gitlab789';
    CREATE DATABASE gitlabhq_production OWNER gitlab;
    GRANT ALL PRIVILEGES ON DATABASE gitlabhq_production TO gitlab;

architecture: standalone
primary:
  resources:
    requests:
      cpu: 250m
      memory: 512Mi
    limits:
      cpu: 500m
      memory: 1Gi
  persistence:
    enabled: true
    storageClass: "standard"
    size: 10Gi

metrics:
  enabled: true
  serviceMonitor:
    enabled: true
    labels:
      release: prometheus
    interval: 30s
```

### 4.3 Validar configuración (dry-run)

```bash
helm upgrade --install postgresql bitnami/postgresql \
  --namespace devops-tools \
  --create-namespace \
  -f postgresql-values.yaml \
  --dry-run --debug
```

### 4.4 Instalar PostgreSQL

```bash
helm upgrade --install postgresql bitnami/postgresql \
  --namespace devops-tools \
  --create-namespace \
  -f postgresql-values.yaml
```

### 4.5 Verificar la instalación

```bash
kubectl get pods -n devops-tools -l app.kubernetes.io/name=postgresql -w
kubectl logs -n devops-tools postgresql-0
```

## 5. Instalación de pgAdmin

### 5.1 Agregar repositorio de pgAdmin

```bash
helm repo add runix https://helm.runix.net
helm repo update
```

### 5.2 Crear archivo pgadmin-values.yaml

```yaml
# pgadmin-values.yaml
commonLabels:
  part-of: microservices-platform

image:
  repository: dpage/pgadmin4
  tag: "8.14"
  pullPolicy: IfNotPresent

env:
  email: "admin@example.com"            # Usar un dominio real
  password: "pgadmin123"
  enhanced_cookie_protection: "False"

serverDefinitions:
  enabled: true
  servers:
    dev-postgres:
      Name: "PostgreSQL - devops-tools"
      Group: "Servers"
      Port: 5432
      Username: "postgres"
      Password: "postgres123"
      Host: "postgresql.devops-tools.svc.cluster.local"
      SSLMode: "prefer"
      MaintenanceDB: "postgres"

persistentVolume:
  enabled: true
  size: 1Gi
  storageClass: "standard"

service:
  type: ClusterIP
  port: 80

ingress:
  enabled: false

resources:
  requests:
    cpu: 200m
    memory: 512Mi
  limits:
    cpu: 500m
    memory: 1Gi

# Desactivar startup probe (evita reinicios prematuros)
startupProbe: {}

readinessProbe:
  httpGet:
    path: /misc/ping
    port: 80
  initialDelaySeconds: 60
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 6

livenessProbe:
  httpGet:
    path: /misc/ping
    port: 80
  initialDelaySeconds: 120
  periodSeconds: 20
  timeoutSeconds: 5
  failureThreshold: 3
```

### 5.3 Validar configuración (dry-run)

```bash
helm upgrade --install pgadmin runix/pgadmin4 \
  --namespace devops-tools \
  -f pgadmin-values.yaml \
  --dry-run --debug
```

### 5.4 Instalar pgAdmin

```bash
helm upgrade --install pgadmin runix/pgadmin4 \
  --namespace devops-tools \
  -f pgadmin-values.yaml
```

### 5.5 Verificar la instalación

```bash
kubectl get pods -n devops-tools -l app.kubernetes.io/name=pgadmin4 -w
kubectl logs -n devops-tools -l app.kubernetes.io/name=pgadmin4
```

**Nota:** Si el pod tarda en arrancar, puede deberse a la validación del email. Asegúrese de usar un dominio real (ej. example.com) en env.email.

## 6. Detalles de Implementación

### 6.1 PostgreSQL

| Parámetro | Valor | Explicación |
|-----------|-------|-------------|
| image.repository | bitnamilegacy/postgresql | Imagen legacy de Bitnami, sin restricciones comerciales. |
| auth.postgresPassword | postgres123 | Contraseña del superusuario postgres. |
| initdbScripts | Script SQL | Crea el usuario gitlab y la base de datos gitlabhq_production. |
| primary.persistence.size | 10Gi | Volumen persistente para datos de PostgreSQL. |
| resources.requests.cpu | 250m | Solicitud mínima de CPU para entornos limitados. |
| metrics.serviceMonitor | enabled: true | Expone métricas para Prometheus. |

### 6.2 pgAdmin

| Parámetro | Valor | Explicación |
|-----------|-------|-------------|
| env.email | admin@example.com | Email de acceso a pgAdmin. |
| serverDefinitions | Preconfigura conexión a PostgreSQL | Evita configurar manualmente el servidor. |
| persistentVolume.size | 1Gi | Almacena configuraciones de conexiones. |
| resources.requests.memory | 512Mi | Memoria suficiente para la interfaz web. |
| startupProbe | {} | Desactivada para evitar reinicios por tiempos de arranque. |

### 6.3 Etiquetado y coherencia

Ambos charts utilizan `commonLabels: part-of: microservices-platform` para identificar los recursos como parte de la arquitectura de microservicios.

## 7. Integración con el Ecosistema

### 7.1 Prometheus

**PostgreSQL:** el chart habilita `metrics.serviceMonitor` con la etiqueta `release: prometheus`, lo que permite que Prometheus Operator descubra el endpoint de métricas (postgresql-metrics en el puerto 9187).

**pgAdmin:** no expone métricas por defecto, pero se puede añadir un ServiceMonitor manual si se desea monitorizar su estado.

### 7.2 Traefik (Ingress)

Para exponer pgAdmin externamente, cree un IngressRoute similar a otros componentes. Ejemplo:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: pgadmin-ui-route
  namespace: devops-tools
  labels:
    part-of: microservices-platform
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`pgadmin.desarrollo`)
      kind: Rule
      services:
        - name: pgadmin-pgadmin4
          port: 80
```

Luego añada `pgadmin.desarrollo` a `/etc/hosts` apuntando a la IP del clúster.

### 7.3 ELK y Jaeger

**Logs:** PostgreSQL y pgAdmin escriben logs en stdout, que son recogidos por Filebeat y enviados a Elasticsearch. Se pueden consultar en Kibana filtrando por `kubernetes.namespace=devops-tools`.

**Trazas:** No aplica directamente, pero las aplicaciones que usen la base de datos pueden instrumentarse con OpenTelemetry.

## 8. Acceso

### 8.1 Acceso a PostgreSQL

**Port-forward:**

```bash
kubectl port-forward -n devops-tools svc/postgresql 5432:5432
```

Luego conéctese con:

```bash
psql -h localhost -U postgres -d postgres
```

Contraseña: `postgres123`.

**Desde dentro del clúster:**

Use el nombre del servicio `postgresql.devops-tools.svc.cluster.local:5432`.

### 8.2 Acceso a pgAdmin

**Port-forward (rápido):**

```bash
kubectl port-forward -n devops-tools svc/pgadmin-pgadmin4 8080:80
```

Abrir http://localhost:8080. Credenciales: `admin@example.com` / `pgadmin123`.

**Ingress (recomendado):**

Una vez creado el IngressRoute, acceda a http://pgadmin.desarrollo.

## 9. Guía de Operaciones Kubernetes

### 9.1 Verificar estado

```bash
# Pods de PostgreSQL
kubectl get pods -n devops-tools -l app.kubernetes.io/name=postgresql

# Pods de pgAdmin
kubectl get pods -n devops-tools -l app.kubernetes.io/name=pgadmin4

# PVCs
kubectl get pvc -n devops-tools -l app.kubernetes.io/name=postgresql
kubectl get pvc -n devops-tools -l app.kubernetes.io/name=pgadmin4
```

### 9.2 Logs

```bash
# Logs de PostgreSQL
kubectl logs -n devops-tools postgresql-0

# Logs de pgAdmin
kubectl logs -n devops-tools -l app.kubernetes.io/name=pgadmin4
```

### 9.3 Reiniciar

```bash
# Reiniciar PostgreSQL (StatefulSet)
kubectl delete pod -n devops-tools postgresql-0

# Reiniciar pgAdmin (Deployment)
kubectl rollout restart deployment pgadmin-pgadmin4 -n devops-tools
```

### 9.4 Solución de problemas comunes

**ImagePullBackOff en PostgreSQL:** Verificar que el tag `17.6.0-debian-12-r0` exista en Docker Hub. Si no, buscar un tag alternativo en `bitnamilegacy/postgresql`.

**pgAdmin no arranca:** Asegurar que `env.email` sea un dominio real (no `.local`). Si falla, usar `admin@example.com`. También aumentar recursos o desactivar `startupProbe`.

**Conexión a PostgreSQL desde pgAdmin:** Verificar que el servicio `postgresql` esté accesible (`kubectl get svc -n devops-tools`) y que el usuario/contraseña coincidan.

**Métricas no aparecen en Prometheus:** Comprobar que el ServiceMonitor de PostgreSQL tenga la etiqueta `release: prometheus` y que el Prometheus Operator esté funcionando.

## 10. Desinstalación

### 10.1 Eliminar PostgreSQL

```bash
helm uninstall postgresql -n devops-tools
kubectl delete pvc -n devops-tools -l app.kubernetes.io/name=postgresql
```

### 10.2 Eliminar pgAdmin

```bash
helm uninstall pgadmin -n devops-tools
kubectl delete pvc -n devops-tools -l app.kubernetes.io/name=pgadmin4
```

### 10.3 Eliminar recursos adicionales

```bash
kubectl delete servicemonitor -n devops-tools postgresql
kubectl delete ingressroute pgadmin-ui-route -n devops-tools
```
