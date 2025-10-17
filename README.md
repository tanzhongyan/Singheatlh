# OOP

Appointment and Queue Management System for SingHealth Clinics

## Fullstack Project: Vite + React Frontend & Spring Boot Backend

This repository contains a **fullstack web application** with:

- **Frontend**: [Vite](https://vitejs.dev/) + React
- **Backend**: [Spring Boot](https://spring.io/projects/spring-boot)

## 📂 Folder Structure

```
├── vite-react-frontend/ # React frontend (Vite)
├── springboot-backend/  # Spring Boot backend (Java)
└── db/                  # Docker Supabase database
```

Refer to [ARCHITECTURE.md](./ARCHITECTURE.md) to understand more on the layered architecture pattern.

Refer to [development.md](./development.md) for API endpoints and database migration guide.

## 🚀 Getting Started

### Prerequisites

- **Frontend**: Node.js (v18+) and npm installed → [Download Node.js](https://nodejs.org/)
- **Backend**: JDK 17+ installed  
  (Maven **not required**, the project uses Maven Wrapper: `mvnw` / `mvnw.cmd`)
- **Database**: Docker and Docker Compose → [Download Docker](https://www.docker.com/products/docker-desktop/)

---

## 🐳 Database Setup with Docker (Supabase)

This project uses a local Supabase instance running in Docker containers for development.

### 1. Setup Environment Variables

First, copy the environment example file and configure it:

```bash
# Navigate to the database directory
cd db

# Copy the environment example file (Windows PowerShell)
Copy-Item .env.example .env

# Or on macOS/Linux
cp .env.example .env
```

**Important**: The `.env.example` file contains default values that work for local development. You can use these values as-is for development, but make sure to change them before going to production.

### 2. Start the Database Services

```bash
# Navigate to the database directory (if not already there)
cd db

# Start all Supabase services
docker compose up

# Or run in detached mode (background)
docker compose up -d
```

This will start **6 services**:
- **PostgreSQL** - Database server
- **Kong** - API Gateway
- **GoTrue** - Authentication service
- **PostgREST** - REST API for database
- **pg-meta** - Database metadata service
- **Studio** - Web-based database GUI

### 3. Access Supabase Studio

Once the containers are running, you can access:

- **Supabase Studio Dashboard**: <http://localhost:8000> (login required)
- **Database Direct Connection**: `localhost:5434`
- **Auth API**: <http://localhost:8000/auth/v1>
- **REST API**: <http://localhost:8000/rest/v1>

**Studio Login Credentials:**
- **Username**: `supabase`
- **Password**: `postgres`

### 4. Spring Boot Database Configuration

Before running the Spring Boot backend, set up your database connection:

```bash
# Navigate to the backend resources directory
cd springboot-backend/src/main/resources

# Copy the application.properties file
cp application.properties.example application.properties
```

**Important**: Open `application.properties` and update the database password to match your `.env` file in the `db` folder (default: `your-super-secret-and-long-postgres-password`).

### 5. Stop the Database Services

```bash
# Stop services (preserves data)
docker compose down

# Stop and remove all data (complete reset)
docker compose down -v
```

**Note**: Data is **not persistent** by default. Running `docker compose down -v` will reset the database to its initial state.

### 6. Preventing Auto-Start on Reboot

The containers are configured with `restart: no`, which means they won't automatically start when you restart your computer or Docker Desktop. You'll need to manually run `docker compose up -d` each time you want to start the database.

If you want containers to auto-start after reboot, change `restart: no` to `restart: unless-stopped` in `docker-compose.yml`.

---

## 🗄️ Database Migrations

The Spring Boot backend uses **Flyway** for automatic database migrations. When you start the backend for the first time, it will:

1. ✅ Automatically create all database tables (Clinic, User_Profile, Doctor, Schedule, Appointment, etc.)
2. ✅ Set up Supabase authentication integration (auto-create user profiles on signup)
3. ✅ Apply all schema changes in version order

**No manual database setup required!** Just start the backend and Flyway handles the rest.

📖 **To make database changes**, see the "Database Migrations" section in [development.md](./development.md)

---

## ▶️ Running the Application

### Step 1: Start the Database

```bash
cd db
docker compose up -d
```

### Step 2: Start the Backend

```bash
cd springboot-backend

# macOS/Linux
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

**First-time setup**: On the first run, the backend will automatically:
- Download dependencies (including Flyway)
- Connect to the database
- Run all migration scripts
- Create all tables and triggers

### Step 3: Start the Frontend

```bash
cd vite-react-frontend
npm install
npm run dev
```

### Step 4: Access the Application

- **Frontend**: <http://localhost:5173>
- **Backend API**: <http://localhost:8080>
- **Supabase Studio**: <http://localhost:8000> (login: supabase/postgres)
- **Database Direct**: `localhost:5434` (user: postgres)

---

## 🔧 Development Workflow

**Recommended startup sequence:**

1. **Start the database**: `cd db && docker compose up -d`
2. **Start the backend**: `cd springboot-backend && .\mvnw.cmd spring-boot:run` (Windows) or `./mvnw spring-boot:run` (Mac/Linux)
3. **Start the frontend**: `cd vite-react-frontend && npm run dev`
4. **Access the application**: Open <http://localhost:5173> in your browser

**When you're done developing:**

```bash
# Stop frontend: Press Ctrl+C in the terminal
# Stop backend: Press Ctrl+C in the terminal
# Stop database: cd db && docker compose down
```

---

## 🐛 Troubleshooting

### "JAVA_HOME environment variable is not defined correctly"

If you get this error when running `.\mvnw.cmd spring-boot:run`, you need to set the JAVA_HOME environment variable.

**Quick Fix (Windows PowerShell - Temporary):**

```powershell
# Set JAVA_HOME for current session only
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"

# Now run Maven
.\mvnw.cmd spring-boot:run
```

**Permanent Fix (Windows):**

1. Find your Java installation path:
   ```powershell
   (Get-Command java).Source
   # Example output: C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot\bin\java.exe
   ```
2. Copy the path WITHOUT `\bin\java.exe` (e.g., `C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot`)

3. Set JAVA_HOME permanently:
   - Press `Win + X` and select "System"
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Under "System variables", click "New"
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot` (your path)
   - Click OK on all dialogs
   - **Restart your terminal** (or restart VS Code)

4. Verify it works:
   ```powershell
   echo $env:JAVA_HOME
   # Should show: C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot
   ```

**Alternative: Run with Java directly (bypass Maven Wrapper)**

If you don't want to set JAVA_HOME, you can use Maven directly:

```powershell
# Navigate to backend
cd springboot-backend

# Run with Maven (if installed)
mvn spring-boot:run
```

### Other Common Issues

| Problem | Solution |
|---------|----------|
| Port 8080 already in use | Another app is using port 8080. Stop it or change Spring Boot port in `application.properties`: `server.port=8081` |
| Database connection refused | Make sure Docker containers are running: `cd db && docker compose up -d` |
| Cannot access Studio at localhost:8000 | Check containers are healthy: `docker ps`. Restart if needed: `cd db && docker compose restart` |
| Flyway migration failed | Reset database: `cd db && docker compose down -v && docker compose up -d` |
