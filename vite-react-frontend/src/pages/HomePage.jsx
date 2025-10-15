import { useAuth } from "../contexts/AuthContext";
import { Link } from "react-router-dom";

const HomePage = () => {
  const { user } = useAuth();

  return (
    <div className="min-vh-100 bg-light">
      <div className="container py-5">
        <div className="row justify-content-center">
          <div className="col-md-8 text-center">
            <h1 className="display-4 fw-bold text-primary mb-4">
              Welcome to MyApp
            </h1>
            <p className="lead text-muted mb-5">
              {user
                ? `Hello ${user.email}! You're successfully logged in.`
                : "Join us today and experience the best service."}
            </p>

            {!user && (
              <div className="d-grid gap-3 d-md-flex justify-content-md-center">
                <Link to="/signup" className="btn btn-primary btn-lg px-4">
                  Get Started
                </Link>
                <Link
                  to="/login"
                  className="btn btn-outline-primary btn-lg px-4"
                >
                  Login
                </Link>
              </div>
            )}

            {user && (
              <div className="mt-5">
                <div className="card shadow-sm">
                  <div className="card-body">
                    <h5 className="card-title">Your Dashboard</h5>
                    <p className="card-text">
                      You're all set! Start exploring your account features.
                    </p>
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
