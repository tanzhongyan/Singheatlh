import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { useEffect } from "react";

const LandingPage = () => {
  const { user, userProfile, loading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // Redirect authenticated users to their respective dashboards
    if (user && userProfile) {
      if (userProfile.role === "S") {
        // System Admin -> redirect to admin dashboard
        navigate("/admin", { replace: true });
      } else if (userProfile.role === "P") {
        // Patient -> redirect to patient home
        navigate("/home", { replace: true });
      } else if (userProfile.role === "C") {
        // Clinic Staff -> redirect to staff dashboard
        navigate("/staff", { replace: true });
      }
    }
  }, [user, userProfile, navigate]);

  // Show loading spinner while checking authentication
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

  // Only show landing page if user is not authenticated
  return (
    <div className="min-vh-100 bg-light">
      {/* Hero Section */}
      <section
        className="bg-primary text-white py-5"
        style={{ minHeight: "70vh" }}
      >
        <div className="container">
          <div className="row align-items-center min-vh-70">
            <div className="col-lg-6 py-5">
              <h1 className="display-3 fw-bold mb-4">
                <i className="bi bi-heart-pulse-fill me-3"></i>
                SingHealth
              </h1>
              <p className="lead mb-4" style={{ fontSize: "1.5rem" }}>
                Your Health, Our Priority
              </p>
              <p className="fs-5 mb-5 opacity-90">
                Book appointments, manage your healthcare records, and access
                quality medical services from Singapore's trusted healthcare
                provider. Available 24/7 at your convenience.
              </p>
              <div className="d-flex gap-3">
                <Link
                  to="/signup"
                  className="btn btn-light btn-lg px-5 py-3 shadow"
                >
                  <i className="bi bi-person-plus me-2"></i>
                  Get Started
                </Link>
                <Link
                  to="/login"
                  className="btn btn-outline-light btn-lg px-5 py-3"
                >
                  <i className="bi bi-box-arrow-in-right me-2"></i>
                  Login
                </Link>
              </div>
            </div>
            <div className="col-lg-6 d-none d-lg-block text-center">
              <i
                className="bi bi-hospital"
                style={{ fontSize: "20rem", opacity: 0.2 }}
              ></i>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-5 bg-white">
        <div className="container py-5">
          <div className="text-center mb-5">
            <h2 className="display-5 fw-bold mb-3">Why Choose SingHealth?</h2>
            <p className="lead text-muted">
              Experience healthcare the modern way
            </p>
          </div>

          <div className="row g-4">
            <div className="col-md-4">
              <div className="card h-100 border-0 shadow-sm hover-shadow transition">
                <div className="card-body text-center p-4">
                  <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex p-4 mb-4">
                    <i
                      className="bi bi-calendar-check text-primary"
                      style={{ fontSize: "3rem" }}
                    ></i>
                  </div>
                  <h4 className="fw-bold mb-3">Easy Booking</h4>
                  <p className="text-muted">
                    Book appointments with your preferred doctors in just a few
                    clicks. Choose from available time slots that suit your
                    schedule.
                  </p>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="card h-100 border-0 shadow-sm hover-shadow transition">
                <div className="card-body text-center p-4">
                  <div className="rounded-circle bg-success bg-opacity-10 d-inline-flex p-4 mb-4">
                    <i
                      className="bi bi-hospital text-success"
                      style={{ fontSize: "3rem" }}
                    ></i>
                  </div>
                  <h4 className="fw-bold mb-3">Multiple Clinics</h4>
                  <p className="text-muted">
                    Access a wide network of general and specialist clinics
                    across Singapore. From cardiology to dermatology, we have
                    you covered.
                  </p>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="card h-100 border-0 shadow-sm hover-shadow transition">
                <div className="card-body text-center p-4">
                  <div className="rounded-circle bg-info bg-opacity-10 d-inline-flex p-4 mb-4">
                    <i
                      className="bi bi-clock-history text-info"
                      style={{ fontSize: "3rem" }}
                    ></i>
                  </div>
                  <h4 className="fw-bold mb-3">Manage Schedule</h4>
                  <p className="text-muted">
                    Track your medical appointments, view your healthcare
                    history, and manage everything from one convenient
                    dashboard.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Services Section */}
      <section className="py-5 bg-light">
        <div className="container py-5">
          <div className="text-center mb-5">
            <h2 className="display-5 fw-bold mb-3">Our Services</h2>
            <p className="lead text-muted">
              Comprehensive healthcare solutions for you and your family
            </p>
          </div>

          <div className="row g-4">
            <div className="col-md-6 col-lg-3">
              <div className="text-center p-4">
                <div className="rounded-circle bg-danger bg-opacity-10 d-inline-flex p-3 mb-3">
                  <i
                    className="bi bi-heart-pulse text-danger"
                    style={{ fontSize: "2.5rem" }}
                  ></i>
                </div>
                <h5 className="fw-bold">Cardiology</h5>
                <p className="text-muted small">
                  Expert heart care and treatment
                </p>
              </div>
            </div>

            <div className="col-md-6 col-lg-3">
              <div className="text-center p-4">
                <div className="rounded-circle bg-warning bg-opacity-10 d-inline-flex p-3 mb-3">
                  <i
                    className="bi bi-eye text-warning"
                    style={{ fontSize: "2.5rem" }}
                  ></i>
                </div>
                <h5 className="fw-bold">Ophthalmology</h5>
                <p className="text-muted small">Complete eye care services</p>
              </div>
            </div>

            <div className="col-md-6 col-lg-3">
              <div className="text-center p-4">
                <div className="rounded-circle bg-success bg-opacity-10 d-inline-flex p-3 mb-3">
                  <i
                    className="bi bi-bandaid text-success"
                    style={{ fontSize: "2.5rem" }}
                  ></i>
                </div>
                <h5 className="fw-bold">General Practice</h5>
                <p className="text-muted small">
                  Primary healthcare for all ages
                </p>
              </div>
            </div>

            <div className="col-md-6 col-lg-3">
              <div className="text-center p-4">
                <div className="rounded-circle bg-info bg-opacity-10 d-inline-flex p-3 mb-3">
                  <i
                    className="bi bi-activity text-info"
                    style={{ fontSize: "2.5rem" }}
                  ></i>
                </div>
                <h5 className="fw-bold">Specialist Care</h5>
                <p className="text-muted small">
                  Neurology, ENT, Orthopedics & more
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-5 bg-primary text-white">
        <div className="container py-5">
          <div className="row justify-content-center text-center">
            <div className="col-lg-8">
              <h2 className="display-5 fw-bold mb-4">Ready to Get Started?</h2>
              <p className="lead mb-4">
                Join thousands of patients who trust SingHealth for their
                healthcare needs.
              </p>
              <Link
                to="/signup"
                className="btn btn-light btn-lg px-5 py-3 shadow"
              >
                Create Your Account Today
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-dark text-white py-4">
        <div className="container">
          <div className="row">
            <div className="col-md-6 text-center text-md-start mb-3 mb-md-0">
              <p className="mb-0">
                <i className="bi bi-heart-pulse-fill me-2"></i>© 2025
                SingHealth. All rights reserved.
              </p>
            </div>
            <div className="col-md-6 text-center text-md-end">
              <a href="#" className="text-white text-decoration-none me-3">
                Privacy Policy
              </a>
              <a href="#" className="text-white text-decoration-none me-3">
                Terms of Service
              </a>
              <a href="#" className="text-white text-decoration-none">
                Contact Us
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
