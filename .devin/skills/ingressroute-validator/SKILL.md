---
name: ingressroute-validator
description: Validates Traefik IngressRoute manifests = service names must match real Kubernetes Services in the same namespace, domains restricted to *.dev.lab (never *.desarrollo), correct entryPoints, TLS and routing rules. Use when editing any IngressRoute or ingress *.yaml.
---

Skill: IngressRoute Service Name Validator
Valida que los service names en IngressRoutes coincidan con los releases de Helm:

Lee cada kustomization.yaml en components/ y extrae releaseName
Lee cada IngressRoute en config/ y extrae services[].name
Cross-reference: el service name debe ser = releaseName o fullnameOverride
Mismatches conocidos (auditoría):

pgadmin-ingress-route.yaml línea 15: pgadmin-pgadmin4 (real: pgadmin4-v1-62-0) → fix: fullnameOverride: pgadmin
artifactory-ingress-route.yaml línea 15: artifactory (real: artifactory-oss) → fix: fullnameOverride: artifactory
kibana-ingress-route.yaml línea 14: kibana-kibana (real: kibana) → fix: cambiar a kibana
sonarqube-ingress-route.yaml línea 14: sonarqube-sonarqube (real: sonarqube) → fix: cambiar a sonarqube
También valida dominios: *.desarrollo es PROHIBIDO, usar *.dev.lab.