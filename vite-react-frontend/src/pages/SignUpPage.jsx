import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const SignUpPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [fullName, setFullName] = useState("");
  const [telephoneNumber, setTelephoneNumber] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const { signUp, user, userProfile } = useAuth();
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");

    if (password !== confirmPassword) {
      setError("Passwords don't match");
      return;
    }

    if (password.length < 6) {
      setError("Password should be at least 6 characters");
      return;
    }

    setLoading(true);

    try {
      const { error } = await signUp({
        email,
        password,
        name: fullName || undefined,  // Optional field
        telephoneNumber: telephoneNumber || undefined  // Optional field
      });
      if (error) throw error;

      setMessage(
        "Check your email for verification link! You can now sign in."
      );
      setEmail("");
      setPassword("");
      setConfirmPassword("");
      setFullName("");
      setTelephoneNumber("");

      setTimeout(() => {
        navigate("/login");
      }, 3000);
    } catch (error) {
      setError(error.message);
    } finally {
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
                <p className="lead mb-4">Join Our Healthcare Community</p>
                <p className="mb-0 opacity-75">
                  Get started today and enjoy seamless access to quality
                  healthcare services.
                </p>
              </div>

              {/* Right Side - Signup Form */}
              <div className="col-md-6 p-5">
                <div className="mb-4">
                  <h2 className="fw-bold mb-2">Create Account</h2>
                  <p className="text-muted">Sign up to get started</p>
                </div>

                {error && (
                  <div className="alert alert-danger border-0" role="alert">
                    <i className="bi bi-exclamation-circle me-2"></i>
                    {error}
                  </div>
                )}

                {message && (
                  <div className="alert alert-success border-0" role="alert">
                    <i className="bi bi-check-circle me-2"></i>
                    {message}
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  <div className="mb-3">
                    <label htmlFor="email" className="form-label fw-semibold">
                      Email Address <span className="text-danger">*</span>
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

                  <div className="mb-3">
                    <label htmlFor="fullName" className="form-label fw-semibold">
                      Full Name <span className="text-muted">(Optional)</span>
                    </label>
                    <input
                      type="text"
                      className="form-control form-control-lg"
                      id="fullName"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      placeholder="Enter your full name"
                    />
                  </div>

                  <div className="mb-3">
                    <label htmlFor="telephoneNumber" className="form-label fw-semibold">
                      Telephone Number <span className="text-muted">(Optional)</span>
                    </label>
                    <input
                      type="tel"
                      className="form-control form-control-lg"
                      id="telephoneNumber"
                      value={telephoneNumber}
                      onChange={(e) => setTelephoneNumber(e.target.value)}
                      placeholder="Enter your phone number"
                    />
                  </div>

                  <div className="mb-3">
                    <label
                      htmlFor="password"
                      className="form-label fw-semibold"
                    >
                      Password <span className="text-danger">*</span>
                    </label>
                    <input
                      type="password"
                      className="form-control form-control-lg"
                      id="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Create a password"
                      required
                    />
                    <div className="form-text">Minimum 6 characters</div>
                  </div>

                  <div className="mb-4">
                    <label
                      htmlFor="confirmPassword"
                      className="form-label fw-semibold"
                    >
                      Confirm Password <span className="text-danger">*</span>
                    </label>
                    <input
                      type="password"
                      className="form-control form-control-lg"
                      id="confirmPassword"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Confirm your password"
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
                        Creating Account...
                      </>
                    ) : (
                      "Create Account"
                    )}
                  </button>
                </form>

                <div className="text-center">
                  <p className="text-muted mb-0">
                    Already have an account?{" "}
                    <Link
                      to="/login"
                      className="text-primary text-decoration-none fw-semibold"
                    >
                      Sign In
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

export default SignUpPage;
