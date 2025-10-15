// src/App.jsx
import { Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignUpPage from './pages/SignUpPage';
import AdminLayout from './components/admin/AdminLayout';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import UserManagementPage from './pages/admin/UserManagementPage';
import DoctorManagementPage from './pages/admin/DoctorManagementPage';
import ClinicManagementPage from './pages/admin/ClinicManagementPage';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar'; // Assuming you have a general navbar
import './App.css';

function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />

        {/* Admin Routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<AdminDashboardPage />} />
            <Route path="users" element={<UserManagementPage />} />
            <Route path="doctors" element={<DoctorManagementPage />} />
            <Route path="clinics" element={<ClinicManagementPage />} />
          </Route>
        </Route>
      </Routes>
    </>
  );
}

export default App;
