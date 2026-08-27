$ErrorActionPreference = 'Stop'

$ProjectRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot 'load-env.ps1') -Path (Join-Path $ProjectRoot '.env')

Push-Location $ProjectRoot
try {
    pnpm dev:web
}
finally {
    Pop-Location
}
