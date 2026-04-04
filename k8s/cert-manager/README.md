# cert-manager

## Objetivo

Instalar **cert-manager** en el clúster Kubernetes para gestionar certificados TLS (Issuer/ClusterIssuer) y habilitar integraciones como webhooks con certificados (por ejemplo, operadores que lo requieran).

---

## ¿Qué son los CRDs?

**CRD** significa **Custom Resource Definition**. Son extensiones de Kubernetes que permiten añadir **nuevos tipos de objetos** (recursos) al clúster, además de los nativos como `Pod`, `Service`, `Deployment`, etc.

### ¿Por qué cert-manager necesita CRDs?

cert-manager introduce sus propios recursos para gestionar certificados TLS:

- `Certificate`: representa un certificado TLS para un servicio específico.
- `Issuer`: emisor de certificados con ámbito de namespace.
- `ClusterIssuer`: emisor de certificados con ámbito de clúster (global).

Sin los CRDs, Kubernetes no entendería estos nuevos objetos. Por eso al instalar cert-manager (ya sea por Helm o manifiesto) se deben instalar primero los CRDs.

### Más información
- https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/

---

## Instalación vía Helm

### Requisitos previos

- Kubernetes operativo (por ejemplo Minikube)
- `kubectl` configurado contra el clúster
- `helm` instalado

> El namespace `cert-manager` ya está declarado en `k8s/config/namespace.yaml`.

### 1) Agregar el repositorio de Helm

```bash
helm repo add jetstack https://charts.jetstack.io
helm repo update
```


### 2) Instalar cert-manager

```bash
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set crds.enabled=true \
  --labels part-of=microservices-architecture
  
# Actualizar la etiqueta  
kubectl label namespace cert-manager part-of=microservices-architecture --overwrite
```

#### Notas

In order to begin issuing certificates, you will need to set up a ClusterIssuer
or Issuer resource (for example, by creating a 'letsencrypt-staging' issuer).

More information on the different types of issuers and how to configure them
can be found in our documentation:

https://cert-manager.io/docs/configuration/

For information on how to configure cert-manager to automatically provision
Certificates for Ingress resources, take a look at the `ingress-shim`
documentation:

https://cert-manager.io/docs/usage/ingress/

For information on how to configure cert-manager to automatically provision
Certificates for Gateway API resources, take a look at the `gateway resource`
documentation:

https://cert-manager.io/docs/usage/gateway/


- `--set crds.enabled=true` es importante para que se instalen los CRDs requeridos por cert-manager.
- Si ya existía una instalación previa, se recomienda:

```bash
helm upgrade --install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --labels part-of=microservices-architecture \
  --set crds.enabled=true
```

---

## Instalación alternativa: vía manifiesto (kubectl)

> Este método **no requiere Helm**. Se basa en el manifiesto oficial proporcionado por cert-manager.

### Descargar y aplicar el manifiesto oficial

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.18.1/cert-manager.yaml
```

#### Notas

- El archivo `cert-manager.yaml` descargado corresponde a la **documentación oficial**: https://cert-manager.io/docs/installation/kubectl/
- El namespace `cert-manager` es creado automáticamente por el manifiesto.
- Si ya existe una instalación previa, el comando `apply` actualizará los recursos existentes.

---

## Verificación

### Pods

```bash
kubectl get pods -n cert-manager
```

Deberías ver en `Running`:
- `cert-manager`
- `cert-manager-webhook`
- `cert-manager-cainjector`

### CRDs

```bash
kubectl get crds | grep cert-manager
```

---

## Documentación oficial

- **Instalación con Helm**: https://cert-manager.io/docs/installation/helm/
- **Instalación con kubectl (manifiesto)**: https://cert-manager.io/docs/installation/kubectl/
