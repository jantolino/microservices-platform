#!/usr/bin/env bash
# Descubre los dominios que el argocd-repo-server necesita resolver para descargar los charts Helm
# (repo, redirecciones del index.yaml y URLs de descarga de los .tgz) y los compara con la allowlist
# de bootstrap/02-argocd/argocd-network-policy.yaml. Imprime los que faltan.
#
# Uso:    bash k8s/discover-helm-domains.sh
# Salida: código 0 si la allowlist cubre todo; código 1 si faltan dominios.
# Requiere: curl, acceso a Internet. No modifica ningún archivo ni el clúster.
set -euo pipefail
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLATFORM_DIR="$SCRIPT_DIR/platform"
POLICY_FILE="$SCRIPT_DIR/bootstrap/02-argocd/argocd-network-policy.yaml"

# Extrae el host (en minúsculas) de cada URL recibida por stdin.
host_of() { sed -E 's#^https?://([^/:"]+).*#\1#' | tr 'A-Z' 'a-z'; }

# tr -d '\r': los archivos en /mnt/<unidad> (WSL) pueden tener finales de línea CRLF.
mapfile -t REPOS < <(
  grep -rhoE 'repo: *https?://[^ ]+' "$PLATFORM_DIR" --include=kustomization.yaml \
    | tr -d '\r' | awk '{print $2}' | sort -u
)

ALLOWED="$(mktemp)"
DISCOVERED="$(mktemp)"
trap 'rm -f "$ALLOWED" "$DISCOVERED"' EXIT

grep -hoE 'matchName: *[^ ]+' "$POLICY_FILE" | tr -d '\r' | awk '{print tolower($2)}' | sort -u > "$ALLOWED"

for repo in "${REPOS[@]}"; do
  index="${repo%/}/index.yaml"
  echo "== $repo"
  {
    echo "$repo" | host_of
    # Redirecciones del index.yaml (p. ej. charts.external-secrets.io -> external-secrets.io)
    curl -sSIL --max-time 20 "$index" | tr -d '\r' | grep -i '^location:' | awk '{print $2}' | host_of || true
    # Dominios de descarga: solo el campo "urls:" de cada versión (el resto son enlaces informativos)
    curl -sSL --max-time 20 "$index" | tr -d '\r' \
      | grep -A1 -E '^[[:space:]]+urls:' | grep -oE 'https?://[^/" ]+' | host_of || true
  } | sort -u | tee -a "$DISCOVERED" | sed 's/^/   /'
done

echo
echo "== Dominios que faltan en la allowlist ($(basename "$POLICY_FILE"))"
MISSING="$(sort -u "$DISCOVERED" | comm -23 - "$ALLOWED")"
if [ -z "$MISSING" ]; then
  echo "   Ninguno: la allowlist cubre todos los dominios descubiertos."
  exit 0
fi
echo "$MISSING" | sed 's/^/   - /'
echo
echo "Añade cada dominio como 'matchName' en las DOS listas de la política (dns.rules y toFQDNs)."
exit 1
