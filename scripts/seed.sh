#!/usr/bin/env sh
# Seeds the database with 10,000 deterministic employees and exits (no web server).
#   ./scripts/seed.sh            seed only if the database is empty
#   ./scripts/seed.sh --reset    delete all employees and regenerate
set -e
cd "$(dirname "$0")/../backend"

RESET=false
[ "$1" = "--reset" ] && RESET=true

mvn -q spring-boot:run -Dspring-boot.run.arguments="--spring.main.web-application-type=none --app.seed.enabled=true --app.seed.reset=$RESET --app.seed.count=${APP_SEED_COUNT:-10000}"
