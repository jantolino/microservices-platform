#!/bin/bash

# Colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Listar todos los componentes de la arquitectura
function list_components() {
    echo -e "${BLUE}=== COMPONENTES DE LA ARQUITECTURA ===${NC}"
    kubectl get all --all-namespaces -l part-of=microservices-architecture
}

# Ver estado de los pods
function pod_status() {
    echo -e "${BLUE}=== ESTADO DE LOS PODS ===${NC}"
    kubectl get pods --all-namespaces -o wide
}

# Ver uso de recursos
function resource_usage() {
    echo -e "${BLUE}=== USO DE RECURSOS ===${NC}"
    kubectl top pods --all-namespaces
}

# Ver eventos del cluster
function cluster_events() {
    echo -e "${BLUE}=== EVENTOS RECIENTES ===${NC}"
    kubectl get events --sort-by='.metadata.creationTimestamp' --field-selector=type!=Normal
}

# Ver recursos de un namespace
function show_namespace() {
    if [ -z "$1" ]; then
        echo -e "${GREEN}Uso: show_namespace <nombre-del-namespace>${NC}"
        return
    fi
    
    local ns=$1
    echo -e "${BLUE}=== RECURSOS EN $ns ===${NC}"
    echo -e "\n${GREEN}PODS:${NC}"
    kubectl get pods -n $ns
    echo -e "\n${GREEN}SERVICIOS:${NC}"
    kubectl get svc -n $ns
    echo -e "\n${GREEN}VOLÚMENES:${NC}"
    kubectl get pvc -n $ns
}

# Mostrar ayuda
function show_help() {
    echo -e "${GREEN}=== UTILIDADES KUBERNETES ===${NC}"
    echo "Comandos disponibles:"
    echo "  list_components    - Lista todos los componentes"
    echo "  pod_status         - Muestra estado de los pods"
    echo "  resource_usage     - Muestra uso de recursos"
    echo "  cluster_events     - Muestra eventos recientes"
    echo "  show_namespace <ns>- Muestra recursos de un namespace"
    echo -e "\nEjemplo: ${BLUE}source utilities.sh && show_namespace event-management${NC}"
}

# Mostrar ayuda al cargar el script
echo -e "${GREEN}Utilidades cargadas. Usa 'show_help' para ver los comandos disponibles.${NC}"
