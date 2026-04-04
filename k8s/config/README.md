# Requisitos Previos para Despliegues en Kubernetes con Minikube

Este documento describe los requisitos previos necesarios para desplegar aplicaciones en un clúster de Kubernetes local utilizando Minikube.

## 1. Configuración del Entorno Local con Minikube

### 1.1. Instalación de Minikube

Minikube es una herramienta que permite ejecutar un clúster de Kubernetes de un solo nodo localmente.

#### Windows:
```powershell
# Instalar Minikube
choco install minikube

# Iniciar Minikube (asegúrate de tener habilitada la virtualización en la BIOS)
minikube start --driver=hyperv  # o --driver=virtualbox si usas VirtualBox

# Configurar el contexto de kubectl
kubectl config use-context minikube
```

#### Linux
```bash
# Instalar Minikube
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Iniciar Minikube
minikube start --driver=docker  # o el driver que prefieras
```

### 1.2. Verificar la instalación

```bash

```

### 1.3. Configuración de recursos (opcional)

Puedes ajustar los recursos asignados a Minikube:

```bash
# Detener Minikube si está en ejecución
minikube stop

# Iniciar Minikube con recursos personalizados
minikube delete

```

## 2. Herramientas Necesarias

### 2.1. Herramientas Requeridas

- **kubectl**: Cliente de línea de comandos para interactuar con el clúster (estos son ejemplos ya que al instalar minikube se instala kubectl)
  - [Instalación en Windows](https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/)
  - [Instalación en Linux/macOS](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

- **Helm**: Gestor de paquetes para Kubernetes
  - Instalación en Windows:
    ```powershell
    choco install kubernetes-helm
    ```
  - Instalación en Linux/macOS:
    ```bash
    curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
    chmod 700 get_helm.sh
    ./get_helm.sh
    ```

- **Docker**: Para construir imágenes de contenedores
  - [Descargar Docker Desktop](https://www.docker.com/products/docker-desktop)

## 3. Configuración del Clúster de Minikube

### 3.1. Habilitar complementos de Minikube

Minikube incluye complementos que facilitan la configuración de componentes comunes:

```bash
# Habilitar el dashboard de Kubernetes
minikube addons enable dashboard

# Habilitar el Ingress Controller (NGINX)
minikube addons enable ingress

# Habilitar el registro de contenedores local
minikube addons enable registry

# Listar complementos disponibles
minikube addons list
```

### 3.2. Acceso al Dashboard de Kubernetes

Para acceder al dashboard de Kubernetes incluido en Minikube:

```bash
# Iniciar el dashboard
minikube dashboard
```

### 3.3. Configuración de Namespaces

Los namespaces deben estar configurados antes de desplegar cualquier aplicación. Ejecuta el siguiente comando para aplicar la configuración de namespaces:

```bash
kubectl apply -f namespace.yaml
```

### 3.4. Storage Classes en Minikube

Minikube incluye una StorageClass estándar llamada 'standard'. Para verificar:

Asegúrate de que las StorageClasses requeridas estén configuradas en el clúster. Las aplicaciones pueden requerir:

- `standard`: Para almacenamiento de propósito general (dinámico)
- `manual`: Para volúmenes persistentes con hostPath (desarrollo)
- `slow`: Para volúmenes de mayor capacidad pero menor rendimiento
- `nfs`: Para almacenamiento en red compartido (producción)

Verifica las StorageClasses disponibles:

```bash
kubectl get storageclass
```

### 3.5. Configuración de Almacenamiento Persistente

El proyecto soporta múltiples estrategias de almacenamiento para garantizar persistencia de datos:

#### 3.5.1. StorageClass `standard` (Dinámico)
- **Uso**: Desarrollo rápido y pruebas
- **Provisionamiento**: Automático mediante Minikube
- **Persistencia**: Limitada (se pierde al eliminar Minikube)
- **Configuración**: 
  ```yaml
  persistence:
    storageClass: standard
    ```

#### 3.5.2. StorageClass `manual` (HostPath)
- **Uso**: Desarrollo local con persistencia real
- **Provisionamiento**: Manual mediante PVs predefinidos
- **Persistencia**: Total (datos en filesystem del host)
- **Configuración**:
  ```yaml
  persistence:
    storageClass: manual
  ```
- **Requisitos**: Aplicar `persistent-volumes.yaml` antes de desplegar aplicaciones

#### 3.5.3. StorageClass `nfs` (Red Compartida)
- **Uso**: Producción y equipos de desarrollo
- **Provisionamiento**: Dinámico mediante NFS Provisioner
- **Persistencia**: Total y compartida entre nodos
- **Configuración**:
  ```yaml
  persistence:
    storageClass: nfs (ver storage-class.yaml)
  ```
- **Ventajas**:
  - Acceso concurrente desde múltiples pods
  - Backups centralizados
  - Recuperación de desastres simplificada
  - Misma configuración para dev, staging y producción

#### 3.5.4. Configuración del Servidor NFS

Para habilitar la StorageClass `nfs`:

```bash
# 1. Instalar servidor NFS
sudo apt install nfs-kernel-server  # Ubuntu/Debian
sudo yum install nfs-utils          # CentOS/RHEL

# 2. Crear estructura de directorios
sudo mkdir -p /opt/minikube/nfs-mnt/{jenkins,nexus,elasticsearch,rabbitmq,redis,prometheus,jaeger,vault,gitlab,sonarqube,argocd,trivy}
sudo chown -R nobody:nogroup /opt/minikube/nfs-mnt/
sudo chmod -R 777 /opt/minikube/nfs-mnt/

# 3. Configurar exports
echo "/opt/minikube/nfs-mnt *(rw,sync,no_subtree_check,no_root_squash)" | sudo tee -a /etc/exports
sudo exportfs -a
sudo systemctl restart nfs-kernel-server

# 4. Instalar NFS Provisioner en Kubernetes
helm repo add nfs-subdir-external-provisioner https://kubernetes-sigs.github.io/nfs-subdir-external-provisioner/
helm repo update
helm install nfs-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
  --namespace nfs-provisioner \
  --create-namespace \
  --set server.extraLabels.'part-of'=microservices-architecture \
  --set nfs.server=$(hostname -I | awk '{print $1}') \
  --set nfs.path=/opt/minikube/nfs-mnt
  
helm install nfs-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
  --namespace nfs-provisioner \
  --create-namespace \
  --set server.extraLabels.'part-of'=microservices-architecture \
  --set nfs.server=$(minikube ssh "ip route | grep default | awk '{print \$3}'") \
  --set nfs.path=/opt/minikube/nfs-mnt
  
 # 5. Desinstalar NFS Provisioner
 helm uninstall nfs-provisioner -n nfs-provisioner
```

#### 3.5.5. Recomendación de Uso

| Ambiente | StorageClass | Razón |
|----------|--------------|-------|
| Desarrollo individual | `standard` | Rapidez y simplicidad |
| Desarrollo en equipo | `nfs` | Compartir datos y configuraciones |
| Producción | `nfs` o cloud storage | Persistencia y escalabilidad |

**Nota**: La StorageClass `nfs` requiere configuración adicional del servidor NFS antes de su uso.

## 4. Configuración de Helm en Minikube

### 4.1. Inicializar Helm en Minikube

Asegúrate de que Tiller (si usas Helm 2) esté configurado correctamente. Para Helm 3 (recomendado), no se necesita Tiller.

```bash
# Verificar la versión de Helm
helm version

# Inicializar Helm (solo para Helm 2)
# helm init
```

### 4.2. Agregar Repositorios Comunes

```bash
# Repositorio de charts estables
helm repo add stable https://charts.helm.sh/stable

# Repositorio de Prometheus Community
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts

# Actualizar repositorios
helm repo update
```

### 4.3. Configuración de Valores por Defecto para Entorno Local

Al trabajar con Minikube, es recomendable ajustar los valores por defecto para optimizar el uso de recursos:

Crea un archivo `values.yaml` en el directorio de tu aplicación para personalizar la configuración. Ejemplo:

```yaml
# gitlab-sonarqube-values.yaml
global:
  namespace: tu-namespace
  storageClass: standard

replicaCount: 1

resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 200m
    memory: 256Mi
```

## 5. Configuración de Red en Minikube

### 5.1. Network Policies en Minikube

Minikube tiene habilitado el plugin de NetworkPolicy de forma predeterminada. Para verificar:

```bash
# Aplicar las políticas de red definidas en el archivo network-policies.yaml
kubectl apply -f network-policies.yaml

# Verificar que las políticas se hayan aplicado correctamente
kubectl get networkpolicies --all-namespaces
```

Asegúrate de que las políticas de red estén configuradas correctamente. Las aplicaciones pueden requerir reglas específicas para la comunicación entre servicios.

### 5.2. Ingress Controller con Traefik

El proyecto utiliza **Traefik** como Ingress Controller principal en lugar del NGINX Ingress Controller de Minikube. Traefik ofrece:

- **Descubrimiento automático** de servicios
- **Integración nativa** con Docker y Kubernetes
- **Dashboard web** para monitoreo
- **Configuración dinámica** sin reinicios


#### 5.2.3. Relación con Minikube Ingress

- **Minikube Ingress**: Complemento básico para desarrollo simple
- **Traefik**: Solución completa con más características y control
- **No son excluyentes**: Pueden coexistir pero se recomienda usar solo Traefik

**Recomendación**: Deshabilitar el Ingress de Minikube si usas Traefik:
```bash
minikube addons disable ingress
```

## 6. Verificación del Entorno Minikube

Antes de desplegar una aplicación, verifica:

1. Que estás en el contexto correcto de Kubernetes:
   ```bash
   kubectl config current-context
   ```

2. Que tienes los permisos necesarios:
   ```bash
   kubectl auth can-i create deployments --all-namespaces
   ```

3. Que los namespaces requeridos existen:
   ```bash
   kubectl get namespaces
   ```


## 7. Comandos Útiles de Minikube

```bash
# Detener Minikube
minikube stop

# Eliminar el clúster actual
minikube delete

# Ver la IP de Minikube
minikube ip

# Abrir el servicio en el navegador
minikube service <nombre-del-servicio>

# Ver recursos del sistema
minikube dashboard

# Ver logs de Minikube
minikube logs
```

## 8. Solución de Problemas Comunes

### 8.1. Problemas de Recursos

Si Minikube se comporta lentamente o falla:
```bash
# Liberar recursos
minikube stop
minikube delete

# Iniciar con más recursos
minikube start --cpus=4 --memory=8192mb --disk-size=40g
```

### 8.2. Problemas de Red

Si tienes problemas de conectividad:
```bash
# Reiniciar la red de Minikube
minikube stop
minikube delete
minikube start --cni=bridge
```

### 8.3. Limpiar recursos no utilizados
```bash
kubectl delete all --all --all-namespaces
kubectl delete pvc --all
kubectl delete pv --all
```

## 9. Documentación Adicional

- [Documentación oficial de Minikube](https://minikube.sigs.k8s.io/docs/)
- [Guía de inicio rápido de Minikube](https://kubernetes.io/docs/tasks/tools/#minikube)
- [Helm en Minikube](https://helm.sh/docs/intro/quickstart/)
- [Solución de problemas de Minikube](https://minikube.sigs.k8s.io/docs/handbook/troubleshooting/)

Si encuentras problemas:

1. Verifica los eventos del namespace:
   ```bash
   kubectl get events -n mi-namespace
   ```

2. Revisa los logs de los pods:
   ```bash
   kubectl logs -n mi-namespace nombre-del-pod
   ```

3. Verifica el estado de los recursos:
   ```bash
   kubectl get all -n mi-namespace
   ```

## 9. Documentación Adicional

- [Documentación oficial de Kubernetes](https://kubernetes.io/docs/home/)
- [Documentación de Helm](https://helm.sh/docs/)
- [Kubernetes Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Helm Best Practices](https://helm.sh/docs/chart_best_practices/)