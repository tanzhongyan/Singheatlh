import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const { signIn, user, userProfile } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    console.log(
      "LoginPage useEffect - user:",
      user,
      "userProfile:",
      userProfile
    );

    // Redirect based on user role from user_profile table if already logged in
    if (user && userProfile) {
      console.log("User role:", userProfile.role);

      if (userProfile.role === "S") {
        // System Admin -> redirect to admin dashboard
        console.log("Redirecting to /admin");
        navigate("/admin", { replace: true });
      } else if (userProfile.role === "P") {
        // Patient -> redirect to patient home
        console.log("Redirecting to /home");
        navigate("/home", { replace: true });
      } else if (userProfile.role === "C") {
        // Clinic Staff -> redirect to staff dashboard
        console.log("Redirecting to /staff");
        navigate("/staff", { replace: true });
      }
    }
  }, [user, userProfile, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const { error } = await signIn({ email, password });
      if (error) throw error;

      // Note: The redirect will happen via useEffect once userProfile is loaded from user_profile table
    } catch (error) {
      setError(error.message);
      console.error("Login error:", error);
      setLoading(false);
    }
  };

  return (
    <div className="min-vh-100 d-flex" style={{ backgroundColor: "#f8f9fa" }}>
      <div className="container">
        <div className="row justify-content-center align-items-center min-vh-100">
          <div className="col-md-10 col-lg-8">
            <div className="row shadow-lg rounded-4 overflow-hidden bg-white">
              {/* Left Side - Branding */}
              <div className="col-md-6 bg-primary text-white p-5 d-flex flex-column justify-content-center">
                <h1 className="display-4 fw-bold mb-3">
                  <i className="bi bi-heart-pulse-fill me-3"></i>
                  SingHealth
                </h1>
                <p className="lead mb-4">Your Health, Our Priority</p>
                <p className="mb-0 opacity-75">
                  Access your medical appointments, records, and healthcare
                  services all in one place.
                </p>
              </div>

              {/* Right Side - Login Form */}
              <div className="col-md-6 p-5">
                <div className="mb-4">
                  <h2 className="fw-bold mb-2">Welcome Back</h2>
                  <p className="text-muted">Please sign in to continue</p>
                </div>

                {error && (
                  <div className="alert alert-danger border-0" role="alert">
                    <i className="bi bi-exclamation-circle me-2"></i>
                    {error}
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  <div className="mb-3">
                    <label htmlFor="email" className="form-label fw-semibold">
                      Email Address
                    </label>
                    <input
                      type="email"
                      className="form-control form-control-lg"
                      id="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="you@example.com"
                      required
                    />
                  </div>

                  <div className="mb-4">
                    <label
                      htmlFor="password"
                      className="form-label fw-semibold"
                    >
                      Password
                    </label>
                    <input
                      type="password"
                      className="form-control form-control-lg"
                      id="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Enter your password"
                      required
                    />
                  </div>

                  <button
                    type="submit"
                    className="btn btn-primary btn-lg w-100 mb-3"
                    disabled={loading}
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" />
                        Signing In...
                      </>
                    ) : (
                      "Sign In"
                    )}
                  </button>
                </form>

                <div className="text-center">
                  <p className="text-muted mb-0">
                    Don't have an account?{" "}
                    <Link
                      to="/signup"
                      className="text-primary text-decoration-none fw-semibold"
                    >
                      Create Account
                    </Link>
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
