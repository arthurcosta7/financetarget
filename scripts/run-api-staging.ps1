param(
    [Parameter(Mandatory = $true)]
    [string]$EnvironmentFile
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ResolvedEnvironmentFile = [IO.Path]::GetFullPath($EnvironmentFile)
. (Join-Path $PSScriptRoot 'load-env.ps1') -Path $ResolvedEnvironmentFile

if ($env:APP_ENV -ne 'staging') {
    throw 'O profile staging exige APP_ENV=staging.'
}

Push-Location (Join-Path $ProjectRoot 'apps\api')
try {
    & '.\mvnw.cmd' spring-boot:run '-Dspring-boot.run.profiles=staging'
}
finally {
    Pop-Location
}
