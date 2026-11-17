---
name: sync-wave-auditor
description: Audits ArgoCD Application wrappers and their repo paths= sync-wave annotation must match the numeric file prefix (config >= 20, components 6-18), path must exist in the repository, kind Application never ApplicationSet, syncOptions CreateNamespace+ServerSideApply, resources-finalizer, destination namespace and repo jantolino/microservices-platform. Use when editing k8s/bootstrap/03-platform-orchestration/*.yaml.
---

Skill: Sync-Wave Auditor
Audita los wrappers Application en k8s/bootstrap/03-platform-orchestration/:

Lista todos los archivos NN-*.yaml
Verifica que el prefijo numérico del archivo coincida con la anotación argocd.argoproj.io/sync-wave
Verifica que cada path apunte a una carpeta que EXISTE (components/ o config/)
Detecta wrappers con CreateNamespace faltante
Detecta config folders sin wrapper (los 18 pendientes de la tarea 1.22)
Errores conocidos: 04-vault-config.yaml apunta a config/vault-config (incorrecto, real: config/vault-config-operator).
