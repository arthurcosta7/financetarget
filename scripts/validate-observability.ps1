$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$OpsPath = Join-Path $ProjectRoot 'ops'
$PrometheusImage = 'prom/prometheus@sha256:5ce7540c3c00ef4ab0c9d2c995c6a5b9c421f44b4a115d97a2c7af3b1c21cbb0'

docker run --rm --entrypoint promtool `
    --mount "type=bind,source=$OpsPath,target=/ops,readonly" `
    $PrometheusImage check rules /ops/observability/prometheus-rules.yml
if ($LASTEXITCODE -ne 0) { throw 'As regras Prometheus são inválidas.' }
