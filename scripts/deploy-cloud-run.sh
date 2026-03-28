#!/usr/bin/env bash
set -euo pipefail

# Deploy MediBridge to Google Cloud Run (builds from Dockerfile in repo root).
#
# Usage:
#   export GCP_PROJECT="your-project-id"
#   export GEMINI_API_KEY="your-key"   # or use --set-secrets after creating a secret
#   ./scripts/deploy-cloud-run.sh
#
# Optional:
#   REGION=us-central1 SERVICE=medibridge ./scripts/deploy-cloud-run.sh

REGION="${REGION:-us-central1}"
SERVICE="${SERVICE:-medibridge}"

if [[ -z "${GCP_PROJECT:-}" ]]; then
  GCP_PROJECT="$(gcloud config get-value project 2>/dev/null)" || true
fi
if [[ -z "${GCP_PROJECT:-}" || "${GCP_PROJECT}" == "(unset)" ]]; then
  echo "Set GCP_PROJECT or run: gcloud config set project YOUR_PROJECT_ID" >&2
  exit 1
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

gcloud config set project "$GCP_PROJECT"

# Cloud Run + Artifact Registry APIs
gcloud services enable run.googleapis.com artifactregistry.googleapis.com cloudbuild.googleapis.com --quiet

ENV_FLAGS=()
if [[ -n "${GEMINI_API_KEY:-}" ]]; then
  ENV_FLAGS+=(--set-env-vars="GEMINI_API_KEY=${GEMINI_API_KEY}")
else
  echo "Warning: GEMINI_API_KEY not set; triage will fall back unless you use Secret Manager (--set-secrets)." >&2
fi

gcloud run deploy "$SERVICE" \
  --project="$GCP_PROJECT" \
  --region="$REGION" \
  --source=. \
  --allow-unauthenticated \
  "${ENV_FLAGS[@]}"

echo "Done. Service URL:"
gcloud run services describe "$SERVICE" --region="$REGION" --format='value(status.url)'
