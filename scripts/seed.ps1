# Seeds the database with 10,000 deterministic employees and exits (no web server).
#   .\scripts\seed.ps1            seed only if the database is empty
#   .\scripts\seed.ps1 -Reset     delete all employees and regenerate
param([switch]$Reset, [int]$Count = 10000)
$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '..\backend')
try {
    $resetFlag = if ($Reset) { 'true' } else { 'false' }
    mvn -q spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-application-type=none --app.seed.enabled=true --app.seed.reset=$resetFlag --app.seed.count=$Count"
} finally {
    Pop-Location
}
