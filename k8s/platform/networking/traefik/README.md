[![Traefik](https://img.shields.io/badge/Traefik-3.6-blue)](https://doc.traefik.io/traefik/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-39.0.7-orange)](https://github.com/traefik/traefik-helm-chart)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# Traefik – Ingress Controller para microservicios

Traefik es el Ingress Controller que gestiona todo el tráfico HTTP/HTTPS hacia los servicios de la arquitectura de microservicios, proporcionando enrutamiento inteligente, observabilidad integrada y exposición centralizada de dashboards.

## 📖 Tabla de Contenidos

- [1. Introducción](#1-introducción)
- [2. Documentación Oficial](#2-documentación-oficial)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación](#4-instalación)
- [5. Detalles de Implementación](#5-detalles-de-implementación)
  - [5.1 Etiquetado Global](#51-etiquetado-global)
  - [5.2 Dashboard y API](#52-dashboard-y-api)
  - [5.3 Logs y Métricas](#53-logs-y-métricas)
  - [5.4 Trazabilidad con Jaeger](#54-trazabilidad-con-jaeger)
  - [5.5 Gateway API y Servicio](#55-gateway-api-y-servicio)
- [6. Integración con el Ecosistema](#6-integración-con-el-ecosistema)
  - [6.1 Exposición de Servicios mediante IngressRoute](#61-exposición-de-servicios-mediante-ingressroute)
  - [6.2 Métricas en Prometheus](#62-métricas-en-prometheus)
  - [6.3 Trazas en Jaeger](#63-trazas-en-jaeger)
- [7. Acceso](#7-acceso)
  - [7.1 Dashboard de Traefik](#71-dashboard-de-traefik)
  - [7.2 URLs de Servicios Expuestos](#72-urls-de-servicios-expuestos)
- [8. Guía de Operaciones Kubernetes](#8-guía-de-operaciones-kubernetes)
- [9. Desinstalación](#9-desinstalación)

---

## 1. Introducción

**Traefik** es un Ingress Controller moderno que actúa como punto de entrada único para todo el tráfico hacia los servicios desplegados en el clúster. En este proyecto, Traefik se despliega en el namespace `ingress` y se encarga de:

- Enrutar peticiones HTTP/HTTPS a los servicios internos (Grafana, Prometheus, Jaeger, Vault, Kibana, etc.).
- Proveer un dashboard de administración para visualizar rutas, servicios y middlewares.
- Exponer métricas en formato Prometheus para su recolección.
- Enviar trazas de las peticiones a Jaeger para análisis de latencia y errores.

La configuración personalizada en `traefik-values.yaml` está optimizada para entornos de desarrollo (WSL2/Ubuntu), con énfasis en la observabilidad y la integración con el resto del stack.

---

## 2. Documentación Oficial

- [Documentación de Traefik](https://doc.traefik.io/traefik/)
- [Instalación con Helm](https://doc.traefik.io/traefik/getting-started/kubernetes/)
- [Helm Chart de Traefik en GitHub](https://github.com/traefik/traefik-helm-chart)
- [Configuración de Gateway API](https://doc.traefik.io/traefik/providers/kubernetes-gateway/)

---

## 3. Requisitos Previos

| Recurso | Versión / Detalle |
|---------|-------------------|
| Kubernetes | 1.19+ (probado en WSL2/Ubuntu) |
| kubectl | Configurado con acceso al clúster |
| Helm | 3.2+ |
| Namespace | `ingress` (se crea automáticamente con `--create-namespace`) |
| Archivo de valores | `traefik-values.yaml` presente en el directorio de trabajo |
| Jaeger | Debe estar desplegado en el namespace `monitoring` para recibir trazas (ver bloque `tracing.otlp`) |
| Prometheus | Debe estar desplegado para recolectar métricas (opcional, pero recomendado) |

> **Nota:** En entornos de desarrollo, se habilita el dashboard con autenticación insegura (`api.insecure: true`). Para producción, se debe configurar autenticación y TLS.

---

## 4. Instalación

### 4.1 Agregar repositorio de Helm

```bash
helm repo add traefik https://traefik.github.io/charts
helm repo update
```

### 4.2 Validar la configuración (dry-run)

```bash
helm install traefik traefik/traefik \
  --namespace ingress \
  --create-namespace \
  -f traefik-sonarqube-values.yaml \
  --dry-run --debug
```

### 4.3 Instalar o actualizar Traefik

```bash
helm upgrade --install traefik traefik/traefik \
  --namespace ingress \
  --create-namespace \
  -f traefik-sonarqube-values.yaml
```

### 4.4 Verificar la instalación

```bash
# Verificar que los pods estén en estado Running/Ready
kubectl get pods -n ingress -l app.kubernetes.io/name=traefik
kubectl get svc -n ingress traefik

# Esperar a que Traefik esté listo
kubectl wait --for=condition=Ready deployment/traefik -n ingress --timeout=300s
```

## 5. Detalles de Implementación

### 5.1 Etiquetado Global

Se aplica la etiqueta `part-of: microservices-platform` a todos los recursos creados por el chart, facilitando la identificación y gestión en el clúster.

```yaml
commonLabels:
  part-of: "microservices-architecture"
```

### 5.2 Dashboard y API

Se habilita el dashboard de administración de Traefik con acceso inseguro (solo para desarrollo). El dashboard se expone mediante un IngressRoute en el host `dashboard.desarrollo`.

```yaml
api:
  dashboard: true
  insecure: true

ingressRoute:
  dashboard:
    enabled: true
    matchRule: "Host(`dashboard.desarrollo`)"   # Dominio local
    entryPoints: ["web"]
```

### 5.3 Logs y Métricas

**Logs de acceso**: Se activan en formato JSON con nivel DEBUG para facilitar el troubleshooting.

**Métricas Prometheus**: Se exponen en el puerto de métricas (por defecto 9100) con las etiquetas de los entrypoints.

```yaml
logs:
  access:
    enabled: true
    level: DEBUG
    format: json

metrics:
  prometheus:
    addEntryPointsLabels: true
```

### 5.4 Trazabilidad con Jaeger

Traefik envía trazas OTLP a Jaeger (desplegado en el namespace monitoring). Esto permite visualizar el recorrido de las peticiones desde el ingreso hasta los servicios finales.

```yaml
tracing:
  otlp:
    enabled: true
    http:
      enabled: true
      endpoint: "http://jaeger.monitoring.svc.cluster.local:4318/v1/traces"
```

### 5.5 Gateway API y Servicio

Se habilita el proveedor de Kubernetes Gateway API para mayor flexibilidad y se configura un listener web que acepta rutas de todos los namespaces.

```yaml
providers:
  kubernetesGateway:
    enabled: true

gateway:
  listeners:
    web:
      namespacePolicy:
        from: All

service:
  type: LoadBalancer          # Para obtener IP externa (minikube tunnel)
```

## 6. Integración con el Ecosistema

### 6.1 Exposición de Servicios mediante IngressRoute

En el directorio `ingress/` se definen los archivos `*-ingress-route.yaml` que exponen cada componente de observabilidad a través de Traefik. Estos archivos siguen el patrón:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: <componente>-route
  namespace: <namespace>
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`<componente>.desarrollo`)
      kind: Rule
      services:
        - name: <servicio-interno>
          port: <puerto>
```

Los IngressRoutes existentes incluyen:

| Componente   | Namespace | Archivo | Host sugerido |
|--------------|-----------|---------|---------------|
| Grafana      | monitoring | grafana-ingress-route.yaml | grafana.desarrollo |
| Prometheus   | monitoring | prometheus-ingress-route.yaml | prometheus.desarrollo |
| Alertmanager | monitoring | alert-manager-ingress-route.yaml | alertmanager.desarrollo |
| Jaeger       | monitoring | jaeger-ingress-route.yaml | jaeger.desarrollo |
| Kibana (ELK) | monitoring | kibana-ingress-route.yaml | kibana.desarrollo |
| Vault        | security | vault-ingress-route.yaml | vault.desarrollo |

### 6.2 Métricas en Prometheus

Traefik expone métricas en el puerto 9100 (por defecto). Se puede crear un ServiceMonitor para que Prometheus las recolecte automáticamente:

### 6.3 Trazas en Jaeger

Gracias a la configuración de `tracing.otlp`, cada petición que pasa por Traefik genera trazas que son enviadas a Jaeger. En la UI de Jaeger se pueden visualizar las trazas completas, incluyendo el tiempo de procesamiento en Traefik y en los servicios finales.

## 7. Acceso

### 7.1 Dashboard de Traefik

Para acceder al dashboard, se puede usar port-forward o la IP externa del servicio LoadBalancer.

**Opción A – Port-forward:**

```bash
kubectl port-forward -n ingress svc/traefik 8080:8080
```

Abrir `http://localhost:8080/dashboard/`

**Opción B – Usando minikube tunnel (para obtener IP externa):**

```bash
minikube tunnel
# En otra terminal:
kubectl get svc -n ingress traefik
# Anotar la EXTERNAL-IP
```

Luego acceder a `http://<EXTERNAL-IP>/dashboard/` (o al host configurado `dashboard.desarrollo` si se tiene DNS local).

### 7.2 URLs de Servicios Expuestos

Una vez creados los IngressRoutes, los servicios son accesibles mediante los hosts definidos:

| Servicio | URL |
|----------|-----|
| Grafana | http://grafana.desarrollo |
| Prometheus | http://prometheus.desarrollo |
| Alertmanager | http://alertmanager.desarrollo |
| Jaeger UI | http://jaeger.desarrollo |
| Kibana | http://kibana.desarrollo |
| Vault UI | http://vault.desarrollo |

Para que estos hosts funcionen localmente, se deben agregar al archivo `/etc/hosts` apuntando a la IP del clúster (por ejemplo, la IP de Minikube).

## 8. Guía de Operaciones Kubernetes

### 8.1 Verificar estado de los recursos

```bash
# Pods de Traefik
kubectl get pods -n ingress -l app.kubernetes.io/name=traefik

# Servicios e IngressRoutes
kubectl get svc -n ingress
kubectl get ingressroute -n ingress --all-namespaces
```

### 8.2 Logs del controlador

```bash
kubectl logs -n ingress -l app.kubernetes.io/name=traefik --tail=50 -f
```

### 8.3 Verificar configuración viva de Traefik

```bash
# Port-forward al puerto de la API (8080)
kubectl port-forward -n ingress svc/traefik 8080:8080 &
curl http://localhost:8080/api/http/services
```

### 8.4 Reiniciar Traefik

```bash
kubectl rollout restart deployment -n ingress traefik
```

### 8.5 Solución de problemas comunes

**Dashboard no accesible:** Verificar que el IngressRoute dashboard esté presente: `kubectl get ingressroute -n ingress`. Si se usa minikube, asegurar que `minikube tunnel` está ejecutándose.

**Métricas no aparecen en Prometheus:** Confirmar que el ServiceMonitor está creado con la etiqueta `release: prometheus` y que el selector coincide con los labels de Traefik.

**Trazas no llegan a Jaeger:** Revisar la conectividad hacia el endpoint de Jaeger (`kubectl run -it --rm test --image=curlimages/curl -- curl http://jaeger.monitoring.svc.cluster.local:4318/v1/traces`). Asegurar que el servicio jaeger está desplegado en el namespace monitoring.

## 9. Desinstalación

### 9.1 Eliminar el release de Helm

```bash
helm uninstall -n ingress traefik
```

### 9.2 Eliminar recursos adicionales (ServiceMonitor, IngressRoutes)

```bash
kubectl delete servicemonitor -n ingress traefik
kubectl delete ingressroute -n ingress --all
```

### 9.3 Limpiar PVCs (si se hubieran creado)

```bash
kubectl delete pvc -n ingress --all
```

---
