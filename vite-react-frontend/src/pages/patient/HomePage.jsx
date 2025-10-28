import { useAuth } from "../../contexts/AuthContext";
import { Link } from "react-router-dom";
import TodayCheckIn from '../../components/patient/TodayCheckIn';

const HomePage = () => {
  const { userProfile } = useAuth();

  return (
    <div className="min-vh-100 bg-light">
      <div className="container py-5">
        <div className="row justify-content-center">
          <div className="col-lg-10">
            {/* Hero Section */}
            <div className="text-center mb-5 py-4">
              <h1 className="display-4 fw-bold text-dark mb-3">
                Welcome back, {userProfile?.name || "Patient"}!
              </h1>
              <p className="lead text-muted mb-4">
                Manage your healthcare appointments with ease.
              </p>
            </div>

            {/* Today Appointment / Check-in */}
            <div className="row g-4 mt-4">
              <div className="col-12">
                <TodayCheckIn />
              </div>
            </div>

            <div className="row g-4 mt-4">
              {/* Quick Actions Card */}
              <div className="col-md-6">
                <div className="card h-100 border-0 shadow-sm hover-card">
                  <div className="card-body p-4">
                    <div className="d-flex align-items-center mb-3">
                      <div className="rounded-circle bg-primary bg-opacity-10 p-3 me-3">
                        <i
                          className="bi bi-calendar-check text-primary"
                          style={{ fontSize: "1.5rem" }}
                        ></i>
                      </div>
                      <h5 className="card-title mb-0 fw-bold">
                        My Appointments
                      </h5>
                    </div>
                    <p className="card-text text-muted mb-4">
                      View and manage all your upcoming and past medical
                      appointments.
                    </p>
                    <Link to="/appointments" className="btn btn-primary w-100">
                      View Appointments
                    </Link>
                  </div>
                </div>
              </div>

              {/* Book Appointment Card */}
              <div className="col-md-6">
                <div className="card h-100 border-0 shadow-sm hover-card">
                  <div className="card-body p-4">
                    <div className="d-flex align-items-center mb-3">
                      <div className="rounded-circle bg-success bg-opacity-10 p-3 me-3">
                        <i
                          className="bi bi-plus-circle text-success"
                          style={{ fontSize: "1.5rem" }}
                        ></i>
                      </div>
                      <h5 className="card-title mb-0 fw-bold">
                        Book New Appointment
                      </h5>
                    </div>
                    <p className="card-text text-muted mb-4">
                      Schedule a new appointment with your preferred doctor and
                      clinic.
                    </p>
                    <Link to="/appointments" className="btn btn-success w-100">
                      Book Appointment
                    </Link>
                  </div>
                </div>
              </div>
            </div>

            {/* Quick Info Section */}
            <div className="row g-4 mt-4">
              <div className="col-12">
                <div className="card border-0 shadow-sm">
                  <div className="card-body p-4">
                    <h5 className="fw-bold mb-4">
                      <i className="bi bi-info-circle me-2 text-primary"></i>
                      Quick Information
                    </h5>
                    <div className="row">
                      <div className="col-md-4 mb-3">
                        <div className="d-flex align-items-center">
                          <i
                            className="bi bi-telephone text-success me-3"
                            style={{ fontSize: "1.5rem" }}
                          ></i>
                          <div>
                            <p className="mb-0 small text-muted">
                              Emergency Hotline
                            </p>
                            <p className="mb-0 fw-semibold">+65 6321 4000</p>
                          </div>
                        </div>
                      </div>
                      <div className="col-md-4 mb-3">
                        <div className="d-flex align-items-center">
                          <i
                            className="bi bi-clock text-info me-3"
                            style={{ fontSize: "1.5rem" }}
                          ></i>
                          <div>
                            <p className="mb-0 small text-muted">
                              Operating Hours
                            </p>
                            <p className="mb-0 fw-semibold">24/7 Available</p>
                          </div>
                        </div>
                      </div>
                      <div className="col-md-4 mb-3">
                        <div className="d-flex align-items-center">
                          <i
                            className="bi bi-envelope text-warning me-3"
                            style={{ fontSize: "1.5rem" }}
                          ></i>
                          <div>
                            <p className="mb-0 small text-muted">
                              Email Support
                            </p>
                            <p className="mb-0 fw-semibold">
                              support@singhealth.sg
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
