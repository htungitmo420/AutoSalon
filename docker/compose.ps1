if (-not $env:COMPOSE_PARALLEL_LIMIT) {
    $env:COMPOSE_PARALLEL_LIMIT = "1"
}

$composeFiles = @("-f", "$PSScriptRoot/docker-compose.yml")

if ($env:COMPOSE_ENV -eq "prod") {
    $composeFiles += @("-f", "$PSScriptRoot/docker-compose.prod.yml")
} else {
    $composeFiles += @("-f", "$PSScriptRoot/docker-compose.local.yml")
}

if ($env:COMPOSE_OBSERVABILITY -eq "true") {
    $composeFiles += @("-f", "$PSScriptRoot/docker-compose.observability.yml", "--profile", "observability")
}

docker compose --env-file "$PSScriptRoot/../.env" @composeFiles @args
exit $LASTEXITCODE
