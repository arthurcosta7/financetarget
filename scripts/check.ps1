$ErrorActionPreference = 'Stop'

$ProjectRoot = Split-Path -Parent $PSScriptRoot

Push-Location $ProjectRoot
try {
    pnpm generate:api
    pnpm lint
    pnpm typecheck
    pnpm test:ci
    pnpm build
    & (Join-Path $ProjectRoot 'apps\api\mvnw.cmd') `
        --batch-mode `
        --file (Join-Path $ProjectRoot 'apps\api\pom.xml') `
        verify
    git diff --check
}
finally {
    Pop-Location
}
