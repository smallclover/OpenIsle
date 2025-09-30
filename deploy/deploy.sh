#!/usr/bin/env bash
set -euo pipefail

# 用法：
#   ./deploy.sh staging
#   ./deploy.sh prod
env="${1:-staging}"          # staging | prod
project="openisle_${env}"

repo_dir="/opt/openisle/OpenIsle-${env}"
compose_file="${repo_dir}/docker/docker-compose.yaml"
env_file="${repo_dir}/.env.${env}"

echo "👉 Enter repo..."
cd "$repo_dir"

echo "👉 Sync & checkout..."
git fetch --all --prune
git checkout -B "main" "origin/main"
git reset --hard "origin/main"

echo "👉 Ensure env file: $env_file"
[ -f "$env_file" ] || { echo "❌ $env_file missing"; exit 1; }

export COMPOSE_PROJECT_NAME="$project"
export ENV_FILE="$env_file"

echo "👉 Validate compose..."
docker compose -f "$compose_file" --env-file "$env_file" config >/dev/null

echo "👉 Pull images..."
docker compose -f "$compose_file" --env-file "$env_file" pull --ignore-pull-failures

echo "👉 Build custom images..."
docker compose -f "$compose_file" --env-file "$env_file" \
  build --pull \
  --build-arg NUXT_ENV="$env" \
  frontend_service opensearch

echo "👉 Up services..."
docker compose -f "$compose_file" --env-file "$env_file" up -d --force-recreate --remove-orphans \
  mysql redis rabbitmq opensearch dashboards websocket-service springboot frontend_service

echo "👉 Status"
docker compose -f "$compose_file" --env-file "$env_file" ps

echo "👉 Prune dangling images"
docker image prune -f

echo "✅ ${env} stack deployed at $(date)"
