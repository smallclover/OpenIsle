#!/usr/bin/env bash
set -euo pipefail

cd /app

echo "👉 Building frontend (Nuxt SSR)..."

if [ -f .env.production.example ] && [ ! -f .env ]; then
  echo "📄 Copying .env.production.example to .env"
  cp .env.production.example .env
fi

npm ci
npm run build

echo "🧪 Smoke-testing: nuxt generate (artifacts will NOT be used)..."

SSR_OUTPUT_DIR=".output"
SSR_OUTPUT_BAK=".output-ssr-backup-$$"
GEN_FAIL_MSG="❌ Generate smoke test failed"

if [ ! -d "${SSR_OUTPUT_DIR}" ]; then
  echo "❌ 未发现 ${SSR_OUTPUT_DIR}，请先确保 npm run build 成功执行"
  exit 1
fi

mv "${SSR_OUTPUT_DIR}" "${SSR_OUTPUT_BAK}"

restore_on_fail() {
  if [ -d ".output" ]; then
    mv .output ".output-generate-failed-$(date +%s)" || true
  fi
  mv "${SSR_OUTPUT_BAK}" "${SSR_OUTPUT_DIR}"
}

trap 'restore_on_fail; echo "${GEN_FAIL_MSG}: unexpected error"; exit 1' ERR

NUXT_TELEMETRY_DISABLED=1 \
NITRO_PRERENDER_FAIL_ON_ERROR=1 \
npx nuxi generate --preset static

if [ ! -d ".output/public" ]; then
  restore_on_fail
  echo "${GEN_FAIL_MSG}: .output/public not found"
  exit 1
fi

rm -rf ".output"
mv "${SSR_OUTPUT_BAK}" "${SSR_OUTPUT_DIR}"
trap - ERR
echo "✅ Generate smoke test passed."

if [ -d ".output/public" ]; then
  mkdir -p /var/www/openisle
  rsync -a --delete .output/public/ /var/www/openisle/
else
  echo "❌ 未发现 .output/public；检查 nuxt.config.ts/nitro preset"
  exit 1
fi

echo "🚀 Starting Nuxt SSR server..."
exec node .output/server/index.mjs
