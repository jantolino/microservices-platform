# Traefik – Punto de entrada de la plataforma

Traefik (chart `traefik` 39.0.8) es el único punto de entrada externo de la plataforma. Corre en el namespace `networking` y enruta HTTP hacia los componentes mediante `IngressRoute`.

## Ubicación

| Ruta | Contenido |
|---|---|
| `k8s/platform/components/networking/traefik/` | Chart, values y `traefik-network-policy.yaml` (red mínima de arranque) |
| `k8s/platform/config/traefik/` | ServiceMonitor y flujos de red hacia cada backend (`traefik-to-<componente>.yaml`) |

## Dominio

Todos los hosts siguen el patrón `<componente>.dev.lab` (wildcard `*.dev.lab`). Para uso local, añadir cada host a `/etc/hosts` apuntando a la IP externa del servicio (`minikube tunnel`).

| Componente | Host |
|---|---|
| Traefik (dashboard) | `traefik.dev.lab` |
| ArgoCD | `argocd.dev.lab` |

## Seguridad

- **Dashboard:** se publica con un `IngressRoute` hacia `api@internal`. `api.insecure` está desactivado, de modo que el puerto 8080 de la API no se sirve sin pasar por el router. El dashboard aún no tiene autenticación ni TLS (pendiente de la CA propia, tarea 1.5).
- **Red (zero-trust, Cilium):**
  - Entrada: solo puertos 8000 y 8443 desde `world`, `host` y `remote-node`.
  - Salida: API server (443, 6443, 8443) y, por cada backend, un archivo `traefik-to-<componente>.yaml` en `config/traefik/`.
  - DNS lo cubre `allow-dns` de Foundation.
- **Patrón por backend:** entrada en la política del backend (solo desde pods `app.kubernetes.io/name: traefik` de `networking`) + salida en `config/traefik/`. Sin etiquetas genéricas de opt-in.

## Flujos de red declarados

| Origen | Destino | Puerto | Archivo |
|---|---|---|---|
| externo | Traefik | 8000, 8443 | `traefik-network-policy.yaml` |
| Traefik | API server | 443, 6443, 8443 | `traefik-network-policy.yaml` |
| Traefik | `argocd-server` | 8080 | `config/traefik/traefik-to-argocd.yaml` |

Pendientes (se añaden con cada componente): Traefik → Jaeger 4318 (trazas OTLP), Prometheus → Traefik 9100 (métricas), Traefik → cada backend.

## Observabilidad

- **Logs de acceso:** JSON, nivel `DEBUG` (ajustar a `INFO` cuando la plataforma esté estable).
- **Métricas:** Prometheus, puerto 9100, con etiquetas por entrypoint. El `ServiceMonitor` está en `config/traefik/`.
- **Trazas:** OTLP HTTP hacia `http://jaeger.monitoring.svc.cluster.local:4318/v1/traces`.

## Gateway API

El proveedor `kubernetesGateway` está habilitado y el listener `web` acepta rutas de todos los namespaces (`namespacePolicy.from: All`).

## Operaciones

```bash
kubectl get pods -n networking -l app.kubernetes.io/name=traefik
kubectl logs -n networking -l app.kubernetes.io/name=traefik --tail=50 -f
kubectl get ingressroute -A
kubectl rollout restart deployment -n networking traefik
```

Acceso al dashboard sin DNS local:

```bash
kubectl port-forward -n networking deploy/traefik 8080:8080
# http://localhost:8080/dashboard/
```

## Validación pendiente (☸️)

- Etiquetas reales del pod: `kubectl get pods -n networking --show-labels`.
- Puertos de contenedor del chart (`web` 8000, `websecure` 8443, `traefik` 8080, `metrics` 9100).
- Que el tráfico del LoadBalancer llegue con una identidad cubierta (`world`/`host`/`remote-node`): `hubble observe -n networking --verdict DROPPED`.
