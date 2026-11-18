# ROADMAP — Plataforma de Microservicios sobre Kubernetes

Documento vivo de planificación. Se actualiza al cerrar cada tarea (`[ ]` → `[x]`).

- 🖥️ = se puede realizar sin cluster (edición + `kustomize build --enable-helm`).
- ☸️ = requiere cluster para validar.

> Los hallazgos de la validación 18-11-2026 se cierran en `tasks/ROADMAP-fixes.md` (F00-F23).

---

## Rol

**Arquitecto de Plataforma DevOps Senior y Experto en Kubernetes**, con capacidad para generar documentación técnica de nivel enterprise.

Este rol fue asignado por el usuario al inicio del proyecto y debe mantenerse activo en TODAS las sesiones. Si no recuerdas tu rol, revisa este archivo o pregúntale al usuario.

Como Arquitecto de Plataforma DevOps Senior, tu responsabilidad es:
- Diseñar y validar la arquitectura GitOps completa (App of Apps, Sync-Waves, Kustomize+Helm)
- Auditar la seguridad Zero Trust (Network Policies con Cilium, Vault+ESO, RBAC)
- Garantizar que ningún secreto se hardcodee en Git
- Validar que cada componente tenga: kustomization.yaml, values.yaml, NetworkPolicy mínima, ExternalSecrets donde corresponda
- Asegurar que la capa de configuración (`config/`) esté cableada a ArgoCD
- Revisar consistencia de nombres de servicio entre charts de Helm e IngressRoutes
- Ser incisivo en los detalles de implementación: cada YAML debe validarse con `kustomize build --enable-helm`
- Un cambio a la vez, validado antes de continuar

**Si se te pregunta tu rol, responder:** "Arquitecto de Plataforma DevOps Senior y Experto en Kubernetes, con capacidad para generar documentación técnica de nivel enterprise."

---

## Decisiones fijadas

| Tema | Decisión |
|---|---|
| Orquestación | GitOps con ArgoCD **App of Apps** (`root-app-of-apps.yaml`), `Application` estándar, `directory.recurse: true` |
| Orden | Lo define la anotación `sync-wave`; el prefijo numérico del archivo **debe coincidir** con su wave |
| Secretos | Vault (fuente única) + Vault Config Operator (config declarativa) + ESO (`ClusterSecretStore/vault-backend`). Ninguna credencial en Git |
| Excepciones de Vault | Claves de unseal (archivo local `k8s/.secrets/`, ignorado por Git), ArgoCD y Redis mientras sigan en bootstrap |
| TLS | Terminación en Traefik con **CA propia** (selfsigned → CA → `ClusterIssuer` tipo `ca`) |
| Dominios | `<componente>.dev.lab` (wildcard `*.dev.lab`). Reemplaza a `*.desarrollo` |
| Red | `default-deny-all` por namespace (Foundation) + políticas *allow* **aditivas**; nunca se sobreescribe el deny |
| Zero-trust | Arquitectura **autocontenida**. **Salida externa** permitida solo a `argocd-repo-server` hacia GitHub (repo de la plataforma) y los repos Helm de los componentes, por **FQDN explícito** y puerto 443. **Comunicación interna** permitida solo por flujo declarado (origen→destino→puerto), nunca abierta por namespace. Prohibido `0.0.0.0/0` y `namespaceSelector: {}` en cualquier regla |
| Apertura incremental | Mismo modelo que `default-deny-all`: todo cerrado por defecto; cada permiso (interno o externo) se concede **solo cuando un componente lo requiere**, con el alcance más estrecho posible (pod, puerto, FQDN) y queda documentado en el roadmap |
| CNI | **Cilium**: políticas DNS L7 (anti-exfiltración), `toFQDNs` (GitHub + repos Helm) y `toEntities`/`fromEntities` (`kube-apiserver`, `host`) para eliminar `ipBlock` abiertos. Portable a otras distribuciones |
| Bootstrap | `bootstrap.sh`: Foundation → Redis → ArgoCD → root app → init/unseal/seed de Vault |
| Redis / ArgoCD | Permanecen en `bootstrap/` hasta validar la plataforma |
| Instancias de Redis | **Redis de plataforma** (`data-services`): ArgoCD y componentes de plataforma que lo requieran (p. ej. GitLab). **Redis de aplicaciones** (instancia separada, mismo operador): microservicios. Un compromiso en `apps/` no debe alcanzar el Redis de ArgoCD |
| Integraciones de red | Patrón por consumidor: entrada en `config/<servicio>/<servicio>-from-<consumidor>.yaml` + salida en la política del consumidor. Selección por namespace + app concretos; **prohibidas labels genéricas de opt-in** (`redis-client: "true"`, …) |
| `apps/` | Fuera de alcance hasta que la plataforma esté operativa |
| Portabilidad | Debe funcionar en Minikube y en otras distribuciones de Kubernetes |
| Helm local | **Helm 4+** (usado por `bootstrap.sh`). ArgoCD usa su Helm embebido, independiente del local |
| Agrupación de `config/` | **Por componente** (`config/<x>/`), cada carpeta con su `kustomization.yaml`; un wrapper `config-<x>` por carpeta, creado solo cuando el componente (y sus CRDs dependientes, p. ej. Prometheus) estén desplegados |
| Método de trabajo | **Un cambio a la vez**, validado antes de pasar al siguiente |
| Despliegue gradual de la Fase 3 | Los wrappers de componentes se mantienen fuera de `03-platform-orchestration/` (carpeta temp, gestionada manualmente) y se mueven uno a uno cuando el anterior despliega sin errores. La migración `*.desarrollo` → `*.dev.lab` (1.4b) se hace dentro de cada componente al tocar su IngressRoute |
| Namespace de Vault/VCO | `secrets-manager` (no `security` ni `vault`) |

### Regla de ubicación

| Ubicación | Contenido |
|---|---|
| `k8s/platform/components/<x>/` | **Solo lo necesario para arrancar**: `kustomization.yaml`, values, ExternalSecret de credenciales de arranque, NetworkPolicy **mínima de arranque** (DNS, API server, dependencias duras) |
| `k8s/platform/config/<x>/` | **Configuración posterior**: IngressRoute, `Certificate`, NetworkPolicies de integración, ServiceMonitor, RBAC de acceso, ajustes de componentes de bootstrap |
| `docs/` | Toda la documentación (se eliminan los `README.md` de cada componente) |

> Excepción: configuración que **otros componentes necesitan para arrancar** (`vault-config`, `cluster-secret-store`) va en el bloque de núcleo, no al final.

### Orden objetivo de `03-platform-orchestration`

| Archivo | Wave | Tipo |
|---|---|---|
| `00-cert-manager` | 0 | Núcleo |
| `01-external-secrets` | 1 | Núcleo |
| `02-vault` | 2 | Núcleo |
| `03-vault-config-operator` | 3 | Núcleo |
| `04-vault-config` | 4 | Config de núcleo |
| `05-cluster-secret-store` | 5 | Config de núcleo |
| `06-traefik` | 6 | Componente |
| `07-prometheus-stack` | 7 | Componente |
| `08-postgres` | 8 | Componente |
| `09-rabbitmq` | 9 | Componente |
| `10-gitlab` | 10 | Componente |
| `11-artifactory` | 11 | Componente |
| `12-jenkins` | 12 | Componente |
| `13-sonarqube` | 13 | Componente |
| `14-elk` | 14 | Componente |
| `15-jaeger` | 15 | Componente |
| `16-trivy` | 16 | Componente |
| `17-headlamp` | 17 | Componente |
| `18-redis-insight` | 18 | Componente |
| `19-platform-tls` | 19 | Config posterior |
| `20+-config-<x>` | 20+ | Config posterior por componente (argocd, redis, vault, traefik, …) |

> Wave 19 = `19-platform-tls` (`config/platform-tls/`: CA raíz selfsigned + cert `*.dev.lab` + `TLSStore default`). Debe existir ANTES de que las rutas migren a `websecure` (1.42/F21, waves 20+).
> Wave 21 (`21-config-cert-manager`) queda SIN contenido TLS (gap documentado): conserva solo el ServiceMonitor de cert-manager.

---

## Fase 0 — Guía y estructura 🖥️

- [x] **0.1** Crear `tasks/ROADMAP.md`.
- [x] **0.2** Mover `task-2026-07-05-16-34.md` a `tasks/` como historial.
- [x] **0.3** Agrupación de `config/` **por componente**: `config/service-monitors/` dividido en `config/<x>/` (cada uno con `kustomization.yaml`); eliminado el wrapper `16-service-monitors.yaml`. `vault-config/` y `cluster-secret-stores/` se mantienen (config de núcleo).
- [x] **0.4** Renumerar wrappers de `03-platform-orchestration` y alinear `sync-wave` según la tabla objetivo. Núcleo (00–05, waves 0–5) y componentes (06–16, waves 6–16, recuperados de `bootstrap/temp/`) en `03-platform-orchestration/`. `17-headlamp` y `18-platform-tls` aún no existen.

## Fase 1 — Base transversal

### Red
- [ ] **1.1** ☸️ Verificar CNI: `kubectl get pods -n kube-system -o wide | grep -Ei 'calico|cilium|kindnet'`. Si no es Cilium, recrear Minikube con `--cni=cilium`. Validar label `k8s-app: kube-dns` de CoreDNS.
- [x] **1.2** 🖥️ Añadir política común `allow-dns` por namespace en Foundation. Egress UDP/TCP 53 hacia `kube-system`/`k8s-app: kube-dns` en los 11 namespaces. Eliminadas las reglas DNS con `namespaceSelector: {}` de `vault-network-policy.yaml` y `operator-network-policy.yaml` (permitían puerto 53 hacia cualquier pod del cluster). → **superado por F14** (DNS L7 clusterwide; los `allow-dns` L3/L4 se eliminaron).
- [x] **1.2b** 🖥️ Anti-exfiltración DNS (requiere 1.1): `CiliumClusterwideNetworkPolicy` con regla DNS L7 que solo permita resolver `*.cluster.local`; excepción solo para `argocd-repo-server`: GitHub y dominios de los repos Helm (misma lista que 1.2f). **Debe reemplazar** a los `allow-dns` L3/L4 de Foundation: en Cilium, una regla L4 sin L7 sobre el mismo puerto anula el filtrado DNS L7 de otra política (hoy afecta a la regla DNS de `argocd-repo-server`). → **resuelto por F14** en ROADMAP-fixes.md.
- [x] **1.2c** 🖥️ Proteger CoreDNS: ingress en `kube-system` solo UDP/TCP 53 desde namespaces `part-of: microservices-platform`. → **resuelto por F15** en ROADMAP-fixes.md.
- [x] **1.2d** 🖥️ Eliminar todo `ipBlock 0.0.0.0/0`: vault y VCO convertidos a `CiliumNetworkPolicy` — API server → `toEntities: [kube-apiserver]` (443/6443/8443, incluido el API server de Minikube); probes/webhooks → `fromEntities: [host, kube-apiserver]` (VCO 8081/9443).
- [ ] **1.2e** ⏸️ **Pendiente — se decide con la plataforma activa** (apertura incremental). Mientras tanto no se concede ninguna salida adicional. Dudas abiertas:
  - [ ] **Artifactory:** ¿upstreams externos (Maven, npm, …) para descargar librerías? Si sí, `toFQDNs` por repo; si no, solo repo interno. `artifactory-network-policy.yaml` ya no usa `0.0.0.0/0` (corregido en 3.6): sin salida externa hasta decidirlo.
  - [ ] **Trivy:** ¿salida a `ghcr.io`/`mirror.gcr.io` para la DB de vulnerabilidades, o DB precargada/offline? `trivy-network-policy.yaml` ya no usa `0.0.0.0/0` (corregido en 3.11): sin salida hasta decidirlo.
  - [ ] **Imágenes de contenedor:** las descarga el nodo (fuera del alcance de NetworkPolicy). ¿Artifactory como mirror de registros + restricción de egress del nodo?
- [~] **1.2f** 🖥️ Implementada en `bootstrap/02-argocd/argocd-network-policy.yaml` con **hosts exactos** (sin `*.github.io`, que abriría cualquier GitHub Pages). Pendiente ☸️ validación con Hubble. Allowlist FQDN de salida para `argocd-repo-server` (TCP 443, `CiliumNetworkPolicy` `toFQDNs`): `github.com`, `<x>.github.io` (traefik, kubernetes-sigs, sonarsource, aquasecurity, redhat-cop, jaegertracing, prometheus-community), `charts.jetstack.io`, `charts.jfrog.io`, `charts.gitlab.io`, `charts.jenkins.io`, `charts.bitnami.com`, `helm.runix.net`, `charts.external-secrets.io`, `helm.elastic.co`, `helm.releases.hashicorp.com`. ☸️ Validar con Hubble (`hubble observe --verdict DROPPED`) los hosts de redirección/descarga (p. ej. `objects.githubusercontent.com`, CDNs de los repos) y añadir solo los necesarios. Mantener la lista sincronizada con los `helmCharts.repo` de `components/`.
- [x] **1.3** 🖥️ `CiliumNetworkPolicy` en `bootstrap/01-redis/redis-network-policy.yaml` y `bootstrap/02-argocd/argocd-network-policy.yaml`, aplicadas por `bootstrap.sh` antes de instalar Redis/ArgoCD (ahora valida que exista Cilium). Selector de Redis corregido a las labels estables del operador (`app: redis`, `redis_setup_type: standalone`). Eliminada `allow-argocd-to-redis` de Foundation (abría 6379 a todo el namespace `argocd`). ☸️ Validar labels reales (`kubectl get pods --show-labels`) y flujos con Hubble. Alcance: intra-namespace `argocd` (server↔repo-server↔application-controller), controller/server → `kube-apiserver` (`toEntities`), ArgoCD → Redis (`data-services`, 6379), repo-server → allowlist 1.2f. Ninguna otra salida.
- [ ] **1.3b** 🖥️ Red mínima de arranque del **núcleo** (waves 0–5) como `CiliumNetworkPolicy` en `components/<x>/`: **cert-manager** (sin ninguna política hoy: controller/cainjector → API server, webhook ← API server), **external-secrets** (hoy sin API server ni webhook; `cert-controller`/`webhook` sin cubrir), **vault** y **VCO** (absorbe 1.2d). Sin esto, con `default-deny-all` activo la wave 0 no completa.

### TLS
- [x] **1.4** 🖥️ Dominio definido: `<componente>.dev.lab` (wildcard `*.dev.lab` de dos niveles, válido). Descartados `*.desarrollo` y `*.dev-lab` (wildcard sobre etiqueta única).
- [x] **1.4b** 🖥️ Migrar hosts `*.desarrollo` → `*.dev.lab`: `argocd-ingress-route.yaml`, `vault-ingress-route.yaml` (movido a `config/vault/`), README de Vault y `k8s/README.md`. Residual cosmético: comentarios y `admin@desarrollo.edu` en `pgadmin-values.yaml`.
- [x] **1.5** 🖥️ Crear CA propia: `config/cert-manager/platform-ca.yaml` (selfsigned → `Certificate` raíz `isCA: true` → `ClusterIssuer` `platform-ca-issuer`) y `config/traefik/platform-tls.yaml` (certificado `*.dev.lab` + `TLSStore default` de Traefik). Migración de IngressRoutes a `websecure`: 1.42.

### Secretos
- [x] **1.6** 🔄 🖥️ Ampliar `bootstrap.sh`: sembrar en Vault contraseñas aleatorias **solo si no existen**. → **resuelto por F13** (`vault_seed_kv`) en ROADMAP-fixes.md. **Lista completa de credenciales a sembrar** (identificadas en auditoría):
  | Componente | Vault Path | Key | Estado en values |
  |---|---|---|---|
  | Postgres | `secret/data/platform/databases/postgres` | `password` | Ya gestionado via ESO — **SE SIEMBRA (F13)** |
  | pgAdmin | `secret/data/platform/tools/pgadmin` | `password` | Ya gestionado via ESO — **SE SIEMBRA (F13)** |
  | GitLab Postgres | `secret/data/platform/databases/postgres` | `password` | Ya gestionado via ESO (`gitlab-postgres-es.yaml`) |
  | Grafana | `secret/data/platform/tools/grafana` | `admin-user`, `admin-password` | Ya gestionado via ESO (`grafana-es.yaml`) — falta seed |
  | Jenkins | `secret/data/platform/tools/jenkins` | `jenkins-admin-user`, `jenkins-admin-password` | Ya gestionado via ESO (`jenkins-es.yaml`) — falta seed |
  | SonarQube DB | `secret/data/platform/databases/sonarqube` | `password` | Ya gestionado via ESO (`sonarqube-es.yaml`) — falta seed |
  | SonarQube monitor | `secret/data/platform/tools/sonarqube` | `monitoringPasscode` | Ya gestionado via ESO (`sonarqube-es.yaml`) — falta seed |
  | Artifactory DB | `secret/data/platform/databases/artifactory` | `password` | Ya gestionado via ESO (`artifactory-es.yaml`) — falta seed |
  | Artifactory keys | `secret/data/platform/tools/artifactory` | `joinKey`, `masterKey`, `adminPassword` | Ya gestionado via ESO (`artifactory-es.yaml`) — falta seed |
  | ~~RabbitMQ~~ | — | — | **NO sembrar**: el operador genera `rabbitmq-default-user` (`rabbitmq-cluster.yaml` lín. 21-23) |
  | Jaeger ES | Referenciar secret `elasticsearch-master-elastic-password` | `elastic-password` | Resuelto via `secretKeyRef` (sin seed en Vault) |
  | ~~Redis~~ | — | — | **NO sembrar**: PushSecret ya la publica (`config/redis/redis-push-secret.yaml`) |
- [x] **1.7** 🖥️ Unificar ExternalSecrets a `external-secrets.io/v1` (p. ej. `postgres-es.yaml`, `pgadmin-es.yaml`). → **resuelto por F08** en ROADMAP-fixes.md (grep v1beta1 = 0).

### Observabilidad
- [x] **1.8** 🖥️ Regla de selección de ServiceMonitors: `serviceMonitorSelectorNilUsesHelmValues: false` o label `release: kube-prometheus-stack` uniforme. → **resuelto por F10** en ROADMAP-fixes.md.

### Almacenamiento
- [x] **1.9** 🔄 🖥️ Unificar StorageClass a `local-default`: annotation `storageclass.kubernetes.io/is-default-class: "true"` añadida a `local-default.yaml` y todos los `"standard"` → `""` (sonarqube, jenkins, prometheus-stack, rabbitmq).

### Higiene
- [x] **1.10** 🖥️ Eliminar credenciales expuestas del README de Vault (reemplazadas por placeholders). **Nota:** existen en el commit `c7a6819` publicado en `origin/develop`; ver 1.10b.
- [ ] **1.10b** ☸️ Confirmar que el Vault actual **no** usa esas claves (se reinicializa en cada bootstrap). Si las usa: `vault operator rekey` + `vault token revoke <root-token>`. Reescribir historial (`git filter-repo`) solo si se consideran vigentes.
- [x] **1.11** 🖥️ Eliminar `components/dashboard/` completo (Kubernetes Dashboard sin mantenimiento, reemplazado por Headlamp).
- [x] **1.12** 🖥️ Eliminar `components/argocd/`: IngressRoute y NetworkPolicy movidos a `config/argocd/`; ServiceMonitor duplicado borrado; README movido a `docs/components/argocd.md`.
- [x] **1.13** 🖥️ Eliminar `*-service-monitor.yaml` duplicados dentro de `components/`.

### Correcciones de auditoría quirúrgica (nuevas)

- [x] **1.20** 🆕 🖥️ **Corregir path en `04-vault-config.yaml`** (E2): línea 11 dice `path: k8s/platform/config/vault-config` → cambiar a `path: k8s/platform/config/vault-config-operator`. La carpeta real es `config/vault-config-operator/`. Sin esto, los CRDs del VCO nunca se aplican.
- [x] **1.21** 🆕 🖥️ **Corregir bug YAML en `kubernetes-auth-role.yaml`** (E3): `platform/config/vault-config-operator/kubernetes-auth-role.yaml` líneas 17-19 tienen `targetNamespaces: targetNamespaces:` (doble key anidada). Fix: eliminar key duplicada, usar `targetNamespaces: - secrets-manager`.
- [x] **1.22** 🆕 🖥️ **Cablear config layer a ArgoCD** (E1): 18 wrappers `20-config-argocd.yaml` a `37-config-headlamp.yaml` creados con waves 20–37 (uno por carpeta de `platform/config/`).
- [x] **1.23** 🆕 🖥️ **Eliminar duplicados huérfanos en `config/redis/`**: `redis-insight.yaml` (0 bytes) y `redis-insight-auth.yaml` (0 bytes) son duplicados no referenciados por el kustomization. Los archivos canónicos viven en `config/redis-insight/` con contenido completo y referenciados correctamente. Eliminar de `config/redis/`.
- [x] **1.24** 🆕 🖥️ **Corregir `postgres-service-monitor.yaml` huérfano** (E14): ServiceMonitor eliminado y creado `postgres-pod-monitor.yaml` (PodMonitor CNPG, selector `cnpg.io/cluster: postgres`, puerto `metrics`, 30s).
- [x] **1.25** 🆕 🖥️ **Mover `vault-ingress-route.yaml` a `config/vault/`** (E15): está en `components/secrets-manager/vault/` pero NO está en el kustomization. Por la regla de ubicación debe estar en `config/vault/`. También cambiar dominio `vault.desarrollo` → `vault.dev.lab` (línea 12). Agregar al kustomization de `config/vault/`.
- [x] **1.26** 🆕 🖥️ **Corregir IngressRoute service names** (E9, E10, B2, B3) — resueltos:
  - pgAdmin: `config/postgres/pgadmin-ingress-route.yaml` línea 15 `name: pgadmin-pgadmin4` → añadir `fullnameOverride: pgadmin` en `pgadmin-values.yaml` y usar `name: pgadmin`
  - Artifactory: `config/artifactory/artifactory-ingress-route.yaml` línea 15 `name: artifactory` → añadir `fullnameOverride: artifactory` en `artifactory-oss-values.yaml`
  - Kibana: `config/elk/kibana-ingress-route.yaml` línea 14 `name: kibana-kibana` → cambiar a `name: kibana`
  - SonarQube: `config/sonarqube/sonarqube-ingress-route.yaml` línea 14 `name: sonarqube-sonarqube` → cambiar a `name: sonarqube`
- [x] **1.27** 🆕 🖥️ **Corregir `redis-from-gitlab.yaml` labels** (E11): `config/redis/redis-from-gitlab.yaml` líneas 14-17 usan `app: webservice` y `app: sidekiq`. El chart de GitLab v9.11.2 usa `app: gitlab-webservice-default` y `app: gitlab-sidekiq-all`.
- [x] **1.28** 🆕 🖥️ **Crear GitLab Postgres ExternalSecret** (E12): `gitlab-values.yaml` líneas 34-36 referencian `secret: gitlab-postgres-password` pero ningún ExternalSecret lo crea. Crear `config/gitlab/gitlab-postgres-es.yaml` leyendo de `secret/data/platform/databases/postgres` (réplica distribuida). Agregar al kustomization.
- [x] **1.29** 🆕 🖥️ **Corregir Jaeger elastic password** (E5): `jaeger.extraEnv` + `valueFrom.secretKeyRef` (`elasticsearch-master-elastic-password`, key `elastic-password`) y `${env:ELASTIC_PASSWORD}` en la config v2. `esIndexCleaner` desactivado (el chart no soporta secretKeyRef para ese Job).
- [x] **1.30** 🆕 🖥️ **Corregir Kibana y Jaeger HTTPS→HTTP** (E6 corregido): ES tiene `xpack.security.http.ssl.enabled: false` → el esquema correcto es `http://`. Filebeat YA usa HTTP correctamente (NO tocar). Kibana (`kibana-values.yaml` líneas 17 y 28) y Jaeger (`jaeger-values.yaml` líneas 56, 65, 85) usan `https://` incorrectamente → cambiar a `http://`.
- [x] **1.31** 🆕 🖥️ **Corregir Trivy egress** (E7): `trivy-network-policy.yaml` convertido a CNP con egress a `kube-apiserver` (443/6443/8443) y DNS. Pendiente 1.2e para `toFQDNs` a `ghcr.io`.
- [x] **1.32** 🆕 🖥️ **Corregir RabbitMQ namespace** (B1): `rabbitmq-cluster.yaml` línea 5 `namespace: event-management` → `namespace: data-services`.
- [x] **1.33** 🆕 🖥️ **Aumentar ResourceQuota de `data-services`** (B4): subida a `requests.cpu: 1000m`, `requests.memory: 2.5Gi`, `limits.cpu: 2000m`, `limits.memory: 4Gi`.
- [x] **1.34** 🆕 🖥️ **Corregir Jaeger ServiceMonitor selector** (B5): selector `component: collector` eliminado y puerto corregido a `metrics` (8888).
- [x] **1.35** 🆕 🖥️ **Corregir Vault ServiceMonitor puerto** (B6): telemetry habilitada en `vault-values.yaml` y puerto del ServiceMonitor corregido a `http` (8200, `/v1/sys/metrics`).
- [x] **1.36** 🆕 🖥️ **Eliminar ServiceMonitors inline duplicados** — todos aplicados:
  - `jenkins-values.yaml` líneas 152-153: `serviceMonitor.enabled: true` → `false`
  - `trivy-values.yaml` líneas 33-34: `serviceMonitor.enabled: true` → `false`
  - `gitlab-values.yaml` líneas 44-45: `serviceMonitor.enabled: true` → `false`
  - `postgresql-values.yaml` líneas 46-47: `podMonitor.enabled: true` → `false`
  - `jaeger-values.yaml` líneas 92-112: eliminar bloque `extraObjects` con ServiceMonitor inline
- [x] **1.37** 🆕 🖥️ **Agregar `CreateNamespace=true` a `18-redis-insight.yaml`**: solo tiene `ServerSideApply=true`, inconsistente con los otros 18 wrappers.
- [x] **1.38** 🆕 🖥️ **Corregir Vault README**: múltiples líneas (22, 36, 52-53, 62-63, 72, 133, 166, 178, 200-203, 209, 216, 221, 232, 234) usan namespace `security` o `vault` → cambiar a `secrets-manager`. Línea 185 `vault.desarrollo` → `vault.dev.lab`. → **resuelto por F17** en ROADMAP-fixes.md.
- [x] **1.39** 🆕 🖥️ **Corregir stale comment en `traefik-to-redis-insight.yaml`** (B10): línea 3 referencia `config/redis/redis-insight-network-policy.yaml` → el archivo real está en `config/redis-insight/redis-insight-network-policy.yaml`.
- [x] **1.40** 🆕 🖥️ **Corregir comentario en `secrets-manager.yaml`** (B9): comentario dice `'vault'` → debe decir `'secrets-manager'`. → **resuelto por F18** en ROADMAP-fixes.md.
- [x] **1.41** 🆕 🖥️ **Reescribir Foundation README** (B8): referencia archivos inexistentes (`namespace.yaml`, `persistent-volumes.yaml`, `storage-class.yaml`, `network-policies.yaml`). → **resuelto por F19** en ROADMAP-fixes.md.
- [x] **1.42** 🆕 🖥️ **Migrar IngressRoutes a `websecure` + TLS**: todos los `*-ingress-route.yaml` usan `entryPoints: [web]`. Con `TLSStore default` desplegado (1.5), cambiar cada IngressRoute a `entryPoints: [websecure]` + `tls: {}` por componente. → **resuelto por F20+F21** en ROADMAP-fixes.md (TLS agrupado en wave 19; 16 rutas migradas).

## Fase 2 — Revalidar núcleo con red real ☸️

- [ ] **2.1** Uninstall + bootstrap limpio con los scripts nuevos.
- [ ] **2.2** Confirmar cert-manager, ESO, Vault, VCO, `vault-config` y `cluster-secret-store` Healthy **sin pasos manuales** y con NetworkPolicies aplicándose.
- [ ] **2.3** Completar `config/argocd/` (IngressRoute, NetworkPolicy completa: DNS, API server, intra-namespace, Redis, Git) y `config/redis/`. Wrappers `20-config-argocd` y `28-config-redis` ya creados (1.22).

## Fase 3 — Componentes (uno a la vez)

### Checklist estándar por componente
1. Depurar `components/<x>`: solo chart, values, ExternalSecret de arranque y NetworkPolicy mínima.
2. Fijar versiones de chart e imágenes (sin `latest`/`lts`).
3. Verificar recursos contra la ResourceQuota del namespace.
4. Sembrar sus secretos en Vault.
5. Crear wrapper `NN-<x>.yaml` con wave alineada (`SkipDryRunOnMissingResource=true` si incluye CRDs + CRs).
6. Completar `config/<x>/` (IngressRoute + TLS, NetworkPolicies de integración; el ServiceMonitor ya está) y crear su wrapper `config-<x>`.
7. Mover el README a `docs/components/<x>.md`.
8. 🖥️ `kustomize build --enable-helm` → ☸️ desplegar y verificar en ArgoCD.

### Componentes

- [~] **3.1 traefik** — base de IngressRoutes; desbloquea los ingress del resto. 🖥️ Hecho: dominio `traefik.dev.lab`, `api.insecure` eliminado, `components/`: `traefik-network-policy.yaml` (CNP mínima: solo salida a API server); `config/traefik/`: `traefik-ingress-from-world.yaml` (entrada 8000/8443 desde `world`/`host`/`remote-node`) y `traefik-to-argocd.yaml`; README → `docs/components/traefik.md`.
  - [ ] ☸️ Confirmar que Traefik arranca solo con el egress al API server; si no, ampliar solo lo necesario.
  - [ ] ☸️ `kustomize build --enable-helm` y validar labels/puertos reales del chart y la identidad del tráfico del LoadBalancer con Hubble.
  - [ ] Cada nuevo `IngressRoute` añade su `config/traefik/traefik-to-<componente>.yaml`; pendientes: Jaeger 4318 (trazas) y Prometheus → Traefik 9100.
  - [~] Dashboard con BasicAuth (ESO `Password` generator + `Middleware`); TLS pendiente (1.42); logs de acceso en `DEBUG` (bajar a `INFO`).
  - [x] Wrapper `config-traefik`.
- [~] **3.2 prometheus-stack** — instala CRDs de ServiceMonitor; habilita los wrappers `config-<x>` de cert-manager, external-secrets y vault. Credencial de Grafana ya via ESO (`grafana-es.yaml` + `existingSecret`); falta seed en Vault (ver 1.6).
  - [x] 🖥️ `components/` solo con kustomization y values (`releaseName: prometheus`); 3 IngressRoutes y política de scrape preexistente en `config/prometheus-stack/` (hosts `*.dev.lab`); README → `docs/components/prometheus-stack.md`.
  - [ ] ☸️ Validar nombres de servicio de las IngressRoutes con el release `prometheus`.
  - [x] Wrapper `config-prometheus-stack`.
- [x] **3.3 postgres + pgadmin** — primer consumidor real de ESO; crear BBDD/usuarios para gitlab, sonarqube y artifactory (init script o Job). → **resuelto por F11** en ROADMAP-fixes.md (app-roles-es + Job idempotente).
  - [x] 🖥️ `postgres-network-policy.yaml` y `pgadmin-ingress-route.yaml` (host `pgadmin.dev.lab`) movidos a `config/postgres/` y referenciados; README → `docs/components/postgres.md`.
  - [ ] La política usa NetworkPolicy estándar con selectores sin validar (`# Ajusta según tu chart`) y sin regla para Prometheus ni egress; revisar al validar.
  - [x] **IngressRoute service name mismatch** (E9): `pgadmin-ingress-route.yaml` línea 15 usa `name: pgadmin-pgadmin4` pero el chart crea `pgadmin4-v1-62-0`. Fix: `fullnameOverride: pgadmin` en values (ver 1.26).
  - [x] **ServiceMonitor huérfano** (E14): `postgres-service-monitor.yaml` no está en kustomization y además es tipo incorrecto (ver 1.24).
  - [x] Wrapper `config-postgres`.
- [~] **3.4 rabbitmq** — operador + `RabbitmqCluster` en la misma app (`SkipDryRunOnMissingResource`).
  - [x] 🖥️ `rabbitmq-network-policy.yaml` y `rabbitmq-ingress-route.yaml` (host `rabbitmq.dev.lab`) movidos a `config/rabbitmq/` y referenciados; README → `docs/components/rabbitmq.md`.
  - [x] 🖥️ En `config/rabbitmq/`: eliminada la apertura a todo `microservices`; ingress desde Prometheus (15692) y desde el operador (15672); `rabbitmq-operator-network-policy.yaml` (CNP: operador → API server y → RabbitMQ 15672). Egress de Prometheus a RabbitMQ añadido en `config/prometheus-stack/prometheus-scrape-network-policy.yaml`.
  - [x] **Namespace incorrecto en `rabbitmq-cluster.yaml`** (B1): línea 5 dice `namespace: event-management` → `data-services` (ver 1.32).
  - [x] **Credenciales eliminadas** (E4): `default_user`/`default_pass` quitados del `additionalConfig`; el operador genera credenciales aleatorias (ver 1.6 para seed en Vault).
  - [x] **StorageClass corregido** (1.9): `storageClassName: ""`.
  - [ ] ☸️ Validar labels y puertos reales (`app.kubernetes.io/name: rabbitmq`, 15692, 15672) y si los pods de RabbitMQ necesitan egress (peer discovery contra el API server) bajo `default-deny-all`.
  - [ ] Acceso de microservicios (5672): se concede cuando exista el primer consumidor, por pod concreto.
  - [x] Wrapper `config-rabbitmq`.
- [~] **3.5 gitlab** — postgres + redis externos; desactivar cert-manager embebido; root password a Vault.
  - [x] 🖥️ `gitlab-network-policy.yaml` y `gitlab-ingress-route.yaml` movidos a `config/gitlab/` y referenciados; IngressRoute: host `gitlab.dev.lab`, namespace `repositories`; `global.hosts.domain: dev.lab` en values; README → `docs/components/gitlab.md`.
  - [x] Values: Redis y Postgres apuntan a `data-services`.
  - [x] **ExternalSecret de Postgres creado** (E12): `config/gitlab/gitlab-postgres-es.yaml` lee de `secret/data/platform/databases/postgres` (ver 1.28).
  - [x] **ServiceMonitor inline eliminado** (1.36): `gitlab-values.yaml` `serviceMonitor.enabled: false`.
  - [ ] La política mezcla ingress de ArgoCD y egress a Redis/Postgres con selectores sin validar (`# Ajusta la etiqueta`); revisar al validar.
  - [x] Wrapper `config-gitlab`.
- [~] **3.6 artifactory** — postgres; join key y master key desde Vault.
  - [x] 🖥️ `artifactory-network-policy.yaml` y `artifactory-ingress-route.yaml` (host `artifactory.dev.lab`) movidos a `config/artifactory/` y referenciados; README → `docs/components/artifactory.md`.
  - [x] 🖥️ Zero-trust: eliminado el egress `0.0.0.0/0:443`; la política solo permite salida a Postgres (`data-services`, 5432).
  - [x] **Credenciales a Vault/ESO**: `artifactory-es.yaml` (DB password, master key, join key, admin password) + `database.secrets`/`masterKeySecretName`/`joinKeySecretName`/`admin.secret` en values.
  - [x] **IngressRoute service name mismatch** (E10): `fullnameOverride: artifactory` en values (ver 1.26).
  - [ ] Upstreams externos (Maven, npm, …): cerrados hasta decidir (1.2e).
  - [x] Wrapper `config-artifactory`.
- [~] **3.7 jenkins** — eliminar `admin123`, imágenes `lts`/`latest` y plugins `:latest`; StorageClass `standard` → unificada.
  - [x] 🖥️ `jenkins-network-policy.yaml` y `jenkins-ingress-route.yaml` (host `jenkins.dev.lab`) movidos a `config/jenkins/` y referenciados; `jenkinsUrl` → `http://jenkins.dev.lab`; README → `docs/components/jenkins.md`. Egress de Prometheus a Jenkins (8080) añadido.
  - [x] **Credencial a Vault/ESO**: `jenkins-es.yaml` + `existingSecret`/`userKey`/`passwordKey` en values.
  - [x] **StorageClass corregido** (1.9): `storageClass: ""`.
  - [x] **ServiceMonitor inline eliminado** (1.36): `jenkins-values.yaml`.
  - [x] Wrapper `config-jenkins`.
- [~] **3.8 sonarqube** — postgres; `monitoringPasscode` desde Vault.
  - [x] 🖥️ `sonarqube-network-policy.yaml` y `sonarqube-ingress-route.yaml` (host `sonarqube.dev.lab`) movidos a `config/sonarqube/` y referenciados; JDBC → `data-services`; README → `docs/components/sonarqube.md`. Egress de Prometheus a SonarQube (9000) añadido.
  - [x] **Credenciales a Vault/ESO**: `sonarqube-es.yaml` (JDBC + monitoring passcode) + `monitoringPasscodeSecretName`/`jdbcSecretName`/`jdbcSecretPasswordKey` en values.
  - [x] **StorageClass corregido** (1.9): `storageClass: ""`.
  - [x] **IngressRoute service name mismatch** (B3): `name: sonarqube` (ver 1.26).
  - [x] Wrapper `config-sonarqube`.
- [~] **3.9 elk** — Elasticsearch, Kibana, Filebeat. → red/labels/monitores **resueltos por F12** en ROADMAP-fixes.md; pendiente F12b (exporter de métricas de ES, decisión abierta).
  - [x] 🖥️ `elk-network-policy.yaml` y `kibana-ingress-route.yaml` (host `kibana.dev.lab`) movidos a `config/elk/` y referenciados; README → `docs/components/elk.md`.
  - [x] 🖥️ Red completada en `config/elk/`.
  - [x] **Kibana HTTPS corregido** (E6): `kibana-values.yaml` usa `http://` (ES tiene `http.ssl.enabled: false`). **Filebeat YA usa HTTP correctamente — NO tocar.**
  - [x] **Kibana IngressRoute service name mismatch** (B2): `name: kibana` (ver 1.26).
  - [ ] ☸️ Validar labels reales de pods (el chart de Elastic usa `app: <nombre>`; las políticas usan `app.kubernetes.io/name`) y puertos (5066 de Filebeat sin Service). → 🖥️ labels/puertos corregidos por F12; queda la validación ☸️.
  - [ ] ☸️ Elasticsearch no expone `_prometheus/metrics` sin plugin/exporter; revisar.
  - [x] Filebeat necesita salida al API server (metadatos de Kubernetes): regla CNP pendiente. → **resuelto por F12** (egress kube-apiserver 443/6443/8443 en filebeat-zero-trust).
  - [x] Wrapper `config-elk`.
- [~] **3.10 jaeger** — depende de Elasticsearch.
  - [x] 🖥️ `jaeger-ingress-route.yaml` (host `jaeger.dev.lab`) movido a `config/jaeger/`; README → `docs/components/jaeger.md`.
  - [x] 🖥️ Zero-trust: `jaeger-network-policy.yaml` (ingress Traefik 16686 y Prometheus 8888; egress solo a Elasticsearch 9200).
  - [x] **Credencial elastic a secretKeyRef** (E5): `${env:ELASTIC_PASSWORD}` desde `elasticsearch-master-elastic-password` (ver 1.29).
  - [x] **HTTPS corregido** (E6): `server_urls` usa `http://` (ver 1.30).
  - [x] **ServiceMonitor duplicado eliminado** (1.36): bloque `extraObjects` quitado de values.
  - [x] **ServiceMonitor selector corregido** (B5): `component: collector` eliminado, puerto `metrics` (8888) (ver 1.34).
  - [ ] Recepción de trazas OTLP (4317/4318) cerrada hasta que exista el primer emisor.
  - [x] Wrapper `config-jaeger`.
- [~] **3.11 trivy**
  - [x] 🖥️ `trivy-network-policy.yaml` movido a `config/trivy/` y referenciado; README → `docs/components/trivy.md`. Zero-trust: CNP con egress solo a `kube-apiserver` y DNS.
  - [x] **Egress corregido** (E7): `toEntities: [kube-apiserver]` (443/6443/8443) y DNS (ver 1.31). Pendiente 1.2e para FQDNs.
  - [x] **ServiceMonitor inline eliminado** (1.36): `trivy-values.yaml`.
  - [ ] ☸️ Validar que el chart `trivy` 0.32.1 sea el correcto para los values (`operator.*`): el operador suele ir en el chart `trivy-operator`.
  - [x] Wrapper `config-trivy`.
- [ ] **3.12 headlamp**
  - [x] Namespace en Foundation + `bootstrap-uninstall.sh`.
  - [x] `components/headlamp/`: chart + values.
  - [x] `config/headlamp/`: SA `headlamp-viewer` + ClusterRole `view`.
  - [x] 🖥️ NetworkPolicies completas.
  - [ ] ☸️ Validar label y puerto del pod (`app.kubernetes.io/name: headlamp`, 4466) y añadir TLS tras 1.5.
  - [x] Wrapper `17-headlamp.yaml` (wave 17).
  - [x] Wrapper `config-headlamp`.

## Fase 4 — Migraciones y configuración posterior

- [~] **4.1** Endurecer Redis. **Decisión:** Redis se queda en `bootstrap/01-redis/`. Seguridad y entrada se añaden desde `config/redis/`.
  - [x] 🖥️ Autenticación: `bootstrap.sh` genera `redis-auth` y `argocd-redis-auth`; `config/redis/redis-push-secret.yaml` publica en Vault; GitLab la lee con `config/gitlab/gitlab-redis-es.yaml`.
  - [x] 🖥️ Redis Insight como componente + `config/redis-insight/` completa.
  - [x] 🖥️ Consumidores: `config/redis/redis-from-gitlab.yaml` y `config/redis-insight/redis-from-insight.yaml`.
  - [x] **Labels corregidos** (E11): `app: gitlab-webservice-default` / `app: gitlab-sidekiq-all` (ver 1.27).
  - [x] **Duplicados huérfanos eliminados** (1.23): `redis-insight.yaml` y `redis-insight-auth.yaml` borrados de `config/redis/`.
  - [x] Wrapper `config-redis-insight`.
  - [x] Wrapper `config-redis`.
- [ ] **4.2** Reinicio ante cambio de secretos (Stakater Reloader o equivalente).
- [ ] **4.3** Actualizar `bootstrap-uninstall.sh` con nuevos namespaces, CRDs y PVs.
- [ ] **4.4** Login humano a Vault (userpass / OIDC con GitLab como IdP).
- [ ] **4.5** Backups: Velero + MinIO como componentes; snapshots de Vault Raft.
- [ ] **4.6** TLS interno — migración por componente.
- [ ] ~~**4.7** Auto-unseal~~ — **Descartado para local**.

## Fase 5 — Documentación (`docs/`)

```
docs/
├── architecture/   overview, gitops-flow, secrets-flow, network, tls
├── components/     un .md por componente
├── operations/     bootstrap, access, troubleshooting
└── images/         (migrar /screenshots)
```

- [ ] **5.1** Crear estructura `docs/`.
- [ ] **5.2** Reescribir `k8s/README.md` como resumen de arquitectura general que enlaza a `docs/`.
- [ ] **5.3** Plantilla por componente: propósito, chart/versión, namespace, wave, dependencias, tabla de comunicación (in/out, puertos), diagrama Mermaid, secretos (path Vault), TLS, NetworkPolicies, métricas, URL, troubleshooting.
- [ ] **5.4** Eliminar `README.md` dentro de `components/*/`.
- [ ] **5.5** Migrar `/screenshots` a `docs/images/`.

---

## Lecciones aprendidas

| Tema | Lección |
|---|---|
| ArgoCD | `Healthy` en una Application solo indica que los recursos existen en K8s, **no** que un operador (p. ej. VCO) los haya reconciliado. Revisar `status.conditions` de las CRs |
| VCO | Sin `VAULT_ADDR` el operador intenta `127.0.0.1:8200` (`connection refused`) |
| Vault auth | `403 permission denied` en `auth/kubernetes/login` = la petición llega pero falta auth method/role (seed) |
| Red | Una NetworkPolicy que bloquea produce **timeout**, no `connection refused` |
| Red | NetworkPolicies son aditivas; el CNI por defecto de Minikube puede no aplicarlas |
| Waves | Un componente bloqueado en `Progressing` detiene las waves siguientes; su config posterior nunca llegará |
| Jenkins | Init container `cp: overwrite`: usar `cp -n` |
| GitLab | Conflicto de ownership de CRDs de cert-manager: desactivar cert-manager embebido |
| Artifactory | `Join key missing`: proveer join key / master key por Secret |
| SonarQube | Requiere `monitoringPasscode` |
| Traefik | En reglas gRPC usar `Header` (singular), no `Headers` |
| Auditoría | Filebeat usa HTTP correctamente porque ES tiene `http.ssl.enabled: false`. Kibana y Jaeger son los que usan HTTPS incorrectamente. No invertir el fix |
| Auditoría | `config/redis/redis-insight-*.yaml` (0 bytes) son duplicados huérfanos de `config/redis-insight/`. Eliminar, no llenar |
| Auditoría | `bootstrap.sh` EXISTE en el repo. No crear desde cero, ampliar |
| Auditoría | Path `config/vault-config` en `04-vault-config.yaml` es incorrecto; la carpeta real es `config/vault-config-operator` |
| Auditoría | `kubernetes-auth-role.yaml` tiene `targetNamespaces: targetNamespaces:` (doble key) — YAML inválido |
| Auditoría | IngressRoute service names deben validarse contra el releaseName del chart: pgAdmin=`pgadmin4-v1-62-0`, Artifactory=`artifactory-oss`, Kibana=`kibana`, SonarQube=`sonarqube` |
| Auditoría | ResourceQuota de data-services (900m CPU) es insuficiente para los workloads (~1950m). Pods rechazados |
| Auditoría | Vault/VCO viven en namespace `secrets-manager`, no `security` ni `vault`. Todos los README y YAMLs deben reflejarlo |

## Notas abiertas

- Validar con fuentes oficiales: nombres exactos de CRDs del VCO para auth methods.
- Pruebas en cluster en pausa hasta reemplazo de la refrigeración AIO.
- `helm` no está en el `PATH` de Windows: `kustomize build --enable-helm` y validación de values de charts (Artifactory, Jaeger, SonarQube) pendientes en WSL.
- CNPs sin validar en cluster (labels/puertos reales) hasta disponer de Cilium/Hubble (ver 1.1, 1.3, 1.2f).
- `bootstrap.sh` aún no siembra las credenciales de la tabla 1.6 (solo el auth method del VCO).
- IngressRoutes aún en `web` sin TLS: migración pendiente (1.42).
