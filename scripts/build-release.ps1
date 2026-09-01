param(
    [string]$Revision,
    [string]$Tag
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot

Push-Location $ProjectRoot
try {
    $HeadRevision = (git rev-parse HEAD).Trim()
    if ([string]::IsNullOrWhiteSpace($Revision)) {
        $Revision = $HeadRevision
    }
    if ($Revision -notmatch '^[a-f0-9]{40}$') {
        throw 'Revision deve ser o SHA Git completo de 40 caracteres.'
    }
    if ($Revision -ne $HeadRevision) {
        throw 'Revision deve corresponder ao HEAD, pois as imagens são construídas a partir da árvore de trabalho atual.'
    }
    $WorkingTreeChanges = @(git status --porcelain --untracked-files=all)
    if ($WorkingTreeChanges.Count -gt 0) {
        throw 'A árvore de trabalho deve estar limpa antes de construir artefatos de release.'
    }
    if ([string]::IsNullOrWhiteSpace($Tag)) {
        $Tag = $Revision.Substring(0, 12)
    }
    if ($Tag -notmatch '^[A-Za-z0-9][A-Za-z0-9_.-]{0,127}$') {
        throw 'Tag de imagem inválida.'
    }

    $ApiImage = "financetarget/api:$Tag"
    $WebImage = "financetarget/web:$Tag"
    docker build --file apps/api/Dockerfile --build-arg "VCS_REF=$Revision" --tag $ApiImage .
    if ($LASTEXITCODE -ne 0) { throw 'Build da imagem da API falhou.' }
    docker build --file apps/web/Dockerfile --build-arg "VCS_REF=$Revision" --tag $WebImage .
    if ($LASTEXITCODE -ne 0) { throw 'Build da imagem web falhou.' }

    $ApiUser = (docker image inspect $ApiImage --format '{{.Config.User}}').Trim()
    $WebUser = (docker image inspect $WebImage --format '{{.Config.User}}').Trim()
    $ApiRevision = (docker image inspect $ApiImage --format '{{index .Config.Labels "org.opencontainers.image.revision"}}').Trim()
    $WebRevision = (docker image inspect $WebImage --format '{{index .Config.Labels "org.opencontainers.image.revision"}}').Trim()
    if ($ApiUser -in @('', '0', 'root') -or $WebUser -in @('', '0', 'root')) {
        throw 'As imagens de release devem executar sem root.'
    }
    if ($ApiRevision -ne $Revision -or $WebRevision -ne $Revision) {
        throw 'Os labels OCI não correspondem ao commit promovido.'
    }

    [PSCustomObject]@{
        revision = $Revision
        apiImage = $ApiImage
        apiImageId = (docker image inspect $ApiImage --format '{{.Id}}').Trim()
        webImage = $WebImage
        webImageId = (docker image inspect $WebImage --format '{{.Id}}').Trim()
        nonRoot = $true
    } | ConvertTo-Json
}
finally {
    Pop-Location
}
