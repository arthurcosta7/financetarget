# syntax=docker/dockerfile:1.8
FROM node:24.14.1-alpine@sha256:8510330d3eb72c804231a834b1a8ebb55cb3796c3e4431297a24d246b8add4d5 AS build

WORKDIR /workspace
ENV NEXT_TELEMETRY_DISABLED=1
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/web/package.json apps/web/package.json
RUN corepack pnpm install --frozen-lockfile
COPY apps/web apps/web
RUN corepack pnpm --filter @financetarget/web build

FROM node:24.14.1-alpine@sha256:8510330d3eb72c804231a834b1a8ebb55cb3796c3e4431297a24d246b8add4d5 AS runtime

ARG VCS_REF
LABEL org.opencontainers.image.title="FinanceTarget Web" \
      org.opencontainers.image.source="https://github.com/arthurcosta7/financetarget" \
      org.opencontainers.image.revision="${VCS_REF}"

ENV NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1 \
    HOSTNAME=0.0.0.0 \
    PORT=3000
WORKDIR /app
COPY --from=build --chown=node:node /workspace/apps/web/.next/standalone ./
COPY --from=build --chown=node:node /workspace/apps/web/.next/static ./apps/web/.next/static

EXPOSE 3000
USER node
HEALTHCHECK --interval=15s --timeout=3s --start-period=15s --retries=5 \
  CMD wget -q -O - http://127.0.0.1:3000/ || exit 1
CMD ["node", "apps/web/server.js"]
