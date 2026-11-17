---
name: externalsecret-builder
description: Generates ExternalSecret and PushSecret manifests for External Secrets Operator with Vault = apiVersion external-secrets.io/v1, ClusterSecretStore vault-backend, creationPolicy Owner, refreshInterval 15m, remoteRef key secret/data/platform/<category>/<component>, never hardcoded credentials. Use when creating *-es.yaml or push-secret*.yaml.
---

Skill: ExternalSecret Builder
Genera ExternalSecrets siguiendo el patrón del proyecto:

Template (usar external-secrets.io/v1):apiVersion: external-secrets.io/v1kind: ExternalSecretmetadata: name: namespace: spec: refreshInterval: 15m secretStoreRef: name: vault-backend kind: ClusterSecretStore target: name: creationPolicy: Owner data: - secretKey: remoteRef: key: secret/data/platform/<databases|tools>/ property:

Reglas:

Réplica Distribuida: componentes que comparten contraseña apuntan al MISMO Vault path
Guardar SIEMPRE en config// y agregar al kustomization.yaml
Patrón de paths: secret/data/platform/databases/ para DBs, secret/data/platform/tools/ para tools
Jaeger NO usa Vault para elastic: referencia el secret auto-generado elasticsearch-master-elastic-password
