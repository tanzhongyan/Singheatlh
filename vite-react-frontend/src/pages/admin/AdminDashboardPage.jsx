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
                <div className="rounded-circle bg-primary bg-opacity-10 p-3">
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
                <div className="rounded-circle bg-success bg-opacity-10 p-3">
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
                <div className="rounded-circle bg-info bg-opacity-10 p-3">
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
                  <div className="rounded-circle bg-primary bg-opacity-10 p-3 me-3">
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
                  <div className="rounded-circle bg-success bg-opacity-10 p-3 me-3">
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
                  <div className="rounded-circle bg-info bg-opacity-10 p-3 me-3">
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
      </div>

      {/* Recent Activity Section */}
      <div className="row mt-4">
        <div className="col-12">
          <div className="card border-0 shadow-sm">
            <div className="card-header bg-white py-3">
              <h5 className="mb-0 fw-bold">
                <i className="bi bi-clock-history me-2 text-muted"></i>
                Recent Activity
              </h5>
            </div>
            <div className="card-body">
              <div className="list-group list-group-flush">
                <div className="list-group-item border-0 px-0">
                  <div className="d-flex align-items-center">
                    <div className="rounded-circle bg-success bg-opacity-10 p-2 me-3">
                      <i className="bi bi-person-plus text-success"></i>
                    </div>
                    <div className="flex-grow-1">
                      <p className="mb-0 fw-semibold">New user registered</p>
                      <small className="text-muted">2 hours ago</small>
                    </div>
                  </div>
                </div>
                <div className="list-group-item border-0 px-0">
                  <div className="d-flex align-items-center">
                    <div className="rounded-circle bg-primary bg-opacity-10 p-2 me-3">
                      <i className="bi bi-pencil text-primary"></i>
                    </div>
                    <div className="flex-grow-1">
                      <p className="mb-0 fw-semibold">Doctor profile updated</p>
                      <small className="text-muted">5 hours ago</small>
                    </div>
                  </div>
                </div>
                <div className="list-group-item border-0 px-0">
                  <div className="d-flex align-items-center">
                    <div className="rounded-circle bg-info bg-opacity-10 p-2 me-3">
                      <i className="bi bi-building text-info"></i>
                    </div>
                    <div className="flex-grow-1">
                      <p className="mb-0 fw-semibold">New clinic added</p>
                      <small className="text-muted">1 day ago</small>
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

export default AdminDashboardPage;
