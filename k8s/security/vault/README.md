[![Vault](https://img.shields.io/badge/Vault-1.21-blue)](https://www.vaultproject.io/)
[![Helm Chart](https://img.shields.io/badge/Helm%20Chart-v3.18.3-orange)](https://github.com/hashicorp/vault-helm)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.35.1+-326CE5)](https://kubernetes.io/)

# Vault en Kubernetes

## 📖 Tabla de Contenidos

- [1. Introducción](#1-introducción)
- [2. Documentación](#2-documentación)
- [3. Requisitos Previos](#3-requisitos-previos)
- [4. Instalación](#4-instalación)
- [5. Detalles de Implementación](#5-detalles-de-implementación)
- [6. Acceso a Vault](#6-acceso-a-vault)
- [7. Guía de Operaciones Kubernetes](#7-guía-de-operaciones-kubernetes)
- [8. Desinstalación completa](#8-desinstalación-completa)

---

## 1. Introducción

Vault es una herramienta de gestión de secrets desarrollada por HashiCorp que centraliza el almacenamiento, control de acceso y rotación de credenciales sensibles (claves API, contraseñas, certificados). En este proyecto, se despliega en el namespace `security` utilizando Helm con una configuración personalizada que asegura persistencia de datos, etiquetado consistente (`part-of: microservices-platform`) y habilitación de la interfaz gráfica para facilitar la administración.

## 2. Documentación

- [Documentación oficial de Vault](https://www.vaultproject.io/docs)
- [Helm Chart de Vault en GitHub](https://github.com/hashicorp/vault-helm)
- [Guía de instalación con Helm en Kubernetes](https://developer.hashicorp.com/vault/docs/deploy/kubernetes/helm)
- [Integración con Kubernetes](https://developer.hashicorp.com/vault/tutorials/kubernetes)

## 3. Requisitos Previos

- Kubernetes 1.19+ (probado en WSL2/Ubuntu).
- `kubectl` configurado con acceso al clúster.
- `Helm` 3.0+ instalado.
- Namespace `security` creado (los comandos de instalación lo crean con `--create-namespace`).
- Archivo `vault-values.yaml` disponible en el directorio actual.
- Permisos de administrador para configurar RBAC (el operador necesita crear ServiceAccounts y roles).

## 4. Instalación

### 4.1 Agregar el repositorio de HashiCorp

```bash
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update
```

### 4.2 Validar la configuración (dry-run)

```bash
helm install vault hashicorp/vault \
  --namespace security \
  --create-namespace \
  -f vault-sonarqube-values.yaml \
  --dry-run --debug
```

### 4.3 Instalar o actualizar Vault

```bash
helm upgrade --install vault hashicorp/vault \
  --namespace security \
  --create-namespace \
  -f vault-sonarqube-values.yaml
```

### 4.4 Verificar la instalación

```bash
# Verificar que los pods estén en estado Running/Ready
kubectl get pods -n security -l app.kubernetes.io/name=vault

# Esperar a que Vault esté listo
kubectl wait --for=condition=Ready vault-0 -n security --timeout=300s
```

## 5. Detalles de Implementación

### 5.1 Etiquetado y coherencia

Para integrar Vault con la arquitectura de microservicios, se añaden etiquetas a los recursos del servidor:

```yaml
server:
  extraLabels:
    part-of: "microservices-architecture"
  podLabels:
    part-of: "microservices-architecture"
```

Esto permite identificar fácilmente todos los componentes de Vault mediante selectores y mantener consistencia en el etiquetado global del proyecto.

### 5.2 Persistencia de datos

Se habilita el almacenamiento persistente con un volumen de 10 Gi para garantizar que los secretos y la configuración sobrevivan a reinicios del clúster:

```yaml
server:
  dataStorage:
    enabled: true
    size: 10Gi
```

El chart genera un PersistentVolumeClaim que utiliza el StorageClass por defecto del clúster. En entornos WSL2/Ubuntu con Minikube, se recomienda usar el driver de almacenamiento apropiado (por ejemplo, `standard`).

### 5.3 Interfaz gráfica (UI)

La UI se habilita para administración visual:

```yaml
ui:
  enabled: true
  serviceType: LoadBalancer
  externalPort: 8200
```

`serviceType: LoadBalancer` expone Vault mediante un balanceador externo. En Minikube, se debe ejecutar `minikube tunnel` para asignar una IP externa; en otros entornos puede requerir ajustes (por ejemplo, cambiar a `NodePort`). El puerto externo es el 8200, el mismo que el interno.

### 5.4 Componentes desplegados

La instalación personalizada crea:
- `vault-0`: Pod principal (StatefulSet) con el servidor Vault.
- `vault-agent-injector`: Mutating Admission Webhook para inyectar secretos en los pods.
- `vault-configurer`: Job opcional para configuraciones iniciales.
- `ServiceAccounts` y RBAC: Roles y bindings necesarios para el funcionamiento del operador.

## 6. Acceso a Vault

### 6.1 Port-forward local

```bash
kubectl port-forward -n security svc/vault 8200:8200
```

Acceder a `http://localhost:8200` en el navegador.

### 6.2 Inicialización y desbloqueo (primera vez)

Tras la instalación, Vault se encuentra sellado (sealed). Se debe inicializar y desbloquear:

```bash
# Inicializar Vault (solo una vez)
kubectl exec -it vault-0 -n security -- vault operator init

# El comando anterior devuelve 5 Unseal Keys y un Root Token
# Guardar estas claves de forma segura (por ejemplo, en un gestor de contraseñas).
Unseal Key 1: GWokmouVmZtJxhs+kfVPTaORP737oTB8YakF3/idwRWx
Unseal Key 2: e8cJttYh+67ftQV8TJhMktlw6i+7GGqrTe/BfvXycwCs
Unseal Key 3: rLxkmZLtmnNBkjm40YDNQTOL78+nk8aw1Bp7jTOER7bI
Unseal Key 4: gzXefVVWGcbejVi5gILtjfAtZhxpUjc8nCeyw0Hlrfmr
Unseal Key 5: K5H+aYrYel+XhnCAKYMq2O6bTz+KleTTQx9TFY/1HdYJ

Initial Root Token: hvs.bmAzY1Gy77WuokdE73VQRRqB

# Desbloquear Vault (requiere 3 claves)
kubectl exec -it vault-0 -n security -- vault operator unseal
# Repetir con tres claves diferentes
```

### 6.3 Acceso con root token

```bash
kubectl exec -it vault-0 -n security -- vault login <root-token>
```

### 6.4 Exposición mediante Ingress (Traefik)

Si se utiliza Traefik como ingress controller, se puede crear un recurso `Ingress` para exponer Vault externamente. Ejemplo:

```yaml
apiVersion: traefik.io/v1alpha1
kind: IngressRoute
metadata:
  name: vault-ui-route
  namespace: security  # Muy importante: debe coincidir con el namespace de Vault
  labels:
    part-of: microservices-platform
spec:
  entryPoints:
    - web
  routes:
    - match: Host(`vault.desarrollo`)  # Tu dominio local
      kind: Rule
      services:
        - name: vault-ui          # El nombre del servicio que crea el Helm de Vault
          port: 8200              # El puerto estándar de la UI de Vault
```

Ajustar el host según la configuración de DNS local.

## 7. Guía de Operaciones Kubernetes

### 7.1 Verificar estado del pod y del clúster

```bash
# Estado de los pods
kubectl get pods -n security -l app.kubernetes.io/name=vault

# Estado interno de Vault
kubectl exec -it vault-0 -n security -- vault status
```

### 7.2 Logs del servidor

```bash
kubectl logs -n security vault-0
```

### 7.3 Reiniciar Vault

```bash
# Reiniciar el StatefulSet
kubectl rollout restart statefulset/vault -n security
```

### 7.4 Solución de problemas comunes

**Vault no arranca**: Verificar que el PVC esté en estado `Bound` (`kubectl get pvc -n security`). Revisar los logs para errores de almacenamiento.

**UI no accesible**: Si se usa `serviceType: LoadBalancer` en Minikube, ejecutar `minikube tunnel` para asignar una IP externa. Alternativamente, cambiar a `NodePort` o usar port-forward.

**Error de permisos en el pod**: Asegurar que el `securityContext` por defecto del chart sea compatible con el clúster. En entornos con PodSecurityPolicy estricta, puede requerir ajustes adicionales.

**Clave de unseal perdida**: Si se pierden las claves de desbloqueo, no se podrá recuperar el acceso. Se debe hacer backup de las claves iniciales.

## 8. Desinstalación completa

```bash
helm uninstall -n security vault
# Eliminar PVCs residuales (opcional)
kubectl delete pvc -n security -l app.kubernetes.io/name=vault
```

---

**Proyecto: microservices-architecture | Entorno: Kubernetes (WSL2/Ubuntu) con Vault**
