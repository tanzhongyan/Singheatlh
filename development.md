# Development Guide

This document outlines reusable components and API endpoints that can be used by other developers.

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
