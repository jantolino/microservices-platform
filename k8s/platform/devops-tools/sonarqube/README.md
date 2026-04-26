[![SonarQube](https://img.shields.io/badge/SonarQube-Community%20Build%20v26.3.0.120487-blue)](https://www.sonarqube.org/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-2026.2.1-orange)](https://github.com/SonarSource/helm-chart-sonarqube)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# SonarQube – Análisis estático de código para microservicios

SonarQube es la plataforma de inspección continua de código que detecta bugs, vulnerabilidades y code smells en más de 30 lenguajes. Se despliega en el namespace `devops-tools` utilizando el chart oficial de SonarSource, conectándose a una base de datos PostgreSQL externa y exponiendo su interfaz a través de Traefik.

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

SonarQube se integra en el pipeline de CI/CD (Jenkins) para garantizar la calidad y seguridad del código de los microservicios. Sus principales funciones son:

- **Detección de errores y vulnerabilidades** en el código fuente.
- **Análisis de deuda técnica** y mantenibilidad.
- **Histórico de métricas** para medir la evolución de la calidad.
- **Integración con Jenkins** mediante el plugin de SonarQube o el escáner CLI.

La instalación se realiza con el chart oficial `sonarqube/sonarqube`, utilizando la edición Community (gratuita), desactivando la base de datos embebida y usando PostgreSQL externo. Los recursos se ajustan a valores reducidos para el clúster de desarrollo, y la exposición se delega en Traefik.

---

## 2. Documentación Oficial

- [SonarQube Helm Chart en Artifact Hub](https://artifacthub.io/packages/helm/sonarqube/sonarqube)
- [Repositorio GitHub de SonarSource](https://github.com/SonarSource/helm-chart-sonarqube)
- [Documentación de instalación en Kubernetes](https://docs.sonarsource.com/sonarqube-server/server-installation/on-kubernetes-or-openshift/)
- [Personalización del Helm chart](https://docs.sonarsource.com/sonarqube-server/server-installation/on-kubernetes-or-openshift/customizing-helm-chart)
- [Quality Gates](https://docs.sonarsource.com/sonarqube/latest/user-guide/quality-gates/)
- [Integración CI/CD](https://docs.sonarsource.com/sonarqube-server/2026.1/devops-platform-integration)

---

## 3. Requisitos Previos

| Recurso | Versión / Detalle |
|---------|-------------------|
| Kubernetes | v1.35.1 (probado en WSL2/Ubuntu con Minikube v1.38.1) |
| kubectl | v1.34.1+ configurado con acceso al clúster |
| Helm | v3.18.3+ |
| Namespace `devops-tools` | Debe existir (se crea automáticamente con `--create-namespace`) |
| PostgreSQL externa | Instancia accesible en el mismo namespace con base de datos `sonarqube` y usuario `sonarqube` |
| StorageClass | `standard` (u otro) disponible en el clúster |
| Traefik | Ingress controller activo (para exponer la interfaz web) |
| Archivos `sonarqube-values.yaml` y `sonarqube-ingress-route.yaml` | Disponibles en el directorio de trabajo |

> **Nota:** SonarQube **no** incluye una base de datos embebida para producción. Es obligatorio usar PostgreSQL externo.

---

## 4. Instalación

### 4.1 Preparar la base de datos PostgreSQL

Antes de instalar SonarQube, debe existir la base de datos y el usuario en PostgreSQL. Ejecutar:

```bash
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "CREATE USER sonarqube WITH PASSWORD 'sonarqube456';"
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "CREATE DATABASE sonarqube OWNER sonarqube;"
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE sonarqube TO sonarqube;"
```

### 4.2 Crear archivo sonarqube-values.yaml

```yaml
# sonarqube-values.yaml

# Etiqueta para los pods (coherencia con la arquitectura)
podLabels:
  part-of: microservices-platform

# Indicar explícitamente que se usa la edición Community
community:
  enabled: true

# Monitoring passcode (obligatorio para las sondas de salud)
monitoringPasscode: "sonarqube-passcode-2026"

# Reducción de recursos (requests y limits)
resources:
  requests:
    cpu: 500m
    memory: 2Gi
  limits:
    cpu: 1000m
    memory: 4Gi

# Habilitar persistencia para los datos de Elasticsearch
persistence:
  enabled: true
  storageClass: "standard"
  size: 10Gi

# Configuración de la base de datos externa PostgreSQL
jdbcOverwrite:
  enabled: true
  jdbcUrl: "jdbc:postgresql://postgresql.devops-tools.svc.cluster.local:5432/sonarqube"
  jdbcUsername: "sonarqube"
  jdbcPassword: "sonarqube456"   # Contraseña en texto plano (desarrollo)

# Activar el exportador de métricas para Prometheus
prometheusExporter:
  enabled: true

# Desactivar los tests (no necesarios en desarrollo)
tests:
  enabled: false
```

### 4.3 Validar configuración (dry-run)

```bash
helm repo add sonarqube https://SonarSource.github.io/helm-chart-sonarqube
helm repo update

helm upgrade --install sonarqube sonarqube/sonarqube \
  --namespace devops-tools \
  -f sonarqube-values.yaml \
  --dry-run --debug
```

### 4.4 Instalar SonarQube

```bash
helm upgrade --install sonarqube sonarqube/sonarqube \
  --namespace devops-tools \
  -f sonarqube-values.yaml
```

### 4.5 Verificar la instalación

```bash
kubectl get pods -n devops-tools -l app.kubernetes.io/name=sonarqube -w
```

Esperar a que el pod sonarqube-sonarqube-0 pase a estado Running (puede tardar varios minutos).

## 5. Detalles de Implementación

El archivo sonarqube-values.yaml personaliza la instalación para el entorno de desarrollo. A continuación se explican los bloques principales.

| Parámetro | Valor | Explicación |
|-----------|-------|-------------|
| podLabels | part-of: microservices-platform | Etiqueta los pods para identificarlos como parte de la arquitectura de microservicios. |
| community.enabled | true | Fuerza el uso de la edición Community (gratuita). |
| monitoringPasscode | sonarqube-passcode-2026 | Clave requerida por las sondas de salud (readinessProbe y livenessProbe). Sin este valor, la validación del chart falla. |
| resources | requests.cpu: 500m, requests.memory: 2Gi | Solicitudes mínimas de CPU y memoria, ajustadas al clúster de 28 GB. |
| resources.limits | cpu: 1000m, memory: 4Gi | Límites máximos para evitar que el pod consuma recursos excesivos. |
| persistence | enabled: true, size: 10Gi | Almacenamiento persistente para los índices de Elasticsearch (datos de análisis). Usa StorageClass standard. |
| jdbcOverwrite | jdbcUrl, jdbcUsername, jdbcPassword | Configuración de la base de datos PostgreSQL externa. La contraseña se escribe en texto plano (aceptable para desarrollo). |
| prometheusExporter | enabled: true | Activa el exportador de métricas JMX para Prometheus (puertos 8000 y 8001). |
| tests.enabled | false | Desactiva la ejecución de tests post-instalación para ahorrar recursos. |


## 6. Integración con el Ecosistema

### 6.1 Traefik (Ingress)

Para exponer SonarQube externamente, se utiliza un IngressRoute (archivo sonarqube-ingress-route.yaml):

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: sonarqube-ui-route
  namespace: devops-tools
  labels:
    part-of: microservices-platform
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`sonarqube.desarrollo`)
      kind: Rule
      services:
        - name: sonarqube-sonarqube
          port: 9000
```

Aplicar:

```bash
kubectl apply -f sonarqube-ingress-route.yaml
```

Luego añadir la entrada en /etc/hosts apuntando a la IP del clúster (ej. minikube ip). Acceder a http://sonarqube.desarrollo.

### 6.2 Prometheus

Con prometheusExporter.enabled: true, SonarQube expone métricas en los puertos 8000 (web) y 8001 (CE). Para que Prometheus las recolecte, otra opción sería crear un ServiceMonitor:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: sonarqube
  namespace: devops-tools
  labels:
    release: prometheus
    part-of: microservices-platform
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: sonarqube
  endpoints:
    - port: web
      interval: 30s
      path: /metrics
```

### 6.3 ELK (Logs)

SonarQube escribe logs en stdout; Filebeat los recoge y envía a Elasticsearch. Se pueden consultar en Kibana filtrando por kubernetes.namespace=devops-tools y kubernetes.container.name=sonarqube.

### 6.4 Jaeger

No se configura de forma nativa, pero los análisis pueden instrumentarse en los pipelines de Jenkins.

## 7. Acceso

### 7.1 Credenciales iniciales

Usuario: admin
Contraseña: admin

En el primer inicio de sesión, el sistema obliga a cambiar la contraseña.

Contraseña modificada: S0n4r.Qube2026

### 7.2 Port-forward (acceso local rápido)

```bash
kubectl port-forward -n devops-tools svc/sonarqube-sonarqube 9000:9000
```

Acceder a http://localhost:9000 con las credenciales anteriores.

### 7.3 Ingress con Traefik

Una vez aplicado el IngressRoute, acceder a http://sonarqube.desarrollo.

## 8. Guía de Operaciones Kubernetes

### 8.1 Verificar estado

```bash
kubectl get statefulset,svc,pod -n devops-tools -l app.kubernetes.io/name=sonarqube
kubectl get pvc -n devops-tools -l app.kubernetes.io/name=sonarqube
```

### 8.2 Logs

```bash
kubectl logs -n devops-tools sonarqube-sonarqube-0 -c sonarqube -f
```

### 8.3 Reiniciar SonarQube

```bash
kubectl delete pod -n devops-tools sonarqube-sonarqube-0
```

El StatefulSet lo recreará automáticamente.

### 8.4 Escalado

SonarQube Community Edition no soporta alta disponibilidad. Mantener replicaCount: 1 (valor por defecto). Para entornos productivos se requiere la edición Enterprise o Data Center Edition.


## 9. Desinstalación

### 9.1 Eliminar el release de Helm

```bash
helm uninstall sonarqube -n devops-tools
```

### 9.2 Eliminar PVCs residuales (opcional)

```bash
kubectl delete pvc -n devops-tools -l app.kubernetes.io/name=sonarqube
```

### 9.3 Eliminar recursos adicionales

```bash
kubectl delete servicemonitor sonarqube -n devops-tools --ignore-not-found
kubectl delete ingressroute sonarqube-ui-route -n devops-tools --ignore-not-found
```

