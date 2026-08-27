param(
    [Parameter(Mandatory = $true)]
    [string]$Path
)

if (-not (Test-Path -LiteralPath $Path)) {
    throw "Arquivo de ambiente não encontrado: $Path"
}

foreach ($Line in Get-Content -LiteralPath $Path) {
    $TrimmedLine = $Line.Trim()

    if ($TrimmedLine.Length -eq 0 -or $TrimmedLine.StartsWith('#')) {
        continue
    }

    $Separator = $TrimmedLine.IndexOf('=')
    if ($Separator -lt 1) {
        throw "Linha inválida no arquivo de ambiente: $Line"
    }

    $Name = $TrimmedLine.Substring(0, $Separator).Trim()
    $Value = $TrimmedLine.Substring($Separator + 1).Trim()
    [Environment]::SetEnvironmentVariable($Name, $Value, 'Process')
}
