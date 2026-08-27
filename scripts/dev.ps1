$ErrorActionPreference = 'Stop'

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$EnvironmentFile = Join-Path $ProjectRoot '.env'

if (-not (Test-Path -LiteralPath $EnvironmentFile)) {
    throw 'Crie .env a partir de .env.example antes de iniciar o ambiente local.'
}

docker compose --env-file $EnvironmentFile up -d --wait postgres

Write-Host 'PostgreSQL local pronto.'
Write-Host 'API: execute .\scripts\run-api.ps1 em outro terminal.'
Write-Host 'Web: execute .\scripts\run-web.ps1 em outro terminal.'
