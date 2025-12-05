# SECOM Database Project

Database setup for loading and analyzing SECOM (SEmiCOnductor Manufacturing) dataset.
https://archive.ics.uci.edu/dataset/179/secom

## Quick Start

### Prerequisites
- Docker and Docker Compose installed

### Starting the Database

1. chmod +x 00-db-init.sh   
2. /00-db-init.sh inti

### Connecting to the Database

```bash
# Connect via Docker
docker exec -it secom-db mariadb -u secom_user -psecom_pass secom
```

### Connection Details
- **Host**: localhost (127.0.0.1)
- **Port**: 3306
- **Database**: secom
- **User**: secom_user
- **Password**: secom_pass
- **Root Password**: rootpassword



# SECOM MES REST API

Complete Spring Boot REST API for the SECOM Manufacturing Execution System Database.

## Overview

This API provides comprehensive access to semiconductor manufacturing data including:
- **Master Data**: Equipment, Shifts, Operators, Product Types, Feature Metadata
- **Production Data**: Lots, Sensor Measurements, Quality Results
- **Analytics**: Production summaries, Equipment health, Shift performance, Quality analytics

### Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.5
- **Database**: MariaDB 11.2
- **ORM**: Spring Data JPA with Hibernate
- **API Documentation**: SpringDoc OpenAPI 2.3.0 (Swagger UI)
- **Build Tool**: Maven

## Quick Start

### Prerequisites

1. **Database**: MariaDB database running (secom-db container)
   ```bash
   docker ps --filter "name=secom-db"
   # Should show: secom-db   Up XX minutes (healthy)
   ```

### Run with Docker

The easiest way to run the API is using Docker:

```bash
# Make scripts executable (first time only)
chmod +x sam-api/start-api.sh sam-api/stop-api.sh

# Start the API
./sam-api/start-api.sh

**Stop the API:**
```bash
./sam-api/stop-api.sh
```
