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

### V2: Auth Trigger
- **Purpose:** Auto-create User_Profile when users sign up via Supabase
- **Behavior:** New users get default role 'P' (Patient)

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

All administrator endpoints are prefixed with `/api/system-administrators`.

### User Management

- `GET /api/system-administrators/users`: Get a list of all users.
- `POST /api/system-administrators/users/patient`: Create a new patient. 
  - **Body**: `CreatePatientRequest`
- `POST /api/system-administrators/users/staff`: Create a new clinic staff member.
  - **Body**: `CreateClinicStaffRequest`
- `PUT /api/system-administrators/users/{userId}`: Update a user's information (name, role).
  - **Body**: `UserDto`
- `DELETE /api/system-administrators/users/{userId}`: Delete a user.

### Doctor Management

- `GET /api/system-administrators/doctors`: Get a list of all doctors.
- `POST /api/system-administrators/doctors`: Create a new doctor.
  - **Body**: `DoctorDto`
- `PUT /api/system-administrators/doctors/{doctorId}`: Update a doctor's information.
  - **Body**: `DoctorDto`
- `DELETE /api/system-administrators/doctors/{doctorId}`: Delete a doctor.

### Clinic Management

- `GET /api/system-administrators/clinics`: Get a list of all clinics.
- `PUT /api/system-administrators/clinics/{clinicId}/hours`: Set a clinic's operating hours.
  - **Body**: `UpdateClinicHoursRequest` (`{ "openingHours": "HH:mm", "closingHours": "HH:mm" }`)
- `PUT /api/system-administrators/clinics/{clinicId}/slot-duration`: Set a clinic's appointment slot duration.
  - **Body**: `UpdateClinicSlotDurationRequest` (`{ "slotDuration": integer }`)
- `POST /api/system-administrators/clinics/import`: Bulk import a list of clinics.
  - **Body**: `List<ClinicDto>`

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
