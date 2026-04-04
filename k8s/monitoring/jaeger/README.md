[![Jaeger](https://img.shields.io/badge/Jaeger-2.16.0-blue)](https://www.jaegertracing.io/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-4.6.0-orange)](https://github.com/jaegertracing/helm-charts)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# Jaeger v2 - Plataforma de Trazabilidad Distribuida

## 📖 Tabla de Contenidos

- [1. Introducción](#1-introducción)
- [2. Documentación](#2-documentación)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación](#4-instalación)
- [5. Detalles de Implementación](#5-detalles-de-implementación)
- [6. Acceso a la Interfaz de Usuario](#6-acceso-a-la-interfaz-de-usuario)
- [7. Guía de Operaciones Kubernetes](#7-guía-de-operaciones-kubernetes)
- [8. Desinstalación completa](#8-desinstalación-completa)

---

## 1. Introducción

Jaeger es un sistema de rastreo distribuido de código abierto que permite monitorear y solucionar problemas en arquitecturas de microservicios. En este proyecto se implementa **Jaeger v2**, que utiliza el colector de OpenTelemetry como base, ofreciendo una mayor flexibilidad y compatibilidad con el estándar OTLP. La solución se integra con el clúster de Elasticsearch para almacenamiento persistente y con Prometheus para la recolección de métricas internas.

## 2. Documentación

- [Sitio Oficial de Jaeger](https://www.jaegertracing.io/)
- [Documentación de Jaeger v2 en Kubernetes](https://www.jaegertracing.io/docs/2.16/deployment/kubernetes/)
- [Helm Charts de Jaeger](https://github.com/jaegertracing/helm-charts)
- [OpenTelemetry](https://opentelemetry.io/)

## 3. Requisitos Previos

- Kubernetes 1.16+ (probado en WSL2/Ubuntu).
- Helm 3.0+ instalado.
- Namespace `monitoring` debe existir.
- Clúster de Elasticsearch accesible desde el namespace `monitoring` (servicio `elasticsearch-master`).
- Credenciales de Elasticsearch (usuario `elastic` y contraseña definida en el secret correspondiente).
- Prometheus Operator (kube-prometheus-stack) instalado con la etiqueta `release: prometheus` para el ServiceMonitor.
- Archivo `jaeger-values.yaml` en el directorio de trabajo.

## 4. Instalación

### 4.1 Agregar el repositorio de Helm

```bash
helm repo add jaegertracing https://jaegertracing.github.io/helm-charts
helm repo update
```

### 4.2 Validar la configuración (dry-run)

```bash
helm upgrade --install jaeger jaegertracing/jaeger \
  --namespace monitoring -f jaeger-sonarqube-values.yaml \
  --dry-run --debug
```

### 4.3 Instalar o actualizar Jaeger

```bash
helm upgrade --install jaeger jaegertracing/jaeger \
  --namespace monitoring \
  --version 4.6.0 \
  -f jaeger-gitlab-sonarqube-values.yaml
```

### 4.4 Verificar la instalación

```bash
# Verificar que los pods estén en estado Running/Ready
kubectl get pods -n monitoring -l app.kubernetes.io/instance=jaeger
kubectl get svc -n monitoring -l app.kubernetes.io/instance=jaeger

# Esperar a que Jaeger esté listo
kubectl wait --for=condition=Ready jaeger -n monitoring --timeout=300s
```

## 5. Detalles de Implementación

### 5.1 Configuración de almacenamiento en Elasticsearch

Jaeger utiliza Elasticsearch como backend persistente para trazas. En `jaeger-values.yaml` se definen dos backends (primario y archivo) apuntando al mismo clúster:

```yaml
storage:
  elasticsearch:
    tls:
      enabled: true
      skip_verify: true
    url: "https://elasticsearch-master.monitoring.svc.cluster.local:9200"
    user: elastic
    password: 1S8eRh5f33afirVl
```

La configuración de TLS con `skip_verify: true` permite conexiones internas seguras sin necesidad de certificados validados externamente, adecuado para entornos de desarrollo. La contraseña está en texto plano en el values; en entornos productivos se recomienda externalizarla mediante un Secret.

### 5.2 Configuración del colector Jaeger v2 (userconfig)

El bloque `userconfig` define la configuración del colector basado en OpenTelemetry, incluyendo:

```yaml
userconfig:
  service:
    telemetry:
      metrics:
        level: detailed
        readers:
          - pull:
              exporter:
                prometheus:
                  host: 0.0.0.0
                  port: 8888
      receivers:
        otlp:
      protocols:
        grpc:
          endpoint: 0.0.0.0:4317
        http:
          endpoint: 0.0.0.0:4318
      extensions:
    jaeger_storage:
      backends:
        primary_store_elasticsearch:
          elasticsearch:
            server_urls: ["https://elasticsearch-master.monitoring.svc.cluster.local:9200"]
            auth:
              basic:
                username: elastic
                password: 1S8eRh5f33afirVl
            tls:
              insecure_skip_verify: true
```

Esta configuración es la base para que los microservicios envíen trazas mediante el protocolo OTLP estándar. Las trazas se reciben por OTLP y se exportan al almacenamiento Elasticsearch.

### 5.3 Integración con Prometheus (ServiceMonitor)

Para que Prometheus pueda recolectar las métricas internas de Jaeger, se inyecta un `ServiceMonitor` utilizando `extraObjects`:

```yaml
extraObjects:
  - apiVersion: monitoring.coreos.com/v1
    kind: ServiceMonitor
    metadata:
      name: jaeger-metrics-monitor
      labels:
        release: prometheus                # Etiqueta requerida por el Prometheus Operator
        part-of: microservices-platform
    spec:
      namespaceSelector:
        matchNames:
          - monitoring
      selector:
        matchLabels:
          app.kubernetes.io/instance: jaeger
          app.kubernetes.io/name: jaeger
      endpoints:
        - port: internal-metrics           # Puerto 8888 expuesto por el servicio
          interval: 15s
          path: /metrics
```

La etiqueta `release: prometheus` es crítica para que el Prometheus Operator descubra este ServiceMonitor, ya que el stack de monitoreo está configurado para seleccionar recursos con esa etiqueta.

### 5.4 Etiquetado común

Todos los recursos creados por el chart llevan la etiqueta `part-of: "microservices-architecture"` para facilitar la identificación y filtrado en todo el clúster.

```yaml
commonLabels:
  part-of: "microservices-architecture"
```

### 5.5 Recursos asignados

Se definen límites y solicitudes de CPU y memoria para el pod principal:

```yaml
resources:
  limits:
    cpu: 500m
    memory: 1Gi
  requests:
    cpu: 200m
    memory: 512Mi
```

Estos valores garantizan un rendimiento estable sin consumir excesivos recursos del clúster.

## 6. Acceso a la Interfaz de Usuario

### 6.1 Port-forward local

Para acceder rápidamente a la UI de Jaeger:

```bash
kubectl port-forward -n monitoring svc/jaeger 16686:16686
```

Luego abre `http://localhost:16686` en tu navegador.

### 6.2 Exposición mediante Ingress (Traefik)

Si se utiliza Traefik como ingress controller, se puede crear un recurso `IngressRoute` o `Ingress` para exponer Jaeger externamente. Ejemplo básico de IngressRoute:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: jaeger-ui-route
  namespace: monitoring
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`jaeger.desarrollo`)
      kind: Rule
      services:
        - name: jaeger
          port: 16686
```

## 7. Guía de Operaciones Kubernetes

### 7.1 Verificar estado de los recursos

```bash
# Pods de Jaeger
kubectl get pods -n monitoring -l app.kubernetes.io/instance=jaeger

# Servicios
kubectl get svc -n monitoring -l app.kubernetes.io/instance=jaeger

# ServiceMonitor (debe estar presente)
kubectl get servicemonitor -n monitoring jaeger-metrics-monitor
```

### 7.2 Logs del componente

```bash
# Logs del pod principal
kubectl logs -n monitoring -l app.kubernetes.io/name=jaeger -c jaeger

# Logs en tiempo real
kubectl logs -n monitoring -l app.kubernetes.io/name=jaeger -c jaeger -f
```

### 7.3 Descripción detallada

```bash
kubectl describe pod -n monitoring -l app.kubernetes.io/name=jaeger
```

### 7.4 Reinicio del despliegue

Si se requiere reiniciar el componente:

```bash
kubectl rollout restart deployment -n monitoring jaeger
```

### 7.5 Troubleshooting común

**Target de Prometheus no aparece**: Verificar que el ServiceMonitor tenga la etiqueta `release: prometheus` y que el Prometheus Operator esté configurado para seleccionar por esa etiqueta. Comprobar que el puerto `internal-metrics` esté expuesto en el servicio de Jaeger (`kubectl get svc jaeger -n monitoring -o yaml`).

**Error de conexión a Elasticsearch**: Revisar los logs de Jaeger. Asegurar que la URL y credenciales son correctas y que el servicio `elasticsearch-master` está accesible desde el pod de Jaeger (resolución DNS interna). Si se utiliza TLS con `skip_verify`, confirmar que el valor esté establecido correctamente.

**Pod en CrashLoopBackOff**: Ejecutar `kubectl describe pod` para ver eventos. Puede deberse a falta de memoria (ajustar `resources`) o errores de configuración en la conexión a Elasticsearch.

## 8. Desinstalación completa

```bash
helm uninstall -n monitoring jaeger
# Opcional: eliminar el ServiceMonitor manualmente (si no fue borrado)
kubectl delete servicemonitor -n monitoring jaeger-metrics-monitor
```

---

**Proyecto: microservices-architecture | Entorno: Kubernetes (WSL2/Ubuntu) con Jaeger v2**
