param(
    [string]$ApiBaseUrl = 'http://127.0.0.1:8080',
    [string]$WebBaseUrl = 'http://127.0.0.1:3000',
    [string]$ExpectedSchemaVersion = '6'
)

$ErrorActionPreference = 'Stop'

function Invoke-SmokeRequest([string]$Uri, [string]$RequestId) {
    $Response = Invoke-WebRequest -Uri $Uri -Headers @{ 'X-Request-ID' = $RequestId } -UseBasicParsing
    if ($Response.StatusCode -ne 200) { throw "Smoke falhou em $Uri." }
    return $Response
}

function Get-SmokeContent($Response) {
    if ($Response.Content -is [byte[]]) {
        return [Text.Encoding]::UTF8.GetString($Response.Content)
    }
    return [string]$Response.Content
}

$Readiness = Invoke-SmokeRequest "$ApiBaseUrl/actuator/health/readiness" 'staging-readiness'
if (((Get-SmokeContent $Readiness) | ConvertFrom-Json).status -ne 'UP') { throw 'Readiness não está UP.' }
if ([string]$Readiness.Headers['X-Request-ID'] -ne 'staging-readiness') { throw 'Request ID não foi propagado.' }

$Status = Invoke-SmokeRequest "$ApiBaseUrl/api/v1/system/status" 'staging-status'
$StatusBody = (Get-SmokeContent $Status) | ConvertFrom-Json
if ($StatusBody.database.schemaVersion -ne $ExpectedSchemaVersion) { throw 'Versão de schema inesperada.' }
if ($Status.Headers['Cache-Control'] -notmatch 'no-store') { throw 'Status deve impedir cache.' }

$Metrics = Invoke-SmokeRequest "$ApiBaseUrl/actuator/prometheus" 'staging-metrics'
if ((Get-SmokeContent $Metrics) -notmatch 'http_server_requests_seconds') { throw 'Métricas HTTP ausentes.' }

$Web = Invoke-SmokeRequest $WebBaseUrl 'staging-web'
foreach ($Header in @('Content-Security-Policy', 'Strict-Transport-Security', 'X-Content-Type-Options')) {
    if ($null -eq $Web.Headers[$Header]) { throw "Header web ausente: $Header" }
}

[PSCustomObject]@{ ready = $true; schemaVersion = $ExpectedSchemaVersion; webHeaders = $true; metrics = $true } | ConvertTo-Json
