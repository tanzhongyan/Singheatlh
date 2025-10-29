# Development Guide

This document outlines reusable components, API endpoints, and database migration procedures for developers.

---

## Local Development Environment

### Database Services

The project uses a minimal Supabase setup with the following services:

| Service | Container | Port | Purpose |
|---------|-----------|------|---------|
| PostgreSQL | `supabase-db` | 5434 → 5432 | Main database |
| Kong | `supabase-kong` | 8000, 8443 | API Gateway |
| GoTrue | `supabase-auth` | (internal) | Authentication |
| PostgREST | `supabase-rest` | (internal) | REST API |
| pg-meta | `supabase-meta` | (internal) | Database metadata |
| Studio | `supabase-studio` | (via Kong) | Web GUI |

### Access Points

- **Studio GUI**: <http://localhost:8000> (login: `supabase` / `postgres`)
- **Database**: `localhost:5434` (user: `postgres`)
- **Auth API**: <http://localhost:8000/auth/v1>
- **REST API**: <http://localhost:8000/rest/v1>
- **GraphQL**: <http://localhost:8000/graphql/v1>

### Database Connection Strings

**Spring Boot** (`application.properties`):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/postgres
spring.datasource.username=postgres
spring.datasource.password=your-super-secret-and-long-postgres-password
```

**Direct psql connection**:
```bash
psql -h localhost -p 5434 -U postgres -d postgres
```

### Quick Start

Run this single command to set up everything:

```bash
npm run dev:setup
```

**What happens automatically:**

1. ✅ Installs npm dependencies
2. ✅ **Resets the database** (removes all existing data and containers)
3. ✅ Starts Docker database (PostgreSQL + Supabase services)
4. ✅ Builds Spring Boot backend
5. ✅ **Backend starts → Flyway runs migrations:**
   - V1: Creates database schema
   - V2: Creates auth trigger
6. ✅ Starts React frontend
7. ✅ Opens both backend (port 8080) and frontend (port 5173)
8. ✅ **Automatically seeds database with ~600K records** (waits 10 seconds for backend to start)

**Result:** Fully populated database with realistic test data ready to use!

**To manually re-seed data** (if you reset the database):

```bash
npm run db:seed
```

---

## Database Migrations with Flyway

### Why Flyway?

- **SQL-first approach** - Write plain SQL, no XML/YAML
- **Version control** - Track schema changes in Git
- **Automatic execution** - Migrations run on app startup
- **Team sync** - Everyone gets the same database schema

### How It Works

Migrations are numbered SQL files (V1, V2, V3...) that run **sequentially** and **only once**:

```
V1__Initial_Schema.sql        → Runs first (creates tables)
V2__Create_Auth_Trigger.sql   → Runs second (adds trigger)
V3__Your_New_Feature.sql      → Runs third (when you add it)
```

Flyway tracks applied migrations in `flyway_schema_history` table. Once applied, a migration never runs again.

---

## Making Database Changes

### Step 1: Create Migration File

**Location:** `springboot-backend/src/main/resources/db/migration/`

**Naming:** `V{number}__{description}.sql`
- Use **double underscore** `__`
- Sequential numbering (V3, V4, V5...)
- Descriptive names

**Examples:**
- ✅ `V3__Add_Appointment_Priority.sql`
- ✅ `V4__Create_Notification_Table.sql`
- ❌ `V3_Add_Feature.sql` (single underscore)

### Step 2: Write SQL

**Add a column:**
```sql
ALTER TABLE Appointment 
ADD COLUMN priority VARCHAR(10) DEFAULT 'NORMAL' 
CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'));

CREATE INDEX idx_appointment_priority ON Appointment(priority);

COMMENT ON COLUMN Appointment.priority IS 'Priority level';
```

**Create a table:**
```sql
CREATE TABLE Notification (
    notification_id CHAR(10) PRIMARY KEY,
    user_id UUID NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) 
        REFERENCES User_Profile(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_user ON Notification(user_id);
CREATE INDEX idx_notification_read ON Notification(is_read);
```

**Modify existing column:**
```sql
ALTER TABLE Doctor ALTER COLUMN doctor_id TYPE VARCHAR(20);
```

### Step 3: Test Locally

```bash
# Start database if not running
cd db && docker compose up -d

# Restart backend (migration runs automatically)
cd springboot-backend
.\mvnw.cmd spring-boot:run
```

### Step 4: Verify

```sql
-- Check migration was applied
SELECT version, description, installed_on, success 
FROM flyway_schema_history 
ORDER BY installed_rank;

-- Verify table structure
\d Appointment
```

---

## Current Database Schema

### V1: Initial Schema
- **Tables:** Clinic, User_Profile, Doctor, Schedule, Appointment, Queue_Ticket, Medical_Summary
- **Constraints:** All primary keys, foreign keys, check constraints
- **Indexes:** 13 performance indexes
- **Queue_Ticket Status Constraint:** Valid statuses are `CHECKED_IN`, `CALLED`, `IN_CONSULTATION`, `COMPLETED`, `NO_SHOW`, `FAST_TRACKED`

### V2: Auth Trigger
- **Purpose:** Auto-create User_Profile when users sign up via Supabase
- **Behavior:** New users get default role 'P' (Patient)

---

## Sample Data Population

Sample data is **automatically loaded** during `npm run dev:setup`.

**Automatic seeding workflow:**

1. `npm run dev:setup` starts the database
2. Waits for database to be ready
3. Runs `npm run db:seed` automatically
4. Loads all CSV files using psql's `\copy` command
5. Then builds and starts backend (Flyway creates schema)
6. Finally starts frontend

**What gets loaded:**
- Clinic: ~1,700 records
- User_Profile: ~3,300 records (1 admin, 2,552 staff, 800 patients)
- Doctor: ~5,896 records (2-5 per clinic)
- Schedule: ~601,170 records (Oct 22 - Nov 15, 2025)
- Appointment: 5,000 records
- Medical_Summary: ~1,243 records (only completed appointments)
- Queue_Ticket: ~199 records (only today's appointments)

**To manually re-seed** (after database reset):

```bash
npm run db:seed
```

**Technical Details:**
- Uses psql's `\copy` command (doesn't require superuser privileges)
- CSV files are pre-generated using `db/generate_mock_data.py`
- Script location: `db/seed-data.sh`
- Requirements: PostgreSQL container must be running

---

## Mock Data Generation

### Generating Sample Data

A Python script is provided to generate realistic, relationally-consistent mock data for testing and development.

**Location:** `db/generate_mock_data.py`

**Requirements:**
- Python 3.7+
- Existing clinic data in `db/sample-data/clinics.csv`

**Usage:**
```bash
# Run from project root
python db/generate_mock_data.py
```

**What it generates:**

| File | Records | Description |
|------|---------|-------------|
| `user_profile.csv` | ~3000+ | 1 system admin, 1-2 staff per clinic, 800 patients |
| `doctor.csv` | ~6000+ | 2-5 doctors per clinic |
| `schedule.csv` | ~600000+ | Schedules from 7 days ago through Nov 15, 2025 with AVAILABLE/UNAVAILABLE blocks |
| `appointment.csv` | 5000 | Appointments in 15-min slots, with past/present/future dates |
| `medical_summary.csv` | ~1200+ | Summaries for all completed appointments |
| `queue_ticket.csv` | ~200 | Tickets for today's appointments with realistic statuses |

**Data Integrity:**
- All foreign key relationships are maintained
- Appointments fit within doctor's AVAILABLE schedule blocks
- No overlapping appointments for same doctor
- Schedule blocks respect clinic opening/closing hours
- Queue tickets only for today's appointments
- Medical summaries only for completed appointments

**Realistic Queue/Appointment Scenarios:**

The script simulates realistic patient arrival patterns for today's appointments:

| Scenario | Probability | Behavior | Result |
|----------|-------------|----------|--------|
| **Early arrival** | 35% | Arrive 5-30 min before appointment | Queue ticket with `CHECKED_IN` status |
| **On-time** | 30% | Arrive 0-5 min before appointment | Queue ticket with `CHECKED_IN` status |
| **Late arrival** | 20% | Arrive 5-20 min after appointment | Queue ticket with late check-in time |
| **Very late** | 10% | Arrive 20-40 min after appointment | Queue ticket with very late check-in |
| **No-show** | 5% | Don't show up at all | No queue ticket, appointment marked as `Missed` |

Queue tickets for appointments earlier today (before 2pm) will show progression through the queue:
- `COMPLETED` - For appointments that happened 1+ hours ago
- `IN_CONSULTATION` - For appointments 30-60 minutes ago
- `CALLED` - For recent appointments
- `CHECKED_IN` - For future appointments today

**Configuration:**
Edit these constants in `generate_mock_data.py` to adjust data volume:
```python
NUM_PATIENTS = 800                          # Number of patient users
NUM_APPOINTMENTS = 5000                     # Total appointments to generate
TODAY = datetime(2025, 10, 29)              # Reference date for "today"
SCHEDULE_END_DATE = datetime(2025, 11, 15)  # Schedules extend to this date

# Adjust arrival probabilities
ARRIVAL_SCENARIOS = {
    "early": 0.35,      # 35% arrive 5-30 min early
    "on_time": 0.30,    # 30% arrive 0-5 min early
    "late": 0.20,       # 20% arrive 5-20 min late
    "very_late": 0.10,  # 10% arrive 20-40 min late
    "no_show": 0.05     # 5% don't show up
}
```

---

## Best Practices

### DO:
- ✅ Use sequential numbering (V1, V2, V3...)
- ✅ Use descriptive file names
- ✅ Test migrations locally before committing
- ✅ One logical change per migration
- ✅ Add indexes for columns used in WHERE/JOIN
- ✅ Add comments for complex logic

### DON'T:
- ❌ Modify migrations that have been applied
- ❌ Delete migration files (breaks version history)
- ❌ Skip version numbers (must be sequential)
- ❌ Use `flyway:clean` in production (deletes all data!)

---

## Common Issues & Solutions

| Problem | Solution |
|---------|----------|
| "Migration checksum mismatch" | You modified an executed migration<br>`.\mvnw.cmd flyway:repair` |
| "Connection refused to database" | Database not running<br>`cd db && docker compose up -d` |
| Want to reset everything | `cd db && docker compose down -v && docker compose up -d`<br>Migrations will rerun on next startup |
| Want to see migration status | `.\mvnw.cmd flyway:info` |
| Studio not loading schemas | Check all containers are healthy: `docker ps`<br>Restart if needed: `docker compose restart` |
| Can't access Studio GUI | Access through Kong at `http://localhost:8000`<br>Login with: `supabase` / `postgres` |
| Port 5434 already in use | Change `POSTGRES_EXTERNAL_PORT` in `.env`<br>Update `application.properties` to match |

---

## Backend API Endpoints

**Base URL**: `http://localhost:8080/api`

**Note**: Most endpoints are currently set to `permitAll()` for development. In production, implement proper role-based access control.

---

### Authentication (`/api/auth`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/signup` | Register new user | `SignUpRequest` | `UserDto` or error |
| POST | `/login` | Authenticate user | `LoginRequest` | `UserDto` or 401 |
| GET | `/profile` | Get current user profile | - | `UserDto` |
| PUT | `/email` | Update user email | `UpdateEmailRequest` | Success message |
| PUT | `/password` | Change password | `ChangePasswordRequest` | Success message |
| POST | `/password/reset` | Request password reset | `ResetPasswordRequest` | Success message |
| POST | `/validate-token` | Validate JWT token | `TokenValidationRequest` | `TokenValidationResponse` |

---

### Users (`/api/users`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/{id}` | Get user by ID (UUID) | - | `UserDto` |
| GET | `/` | Get all users | - | `List<UserDto>` |
| GET | `/email/{email}` | Get user by email | - | `UserDto` |
| PUT | `/{id}` | Update user | `UserDto` | `UserDto` |
| DELETE | `/{id}` | Delete user | - | 204 No Content |

---

### Patients (`/api/patients`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/{id}` | Get patient by ID (UUID) | - | `PatientDto` |
| GET | `/` | Get all patients | - | `List<PatientDto>` |
| POST | `/` | Create new patient | `CreatePatientRequest` | `PatientDto` |
| PUT | `/{id}` | Update patient | `PatientDto` | `PatientDto` |
| DELETE | `/{id}` | Delete patient | - | 204 No Content |

---

### Clinic Staff (`/api/clinic-staff`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/` | Create clinic staff | `CreateClinicStaffRequest` | `ClinicStaffDto` |
| GET | `/{id}` | Get clinic staff by ID (UUID) | - | `ClinicStaffDto` |
| GET | `/` | Get all clinic staff | - | `List<ClinicStaffDto>` |
| PUT | `/{id}` | Update clinic staff | `ClinicStaffDto` | `ClinicStaffDto` |
| DELETE | `/{id}` | Delete clinic staff | - | 204 No Content |
| GET | `/search?name={name}` | Search by name | - | `List<ClinicStaffDto>` |
| GET | `/clinic/{clinicId}` | Get staff by clinic | - | `List<ClinicStaffDto>` |
| GET | `/email/{email}` | Get staff by email | - | `ClinicStaffDto` |

---

### System Administrators (`/api/system-administrators`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/` | Create system admin | `CreateSystemAdministratorRequest` | `SystemAdministratorDto` |
| GET | `/{id}` | Get admin by ID (UUID) | - | `SystemAdministratorDto` |
| GET | `/` | Get all admins | - | `List<SystemAdministratorDto>` |
| PUT | `/{id}` | Update admin | `SystemAdministratorDto` | `SystemAdministratorDto` |
| DELETE | `/{id}` | Delete admin | - | 204 No Content |

**Admin-specific operations:**

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/users` | Get all users (admin view) | - | `List<UserDto>` |
| POST | `/users/patient` | Create patient | `CreatePatientRequest` | `PatientDto` |
| POST | `/users/staff` | Create clinic staff | `CreateClinicStaffRequest` | `ClinicStaffDto` |
| PUT | `/users/{userId}` | Update user | `UserDto` | `UserDto` |
| DELETE | `/users/{userId}` | Delete user | - | 204 No Content |
| GET | `/doctors` | Get all doctors | - | `List<DoctorDto>` |
| POST | `/doctors` | Create doctor | `DoctorDto` | `DoctorDto` |
| PUT | `/doctors/{doctorId}` | Update doctor | `DoctorDto` | `DoctorDto` |
| DELETE | `/doctors/{doctorId}` | Delete doctor | - | 204 No Content |
| GET | `/clinics` | Get all clinics | - | `List<ClinicDto>` |
| PUT | `/clinics/{clinicId}/hours` | Update clinic hours | `UpdateClinicHoursRequest` | `ClinicDto` |
| POST | `/clinics/import` | Bulk import clinics | `List<ClinicDto>` | `List<ClinicDto>` |

---

### Doctors (`/api/doctor`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/{id}` | Get doctor by ID (String) | - | `DoctorDto` |
| GET | `/` | Get all doctors | - | `List<DoctorDto>` |
| GET | `/clinic/{clinicId}` | Get doctors by clinic | - | `List<DoctorDto>` |
| POST | `/` | Create doctor | `DoctorDto` | `DoctorDto` |
| PUT | `/` | Update doctor | `DoctorDto` | `DoctorDto` |
| DELETE | `/{id}` | Delete doctor | - | 204 No Content |

---

### Appointments (`/api/appointments`)

**Create & List:**

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/` | Create appointment | `CreateAppointmentRequest` | `AppointmentDto` |
| GET | `/` | Get all appointments | - | `List<AppointmentDto>` |
| GET | `/{id}` | Get appointment by ID (String) | - | `AppointmentDto` |

**Filter by Status:**

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/status/{status}` | Get by status (Upcoming/Completed/Cancelled/Missed/Ongoing) | `List<AppointmentDto>` |
| GET | `/upcoming` | Get upcoming appointments | `List<AppointmentDto>` |
| GET | `/completed` | Get completed appointments | `List<AppointmentDto>` |
| GET | `/cancelled` | Get cancelled appointments | `List<AppointmentDto>` |
| GET | `/missed` | Get missed appointments | `List<AppointmentDto>` |
| GET | `/ongoing` | Get ongoing appointments | `List<AppointmentDto>` |

**Filter by Patient/Doctor:**

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/patient/{patientId}` | Get all appointments for patient (UUID) | `List<AppointmentDto>` |
| GET | `/patient/{patientId}/upcoming` | Get upcoming appointments for patient | `List<AppointmentDto>` |
| GET | `/doctor/{doctorId}` | Get all appointments for doctor (String) | `List<AppointmentDto>` |
| GET | `/doctor/{doctorId}/upcoming` | Get upcoming appointments for doctor | `List<AppointmentDto>` |

**Update Operations:**

| Method | Endpoint | Description | Query Params | Response |
|--------|----------|-------------|--------------|----------|
| PUT | `/{id}/status` | Update appointment status | `?status={AppointmentStatus}` | `AppointmentDto` |
| PUT | `/{id}/cancel` | Cancel appointment | - | 200 OK |
| PUT | `/{id}/complete` | Mark as completed | - | `AppointmentDto` |
| PUT | `/{id}/mark-missed` | Mark as missed | - | `AppointmentDto` |
| PUT | `/{id}/start` | Start appointment (Ongoing) | - | `AppointmentDto` |
| PUT | `/{id}/reschedule` | Reschedule appointment | `?newDateTime={ISO-8601}` | `AppointmentDto` |

---

### Clinics (`/api/admin`)

**Admin Clinic Management:**

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/clinics` | Create clinic | `ClinicDto` | `ClinicDto` |
| GET | `/clinics` | Get all clinics | - | `List<ClinicDto>` |
| GET | `/clinics/{clinicId}` | Get clinic by ID | - | `ClinicDto` |
| GET | `/clinics/type/{type}` | Get clinics by type | - | `List<ClinicDto>` |
| PUT | `/clinics/{clinicId}` | Update clinic | `ClinicDto` | `ClinicDto` |
| DELETE | `/clinics/{clinicId}` | Delete clinic | - | 204 No Content |
| POST | `/clinics/import` | Bulk import clinics | `List<ClinicDto>` | `List<ClinicDto>` |
| PUT | `/clinics/{clinicId}/hours` | Update operating hours | `UpdateClinicHoursRequest` | `ClinicDto` |

---

### Data Types Reference

**ID Types:**
- **User IDs** (patients, clinic staff, admins): `UUID` (e.g., `"550e8400-e29b-41d4-a716-446655440000"`)
- **Doctor IDs**: `String` CHAR(10) (e.g., `"D000000001"`)
- **Appointment IDs**: `String` CHAR(10) (e.g., `"A000000001"`)
- **Clinic IDs**: `Integer` (e.g., `1`, `2`, `3`)

**Enums:**
- **Role**: `P` (Patient), `C` (Clinic Staff), `S` (System Administrator)
- **AppointmentStatus**: `Upcoming`, `Completed`, `Cancelled`, `Missed`, `Ongoing`

**DateTime Format:**
- Use ISO-8601 format: `2025-10-17T14:30:00` or `2025-10-17T14:30:00Z`

---

## Frontend Components

### API Client

- **`src/api/apiClient.js`**: A pre-configured Axios instance that automatically includes the base URL and JWT authorization header. 
  - **Usage**: `import apiClient from './api/apiClient'; apiClient.get('/users');`

### Admin Components

Located in `src/components/admin/`:

- **`AdminLayout.jsx`**: The main layout for the admin section, including a navigation bar. It uses `react-router-dom`'s `<Outlet />` to render child routes.
- **`AddUserModal.jsx` / `EditUserModal.jsx`**: Reusable modals for creating and editing users. They handle form state and submission.
- **`AddDoctorModal.jsx` / `EditDoctorModal.jsx`**: Reusable modals for creating and editing doctors.
- **`ClinicCard.jsx`**: A component that displays a single clinic's information and provides forms to update its settings directly.

### Protected Route

- **`src/components/ProtectedRoute.jsx`**: A wrapper component that checks for user authentication and role before allowing access to a route. Currently configured to allow access for demonstration purposes.
