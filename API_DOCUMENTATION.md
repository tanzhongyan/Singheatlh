# Clinic Appointment & Queue Management System - API Documentation

## Overview
This document provides comprehensive documentation for all REST API endpoints in the Clinic Appointment & Queue Management System.

**Base URL**: `http://localhost:8080/api`

**Authentication**: Most endpoints do not require authentication. Endpoints requiring authentication will be marked with **[Auth Required]**.

---

## Quick Summary

**Total Endpoints**: 122+
**Total Controllers**: 11
**Authentication Required**: Only for user profile endpoints

### Endpoints by Category

| Category | Base Path | Count |
|----------|-----------|-------|
| Authentication | `/api/auth` | 8 |
| Patients | `/api/patients` | 5 |
| Doctors | `/api/doctor` | 7 |
| Clinics | `/api/clinic` | 10 |
| Users | `/api/users` | 11 |
| Schedules | `/api/schedules` | 9 |
| System Monitoring | `/api/system-administrators/monitoring` | 1 |
| **Backup & Restore** | **`/api/system-administrators/backup`** | **5** |
| **TOTAL** | | **56** |

### Backup & Restore (NEW!)

The system now supports **full database backup and restore**:
- **Format**: PostgreSQL compressed dumps (.sql.gz)
- **Execution**: Runs inside Docker container (no local tools needed)
- **Compatibility**: Works on macOS, Windows, and Linux
- **Key Endpoints**:
  - `POST /api/system-administrators/backup/create` - Create backup
  - `GET /api/system-administrators/backup/history` - List backups
  - `GET /api/system-administrators/backup/download/{backupId}` - Download backup
  - `POST /api/system-administrators/backup/restore/{backupId}` - Restore backup
  - `DELETE /api/system-administrators/backup/{backupId}` - Delete backup

---

## Table of Contents
1. [Authentication APIs](#authentication-apis)
2. [Patient Management APIs](#patient-management-apis)
3. [Doctor Management APIs](#doctor-management-apis)
4. [Clinic Management APIs](#clinic-management-apis)
5. [User Management APIs](#user-management-apis)
6. [Schedule Management APIs](#schedule-management-apis)
7. [System Monitoring APIs](#system-monitoring-apis)
8. [System Backup & Restore APIs](#system-backup--restore-apis)
9. [Error Responses](#error-responses)
10. [Data Models](#data-models)

---

## Authentication APIs

Base Path: `/api/auth`

| # | Method | Endpoint | Auth Required | Purpose |
|---|--------|----------|---------------|---------|
| 1 | GET | `/api/auth/health` | No | Check if API is running |
| 2 | POST | `/api/auth/signup` | No | User registration |
| 3 | POST | `/api/auth/login` | No | User authentication |
| 4 | GET | `/api/auth/profile` | Yes | Get authenticated user's profile |
| 5 | PUT | `/api/auth/email` | Yes | Change user email |
| 6 | PUT | `/api/auth/password` | Yes | Change user password |
| 7 | POST | `/api/auth/password/reset` | No | Request password reset |
| 8 | POST | `/api/auth/validate-token` | No | Validate JWT token |

---

## Patient Management APIs

Base Path: `/api/patients`

| # | Method | Endpoint | Purpose |
|---|--------|----------|---------|
| 1 | GET | `/api/patients` | Get all patients |
| 2 | GET | `/api/patients/{patientId}` | Get patient by ID |
| 3 | POST | `/api/patients` | Create patient |
| 4 | PUT | `/api/patients/{patientId}` | Update patient |
| 5 | DELETE | `/api/patients/{patientId}` | Delete patient |

---

## Doctor Management APIs

Base Path: `/api/doctor` or `/api/system-administrators/doctors`

| # | Method | Endpoint | Purpose |
|---|--------|----------|---------|
| 1 | GET | `/api/doctor` | Get all doctors |
| 2 | GET | `/api/doctor/{doctorId}` | Get doctor by ID |
| 3 | GET | `/api/doctor/clinic/{clinicId}` | Get doctors by clinic |
| 4 | GET | `/api/doctor/count` | Get doctor count |
| 5 | POST | `/api/doctor` | Create doctor |
| 6 | PUT | `/api/doctor/{doctorId}` | Update doctor |
| 7 | DELETE | `/api/doctor/{doctorId}` | Delete doctor |

---

## Clinic Management APIs

Base Path: `/api/clinic` or `/api/system-administrators/clinics`

| # | Method | Endpoint | Purpose |
|---|--------|----------|---------|
| 1 | GET | `/api/clinic` | Get all clinics |
| 2 | GET | `/api/clinic/{clinicId}` | Get clinic by ID |
| 3 | GET | `/api/clinic/type/{type}` | Get clinics by type (G or S) |
| 4 | GET | `/api/clinic/name/{name}` | Get clinic by name |
| 5 | GET | `/api/clinic/count` | Get clinic count |
| 6 | POST | `/api/clinic` | Create clinic |
| 7 | PUT | `/api/clinic/{clinicId}` | Update clinic |
| 8 | PUT | `/api/clinic/{clinicId}/hours` | Update clinic hours |
| 9 | DELETE | `/api/clinic/{clinicId}` | Delete clinic |
| 10 | POST | `/api/clinic/import` | Import multiple clinics |

---

## User Management APIs

Base Path: `/api/users` or `/api/system-administrators/users`

| # | Method | Endpoint | Purpose |
|---|--------|----------|---------|
| 1 | GET | `/api/users` | Get all users |
| 2 | GET | `/api/users/{userId}` | Get user by ID |
| 3 | GET | `/api/users/email/{email}` | Get user by email |
| 4 | GET | `/api/users/role/{role}` | Get users by role (P, C, S) |
| 5 | GET | `/api/users/count` | Get user count |
| 6 | GET | `/api/system-administrators/users/paginated` | Get users paginated |
| 7 | POST | `/api/users` | Create user |
| 8 | POST | `/api/system-administrators/users/patient` | Create patient user |
| 9 | POST | `/api/system-administrators/users/staff` | Create clinic staff user |
| 10 | PUT | `/api/users/{userId}` | Update user |
| 11 | DELETE | `/api/users/{userId}` | Delete user |

---

## User Management APIs - Detailed

### 1. Get All Users
Retrieve all users in the system.

**Endpoint**: `GET /api/system-administrators/users`

**Response**: `200 OK`
```json
[
  {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "P",
    "telephoneNumber": "+6591234567",
    "clinicId": null
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/system-administrators/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Get User by ID
Retrieve a specific user by their ID.

**Endpoint**: `GET /api/system-administrators/users/{userId}`

**Path Parameters**:
- `userId` (UUID, required): The user's unique identifier

**Response**: `200 OK`
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "role": "P",
  "telephoneNumber": "+6591234567",
  "clinicId": null
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/system-administrators/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Create User
Create a new user (Patient or Clinic Staff).

**Endpoint**: `POST /api/system-administrators/users`

**Request Body**:
```json
{
  "name": "Jane Smith",
  "email": "jane.smith@example.com",
  "role": "C",
  "telephoneNumber": "+6598765432",
  "clinicId": 1
}
```

**Field Descriptions**:
- `name` (string, required): User's full name
- `email` (string, required): User's email address
- `role` (string, required): User role - 'P' for Patient, 'C' for Clinic Staff
- `telephoneNumber` (string, optional): User's phone number
- `clinicId` (integer, conditional): Required if role is 'C', otherwise null

**Response**: `201 CREATED`
```json
{
  "userId": "223e4567-e89b-12d3-a456-426614174111",
  "name": "Jane Smith",
  "email": "jane.smith@example.com",
  "role": "C",
  "telephoneNumber": "+6598765432",
  "clinicId": 1
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/system-administrators/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "role": "C",
    "telephoneNumber": "+6598765432",
    "clinicId": 1
  }'
```

---

### 4. Update User
Update an existing user's information.

**Endpoint**: `PUT /api/system-administrators/users/{userId}`

**Path Parameters**:
- `userId` (UUID, required): The user's unique identifier

**Request Body**:
```json
{
  "name": "Jane Smith Updated",
  "email": "jane.smith@example.com",
  "role": "C",
  "telephoneNumber": "+6598765432",
  "clinicId": 2
}
```

**Response**: `200 OK`
```json
{
  "userId": "223e4567-e89b-12d3-a456-426614174111",
  "name": "Jane Smith Updated",
  "email": "jane.smith@example.com",
  "role": "C",
  "telephoneNumber": "+6598765432",
  "clinicId": 2
}
```

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/system-administrators/users/223e4567-e89b-12d3-a456-426614174111 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith Updated",
    "email": "jane.smith@example.com",
    "role": "C",
    "telephoneNumber": "+6598765432",
    "clinicId": 2
  }'
```

---

### 5. Delete User
Delete a user from the system.

**Endpoint**: `DELETE /api/system-administrators/users/{userId}`

**Path Parameters**:
- `userId` (UUID, required): The user's unique identifier

**Response**: `200 OK`
```json
{
  "message": "User deleted successfully"
}
```

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/api/system-administrators/users/223e4567-e89b-12d3-a456-426614174111 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Clinic Management APIs

### 1. Get All Clinics
Retrieve all clinics in the system.

**Endpoint**: `GET /api/system-administrators/clinics`

**Response**: `200 OK`
```json
[
  {
    "clinicId": 1,
    "name": "SingHealth Polyclinic - Bedok",
    "type": "G",
    "address": "11 Bedok North Street 1, #01-01",
    "telephoneNumber": "+6565551234",
    "openingHours": "08:00:00",
    "closingHours": "17:00:00"
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/system-administrators/clinics \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Get Clinic by ID
Retrieve a specific clinic by its ID.

**Endpoint**: `GET /api/system-administrators/clinics/{clinicId}`

**Path Parameters**:
- `clinicId` (integer, required): The clinic's unique identifier

**Response**: `200 OK`
```json
{
  "clinicId": 1,
  "name": "SingHealth Polyclinic - Bedok",
  "type": "G",
  "address": "11 Bedok North Street 1, #01-01",
  "telephoneNumber": "+6565551234",
  "openingHours": "08:00:00",
  "closingHours": "17:00:00"
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/system-administrators/clinics/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Create Clinic
Create a new clinic.

**Endpoint**: `POST /api/system-administrators/clinics`

**Request Body**:
```json
{
  "name": "SingHealth Polyclinic - Tampines",
  "type": "S",
  "address": "1 Tampines Street 41, #02-01",
  "telephoneNumber": "+6565559876",
  "openingHours": "09:00:00",
  "closingHours": "18:00:00"
}
```

**Field Descriptions**:
- `name` (string, required): Clinic name
- `type` (string, required): 'G' for General Practice, 'S' for Specialist
- `address` (string, optional): Clinic address
- `telephoneNumber` (string, optional): Clinic phone number
- `openingHours` (time, optional): Opening time in HH:MM:SS format
- `closingHours` (time, optional): Closing time in HH:MM:SS format

**Response**: `201 CREATED`
```json
{
  "clinicId": 6,
  "name": "SingHealth Polyclinic - Tampines",
  "type": "S",
  "address": "1 Tampines Street 41, #02-01",
  "telephoneNumber": "+6565559876",
  "openingHours": "09:00:00",
  "closingHours": "18:00:00"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/system-administrators/clinics \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "SingHealth Polyclinic - Tampines",
    "type": "S",
    "address": "1 Tampines Street 41, #02-01",
    "telephoneNumber": "+6565559876",
    "openingHours": "09:00:00",
    "closingHours": "18:00:00"
  }'
```

---

### 4. Update Clinic
Update an existing clinic.

**Endpoint**: `PUT /api/system-administrators/clinics/{clinicId}`

**Path Parameters**:
- `clinicId` (integer, required): The clinic's unique identifier

**Request Body**:
```json
{
  "name": "SingHealth Polyclinic - Tampines (Updated)",
  "type": "G",
  "address": "1 Tampines Street 41, #02-01",
  "telephoneNumber": "+6565559876",
  "openingHours": "08:00:00",
  "closingHours": "20:00:00"
}
```

**Response**: `200 OK`
```json
{
  "clinicId": 6,
  "name": "SingHealth Polyclinic - Tampines (Updated)",
  "type": "G",
  "address": "1 Tampines Street 41, #02-01",
  "telephoneNumber": "+6565559876",
  "openingHours": "08:00:00",
  "closingHours": "20:00:00"
}
```

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/system-administrators/clinics/6 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "SingHealth Polyclinic - Tampines (Updated)",
    "type": "G",
    "address": "1 Tampines Street 41, #02-01",
    "telephoneNumber": "+6565559876",
    "openingHours": "08:00:00",
    "closingHours": "20:00:00"
  }'
```

---

### 5. Delete Clinic
Delete a clinic from the system.

**Endpoint**: `DELETE /api/system-administrators/clinics/{clinicId}`

**Path Parameters**:
- `clinicId` (integer, required): The clinic's unique identifier

**Response**: `200 OK`
```json
{
  "message": "Clinic deleted successfully"
}
```

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/api/system-administrators/clinics/6 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Doctor Management APIs

### 1. Get All Doctors
Retrieve all doctors in the system.

**Endpoint**: `GET /api/system-administrators/doctors`

**Response**: `200 OK`
```json
[
  {
    "doctorId": "D000000001",
    "name": "Dr. John Smith",
    "clinicId": 1,
    "clinicName": "SingHealth Polyclinic - Bedok",
    "appointmentDurationInMinutes": 15
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/system-administrators/doctors \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Get Doctor by ID
Retrieve a specific doctor by their ID.

**Endpoint**: `GET /api/system-administrators/doctors/{doctorId}`

**Path Parameters**:
- `doctorId` (string, required): The doctor's unique identifier (CHAR(10))

**Response**: `200 OK`
```json
{
  "doctorId": "D000000001",
  "name": "Dr. John Smith",
  "clinicId": 1,
  "clinicName": "SingHealth Polyclinic - Bedok",
  "appointmentDurationInMinutes": 15
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/system-administrators/doctors/D000000001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Create Doctor
Create a new doctor.

**Endpoint**: `POST /api/system-administrators/doctors`

**Request Body**:
```json
{
  "name": "Dr. Emily Chen",
  "clinicId": 2,
  "appointmentDurationInMinutes": 20
}
```

**Field Descriptions**:
- `name` (string, required): Doctor's full name
- `clinicId` (integer, required): ID of the clinic where the doctor practices
- `appointmentDurationInMinutes` (integer, required): Default duration for appointments in minutes

**Response**: `201 CREATED`
```json
{
  "doctorId": "D000000010",
  "name": "Dr. Emily Chen",
  "clinicId": 2,
  "clinicName": "SingHealth Polyclinic - Tampines",
  "appointmentDurationInMinutes": 20
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/system-administrators/doctors \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Emily Chen",
    "clinicId": 2,
    "appointmentDurationInMinutes": 20
  }'
```

---

### 4. Update Doctor
Update an existing doctor's information.

**Endpoint**: `PUT /api/system-administrators/doctors/{doctorId}`

**Path Parameters**:
- `doctorId` (string, required): The doctor's unique identifier

**Request Body**:
```json
{
  "name": "Dr. Emily Chen (Senior)",
  "clinicId": 3,
  "appointmentDurationInMinutes": 30
}
```

**Response**: `200 OK`
```json
{
  "doctorId": "D000000010",
  "name": "Dr. Emily Chen (Senior)",
  "clinicId": 3,
  "clinicName": "SingHealth Specialist Centre",
  "appointmentDurationInMinutes": 30
}
```

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/system-administrators/doctors/D000000010 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Emily Chen (Senior)",
    "clinicId": 3,
    "appointmentDurationInMinutes": 30
  }'
```

---

### 5. Delete Doctor
Delete a doctor from the system.

**Endpoint**: `DELETE /api/system-administrators/doctors/{doctorId}`

**Path Parameters**:
- `doctorId` (string, required): The doctor's unique identifier

**Response**: `200 OK`
```json
{
  "message": "Doctor deleted successfully"
}
```

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/api/system-administrators/doctors/D000000010 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Schedule Management APIs

Base Path: `/api/schedules`

| # | Method | Endpoint | Purpose |
|---|--------|----------|---------|
| 1 | GET | `/api/schedules` | Get all schedules |
| 2 | GET | `/api/schedules/{scheduleId}` | Get schedule by ID |
| 3 | GET | `/api/schedules/doctor/{doctorId}` | Get schedules by doctor |
| 4 | POST | `/api/schedules` | Create schedule block |
| 5 | PUT | `/api/schedules/{scheduleId}` | Update schedule block |
| 6 | DELETE | `/api/schedules/{scheduleId}` | Delete schedule block |
| 7 | GET | `/api/schedules/doctor/{doctorId}/check-overlap` | Check overlapping schedules |
| 8 | GET | `/api/schedules/doctor/{doctorId}/available` | Get available schedules |
| 9 | GET | `/api/schedules/doctor/{doctorId}/date-range` | Get schedules by date range |

---

### Detailed Endpoints

#### 1. Get All Schedules
Retrieve all schedule blocks in the system.

**Endpoint**: `GET /api/schedules`

**Response**: `200 OK`
```json
[
  {
    "scheduleId": "S000000001",
    "doctorId": "D000000001",
    "doctorName": "Dr. John Smith",
    "startDatetime": "2025-11-10T09:00:00",
    "endDatetime": "2025-11-10T12:00:00",
    "type": "AVAILABLE"
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/schedules \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Get Schedule by ID
Retrieve a specific schedule block by its ID.

**Endpoint**: `GET /api/schedules/{scheduleId}`

**Path Parameters**:
- `scheduleId` (string, required): The schedule's unique identifier (CHAR(10))

**Response**: `200 OK`
```json
{
  "scheduleId": "S000000001",
  "doctorId": "D000000001",
  "doctorName": "Dr. John Smith",
  "startDatetime": "2025-11-10T09:00:00",
  "endDatetime": "2025-11-10T12:00:00",
  "type": "AVAILABLE"
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/schedules/S000000001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Get Schedules by Doctor
Retrieve all schedule blocks for a specific doctor.

**Endpoint**: `GET /api/schedules/doctor/{doctorId}`

**Path Parameters**:
- `doctorId` (string, required): The doctor's unique identifier

**Response**: `200 OK`
```json
[
  {
    "scheduleId": "S000000001",
    "doctorId": "D000000001",
    "doctorName": "Dr. John Smith",
    "startDatetime": "2025-11-10T09:00:00",
    "endDatetime": "2025-11-10T12:00:00",
    "type": "AVAILABLE"
  },
  {
    "scheduleId": "S000000002",
    "doctorId": "D000000001",
    "doctorName": "Dr. John Smith",
    "startDatetime": "2025-11-10T14:00:00",
    "endDatetime": "2025-11-10T17:00:00",
    "type": "AVAILABLE"
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/schedules/doctor/D000000001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 4. Create Schedule Block
Create a new schedule block for a doctor.

**Endpoint**: `POST /api/schedules`

**Request Body**:
```json
{
  "doctorId": "D000000001",
  "startDatetime": "2025-11-15T09:00:00",
  "endDatetime": "2025-11-15T12:00:00",
  "type": "AVAILABLE"
}
```

**Field Descriptions**:
- `doctorId` (string, required): Doctor's unique identifier
- `startDatetime` (datetime, required): Start date and time in ISO 8601 format
- `endDatetime` (datetime, required): End date and time in ISO 8601 format
- `type` (string, required): Schedule type - "AVAILABLE" or "UNAVAILABLE"

**Response**: `201 CREATED`
```json
{
  "scheduleId": "S000000050",
  "doctorId": "D000000001",
  "doctorName": "Dr. John Smith",
  "startDatetime": "2025-11-15T09:00:00",
  "endDatetime": "2025-11-15T12:00:00",
  "type": "AVAILABLE"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/schedules \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": "D000000001",
    "startDatetime": "2025-11-15T09:00:00",
    "endDatetime": "2025-11-15T12:00:00",
    "type": "AVAILABLE"
  }'
```

---

### 5. Update Schedule Block
Update an existing schedule block.

**Endpoint**: `PUT /api/schedules/{scheduleId}`

**Path Parameters**:
- `scheduleId` (string, required): The schedule's unique identifier

**Request Body**:
```json
{
  "doctorId": "D000000001",
  "startDatetime": "2025-11-15T10:00:00",
  "endDatetime": "2025-11-15T13:00:00",
  "type": "UNAVAILABLE"
}
```

**Response**: `200 OK`
```json
{
  "scheduleId": "S000000050",
  "doctorId": "D000000001",
  "doctorName": "Dr. John Smith",
  "startDatetime": "2025-11-15T10:00:00",
  "endDatetime": "2025-11-15T13:00:00",
  "type": "UNAVAILABLE"
}
```

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/schedules/S000000050 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": "D000000001",
    "startDatetime": "2025-11-15T10:00:00",
    "endDatetime": "2025-11-15T13:00:00",
    "type": "UNAVAILABLE"
  }'
```

---

### 6. Delete Schedule Block
Delete a schedule block from the system.

**Endpoint**: `DELETE /api/schedules/{scheduleId}`

**Path Parameters**:
- `scheduleId` (string, required): The schedule's unique identifier

**Response**: `200 OK`
```json
{
  "message": "Schedule deleted successfully!"
}
```

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/api/schedules/S000000050 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 7. Check for Overlapping Schedules
Check if a new schedule block would overlap with existing schedules.

**Endpoint**: `GET /api/schedules/doctor/{doctorId}/check-overlap`

**Path Parameters**:
- `doctorId` (string, required): The doctor's unique identifier

**Query Parameters**:
- `startTime` (datetime, required): Proposed start time (ISO 8601 format)
- `endTime` (datetime, required): Proposed end time (ISO 8601 format)

**Response**: `200 OK`
```json
true
```

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/schedules/doctor/D000000001/check-overlap?startTime=2025-11-15T09:00:00&endTime=2025-11-15T12:00:00" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 8. Get Available Schedules by Doctor
Retrieve only AVAILABLE schedule blocks for a specific doctor.

**Endpoint**: `GET /api/schedules/doctor/{doctorId}/available`

**Path Parameters**:
- `doctorId` (string, required): The doctor's unique identifier

**Response**: `200 OK`
```json
[
  {
    "scheduleId": "S000000001",
    "doctorId": "D000000001",
    "doctorName": "Dr. John Smith",
    "startDatetime": "2025-11-10T09:00:00",
    "endDatetime": "2025-11-10T12:00:00",
    "type": "AVAILABLE"
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/schedules/doctor/D000000001/available \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 9. Get Schedules by Date Range
Retrieve schedule blocks within a specific date range for a doctor.

**Endpoint**: `GET /api/schedules/doctor/{doctorId}/date-range`

**Path Parameters**:
- `doctorId` (string, required): The doctor's unique identifier

**Query Parameters**:
- `startDate` (datetime, required): Range start (ISO 8601 format)
- `endDate` (datetime, required): Range end (ISO 8601 format)

**Response**: `200 OK`
```json
[
  {
    "scheduleId": "S000000001",
    "doctorId": "D000000001",
    "doctorName": "Dr. John Smith",
    "startDatetime": "2025-11-10T09:00:00",
    "endDatetime": "2025-11-10T12:00:00",
    "type": "AVAILABLE"
  }
]
```

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/schedules/doctor/D000000001/date-range?startDate=2025-11-01T00:00:00&endDate=2025-11-30T23:59:59" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## System Monitoring APIs

Base Path: `/api/system-administrators/monitoring`

| # | Method | Endpoint | Purpose |
|---|--------|----------|---------|
| 1 | GET | `/api/system-administrators/monitoring/statistics` | Get system statistics |

---

### Detailed Endpoints

#### 1. Get System Statistics

Retrieve real-time system statistics including user counts, appointment data, system uptime, and active users.

**Endpoint**: `GET /api/system-administrators/monitoring/statistics`

**Response**: `200 OK`
```json
{
  "totalUsers": 150,
  "totalDoctors": 25,
  "totalClinics": 8,
  "totalAppointments": 1250,
  "completedAppointments": 890,
  "pendingAppointments": 300,
  "cancelledAppointments": 60,
  "totalPatients": 100,
  "totalClinicStaff": 35,
  "totalAdministrators": 5,
  "lastBackupTime": "2025-11-06T15:30:00",
  "systemUptime": "45 days 12 hours",
  "activeUsers": 12
}
```

**Field Descriptions**:
- `totalUsers` (integer): Total number of users across all roles
- `totalDoctors` (integer): Total number of registered doctors
- `totalClinics` (integer): Total number of clinics
- `totalAppointments` (integer): Total appointments in system
- `completedAppointments` (integer): Appointments with status "Completed"
- `pendingAppointments` (integer): Upcoming/scheduled appointments
- `cancelledAppointments` (integer): Cancelled appointments
- `totalPatients` (integer): Users with role "P" (Patient)
- `totalClinicStaff` (integer): Users with role "C" (Clinic Staff)
- `totalAdministrators` (integer): Users with role "S" (System Admin)
- `lastBackupTime` (datetime): Timestamp of last system backup
- `systemUptime` (string): Human-readable system uptime duration
- `activeUsers` (integer): Currently logged-in users

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/system-administrators/monitoring/statistics \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## System Backup & Restore APIs

Base Path: `/api/system-administrators/backup`

| # | Method | Endpoint | Purpose |
|---|--------|----------|---------|
| 1 | POST | `/api/system-administrators/backup/create` | Create backup |
| 2 | GET | `/api/system-administrators/backup/history` | Get backup history |
| 3 | GET | `/api/system-administrators/backup/download/{backupId}` | Download backup |
| 4 | POST | `/api/system-administrators/backup/restore/{backupId}` | Restore backup |
| 5 | DELETE | `/api/system-administrators/backup/{backupId}` | Delete backup |

---

### Detailed Endpoints

#### 1. Create Backup

Create a complete backup of all system data (users, doctors, clinics, appointments, schedules, clinic staff).

**Endpoint**: `POST /api/system-administrators/backup/create`

**Request Body**: *(None required)*

**Response**: `201 CREATED`
```json
{
  "backupId": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f789",
  "createdAt": "2025-11-06T15:30:00",
  "sizeInBytes": 2621440,
  "status": "COMPLETED",
  "description": "System backup completed successfully",
  "recordCount": 1500
}
```

**Field Descriptions**:
- `backupId` (string): Unique identifier for this backup (UUID)
- `createdAt` (datetime): Timestamp when backup was created
- `sizeInBytes` (long): Size of compressed backup file in bytes
- `status` (string): Backup status - "COMPLETED", "FAILED", or "IN_PROGRESS"
- `description` (string): Human-readable status message
- `recordCount` (integer): Total number of database records backed up

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/system-administrators/backup/create \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Get Backup History
Retrieve list of all available backups with metadata.

**Endpoint**: `GET /api/system-administrators/backup/history`

**Response**: `200 OK`
```json
[
  {
    "backupId": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f789",
    "createdAt": "2025-11-06T15:30:00",
    "sizeInBytes": 2621440,
    "status": "COMPLETED",
    "description": "Backup file",
    "recordCount": 1500
  },
  {
    "backupId": "b2c3d4e5-f6a7-4890-b123-c4d5e6f7a890",
    "createdAt": "2025-11-05T14:15:00",
    "sizeInBytes": 2555555,
    "status": "COMPLETED",
    "description": "Backup file",
    "recordCount": 1485
  }
]
```

**Response Notes**:
- Array is sorted by creation time (newest first)
- Each backup is a complete snapshot of the system

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/system-administrators/backup/history \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Download Backup
Download a backup file as a ZIP archive.

**Endpoint**: `GET /api/system-administrators/backup/download/{backupId}`

**Path Parameters**:
- `backupId` (string, required): UUID of the backup to download

**Response**: `200 OK` - ZIP file (blob)

**Response Headers**:
```
Content-Type: application/zip
Content-Disposition: attachment; filename="backupId.zip"
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/system-administrators/backup/download/a1b2c3d4-e5f6-4789-a012-b3c4d5e6f789 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o backup.zip
```

---

### 4. Restore Backup
Restore system data from a previous backup.

**Endpoint**: `POST /api/system-administrators/backup/restore/{backupId}`

**Path Parameters**:
- `backupId` (string, required): UUID of the backup to restore from

**Request Body**: *(None required)*

**Response**: `200 OK`
```json
{
  "message": "Backup restored successfully!"
}
```

**⚠️ WARNING**: This operation will replace all current data with data from the backup.

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/system-administrators/backup/restore/a1b2c3d4-e5f6-4789-a012-b3c4d5e6f789 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 5. Delete Backup
Remove a backup file from storage.

**Endpoint**: `DELETE /api/system-administrators/backup/{backupId}`

**Path Parameters**:
- `backupId` (string, required): UUID of the backup to delete

**Response**: `200 OK`
```json
{
  "message": "Backup deleted successfully!"
}
```

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/api/system-administrators/backup/a1b2c3d4-e5f6-4789-a012-b3c4d5e6f789 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
Invalid input data or validation error.
```json
{
  "timestamp": "2025-11-06T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: name cannot be blank"
}
```

### 401 Unauthorized
Missing or invalid authentication token.
```json
{
  "timestamp": "2025-11-06T10:15:30",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 403 Forbidden
User does not have permission to access this resource.
```json
{
  "timestamp": "2025-11-06T10:15:30",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied"
}
```

### 404 Not Found
Requested resource does not exist.
```json
{
  "timestamp": "2025-11-06T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Doctor not found with id: D000000999"
}
```

### 409 Conflict
Request conflicts with existing data (e.g., overlapping schedules).
```json
{
  "timestamp": "2025-11-06T10:15:30",
  "status": 409,
  "error": "Conflict",
  "message": "Schedule overlaps with existing schedule"
}
```

### 500 Internal Server Error
Server-side error.
```json
{
  "timestamp": "2025-11-06T10:15:30",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Data Models

### User
```json
{
  "userId": "UUID",
  "name": "string",
  "email": "string",
  "role": "string (P|C|S)",
  "telephoneNumber": "string",
  "clinicId": "integer (nullable)"
}
```

### Clinic
```json
{
  "clinicId": "integer",
  "name": "string",
  "type": "string (G|S)",
  "address": "string",
  "telephoneNumber": "string",
  "openingHours": "time (HH:MM:SS)",
  "closingHours": "time (HH:MM:SS)"
}
```

### Doctor
```json
{
  "doctorId": "string (CHAR(10))",
  "name": "string",
  "clinicId": "integer",
  "clinicName": "string (read-only)",
  "appointmentDurationInMinutes": "integer"
}
```

### Schedule
```json
{
  "scheduleId": "string (CHAR(10))",
  "doctorId": "string (CHAR(10))",
  "doctorName": "string (read-only)",
  "startDatetime": "datetime (ISO 8601)",
  "endDatetime": "datetime (ISO 8601)",
  "type": "string (AVAILABLE|UNAVAILABLE)"
}
```

---

## Testing Notes

1. **Authentication**: All requests require a valid JWT token. Obtain a token by logging in through the authentication endpoint.

2. **Database Seeding**: Use the provided seed scripts to populate the database with sample data for testing.

3. **Date Formats**: All datetime fields use ISO 8601 format (`YYYY-MM-DDTHH:MM:SS`).

4. **Schedule Validation**: The system automatically validates that:
   - End time is after start time
   - No overlapping schedules exist for the same doctor
   - Doctor exists before creating schedules

5. **Cascade Deletes**: Be aware that:
   - Deleting a doctor will delete all their schedules and appointments
   - Deleting a clinic will delete all associated doctors (and their cascading data)
   - Deleting a user will delete all their appointments

---

## Postman Collection

A Postman collection with all these endpoints pre-configured is available in the repository at:
`/docs/postman/System_Administrator_APIs.postman_collection.json`

Import this collection into Postman for quick testing of all endpoints.

---

*Last Updated: November 6, 2025*
*Version: 1.0.0*
