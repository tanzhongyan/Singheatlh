// src/App.jsx
import { Routes, Route, Navigate } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import HomePage from "./pages/patient/HomePage";
import LoginPage from "./pages/LoginPage";
import SignUpPage from "./pages/SignUpPage";
import AppointmentsPage from "./pages/patient/AppointmentsPage";
import AdminLayout from "./components/admin/AdminLayout";
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";
import UserManagementPage from "./pages/admin/UserManagementPage";
import DoctorManagementPage from "./pages/admin/DoctorManagementPage";
import ClinicManagementPage from "./pages/admin/ClinicManagementPage";
import StaffDashboardPage from "./pages/clinicStaff/StaffDashboardPage";
import ProtectedRoute from "./components/ProtectedRoute";
import Navbar from "./components/Navbar";
import "./App.css";

function App() {
  return (
    <>
      <Navbar />
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />

        {/* Patient Routes - Only accessible by role 'P' */}
        <Route element={<ProtectedRoute allowedRoles={["P"]} />}>
          <Route path="/home" element={<HomePage />} />
          <Route path="/appointments" element={<AppointmentsPage />} />
        </Route>

        {/* Admin Routes - Only accessible by role 'S' */}
        <Route element={<ProtectedRoute allowedRoles={["S"]} />}>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<AdminDashboardPage />} />
            <Route path="users" element={<UserManagementPage />} />
            <Route path="doctors" element={<DoctorManagementPage />} />
            <Route path="clinics" element={<ClinicManagementPage />} />
          </Route>
        </Route>

        {/* Clinic Staff Routes - Only accessible by role 'C' */}
        <Route element={<ProtectedRoute allowedRoles={["C"]} />}>
          <Route path="/staff" element={<StaffDashboardPage />} />
        </Route>

        {/* Catch all - redirect to landing page */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}

export default App;
