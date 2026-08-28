param(
    [switch]$SkipContainerScan
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$OsvScannerImage = 'ghcr.io/google/osv-scanner@sha256:5116601dedc01c1c580eb92371883ec052fc4c13c3fbc109d621a63ac416d475'

Push-Location $ProjectRoot
try {
    corepack pnpm audit --prod --audit-level high
    if ($LASTEXITCODE -ne 0) { throw 'A auditoria pnpm encontrou vulnerabilidades no nível bloqueante.' }

    if (-not $SkipContainerScan) {
        Push-Location (Join-Path $ProjectRoot 'apps/api')
        try {
            $MavenWrapper = if ($IsWindows) { '.\mvnw.cmd' } else { './mvnw' }
            & $MavenWrapper --batch-mode '-DskipTests' package
            if ($LASTEXITCODE -ne 0) { throw 'A geração do SBOM Maven falhou.' }
        }
        finally {
            Pop-Location
        }

        docker run --rm --mount "type=bind,source=$ProjectRoot,target=/src,readonly" `
            $OsvScannerImage scan source /src/apps/api/target/classes/META-INF/sbom/application.cdx.json /src/pnpm-lock.yaml
        if ($LASTEXITCODE -ne 0) { throw 'A auditoria OSV encontrou vulnerabilidades ou não pôde ser concluída.' }
    }
}
finally {
    Pop-Location
}
