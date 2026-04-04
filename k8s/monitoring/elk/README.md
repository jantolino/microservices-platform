[![Elasticsearch](https://img.shields.io/badge/Elastic%20Stack-8.5.1-blue)](https://www.elastic.co/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-8.5.1-orange)](https://github.com/elastic/helm-charts)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# ELK Stack para Monitoreo de Logs

Este directorio contiene las configuraciones para desplegar el stack ELK (Elasticsearch, Kibana, Filebeat) en Kubernetes utilizando los charts oficiales de Elastic. El stack se integra con la arquitectura de microservicios (`part-of: microservices-platform`) y está optimizado para entornos de desarrollo con recursos limitados (WSL2/Ubuntu).

## 📖 Tabla de Contenidos

- [1. Introducción](#1-introducción)
- [2. Documentación](#2-documentación)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación](#4-instalación)
- [5. Detalles de Implementación](#5-detalles-de-implementación)
- [6. Acceso a las Interfaces](#6-acceso-a-las-interfaces)
- [7. Guía de Operaciones Kubernetes](#7-guía-de-operaciones-kubernetes)
- [8. Desinstalación completa](#8-desinstalación-completa)

---

## 1. Introducción

El stack ELK proporciona:

- **Elasticsearch**: Almacenamiento y búsqueda centralizada de logs.
- **Kibana**: Visualización y análisis de logs a través de dashboards.
- **Filebeat**: Recolección de logs desde los nodos del clúster y contenedores.

Este despliegue forma parte de la solución de observabilidad junto con Prometheus (métricas) y Jaeger (trazas). Todos los componentes están etiquetados con `part-of: microservices-platform` para facilitar la identificación en el clúster.

## 2. Documentación

- [Elastic Helm Charts](https://github.com/elastic/helm-charts)
- [Elasticsearch Helm Chart](https://github.com/elastic/helm-charts/tree/main/elasticsearch)
- [Kibana Helm Chart](https://github.com/elastic/helm-charts/tree/main/kibana)
- [Filebeat Helm Chart](https://github.com/elastic/helm-charts/tree/main/filebeat)
- [Documentación oficial de Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Documentación de Filebeat](https://www.elastic.co/guide/en/beats/filebeat/current/index.html)

## 3. Requisitos Previos

- Kubernetes 1.16+ (probado en WSL2/Ubuntu).
- `kubectl` configurado con acceso al clúster.
- `Helm` 3.0+ instalado.
- Namespace `monitoring` creado (los comandos de instalación lo crean con `--create-namespace`).
- Archivos de valores en el directorio actual:
  - `elasticsearch-values.yaml` 
  - `kibana-values.yaml` 
  - `filebeat-values.yaml` 

## 4. Instalación

### 4.1 Agregar el repositorio de Elastic

```bash
helm repo add elastic https://helm.elastic.co
helm repo update
```

### 4.2 Validar configuración (dry-run)

```bash
# Elasticsearch
helm install elasticsearch elastic/elasticsearch \
  --version 7.17.3 \
  --namespace monitoring \
  --create-namespace \
  -f elasticsearch-sonarqube-values.yaml \
  --dry-run --debug

# Kibana
helm install kibana elastic/kibana \
  --version 7.17.3 \
  --namespace monitoring \
  --create-namespace \
  -f kibana-sonarqube-values.yaml \
  --dry-run --debug

# Filebeat
helm install filebeat elastic/filebeat \
  --version 7.17.3 \
  --namespace monitoring \
  --create-namespace \
  -f filebeat-sonarqube-values.yaml \
  --dry-run --debug
```

### 4.3 Instalar los componentes

```bash
# Elasticsearch
helm upgrade --install elasticsearch elastic/elasticsearch \
  --version 7.17.3 \
  --namespace monitoring \
  --create-namespace \
  -f elasticsearch-sonarqube-values.yaml

# Kibana (requiere que Elasticsearch esté disponible)
helm upgrade --install kibana elastic/kibana \
  --version 7.17.3 \
  --namespace monitoring \
  --create-namespace \
  -f kibana-sonarqube-values.yaml

# Filebeat
helm upgrade --install filebeat elastic/filebeat \
  --version 7.17.3 \
  --namespace monitoring \
  --create-namespace \
  -f filebeat-sonarqube-values.yaml
```

### 4.4 Verificar la instalación

```bash
# Verificar que los pods estén en estado Running/Ready
kubectl get pods -n monitoring -l app=elasticsearch-master -w
kubectl get pods -n monitoring -l app=kibana
kubectl get pods -n monitoring -l app=filebeat-filebeat

# Esperar a que Elasticsearch esté listo
kubectl wait --for=condition=Ready elasticsearch/elasticsearch -n monitoring --timeout=300s
```

## 5. Detalles de Implementación

### 5.1 Configuración de Elasticsearch

El archivo `elasticsearch-values.yaml` define un clúster de un solo nodo con los siguientes ajustes:

```yaml
replicas: 1
minimumMasterNodes: 1

clusterHealthCheckParams: "wait_for_status=yellow&timeout=1s"

clusterConfig:
  elasticsearch.yml:
    cluster.routing.allocation.disk.threshold_enabled: false

volumeClaimTemplate:
  accessModes: ["ReadWriteOnce"]
  resources:
    requests:
      storage: 10Gi

persistence:
  enabled: true

resources:
  requests:
    cpu: "500m"
    memory: "2Gi"
  limits:
    memory: "2Gi"

esJavaOpts: "-Xmx1g -Xms1g"

extraLabels:
  part-of: microservices-platform
```

- **Persistencia**: Se solicita un PVC de 10 Gi para almacenar los índices. Se utiliza `volumeClaimTemplate` para que Helm genere el PVC automáticamente.
- **Recursos**: Límite de memoria de 2 Gi y solicitud de 2 Gi; el heap de Java se limita a 1 Gi (-Xmx1g).
- **Salud del clúster**: Se espera un estado `yellow` (tolerante para un solo nodo).
- **Desactivación del umbral de disco**: Para evitar bloqueos por espacio en entornos de desarrollo.

### 5.2 Configuración de Filebeat

Filebeat se despliega como DaemonSet para recolectar logs de cada nodo. El archivo `filebeat-values.yaml` incluye:

```yaml
daemonset:
  extraLabels:
    part-of: microservices-platform

  securityContext:
    runAsUser: 0
    privileged: true

  resources:
    requests:
      cpu: "100m"
      memory: "250Mi"
    limits:
      memory: "512Mi"

elasticsearchHosts: "http://elasticsearch-master:9200"
```

- **Seguridad**: `runAsUser: 0` y `privileged: true` son necesarios para acceder a los logs del sistema (por ejemplo, `/var/log/containers`). En producción se recomienda usar `runAsNonRoot` con capacidades específicas, pero para desarrollo se simplifica.
- **Recursos**: Solicita 100m de CPU y 250Mi de RAM, con límite de 512Mi.
- **Destino de logs**: Envía los logs al servicio `elasticsearch-master` en el puerto 9200 (nombre del servicio generado por el chart de Elasticsearch).

### 5.3 Configuración de Kibana

Kibana se conecta a Elasticsearch mediante la misma URL. El archivo `kibana-values.yaml` define:

```yaml
elasticsearchHosts: "http://elasticsearch-master:9200"

resources:
  requests:
    cpu: "500m"
    memory: "1Gi"
  limits:
    memory: "2Gi"

config:
  xpack.security.secureCookies: false

extraLabels:
  part-of: microservices-platform
```

- **Recursos**: Solicita 1Gi de memoria y permite hasta 2Gi.
- **Seguridad**: Se desactiva `secureCookies` para entornos sin TLS (por simplicidad en desarrollo).
- **Etiqueta de integración**: Al igual que los otros componentes, se etiqueta con `part-of: microservices-platform`.

### 5.4 Integración con el Proyecto

Todos los componentes llevan la etiqueta `extraLabels.part-of: microservices-platform`, lo que facilita la gestión mediante selectores y la identificación en herramientas como Prometheus (si se expusieran métricas).

## 6. Acceso a las Interfaces

### 6.1 Obtener credenciales de Elasticsearch

Elasticsearch genera un secret con el usuario `elastic`. Para obtener la contraseña:

```bash
kubectl get secrets --namespace monitoring elasticsearch-master-credentials -o jsonpath='{.data.password}' | base64 -d ; echo

# Retrieve the kibana service account token.
kubectl get secrets --namespace=monitoring kibana-kibana-es-token -o jsonpath='{.data.token}' | base64 -d
```

### 6.2 Acceder a Kibana (port-forward)

```bash
kubectl port-forward --namespace monitoring svc/kibana-kibana 5601:5601
```

Acceder a `http://localhost:5601` con usuario `elastic` y la contraseña obtenida.

### 6.3 Exposición mediante Ingress (Traefik)

Si se utiliza Traefik como ingress controller, se puede crear un recurso `Ingress` para exponer Kibana de forma externa. Ejemplo:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: kibana-ui-route
  namespace: monitoring
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`kibana.desarrollo`)
      kind: Rule
      services:
        - name: kibana-kibana
          port: 5601
```

Ajustar el host según la configuración de DNS local.

## 7. Guía de Operaciones Kubernetes

### 7.1 Verificar estado de los recursos

```bash
# Pods del stack
kubectl get pods -n monitoring -l 'app in (elasticsearch-master, kibana, filebeat)'

# PVCs
kubectl get pvc -n monitoring -l app=elasticsearch-master

# Servicios
kubectl get svc -n monitoring | grep -E 'elasticsearch|kibana|filebeat'
```

### 7.2 Logs de componentes

```bash
# Logs de Elasticsearch (primero obtener el nombre del pod)
kubectl logs -n monitoring elasticsearch-master-0

# Logs de Kibana
kubectl logs -n monitoring -l app=kibana

# Logs de Filebeat
kubectl logs -n monitoring -l app=filebeat-filebeat
```

### 7.3 Descripción detallada de recursos

```bash
kubectl describe statefulset -n monitoring elasticsearch-master
kubectl describe daemonset -n monitoring filebeat-filebeat
kubectl describe deployment -n monitoring kibana-kibana
```

### 7.4 Reinicio ordenado

Si se requiere reiniciar un componente:

```bash
# Elasticsearch (StatefulSet)
kubectl rollout restart statefulset elasticsearch-master -n monitoring

# Kibana (Deployment)
kubectl rollout restart deployment kibana-kibana -n monitoring

# Filebeat (DaemonSet)
kubectl rollout restart daemonset filebeat-filebeat -n monitoring
```

### 7.5 Solución de problemas comunes

**Elasticsearch no inicia**:
- Verificar los logs del pod: `kubectl logs -n monitoring elasticsearch-master-0`
- Revisar el PVC: `kubectl describe pvc -n monitoring elasticsearch-master-elasticsearch-master-0`
- Asegurar que el cluster tiene suficiente memoria y que el nodo cumple con los requisitos

**Kibana no se conecta a Elasticsearch**:
- Comprobar que el servicio `elasticsearch-master` está accesible: `kubectl exec -n monitoring <kibana-pod> -- curl -s http://elasticsearch-master:9200`
- Validar la configuración de `elasticsearchHosts` en el deployment

**Filebeat no envía logs**:
- Revisar los logs de Filebeat: `kubectl logs -n monitoring <filebeat-pod>`
- Verificar que Filebeat tiene permisos para leer `/var/log/containers` (requiere `privileged` o `runAsUser: 0`)
- Probar conectividad con Elasticsearch: desde un pod temporal, hacer `curl http://elasticsearch-master:9200`

**Filebeat falla después de reiniciar Minikube (error de archivo de bloqueo)**:

El problema es que después de reiniciar Minikube, Filebeat intenta usar un archivo de bloqueo (filebeat.lock) que pertenece a un proceso antiguo, lo que le impide iniciarse.

Para solucionarlo, tienes que eliminar manualmente ese archivo de bloqueo y forzar la recreación del DaemonSet.

**🛠️ Pasos para solucionar el error**

1. Identifica el nodo donde Filebeat está fallando.

El DaemonSet ejecuta un pod en cada nodo. Para saber en qué nodo(s) está el problema, usa:

```bash
kubectl get pods -n monitoring -o wide | grep filebeat
```

Esto te mostrará el nombre del pod y el nodo en el que se está ejecutando (en este caso, será minikube).

2. Accede al nodo problemático y elimina el archivo de bloqueo.

Conéctate a la máquina virtual de Minikube y elimina el archivo filebeat.lock.

```bash
# Acceder a la VM de Minikube
minikube ssh

# Una vez dentro, busca y elimina el archivo de bloqueo
sudo find /var/lib -name "filebeat.lock" -type f -exec rm -f {} \;

# Salir de la VM
exit
```

3. Elimina los pods de Filebeat que están fallando.

Después de limpiar el archivo de bloqueo en el(los) nodo(s), elimina los pods de Filebeat. El DaemonSet los recreará automáticamente con el estado limpio.

```bash
kubectl delete pods -n monitoring -l app=filebeat-filebeat
```

Si tu versión de kubectl es 1.15 o superior, puedes usar:

```bash
kubectl rollout restart daemonset filebeat-filebeat -n monitoring
```

Si no, la eliminación selectiva de pods es igual de efectiva.

**Kibana muestra error "Readiness probe failed: Error: Got HTTP code 000 but expected a 200"**:

La solución más rápida y efectiva para eliminar el error sin necesidad de modificar el values.yaml es parchear manualmente el Deployment después de la instalación, eliminando la sonda de readiness que está fallando.

**🔧 Solución manual (un solo comando)**

```bash
kubectl patch deployment kibana-kibana -n monitoring --type json -p='[{"op": "remove", "path": "/spec/template/spec/containers/0/readinessProbe"}]'
```

Este comando elimina la sonda de readiness del contenedor principal. Una vez aplicado, el Deployment se reiniciará y el nuevo pod ya no tendrá la sonda, por lo que se marcará como Ready inmediatamente y el error desaparecerá.

**✅ Verificación**

```bash
kubectl get pods -n monitoring -l app=kibana
```

El pod debería mostrar `Running` y `1/1` en READY.

**📌 ¿Por qué no funcionaron los intentos anteriores?**

- `readinessProbe: {}` en values.yaml no sobrescribe la sonda por defecto, porque el chart ya tiene una definición hardcodeada.
- Agregar un bloque `deployment.spec.template.spec.containers` en el values.yaml a veces duplica la sonda, causando el error "may not specify more than 1 handler type".
- Parchear directamente el Deployment es la forma más directa de eliminar la sonda sin conflictos.

**🔁 Si quieres mantener la sonda pero con el endpoint correcto**

En lugar de eliminarla, podrías corregirla también con un patch:

```bash
kubectl patch deployment kibana-kibana -n monitoring --type json -p='[{"op": "replace", "path": "/spec/template/spec/containers/0/readinessProbe/httpGet/path", "value": "/api/status"}]'
```

Pero como el error de HTTP code 000 indica que ni siquiera puede conectar al puerto, es más seguro eliminar la sonda (para un laboratorio). Una vez que Kibana esté funcionando y accesible, puedes optar por dejar la sonda eliminada.

## 8. Desinstalación completa

```bash
helm uninstall -n monitoring elasticsearch kibana filebeat
# Eliminar PVCs residuales (opcional)
kubectl delete pvc -n monitoring -l app=elasticsearch-master
```

---

**Proyecto: microservices-architecture | Entorno: Kubernetes (WSL2/Ubuntu) con ELK Stack**
