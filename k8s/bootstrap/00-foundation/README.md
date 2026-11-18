# Foundation — Base transversal de la plataforma

Capa base que `k8s/bootstrap.sh` aplica **antes** de Redis y ArgoCD. Contiene la gobernanza
de namespaces, el almacenamiento y las políticas de red clusterwide (Cilium).

## Inventario real

| Archivo | Contenido |
|---|---|
| `namespaces/{argocd,cert-manager,data-services,devops-tools,headlamp,microservices,monitoring,networking,repositories,security,secrets-manager}.yaml` | Por namespace: `Namespace` (label `part-of: microservices-platform`) + `ResourceQuota` + `LimitRange` + `NetworkPolicy default-deny-all` |
| `storage-class/local-default.yaml` | StorageClass local **marcada como default** (`storageclass.kubernetes.io/is-default-class: "true"`) |
| `storage-class/nfs.yaml` | StorageClass `nfs` para almacenamiento compartido (requiere provisioner NFS) |
| `clusterwide-dns-allowlist.yaml` | `CiliumClusterwideNetworkPolicy`: DNS L7 — solo se resuelve `*cluster.local` (anti-exfiltración); excepción `argocd-repo-server` en `bootstrap/02-argocd/` |
| `coredns-ingress-protection.yaml` | `CiliumClusterwideNetworkPolicy`: CoreDNS solo acepta DNS desde namespaces `part-of: microservices-platform`, probes del kubelet y métricas de Prometheus |

> Los values de los charts usan `storageClassName: ""` (heredan la default `local-default`).
> La StorageClass `standard` de Minikube **no se usa**.

## Requisito: Cilium

Las políticas (`CiliumNetworkPolicy` / `CiliumClusterwideNetworkPolicy`) y el filtrado DNS L7
exigen **Cilium** como CNI. Minikube debe crearse con:

```bash
minikube start --cni=cilium
kubectl -n kube-system get pods | grep cilium
```

## Instalación de Minikube (referencia rápida)

```powershell
# Windows
choco install minikube
minikube start --driver=hyperv --cni=cilium --cpus=4 --memory=8192
```

```bash
# Linux (WSL2/Ubuntu)
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
minikube start --driver=docker --cni=cilium --cpus=4 --memory=8192
```

Herramientas: `kubectl` (viene con Minikube), `helm` 4+, `kustomize` (para validar:
`kustomize build --enable-helm <carpeta>`).

## Verificación tras aplicar Foundation

```bash
kubectl get namespaces -l part-of=microservices-platform
kubectl get storageclass
kubectl get networkpolicies -A
```

## Documentación

- Plan de trabajo y decisiones: `tasks/ROADMAP.md`
- Hallazgos de la validación 18-11-2026: `tasks/ROADMAP-fixes.md`
- [Documentación de Minikube](https://minikube.sigs.k8s.io/docs/)