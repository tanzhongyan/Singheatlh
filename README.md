# OOP

Appointment and Queue Management System for SingHealth Clinics

## Fullstack Project: Vite + React Frontend & Spring Boot Backend

This repository contains a **fullstack web application** with:

- **Frontend**: [Vite](https://vitejs.dev/) + React
- **Backend**: [Spring Boot](https://spring.io/projects/spring-boot)

---

## 📂 Folder Structure

```
├── vite-react-frontend/ # React frontend (Vite)
└── springboot-backend/  # Spring Boot backend (Java)
```

---

## 🏗️ Backend Architecture Overview

The Spring Boot backend follows a **layered architecture** pattern with clear separation of concerns. Here's how each layer works and interacts:

### **Layer Flow (Top to Bottom)**

```
Client Request → Controller → Service → Repository → Database
                     ↓           ↓          ↓
                    DTO ←   Mapper  ←   Entity
```

### **1. Entity Layer** (`entity/`)
**Purpose**: Represents database tables as Java objects using JPA annotations.

**Key Components**:
- **User hierarchy**: `User.java` (base class) → `Patient.java`, `ClinicStaff.java`, `SystemAdministrator.java`
- **Core entities**: `Clinic.java`, `Doctor.java`, `Appointment.java`, `QueueTicket.java`
- **Enums**: `Role.java`, `AppointmentStatus.java`, `QueueStatus.java`

**Annotations**: `@Entity`, `@OneToMany`, `@ManyToOne`, `@Inheritance`

**Interaction**: Entities are managed by the Repository layer and never directly exposed to clients.

---

### **2. Repository Layer** (`repository/`)
**Purpose**: Handles all database operations using Spring Data JPA.

**Key Components**:
- Interfaces extending `JpaRepository<EntityType, IdType>`
- Custom query methods (e.g., `findByEmail()`, `findByClinicId()`)

**Examples**:
- `UserRepository.java` - User authentication and lookup
- `AppointmentRepository.java` - Query appointments by patient/doctor
- `QueueTicketRepository.java` - Manage queue operations

**Interaction**: Called by the Service layer to perform CRUD operations. Returns entities.

---

### **3. DTO Layer** (`dto/`)
**Purpose**: Data Transfer Objects for API requests/responses. Prevents exposing internal entity structure.

**Key Components**:
- **Request DTOs**: `CreateAppointmentRequest.java`, `LoginRequest.java`, `CreateUserRequest.java`
- **Response DTOs**: `AppointmentDto.java`, `UserDto.java`, `JwtResponse.java`, `QueueStatusDto.java`

**Interaction**: Controllers receive request DTOs from clients and return response DTOs. Mappers convert between DTOs and entities.

---

### **4. Mapper Layer** (`mapper/`)
**Purpose**: Converts between entities and DTOs using MapStruct or manual mapping.

**Key Components**:
- `AppointmentMapper.java` - Maps `Appointment` ↔ `AppointmentDto`
- `UserMapper.java` - Maps `User` ↔ `UserDto`

**Interaction**: Used by the Service layer to transform data between layers.

---

### **5. Service Layer** (`service/` & `service/impl/`)
**Purpose**: Contains all business logic, validations, and orchestration.

**Key Components**:
- **Interfaces**: `AppointmentService.java`, `QueueService.java`, `UserService.java`, `NotificationService.java`
- **Implementations**: `AppointmentServiceImpl.java`, `QueueServiceImpl.java`, etc.

**Responsibilities**:
- Validate business rules (e.g., appointment conflicts, queue logic)
- Coordinate multiple repositories
- Call mappers to convert entities to DTOs
- Trigger notifications

**Interaction**: Called by controllers, uses repositories and mappers, returns DTOs.

---

### **6. Controller Layer** (`controller/`)
**Purpose**: Exposes RESTful API endpoints for client applications.

**Key Components**:
- `AuthController.java` - Registration, login (`/api/auth/**`)
- `AppointmentController.java` - Book, cancel, reschedule appointments (`/api/appointments/**`)
- `QueueController.java` - Check-in, queue status, call next patient (`/api/queue/**`)
- `AdminController.java` - Clinic/doctor management (`/api/admin/**`)

**Annotations**: `@RestController`, `@RequestMapping`, `@PostMapping`, `@GetMapping`

**Interaction**: Receives HTTP requests, calls services, returns DTOs as JSON responses.

---

### **7. Security & Configuration** (`config/`)
**Purpose**: Handles authentication, authorization, and application configuration.

**Key Components**:
- **SecurityConfig.java**: Defines security rules, public/private endpoints, password encoding
- **JwtAuthFilter.java**: Intercepts requests, validates JWT tokens
- **JwtUtils.java**: Generates and validates JWT tokens

**Flow**:
1. Client sends request with JWT in `Authorization: Bearer <token>` header
2. `JwtAuthFilter` validates token and sets authentication context
3. `SecurityConfig` checks if the user has access to the endpoint
4. Request proceeds to controller if authorized

---

### **8. Exception Handling** (`exception/`)
**Purpose**: Centralized error handling for consistent API responses.

**Key Components**:
- `ResourceNotFoundException.java` - Thrown when entities are not found
- Global exception handlers (optional) for formatting error responses

---

## 🔄 Complete Request Flow Example

**Scenario**: Patient books an appointment

1. **Client** sends `POST /api/appointments` with `CreateAppointmentRequest` JSON
2. **JwtAuthFilter** validates the JWT token and authenticates the user
3. **SecurityConfig** checks if the user has permission (role check)
4. **AppointmentController** receives the request DTO
5. **AppointmentService** validates:
   - Patient exists (via `PatientRepository`)
   - Doctor exists and is available (via `DoctorRepository`)
   - No scheduling conflicts (via `AppointmentRepository`)
6. **AppointmentService** creates an `Appointment` entity
7. **AppointmentRepository** saves the entity to the database
8. **AppointmentMapper** converts the saved entity to `AppointmentDto`
9. **NotificationService** sends confirmation email/notification
10. **Controller** returns `AppointmentDto` as JSON response to client

---

## 📋 Developer Guidelines

### **Adding New Features**

1. **Define the Entity** in `entity/` with JPA annotations
2. **Create Repository** interface in `repository/`
3. **Create DTOs** for request/response in `dto/`
4. **Create Mapper** in `mapper/` for entity ↔ DTO conversion
5. **Implement Service** interface and implementation in `service/`
6. **Create Controller** endpoints in `controller/`
7. **Update Security Rules** in `SecurityConfig.java` if needed

### **Best Practices**

- **Never expose entities directly** in controllers - always use DTOs
- **Keep controllers thin** - business logic belongs in services
- **Use constructor injection** for dependencies (recommended over `@Autowired`)
- **Write service layer tests** - test business logic thoroughly
- **Use transactions** - annotate service methods with `@Transactional` when needed
- **Handle exceptions gracefully** - create custom exceptions and global handlers

---

## 🚀 Getting Started

### Prerequisites

- **Frontend**: Node.js (v18+) and npm installed → [Download Node.js](https://nodejs.org/)
- **Backend**: JDK 17+ installed  
  (Maven **not required**, the project uses Maven Wrapper: `mvnw` / `mvnw.cmd`)

---

### ▶️ Running the Frontend

```bash
cd vite-react-frontend
npm install
npm run dev
```

### ▶️ Running the Backend

```bash
cd springboot-backend
# macOS/Linux
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw.cmd spring-boot:run
```
