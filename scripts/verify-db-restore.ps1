param(
    [Parameter(Mandatory = $true)]
    [string]$EnvironmentFile,
    [Parameter(Mandatory = $true)]
    [string]$BackupPath,
    [string]$ContainerName,
    [string]$DatabaseUser,
    [string]$ExpectedSchemaVersion
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ResolvedEnvironmentFile = [IO.Path]::GetFullPath($EnvironmentFile)
$ResolvedBackup = [IO.Path]::GetFullPath($BackupPath)
. (Join-Path $PSScriptRoot 'load-env.ps1') -Path $ResolvedEnvironmentFile

if ($env:APP_ENV -notin @('dev', 'staging')) {
    throw 'A restauração automatizada aceita somente dev ou staging.'
}
if (-not (Test-Path -LiteralPath $ResolvedBackup)) {
    throw 'Backup não encontrado.'
}

$Key = $null
$Plaintext = $null
$ContainerId = $null
$RestoreDatabase = $null
$ContainerTemporary = $null
$LocalTemporary = $null

try {
    $Key = [Convert]::FromBase64String($env:BACKUP_ENCRYPTION_KEY_BASE64)
    if ($Key.Length -ne 32) {
        throw 'BACKUP_ENCRYPTION_KEY_BASE64 deve conter exatamente 32 bytes em Base64.'
    }
    $Encrypted = [IO.File]::ReadAllBytes($ResolvedBackup)
    if ($Encrypted.Length -lt 33 -or [Text.Encoding]::ASCII.GetString($Encrypted, 0, 4) -ne 'FTB1') {
        throw 'Formato de backup inválido.'
    }

    $Nonce = $Encrypted[4..15]
    $Tag = $Encrypted[16..31]
    $Ciphertext = $Encrypted[32..($Encrypted.Length - 1)]
    $Plaintext = [byte[]]::new($Ciphertext.Length)
    $Aes = [Security.Cryptography.AesGcm]::new($Key, 16)
    try {
        $Aes.Decrypt($Nonce, $Ciphertext, $Tag, $Plaintext)
    }
    finally {
        $Aes.Dispose()
    }

    $EffectiveDatabaseUser = if ([string]::IsNullOrWhiteSpace($DatabaseUser)) { $env:POSTGRES_USER } else { $DatabaseUser }
    if ($EffectiveDatabaseUser -notmatch '^[A-Za-z_][A-Za-z0-9_]{0,62}$') { throw 'Usuário de banco inválido.' }
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

    $Suffix = [Guid]::NewGuid().ToString('N')
    $RestoreDatabase = 'financetarget_restore_' + $Suffix
    if ($RestoreDatabase -notmatch '^financetarget_restore_[a-f0-9]{32}$') {
        throw 'Nome isolado de restauração inválido.'
    }
    $TemporaryName = 'financetarget-restore-' + $Suffix + '.dump'
    $ContainerTemporary = '/tmp/' + $TemporaryName
    $LocalTemporary = [IO.Path]::GetTempFileName()

    [IO.File]::WriteAllBytes($LocalTemporary, $Plaintext)
    docker cp $LocalTemporary ($ContainerId + ':' + $ContainerTemporary)
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível copiar o backup para restauração.' }

    docker exec $ContainerId createdb -U $EffectiveDatabaseUser $RestoreDatabase
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível criar o banco isolado de restauração.' }
    docker exec $ContainerId pg_restore -U $EffectiveDatabaseUser -d $RestoreDatabase `
        --no-owner --no-acl --exit-on-error $ContainerTemporary
    if ($LASTEXITCODE -ne 0) { throw 'pg_restore falhou.' }

    $SchemaVersion = (docker exec $ContainerId psql -U $EffectiveDatabaseUser -d $RestoreDatabase `
        -t -A -c "select metadata_value from app_metadata where metadata_key='schema_version'").Trim()
    if ($LASTEXITCODE -ne 0 -or $SchemaVersion -notmatch '^\d+$') {
        throw 'O banco restaurado não passou na verificação de integridade mínima.'
    }
    if (-not [string]::IsNullOrWhiteSpace($ExpectedSchemaVersion) -and $SchemaVersion -ne $ExpectedSchemaVersion) {
        throw "O schema restaurado é $SchemaVersion, mas $ExpectedSchemaVersion era esperado."
    }
    [PSCustomObject]@{ restored = $true; schemaVersion = $SchemaVersion; isolatedDatabase = $RestoreDatabase } | ConvertTo-Json
}
finally {
    if (-not [string]::IsNullOrWhiteSpace($ContainerId)) {
        if (-not [string]::IsNullOrWhiteSpace($RestoreDatabase)) {
            docker exec $ContainerId dropdb -U $EffectiveDatabaseUser --if-exists $RestoreDatabase 2>$null
        }
        if (-not [string]::IsNullOrWhiteSpace($ContainerTemporary)) {
            docker exec $ContainerId rm -- $ContainerTemporary 2>$null
        }
    }
    if ($null -ne $LocalTemporary -and (Test-Path -LiteralPath $LocalTemporary)) {
        Remove-Item -LiteralPath $LocalTemporary -Force
    }
    if ($null -ne $Plaintext) { [Security.Cryptography.CryptographicOperations]::ZeroMemory($Plaintext) }
    if ($null -ne $Key) { [Security.Cryptography.CryptographicOperations]::ZeroMemory($Key) }
}
