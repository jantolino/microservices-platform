---
name: component-checklist-runner
description: Runs the phase 3 component onboarding checklist for the microservices-platform repo= namespace, ExternalSecrets, CiliumNetworkPolicies, IngressRoute, Helm values, ArgoCD wrapper, ServiceMonitors and documentation, verified in order. Use when adding or reviewing a platform component (traefik, vault, postgres, redis, rabbitmq, elasticsearch...).
---

Skill: Component Checklist Runner
Ejecuta el checklist estándar de Fase 3 del ROADMAP para un componente:

Depurar components/: solo chart, values, ExternalSecret de arranque, NetworkPolicy mínima
Fijar versiones (sin latest/lts)
Verificar recursos vs ResourceQuota del namespace
Sembrar secretos en Vault (ver secret-scanner para paths)
Crear wrapper NN-.yaml con wave alineada
Completar config//: IngressRoute + TLS, NetworkPolicies de integración, wrapper config-
Mover README a docs/components/.md
Validar: kustomize build --enable-helm
Al finalizar, marcar [x] en tasks/ROADMAP.md.
