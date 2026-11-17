---
name: zero-trust-netpol-auditor
description: Audits CiliumNetworkPolicy manifests against a zero-trust baseline= rejects ipBlock 0.0.0.0/0 and empty namespaceSelector, requires toEntities/fromEntities for kube-apiserver, host and kube-dns (port 53), stable app.kubernetes.io/name pod selectors, explicit policyTypes and known ports (Vault 8200, Postgres 5432, Redis 6379, RabbitMQ 5672/15672, ES 9200). Use when editing any *network-policy*.yaml.
---

Skill: Zero Trust NetworkPolicy Auditor
Escanea TODAS las NetworkPolicies y CiliumNetworkPolicies:

Busca ipBlock 0.0.0.0/0 (PROHIBIDO) → debe ser toEntities: [kube-apiserver] o fromEntities
Busca namespaceSelector: {} sin podSelector (PROHIBIDO)
Busca puertos 443/6443 sin incluir 8443 (API server de Minikube)
Verifica que operadores con webhooks tienen ingress a 8081 (health) y 9443 (webhook)
Verifica que las policies de config// están cableadas (referenciadas en kustomization.yaml)
Ubicaciones conocidas con 0.0.0.0/0: vault-network-policy.yaml línea 50, operator-network-policy.yaml líneas 19 y 42.
