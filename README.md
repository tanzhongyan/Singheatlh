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

- **Node.js** (v18+) and npm → [Download Node.js](https://nodejs.org/)
- **JDK 21+** → Installed automatically by Maven Wrapper, or [Download Java](https://adoptium.net/)
- **Docker Desktop** → [Download Docker](https://www.docker.com/products/docker-desktop/)

### One-Time Setup

Before running the app for the first time, copy the configuration files:

**Windows (PowerShell):**

```powershell
Copy-Item db\.env.example db\.env ; Copy-Item springboot-backend\src\main\resources\application.properties.example springboot-backend\src\main\resources\application.properties ; Copy-Item vite-react-frontend\.env.example vite-react-frontend\.env
```

**macOS/Linux:**

```bash
cp db/.env.example db/.env && cp springboot-backend/src/main/resources/application.properties.example springboot-backend/src/main/resources/application.properties && cp vite-react-frontend/.env.example vite-react-frontend/.env
```

> **Note**: Default values work for local development. Update passwords before production!

### Quick Start (Automated)

```bash
# 1. Install dependencies
npm install

# 2. Start everything (database + backend + frontend)
npm run dev:setup
```

**That's it!** Your app will be running at:

- **Frontend**: <http://localhost:5173>
- **Backend**: <http://localhost:8080>
- **Database Studio**: <http://localhost:8000> (login: `supabase` / `postgres`)

---

## 📋 Available Commands

| Command              | Description                                                         |
| -------------------- | ------------------------------------------------------------------- |
| `npm run dev:setup`  | **First-time setup**: Start database, build backend, run everything |
| `npm run dev`        | **Daily use**: Start backend + frontend (assumes DB is running)     |
| `npm run db:up`      | Start database containers                                           |
| `npm run db:down`    | Stop database and remove all data                                   |
| `npm run db:restart` | Restart database (useful for resets)                                |
| `npm run backend`    | Start Spring Boot backend only                                      |
| `npm run frontend`   | Start React frontend only                                           |

### Common Workflows

**Starting Fresh (First Time):**

```bash
npm install
npm run dev:setup
```

**Daily Development:**

```bash
npm run db:up        # Start database
npm run dev          # Start backend + frontend
```

**Reset Everything:**

```bash
npm run db:down      # Stop and clear database
npm run dev:setup    # Restart from scratch
```

---

## 🔧 Manual Setup (Alternative)

If you prefer to run components individually:

### 1. Database Setup

```bash
cd db

# Copy environment file (one-time)
Copy-Item .env.example .env    # Windows PowerShell
# or: cp .env.example .env     # macOS/Linux

# Start database
docker compose up -d

# Access Studio at http://localhost:8000
# Login: supabase / postgres
```

### 2. Backend Setup

```bash
cd springboot-backend

# Copy config file (one-time)
Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties

# Start backend
.\mvnw.cmd spring-boot:run     # Windows
# or: ./mvnw spring-boot:run   # macOS/Linux
```

**First run**: Flyway automatically creates all database tables.

### 3. Frontend Setup

```bash
cd vite-react-frontend

# Install and start
npm install
npm run dev
```

## � Documentation

- **[ARCHITECTURE.md](./ARCHITECTURE.md)**: Layered architecture pattern and project structure
- **[development.md](./development.md)**: API endpoints reference, database migrations guide, CORS configuration

---

## 🗄️ Database Info

- **PostgreSQL** on port **5434** (not default 5432 to avoid conflicts)
- **Supabase Studio** at <http://localhost:8000> (login: `supabase` / `postgres`)
- **Flyway migrations** run automatically on backend startup
- **Data is not persistent** - stopping containers resets the database

---

## 🔍 What Happens on First Run

When you run `npm run dev:setup`, here's what happens automatically:

1. ✅ **Database**: Docker starts 6 containers (PostgreSQL, Kong, GoTrue, PostgREST, pg-meta, Studio)
2. ✅ **Backend**: Maven downloads dependencies, Flyway creates all tables/triggers
3. ✅ **Frontend**: npm installs React, Vite, and dependencies
4. ✅ **Ready**: All services running and connected

**Database tables created by Flyway:**

- `Clinic`, `User_Profile`, `Doctor`, `Schedule`, `Appointment`, `Queue_Ticket`, `Medical_Summary`
- Authentication trigger for auto-creating user profiles on Supabase signup

---

## 🐛 Troubleshooting

### Database Not Starting

```bash
# Check if containers are running
docker ps

# View logs
docker compose logs

# Restart everything
npm run db:restart
```

### Port Conflicts

| Port | Service       | Solution                                         |
| ---- | ------------- | ------------------------------------------------ |
| 5434 | PostgreSQL    | Change `POSTGRES_EXTERNAL_PORT` in `db/.env`     |
| 8080 | Spring Boot   | Change `server.port` in `application.properties` |
| 8000 | Supabase Kong | Change port mapping in `docker-compose.yml`      |
| 5173 | Vite          | Change port in `vite.config.js`                  |

### Backend Won't Start

**Error: "Connection refused to database"**

```bash
npm run db:up  # Make sure database is running
```

**Error: "Flyway migration failed"**

```bash
npm run db:restart  # Reset database and migrations
```

### JAVA_HOME Not Set (Windows)

If Maven can't find Java:

```powershell
# Temporary fix (current session only)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"
npm run backend

# Permanent fix: Set as Windows environment variable
# Settings → System → Advanced → Environment Variables → New
# Variable: JAVA_HOME
# Value: C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot
```

### Frontend Issues

```bash
# Clear node_modules and reinstall
cd vite-react-frontend
Remove-Item -Recurse -Force node_modules
npm install
npm run dev
```

### CORS Errors

CORS is configured for `http://localhost:5173` only. If you change the frontend port, update:

- `springboot-backend/src/main/java/Singheatlh/springboot_backend/config/SecurityConfig.java`
- Change `allowedOrigins` to match your new port

---

## 📖 Additional Resources

- **API Documentation**: See [development.md](./development.md) for all 74 available endpoints
- **Database Migrations**: Add new tables/columns using Flyway - guide in [development.md](./development.md)
- **Architecture**: Understand the layered pattern in [ARCHITECTURE.md](./ARCHITECTURE.md)

---

## 🛠️ Tech Stack

**Frontend:**

- React 19 + Vite
- React Router v6
- Bootstrap 5
- Axios
- Supabase JS Client

**Backend:**

- Spring Boot 4.0.0-M3
- Hibernate 7.1.1
- Flyway 11.13.1
- PostgreSQL 15

**Infrastructure:**

- Docker + Docker Compose
- Supabase (local)
- Maven Wrapper

---

## 👥 Contributors

See the [GitHub contributors page](https://github.com/jovibong/OOP/graphs/contributors) for the full list.

---

## 📝 License

This project is licensed under the ISC License.
