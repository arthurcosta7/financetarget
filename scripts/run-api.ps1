$ErrorActionPreference = 'Stop'

$ProjectRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot 'load-env.ps1') -Path (Join-Path $ProjectRoot '.env')

Push-Location (Join-Path $ProjectRoot 'apps\api')
try {
    & '.\mvnw.cmd' spring-boot:run '-Dspring-boot.run.profiles=dev'
}
finally {
    Pop-Location
}
