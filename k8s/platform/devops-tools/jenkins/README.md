[![Jenkins](https://img.shields.io/badge/Jenkins-2.541.3-blue)](https://jenkins.io/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-5.9.12-orange)](https://github.com/jenkinsci/helm-charts)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# Jenkins – Servidor de CI/CD para microservicios

Jenkins es el motor de integración y entrega continua que orquesta los pipelines de construcción, prueba y despliegue de los microservicios. Se despliega en el namespace `devops-tools`  con una configuración optimizada para entornos de desarrollo (WSL2/Ubuntu), utilizando el chart oficial de Jenkins y adaptado a los recursos limitados del clúster.

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

Jenkins se despliega como un servidor de CI/CD autónomo dentro del clúster de Kubernetes. Sus funciones principales son:

- **Ejecutar pipelines** de construcción, pruebas y despliegue.
- **Integrarse con GitLab** (repositorio de código) mediante el plugin `gitlab-plugin`, permitiendo webhooks y reporte de estados.
- **Construir imágenes Docker** usando el plugin `docker-workflow`.
- **Crear agentes efímeros** en Kubernetes (plugin `kubernetes`) para ejecutar builds de forma aislada.

La configuración se realiza mediante Helm, utilizando el archivo `jenkins-values.yaml`  que define recursos reducidos, persistencia, plugins preinstalados y etiquetas de integración (`part-of: microservices-platform`).

---

## 2. Documentación Oficial

- [Jenkins Helm Chart](https://github.com/jenkinsci/helm-charts)
- [Jenkins Configuration as Code (JCasC)](https://plugins.jenkins.io/configuration-as-code/)
- [GitLab Plugin](https://plugins.jenkins.io/gitlab-plugin/)
- [Docker Workflow Plugin](https://plugins.jenkins.io/docker-workflow/)
- [Kubernetes Plugin](https://plugins.jenkins.io/kubernetes/)

---

## 3. Requisitos Previos

| Recurso | Versión / Detalle |
|---------|-------------------|
| Kubernetes | v1.35.1 (probado en WSL2/Ubuntu con Minikube v1.38.1) |
| kubectl | v1.34.1+ configurado con acceso al clúster |
| Helm | v3.18.3+ |
| Namespace `devops-tools` | Debe existir (se crea automáticamente con `--create-namespace`) |
| StorageClass | Debe existir uno (ej. `standard` en Minikube) |
| Prometheus Operator | Opcional, para el ServiceMonitor de métricas |
| GitLab | Accesible desde el namespace `devops-tools` para integración CI/CD |
| Archivo `jenkins-values.yaml` | Disponible en el directorio de trabajo |

---

## 4. Instalación

### 4.1 Agregar repositorio de Jenkins

```bash
helm repo add jenkins https://charts.jenkins.io
helm repo update
```

### 4.2 Validar configuración (dry-run)

```bash
helm upgrade --install jenkins jenkins/jenkins \
  --namespace devops-tools \
  --create-namespace \
  -f jenkins-sonarqube-values.yaml \
  --dry-run --debug
```

### 4.3 Instalar o actualizar Jenkins

```bash
helm upgrade --install jenkins jenkins/jenkins \
  --namespace devops-tools \
  --create-namespace \
  -f jenkins-sonarqube-values.yaml
```

### 4.4 Verificar la instalación

```bash
kubectl get pods -n devops-tools -l app.kubernetes.io/name=jenkins -w
```

El pod jenkins-0 pasará a estado Running después de unos minutos, mientras instala los plugins definidos.

## 5. Detalles de Implementación

El archivo jenkins-values.yaml contiene todas las personalizaciones necesarias para un entorno de desarrollo. A continuación se explican los bloques clave.

### 5.1 Etiquetado y coherencia

Se añaden etiquetas a los recursos del controlador para identificarlos como parte de la arquitectura:

```yaml
controller:
  labels:
    part-of: microservices-platform
  podLabels:
    part-of: microservices-platform
  serviceLabels:
    part-of: microservices-platform
```

Esto facilita la gestión mediante selectores y la integración con Prometheus y ELK.

### 5.2 Recursos ajustados al clúster

Los recursos están configurados para adaptarse a un clúster de 28 GB (como el del proyecto), evitando sobrecarga:

```yaml
controller:
  resources:
    requests:
      cpu: "300m"
      memory: "1Gi"
    limits:
      cpu: "500m"
      memory: "2Gi"
```

Solicitudes (requests): 300m CPU y 1 Gi de memoria, garantizan que el pod obtenga recursos suficientes.
Límites (limits): 500m CPU y 2 Gi de memoria, evitan que el pod consuma excesivos recursos.

### 5.3 Persistencia

Se habilita un volumen persistente de 10 Gi para almacenar el directorio /var/jenkins_home, asegurando que las configuraciones, jobs e historial sobrevivan a reinicios del pod.

```yaml
persistence:
  enabled: true
  size: "10Gi"
  storageClass: "standard"
```

### 5.4 Plugins preinstalados

La lista de installPlugins incluye los plugins esenciales para la integración con GitLab, Docker y Kubernetes:

| Plugin | Propósito |
|--------|-----------|
| kubernetes | Permite crear pods dinámicos como agentes de build. |
| gitlab-plugin | Integra Jenkins con GitLab (webhooks, reporte de estado). |
| docker-workflow | Construye y publica imágenes Docker desde pipelines. |
| configuration-as-code | Gestiona la configuración de Jenkins mediante YAML (JCasC). |
| pipeline-stage-view | Visualización gráfica de etapas en pipelines. |

### 5.5 Credenciales de administrador

Se establece un usuario y contraseña fijos para simplificar el acceso inicial:

```yaml
controller:
  admin:
    user: "admin"
    password: "admin123"   # Cambiar en producción
```

### 5.6 Configuración de salud (probes)

Las sondas de readiness y liveness se configuran con tiempos de espera amplios para permitir el arranque completo de Jenkins:

```yaml
controller:
  readinessProbe:
    initialDelaySeconds: 60
    periodSeconds: 10
    failureThreshold: 12
  livenessProbe:
    initialDelaySeconds: 120
    periodSeconds: 30
    failureThreshold: 6
```

### 5.7 Ingress y servicio

El servicio se mantiene como ClusterIP porque la exposición externa se realizará mediante un IngressRoute de Traefik (no se incluye en el chart). El bloque ingress.enabled: false evita que el chart cree su propio Ingress.

## 6. Integración con el Ecosistema

### 6.1 Prometheus

El archivo jenkins-values.yaml habilita la integración con Prometheus:

```yaml
prometheus:
  enabled: true
  serviceMonitor:
    enabled: true
    labels:
      release: prometheus
    interval: 30s
```

Esto crea un ServiceMonitor que Prometheus Operator descubre automáticamente, exponiendo métricas de Jenkins en /prometheus.

### 6.2 Traefik (Ingress)

Para exponer Jenkins externamente, se debe crear un IngressRoute en el directorio de ingresses de Traefik. Ejemplo:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: jenkins-ui-route
  namespace: devops-tools
  labels:
    part-of: microservices-platform
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`jenkins.desarrollo`)
      kind: Rule
      services:
        - name: jenkins
          port: 8080
```

Luego, añadir la entrada en /etc/hosts apuntando a la IP del clúster (ej. minikube ip).

### 6.3 ELK y Jaeger

Logs: Jenkins escribe logs en stdout, que Filebeat recoge y envía a Elasticsearch. Se pueden ver en Kibana filtrando por kubernetes.namespace=devops-tools y kubernetes.container.name=jenkins.

Trazas: Jenkins no envía trazas de forma nativa, pero las aplicaciones que construye pueden instrumentarse con OpenTelemetry.

### 6.4 GitLab

Con el plugin gitlab-plugin instalado, se puede configurar en Jenkins la URL de GitLab y un token de acceso para que los pipelines reciban webhooks y reporten el estado de los builds. La configuración se realiza en Administrar Jenkins → Configurar sistema → GitLab.

## 7. Acceso

### 7.1 Port-forward (acceso local rápido)

```bash
kubectl port-forward -n devops-tools svc/jenkins 8080:8080
```

Abrir http://localhost:8080 en el navegador. Usar usuario admin y la contraseña definida (admin123 o la que se configuró).

### 7.2 Ingress con Traefik

Una vez creado el IngressRoute, acceder a http://jenkins.desarrollo (previo ajuste de DNS local).

### 7.3 Obtener la contraseña inicial (si no se definió en values)

```bash
kubectl exec -n devops-tools jenkins-0 -- cat /var/jenkins_home/secrets/initialAdminPassword
```

## 8. Guía de Operaciones Kubernetes

### 8.1 Verificar estado

```bash
kubectl get statefulset,svc,pod -n devops-tools -l app.kubernetes.io/name=jenkins
kubectl get pvc -n devops-tools -l app.kubernetes.io/name=jenkins
```

### 8.2 Logs

```bash
# Logs del contenedor principal de Jenkins
kubectl logs -n devops-tools jenkins-0 -c jenkins -f

# Logs del sidecar de recarga de configuración
kubectl logs -n devops-tools jenkins-0 -c config-reload -f
```

### 8.3 Reiniciar Jenkins

```bash
kubectl delete pod -n devops-tools jenkins-0
```

El StatefulSet recreará el pod con la misma configuración.

### 8.4 Solución de problemas comunes

- **Pod en Pending:** Verificar que los recursos del nodo no estén saturados (kubectl describe node). Si es necesario, reducir las solicitudes de CPU en el values.

- **Fallo de plugins al inicio:** Revisar los logs del contenedor jenkins; pueden deberse a errores de red durante la descarga. Se pueden instalar manualmente desde la interfaz web.

- **Métricas no aparecen en Prometheus:** Comprobar que el ServiceMonitor se creó (kubectl get servicemonitor -n devops-tools) y que tiene la etiqueta release: prometheus.

- **Probes fallando:** Si los tiempos de arranque son mayores, aumentar initialDelaySeconds en el values.

- **Jenkins no inicia después de reiniciar Minikube (Init:CrashLoopBackOff):**

  Después de reiniciar el nodo de Minikube (o al recrear el pod), Jenkins no iniciaba correctamente. El pod entraba en estado Init:CrashLoopBackOff y el contenedor principal nunca se levantaba. Los logs del init container (nombre init) mostraban repetidamente:

  ```text
  cp: overwrite '/var/jenkins_plugins/...'?
  ```

  El init container ejecutaba un script (apply_config.sh) que copiaba los plugins desde /usr/share/jenkins/ref/plugins/ a /var/jenkins_plugins/ usando el comando:

  ```bash
  yes n | cp -i /usr/share/jenkins/ref/plugins/* /var/jenkins_plugins/ ;
  ```

  A pesar de usar yes n |, el comando cp -i seguía esperando confirmación interactiva, bloqueando la inicialización. Esto ocurría porque el volumen persistente (/var/jenkins_home) ya contenía plugins de ejecuciones anteriores, y el script no estaba diseñado para manejar sobrescritura de forma no interactiva. Como resultado, el init container nunca terminaba y Jenkins no arrancaba.

  **Solución aplicada**

  Se modificó el script apply_config.sh dentro del ConfigMap jenkins (ubicado en el namespace devops-tools). La línea problemática se reemplazó por:

  ```bash
  cp -n /usr/share/jenkins/ref/plugins/* /var/jenkins_plugins/ 2>/dev/null || true
  ```

  - `-n` : evita sobrescribir archivos existentes (no pregunta).
  - `2>/dev/null` : descarta mensajes de error (ej. si no hay archivos).
  - `|| true` : asegura que el script no falle si el comando retorna error.

  **Pasos realizados:**

  1. Editar el ConfigMap:

  ```bash
  kubectl edit configmap jenkins -n devops-tools
  ```

  Localizar la clave apply_config.sh y modificar la línea mencionada.

  Guardar los cambios.

  2. Elimina el pod de Jenkins para que se recree con la nueva configuración:

  ```bash
  kubectl delete pod jenkins-0 -n devops-tools
  ```

## 9. Desinstalación

### 9.1 Eliminar el release de Helm

```bash
helm uninstall jenkins -n devops-tools
```

### 9.2 Eliminar PVCs residuales (opcional)

```bash
kubectl delete pvc -n devops-tools -l app.kubernetes.io/name=jenkins
```

### 9.3 Eliminar ServiceMonitor e IngressRoute (si se crearon manualmente)

```bash
kubectl delete servicemonitor jenkins -n devops-tools
kubectl delete ingressroute jenkins-ui-route -n devops-tools
```
