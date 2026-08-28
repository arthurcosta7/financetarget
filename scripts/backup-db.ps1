param(
    [Parameter(Mandatory = $true)]
    [string]$EnvironmentFile,
    [Parameter(Mandatory = $true)]
    [string]$OutputPath,
    [string]$ContainerName,
    [string]$DatabaseName,
    [string]$DatabaseUser
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ResolvedEnvironmentFile = [IO.Path]::GetFullPath($EnvironmentFile)
$ResolvedOutput = [IO.Path]::GetFullPath($OutputPath)
. (Join-Path $PSScriptRoot 'load-env.ps1') -Path $ResolvedEnvironmentFile

if ($env:APP_ENV -notin @('dev', 'staging')) {
    throw 'Este script automatizado aceita somente dev ou staging.'
}

$Key = [Convert]::FromBase64String($env:BACKUP_ENCRYPTION_KEY_BASE64)
if ($Key.Length -ne 32) {
    throw 'BACKUP_ENCRYPTION_KEY_BASE64 deve conter exatamente 32 bytes em Base64.'
}

$OutputDirectory = Split-Path -Parent $ResolvedOutput
if (Test-Path -LiteralPath $ResolvedOutput) {
    throw 'O arquivo de saída já existe; backups não são sobrescritos.'
}
if (-not (Test-Path -LiteralPath $OutputDirectory)) {
    New-Item -ItemType Directory -Path $OutputDirectory | Out-Null
}

$EffectiveDatabaseName = if ([string]::IsNullOrWhiteSpace($DatabaseName)) { $env:POSTGRES_DB } else { $DatabaseName }
$EffectiveDatabaseUser = if ([string]::IsNullOrWhiteSpace($DatabaseUser)) { $env:POSTGRES_USER } else { $DatabaseUser }
if ($EffectiveDatabaseName -notmatch '^[A-Za-z_][A-Za-z0-9_]{0,62}$' -or
    $EffectiveDatabaseUser -notmatch '^[A-Za-z_][A-Za-z0-9_]{0,62}$') {
    throw 'Nome de banco ou usuário inválido.'
}

if ([string]::IsNullOrWhiteSpace($ContainerName)) {
    $ContainerId = (docker compose --env-file $ResolvedEnvironmentFile ps -q postgres).Trim()
}
else {
    if ($ContainerName -notmatch '^[A-Za-z0-9][A-Za-z0-9_.-]{0,127}$') { throw 'Nome de container inválido.' }
    $ContainerId = (docker ps --filter "name=^$ContainerName$" --format '{{.ID}}').Trim()
}
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($ContainerId)) {
    throw 'O container PostgreSQL do projeto não está em execução.'
}

$TemporaryName = 'financetarget-' + [Guid]::NewGuid().ToString('N') + '.dump'
$ContainerTemporary = '/tmp/' + $TemporaryName
$LocalTemporary = [IO.Path]::GetTempFileName()

try {
    docker exec $ContainerId pg_dump -U $EffectiveDatabaseUser -d $EffectiveDatabaseName `
        --format=custom --no-owner --no-acl --file $ContainerTemporary
    if ($LASTEXITCODE -ne 0) { throw 'pg_dump falhou.' }

    docker cp ($ContainerId + ':' + $ContainerTemporary) $LocalTemporary
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível copiar o dump temporário.' }

    $Plaintext = [IO.File]::ReadAllBytes($LocalTemporary)
    $Nonce = [Security.Cryptography.RandomNumberGenerator]::GetBytes(12)
    $Tag = [byte[]]::new(16)
    $Ciphertext = [byte[]]::new($Plaintext.Length)
    $Aes = [Security.Cryptography.AesGcm]::new($Key, 16)
    try {
        $Aes.Encrypt($Nonce, $Plaintext, $Ciphertext, $Tag)
    }
    finally {
        $Aes.Dispose()
    }
    $Magic = [Text.Encoding]::ASCII.GetBytes('FTB1')
    $Encrypted = [byte[]]::new($Magic.Length + $Nonce.Length + $Tag.Length + $Ciphertext.Length)
    [Array]::Copy($Magic, 0, $Encrypted, 0, $Magic.Length)
    [Array]::Copy($Nonce, 0, $Encrypted, 4, $Nonce.Length)
    [Array]::Copy($Tag, 0, $Encrypted, 16, $Tag.Length)
    [Array]::Copy($Ciphertext, 0, $Encrypted, 32, $Ciphertext.Length)
    [IO.File]::WriteAllBytes($ResolvedOutput, $Encrypted)
    $Hash = (Get-FileHash -LiteralPath $ResolvedOutput -Algorithm SHA256).Hash.ToLowerInvariant()
    [PSCustomObject]@{ path = $ResolvedOutput; sha256 = $Hash; encrypted = $true } | ConvertTo-Json
}
finally {
    docker exec $ContainerId rm -- $ContainerTemporary 2>$null
    if (Test-Path -LiteralPath $LocalTemporary) { Remove-Item -LiteralPath $LocalTemporary -Force }
    if ($null -ne $Plaintext) { [Security.Cryptography.CryptographicOperations]::ZeroMemory($Plaintext) }
    [Security.Cryptography.CryptographicOperations]::ZeroMemory($Key)
}
