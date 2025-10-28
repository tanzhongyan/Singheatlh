import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const ProtectedRoute = ({ allowedRoles }) => {
  const { user, userProfile, loading } = useAuth();

  if (loading) {
    return (
      <div
        className="d-flex justify-content-center align-items-center"
        style={{ minHeight: "100vh" }}
      >
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  // If user is not logged in (no Supabase Auth user), redirect to landing page
  if (!user) {
    return <Navigate to="/" replace />;
  }

  // If user profile hasn't loaded yet from user_profile table, show loading
  if (!userProfile) {
    return (
      <div
        className="d-flex justify-content-center align-items-center"
        style={{ minHeight: "100vh" }}
      >
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading profile...</span>
        </div>
      </div>
    );
  }

  // If allowedRoles is specified, check if user has the required role from user_profile table
  if (allowedRoles && allowedRoles.length > 0) {
    if (!allowedRoles.includes(userProfile.role)) {
      // Redirect based on user role from user_profile table
      if (userProfile.role === "S") {
        // System Admin trying to access patient route -> redirect to admin
        return <Navigate to="/admin" replace />;
      } else if (userProfile.role === "P") {
        // Patient trying to access admin route -> redirect to home
        return <Navigate to="/home" replace />;
      } else if (userProfile.role === "C") {
        // Clinic Staff trying to access other route -> redirect to staff dashboard
        return <Navigate to="/staff" replace />;
      }
    }
  }

  return <Outlet />;
};

export default ProtectedRoute;
