#!/bin/bash

# SECOM MES API Stop Script
# This script stops the API Docker container

set -e

echo "=========================================="
echo "  Stopping SECOM MES REST API"
echo "=========================================="
echo

# Change to script directory
cd "$(dirname "$0")"

# Check if container is running
if ! docker ps --format '{{.Names}}' | grep -q '^secom-api$'; then
    echo "API container 'secom-api' is not running"

    # Check if container exists but is stopped
    if docker ps -a --format '{{.Names}}' | grep -q '^secom-api$'; then
        echo "Removing stopped container..."
        docker compose -f docker-compose-api.yml down
    fi

    echo
    echo "API is not running"
    echo
    exit 0
fi

# Stop the container
echo "Stopping API container..."
docker compose -f docker-compose-api.yml down

echo
echo "API stopped successfully"
echo
echo "To start again: ./start-api.sh"
echo
