---
name: component-doc-generator
description: Generates per-component documentation (README) for the platform= purpose, chart name and exact version, namespace, *.dev.lab URLs, required Vault secrets (secret/data/platform/...), network policies and sync-wave. Follows the existing docs structure. Use after onboarding a component or when its configuration changes.
---

Skill: Component Documentation Generator
Genera docs por componente con la plantilla de Fase 5:

Estructura obligatoria: propósito, chart/versión, namespace, wave, dependencias, tabla de comunicación (origen, destino, puerto, protocolo), diagrama Mermaid, secretos (path de Vault), TLS, NetworkPolicies activas, métricas (ServiceMonitor + puerto), URL (*.dev.lab), troubleshooting.

Guardar en docs/components/.md y ELIMINAR el README.md de components//.

Regla de ubicación: components/ = arranque; config/ = posterior; docs/ = documentación.
