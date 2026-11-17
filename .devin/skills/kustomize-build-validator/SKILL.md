---
name: kustomize-build-validator
description: Validates kustomization.yaml files in a Kustomize + Helm setup (kustomize build --enable-helm). Enforces namespace at root, resources before helmCharts, every .yaml referenced in resources, exact chart versions, and patch targets matching releaseName. Use whenever editing kustomization.yaml, helmCharts entries or patches.
---

Skill: Kustomize Build Validator
Cuando el usuario pida validar un componente o antes de cualquier commit:

Ejecuta kustomize build --enable-helm k8s/platform/components/<componente>/
Si falla, analiza el error y propón el fix mínimo
Valida también k8s/platform/config/<componente>/ si existe
Reporta: OK o errores exactos con línea y fix propuesto
NUNCA hacer commit sin validación previa
Regla de oro: un cambio a la vez, validado antes de continuar.
