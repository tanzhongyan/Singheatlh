import { useState, useEffect, version } from "react";
import { useAuth } from "../../contexts/AuthContext";
import apiClient from "../../api/apiClient";
import DateTimeSelector from "./DateTimeSelector";
import SelectSlot from "./SelectSlot";

const BookAppointmentModal = ({ show, onHide, onSuccess }) => {
  const { user } = useAuth();
  const [step, setStep] = useState(1); // 1: Select Clinic, 2: Select Doctor, 3: Select Date/Time
  const [clinics, setClinics] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [selectedClinic, setSelectedClinic] = useState(null);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedTime, setSelectedTime] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [hasFetched, setHasFetched] = useState(false);

  const useVersion = "v2"; // switch to v1 to use DateTimeSelector

  // Fetch clinics only once when modal opens for the first time
  useEffect(() => {
    if (show && !hasFetched) {
      fetchClinics();
      setHasFetched(true);
    }
  }, [show, hasFetched]);

  const fetchClinics = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get("/api/clinic");
      setClinics(response.data);
    } catch (err) {
      console.error("Error fetching clinics:", err);
      setError(
        "Failed to load clinics. Please make sure the backend is running."
      );
    } finally {
      setLoading(false);
    }
  };

  const fetchDoctorsByClinic = async (clinicId) => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get(`/api/doctor/clinic/${clinicId}`);
      setDoctors(response.data);
    } catch (err) {
      console.error("Error fetching doctors:", err);
      setError("Failed to load doctors for this clinic.");
    } finally {
      setLoading(false);
    }
  };

  const handleClinicSelect = (clinic) => {
    setSelectedClinic(clinic);
    fetchDoctorsByClinic(clinic.clinicId);
    setStep(2);
  };

  const handleDoctorSelect = (doctor) => {
    setSelectedDoctor(doctor);
    setStep(3);
  };

  const handleBookAppointment = async (e) => {
    e.preventDefault();

    if (!selectedDoctor || !selectedDate || !selectedTime) {
      setError("Please fill in all fields");
      return;
    }

    if (!user?.id) {
      setError("User not authenticated. Please log in again.");
      return;
    }

    try {
      setLoading(true);
      setError(null);

      // Calculate start and end datetime (15 minutes duration)
      const startDatetime = `${selectedDate}T${selectedTime}:00`;

      // Parse the datetime string and add 15 minutes
      const [datePart, timePart] = startDatetime.split("T");
      const [hours, minutes] = timePart.split(":").map(Number);

      // Add 15 minutes
      const totalMinutes = hours * 60 + minutes + 15;
      const endHours = String(Math.floor(totalMinutes / 60)).padStart(2, "0");
      const endMinutes = String(totalMinutes % 60).padStart(2, "0");
      const endDatetime = `${datePart}T${endHours}:${endMinutes}:00`;

      const payload = {
        patientId: user.id,
        doctorId: selectedDoctor.doctorId,
        startDatetime: startDatetime,
        endDatetime: endDatetime,
      };

      await apiClient.post("/api/appointments", payload);

      // Show success message
      setSuccess(true);
      setError(null);

      // Wait 2 seconds then close and refresh
      setTimeout(() => {
        onSuccess();
        resetModal();
      }, 2000);
    } catch (err) {
      console.error("Error booking appointment:", err);
      const errorMessage =
        err.response?.data?.message ||
        err.response?.data ||
        err.message ||
        "Failed to book appointment. Please try again.";
      setError(
        typeof errorMessage === "string"
          ? errorMessage
          : JSON.stringify(errorMessage)
      );
    } finally {
      setLoading(false);
    }
  };

  const resetModal = () => {
    setStep(1);
    setSelectedClinic(null);
    setSelectedDoctor(null);
    setSelectedDate("");
    setSelectedTime("");
    setError(null);
    setSuccess(false);
    setDoctors([]);
  };

  const handleClose = () => {
    resetModal();
    onHide();
  };

  const goBack = () => {
    if (step === 2) {
      setSelectedClinic(null);
      setDoctors([]);
      setError(null);
      setStep(1);
    } else if (step === 3) {
      setSelectedDoctor(null);
      setSelectedDate("");
      setSelectedTime("");
      setError(null);
      setStep(2);
    }
  };
  if (!show) return null;

  return (
    <div
      className="modal show d-block"
      style={{ backgroundColor: "rgba(0,0,0,0.4)", zIndex: 1050 }}
    >
      <div className="modal-dialog modal-dialog-centered modal-lg">
        <div className="modal-content border-0 shadow-lg">
          <div className="modal-header border-0 pb-0">
            <div>
              <h4 className="modal-title mb-1">Book Appointment</h4>
              <p className="text-muted small mb-0">Step {step} of 3</p>
            </div>
            <button
              type="button"
              className="btn-close"
              onClick={handleClose}
            ></button>
          </div>

          <div className="modal-body px-4" style={{ minHeight: "400px" }}>
            {/* Progress Bar */}
            <div className="mb-4">
              <div className="progress" style={{ height: "3px" }}>
                <div
                  className="progress-bar bg-primary"
                  style={{
                    width: `${(step / 3) * 100}%`,
                    transition: "width 0.3s",
                  }}
                ></div>
              </div>
            </div>

            {success && (
              <div
                className="alert alert-light border border-primary bg-white mb-4"
                role="alert"
              >
                <div className="d-flex align-items-center">
                  <i
                    className="bi bi-check-circle-fill text-primary me-3"
                    style={{ fontSize: "1.5rem" }}
                  ></i>
                  <div>
                    <h6 className="mb-1 fw-bold">
                      Appointment Booked Successfully
                    </h6>
                    <p className="mb-0 text-muted small">
                      Your appointment has been confirmed. Redirecting...
                    </p>
                  </div>
                </div>
              </div>
            )}

            {error && (
              <div
                className="alert alert-danger border-0 d-flex align-items-start"
                role="alert"
              >
                <i className="bi bi-exclamation-circle me-2 mt-1"></i>
                <div className="flex-grow-1">{error}</div>
                <button
                  className="btn btn-sm btn-outline-danger ms-2"
                  onClick={() => {
                    setError(null);
                    if (step === 1) {
                      fetchClinics();
                    } else if (step === 2 && selectedClinic) {
                      fetchDoctorsByClinic(selectedClinic.clinicId);
                    }
                  }}
                >
                  Retry
                </button>
              </div>
            )}

            {/* Step 1: Select Clinic */}
            {step === 1 && !success && (
              <div>
                <h6 className="mb-3">Select a Clinic</h6>
                {loading ? (
                  <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                      <span className="visually-hidden">Loading...</span>
                    </div>
                  </div>
                ) : (
                  <div className="row g-3">
                    {clinics.map((clinic) => (
                      <div key={clinic.clinicId} className="col-12">
                        <div
                          className="card border hover-shadow h-100"
                          onClick={() => handleClinicSelect(clinic)}
                          style={{ cursor: "pointer", transition: "all 0.2s" }}
                        >
                          <div className="card-body">
                            <div className="d-flex justify-content-between align-items-start">
                              <div>
                                <h6 className="mb-1">{clinic.name}</h6>
                                <p className="text-muted small mb-0">
                                  {clinic.address}
                                </p>
                              </div>
                              <span className="badge bg-light text-dark border">
                                {clinic.type}
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Step 2: Select Doctor */}
            {step === 2 && !success && (
              <div>
                <div className="alert alert-light border mb-3">
                  <small className="text-muted">Selected Clinic</small>
                  <div className="fw-semibold">{selectedClinic?.name}</div>
                </div>
                <h6 className="mb-3">Select a Doctor</h6>
                {loading ? (
                  <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                      <span className="visually-hidden">Loading...</span>
                    </div>
                  </div>
                ) : doctors.length === 0 ? (
                  <div className="text-center py-5 text-muted">
                    <p>No doctors available at this clinic.</p>
                  </div>
                ) : (
                  <div className="row g-3">
                    {doctors.map((doctor) => (
                      <div key={doctor.doctorId} className="col-12">
                        <div
                          className="card border hover-shadow"
                          onClick={() => handleDoctorSelect(doctor)}
                          style={{ cursor: "pointer", transition: "all 0.2s" }}
                        >
                          <div className="card-body">
                            <div className="d-flex align-items-center">
                              <div className="rounded-circle bg-light p-3 me-3">
                                <i
                                  className="bi bi-person text-primary"
                                  style={{ fontSize: "1.5rem" }}
                                ></i>
                              </div>
                              <div>
                                <h6 className="mb-0">Dr. {doctor.name}</h6>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Step 3: Select Date & Time */}
            {step === 3 && !success ? (
              version === "v1" ? (
                <DateTimeSelector
                  selectedClinic={selectedClinic}
                  selectedDoctor={selectedDoctor}
                  selectedDate={selectedDate}
                  selectedTime={selectedTime}
                  setSelectedDate={setSelectedDate}
                  setSelectedTime={setSelectedTime}
                  handleBookAppointment={handleBookAppointment}
                />
              ) : (
                <SelectSlot
                  selectedClinic={selectedClinic}
                  selectedDoctor={selectedDoctor}
                  selectedDate={selectedDate}
                  selectedTime={selectedTime}
                  setSelectedDate={setSelectedDate}
                  setSelectedTime={setSelectedTime}
                />
              )
            ) : null}
          </div>

          <div className="modal-footer border-0 pt-0">
            {!success && (
              <>
                {step > 1 && (
                  <button
                    type="button"
                    className="btn btn-light"
                    onClick={goBack}
                  >
                    Back
                  </button>
                )}
                <button
                  type="button"
                  className="btn btn-light"
                  onClick={handleClose}
                >
                  Cancel
                </button>
                {step === 3 && (
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleBookAppointment}
                    disabled={loading || !selectedDate || !selectedTime}
                  >
                    {loading ? (
                      <>
                        <span
                          className="spinner-border spinner-border-sm me-2"
                          role="status"
                        ></span>
                        Booking...
                      </>
                    ) : (
                      "Confirm Booking"
                    )}
                  </button>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookAppointmentModal;
