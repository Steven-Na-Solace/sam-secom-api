#!/bin/bash
# ============================================================================
# SECOM MES Database - Complete Initialization & Management Script
# ============================================================================
#
# Usage:
#   ./00-db-init.sh init     - Initialize database with 2025 timeline data
#   ./00-db-init.sh clean    - Stop database and remove all data
#   ./00-db-init.sh restart  - Clean and reinitialize
#   ./00-db-init.sh status   - Check database status
#
# ============================================================================

set -e  # Exit on error

COMPOSE_FILE="docker-compose-db.yml"
CONTAINER_NAME="secom-db"
DB_USER="secom_user"
DB_PASS="secom_pass"
DB_NAME="secom"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================================================
# Helper Functions
# ============================================================================

print_header() {
    echo ""
    echo "============================================================================"
    echo "$1"
    echo "============================================================================"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    print_success "Docker is installed"

    # Check Python3
    if ! command -v python3 &> /dev/null; then
        print_error "Python3 is not installed"
        exit 1
    fi
    print_success "Python3 is installed: $(python3 --version)"

    # Check/Install pymysql
    if python3 -c "import pymysql" 2>/dev/null; then
        print_success "pymysql is installed"
    else
        print_info "Installing pymysql..."
        pip3 install pymysql --quiet
        print_success "pymysql installed"
    fi

    # Check data files exist
    if [ ! -f "data/secom.data" ] || [ ! -f "data/secom_labels.data" ]; then
        print_error "Data files not found in data/ directory"
        exit 1
    fi
    print_success "Data files found"

    echo ""
}

wait_for_db() {
    print_info "Waiting for database to be ready..."
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if docker exec $CONTAINER_NAME mariadb -u$DB_USER -p$DB_PASS -e "SELECT 1" &>/dev/null; then
            print_success "Database is ready!"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    print_error "Database failed to become ready after $max_attempts attempts"
    return 1
}

# ============================================================================
# Main Functions
# ============================================================================

db_clean() {
    print_header "Cleaning Database"

    print_info "Stopping and removing containers..."
    docker compose -f $COMPOSE_FILE down -v 2>/dev/null || true
    print_success "Database cleaned"

    # Clean generated files
    if [ -f "db/init/25-feature-meta.sql" ]; then
        rm db/init/25-feature-meta.sql
        print_success "Removed generated feature metadata SQL"
    fi
}

db_status() {
    print_header "Database Status"

    if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        print_success "Container is running"

        # Get container info
        docker ps --filter "name=${CONTAINER_NAME}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        echo ""

        # Check database connectivity
        if docker exec $CONTAINER_NAME mariadb -u$DB_USER -p$DB_PASS -e "SELECT 1" &>/dev/null; then
            print_success "Database is accessible"

            # Get data statistics
            echo ""
            print_info "Database Statistics:"
            docker exec $CONTAINER_NAME mariadb -u$DB_USER -p$DB_PASS $DB_NAME -e "
                SELECT
                    'Total Lots' as metric, COUNT(*) as value FROM lot
                UNION ALL
                SELECT 'Total Measurements', COUNT(*) FROM lot_measurement
                UNION ALL
                SELECT 'Total Features', COUNT(*) FROM feature_meta
                UNION ALL
                SELECT 'Pass Count', SUM(CASE WHEN classification = -1 THEN 1 ELSE 0 END) FROM quality_result
                UNION ALL
                SELECT 'Fail Count', SUM(CASE WHEN classification = 1 THEN 1 ELSE 0 END) FROM quality_result
                UNION ALL
                SELECT 'Fail Rate %', ROUND(SUM(CASE WHEN classification = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) FROM quality_result;
            "

            # Production timeline
            echo ""
            print_info "Production Timeline:"
            docker exec $CONTAINER_NAME mariadb -u$DB_USER -p$DB_PASS $DB_NAME -e "
                SELECT
                    MIN(production_start) as first_lot,
                    MAX(production_end) as last_lot,
                    DATEDIFF(MAX(production_end), MIN(production_start)) as days_span
                FROM lot;
            "
        else
            print_warning "Database is not accessible"
        fi
    else
        print_warning "Container is not running"
    fi

    echo ""
}

db_init() {
    print_header "SECOM MES Database Initialization - 2025 Timeline"

    # Check prerequisites
    check_prerequisites

    # Step 1: Clean any existing setup
    print_header "Step 1: Cleaning Previous Setup"
    db_clean

    # Step 2: Start database
    print_header "Step 2: Starting Database Container"
    print_info "Starting MariaDB container..."
    docker compose -f $COMPOSE_FILE up -d
    print_success "Container started"

    # Wait for database
    wait_for_db || exit 1

    # Give it a moment for initialization scripts to complete
    sleep 5

    # Step 3: Generate feature metadata
    print_header "Step 3: Generating Feature Metadata"
    print_info "Running feature metadata generator..."
    python3 db/init/25-feature-meta-generator.py > db/init/25-feature-meta.sql
    print_success "Feature metadata SQL generated (590 features)"

    # Step 4: Load feature metadata
    print_header "Step 4: Loading Feature Metadata"
    print_info "Loading into database..."
    docker exec -i $CONTAINER_NAME mariadb -u root -prootpassword $DB_NAME < db/init/25-feature-meta.sql
    print_success "Feature metadata loaded"

    # Step 5: Load production data
    print_header "Step 5: Loading Production Data (2025 Timeline)"
    print_info "This will take 1-2 minutes..."
    echo ""
    python3 db/init/30-load-production-data.py
    echo ""
    print_success "Production data loaded"

    # Step 6: Calculate analytics
    print_header "Step 6: Calculating Analytics"
    print_info "Computing feature importance..."
    docker exec -i $CONTAINER_NAME mariadb -u root -prootpassword $DB_NAME < db/init/35-calculate-analytics.sql > /dev/null
    print_success "Analytics calculated"

    # Step 7: Verification
    print_header "Step 7: Verification"

    print_info "Database Summary:"
    docker exec $CONTAINER_NAME mariadb -u$DB_USER -p$DB_PASS $DB_NAME -e "SELECT * FROM production_summary;"

    echo ""
    print_info "Recent Production Lots:"
    docker exec $CONTAINER_NAME mariadb -u$DB_USER -p$DB_PASS $DB_NAME -e "
        SELECT
            l.lot_number,
            DATE_FORMAT(l.production_end, '%Y-%m-%d %H:%i') as produced,
            CASE WHEN qr.classification = -1 THEN 'PASS' ELSE 'FAIL' END as result,
            ROUND(qr.predicted_risk, 4) as risk,
            qr.defect_type
        FROM lot l
        JOIN quality_result qr ON l.lot_id = qr.lot_id
        ORDER BY l.production_end DESC
        LIMIT 10;
    "

    echo ""
    print_info "Equipment Health:"
    docker exec $CONTAINER_NAME mariadb -u$DB_USER -p$DB_PASS $DB_NAME -e "
        SELECT
            equipment_code,
            total_lots_processed as lots,
            failed_lots as fails,
            equipment_fail_rate_pct as fail_rate,
            health_score
        FROM equipment_health_stats
        WHERE total_lots_processed > 0
        ORDER BY equipment_fail_rate_pct DESC;
    "

    # Final success message
    print_header "✅ Database Initialization Complete!"

    echo "Database is ready for use with 2025 timeline data!"
    echo ""
    echo "Connection Details:"
    echo "  Host: localhost"
    echo "  Port: 3306"
    echo "  Database: $DB_NAME"
    echo "  User: $DB_USER"
    echo "  Password: $DB_PASS"
    echo ""
    echo "Connect using:"
    echo "  docker exec -it $CONTAINER_NAME mariadb -u$DB_USER -p$DB_PASS $DB_NAME"
    echo ""
    echo "Or from host:"
    echo "  mysql -h 127.0.0.1 -P 3306 -u$DB_USER -p$DB_PASS $DB_NAME"
    echo ""
}

db_restart() {
    print_header "Restarting Database"
    db_clean
    db_init
}

# ============================================================================
# Command Line Interface
# ============================================================================

show_usage() {
    echo "SECOM MES Database Management Script"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  init      Initialize database with 2025 timeline data"
    echo "  clean     Stop database and remove all data"
    echo "  restart   Clean and reinitialize database"
    echo "  status    Check database status and statistics"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 init       # First time setup"
    echo "  $0 status     # Check current status"
    echo "  $0 restart    # Fresh restart with clean data"
    echo "  $0 clean      # Remove everything"
    echo ""
}

# Main command dispatcher
case "${1:-}" in
    init)
        db_init
        ;;
    clean)
        db_clean
        ;;
    restart)
        db_restart
        ;;
    status)
        db_status
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        print_error "Unknown command: ${1:-}"
        echo ""
        show_usage
        exit 1
        ;;
esac
