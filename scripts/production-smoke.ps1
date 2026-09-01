param(
    [Parameter(Mandatory = $true)]
    [string]$ApiBaseUrl,
    [Parameter(Mandatory = $true)]
    [string]$WebBaseUrl,
    [Parameter(Mandatory = $true)]
    [string]$ExpectedReleaseId,
    [string]$ExpectedSchemaVersion = '7',
    [switch]$VerifyGatewayRoute,
    [switch]$AllowHttpForLocalValidation
)

$ErrorActionPreference = 'Stop'

function Assert-BaseUrl([string]$Value) {
    $Uri = [Uri]$Value
    if ($Uri.Scheme -eq 'https') { return }
    if ($AllowHttpForLocalValidation -and $Uri.Scheme -eq 'http' -and
        $Uri.Host -in @('localhost', '127.0.0.1', '::1')) { return }
    throw 'Smoke de produção exige HTTPS; HTTP só é aceito explicitamente em loopback.'
}

function Invoke-SmokeRequest([string]$Uri, [string]$RequestId) {
    $Response = Invoke-WebRequest -Uri $Uri -Headers @{ 'X-Request-ID' = $RequestId } -UseBasicParsing
    if ($Response.StatusCode -ne 200) { throw "Smoke falhou em $Uri." }
    return $Response
}

function Get-SmokeContent($Response) {
    if ($Response.Content -is [byte[]]) { return [Text.Encoding]::UTF8.GetString($Response.Content) }
    return [string]$Response.Content
}

Assert-BaseUrl $ApiBaseUrl
Assert-BaseUrl $WebBaseUrl
if ($ExpectedReleaseId -notmatch '^[a-f0-9]{40}$') { throw 'ExpectedReleaseId deve ser um SHA Git completo.' }

$Readiness = Invoke-SmokeRequest "$ApiBaseUrl/actuator/health/readiness" 'production-readiness'
if (((Get-SmokeContent $Readiness) | ConvertFrom-Json).status -ne 'UP') { throw 'Readiness não está UP.' }
if ([string]$Readiness.Headers['X-Request-ID'] -ne 'production-readiness') { throw 'Request ID não foi propagado.' }

$Status = Invoke-SmokeRequest "$ApiBaseUrl/api/v1/system/status" 'production-status'
$StatusBody = (Get-SmokeContent $Status) | ConvertFrom-Json
if ($StatusBody.database.schemaVersion -ne $ExpectedSchemaVersion) { throw 'Versão de schema inesperada.' }
if ($StatusBody.releaseId -ne $ExpectedReleaseId) { throw 'Release ativo não corresponde ao artefato aprovado.' }
if ($Status.Headers['Cache-Control'] -notmatch 'no-store') { throw 'Status deve impedir cache.' }

$Csrf = Invoke-SmokeRequest "$ApiBaseUrl/api/v1/auth/csrf" 'production-csrf'
$SetCookie = [string]$Csrf.Headers['Set-Cookie']
if ($SetCookie -notmatch 'Secure' -or $SetCookie -notmatch 'SameSite=Lax') {
    throw 'Cookie CSRF de produção não possui os atributos esperados.'
}

$Web = Invoke-SmokeRequest $WebBaseUrl 'production-web'
foreach ($Header in @('Content-Security-Policy', 'Strict-Transport-Security', 'X-Content-Type-Options')) {
    if ($null -eq $Web.Headers[$Header]) { throw "Header web ausente: $Header" }
}

if ($VerifyGatewayRoute) {
    $GatewayStatus = Invoke-SmokeRequest "$WebBaseUrl/api/v1/system/status" 'production-gateway'
    if (((Get-SmokeContent $GatewayStatus) | ConvertFrom-Json).releaseId -ne $ExpectedReleaseId) {
        throw 'O gateway não encaminhou /api para o release esperado.'
    }
}

[PSCustomObject]@{
    ready = $true
    releaseId = $ExpectedReleaseId
    schemaVersion = $ExpectedSchemaVersion
    secureCsrf = $true
    webHeaders = $true
    gatewayRoute = [bool]$VerifyGatewayRoute
} | ConvertTo-Json
