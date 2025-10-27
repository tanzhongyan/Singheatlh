import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const Navbar = () => {
  const { user, userProfile, signOut } = useAuth();
  const navigate = useNavigate();

  const handleSignOut = async () => {
    try {
      await signOut();
      navigate("/");
    } catch (error) {
      console.error("Error signing out:", error);
    }
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
      <div className="container">
        <Link
          className="navbar-brand fw-bold"
          to={user ? (userProfile?.role === "S" ? "/admin" : "/home") : "/"}
        >
          <i className="bi bi-heart-pulse-fill me-2 text-primary"></i>
          SingHealth
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav ms-auto align-items-center">
            {user && userProfile ? (
              <>
                {/* Patient Navigation */}
                {userProfile.role === "P" && (
                  <>
                    <li className="nav-item">
                      <Link className="nav-link" to="/home">
                        <i className="bi bi-house-door me-1"></i>
                        Home
                      </Link>
                    </li>
                    <li className="nav-item">
                      <Link className="nav-link" to="/appointments">
                        <i className="bi bi-calendar-check me-1"></i>
                        My Appointments
                      </Link>
                    </li>
                  </>
                )}

                {/* Admin Navigation */}
                {userProfile.role === "S" && (
                  <>
                    <li className="nav-item">
                      <Link className="nav-link" to="/admin">
                        <i className="bi bi-speedometer2 me-1"></i>
                        Dashboard
                      </Link>
                    </li>
                    <li className="nav-item">
                      <Link className="nav-link" to="/admin/users">
                        <i className="bi bi-people me-1"></i>
                        Users
                      </Link>
                    </li>
                    <li className="nav-item">
                      <Link className="nav-link" to="/admin/doctors">
                        <i className="bi bi-heart-pulse me-1"></i>
                        Doctors
                      </Link>
                    </li>
                    <li className="nav-item">
                      <Link className="nav-link" to="/admin/clinics">
                        <i className="bi bi-hospital me-1"></i>
                        Clinics
                      </Link>
                    </li>
                  </>
                )}

                <li className="nav-item dropdown">
                  <a
                    className="nav-link dropdown-toggle"
                    href="#"
                    id="userDropdown"
                    role="button"
                    data-bs-toggle="dropdown"
                    aria-expanded="false"
                  >
                    <i className="bi bi-person-circle me-1"></i>
                    {userProfile.name || "User"}
                  </a>
                  <ul
                    className="dropdown-menu dropdown-menu-end"
                    aria-labelledby="userDropdown"
                  >
                    <li>
                      <span className="dropdown-item-text small text-muted">
                        {user.email}
                      </span>
                    </li>
                    <li>
                      <span className="dropdown-item-text small">
                        <span className="badge bg-primary">
                          {userProfile.role === "S" ? "Admin" : "Patient"}
                        </span>
                      </span>
                    </li>
                    <li>
                      <hr className="dropdown-divider" />
                    </li>
                    <li>
                      <button
                        className="dropdown-item text-danger"
                        onClick={handleSignOut}
                      >
                        <i className="bi bi-box-arrow-right me-2"></i>
                        Logout
                      </button>
                    </li>
                  </ul>
                </li>
              </>
            ) : (
              <>
                <li className="nav-item">
                  <Link className="nav-link" to="/login">
                    <i className="bi bi-box-arrow-in-right me-1"></i>
                    Login
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="btn btn-primary btn-sm ms-2" to="/signup">
                    <i className="bi bi-person-plus me-1"></i>
                    Sign Up
                  </Link>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
