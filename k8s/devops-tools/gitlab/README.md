[![GitLab](https://img.shields.io/badge/GitLab-v18.10.1-blue)](https://gitlab.com/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-9.10.1-orange)](https://charts.gitlab.io/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# GitLab – Repositorio de código y plataforma DevOps

GitLab actúa como el sistema central de control de versiones para la arquitectura de microservicios. Se despliega en modo "light" optimizado para entornos de desarrollo, reutilizando componentes existentes (Redis) y con recursos ajustados para clústeres pequeños como WSL2/Minikube.

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

GitLab es una plataforma DevOps integral que proporciona:

- **Repositorio Git** para alojar el código fuente de los microservicios.
- **Gestión de proyectos** con issues, merge requests y wikis.
- **CI/CD integrado** (aunque en este proyecto se usará Jenkins como motor de pipelines, GitLab solo actuará como SCM).

El despliegue se realiza con el chart oficial de GitLab, utilizando una configuración **light**:
- Recursos reducidos para webservice, sidekiq y Gitaly.
- **Redis externo** (el clúster ya desplegado con Redis Operator en `event-management`).
- **PostgreSQL externo** (base de datos desplegada en `devops-tools` con Bitnami).
- Métricas Prometheus habilitadas y logs recolectados por Filebeat.

### 1.1 Dependencias Externas

Esta implementación de GitLab está configurada para trabajar con **servicios externos** en lugar de desplegar sus propias instancias de Redis y PostgreSQL. Esto permite:

- **Optimizar recursos**: Evita duplicar servicios que ya existen en el clúster.
- **Centralizar gestión**: Redis y PostgreSQL pueden ser compartidos por múltiples aplicaciones (GitLab, Nexus, etc.).
- **Simplificar operaciones**: Un solo punto de administración para bases de datos y caché.

**Servicios externos requeridos:**

| Servicio | Ubicación | Propósito |
|----------|-----------|-----------|
| **Redis** | `redis-cluster.event-management.svc.cluster.local:6379` | Caché de sesiones, jobs y datos temporales de GitLab |
| **PostgreSQL** | `postgresql.devops-tools.svc.cluster.local:5432` | Base de datos principal (`gitlabhq_production`) con usuario `gitlab` |

**Configuración en `gitlab-values.yaml`:**

```yaml
global:
  # Redis externo
  redis:
    host: redis-cluster.event-management.svc.cluster.local
    port: 6379
    auth:
      enabled: false
  
  # PostgreSQL externo
  psql:
    host: postgresql.devops-tools.svc.cluster.local
    port: 5432
    username: gitlab
    database: gitlabhq_production
    password:
      plain: "gitlab789"
    sslmode: disable

# Deshabilitar instalación de componentes internos
postgresql:
  install: false

redis:
  install: false
```

**Nota importante:** Antes de instalar GitLab, asegúrese de que:
1. Redis está desplegado y accesible en el namespace `event-management`
2. PostgreSQL está desplegado en el namespace `devops-tools`
3. La base de datos `gitlabhq_production` y el usuario `gitlab` han sido creados en PostgreSQL

**Comandos para crear el esquema de GitLab en PostgreSQL:**

Si aún no has creado el usuario y la base de datos de GitLab en PostgreSQL, ejecuta los siguientes comandos:

```bash
# Crear usuario gitlab
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "CREATE USER gitlab WITH PASSWORD 'gitlab789';"

# Crear base de datos gitlabhq_production
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "CREATE DATABASE gitlabhq_production OWNER gitlab;"

# Otorgar privilegios al usuario gitlab
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE gitlabhq_production TO gitlab;"
```

**Verificar la creación:**

```bash
# Listar usuarios
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "\du"

# Listar bases de datos
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres -c "\l"
```

**Método alternativo (interactivo con extensiones):**

Si prefieres crear el usuario, la base de datos y las extensiones necesarias de forma interactiva:

1. Conéctate a PostgreSQL:

```bash
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres
```

2. Ejecuta los siguientes comandos SQL:

```sql
-- Crear el usuario gitlab (si no existe)
CREATE USER gitlab WITH PASSWORD 'gitlab789';

-- Crear la base de datos con el nombre correcto
CREATE DATABASE gitlabhq_production OWNER gitlab;

-- Otorgar todos los privilegios
GRANT ALL PRIVILEGES ON DATABASE gitlabhq_production TO gitlab;

-- Conectarse a la nueva base y crear extensiones
\c gitlabhq_production
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE EXTENSION IF NOT EXISTS amcheck;

-- Salir
\q
```

3. Verifica que aparezca en la lista:

```sql
\l
```

**Asegurar permisos completos del usuario gitlab:**

Si necesitas verificar o corregir los permisos del usuario `gitlab` sobre la base de datos y el esquema:

1. Conéctate a PostgreSQL:

```bash
kubectl exec -it -n devops-tools postgresql-0 -- psql -U postgres
```

2. Cambiar el propietario de la base de datos a gitlab:

```sql
ALTER DATABASE gitlabhq_production OWNER TO gitlab;
```

Verifica con `\l` que ahora el propietario sea `gitlab`.

3. Asegura que el usuario gitlab tenga todos los privilegios (ya los tiene, pero por si acaso):

```sql
GRANT ALL PRIVILEGES ON DATABASE gitlabhq_production TO gitlab;
```

4. Conéctate a la base y otorga privilegios sobre el esquema public:

```sql
\c gitlabhq_production
GRANT ALL ON SCHEMA public TO gitlab;
```

5. Sal de PostgreSQL:

```sql
\q
```

---

## 2. Documentación Oficial

- [GitLab Helm Chart](https://docs.gitlab.com/charts/)
- [GitLab Architecture](https://docs.gitlab.com/ee/architecture/)
- [GitLab Community Edition](https://about.gitlab.com/install/)
- [Configuración de recursos reducidos](https://docs.gitlab.com/charts/charts/globals.html#configure-resource-requests-and-limits)

---

## 3. Requisitos Previos

| Recurso                  | Versión / Detalle |
|--------------------------|-------------------|
| Kubernetes               | v1.35.1 (probado en WSL2/Ubuntu con Minikube) |
| kubectl                  | Configurado con acceso al clúster |
| Helm                     | v3.18.3 |
| Namespace `devops-tools` | Se creará automáticamente |
| Redis externo            | `redis-cluster.event-management.svc.cluster.local:6379` (sin contraseña) |
| StorageClass             | `standard` disponible (en Minikube, asegurar `storage-provisioner` activado) |
| Prometheus Operator      | Para el ServiceMonitor de métricas |
| Filebeat                 | Para recolección de logs (ya desplegado en `monitoring`) |

---

## 4. Instalación

### 4.1 Agregar repositorio de GitLab

```bash
helm repo add gitlab https://charts.gitlab.io
helm repo update
```

### 4.2 Crear Secret para la contraseña de PostgreSQL

Para conectarse a la base de datos PostgreSQL de forma segura, es necesario crear un **Kubernetes Secret** que contenga la contraseña del usuario `gitlab`. Este Secret será referenciado en el archivo `gitlab-values.yaml`.

**Crear el Secret:**

```bash
kubectl create secret generic gitlab-postgres-password \
  --namespace devops-tools \
  --from-literal=password=gitlab789
```

**Verificar la creación del Secret:**

```bash
kubectl get secret gitlab-postgres-password -n devops-tools
```

**Configuración en `gitlab-values.yaml`:**

En lugar de usar `password.plain` (que expone la contraseña en texto plano), se debe usar la referencia al Secret:

```yaml
global:
  psql:
    host: postgresql.devops-tools.svc.cluster.local
    port: 5432
    username: gitlab
    database: gitlabhq_production
    password:
      secret: gitlab-postgres-password  # Nombre del Secret
      key: password                      # Clave dentro del Secret
    sslmode: disable
```

**Nota:** Esta es la forma recomendada de manejar credenciales en Kubernetes, evitando almacenar contraseñas en texto plano en los archivos de configuración.

### 4.3 Crear archivo gitlab-values.yaml

Guarda el siguiente contenido (ajusta la IP en `global.hosts.domain` según la IP de tu clúster):

```yaml
# gitlab-sonarqube-values.yaml – Configuración minimalista para repositorio GitLab
global:
  common:
    labels:
      part-of: microservices-platform

  edition: ce

  hosts:
    domain: gitlab.desarrollo
    https: false

  ingress:
    configureCertmanager: false
    tls:
      enabled: false

  redis:
    host: redis.event-management.svc.cluster.local
    port: 6379
    auth:
      enabled: false
  
  psql:
    host: postgresql.devops-tools.svc.cluster.local
    port: 5432
    username: gitlab
    database: gitlabhq_production
    password:
      secret: gitlab-postgres-password
      key: password
    sslmode: disable

  metrics:
    enabled: true
    serviceMonitor:
      enabled: true
      labels:
        release: prometheus

  appConfig:
    lfs:
      enabled: false
    uploads:
      enabled: false
    packages:
      enabled: false
    ci:
      incremental_logging: false

  kas:
    enabled: false

nginx-ingress:
  enabled: false

registry:
  enabled: false

prometheus:
  install: false

redis:
  install: false

gitlab-runner:
  install: false

postgresql:
  install: false

webservice:
  logLevel: debug
  replicas: 1
  resources:
    requests:
      cpu: 100m
      memory: 512Mi
    limits:
      cpu: 500m
      memory: 1Gi

sidekiq:
  logLevel: debug
  replicas: 1
  resources:
    requests:
      cpu: 100m
      memory: 1Gi
    limits:
      cpu: 500m
      memory: 2Gi

gitlab:
  gitlab-exporter:
    enabled: false
  registry:
    enabled: false
  pages:
    enabled: false
  gitlab-prometheus:
    enabled: false
  nginx-ingress:
    enabled: false
  toolbox:
    enabled: false
  mailroom:
    enabled: false
  spamcheck:
    enabled: false
  gitlab-pages:
    enabled: false
  praefect:
    enabled: false
  geo-logcursor:
    enabled: false
```

### 4.3 Solución al conflicto de CRDs de cert-manager

El error que estás recibiendo confirma que Helm está recorriendo la lista de CRDs de **cert-manager** uno por uno. Ya pasamos por `challenges.acme.cert-manager.io` y ahora se ha detenido en `certificaterequests.cert-manager.io`.

Helm bloquea la instalación porque estos recursos existen globalmente en tu clúster de Minikube (probablemente de una instalación previa) y el Chart de GitLab intenta tomarlos como propios.

#### La Solución (Limpieza Masiva)

Para evitar ir uno por uno, ejecuta este bloque de comandos en tu terminal para "ceder" todos los CRDs de cert-manager al release de GitLab. Esto corregirá los metadatos de propiedad que Helm está exigiendo:

```bash
# Definir variables para evitar errores de escritura
RELEASE="gitlab"
NAMESPACE="devops-tools"

# Lista de CRDs que están causando el conflicto
CRDS=(
  "certificaterequests.cert-manager.io"
  "certificates.cert-manager.io"
  "challenges.acme.cert-manager.io"
  "clusterissuers.cert-manager.io"
  "issuers.cert-manager.io"
  "orders.acme.cert-manager.io"
)

for crd in "${CRDS[@]}"; do
  echo "Etiquetando CRD: $crd"
  kubectl annotate crd "$crd" meta.helm.sh/release-name="$RELEASE" --overwrite
  kubectl annotate crd "$crd" meta.helm.sh/release-namespace="$NAMESPACE" --overwrite
  kubectl label crd "$crd" app.kubernetes.io/managed-by=Helm --overwrite
done
```

#### Por qué esto detendrá los errores

Helm utiliza tres anotaciones/etiquetas específicas para decidir si puede gestionar un recurso existente:

1. `meta.helm.sh/release-name`: Debe coincidir con el nombre de tu instalación (`gitlab`).
2. `meta.helm.sh/release-namespace`: Debe coincidir con el namespace donde instalas (`devops-tools`).
3. `app.kubernetes.io/managed-by`: Debe ser `Helm`.

Al ejecutar el script de arriba, le estás diciendo a Kubernetes que esos CRDs "pertenecen" a la instalación que estás intentando hacer, permitiendo que `helm upgrade --install` proceda sin errores de validación.

> **Nota:** Esta solución es segura para desarrollo. Los CRDs seguirán funcionando normalmente con cert-manager, solo que ahora Helm también los "reconoce" como parte de GitLab.


> **Nota:** Estos secrets se crean vacíos para desarrollo. En producción, deberías usar valores seguros generados aleatoriamente.

### 4.5 Validar configuración (dry-run)

```bash
helm upgrade --install gitlab gitlab/gitlab \
  --namespace devops-tools \
  --create-namespace \
  -f gitlab-sonarqube-values.yaml \
  --dry-run --debug
```

### 4.6 Instalar GitLab

```bash
helm upgrade --install gitlab gitlab/gitlab \
  --namespace devops-tools \
  --create-namespace \
  -f gitlab-sonarqube-values.yaml
```

### 4.7 Verificar la instalación

```bash
kubectl get pods -n devops-tools-tools -w
```

Los pods pueden tardar varios minutos en estar Running. Espera a que los pods `webservice`, `sidekiq`, `gitaly` y `postgresql` estén listos.

## 5. Detalles de Implementación

### 5.1 Configuración 

Se eligió un perfil reducido para que GitLab quepa en un clúster de desarrollo (~28GB RAM). Las principales decisiones:

- **Redis externo**: se reutiliza el clúster Redis desplegado con el Redis Operator en `event-management`, ahorrando recursos y evitando otro servicio.
- **MinIO desactivado**: en desarrollo los artefactos de pipelines no son críticos; se pueden perder sin afectar los repositorios. Esto evita un PVC adicional.
- **PostgreSQL con persistencia reducida**: 8Gi es suficiente para pruebas iniciales.
- **Recursos ajustados**: cada componente tiene límites bajos pero suficientes para un uso básico.

### 5.2 Integración con Prometheus

El bloque `global.metrics.serviceMonitor` crea automáticamente los ServiceMonitors para los componentes de GitLab. La etiqueta `release: prometheus` permite que el Prometheus Operator los descubra. Las métricas estarán disponibles en los targets de Prometheus.

### 5.3 Logs con Filebeat

Filebeat (desplegado en `monitoring`) recoge todos los logs de los contenedores de GitLab sin configuración adicional. Los logs aparecerán en Kibana con los campos `kubernetes.namespace=devops` y `kubernetes.container.name` (webservice, sidekiq, etc.).

### 5.4 Persistencia

- **PostgreSQL**: PVC de 8Gi con StorageClass `standard` (se debe asegurar que el provisionador esté activo en Minikube).
- **Gitaly**: datos de repositorios almacenados en su propio PVC (tamaño por defecto 10Gi). Si se necesita más, se puede ajustar en el values.
- **MinIO**: desactivado; no se almacenan artefactos.

## 6. Integración con el Ecosistema

### 6.1 Prometheus

Los ServiceMonitors creados exponen métricas en los endpoints `/metrics` de cada componente. Puedes consultar en Prometheus:

- `gitlab_webservice_requests_total`
- `gitlab_sidekiq_jobs_completed_total`
- `gitlab_gitaly_requests_total`

### 6.2 Traefik (Ingress)

Para exponer GitLab externamente, se puede crear un IngressRoute (el chart ya usa Ingress automáticamente con el dominio configurado). Asegúrate de que Traefik esté escuchando. Ejemplo:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: gitlab-web-route
  namespace: devops
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`gitlab.192.168.49.2.nip.io`)
      kind: Rule
      services:
        - name: gitlab-webservice-default
          port: 8181
```

### 6.3 ELK

Los logs de GitLab se envían automáticamente a Elasticsearch mediante Filebeat. En Kibana, filtra por `kubernetes.namespace=devops` para verlos.

## 7. Acceso

### 7.1 Obtener la contraseña de root

```bash
kubectl get secret gitlab-gitlab-initial-root-password -n devops-tools -o jsonpath='{.data.password}' | base64 -d ; echo
```

### 7.2 Port-forward (acceso local)

```bash
kubectl port-forward -n devops-tools svc/gitlab-webservice-default 8080:8181
```

Abre `http://localhost:8080` en tu navegador. Usa usuario `root` y la contraseña obtenida.

### 7.3 Acceso por Ingress

Si configuraste el dominio (por ejemplo, `gitlab.192.168.49.2.nip.io`), puedes acceder directamente si tienes Traefik funcionando. Asegúrate de que la IP resuelva.

## 8. Guía de Operaciones Kubernetes

### 8.1 Verificar estado de los recursos

```bash
kubectl get pods -n devops-tools
kubectl get pvc -n devops-tools
kubectl get servicemonitor -n devops-tools
```

### 8.2 Logs de un componente

```bash
kubectl logs -n devops-tools -l app=webservice
kubectl logs -n devops-tools -l app=sidekiq
```

### 8.3 Reiniciar un despliegue

```bash
kubectl rollout restart deployment -n devops-tools gitlab-webservice-default
```

### 8.4 Solución de problemas comunes

**PostgreSQL no arranca**: Verificar que el PVC esté `Bound` (`kubectl get pvc -n devops-tools`). En Minikube, activar `minikube addons enable storage-provisioner`.

**Redis no alcanzable**: Comprobar que el servicio `redis-cluster.event-management.svc.cluster.local` existe y es accesible desde el namespace `devops`.

**Métricas no aparecen**: Confirmar que los ServiceMonitors tienen la etiqueta `release: prometheus` y que el Prometheus Operator está funcionando.

**GitLab tarda en iniciar**: Es normal, especialmente la primera vez. Puede tomar 5-10 minutos mientras se configura la base de datos y los repositorios iniciales.

## 9. Desinstalación

### 9.1 Eliminar el release de Helm

```bash
helm uninstall -n devops-tools gitlab
```

### 9.2 Eliminar PVCs residuales

```bash
kubectl delete pvc -n devops-tools --all
```

### 9.3 Eliminar ServiceMonitors y otros recursos

```bash
kubectl delete servicemonitor -n devops-tools --all
```

---

**Proyecto: microservices-architecture | Entorno: Kubernetes (WSL2/Ubuntu) con GitLab como repositorio de código**