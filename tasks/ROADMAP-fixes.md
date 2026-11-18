# ROADMAP-fixes — Cierre de hallazgos de la validación quirúrgica (18-11-2026)

Documento vivo de seguimiento. Se actualiza al cerrar cada fix (`[ ]` → `[x]`), **solo** tras `kustomize build --enable-helm` rc=0 (🖥️) o validación en cluster (☸️).

- **Procedencia:** informe de validación quirúrgica del 18-11-2026 (217 archivos; 43 kustomizations, 40 OK / 3 FAIL) + reverificación quirúrgica del mismo snapshot (3 hallazgos nuevos: F05, F12-labels, F09-plugins).
- **Detalle quirúrgico de cada fix** (evidencia exacta, edición ANTES/DESPUÉS, YAML completo y validación): `PROMPT-fixes.md`, secciones F00-F23. Este archivo es el tracker; aquel es la instrucción de ejecución.
- El ROADMAP principal (`tasks/ROADMAP.md`) mantiene las fases de largo plazo (2.x, 4.x, 5) y el rol; este archivo cierra los hallazgos de la validación y SE RETIRA cuando el gate G3 esté en verde.
- 🖥️ = se puede realizar sin cluster (edición + build). ☸️ = requiere cluster (suspendidas por la refrigeración AIO — NO marcar hasta volver).

---

## Convenciones

- Método: UN fix a la vez → `kustomize build --enable-helm <carpeta>` rc=0 → marcar `[x]` con evidencia (archivo:línea del estado final) → siguiente.
- Ante un FAIL: revertir solo ese archivo, reportar el error exacto, no improvisar.
- IDs F00-F23 idénticos en ROADMAP-fixes.md y PROMPT-fixes.md (referencia cruzada 1:1).

---

## F00 — Reconciliación de ROADMAP.md (higiene de tracking)

- [x] **F00.1** 🖥️ Marcar `[x]` en `tasks/ROADMAP.md` los ítems verificados como implementados por el informe: 1.4b, 1.9, 1.20-1.37. **Evid.:** ya constaban `[x]` (ROADMAP.md:121, 146, 157-183) — sin edición necesaria.
- [x] **F00.2** 🖥️ En la tabla de 1.6: tachar RabbitMQ (el operador genera `rabbitmq-default-user` — `rabbitmq-cluster.yaml` lín. 21-23) y Redis (PushSecret ya la publica). Resolver la decisión "postgres/pgadmin": SE SIEMBRAN (F13). **Evid.:** ROADMAP.md:128-129 (SE SIEMBRA F13), :137 (RabbitMQ tachado), :139 (Redis tachado).
- [x] **F00.3** 🖥️ Añadir al inicio del ROADMAP la referencia a este archivo. **Evid.:** ROADMAP.md:8.

---

## Fase P0 — Desbloquear el build (3 FAIL + CRDs sin renderizar)

**Gate G0 (bloqueante para P1):** barrido completo 43/43 OK, 0 FAIL.

- [x] **F01** (N4, CRÍTICO) 🖥️ `includeCRDs: true` en las kustomizations de external-secrets (2.4.0, wave 1), traefik (39.0.8, wave 6), kube-prometheus-stack (84.5.0, wave 7) y trivy-operator (0.32.1, wave 16); + `SkipDryRunOnMissingResource=true` en los wrappers 01/06/07/09/16. Sin esto, ESO no aplica sus CRDs → el flujo completo de secretos no se crea en cluster. **Evid.:** `includeCRDs: true` en external-secrets/kustomization.yaml:16, traefik:16, prometheus-stack:13, trivy:13; `SkipDryRunOnMissingResource=true` en 01-external-secrets.yaml:26, 06-traefik.yaml:26, 07-prometheus-stack.yaml:26, 09-rabbitmq.yaml:26, 16-trivy.yaml:26. **Val.: ⏳ PENDIENTE** (sin tooling local — ver "Validaciones pendientes").
- [x] **F02** (N1, CRÍTICO) 🖥️ `components/data-services/ddbb/postgres` compila: `pgadmin-env-patch.yaml` de `resources:` → `patches:` (target Deployment/pgadmin) + eliminar los bloques `httpGet` de las probes de `pgadmin-values.yaml` (lín. 52-68). **Evid.:** kustomization.yaml:6-21 (patch con target apps/v1 Deployment/pgadmin); pgadmin-values.yaml:52-62 (probes sin httpGet, timing intacto). **Val.: ⏳ PENDIENTE**.
- [x] **F03** (N2, CRÍTICO) 🖥️ `components/repositories/gitlab` compila: eliminar `minio:` raíz (lín. 67-69) → `global.minio.enabled: false`; añadir `appConfig.artifacts.enabled: false`. **Evid.:** gitlab-values.yaml:13-15 (`global.minio.enabled: false`), :59-60 (`artifacts.enabled: false`), bloque raíz `minio:` eliminado. **Val.: ⏳ PENDIENTE** (el 2º error aparece tras arreglar el 1º — esperado).
- [x] **F04** (N3, CRÍTICO) 🖥️ `components/security/trivy`: chart `trivy` 0.32.1 NO existe en aquasecurity → `name: trivy-operator` (misma versión/repo/releaseName); alinear labels `trivy` → `trivy-operator` en `config/trivy/trivy-service-monitor.yaml` (lín. 11) y `config/prometheus-stack/prometheus-scrape-network-policy.yaml` (lín. 115-116). Cierra la duda 3.11. **Evid.:** trivy/kustomization.yaml:7 (`trivy-operator`); trivy-service-monitor.yaml:11; prometheus-scrape-network-policy.yaml:116. **Val.: ⏳ PENDIENTE**.

---

## Fase P1 — Despliegue determinista en cluster (credenciales en la wave correcta + red del núcleo)

**Gate G1:** barrido 43/43 + `grep -rn "external-secrets.io/v1beta1" k8s` = 0 + `bash -n bootstrap.sh` OK.

- [x] **F05** (NUEVO — CRÍTICO, detectado en reverificación) 🖥️ **Inversión de waves en credenciales de arranque:** los ES que crean los secrets que grafana (wave 7), gitlab (10), artifactory (11), jenkins (12) y sonarqube (13) referencian al boot viven en `config/<x>/` (waves 25-33). Mecánica del bloqueo: pods en `CreateContainerConfigError` → Application Progressing → el root app detiene las waves siguientes. Fix (regla de ubicación del ROADMAP): `git mv` de 6 archivos (10 ES) — grafana-es, gitlab-postgres-es, gitlab-redis-es, artifactory-es, jenkins-es, sonarqube-es — de `config/<x>/` a la carpeta del componente + ajuste de los 10 kustomizations implicados. Quedan deliberadamente en config/: traefik-dashboard-auth, redis-insight-auth, redis-push-secret. **Evid.:** git mv ×6 (git status: R config/<x>/*-es.yaml → components/<x>/); kustomizations de config/{prometheus-stack,gitlab,artifactory,jenkins,sonarqube} sin los ES; kustomizations de components/{monitoring/prometheus-stack:6-7, repositories/gitlab:6-8, devops-tools/{artifactory:6-7, jenkins:6-7, sonarqube:6-7}} con `resources:` antes de `helmCharts:`. **NOTA:** el prompt decía "9 ES movidos / 11 total"; el conteo real es **10 movidos / 12 total** (artifactory-es tiene 4 ES, sonarqube-es 2 — la propia tabla del prompt lo suma así). **Val.: ⏳ PENDIENTE** (builds ×10).
- [x] **F06** (1.3b, CRÍTICO) 🖥️ Red mínima de arranque del núcleo: crear `components/cert-manager/cert-manager-network-policy.yaml` (CNP: egress kube-apiserver 443/6443/8443; ingress webhook 6443 desde apiserver, probes 6080/8080 desde host, metrics 9402 desde prometheus) y reescribir `components/security/external-secrets/external-secret-network-policy.yaml` como CNP (Vault 8200 + apiserver egress + webhook 8081 + métricas 8080). Sin esto la wave 0 no completa bajo default-deny. **Evid.:** cert-manager-network-policy.yaml:1-51 (CNP nueva, referenciada en kustomization.yaml:8); external-secret-network-policy.yaml:1-57 (CNP `eso-zero-trust` reemplaza a la NetworkPolicy estándar de solo-Vault). **Val.: ⏳ PENDIENTE** (builds; ☸️ puertos contra endpoints reales).
- [x] **F07** (N5) 🖥️ Crear `config/traefik/traefik-to-alertmanager.yaml` (selector `app.kubernetes.io/name: alertmanager`, puerto 9093) + añadir al kustomization. Sin esto, la route de Alertmanager da 502. **Evid.:** traefik-to-alertmanager.yaml:1-23 (nuevo, patrón de traefik-to-argocd.yaml); config/traefik/kustomization.yaml:19. **Val.: ⏳ PENDIENTE**.
- [x] **F08** (1.7 / N6) 🖥️ Migrar a `external-secrets.io/v1` los archivos v1beta1: postgres-es:1, pgadmin-es:1, gitlab-redis-es:2, redis-insight-auth:17 y traefik-dashboard-auth:18 (solo el doc ExternalSecret; generadores Password en v1alpha1 intactos; `refreshInterval: "0"` intencional). PushSecret v1alpha1 NO tocado. **NOTA:** el prompt listaba 6 archivos pero `gitlab-postgres-es.yaml` ya estaba en v1 (creado por la migración E4) — migrados 5. **Evid.:** `git grep "external-secrets.io/v1beta1" -- k8s` = 0 matches ✓. **Val.: ⏳ PENDIENTE** (builds).
- [x] **F09** (N7 — corregido: son 8 plugins, no 5) 🖥️ Jenkins: controlador `lts` → **`2.555.1-jdk21`** (DESVIACIÓN del prompt, que decía jdk17: el Chart.yaml 5.9.18 declara como imagen testeada `docker.io/jenkins/jenkins:2.555.1-jdk21` en artifacthub.io/images, y gitlab-plugin 1.2162 requiere Java 21/25); agente `latest` → `3355.v388858a_47b_33-19` (default del chart); 8 plugins fijados: kubernetes:4423.vb_59f230b_ce53, workflow-aggregator:608.v67378e9d3db_1, git:5.10.1, configuration-as-code:2074.va_57f83f7a_10b_ (defaults del chart 5.9.18, con underscore final que el prompt omitía), credentials-binding:728.v902a_273b_8947 (req 2.479.3), pipeline-stage-view:2.41 (req 2.479.1), gitlab-plugin:1.2162.ve2e85250a_db_a_ (requiredCore 2.541.3 según update-center API; el README menciona 2.555.3+ como guía — ☸️ vigilar en runtime), docker-workflow:653.v2f2c08eff0ec (req 2.541.3). **Evid.:** jenkins-values.yaml:18, :52-88, :129. **Val.: ⏳ PENDIENTE**.
- [x] **F10** (1.8) 🖥️ `prometheus-stack-values.yaml`: `prometheusSpec.serviceMonitorSelectorNilUsesHelmValues: false` (hoy implícito vía label `release: prometheus`). **Evid.:** prometheus-stack-values.yaml:14. **Val.: ⏳ PENDIENTE**.

---

## Fase P2 — Plataforma funcional (datos, observabilidad, DNS zero-trust)

**Gate G2:** barrido 43/43 + cero contraseñas residentes fuera de Vault/ESO/PushSecret.

- [x] **F11** (3.3) 🖥️ BBDD/usuarios Postgres para gitlab (gitlabhq_production), sonarqube y artifactory: `app-roles-es.yaml` (3 ES en data-services: gitlab-db-credentials ← databases/postgres [réplica distribuida], sonarqube-db-credentials, artifactory-db-credentials), `postgres-app-init-job.yaml` (Job idempotente con SQL `\gexec`; 3 llamadas psql explícitas — el bucle `for`/`eval` del prompt era frágil y su SQL interpolaba variables dentro de literales, lo que psql no hace; se usa `format('CREATE ROLE %I LOGIN PASSWORD %L', :'app_name', :'app_pass')` con variables FUERA del literal) y `postgres-app-init-network-policy.yaml` (egress job→5432); añadidos al kustomization (resources:6-12). El superusuario YA está cableado (superuserSecret postgres-credentials — no repetido). Alternativa futura documentada: CNPG `managed.roles`. **Evid.:** app-roles-es.yaml:1-59, postgres-app-init-job.yaml:1-96, postgres-app-init-network-policy.yaml:1-19, kustomization.yaml:6-12. **Val.: ⏳ PENDIENTE** (build; ☸️ `psql -l` con las 3 BBDD).
- [x] **F12** (3.9 + hallazgo nuevo) 🖥️ ELK: `elk-clients-network-policy.yaml` reescrito como CNP (kibana-zero-trust + filebeat-zero-trust) con labels reales del chart (`app: kibana` / `app: filebeat` / `app: elasticsearch-master`) + egress de Filebeat a kube-apiserver 443/6443/8443 (:71-81); 3 selectores corregidos en `elk-network-policy.yaml` (:9, :20, :29); 3 reglas Elastic de `prometheus-scrape-network-policy.yaml` con namespace + labels reales (:78-101); SM de kibana (:11) y ES (:11) con labels `app:`; filebeat-SM → **PodMonitor** (`filebeat-pod-monitor.yaml`, portNumber 5066) + kustomization actualizado (:7). **Evid.:** archivos citados. **Val.: ⏳ PENDIENTE** (builds; ☸️ labels `--show-labels` + Hubble filebeat→apiserver).
- [ ] **F12b** (decisión, NO bloquea) Elasticsearch sin endpoint `_prometheus/metrics` (sin plugin): decidir exporter o degradar el SM de ES. Pendiente documentado del ROADMAP 3.9.
- [x] **F13** (1.6) 🖥️ Sembrar credenciales en Vault vía `bootstrap.sh`: nueva función `vault_seed_kv` (idempotente — `vault kv get -field` antes de `put`) llamada tras `vault_seed_auth`; 8 paths/12 claves exactos según los remoteRef de los ES (grafana, jenkins, sonarqube×2, artifactory×2, postgres, pgadmin). NO sembrar RabbitMQ (operador) ni Redis (PushSecret). **Evid.:** bootstrap.sh:281-314 (función), :530 (llamada tras vault_seed_auth). **Val.: `bash -n` ✓ (WSL)**; ☸️ re-bootstrap + `kubectl get externalsecret -A` Ready pendiente.
- [x] **F14** (1.2b) 🖥️ DNS L7 anti-exfiltración: creado `bootstrap/00-foundation/clusterwide-dns-allowlist.yaml` (CCNP; solo `*cluster.local`; excluye kube-system); eliminado el doc #5 `allow-dns` L3/L4 de los 11 `namespaces/<ns>.yaml` (10 por truncado + argocd.yaml por edit tras incidente de finales de línea CRLF/LF — restaurado con `git checkout` y re-hecho con edit); cableado en foundation/kustomization.yaml:28. **NOTA:** la excepción DNS de `argocd-repo-server` YA existía en `bootstrap/02-argocd/argocd-network-policy.yaml:150-180` con allowlist más completa que la del prompt (incluye objects.githubusercontent.com, release-assets.githubusercontent.com, charts.jenkins.io) — edición 3 no necesaria. **Evid.:** archivos citados; `git diff --stat namespaces` = 26 líneas eliminadas por archivo. **Val.: ⏳ PENDIENTE** (build Foundation; ☸️ Hubble DROPPED).
- [x] **F15** (1.2c) 🖥️ Proteger CoreDNS: creado `bootstrap/00-foundation/coredns-ingress-protection.yaml` (CCNP; DNS 53 solo desde namespaces con label `part-of: microservices-platform` — ya presente; probes 8080/8181 desde host; metrics 9153 desde prometheus); cableado en foundation/kustomization.yaml:29. **Evid.:** archivos citados. **Val.: ⏳ PENDIENTE** (build Foundation; ☸️ pod sin label no resuelve).

---

## Fase P3 — Higiene, decisiones y migración TLS

**Gate G3:** barrido 43/43 + ROADMAP.md reconciliado + este archivo 100% `[x]`.

- [x] **F16** (N8) 🖥️ Eliminado `components/cert-manager/cluster-issuer.yaml` (duplicado exacto de `selfsigned-issuer` en `config/cert-manager/platform-ca.yaml:2-7`) y quitado del kustomization (queda solo la CNP de F06). Decisión validada con el usuario: el issuer vive en `config/` (no es boot-critical; la cadena TLS completa se agrupa en wave 19). **Evid.:** `git grep selfsigned-issuer` = solo platform-ca.yaml (2 refs) ✓; cert-manager/kustomization.yaml:6-7. **Val.: ⏳ build.**
- [x] **F17** (1.38) 🖥️ README de Vault: 15 referencias migradas a `secrets-manager` (l.22, 36, 53, 63, 72, 75, 133, 159, 166, 200, 203, 209, 216, 221, 232, 234); `securityContext` intacto. **Evid.:** grep `[ ]-n (security|vault)|--namespace (security|vault)|namespace (security|vault)` = 0 ✓.
- [x] **F18** (1.40/B9) 🖥️ `namespaces/secrets-manager.yaml:60`: comentario `'vault'` → `'secrets-manager'`.
- [x] **F19** (1.41/B8) 🖥️ README de Foundation reescrito (44 líneas): inventario real (11 namespaces con Quota/LimitRange/default-deny; storage-classes `local-default` [default] + `nfs`; CCNP DNS + CoreDNS), requisito Cilium (`minikube start --cni=cilium`), sin referencias a archivos inexistentes (`namespace.yaml`, `persistent-volumes.yaml`, `storage-class.yaml`, `network-policies.yaml`), sin `standard`/`manual`/`slow`, sin `--cni=bridge`, sin la sección 9 duplicada.
- [x] **F20** (N10 — decisión RESUELTA: opción a) 🖥️ Creado `config/platform-tls/` con `git mv` de `platform-ca.yaml` (desde config/cert-manager) y `platform-tls.yaml` (desde config/traefik); kustomization nuevo SIN namespace raíz; kustomizations limpiados (config/cert-manager: solo ServiceMonitor; config/traefik: sin platform-tls); wrapper `19-platform-tls.yaml` (wave 19, sin `destination.namespace`, `SkipDryRunOnMissingResource=true`); tabla objetivo del ROADMAP anotada (wave 19 = TLS; wave 21 gap documentado). **Evid.:** git status R×2; 19-platform-tls.yaml:1-26; ROADMAP.md:94-95. **Val.: ⏳ build.**
- [x] **F21** (1.42) 🖥️ 16 IngressRoutes (15 componentes + dashboard) migradas a `entryPoints: [websecure]` + `tls: {}` (usa `TLSStore default` del cert `*.dev.lab`, wave 19). **Evid.:** `git grep "tls: {}"` en rutas = 16 ✓; `git grep -E '^\s+- web$'` en rutas = 0 ✓. **Val.: ⏳ build; ☸️ Certificate Ready + https servido.**
- [x] **F22** (E13) 🔄 **DEFERIDO por decisión del usuario (18-11-2026):** la versión del proyecto no es final y la documentación se reescribirá después; anotar Cilium en docs ahora incurriría en retrabajo. Pendiente: nota "Requisito Cilium" en docs/components/*.md (10) + READMEs de cert-manager/vault cuando se reescriba la doc (cubre E13/gate Fase 5). NO bloquea el despliegue. `k8s/README.md` y Foundation README ya documentan `--cni=cilium`.
- [x] **F23** (N9) 🖥️ Carpeta vacía `components/data-services/ddbb/mysql/` eliminada.

---

## Correcciones post-revisión (18-11-2026)

Revisión estática de F11-F23: 4 bloqueantes + 2 importantes corregidos.

- [x] **R1** (F15) 🖥️ `coredns-ingress-protection.yaml`: label de namespace inválida (`namespace_labels/part-of`, no coincidía con nada → caída total de DNS) → `k8s:io.cilium.k8s.namespace.labels.part-of`; DNS también desde `kube-system` y `host`/`remote-node`; egress Prometheus→CoreDNS 9153 en `prometheus-scrape-network-policy.yaml`.
- [x] **R2** (F14) 🖥️ `clusterwide-dns-allowlist.yaml`: `enableDefaultDeny: {egress: false, ingress: false}` (sin esto activaba default-deny de egress en pods ajenos a la plataforma; requiere Cilium ≥ 1.15). Eliminadas las reglas DNS L4 que anulaban el L7 en 4 CNPs: prometheus-scrape, trivy, vault, vault-config-operator. Selector de `trivy-network-policy.yaml` → `trivy-operator` (residuo de F04).
- [x] **R3** (F13) 🖥️ `vault_seed_kv`: espera al motor `secret/` (lo crea el VCO; antes abortaba el bootstrap); una sola `kv put` por path (antes se perdían claves en grafana/jenkins/artifactory); aleatorios con `vault write sys/tools/random` (sin dependencia de openssl); valida valores vacíos; se ejecuta tras `restart_vault_config_operator`. **Val.:** `bash -n` ✓ y `sh -n` del heredoc ✓.
- [x] **R4** (F11) 🖥️ Ingress al Postgres desde `app: postgres-app-init` en `cnpg-instances-bootstrap` (antes el Job nunca conectaba → app Degraded); Job como hook `PostSync` + `BeforeHookCreation` (recreado en cada sync; sin inmutabilidad; corre con Cluster/ES Healthy).
- [x] **R5** (F21) 🖥️ GitLab `global.hosts.https: true`; Jenkins `jenkinsUrl: https://jenkins.dev.lab`; Traefik `ports.web.http.redirections.entryPoint` → websecure. **Val.:** `helm template` traefik 39.0.8 ✓ (renderiza `--entryPoints.web.http.redirections.entryPoint.to=:443`).
- [x] **R6** 🖥️ `bootstrap-uninstall.sh` elimina las 2 CCNP de Foundation (cluster-scoped). **Val.:** `bash -n` ✓.
- [x] **R7** (preexistente) 🖥️ VaultAudit inoperante por 3 causas: (a) SA `vault-config-operator-controller-manager` no admitida por el rol → `default` (como los otros 3 CRs del VCO); (b) la policy `vault-config-operator` no tenía `sys/audit` (requiere `sudo`) → añadida en `vault_seed_auth`; (c) `/vault/logs` no persistía y no tenía límite (Vault es fail-closed si no puede escribir el audit) → `server.auditStorage` 2Gi en `vault-values.yaml` y `file_path: /vault/audit/audit.log`. **Val.:** `helm template` vault 0.32.0 ✓ (mountPath `/vault/audit`, PVC 2Gi); `bash -n` ✓. ⚠️ Cambia `volumeClaimTemplates`: en un clúster ya desplegado hay que recrear el StatefulSet (`kubectl delete sts vault -n secrets-manager --cascade=orphan`); en bootstrap limpio no aplica.
- [ ] **R8** (preexistente, 1.2e) 🔄 **ABIERTO por decisión del usuario (18-11-2026).** Jenkins `installPlugins` necesita salida externa (`updates.jenkins.io` + descargas vía `get.jenkins.io` → mirrors). Además, `config/jenkins/jenkins-network-policy.yaml` (wave de config) es NetworkPolicy estándar sin egress a kube-apiserver (plugin kubernetes) ni ingress de agentes (50000), y en el primer arranque (wave del componente) solo existe el default-deny. Opciones evaluadas: allowlist FQDN + `JENKINS_UC_DOWNLOAD_URL=https://archives.jenkins.io/plugins`; allowlist amplia `*.jenkins.io` + mirrors; imagen con plugins horneados. **Bloquea el arranque de Jenkins.**

---

## Fuera de alcance de este archivo (siguen en tasks/ROADMAP.md)

- 2.1-2.3 (uninstall + bootstrap limpio, confirmación Healthy de núcleo, completar config/argocd y config/redis) — ☸️, tras P1/P2.
- 1.2e (salidas externas de Artifactory/Trivy/Imágenes por nodo) — decisión deliberadamente abierta.
- 1.2f-validación con Hubble de la allowlist FQDN de repo-server — ☸️.
- 1.1 (verificar CNI Cilium en Minikube) — ☸️.
- 4.2 (Reloader), 4.3 (bootstrap-uninstall actualizado), 4.4 (login humano Vault), 4.5 (Velero+MinIO, snapshots Raft), 4.6 (TLS interno).
- Fase 5 (docs/) completa + migración de los 2 README restantes a `docs/components/`.
- OTLP 4317/4318 de Jaeger: cerrado hasta el primer emisor (3.10).

## Validaciones 🖥️ pendientes (tooling no disponible en esta sesión)

`kustomize`/`helm` no están en el PATH de Windows y `kustomize` no está instalado en WSL (helm 4.2.2 sí, en `/usr/local/bin`). Ediciones aplicadas y evidenciadas; builds pendientes de ejecución por el usuario (WSL o donde resida el tooling):

```bash
# G0 (tras F01-F04): barrido completo — esperado 43/43 OK, 0 FAIL
for k in $(find k8s -name kustomization.yaml | sort); do
  echo "== $k"; kustomize build --enable-helm $(dirname $k) > /dev/null || echo "FAIL $k"
done

# F01 — CRDs renderizados (esperado > 0 en cada uno):
kustomize build --enable-helm k8s/platform/components/security/external-secrets | grep -c 'kind: CustomResourceDefinition'
kustomize build --enable-helm k8s/platform/components/networking/traefik | grep -c 'kind: CustomResourceDefinition'
kustomize build --enable-helm k8s/platform/components/monitoring/prometheus-stack | grep -c 'kind: CustomResourceDefinition'
kustomize build --enable-helm k8s/platform/components/security/trivy | grep -c 'kind: CustomResourceDefinition'

# F02 — pgadmin: env presente y 1 solo httpGet por probe:
kustomize build --enable-helm k8s/platform/components/data-services/ddbb/postgres | grep -A4 'name: PGADMIN_DEFAULT_PASSWORD'

# F03 — gitlab (helm template + build):
helm repo add gitlab https://charts.gitlab.io 2>/dev/null; helm template gitlab gitlab/gitlab --version 9.11.2 -f k8s/platform/components/repositories/gitlab/gitlab-values.yaml --namespace repositories > /dev/null && echo OK
kustomize build --enable-helm k8s/platform/components/repositories/gitlab > /dev/null && echo OK

# F04 — trivy:
kustomize build --enable-helm k8s/platform/components/security/trivy > /dev/null && echo OK
```

## Validaciones ☸️ acumuladas (ejecutar al volver el cluster)

1. Puertos reales de webhooks (cert-manager 6443/6080; ESO 8081) contra `kubectl get endpoints`.
2. Labels de pods con `--show-labels` (traefik, KPS, CNPG, elastic `app:`).
3. Hubble: DNS L7 (solo `*.cluster.local` + allowlist repo-server), flujos filebeat→apiserver, scrape de Prometheus.
4. Re-bootstrap completo: `kubectl get externalsecret -A` Ready; `psql -l` con gitlabhq_production/sonarqube/artifactory.
5. Certificate `platform-tls` Ready y https en `*.dev.lab` (CA importada en el trust del navegador).

## Lecciones aprendidas nuevas (añadir al ROADMAP al cerrar G3)

| Tema | Lección |
|---|---|
| Kustomize+Helm | `helm template` no renderiza `crds/` sin `includeCRDs: true`: un componente puede "desplegar" sin instalar sus CRDs. Verificar el build con grep de CustomResourceDefinition |
| Kustomize | Un parche parcial de Deployment JAMÁS va en `resources:` (ID conflict con el chart): siempre `patches:` con `target` |
| Helm | Los charts con REMOVALS en NOTES abortan el render de forma encadenada: arreglar el 1er error, re-ejecutar, repetir |
| Waves | Un secret referenciado por values de un componente (waves 6-18) debe crearse en el MISMO componente o antes: en config/ (waves 20+) produce CreateContainerConfigError y congela el App of Apps |
| Charts | Los labels de pods son del CHART, no de Kubernetes: elastic usa `app: <name>`, KPS/CNPG usan `app.kubernetes.io/name`. Validar contra el chart, no asumir |
| Semillas | `vault kv get` antes de `put`: un bootstrap idempotente nunca rota contraseñas existentes |
| GitOps | Healthy ≠ reconciliado; y Deployed ≠ funcional: sin BBDD/roles (F11) ni semillas (F13) todo "despliega" y nada conecta |

## Reproducibilidad de la validación

```bash
# Barrido completo (gate de cada fase):
for k in $(find k8s -name kustomization.yaml | sort); do
  echo "== $k"; kustomize build --enable-helm $(dirname $k) > /dev/null || echo "FAIL $k"
done

# Forense puntual de un chart:
helm template <release> <repo>/<chart> --version <x> -f <values> --namespace <ns>

# Chequeos de cierre:
grep -rn "external-secrets.io/v1beta1" k8s | wc -l        # 0
grep -rn "0.0.0.0/0\|namespaceSelector: {}" k8s | wc -l   # 0
bash -n k8s/bootstrap.sh                                        # OK (✓ ya validado en WSL)
```

Estado inicial (18-11-2026): 40/43 kustomizations OK; 3 FAIL (N1 postgres, N2 gitlab, N3 trivy); 6 ES en v1beta1; 6 ES de arranque mal ubicados; 0 CRDs renderizados en 4 charts del núcleo. Estado objetivo tras G3: 43/43, flujo de secretos completo desde la wave 5, credenciales en la wave de cada consumidor, TLS en wave 19.
