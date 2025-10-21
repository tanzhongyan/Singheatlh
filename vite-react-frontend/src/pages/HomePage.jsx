import { useAuth } from "../contexts/AuthContext";
import { Link } from "react-router-dom";

const HomePage = () => {
  const { user } = useAuth();

  return (
    <div className="min-vh-100 bg-white">
      <div className="container py-5">
        <div className="row justify-content-center">
          <div className="col-lg-10">
            {/* Hero Section */}
            <div className="text-center mb-5 py-5">
              <h1 className="display-3 fw-bold text-dark mb-3">
                Welcome to SingHealth
              </h1>
              <p className="lead text-muted mb-4">
                {user
                  ? `Hello! Manage your healthcare appointments with ease.`
                  : "Your health, our priority. Book appointments and manage your medical care online."}
              </p>
            </div>

            {!user && (
              <div className="d-flex gap-3 justify-content-center mb-5">
                <Link to="/signup" className="btn btn-primary btn-lg px-5 py-3">
                  Get Started
                </Link>
                <Link to="/login" className="btn btn-outline-primary btn-lg px-5 py-3">
                  Login
                </Link>
              </div>
            )}

            {user && (
              <div className="row g-4 mt-4">
                {/* Quick Actions Card */}
                <div className="col-md-6">
                  <div className="card h-100 border shadow-sm">
                    <div className="card-body p-4">
                      <div className="d-flex align-items-center mb-3">
                        <div className="rounded-circle bg-primary bg-opacity-10 p-3 me-3">
                          <i className="bi bi-calendar-check text-primary" style={{ fontSize: '1.5rem' }}></i>
                        </div>
                        <h5 className="card-title mb-0 fw-bold">My Appointments</h5>
                      </div>
                      <p className="card-text text-muted mb-4">
                        View and manage all your upcoming and past medical appointments.
                      </p>
                      <Link to="/appointments" className="btn btn-primary w-100">
                        View Appointments
                      </Link>
                    </div>
                  </div>
                </div>

                {/* Book Appointment Card */}
                <div className="col-md-6">
                  <div className="card h-100 border shadow-sm">
                    <div className="card-body p-4">
                      <div className="d-flex align-items-center mb-3">
                        <div className="rounded-circle bg-success bg-opacity-10 p-3 me-3">
                          <i className="bi bi-plus-circle text-success" style={{ fontSize: '1.5rem' }}></i>
                        </div>
                        <h5 className="card-title mb-0 fw-bold">Book New Appointment</h5>
                      </div>
                      <p className="card-text text-muted mb-4">
                        Schedule a new appointment with your preferred doctor and clinic.
                      </p>
                      <Link to="/appointments" className="btn btn-success w-100">
                        Book Appointment
                      </Link>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Features Section */}
            {!user && (
              <div className="row g-4 mt-5">
                <div className="col-md-4">
                  <div className="text-center">
                    <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex p-4 mb-3">
                      <i className="bi bi-calendar-check text-primary" style={{ fontSize: '2rem' }}></i>
                    </div>
                    <h5 className="fw-bold mb-2">Easy Booking</h5>
                    <p className="text-muted">Book appointments with your preferred doctors in just a few clicks.</p>
                  </div>
                </div>
                <div className="col-md-4">
                  <div className="text-center">
                    <div className="rounded-circle bg-success bg-opacity-10 d-inline-flex p-4 mb-3">
                      <i className="bi bi-hospital text-success" style={{ fontSize: '2rem' }}></i>
                    </div>
                    <h5 className="fw-bold mb-2">Multiple Clinics</h5>
                    <p className="text-muted">Choose from a wide network of clinics and specialists.</p>
                  </div>
                </div>
                <div className="col-md-4">
                  <div className="text-center">
                    <div className="rounded-circle bg-info bg-opacity-10 d-inline-flex p-4 mb-3">
                      <i className="bi bi-clock-history text-info" style={{ fontSize: '2rem' }}></i>
                    </div>
                    <h5 className="fw-bold mb-2">Manage Schedule</h5>
                    <p className="text-muted">Track your medical appointments and healthcare history.</p>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
