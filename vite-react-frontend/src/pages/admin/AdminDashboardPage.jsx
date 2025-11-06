import { Link } from "react-router-dom";
import { useDashboardCounts } from "../../hooks/useDashboardCounts";

const AdminDashboardPage = () => {
  const { userCount, doctorCount, clinicCount } = useDashboardCounts();
  return (
    <div className="container-fluid py-4">
      {/* Welcome Header */}
      <div className="row mb-4">
        <div className="col-12">
          <div className="card border-0 shadow-sm bg-primary text-white">
            <div className="card-body p-4">
              <h1 className="display-5 fw-bold mb-2">
                <i className="bi bi-speedometer2 me-3"></i>
                Admin Dashboard
              </h1>
              <p className="lead mb-0 opacity-90">
                Welcome back, Administrator! Manage your healthcare system
                efficiently.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="row g-4 mb-4">
        <div className="col-md-4">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body p-4">
              <div className="d-flex justify-content-between align-items-start">
                <div>
                  <p className="text-muted mb-1 text-uppercase small fw-semibold">
                    Total Users
                  </p>
                  <h3 className="fw-bold mb-0">{userCount ? userCount : ""}</h3>
                </div>
                <div
                  className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                  style={{ width: "60px", height: "60px" }}
                >
                  <i
                    className="bi bi-people text-primary"
                    style={{ fontSize: "1.5rem" }}
                  ></i>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body p-4">
              <div className="d-flex justify-content-between align-items-start">
                <div>
                  <p className="text-muted mb-1 text-uppercase small fw-semibold">
                    Total Doctors
                  </p>
                  <h3 className="fw-bold mb-0">
                    {doctorCount ? doctorCount : ""}
                  </h3>
                </div>
                <div
                  className="rounded-circle bg-success bg-opacity-10 d-flex align-items-center justify-content-center"
                  style={{ width: "60px", height: "60px" }}
                >
                  <i
                    className="bi bi-heart-pulse text-success"
                    style={{ fontSize: "1.5rem" }}
                  ></i>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card border-0 shadow-sm h-100">
            <div className="card-body p-4">
              <div className="d-flex justify-content-between align-items-start">
                <div>
                  <p className="text-muted mb-1 text-uppercase small fw-semibold">
                    Total Clinics
                  </p>
                  <h3 className="fw-bold mb-0">
                    {clinicCount ? clinicCount : ""}
                  </h3>
                </div>
                <div
                  className="rounded-circle bg-info bg-opacity-10 d-flex align-items-center justify-content-center"
                  style={{ width: "60px", height: "60px" }}
                >
                  <i
                    className="bi bi-hospital text-info"
                    style={{ fontSize: "1.5rem" }}
                  ></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Management Cards */}
      <div className="row g-4">
        <div className="col-lg-4 col-md-6">
          <Link to="/admin/users" className="text-decoration-none">
            <div className="card border-0 shadow-sm h-100 hover-card">
              <div className="card-body p-4">
                <div className="d-flex align-items-center mb-3">
                  <div
                    className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center me-3"
                    style={{ width: "70px", height: "70px", flexShrink: 0 }}
                  >
                    <i
                      className="bi bi-people text-primary"
                      style={{ fontSize: "2rem" }}
                    ></i>
                  </div>
                  <h4 className="card-title mb-0 fw-bold text-dark">
                    User Management
                  </h4>
                </div>
                <p className="card-text text-muted mb-3">
                  Manage user accounts, roles, and permissions. View, add, edit,
                  or remove users from the system.
                </p>
                <div className="d-flex align-items-center text-primary">
                  <span className="fw-semibold">Manage Users</span>
                  <i className="bi bi-arrow-right ms-2"></i>
                </div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-lg-4 col-md-6">
          <Link to="/admin/doctors" className="text-decoration-none">
            <div className="card border-0 shadow-sm h-100 hover-card">
              <div className="card-body p-4">
                <div className="d-flex align-items-center mb-3">
                  <div
                    className="rounded-circle bg-success bg-opacity-10 d-flex align-items-center justify-content-center me-3"
                    style={{ width: "70px", height: "70px", flexShrink: 0 }}
                  >
                    <i
                      className="bi bi-heart-pulse text-success"
                      style={{ fontSize: "2rem" }}
                    ></i>
                  </div>
                  <h4 className="card-title mb-0 fw-bold text-dark">
                    Doctor Management
                  </h4>
                </div>
                <p className="card-text text-muted mb-3">
                  Oversee doctor profiles, specializations, and clinic
                  assignments. Add new doctors or update existing ones.
                </p>
                <div className="d-flex align-items-center text-success">
                  <span className="fw-semibold">Manage Doctors</span>
                  <i className="bi bi-arrow-right ms-2"></i>
                </div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-lg-4 col-md-6">
          <Link to="/admin/clinics" className="text-decoration-none">
            <div className="card border-0 shadow-sm h-100 hover-card">
              <div className="card-body p-4">
                <div className="d-flex align-items-center mb-3">
                  <div
                    className="rounded-circle bg-info bg-opacity-10 d-flex align-items-center justify-content-center me-3"
                    style={{ width: "70px", height: "70px", flexShrink: 0 }}
                  >
                    <i
                      className="bi bi-hospital text-info"
                      style={{ fontSize: "2rem" }}
                    ></i>
                  </div>
                  <h4 className="card-title mb-0 fw-bold text-dark">
                    Clinic Management
                  </h4>
                </div>
                <p className="card-text text-muted mb-3">
                  Manage clinic information, locations, operating hours, and
                  services. Add or update clinic details.
                </p>
                <div className="d-flex align-items-center text-info">
                  <span className="fw-semibold">Manage Clinics</span>
                  <i className="bi bi-arrow-right ms-2"></i>
                </div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-lg-4 col-md-6">
          <Link to="/admin/monitoring" className="text-decoration-none">
            <div className="card border-0 shadow-sm h-100 hover-card">
              <div className="card-body p-4">
                <div className="d-flex align-items-center mb-3">
                  <div
                    className="rounded-circle bg-warning bg-opacity-10 d-flex align-items-center justify-content-center me-3"
                    style={{ width: "70px", height: "70px", flexShrink: 0 }}
                  >
                    <i
                      className="bi bi-graph-up text-warning"
                      style={{ fontSize: "2rem" }}
                    ></i>
                  </div>
                  <h4 className="card-title mb-0 fw-bold text-dark">
                    System Monitoring
                  </h4>
                </div>
                <p className="card-text text-muted mb-3">
                  View real-time system statistics, usage metrics, and
                  performance overview. Monitor active users and system health.
                </p>
                <div className="d-flex align-items-center text-warning">
                  <span className="fw-semibold">View Statistics</span>
                  <i className="bi bi-arrow-right ms-2"></i>
                </div>
              </div>
            </div>
          </Link>
        </div>

        <div className="col-lg-4 col-md-6">
          <Link to="/admin/backup" className="text-decoration-none">
            <div className="card border-0 shadow-sm h-100 hover-card">
              <div className="card-body p-4">
                <div className="d-flex align-items-center mb-3">
                  <div
                    className="rounded-circle bg-danger bg-opacity-10 d-flex align-items-center justify-content-center me-3"
                    style={{ width: "70px", height: "70px", flexShrink: 0 }}
                  >
                    <i
                      className="bi bi-cloud-arrow-down text-danger"
                      style={{ fontSize: "2rem" }}
                    ></i>
                  </div>
                  <h4 className="card-title mb-0 fw-bold text-dark">
                    Backup & Restore
                  </h4>
                </div>
                <p className="card-text text-muted mb-3">
                  Create system backups, manage backup history, download backups,
                  and restore data from previous backup points.
                </p>
                <div className="d-flex align-items-center text-danger">
                  <span className="fw-semibold">Manage Backups</span>
                  <i className="bi bi-arrow-right ms-2"></i>
                </div>
              </div>
            </div>
          </Link>
        </div>
      </div>

    </div>
  );
};

export default AdminDashboardPage;
