#!/bin/bash

# =====================================================
# PostgreSQL Database Seeding Script
# Loads sample data from CSV files into database
# =====================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Database configuration
DB_NAME="postgres"
DB_USER="postgres"
DB_PASSWORD="your-super-secret-and-long-postgres-password"
DB_HOST="localhost"
DB_PORT="5434"
DB_CONTAINER="supabase-db"

# Function to execute SQL command via docker
execute_sql() {
    docker exec -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "$1"
}

# Function to execute SQL and return plain value (no formatting)
execute_sql_plain() {
    docker exec -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -A -t -c "$1"
}

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  PostgreSQL Database Seeding${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if database container is running
echo -e "${BLUE}[1/9]${NC} Checking database container..."
if ! docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
    echo -e "${RED} Error: Database container '$DB_CONTAINER' is not running${NC}"
    echo -e "${YELLOW}Please start the database with: npm run db:up${NC}"
    exit 1
fi

# Wait for database to be ready
echo -e "${BLUE}Waiting for database to be ready...${NC}"
RETRY_COUNT=0
MAX_RETRIES=30

while ! execute_sql "SELECT 1;" > /dev/null 2>&1; do
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
        echo -e "${RED} Error: Database did not become ready in time${NC}"
        exit 1
    fi
    echo -e "${YELLOW}Waiting for database... (${RETRY_COUNT}/${MAX_RETRIES})${NC}"
    sleep 1
done
echo -e "${GREEN} Database connection successful${NC}"

# Wait for tables to be created (by Flyway migrations when backend starts)
echo ""
echo -e "${BLUE}Waiting for database tables to be created...${NC}"
echo -e "${YELLOW}(Make sure the backend is running to create tables)${NC}"
RETRY_COUNT=0
MAX_RETRIES=60

while ! execute_sql "SELECT 1 FROM Clinic LIMIT 1;" > /dev/null 2>&1; do
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
        echo -e "${RED} Error: Tables not created after ${MAX_RETRIES} seconds${NC}"
        echo -e "${YELLOW}Please ensure the Spring Boot backend is running (it creates tables automatically)${NC}"
        exit 1
    fi
    echo -e "${YELLOW}Waiting for tables... (${RETRY_COUNT}/${MAX_RETRIES})${NC}"
    sleep 1
done
echo -e "${GREEN} Tables found${NC}"

# Verify migrations have run
echo ""
echo -e "${BLUE}Verifying Flyway migrations...${NC}"
MIGRATIONS=$(execute_sql "SELECT version, description FROM flyway_schema_history ORDER BY installed_rank;" -t 2>/dev/null || echo "")
if [ -z "$MIGRATIONS" ]; then
    echo -e "${RED} Error: No Flyway migrations found${NC}"
    echo -e "${YELLOW}Please ensure the Spring Boot backend has started and run migrations${NC}"
    exit 1
fi
echo -e "${GREEN} Migrations verified:${NC}"
echo "$MIGRATIONS"

# Verify auth trigger exists (from V2 migration)
echo ""
echo -e "${BLUE}Verifying auth trigger...${NC}"

# Check if function exists in public schema
FUNCTION_EXISTS=$(execute_sql_plain "SELECT EXISTS(SELECT 1 FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE p.proname = 'handle_new_user' AND n.nspname = 'public');")

# Check if trigger exists on auth.users table
TRIGGER_EXISTS=$(execute_sql_plain "SELECT EXISTS(SELECT 1 FROM pg_trigger WHERE tgname = 'on_auth_user_created');")

if [ "$FUNCTION_EXISTS" = "t" ] && [ "$TRIGGER_EXISTS" = "t" ]; then
    echo -e "${GREEN} Auth trigger function and trigger on auth.users exist${NC}"
elif [ "$FUNCTION_EXISTS" = "t" ]; then
    echo -e "${YELLOW} Warning: Function exists but trigger on auth.users not found (insufficient privileges)${NC}"
elif [ "$TRIGGER_EXISTS" = "t" ]; then
    echo -e "${YELLOW} Warning: Trigger exists but function not found${NC}"
else
    echo -e "${YELLOW} Warning: Neither function nor trigger found. V2 migration may not have run successfully.${NC}"
fi

# Copy CSV files to container
echo ""
echo -e "${BLUE}[2/9]${NC} Copying CSV files to container..."
docker exec "$DB_CONTAINER" mkdir -p /tmp/seed-data
docker cp sample-data/. "$DB_CONTAINER":/tmp/seed-data/
echo -e "${GREEN} CSV files copied${NC}"

# 1. Load Clinic data
echo ""
echo -e "${BLUE}[3/9]${NC} Loading Clinic data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Clinic(name, address, telephone_number, type, opening_hours, closing_hours) FROM '/tmp/seed-data/clinics.csv' CSV HEADER
EOF
CLINIC_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Clinic;")
echo -e "${GREEN} Loaded $CLINIC_COUNT clinics${NC}"

# 2. Load User_Profile data
echo ""
echo -e "${BLUE}[4/9]${NC} Loading User_Profile data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy User_Profile(user_id, name, role, email, telephone_number, clinic_id) FROM '/tmp/seed-data/user_profile.csv' CSV HEADER
EOF
USER_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM User_Profile;")
echo -e "${GREEN} Loaded $USER_COUNT users${NC}"

# 3. Load Doctor data
echo ""
echo -e "${BLUE}[5/9]${NC} Loading Doctor data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Doctor(doctor_id, name, clinic_id) FROM '/tmp/seed-data/doctor.csv' CSV HEADER
EOF
DOCTOR_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Doctor;")
echo -e "${GREEN} Loaded $DOCTOR_COUNT doctors${NC}"

# 4. Load Schedule data
echo ""
echo -e "${BLUE}[6/9]${NC} Loading Schedule data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Schedule(schedule_id, doctor_id, start_datetime, end_datetime, type) FROM '/tmp/seed-data/schedule.csv' CSV HEADER
EOF
SCHEDULE_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Schedule;")
echo -e "${GREEN} Loaded $SCHEDULE_COUNT schedules${NC}"

# 5. Load Appointment data
echo ""
echo -e "${BLUE}[7/9]${NC} Loading Appointment data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Appointment(appointment_id, patient_id, doctor_id, start_datetime, end_datetime, status) FROM '/tmp/seed-data/appointment.csv' CSV HEADER
EOF
APPOINTMENT_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Appointment;")
echo -e "${GREEN} Loaded $APPOINTMENT_COUNT appointments${NC}"

# 6. Load Medical_Summary data
echo ""
echo -e "${BLUE}[8/9]${NC} Loading Medical_Summary data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Medical_Summary(summary_id, appointment_id, treatment_summary) FROM '/tmp/seed-data/medical_summary.csv' CSV HEADER
EOF
MEDICAL_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Medical_Summary;")
echo -e "${GREEN} Loaded $MEDICAL_COUNT medical summaries${NC}"

# 7. Load Queue_Ticket data
echo ""
echo -e "${BLUE}[9/9]${NC} Loading Queue_Ticket data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Queue_Ticket(appointment_id, status, check_in_time, queue_number, is_fast_tracked) FROM '/tmp/seed-data/queue_ticket.csv' CSV HEADER
EOF
QUEUE_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Queue_Ticket;")
echo -e "${GREEN} Loaded $QUEUE_COUNT queue tickets${NC}"

# Cleanup
echo ""
echo -e "${BLUE}Cleaning up...${NC}"
docker exec "$DB_CONTAINER" rm -rf /tmp/seed-data
echo -e "${GREEN} Cleanup complete${NC}"

# Final summary
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Seeding Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Summary:"
echo -e "  - Clinics: $CLINIC_COUNT"
echo -e "  - Users: $USER_COUNT"
echo -e "  - Doctors: $DOCTOR_COUNT"
echo -e "  - Schedules: $SCHEDULE_COUNT"
echo -e "  - Appointments: $APPOINTMENT_COUNT"
echo -e "  - Medical Summaries: $MEDICAL_COUNT"
echo -e "  - Queue Tickets: $QUEUE_COUNT"
echo ""