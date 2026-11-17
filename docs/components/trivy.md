[![Trivy Operator](https://img.shields.io/badge/Trivy%20Operator-0.32.1-blue)](https://aquasecurity.github.io/trivy-operator/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-v3.18.3-orange)](https://github.com/aquasecurity/trivy-operator)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# Trivy Operator

## 📖 Tabla de Contenidos

- [1. Introducción](#1-introducción)
- [2. Documentación](#2-documentación)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación](#4-instalación)
- [5. Detalles de Implementación](#5-detalles-de-implementación)
- [6. Acceso a los Reportes de Seguridad](#6-acceso-a-los-reportes-de-seguridad)
- [7. Guía de Operaciones Kubernetes](#7-guía-de-operaciones-kubernetes)
- [8. Desinstalación completa](#8-desinstalación-completa)

---

## 1. Introducción

Trivy Operator es un operador de Kubernetes que automatiza el escaneo de seguridad de imágenes de contenedores y configuraciones del clúster. En este proyecto se utiliza para:

- Detectar vulnerabilidades en imágenes desplegadas.
- Auditar configuraciones de Kubernetes (RBAC, recursos, etc.) en busca de riesgos.
- Generar reportes de cumplimiento (CIS Benchmarks, NSA).
- Facilitar la integración con pipelines DevSecOps y dashboards de seguridad.

La instalación se realiza con Helm en el namespace `security`, siguiendo prácticas de etiquetado (`part-of: microservices-platform`) y con una configuración optimizada para recursos limitados (clúster de 28GB).

## 2. Documentación

- [Documentación oficial de Trivy Operator](https://aquasecurity.github.io/trivy-operator/)
- [Repositorio GitHub de Trivy Operator](https://github.com/aquasecurity/trivy-operator)
- [Benchmarks de seguridad CIS](https://www.cisecurity.org/benchmark/kubernetes/)

## 3. Requisitos Previos

- Kubernetes 1.16+ (probado en WSL2/Ubuntu).
- `kubectl` configurado con acceso al clúster.
- `Helm` 3.0+ instalado.
- Namespace `security` creado (los comandos de instalación lo crean con `--create-namespace`).
- Archivo `trivy-values.yaml` disponible en el directorio actual.

## 4. Instalación

### 4.1 Agregar repositorio de Trivy

```bash
helm repo add aqua https://aquasecurity.github.io/helm-charts/
helm repo update
```

### 4.2 Validar la configuración (dry-run)

```bash
helm install trivy-operator aqua/trivy-operator \
  --namespace security \
  --create-namespace \
  -f trivy-sonarqube-values.yaml \
  --dry-run --debug
```

### 4.3 Instalar Trivy Operator

```bash
helm install trivy-operator aqua/trivy-operator \
  --namespace security \
  --create-namespace \
  -f trivy-sonarqube-values.yaml
```

### 4.4 Verificar la instalación

```bash
# Verificar que los pods estén en estado Running/Ready
kubectl get pods -n security -l app.kubernetes.io/name=trivy-operator

# Esperar a que Trivy Operator esté listo
kubectl wait --for=condition=Ready deployment/trivy-operator -n security --timeout=300s
```

## 5. Detalles de Implementación

### 5.1 Etiquetado global

Todos los recursos creados por el chart llevan la etiqueta `part-of: microservices-platform`.

```yaml
commonLabels:
  part-of: "microservices-architecture"
```

### 5.2 Configuración del operador

El bloque `operator` define qué namespaces se escanean y qué tipos de auditorías se realizan:

```yaml
operator:
  excludeNamespaces: "kube-system,kube-public"
  configAuditScannerEnabled: true
  rbacScannerEnabled: true
  complianceEnabled: true
```

- **Exclusión de namespaces**: Se excluyen `kube-system` y `kube-public` para evitar ruido en recursos del sistema.
- **Auditoría de configuración**: Escanea recursos como Deployments, Services, ConfigMaps, etc., en busca de malas prácticas.
- **Escáner RBAC**: Analiza roles y bindings para detectar permisos excesivos.
- **Cumplimiento**: Genera reportes de CIS Kubernetes Benchmark y NSA.

### 5.3 Configuración de Trivy (motor de escaneo)

```yaml
trivy:
  resources:
    limits:
      cpu: "1"
      memory: "2Gi"
  ignoreUnfixed: true
  severity: "HIGH,CRITICAL"
```

- **Recursos**: Se asignan límites de 1 CPU y 2Gi de memoria para que el escáner no consuma todos los recursos del clúster (ajustado para un clúster de ~28GB).
- **ignoreUnfixed**: Ignora vulnerabilidades que no tienen parche disponible, reduciendo el ruido en reportes.
- **severity**: Solo se reportan vulnerabilidades de severidad HIGH o CRITICAL, enfocando la atención en los riesgos más críticos.

### 5.4 Integración con el ecosistema de observabilidad

Aunque el values no incluye un ServiceMonitor, los reportes generados por Trivy Operator pueden ser consumidos por herramientas como Grafana mediante las CRDs `VulnerabilityReport` y `ConfigAuditReport`. Se puede extender la configuración con `extraObjects` si se desea exponer métricas personalizadas.

## 6. Acceso a los Reportes de Seguridad

Trivy Operator no proporciona una interfaz web nativa, pero los reportes se almacenan como objetos personalizados (CRDs) dentro del clúster. Se pueden consultar con `kubectl`:

### 6.1 Listar reportes de vulnerabilidades

```bash
kubectl get vulnerabilityreports --all-namespaces -o wide
```

### 6.2 Listar reportes de auditoría de configuración

```bash
kubectl get configauditreports --all-namespaces -o wide
```

### 6.3 Ver un reporte detallado

```bash
kubectl describe vulnerabilityreport <nombre> -n <namespace>
```

### 6.4 Opcional: Exponer métricas a Prometheus

Para integrar con el stack de monitoreo, se puede crear manualmente un ServiceMonitor que apunte al puerto de métricas del operador (por defecto `8080`). Ejemplo:

```yaml
apiVersion: security.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: trivy-operator
  namespace: security
  labels:
    release: prometheus
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: trivy-operator
  endpoints:
  - port: metrics
    interval: 30s
```

## 7. Guía de Operaciones Kubernetes

### 7.1 Verificar estado del operador

```bash
kubectl get pods -n security -l app.kubernetes.io/name=trivy-operator
kubectl logs -n security -l app.kubernetes.io/name=trivy-operator
```

### 7.2 Revisar reportes generados

```bash
# Vulnerabilidades por namespace
kubectl get vulnerabilityreports -A | grep -v "No resources"

# Auditorías de configuración por namespace
kubectl get configauditreports -A | grep -v "No resources"

# Inspect created VulnerabilityReports by:

    kubectl get vulnerabilityreports --all-namespaces -o wide

# Inspect created ConfigAuditReports by:

    kubectl get configauditreports --all-namespaces -o wide

# Inspect the work log of trivy-operator by:

    kubectl logs -n security deployment/trivy-operator

```

### 7.3 Eliminar reportes antiguos (si es necesario)

```bash
kubectl delete vulnerabilityreports --all-namespaces --all
kubectl delete configauditreports --all-namespaces --all
```

### 7.4 Reiniciar el operador

Si se requiere reiniciar el componente:

```bash
kubectl rollout restart deployment -n security trivy-operator
```

### 7.5 Solución de problemas comunes

**No se generan reportes**: Verificar que el operador tenga permisos para leer los recursos (`kubectl logs -n security deployment/trivy-operator`). Comprobar que los namespaces excluidos no contengan imágenes que se quieran escanear.

**Alto consumo de recursos**: Ajustar los límites de CPU/memoria en `trivy.resources`. Si el clúster tiene menos de 28GB, reducir los límites.

**Vulnerabilidades no reportadas**: Confirmar que `ignoreUnfixed` esté en `false` si se desean ver todas. También revisar que el nivel de severidad incluya las que se esperan.

**Error de autenticación en registries privados**: Trivy Operator necesita credenciales para acceder a imágenes privadas. Se pueden configurar secrets `imagePullSecrets` en el namespace del operador.

## 8. Desinstalación completa

```bash
helm uninstall -n security trivy-operator
# Eliminar reportes residuales (opcional)
kubectl delete vulnerabilityreports --all-namespaces --all
kubectl delete configauditreports --all-namespaces --all
```

---

**Proyecto: microservices-architecture | Entorno: Kubernetes (WSL2/Ubuntu) con Trivy Operator**
