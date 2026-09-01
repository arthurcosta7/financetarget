param(
    [string]$Revision,
    [string]$Tag
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ComposeFile = Join-Path $ProjectRoot 'ops\deployment\compose.release-validation.yml'

Push-Location $ProjectRoot
try {
    if ([string]::IsNullOrWhiteSpace($Revision)) { $Revision = (git rev-parse HEAD).Trim() }
    if ($Revision -notmatch '^[a-f0-9]{40}$') { throw 'Revision deve ser o SHA Git completo.' }
    if ([string]::IsNullOrWhiteSpace($Tag)) { $Tag = $Revision.Substring(0, 12) }

    $env:RELEASE_API_IMAGE = "financetarget/api:$Tag"
    $env:RELEASE_WEB_IMAGE = "financetarget/web:$Tag"
    $env:RELEASE_REVISION = $Revision
    $env:RELEASE_DB_PASSWORD = 'synthetic-' + [Guid]::NewGuid().ToString('N')

    docker compose --file $ComposeFile up --detach --wait
    if ($LASTEXITCODE -ne 0) { throw 'A topologia de validação não ficou saudável.' }

    & (Join-Path $PSScriptRoot 'production-smoke.ps1') `
        -ApiBaseUrl 'http://127.0.0.1:18080' `
        -WebBaseUrl 'http://127.0.0.1:13000' `
        -ExpectedReleaseId $Revision `
        -ExpectedSchemaVersion '7' `
        -AllowHttpForLocalValidation
}
finally {
    docker compose --file $ComposeFile down --volumes --remove-orphans 2>$null
    Remove-Item Env:RELEASE_API_IMAGE,Env:RELEASE_WEB_IMAGE,Env:RELEASE_REVISION,Env:RELEASE_DB_PASSWORD -ErrorAction SilentlyContinue
    Pop-Location
}
