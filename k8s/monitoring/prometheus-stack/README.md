[![Kube Prometheus Stack](https://img.shields.io/badge/Kube%20Prometheus%20Stack-75.4.0-blue)](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-v1.35.1-326CE5)](https://kubernetes.io/)

# kube-prometheus-stack – Monitoreo para microservicios

Este documento describe la implementación personalizada del stack de monitoreo para el clúster de microservicios, basada en el archivo `prometheus-stack-values.yaml`. Incluye Prometheus, Grafana y Alertmanager con configuraciones de persistencia, etiquetado común y ajustes de seguridad para entornos WSL2/Ubuntu.

## 📖 Tabla de Contenidos

- [1. Introducción](#1-introducción)
- [2. Documentación](#2-documentación)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación](#4-instalación)
- [5. Detalles de Implementación](#5-detalles-de-implementación)
- [6. Acceso a las Interfaces](#6-acceso-a-las-interfaces)
- [7. Guía de Operaciones Kubernetes](#7-guía-de-operaciones-kubernetes)

---

## 1. Introducción

El stack desplegado con Helm (`kube-prometheus-stack`) proporciona:
- **Prometheus** para la recolección y almacenamiento de métricas del clúster y aplicaciones.
- **Grafana** para la visualización de dashboards preconfigurados.
- **Alertmanager** para la gestión de alertas.
- **kube-state-metrics** y **node-exporter** para métricas de Kubernetes y nodos.

La personalización en `prometheus-stack-values.yaml` se enfoca en:
- **Etiquetado consistente** con el proyecto de microservicios (`part-of: microservices-platform`).
- **Persistencia** para evitar pérdida de datos tras reinicios.
- **Corrección de permisos** en Grafana para evitar errores comunes en volúmenes persistentes bajo WSL2/Ubuntu.

## 2. Documentación

- [Helm Chart: kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack)
- [Documentación de Prometheus](https://prometheus.io/docs/introduction/overview/)
- [Documentación de Grafana](https://grafana.com/docs/grafana/latest/)

## 3. Requisitos Previos

- **Kubernetes** 1.16+ (entorno WSL2/Ubuntu probado).
- **kubectl** configurado con acceso al clúster.
- **Helm** 3.0+ instalado.
- **Recursos suficientes**: mínimo 4GB de RAM recomendados para el stack completo.
- **Namespace** `monitoring` creado (el comando de instalación lo crea con `--create-namespace`).
- El archivo `prometheus-stack-values.yaml` disponible en el directorio actual.

## 4. Instalación

Agregar el repositorio de Prometheus Community y actualizar:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```

Verificar versiones disponibles (opcional):

```bash
helm search repo prometheus-community/kube-prometheus-stack --versions
```

Instalar el chart con la configuración personalizada:

```bash
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  -f prometheus-stack-sonarqube-values.yaml
```

Para validar la configuración sin aplicar cambios reales:

```bash
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  -f prometheus-stack-sonarqube-values.yaml \
  --dry-run --debug
```

Verificar que los pods se hayan creado correctamente:

```bash
kubectl --namespace monitoring get pods -l "release=prometheus"
```

## 5. Detalles de Implementación

### 5.1 Etiquetado y Coherencia

Se define `commonLabels` y `externalLabels` para identificar todas las métricas con el proyecto `microservices-architecture`. Esto facilita la búsqueda y filtrado en PromQL.

```yaml
commonLabels:
  part-of: "microservices-architecture"

prometheus:
  prometheusSpec:
    externalLabels:
      part-of: "microservices-architecture"
```

### 5.2 Persistencia de Prometheus

Se configura un volumen persistente de 10 Gi para retener las métricas durante 10 días. El PVC utiliza `storageClassName: "standard"` (ajustar según el storage class del clúster).

```yaml
prometheus:
  prometheusSpec:
    retention: 10d
    storageSpec:
      volumeClaimTemplate:
        spec:
          accessModes: ["ReadWriteOnce"]
          resources:
            requests:
              storage: 10Gi
```

### 5.3 Persistencia y Permisos de Grafana

Grafana utiliza un PVC de 5 Gi. Se ha aplicado una corrección de permisos para evitar el error `chown: changing ownership of '/var/lib/grafana'` común en entornos con volúmenes persistentes y usuarios no root.

**Solución aplicada:**
- Se desactiva `initChownData` que puede fallar al intentar cambiar propietarios.
- Se establece `fsGroup: 472` a nivel de Pod (permite que el volumen sea accesible por el grupo).
- Se ejecuta el contenedor principal con `runAsUser: 472` y `runAsGroup: 472`, el UID/GID por defecto de Grafana.

```yaml
grafana:
  persistence:
    enabled: true
    type: pvc
    storageClassName: "standard"
    accessModes:
      - ReadWriteOnce
    size: 5Gi
  initChownData:
    enabled: false
  podSecurityContext:
    fsGroup: 472
  containerSecurityContext:
    runAsUser: 472
    runAsGroup: 472
```

Esta configuración garantiza que Grafana pueda escribir en el volumen persistente sin problemas de permisos.

### 5.4 Contraseña de Administrador de Grafana

Se define una contraseña inicial en el values (en producción se recomienda externalizarla mediante Secret). El secreto generado por el chart contiene la contraseña codificada en base64.

```yaml
grafana:
  adminPassword: "admin"
```

## 6. Acceso a las Interfaces

### 6.1 Obtener la contraseña de Grafana

```bash
kubectl get secret --namespace monitoring prometheus-grafana -o jsonpath="{.data.admin-password}" | base64 -d ; echo
```

### 6.2 Port-forward a Grafana (puerto 3000)

```bash
kubectl port-forward --namespace monitoring svc/prometheus-grafana 3000:80
```

Acceder a `http://localhost:3000` con usuario `admin` y la contraseña obtenida.

### 6.3 Port-forward a Prometheus (puerto 9090)

```bash
kubectl port-forward --namespace monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090
```

Acceder a `http://localhost:9090` para consultas PromQL.

### 6.4 Exposición mediante Ingress (Traefik)

Si se dispone de Traefik como ingress controller, se puede exponer los servicios creando recursos `Ingress` o `IngressRoute`. Por ejemplo, para Grafana:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: grafana-ui-route
  namespace: monitoring
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`grafana.desarrollo`)
      kind: Rule
      services:
        - name: prometheus-grafana
          port: 80
```

Ajustar según la configuración de DNS local.

## 7. Guía de Operaciones Kubernetes

### 7.1 Verificar estado de los recursos

```bash
# Pods en el namespace monitoring
kubectl get pods -n monitoring

# PersistentVolumeClaims
kubectl get pvc -n monitoring

# Servicios
kubectl get svc -n monitoring
```

### 7.2 Logs de un pod específico

```bash
kubectl logs -n monitoring <nombre-del-pod>
```

Para logs continuos:

```bash
kubectl logs -f -n monitoring <nombre-del-pod>
```

### 7.3 Descripción detallada de un recurso

```bash
kubectl describe pod -n monitoring <nombre-del-pod>
kubectl describe pvc -n monitoring <nombre-del-pvc>
```

### 7.4 Eventos del namespace

```bash
kubectl get events -n monitoring --sort-by='.lastTimestamp'
```

### 7.5 Reiniciar un despliegue

```bash
kubectl rollout restart deployment -n monitoring <nombre-del-deployment>
```

### 7.6 Solución de problemas comunes

**Error de permisos en Grafana**: Verificar que los valores de `fsGroup` y `runAsUser/Group` estén aplicados correctamente en el pod. Ejecutar `kubectl describe pod -n monitoring prometheus-grafana-...` y confirmar la presencia de `fsGroup` en la sección `Security Context`.

**PVC en estado Pending**: Revisar que exista un storage class disponible (`kubectl get storageclass`) y que el clúster tenga nodos con capacidad.

**Prometheus sin datos**: Comprobar los targets en la UI de Prometheus (Status > Targets) o los logs del pod `prometheus-kube-prometheus-prometheus-...`.

### 7.7 Desinstalación completa

```bash
helm uninstall --namespace monitoring prometheus

# Eliminar CRDs (opcional, si no se necesitan en otros proyectos)
kubectl delete crd alertmanagerconfigs.monitoring.coreos.com
kubectl delete crd alertmanagers.monitoring.coreos.com
kubectl delete crd podmonitors.monitoring.coreos.com
kubectl delete crd probes.monitoring.coreos.com
kubectl delete crd prometheusagents.monitoring.coreos.com
kubectl delete crd prometheuses.monitoring.coreos.com
kubectl delete crd prometheusrules.monitoring.coreos.com
kubectl delete crd scrapeconfigs.monitoring.coreos.com
kubectl delete crd servicemonitors.monitoring.coreos.com
kubectl delete crd thanosrulers.monitoring.coreos.com
```

---

**Proyecto: microservices-architecture | Entorno: Kubernetes (WSL2/Ubuntu) con kube-prometheus-stack**
