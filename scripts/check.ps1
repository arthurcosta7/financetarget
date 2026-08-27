$ErrorActionPreference = 'Stop'

$ProjectRoot = Split-Path -Parent $PSScriptRoot

Push-Location $ProjectRoot
try {
    corepack pnpm generate:api
    corepack pnpm lint
    corepack pnpm typecheck
    corepack pnpm test:ci
    corepack pnpm build
    & (Join-Path $ProjectRoot 'apps\api\mvnw.cmd') `
        --batch-mode `
        --file (Join-Path $ProjectRoot 'apps\api\pom.xml') `
        verify
    git diff --check
}
finally {
    Pop-Location
}
