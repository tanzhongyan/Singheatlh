#!/bin/bash

# =====================================================
# PostgreSQL Database Seeding Script
# Loads sample data from CSV files into database
# =====================================================

set -e  # Exit on any error

# Detect working Python installation (cross-platform compatible)
PYTHON_CMD=""
if python --version &> /dev/null; then
    PYTHON_CMD="python"
elif python3 --version &> /dev/null; then
    PYTHON_CMD="python3"
elif py --version &> /dev/null; then
    # Windows py launcher fallback
    PYTHON_CMD="py"
fi

# Install Python dependencies if Python is available
if [ -n "$PYTHON_CMD" ]; then
    echo "Using Python: $($PYTHON_CMD --version)"
    echo "requests>=2.25.1" > requirements.txt
    echo "python-dotenv>=0.19.0" >> requirements.txt
    $PYTHON_CMD -m pip install -r requirements.txt -q 2>&1 || true
fi

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

# Sample data directory
DATA_DIR="sample-data-mini"

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
echo -e "${BLUE}[1/10]${NC} Checking database container..."
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
echo -e "${BLUE}[2/10]${NC} Copying CSV files to container..."
docker exec "$DB_CONTAINER" mkdir -p /tmp/seed-data
docker cp "$DATA_DIR"/. "$DB_CONTAINER":/tmp/seed-data/
echo -e "${GREEN} CSV files copied${NC}"

# 1. Load Clinic data
echo ""
echo -e "${BLUE}[3/10]${NC} Loading Clinic data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Clinic(name, address, telephone_number, type, opening_hours, closing_hours) FROM '/tmp/seed-data/clinics.csv' CSV HEADER
EOF
CLINIC_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Clinic;")
echo -e "${GREEN} Loaded $CLINIC_COUNT clinics${NC}"

# 2. Load Doctor data
echo ""
echo -e "${BLUE}[4/10]${NC} Loading Doctor data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Doctor(doctor_id, name, clinic_id, appointment_duration_in_minutes) FROM '/tmp/seed-data/doctor.csv' CSV HEADER
EOF
DOCTOR_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Doctor;")
echo -e "${GREEN} Loaded $DOCTOR_COUNT doctors${NC}"

# 3. Create Auth Users via Python script
echo ""
echo -e "${BLUE}[5/10]${NC} Creating Auth users..."
if [ -z "$PYTHON_CMD" ]; then
    echo -e "${RED} Error: Python is required but not installed${NC}"
    exit 1
fi
cd "$(dirname "$0")"  # Ensure we're in the db folder
$PYTHON_CMD create_auth_users.py
cd - > /dev/null
echo -e "${GREEN} Auth users created${NC}"

# 4. Update User_Profile data AND load appointments in ONE session
echo ""
echo -e "${BLUE}[6,7/10]${NC} Updating User_Profile and loading related data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
-- Copy CSV data into temporary table
CREATE TEMP TABLE temp_user_profiles (
    user_id UUID,
    name VARCHAR(255),
    role CHAR(1),
    email VARCHAR(255),
    telephone_number VARCHAR(20),
    clinic_id INTEGER
);

\copy temp_user_profiles FROM '/tmp/seed-data/user_profile.csv' CSV HEADER;

-- Update existing user_profiles with data from CSV
UPDATE User_Profile 
SET 
    name = tup.name,
    role = tup.role,
    telephone_number = tup.telephone_number,
    clinic_id = tup.clinic_id
FROM temp_user_profiles tup
WHERE User_Profile.email = tup.email;

-- Create a mapping table between old user_ids and new auth user_ids
CREATE TEMP TABLE user_id_mapping AS
SELECT 
    tup.user_id as old_user_id,
    up.user_id as new_user_id
FROM temp_user_profiles tup
JOIN User_Profile up ON tup.email = up.email;

-- Load appointments with mapped user_ids
CREATE TEMP TABLE temp_appointments (
    appointment_id CHAR(10),
    patient_id UUID,
    doctor_id CHAR(10),
    start_datetime TIMESTAMP,
    end_datetime TIMESTAMP,
    status VARCHAR(20)
);

\copy temp_appointments FROM '/tmp/seed-data/appointment.csv' CSV HEADER;

-- Insert appointments with correct user IDs
INSERT INTO Appointment (appointment_id, patient_id, doctor_id, start_datetime, end_datetime, status)
SELECT 
    ta.appointment_id,
    um.new_user_id as patient_id,  -- Use the mapped auth user_id
    ta.doctor_id,
    ta.start_datetime,
    ta.end_datetime,
    ta.status
FROM temp_appointments ta
JOIN user_id_mapping um ON ta.patient_id = um.old_user_id;

-- Clean up temp tables for this session
DROP TABLE temp_appointments;

-- Count results for logging
SELECT 'Updated ' || COUNT(*) || ' user profiles' FROM User_Profile up
JOIN temp_user_profiles tup ON up.email = tup.email;

SELECT 'Loaded ' || COUNT(*) || ' appointments' FROM Appointment;
EOF

# Get counts for final summary
USER_COUNT=$(execute_sql "SELECT COUNT(*) FROM User_Profile;" -t | tr -d ' ')
APPOINTMENT_COUNT=$(execute_sql "SELECT COUNT(*) FROM Appointment;" -t | tr -d ' ')
echo -e "${GREEN} Updated user profiles (total: $USER_COUNT users)${NC}"
echo -e "${GREEN} Loaded $APPOINTMENT_COUNT appointments${NC}"

# 5. Load Schedule data
echo ""
echo -e "${BLUE}[8/10]${NC} Loading Schedule data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Schedule(schedule_id, doctor_id, start_datetime, end_datetime, type) FROM '/tmp/seed-data/schedule.csv' CSV HEADER
EOF
SCHEDULE_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Schedule;")
echo -e "${GREEN} Loaded $SCHEDULE_COUNT schedules${NC}"

# 6. Load Medical_Summary data
echo ""
echo -e "${BLUE}[9/10]${NC} Loading Medical_Summary data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Medical_Summary(summary_id, appointment_id, treatment_summary) FROM '/tmp/seed-data/medical_summary.csv' CSV HEADER
EOF
MEDICAL_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Medical_Summary;")
echo -e "${GREEN} Loaded $MEDICAL_COUNT medical summaries${NC}"

# 7. Load Queue_Ticket data
echo ""
echo -e "${BLUE}[10/10]${NC} Loading Queue_Ticket data..."
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
\copy Queue_Ticket(appointment_id, status, check_in_time, queue_number, is_fast_tracked) FROM '/tmp/seed-data/queue_ticket.csv' CSV HEADER
EOF
QUEUE_COUNT=$(execute_sql_plain "SELECT COUNT(*) FROM Queue_Ticket;")
echo -e "${GREEN} Loaded $QUEUE_COUNT queue tickets${NC}"


# Drop temporary tables at the very end
echo ""
echo -e "${BLUE}Cleaning temporary tables...${NC}"
docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" << 'EOF'
DROP TABLE IF EXISTS temp_user_profiles;
DROP TABLE IF EXISTS user_id_mapping;
EOF
echo -e "${GREEN} Temporary tables cleaned${NC}"

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