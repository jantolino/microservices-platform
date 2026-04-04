[![JFrog Artifactory OSS](https://img.shields.io/badge/JFrog%20Artifactory-107.133.17-blue)](https://jfrog.com/artifactory/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-107.133.17-orange)](https://github.com/jfrog/charts)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# JFrog Artifactory OSS – Repositorio universal de artefactos

JFrog Artifactory (Open Source Edition) actúa como el repositorio centralizado para gestionar artefactos de construcción (binarios, imágenes Docker, paquetes, etc.). Se despliega mediante el chart oficial `jfrog/artifactory-oss`  y se integra con PostgreSQL externo y el ingress controller Traefik.

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

Artifactory OSS es un repositorio universal que almacena artefactos de cualquier tipo (Docker, Maven, npm, Helm, etc.). En este proyecto se utiliza para:

- Almacenar imágenes Docker generadas por Jenkins.
- Cachear dependencias de construcción (Maven, npm, etc.).
- Distribuir charts de Helm para despliegues en Kubernetes.

La instalación se realiza con el chart oficial de JFrog, utilizando una base de datos PostgreSQL externa (ya existente en el namespace `devops-tools` ) y desactivando componentes innecesarios (Nginx, Ingress automático) para delegar la exposición en Traefik.

---

## 2. Documentación Oficial

- [JFrog Artifactory OSS Helm Chart](https://github.com/jfrog/charts/tree/master/stable/artifactory-oss)
- [Documentación de Artifactory OSS](https://jfrog.com/open-source/)

---

## 3. Requisitos Previos

| Recurso | Versión / Detalle |
|---------|-------------------|
| Kubernetes | v1.35.1 (probado en WSL2/Ubuntu con Minikube v1.38.1) |
| kubectl | v1.34.1+ configurado con acceso al clúster |
| Helm | v3.18.3+ |
| Namespace `devops-tools` | Debe existir (se crea automáticamente con `--create-namespace`) |
| StorageClass | `standard` (u otro) disponible en el clúster |
| PostgreSQL externa | Instancia accesible en el mismo namespace (ej. `postgresql.devops-tools.svc.cluster.local`) |
| Base de datos `artifactory` y usuario `artifactory` | Creados previamente en PostgreSQL (ver sección 5.3) |
| Traefik | Ingress controller activo (para exponer la interfaz web) |
| Archivo `jfrog-artifactory-oss-values.yaml` | Disponible en el directorio de trabajo |

---

## 4. Instalación

### 4.1 Agregar repositorio de JFrog

```bash
helm repo add jfrog https://charts.jfrog.io
helm repo update
```

### 4.2 Validar configuración (dry-run)

```bash
helm upgrade --install artifactory jfrog/artifactory-oss \
  --namespace devops-tools \
  -f artifactory-oss-values.yaml \
  --dry-run --debug
```

### 4.3 Instalar Artifactory

```bash
helm upgrade --install artifactory jfrog/artifactory-oss \
  --namespace devops-tools \
  -f artifactory-oss-values.yaml
```

### 4.4 Verificar la instalación

```bash
kubectl get pods -n devops-tools -l app=artifactory -w
```

Esperar a que el pod artifactory-0 pase a estado Running (puede tardar varios minutos).

## 5. Detalles de Implementación

El archivo jfrog-artifactory-oss-values.yaml personaliza la instalación para el entorno de desarrollo. A continuación se explican los bloques principales.

### 5.1 Etiquetado global

```yaml
commonLabels:
  part-of: microservices-platform
```

Aplica la etiqueta a todos los recursos creados por el chart (StatefulSet, Service, Pods), facilitando la identificación en el clúster.

### 5.2 Recursos asignados

```yaml
artifactory:
  resources:
    requests:
      cpu: "500m"
      memory: "2Gi"
    limits:
      cpu: "1"
      memory: "4Gi"
  javaOpts:
    xms: "1g"
    xmx: "2g"
```

Se definen solicitudes y límites de CPU y memoria ajustados al clúster de 28 GB. Los parámetros xms y xmx configuran el heap de Java para evitar un consumo excesivo.

### 5.3 Base de datos externa (preparación previa obligatoria)

Antes de instalar Artifactory, debe existir una base de datos artifactory y un usuario artifactory en PostgreSQL con la contraseña definida en el values.yaml. Ejecutar en el pod de PostgreSQL:

```bash
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "CREATE USER artifactory WITH PASSWORD 'artifactory456';"
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "CREATE DATABASE artifactory OWNER artifactory;"
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE artifactory TO artifactory;"
```

Luego, en el archivo values.yaml:

```yaml
postgresql:
  enabled: false

database:
  type: postgresql
  driver: org.postgresql.Driver
  url: jdbc:postgresql://postgresql.devops-tools.svc.cluster.local:5432/artifactory
  user: artifactory
  password: "artifactory456"
```

Se desactiva el PostgreSQL interno del chart y se apunta a la instancia externa.

### 5.4 Desactivación de Nginx e Ingress automático

```yaml
nginx:
  enabled: false

ingress:
  enabled: false
```

Se evita que el chart cree su propio Ingress y su servidor Nginx, ya que la exposición externa se realizará mediante un IngressRoute de Traefik (ver sección 6.2).

### 5.5 Persistencia

El chart crea automáticamente un PVC para almacenar los datos de Artifactory (/var/opt/jfrog/artifactory). Por defecto se usa el StorageClass standard con tamaño dinámico.

## 6. Integración con el Ecosistema

### 6.1 Prometheus

Artifactory expone métricas en el puerto 8082 en la ruta /metrics. Para integrarlo con Prometheus, se puede crear un ServiceMonitor con la etiqueta release: prometheus. Ejemplo:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: artifactory
  namespace: devops-tools
  labels:
    release: prometheus
    part-of: microservices-platform
spec:
  selector:
    matchLabels:
      app: artifactory
  endpoints:
    - port: artifactory
      interval: 30s
      path: /metrics
```

### 6.2 Traefik (Ingress)

Para exponer Artifactory externamente, se utiliza un IngressRoute (archivo artifactory-ingress-route.yaml):

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: artifactory-ui-route
  namespace: devops-tools
  labels:
    part-of: microservices-platform
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`artifactory.desarrollo`)
      kind: Rule
      services:
        - name: artifactory
          port: 8082
```

Aplicar:

```bash
kubectl apply -f artifactory-ingress-route.yaml
```

Luego añadir la entrada en /etc/hosts apuntando a la IP del clúster (ej. minikube ip).

### 6.3 ELK y Jaeger

**Logs:** Artifactory escribe logs en stdout; Filebeat los recoge y envía a Elasticsearch. Se pueden consultar en Kibana filtrando por kubernetes.namespace=devops-tools y kubernetes.container.name=artifactory.

**Trazas:** No se configura de forma nativa, pero se puede instrumentar con OpenTelemetry si es necesario.

## 7. Acceso

### 7.1 Credenciales iniciales

El usuario por defecto es admin y la contraseña inicial es password (sin comillas). Tras el primer inicio de sesión, el sistema obligará a cambiar la contraseña, contrseña utilizada tras el cambio: "Password123**".

**Nota:** No es necesario extraer ninguna contraseña de secretos de Kubernetes; las credenciales por defecto son las indicadas.

### 7.2 Port-forward

```bash
kubectl port-forward -n devops-tools svc/artifactory 8082:8082
```

Acceder a http://localhost:8082 con usuario admin y contraseña password.

### 7.3 Ingress con Traefik

Una vez aplicado el IngressRoute, acceder a http://artifactory.desarrollo (previo ajuste de DNS local). Credenciales idénticas.

## 8. Guía de Operaciones Kubernetes

### 8.1 Verificar estado

```bash
kubectl get statefulset,svc,pod -n devops-tools -l app=artifactory
kubectl get pvc -n devops-tools -l app=artifactory
```

### 8.2 Logs

```bash
# Logs del contenedor principal
kubectl logs -n devops-tools artifactory-0 -c artifactory -f

# Logs de otros contenedores (router, frontend, etc.)
kubectl logs -n devops-tools artifactory-0 -c router
```

### 8.3 Reiniciar Artifactory

```bash
kubectl delete pod -n devops-tools artifactory-0
```

El StatefulSet lo recreará automáticamente.

### 8.4 Solución de problemas comunes

**Pod en CrashLoopBackOff** – Revisar logs con kubectl logs. Los errores más frecuentes ocurridos en esta instalación:

**Fallo de autenticación en PostgreSQL:** Verificar que la base de datos y el usuario existan y que la contraseña en database.password coincida. Conectar manualmente desde un pod de prueba:

```bash
kubectl run -it --rm debug --image=postgres:15 -n devops-tools -- bash
psql "host=postgresql.devops-tools.svc.cluster.local port=5432 user=artifactory dbname=artifactory password=artifactory456"
```

**No se puede acceder a la interfaz:** Verificar que el servicio artifactory esté en ejecución y que el IngressRoute apunte al puerto 8082. Probar con port-forward.

## 9. Desinstalación

### 9.1 Eliminar el release de Helm

```bash
helm uninstall artifactory -n devops-tools
```

### 9.2 Eliminar PVCs residuales (opcional)

```bash
kubectl delete pvc -n devops-tools -l app=artifactory
```

### 9.3 Eliminar recursos adicionales

```bash
kubectl delete servicemonitor artifactory -n devops-tools --ignore-not-found
kubectl delete ingressroute artifactory-ui-route -n devops-tools --ignore-not-found
```
