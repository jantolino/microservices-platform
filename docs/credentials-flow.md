# Flujo de credenciales de la plataforma

Regla de oro: **ninguna credencial en Git**. Fuente única: Vault. Este documento describe el ciclo de vida completo de una credencial en la plataforma.

## Arquitectura del flujo

```
bootstrap.sh (semilla) ──► Vault (fuente única)
                              │
                              ▼
              External Secrets Operator (ESO)
              ClusterSecretStore: vault-backend
                              │
                              ▼
                 Secret de Kubernetes (namespace destino)
                              │
                              ▼
        values.yaml (existingSecret / secretKeyRef) ──► Componente
```

## 1. Semilla: `bootstrap.sh`

- Genera contraseñas **aleatorias** y las siembra en Vault **solo si no existen** (idempotente).
- Las claves de unseal de Vault se guardan en `k8s/.secrets/` (ignorado por Git).
- Excepciones temporales (mientras sigan en bootstrap): ArgoCD y Redis.

## 2. Fuente única: Vault

Rutas KV v2 (`secret/data/platform/...`):

| Ruta | Propiedades | Consumidores |
|------|-------------|--------------|
| `secret/data/platform/databases/postgres` | `password` | GitLab, Artifactory, SonarQube, pgAdmin |
| `secret/data/platform/databases/artifactory` | `password` | Artifactory |
| `secret/data/platform/tools/artifactory` | `masterKey`, `joinKey` | Artifactory |
| `secret/data/platform/tools/rabbitmq` | `default_user`, `default_pass` | (reservado; hoy el operador genera credenciales) |
| `secret/data/platform/tools/grafana` | `adminPassword` | Grafana |

## 3. Sincronización: External Secrets Operator

Cada `ExternalSecret` (en `k8s/platform/config/<componente>/`) lee de Vault mediante el `ClusterSecretStore` `vault-backend` y crea un Secret de Kubernetes con `creationPolicy: Owner` y `refreshInterval: 15m`.

| ExternalSecret | Namespace | Secret generado | Claves |
|----------------|-----------|-----------------|--------|
| `gitlab-redis-password` | repositories | `argocd-redis-auth` | `redis-password` |
| `gitlab-postgres-password` | repositories | `gitlab-postgres-password` | `password` |
| `artifactory-database` | devops-tools | `artifactory-database` | `db-password` |
| `artifactory-masterkey` | devops-tools | `artifactory-masterkey` | `master-key` |
| `artifactory-joinkey` | devops-tools | `artifactory-joinkey` | `join-key` |
| `artifactory-admin-credentials` | devops-tools | `artifactory-admin-credentials` | `admin-password` |
| `pgadmin-secrets` | data-services | `pgadmin-secrets` | `PGADMIN_DEFAULT_PASSWORD` |

## 4. Consumo: values.yaml

Los values de los charts **nunca** contienen contraseñas; referencian Secrets:

```yaml
# Ejemplo: Artifactory (database.secrets)
database:
  user: artifactory
  secrets:
    password:
      name: artifactory-database
      key: db-password

# Ejemplo: Jaeger v2 (env var + ${env:} en config)
jaeger:
  extraEnv:
    - name: ELASTIC_PASSWORD
      valueFrom:
        secretKeyRef:
          name: elasticsearch-master-elastic-password
          key: elastic-password
```

## Casos especiales

- **Elasticsearch**: el chart elastic auto-genera `elasticsearch-master-elastic-password` (sin Vault). Jaeger lo consume vía `secretKeyRef`.
- **RabbitMQ**: sin `default_user`/`default_pass` en el CR, el operador genera credenciales aleatorias en el Secret `rabbitmq-default-user` (namespace `data-services`). Un ExternalSecret no puede inyectar en `additionalConfig` del CR.
- **Redis**: Secret `redis-auth` generado por `bootstrap.sh` (excepción temporal hasta migrar a Vault).
- **Vault**: la propia plataforma se autogestiona; unseal keys solo en `k8s/.secrets/`.

## Verificación

```bash
# Estado de los ExternalSecrets
kubectl get externalsecrets -A

# Secret sincronizado
kubectl get secret artifactory-database -n devops-tools -o jsonpath='{.data.db-password}' | base64 -d
```
