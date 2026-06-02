if (-not $env:COMPOSE_PARALLEL_LIMIT) {
    $env:COMPOSE_PARALLEL_LIMIT = "1"
}

docker compose --env-file "$PSScriptRoot/../.env" -f "$PSScriptRoot/docker-compose.yml" @args
exit $LASTEXITCODE
