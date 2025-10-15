import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

// Placeholder for now. In a real app, you'd check for admin role.
const useAuth = () => {
    // For now, let's assume the user is an admin.
    // In a real app, you would get this from your AuthContext.
    const user = { role: 'SYSTEM_ADMINISTRATOR' }; 
    return user && user.role === 'SYSTEM_ADMINISTRATOR';
};

const ProtectedRoute = () => {
    const isAuth = useAuth();
    return isAuth ? <Outlet /> : <Navigate to="/login" />;
};

export default ProtectedRoute;
