#!/bin/bash

# SECOM MES API Startup Script
# This script starts the API in a Docker container that connects to the existing database

set -e

echo "=========================================="
echo "  SECOM MES REST API - Docker Startup"
echo "=========================================="
echo

# Change to script directory
cd "$(dirname "$0")"

# Check if database container is running
echo "Checking database container status..."
if ! docker ps --format '{{.Names}}' | grep -q '^secom-db$'; then
    echo
    echo "ERROR: Database container 'secom-db' is not running!"
    echo
    echo "Please start the database first:"
    echo "  ./00-db-init.sh status"
    echo
    exit 1
fi

echo "Database container 'secom-db' is running"
echo

# Check if database is healthy
DB_HEALTH=$(docker inspect secom-db --format='{{.State.Health.Status}}' 2>/dev/null || echo "unknown")
if [ "$DB_HEALTH" = "healthy" ]; then
    echo "Database health check: HEALTHY"
else
    echo "WARNING: Database health status: $DB_HEALTH"
    echo "Continuing anyway, but API may fail to connect..."
fi
echo

# Stop existing API container if running
if docker ps -a --format '{{.Names}}' | grep -q '^secom-api$'; then
    echo "Stopping existing API container..."
    docker compose -f docker-compose-api.yml down
    echo
fi

# Build and start API
echo "Building and starting API container..."
echo "(This may take a few minutes on first run)"
echo
docker compose -f docker-compose-api.yml up -d --build

if [ $? -ne 0 ]; then
    echo
    echo "ERROR: Failed to start API container"
    echo "Check logs: docker compose -f docker-compose-api.yml logs"
    exit 1
fi

echo
echo "API container started. Waiting for health check..."
echo

# Wait for health check to pass
MAX_WAIT=60
WAIT_COUNT=0

while [ $WAIT_COUNT -lt $MAX_WAIT ]; do
    HEALTH_STATUS=$(docker inspect secom-api --format='{{.State.Health.Status}}' 2>/dev/null || echo "starting")

    if [ "$HEALTH_STATUS" = "healthy" ]; then
        echo
        echo "=========================================="
        echo "  API is HEALTHY and READY!"
        echo "=========================================="
        echo
        echo "Access Points:"
        echo "  Base URL:    http://localhost:8080/api/v1"
        echo "  Swagger UI:  http://localhost:8080/api/v1/swagger"
        echo "  API Docs:    http://localhost:8080/api/v1/docs"
        echo
        echo "Test Commands:"
        echo "  curl http://localhost:8080/api/v1/equipment | jq"
        echo "  curl http://localhost:8080/api/v1/lots | jq"
        echo
        echo "Useful Commands:"
        echo "  View logs:   docker compose -f docker-compose-api.yml logs -f"
        echo "  Stop API:    ./stop-api.sh"
        echo "  Restart:     ./stop-api.sh && ./start-api.sh"
        echo
        exit 0
    fi

    echo -n "."
    sleep 2
    WAIT_COUNT=$((WAIT_COUNT + 1))
done

echo
echo "=========================================="
echo "  WARNING: Health check timeout"
echo "=========================================="
echo
echo "The API container started but health check did not pass within ${MAX_WAIT} attempts."
echo
echo "The API may still be starting. Check the logs:"
echo "  docker compose -f docker-compose-api.yml logs -f"
echo "Container status:"
docker ps --filter "name=secom-api" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo
exit 1
