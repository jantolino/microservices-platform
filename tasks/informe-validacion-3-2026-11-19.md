# INFORME — Tercera validación quirúrgica de la plataforma K8s self-contained

**Fecha:** 19-11-2026 · **Snapshot evaluado:** `k8s.rar` (tercera entrega) — 220 YAML, **39 kustomizations** (17 componentes + 21 config + 1 Foundation), 38 wrappers + `root-app-of-apps.yaml`, `bootstrap.sh` (581 líneas) + `bootstrap-uninstall.sh`.
**Contrastado contra:** `ROADMAP.md` (fases 0-5), `ROADMAP-fixes.md` (F00-F23 + R1-R8) y `PROMPT-fixes.md` (instrucciones F00-F23).
**Método:** lectura archivo-por-archivo + **primera ejecución real de los gates de build** (las validaciones 🖥️ marcadas "⏳ PENDIENTE" nunca se habían ejecutado; en esta sesión se instalaron `kustomize` 5.4.3 y `helm` 3.16.4/v4.2.2 y se corrió el barrido completo con descarga real de los charts: gitlab 9.11.2, kube-prometheus-stack 84.5.0, traefik 39.0.8, jenkins 5.9.18, cert-manager 1.18.2, vault 0.32.0, sonarqube, artifactory, elk 8.5.1, cloudnative-pg, trivy-operator…).

---

## 1. Resultado ejecutivo

| Bloque | Veredicto |
|---|---|
| F00-F23 (ROADMAP-fixes) | **22/23 aplicados y verificados completos**. **F03 INCOMPLETO**: el bloque raíz `minio:` sigue en `gitlab-values.yaml` → build de gitlab sigue fallando (ver hallazgo B) |
| R1-R7 (post-revisión) | **7/7 aplicados y verificados** |
| R8 (Jenkins egress externa) | **ABIERTO** — por decisión del usuario (18-11); bloqueará el arranque de Jenkins en cluster |
| F12b (exporter de métricas ES) | **ABIERTO** — decisión, no bloquea despliegue |
| Barrido de builds (gate G0/G1/G2 combinado) | **37/39 OK, 2 FAIL** — los 2 FAIL son los hallazgos A y B |
| Invariantes del proyecto | **Todos en verde** (0 credenciales en Git, 0 `0.0.0.0/0`, 0 `namespaceSelector: {}`, 0 ApplicationSet, 0 `*.desarrollo`, 0 storageClass `standard`, Filebeat HTTP intacto, App of Apps puro) |
| ROADMAP.md reconciliado | ✓ (ítems `[x]` re-verificados contra código uno a uno) |

**Conclusión:** las recomendaciones de PROMPT-fixes.md **sí se aplicaron** (con una desviación documentada y válida en F09: jdk21 en lugar de jdk17). Quedan **exactamente 2 ediciones de 3 líneas en total** para tener 39/39 builds en verde y todo el árbol desplegable en cuanto haya cluster. Además hay **1 incompatibilidad de tooling** (kustomize↔helm4) que explica por qué las validaciones 🖥️ llevaban meses "pendientes" y hay 4 decisiones/pendientes de fondo (R8, F12b, 1.2e) más la Fase 2 ☸️.

---

## 2. Hallazgos nuevos (NO estaban en F00-F23 ni R1-R8)

### Hallazgo A (CRÍTICO — bloquea la wave 24+ en cluster): `z` corrupto en `config/traefik/kustomization.yaml`

`k8s/platform/config/traefik/kustomization.yaml` **línea 5** contiene un carácter `z` colgado al inicio de la línea del primer recurso:

```yaml
# ANTES (línea 5):
z  - traefik-service-monitor.yaml
# DESPUÉS:
  - traefik-service-monitor.yaml
```

- **Síntoma exacto:** `Error: invalid Kustomization: yaml: line 24: could not find expected ':'` (el parser reporta el final del archivo; la causa es la línea 5).
- **Efecto en cluster:** la Application `config-traefik` (wave 24) no puede renderizar → Degraded → **el root app congela las waves 24-37**: todas las políticas `traefik-to-<x>` (incluida la nueva `traefik-to-alertmanager` de F07), el dashboard con BasicAuth y el ServiceMonitor de Traefik quedarían sin aplicar.
- **Evidencia de que es la única corrupción:** el archivo tiene 23 líneas de recursos correctas; ningún otro kustomization del árbol presenta errores de sintaxis (37/39 builds OK).
- **Fix simulado en esta validación:** eliminando solo el carácter `z`, `kustomize build` → **rc=0**. Es una edición de 1 carácter.

### Hallazgo B (CRÍTICO — F03 quedó incompleto): el bloque raíz `minio:` sigue en `gitlab-values.yaml`

ROADMAP-fixes marca F03 `[x]` con evidencia *"bloque raíz `minio:` eliminado"* — **la evidencia es falsa en el snapshot actual**:

- `gitlab-values.yaml:14-15` — `global.minio.enabled: false` ✓ **aplicado**
- `gitlab-values.yaml:59-60` — `appConfig.artifacts.enabled: false` ✓ **aplicado**
- `gitlab-values.yaml:73-75` — bloque raíz **AÚN PRESENTE** ✗:

```yaml
# ANTES (líneas 73-75, tal cual están hoy):
# Sin MinIO: el PoC usa storage local (Gitaly) y no almacena objetos
minio:
  enabled: false

# DESPUÉS (las 3 líneas eliminadas por completo; el comentario puede moverse a global.minio si se quiere conservar)
```

- **Síntoma exacto (reproducido):** `Error: execution error at (gitlab/templates/NOTES.txt:232:3): REMOVALS: minio: Chart-local 'enabled' property has been moved to global. Please remove 'minio.enabled'...`
- **Efecto en cluster:** la Application `gitlab` (wave 10) no renderiza → **congela las waves 11+**: artifactory, jenkins, sonarqube, elk, jaeger, trivy, headlamp, redis-insight, TLS 19-37 y TODA la config posterior no llegarían nunca.
- **Fix simulado en esta validación:** eliminando SOLO el bloque raíz (73-75), `kustomize build --enable-helm` → **rc=0**. No hay errores encadenados posteriores (las otras 2 ediciones de F03 ya están aplicadas y correctas).

### Hallazgo C (tooling — explica los meses de validaciones pendientes): kustomize 5.4.3 es INCOMPATIBLE con helm v4

- kustomize ejecuta internamente `helm version -c --short`; **helm v4 eliminó el shorthand `-c`** → TODOS los builds con `helmCharts` fallan con `unknown shorthand flag: 'c' in -c` con helm 4.2.2 en el PATH (reproducido: 16/39 FAIL sistemáticos).
- Con **helm 3.16.4** el mismo barrido pasa a 37/39 (los 2 restantes son los hallazgos A y B, reales).
- **Acción:** para ejecutar los gates en WSL/Windows el usuario necesita **helm 3.x dedicado a kustomize** (p. ej. `helm3` en el PATH y un alias, o usar la combinación validada aquí: kustomize 5.4.3 + helm 3.16.4). El `bootstrap.sh` puede seguir usando helm 4.2.2 para `helm upgrade --install` (ese path no pasa por kustomize). Nota adicional: kustomize deja los charts descargados en `<carpeta>/charts/` → **añadir `charts/` a `.gitignore`** para no commitear ~200 MB de charts (gitlab 9.11.2 descomprime 200+ archivos YAML).

### Observación D (higiene de gates): el conteo "43 kustomizations" de los gates ya no corresponde

El árbol actual tiene **39 kustomizations** (inventario: 17 `components/` + 21 `config/` + 1 `bootstrap/00-foundation`). Se verificó que **no falta ninguna carpeta**: los únicos directorios con YAML sin kustomization son los de `bootstrap/` que se aplican por diseño (namespaces y storage-class vía `kubectl apply -k` de Foundation; 01-redis y 02-argocd vía `bootstrap.sh`; 03-platform-orchestration vía ArgoCD directory). Ninguna carpeta de `components/` ni `config/` está huérfana. **Acción:** re-baselinar el texto de los gates G0-G3 de "43/43" a **"39/39"** en el próximo PROMPT/ROADMAP-fixes para evitar falsos negativos.

---

## 3. Verificación fix por fix (F00-F23) — evidencia contra el snapshot

| ID | Veredicto | Evidencia verificada (archivo: línea del estado actual) | Build/validación ejecutada |
|---|---|---|---|
| **F00** Reconciliación ROADMAP | ✓ aplicado | ROADMAP.md:8 (referencia a ROADMAP-fixes), 1.4b/1.9/1.20-1.37 marcados `[x]`, tabla 1.6 con RabbitMQ/Redis tachados y "SE SIEMBRA (F13)" | Lectura directa |
| **F01** includeCRDs + SkipDryRun | ✓ aplicado | `includeCRDs: true` en external-secrets:16, traefik:16, prometheus-stack:16, trivy:13 (+ preexistente postgres:32, VCO:16). `SkipDryRunOnMissingResource=true` en wrappers 01/06/07/09/16/19 | **CRDs renderizados (gate ejecutado): ESO 23, Traefik 29, KPS 10, trivy-operator 12** — todos >0 |
| **F02** postgres compila | ✓ aplicado | kustomization.yaml: pgadmin-env-patch en `patches:` con target Deployment/pgadmin (:14-24); resources:6-12; pgadmin-values.yaml:52-64 probes SIN `httpGet` | **build rc=0**; salida con `PGADMIN_DEFAULT_PASSWORD` ← secretKeyRef `pgadmin-secrets` verificado; 21 httpGet en todo el build (operator+chart), sin duplicados de probe |
| **F03** gitlab compila | **✗ INCOMPLETO** | global.minio:14-15 ✓, appConfig.artifacts:59-60 ✓, **bloque raíz minio:73-75 PRESENTE** | **build FAIL** (REMOVALS minio). Simulado: rc=0 tras borrar 73-75 |
| **F04** trivy → trivy-operator | ✓ aplicado | kustomization.yaml:7 `name: trivy-operator`; labels alineados en trivy-service-monitor.yaml:11 y prometheus-scrape-network-policy.yaml:119 | **build rc=0 + 12 CRDs** |
| **F05** ES de arranque a components/ | ✓ aplicado | grafana-es (prometheus-stack/), gitlab-postgres-es:7 + gitlab-redis-es:8, artifactory-es:7, jenkins-es:7, sonarqube-es:7 — todos en `resources:` ANTES de `helmCharts:`; namespaces coinciden (grafana-es ns monitoring ✓); config/ ya no los referencia; quedan en config/ solo traefik-dashboard-auth, redis-insight-auth, redis-push-secret (correcto) | builds rc=0 en las 10 carpetas afectadas |
| **F06** red núcleo (cert-manager + ESO) | ✓ aplicado | cert-manager-network-policy.yaml (CNP `cert-manager-minimal-boot`, egress apiserver 443/6443/8443, webhook 6443, probes 6080/8080, metrics 9402); external-secret-network-policy.yaml reescrito como CNP `eso-zero-trust` (Vault 8200 + apiserver + webhook 8081 + metrics 8080) | builds rc=0 |
| **F07** traefik-to-alertmanager | ✓ aplicado | config/traefik/traefik-to-alertmanager.yaml existe y está en kustomization.yaml:18 (selector alertmanager, 9093) | build OK (tras simular fix A) |
| **F08** v1beta1 → v1 | ✓ aplicado | `grep external-secrets.io/v1beta1` = **0 matches**; generadores Password v1alpha1 y PushSecret intactos | grep ejecutado = 0 |
| **F09** Jenkins versiones | ✓ aplicado (con desviación documentada) | jenkins-values.yaml:18 `2.555.1-jdk21`, :129 agente `3355.v388858a_47b_33-19`, 8 plugins fijados (:52-88, incl. gitlab-plugin 1.2162, docker-workflow 653.v2f…) | build rc=0; 0 `lts`/`latest` |
| **F10** SM selector explícito | ✓ aplicado | prometheus-stack-values.yaml:14 `serviceMonitorSelectorNilUsesHelmValues: false` | build rc=0 |
| **F11** BBDD/roles Postgres | ✓ aplicado | app-roles-es.yaml (3 ES: gitlab/sonarqube/artifactory-db-credentials), postgres-app-init-job.yaml (hook PostSync + BeforeHookCreation :13-14, `\gexec` idempotente), postgres-app-init-network-policy.yaml (egress 5432), kustomization resources:9-11 | build rc=0 |
| **F12** ELK labels + filebeat | ✓ aplicado | elk-clients-network-policy.yaml (CNP kibana/filebeat con `app:` reales + egress filebeat→apiserver 443/6443/8443); elk-network-policy.yaml:9/20/29 (`app: elasticsearch-master`/`kibana`/`filebeat`); scrape-policy:80-97 (3 reglas con namespace+labels reales); SMs kibana:11 y es:11; **filebeat-pod-monitor.yaml** (PodMonitor, portNumber 5066) + kustomization actualizado | builds rc=0 |
| **F13** semilla Vault | ✓ aplicado | bootstrap.sh:285-327 `vault_seed_kv` (espera motor `secret/` :296-300, una sola `kv put` por path :314, `sys/tools/random` :303, valida vacíos :311-313, 8 paths/12 claves exactos); llamada en :544 tras `restart_vault_config_operator` (:543) | `bash -n` ✓ |
| **F14** DNS L7 clusterwide | ✓ aplicado | clusterwide-dns-allowlist.yaml (CCNP, solo `*cluster.local`, excluye kube-system); `allow-dns` eliminado de los 11 namespaces (grep = 0); cableado foundation/kustomization.yaml:28; excepción repo-server ya existente en argocd-network-policy.yaml:160+ (matchName github.com…) | build Foundation rc=0 |
| **F15** CoreDNS protegido | ✓ aplicado | coredns-ingress-protection.yaml (CCNP; label `k8s:io.cilium.k8s.namespace.labels.part-of` :18, DNS también desde kube-system/host/remote-node :20-27, probes 8080/8181, metrics 9153) | build Foundation rc=0 |
| **F16** cluster-issuer duplicado | ✓ aplicado | components/cert-manager/ solo kustomization + values + CNP (+README, pendiente 5.4); `selfsigned-issuer` solo en platform-ca.yaml | grep = 1 fuente |
| **F17** README Vault | ✓ aplicado | 18 refs `secrets-manager`, 0 refs `security`/`vault` como namespace | grep = 0 |
| **F18** comentario secrets-manager | ✓ aplicado | namespaces/secrets-manager.yaml:57 `podSelector: {} # … DENTRO de 'secrets-manager'` | lectura directa |
| **F19** README Foundation | ✓ aplicado | README reescrito con inventario real (11 namespaces+Quota+LimitRange+default-deny, storage-classes, 2 CCNP, requisito `--cni=cilium`) | lectura directa |
| **F20** TLS wave 19 | ✓ aplicado | config/platform-tls/ (platform-ca.yaml + platform-tls.yaml + kustomization SIN namespace raíz); wrapper 19-platform-tls.yaml (wave 19, sin destination.namespace, SkipDryRun) | build rc=0; wave 19 == prefijo |
| **F21** IngressRoutes websecure | ✓ aplicado | 16 rutas con `tls: {}` + `entryPoints: websecure`; 0 rutas en `web` | grep contados |
| **F22** Cilium en docs | ✓ diferido (según decisión) | k8s/README + Foundation README documentan `--cni=cilium`; resto de docs diferido a Fase 5 | — |
| **F23** mysql eliminado | ✓ aplicado | components/data-services/ddbb/ contiene solo `postgres/` | ls verificado |

**R1-R7 (correcciones post-revisión): todas verificadas aplicadas:**

| ID | Evidencia |
|---|---|
| R1 | label corregida :18; DNS desde kube-system + host/remote-node :20-27; egress Prometheus→CoreDNS 9153 en scrape-policy:159-162 |
| R2 | `enableDefaultDeny: {egress:false, ingress:false}` en clusterwide-dns-allowlist.yaml:14; 0 reglas DNS L4 residuales en las 4 CNPs (grep port "53" = solo argocd-network-policy, que es la excepción intencional); selector trivy → trivy-operator (:11) |
| R3 | verificado íntegro en F13 (espera motor, put única, sys/tools/random, tras restart VCO) |
| R4 | Job hook PostSync + BeforeHookCreation (:13-14); ingress `app: postgres-app-init` en cnpg-instances-bootstrap (cnpg-network-policy.yaml:70-77) |
| R5 | gitlab `https: true` (values:11); jenkinsUrl https (jenkins-values:40); traefik redirection → websecure (traefik-values:55-57) |
| R6 | bootstrap-uninstall.sh:50 CLUSTERWIDE_POLICIES = las 2 CCNP + delete en :340-341; `bash -n` ✓ |
| R7 | vault-audit.yaml SA `default` (:14-16), policy `sys/audit` con sudo (bootstrap.sh:269-270), `auditStorage` (vault-values:30), `file_path: /vault/audit/audit.log` (:18) |
| **R8** | **SIGUE ABIERTO (decisión del usuario)**: config/jenkins/jenkins-network-policy.yaml sigue siendo NetworkPolicy estándar (wave 32) sin egress a kube-apiserver ni ingress 50000 de agentes; en el arranque solo existe default-deny → **Jenkins no booteará con plugins** (necesita updates.jenkins.io). Es el único bloqueante de despliegue que NO es un bug |

---

## 4. Barrido completo de builds (gate ejecutado por primera vez)

```bash
for k in $(find k8s -name kustomization.yaml | sort); do
  kustomize build --enable-helm $(dirname $k) > /dev/null || echo "FAIL $k"
done
```

**Resultado: 37/39 OK.** FAIL: `components/repositories/gitlab` (hallazgo B) y `config/traefik` (hallazgo A). Los otros 37 — incluidos los 3 que fallaban en la primera validación (postgres N1 ✓, trivy N3 ✓; gitlab N2 sigue) — compilan con sus charts reales descargados.

**Gates acumulados:**
- G0: 37/39 (falta cerrar A y B — 3 líneas de edición en total, ya simuladas rc=0)
- G1: `grep v1beta1` = 0 ✓ · `bash -n bootstrap.sh` ✓ · `bash -n bootstrap-uninstall.sh` ✓
- G2 (parte estática): 0 contraseñas literales en YAML (todas las coincidencias `password` son `property:` de remoteRef, `${env:…}` o templates htpasswd) ✓ · RabbitMQ sin `default_user` (solo el comentario que lo documenta) ✓
- G3: ROADMAP reconciliado ✓ · ROADMAP-fixes NO puede retirarse todavía (F03 abierto + 2 hallazgos nuevos + validaciones ☸️)

---

## 5. Invariantes del proyecto (todos en verde)

| Invariante | Resultado |
|---|---|
| Cero credenciales en Git | ✓ (escaneo G2) |
| Cero `ipBlock 0.0.0.0/0` (código del repo) | ✓ 0 matches (los 12 hits están dentro de `charts/` descargados por el tooling — ignorar y no commitear) |
| Cero `namespaceSelector: {}` | ✓ 0 matches (ídem) |
| App of Apps puro (38 Application + root; 0 ApplicationSet) | ✓ root con `directory.recurse: true` + `exclude: root-app-of-apps.yaml` |
| wave == prefijo numérico en los 38 wrappers | ✓ 38/38 verificado programáticamente; CreateNamespace+ServerSideApply en todos; paths apuntan a carpetas reales |
| Dominios `*.dev.lab` | ✓ 0 rutas con `desarrollo` como host (residuales cosméticos ya documentados: comentarios "entorno de desarrollo" y `admin@desarrollo.edu` en pgadmin-values — email, no dominio) |
| storageClass unificada | ✓ 0 `standard` en código propio |
| Filebeat HTTP intacto | ✓ `http://elasticsearch-master:9200` (NO tocado — regla respetada) |
| bootstrap.sh extendido, no recreado | ✓ flujo completo: valida Cilium (CRD :91-96) → Foundation (`kubectl apply -k` :433) → CNPs redis/argocd (:444-445) → Redis → ArgoCD → root-app (:526) → Vault init/unseal/seed_auth/restart VCO/seed_kv (:538-544) |
| Regla de ubicación components/config | ✓ (F05 aplicado; solo config-para-config en config/) |

**Re-verificación de los ítems [x] del ROADMAP principal (muestra quirúrgica 1.20-1.42):** E2 path corregido (04-vault-config.yaml:15 → `k8s/platform/config/vault-config-operator`) ✓ · E3 `targetNamespaces` sin doble key (kubernetes-auth-role.yaml:17) ✓ · 1.23 huérfanos `config/redis/redis-insight-*.yaml` eliminados (config/redis solo tiene from-gitlab, push-secret, service-monitor) ✓ · 1.24 postgres-pod-monitor.yaml ✓ · 1.25 vault-ingress-route en config/vault ✓ · 1.26 los 4 service names (pgadmin + fullnameOverride:5, artifactory + fullnameOverride:5, kibana:16, sonarqube:16) ✓ · 1.27 labels `gitlab-webservice-default`/`gitlab-sidekiq-all` (:18,:21) ✓ · 1.28 gitlab-postgres-es ✓ · 1.29 jaeger `secretKeyRef` + `${env:ELASTIC_PASSWORD}` (:19-22,:71,:80) ✓ · 1.30 kibana `http://` (:17,:28) ✓ · 1.31 trivy egress CNP ✓ · 1.32 rabbitmq namespace `data-services` ✓ · 1.33 Quota 1000m/2.5Gi/2000m/4Gi ✓ · 1.34/1.35 puertos metrics/http ✓ · 1.36 los 4 serviceMonitor/podMonitor `enabled: false` ✓ · 1.37 CreateNamespace en 18-redis-insight ✓ · 1.38-1.41 READMEs/comentarios ✓ · 1.42 (F20/F21) ✓.

---

## 6. Puntos faltantes / que se están pasando por alto (para continuar el desarrollo)

**Bloqueantes inmediatos de despliegue (2 ediciones, ~3 líneas):**
1. **A:** borrar el carácter `z` en `k8s/platform/config/traefik/kustomization.yaml:5`.
2. **B:** borrar las líneas 73-75 (bloque raíz `minio:`) de `k8s/platform/components/repositories/gitlab/gitlab-values.yaml`.
3. Tras 1+2, re-correr el barrido → **39/39** y actualizar ROADMAP-fixes (F03 se cierra de verdad; añadir F24-A/F24-B o un bloque "hallazgos 3ª validación").

**Tooling (imprescindible para que el usuario pueda validar solo):**
4. Instalar helm 3.x para kustomize (hallazgo C); documentarlo en `k8s/README.md` o en las notas del ROADMAP; añadir `charts/` a `.gitignore`.

**Decisiones abiertas que congelan funcionalidad (no bugs):**
5. **R8 — Jenkins:** sin allowlist FQDN (`updates.jenkins.io`, `get.jenkins.io`/mirrors, o imagen con plugins horneados) Jenkins no arranca tras 2.555.1-jdk21. Es la PRÓXIMA decisión que hay que tomar; el resto de la plataforma ya no tiene bloqueantes 🖥️.
6. **F12b — exporter de métricas de Elasticsearch** (prometheus-exporter plugin vs degradar el SM).
7. **1.2e — salidas externas:** Artifactory (upstreams Maven/npm), Trivy (DB de vulnerabilidades: ghcr.io/mirror.gcr.io vs offline), imágenes por nodo / Artifactory como mirror de registros.

**Fase 2 ☸️ (nada de esto puede marcarse hasta volver el cluster):**
8. 2.1-2.3: uninstall + bootstrap limpio con los scripts nuevos (ahora sí con las semillas F13 y las CNPs del núcleo F06), confirmar cert-manager/ESO/Vault/VCO/vault-config/ClusterSecretStore Healthy SIN pasos manuales, completar la validación de `config/argocd/` y `config/redis/`.
9. Validaciones ☸️ acumuladas de ROADMAP-fixes (puertos reales de webhooks con `kubectl get endpoints`, labels con `--show-labels`, Hubble DNS L7 DROPPED, filebeat→apiserver, `externalsecret -A` Ready, `psql -l` con las 3 BBDD, Certificate Ready + https servido, Hubble allowlist repo-server).
10. R7-Aviso operativo: `auditStorage` cambia `volumeClaimTemplates` → en un cluster YA desplegado hay que recrear el StatefulSet (`kubectl delete sts vault -n secrets-manager --cascade=orphan`); en bootstrap limpio no aplica.

**Largo plazo (sin cambios respecto al ROADMAP principal):**
11. Fase 5 completa de `docs/` (plantilla por componente, migración de screenshots, retirada de los README.md de `components/*/` — p. ej. `components/cert-manager/README.md` sigue presente, previsto en 5.4/F22).
12. 4.2 Reloader, 4.3 bootstrap-uninstall (parcialmente hecho con R6), 4.4 login humano Vault, 4.5 Velero+MinIO+snapshots Raft, 4.6 TLS interno.
13. OTLP 4317/4318 de Jaeger: cerrado por diseño hasta el primer emisor.

**Higiene de tracking:**
14. Re-baselinar los gates de "43/43" a "39/39" (observación D).
15. En ROADMAP-fixes, F03/F05/F08/F09 ya anotaron desviaciones correctas respecto al prompt (10 ES movidos no 9; 5 archivos migrados no 6; jdk21 no jdk17; 8 plugins no 5) — mantener esa disciplina: **la evidencia de F03 debe corregirse cuando se aplique el fix real**.

---

## 7. Reproducibilidad

```bash
# Requisito: helm 3.x en el PATH para kustomize (helm 4 rompe 'helm version -c')
# (helm 4.2.2 puede seguir usándose para bootstrap.sh; kustomize NO lo soporta)

# Gate de barrido (esperado tras cerrar A y B: 39/39, 0 FAIL):
for k in $(find k8s -name kustomization.yaml | sort); do
  echo "== $k"; kustomize build --enable-helm $(dirname $k) > /dev/null || echo "FAIL $k"
done

# F01 — CRDs renderizados (>0 cada uno):
kustomize build --enable-helm k8s/platform/components/security/external-secrets | grep -c 'kind: CustomResourceDefinition'   # 23
kustomize build --enable-helm k8s/platform/components/networking/traefik | grep -c 'kind: CustomResourceDefinition'          # 29
kustomize build --enable-helm k8s/platform/components/monitoring/prometheus-stack | grep -c 'kind: CustomResourceDefinition' # 10
kustomize build --enable-helm k8s/platform/components/security/trivy | grep -c 'kind: CustomResourceDefinition'              # 12

# F03 (tras el fix): 
kustomize build --enable-helm k8s/platform/components/repositories/gitlab > /dev/null && echo OK

# Chequeos de cierre:
grep -rn "external-secrets.io/v1beta1" k8s | wc -l         # 0
grep -rn "0.0.0.0/0\|namespaceSelector: {}" k8s --include='*.yaml' | grep -v charts | wc -l   # 0
bash -n k8s/bootstrap.sh && bash -n k8s/bootstrap-uninstall.sh   # OK (ya validados)
```

---

## 8. Estado objetivo

| Métrica | Primera validación (18-11) | Tercera validación (19-11, este informe) | Tras cerrar A+B |
|---|---|---|---|
| Kustomizations OK | 40/43 (3 FAIL, sin ejecutar gates) | **37/39 ejecutado con charts reales** | 39/39 |
| CRDs renderizados (ESO/traefik/KPS/trivy) | 0/4 | **4/4** | 4/4 |
| Credenciales de arranque en la wave correcta | 0/9 | **10/10 ES** en components/ | ídem |
| Semillas Vault | 0 | **8 paths / 12 claves** + audit operativo | ídem |
| TLS wave 19 + websecure | 0 | **16 rutas migradas + TLSStore** | ídem |
| Bloqueantes de despliegue restantes | 3 builds + 4 charts sin CRDs + inversión de waves | **2 (3 líneas) + R8 (decisión)** | 0 🖥️ (queda R8 y ☸️) |

**La plataforma está a 3 líneas de código y 1 decisión (R8) de estar lista para el re-bootstrap en cluster.**
