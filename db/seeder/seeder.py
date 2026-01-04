#!/usr/bin/env python3
"""
Singheatlh Database Seeder
Containerized version of seed-data.sh
Loads sample data from CSV files into PostgreSQL database
"""

import os
import sys
import time
import subprocess
import csv
import requests
import psycopg2
from psycopg2 import sql

# ANSI color codes
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    BLUE = '\033[0;34m'
    YELLOW = '\033[1;33m'
    NC = '\033[0m'  # No Color

def log_info(step, message):
    """Print info message with step number"""
    print(f"{Colors.BLUE}[{step}]{Colors.NC} {message}")

def log_success(message):
    """Print success message"""
    print(f"{Colors.GREEN}✓{Colors.NC} {message}")

def log_error(message):
    """Print error message"""
    print(f"{Colors.RED}✗{Colors.NC} {message}")

def log_warning(message):
    """Print warning message"""
    print(f"{Colors.YELLOW}⚠{Colors.NC} {message}")

def wait_for_postgres(db_config, max_retries=60):
    """Wait for PostgreSQL to be ready"""
    log_info("1/10", "Waiting for database to be ready...")
    
    for attempt in range(max_retries):
        try:
            conn = psycopg2.connect(**db_config)
            conn.close()
            log_success("Database connection successful")
            return True
        except psycopg2.OperationalError:
            log_warning(f"Waiting for database... ({attempt + 1}/{max_retries})")
            time.sleep(1)
    
    log_error(f"Database did not become ready in {max_retries} seconds")
    return False

def wait_for_tables(db_config, max_retries=60):
    """Wait for database tables to be created by Flyway migrations"""
    log_info("1/10", "Waiting for database tables to be created...")
    log_warning("(Make sure the backend is running to create tables)")
    
    for attempt in range(max_retries):
        try:
            conn = psycopg2.connect(**db_config)
            cursor = conn.cursor()
            cursor.execute("SELECT 1 FROM clinic LIMIT 1;")
            cursor.close()
            conn.close()
            log_success("Tables found")
            return True
        except psycopg2.Error:
            log_warning(f"Waiting for tables... ({attempt + 1}/{max_retries})")
            time.sleep(1)
    
    log_error(f"Tables not created after {max_retries} seconds")
    log_warning("Please ensure the Spring Boot backend is running (it creates tables automatically)")
    return False

def verify_migrations(db_config):
    """Verify Flyway migrations have run"""
    log_info("", "Verifying Flyway migrations...")
    
    try:
        conn = psycopg2.connect(**db_config)
        cursor = conn.cursor()
        cursor.execute("SELECT version, description FROM flyway_schema_history ORDER BY installed_rank;")
        migrations = cursor.fetchall()
        cursor.close()
        conn.close()
        
        if not migrations:
            log_error("No Flyway migrations found")
            log_warning("Please ensure the Spring Boot backend has started and run migrations")
            return False
        
        log_success("Migrations verified:")
        for version, description in migrations:
            print(f"  - {version}: {description}")
        return True
        
    except psycopg2.Error as e:
        log_error(f"Failed to verify migrations: {e}")
        return False

def verify_auth_trigger(db_config):
    """Verify auth trigger exists"""
    log_info("", "Verifying auth trigger...")
    
    try:
        conn = psycopg2.connect(**db_config)
        cursor = conn.cursor()
        
        # Check if function exists in public schema
        cursor.execute("""
            SELECT EXISTS(
                SELECT 1 FROM pg_proc p 
                JOIN pg_namespace n ON p.pronamespace = n.oid 
                WHERE p.proname = 'handle_new_user' AND n.nspname = 'public'
            );
        """)
        function_exists = cursor.fetchone()[0]
        
        # Check if trigger exists on auth.users table
        cursor.execute("""
            SELECT EXISTS(
                SELECT 1 FROM pg_trigger 
                WHERE tgname = 'on_auth_user_created'
            );
        """)
        trigger_exists = cursor.fetchone()[0]
        
        cursor.close()
        conn.close()
        
        if function_exists and trigger_exists:
            log_success("Auth trigger function and trigger on auth.users exist")
        elif function_exists:
            log_warning("Warning: Function exists but trigger on auth.users not found (insufficient privileges)")
        elif trigger_exists:
            log_warning("Warning: Trigger exists but function not found")
        else:
            log_warning("Warning: Neither function nor trigger found. V2 migration may not have run successfully.")
        
        return True
        
    except psycopg2.Error as e:
        log_warning(f"Could not verify auth trigger: {e}")
        return True  # Non-critical, continue anyway

def load_csv_to_table(db_config, table_name, csv_file, columns):
    """Load CSV data into a table using COPY command"""
    try:
        conn = psycopg2.connect(**db_config)
        cursor = conn.cursor()
        
        with open(csv_file, 'r') as f:
            # Skip header
            next(f)
            cursor.copy_expert(
                sql.SQL("COPY {}({}) FROM STDIN WITH CSV").format(
                    sql.Identifier(table_name),
                    sql.SQL(', ').join(map(sql.Identifier, columns))
                ),
                f
            )
        
        conn.commit()
        cursor.close()
        conn.close()
        return True
        
    except psycopg2.Error as e:
        log_error(f"Failed to load {table_name}: {e}")
        return False

def execute_sql(db_config, query):
    """Execute SQL query and return result"""
    try:
        conn = psycopg2.connect(**db_config)
        cursor = conn.cursor()
        cursor.execute(query)
        
        # Check if this is a SELECT query
        if cursor.description:
            result = cursor.fetchall()
        else:
            result = None
        
        conn.commit()
        cursor.close()
        conn.close()
        return result
        
    except psycopg2.Error as e:
        log_error(f"SQL error: {e}")
        return None

def create_auth_users(supabase_url, service_key, mock_password):
    """Create auth users via Supabase Admin API"""
    log_info("5/10", "Creating Auth users...")
    
    users = []
    with open('./sample-data/user_profile.csv', 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            users.append({
                'email': row['email'],
                'password': mock_password,
                'user_metadata': {'name': row['name']},
                'email_confirm': True
            })
    
    print(f"📧 Creating {len(users)} auth users...")
    
    success_count = 0
    for user in users:
        try:
            response = requests.post(
                f"{supabase_url}/auth/v1/admin/users",
                headers={
                    'Authorization': f'Bearer {service_key}',
                    'apikey': service_key,
                    'Content-Type': 'application/json'
                },
                json=user,
                timeout=30
            )
            
            if response.status_code == 200:
                success_count += 1
            else:
                log_warning(f"Failed to create {user['email']}: {response.status_code}")
                
        except requests.exceptions.RequestException as e:
            log_warning(f"Network error for {user['email']}: {e}")
    
    log_success(f"Auth users created ({success_count}/{len(users)})")
    return True

def load_user_profiles_and_appointments(db_config):
    """Load user profiles and appointments with ID mapping"""
    log_info("6,7/10", "Updating User_Profile and loading related data...")
    
    try:
        conn = psycopg2.connect(**db_config)
        cursor = conn.cursor()
        
        # Create temp table for user profiles
        cursor.execute("""
            CREATE TEMP TABLE temp_user_profiles (
                user_id UUID,
                name VARCHAR(255),
                role CHAR(1),
                email VARCHAR(255),
                telephone_number VARCHAR(20),
                clinic_id INTEGER
            );
        """)
        
        # Load user profile CSV
        with open('./sample-data/user_profile.csv', 'r') as f:
            next(f)  # Skip header
            cursor.copy_expert(
                """COPY temp_user_profiles FROM STDIN WITH CSV""",
                f
            )
        
        # Update existing user_profiles
        cursor.execute("""
            UPDATE user_profile 
            SET 
                name = tup.name,
                role = tup.role,
                telephone_number = tup.telephone_number,
                clinic_id = tup.clinic_id
            FROM temp_user_profiles tup
            WHERE user_profile.email = tup.email;
        """)
        
        # Create user ID mapping
        cursor.execute("""
            CREATE TEMP TABLE user_id_mapping AS
            SELECT 
                tup.user_id as old_user_id,
                up.user_id as new_user_id
            FROM temp_user_profiles tup
            JOIN user_profile up ON tup.email = up.email;
        """)
        
        # Create temp table for appointments
        cursor.execute("""
            CREATE TEMP TABLE temp_appointments (
                appointment_id CHAR(10),
                patient_id UUID,
                doctor_id CHAR(10),
                start_datetime TIMESTAMP,
                end_datetime TIMESTAMP,
                status VARCHAR(20)
            );
        """)
        
        # Load appointment CSV
        with open('./sample-data/appointment.csv', 'r') as f:
            next(f)  # Skip header
            cursor.copy_expert(
                """COPY temp_appointments FROM STDIN WITH CSV""",
                f
            )
        
        # Insert appointments with correct user IDs
        cursor.execute("""
            INSERT INTO appointment (appointment_id, patient_id, doctor_id, start_datetime, end_datetime, status)
            SELECT 
                ta.appointment_id,
                um.new_user_id as patient_id,
                ta.doctor_id,
                ta.start_datetime,
                ta.end_datetime,
                ta.status
            FROM temp_appointments ta
            JOIN user_id_mapping um ON ta.patient_id = um.old_user_id;
        """)
        
        # Get counts
        cursor.execute('SELECT COUNT(*) FROM user_profile;')
        user_count = cursor.fetchone()[0]
        
        cursor.execute('SELECT COUNT(*) FROM appointment;')
        appointment_count = cursor.fetchone()[0]
        
        conn.commit()
        cursor.close()
        conn.close()
        
        log_success(f"Updated user profiles (total: {user_count} users)")
        log_success(f"Loaded {appointment_count} appointments")
        return user_count, appointment_count
        
    except psycopg2.Error as e:
        log_error(f"Failed to load user profiles and appointments: {e}")
        return 0, 0

def main():
    """Main seeding process"""
    print(f"{Colors.BLUE}========================================{Colors.NC}")
    print(f"{Colors.BLUE}  PostgreSQL Database Seeding{Colors.NC}")
    print(f"{Colors.BLUE}========================================{Colors.NC}")
    print()
    
    # Get database configuration from environment
    db_config = {
        'dbname': os.getenv('DB_NAME', 'postgres'),
        'user': os.getenv('DB_USER', 'postgres'),
        'password': os.getenv('DB_PASSWORD'),
        'host': os.getenv('DB_HOST', 'supabase-db'),
        'port': os.getenv('DB_PORT', '5432')
    }
    
    supabase_url = os.getenv('API_EXTERNAL_URL')
    service_key = os.getenv('SERVICE_ROLE_KEY')
    mock_password = os.getenv('MOCK_USER_PASSWORD')
    
    # Validate environment variables
    if not all([db_config['password'], supabase_url, service_key, mock_password]):
        log_error("Missing required environment variables")
        log_warning("Required: DB_PASSWORD, API_EXTERNAL_URL, SERVICE_ROLE_KEY, MOCK_USER_PASSWORD")
        sys.exit(1)
    
    # Wait for database
    if not wait_for_postgres(db_config):
        sys.exit(1)
    
    # Wait for tables (created by Flyway migrations)
    if not wait_for_tables(db_config):
        sys.exit(1)
    
    # Verify migrations
    if not verify_migrations(db_config):
        sys.exit(1)
    
    # Verify auth trigger
    verify_auth_trigger(db_config)
    
    # Load Clinic data
    print()
    log_info("2/10", "Loading Clinic data...")
    if load_csv_to_table(db_config, 'clinic', './sample-data/clinics.csv', 
                         ['name', 'address', 'telephone_number', 'type', 'opening_hours', 'closing_hours']):
        result = execute_sql(db_config, 'SELECT COUNT(*) FROM clinic;')
        clinic_count = result[0][0] if result else 0
        log_success(f"Loaded {clinic_count} clinics")
    else:
        sys.exit(1)
    
    # Load Doctor data
    print()
    log_info("3/10", "Loading Doctor data...")
    if load_csv_to_table(db_config, 'doctor', './sample-data/doctor.csv',
                         ['doctor_id', 'name', 'clinic_id', 'appointment_duration_in_minutes']):
        result = execute_sql(db_config, 'SELECT COUNT(*) FROM doctor;')
        doctor_count = result[0][0] if result else 0
        log_success(f"Loaded {doctor_count} doctors")
    else:
        sys.exit(1)
    
    # Create Auth users
    print()
    if not create_auth_users(supabase_url, service_key, mock_password):
        sys.exit(1)
    
    # Load User Profiles and Appointments
    print()
    user_count, appointment_count = load_user_profiles_and_appointments(db_config)
    if user_count == 0:
        sys.exit(1)
    
    # Load Schedule data
    print()
    log_info("8/10", "Loading Schedule data...")
    if load_csv_to_table(db_config, 'schedule', './sample-data/schedule.csv',
                         ['schedule_id', 'doctor_id', 'start_datetime', 'end_datetime', 'type']):
        result = execute_sql(db_config, 'SELECT COUNT(*) FROM schedule;')
        schedule_count = result[0][0] if result else 0
        log_success(f"Loaded {schedule_count} schedules")
    else:
        sys.exit(1)
    
    # Load Medical_Summary data
    print()
    log_info("9/10", "Loading Medical_Summary data...")
    if load_csv_to_table(db_config, 'medical_summary', './sample-data/medical_summary.csv',
                         ['summary_id', 'appointment_id', 'treatment_summary']):
        result = execute_sql(db_config, 'SELECT COUNT(*) FROM medical_summary;')
        medical_count = result[0][0] if result else 0
        log_success(f"Loaded {medical_count} medical summaries")
    else:
        sys.exit(1)
    
    # Load Queue_Ticket data
    print()
    log_info("10/10", "Loading Queue_Ticket data...")
    if load_csv_to_table(db_config, 'queue_ticket', './sample-data/queue_ticket.csv',
                         ['appointment_id', 'status', 'check_in_time', 'queue_number', 'is_fast_tracked', 
                          'fast_track_reason', 'ticket_number_for_day', 'consultation_start_time', 'consultation_complete_time']):
        result = execute_sql(db_config, 'SELECT COUNT(*) FROM queue_ticket;')
        queue_count = result[0][0] if result else 0
        log_success(f"Loaded {queue_count} queue tickets")
    else:
        sys.exit(1)
    
    # Drop temporary tables
    print()
    log_info("", "Cleaning temporary tables...")
    execute_sql(db_config, "DROP TABLE IF EXISTS temp_user_profiles;")
    execute_sql(db_config, "DROP TABLE IF EXISTS user_id_mapping;")
    execute_sql(db_config, "DROP TABLE IF EXISTS temp_appointments;")
    log_success("Temporary tables cleaned")
    
    # Final summary
    print()
    print(f"{Colors.GREEN}========================================{Colors.NC}")
    print(f"{Colors.GREEN}  Seeding Complete!{Colors.NC}")
    print(f"{Colors.GREEN}========================================{Colors.NC}")
    print("Summary:")
    print(f"  - Clinics: {clinic_count}")
    print(f"  - Users: {user_count}")
    print(f"  - Doctors: {doctor_count}")
    print(f"  - Schedules: {schedule_count}")
    print(f"  - Appointments: {appointment_count}")
    print(f"  - Medical Summaries: {medical_count}")
    print(f"  - Queue Tickets: {queue_count}")
    print()

if __name__ == "__main__":
    main()
