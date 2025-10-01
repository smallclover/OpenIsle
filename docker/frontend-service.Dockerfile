# ==== builder ====
FROM node:20-bullseye AS builder
WORKDIR /app

# 通过构建参数选择环境：staging / production（默认 staging）
ARG NUXT_ENV=staging
ENV NODE_ENV=production \
    NUXT_TELEMETRY_DISABLED=1

# 复制源代码（假设仓库根目录包含 frontend_nuxt）
# 构建上下文由 docker-compose 指向仓库根目录
COPY ./frontend_nuxt/package*.json /app/
RUN npm ci

# 拷贝剩余代码
COPY ./frontend_nuxt/ /app/

# 若存在环境样例文件，则在构建期复制为 .env（你也可以用 --build-arg 覆盖）
RUN if [ -f ".env.${NUXT_ENV}.example" ]; then cp ".env.${NUXT_ENV}.example" .env; fi

# 构建 SSR：产物在 .output
RUN npm run build

# ==== runner ====
FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production \
    NUXT_TELEMETRY_DISABLED=1 \
    PORT=3000 \
    HOST=0.0.0.0

# 复制构建产物
COPY --from=builder /app/.output /app/.output

# 健康检查（简洁起见，探测首页）
HEALTHCHECK --interval=10s --timeout=5s --retries=30 CMD wget -qO- http://127.0.0.1:${PORT}/ >/dev/null 2>&1 || exit 1

EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
