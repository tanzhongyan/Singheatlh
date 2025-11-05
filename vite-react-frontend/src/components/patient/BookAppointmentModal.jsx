import { useState, useEffect } from "react";
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
  const [toasts, setToasts] = useState([]);
  const [clinicTypeFilter, setClinicTypeFilter] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const clinicsPerPage = 6; // 'all', 'G', 'S'

  const useVersion = "v2"; // switch to v1 to use DateTimeSelector
  const TOAST_LIFETIME = 5000; // ms

  // Inject keyframes for slide-down animation
  useEffect(() => {
    const styleId = 'toast-animation-styles';
    if (!document.getElementById(styleId)) {
      const style = document.createElement('style');
      style.id = styleId;
      style.textContent = `
        @keyframes slideDown {
          from {
            transform: translateY(-100%);
            opacity: 0;
          }
          to {
            transform: translateY(0);
            opacity: 1;
          }
        }
      `;
      document.head.appendChild(style);
    }
  }, []);

  const addToast = (type, text) => {
    const id = Date.now() + Math.random();
    const toast = { id, type, text };
    setToasts((t) => [...t, toast]);
    // auto-remove
    setTimeout(() => {
      setToasts((t) => t.filter((x) => x.id !== id));
    }, TOAST_LIFETIME);
  };

  // Fetch clinics only once when modal opens for the first time
  useEffect(() => {
    if (show && !hasFetched) {
      fetchClinics();
      setHasFetched(true);
    }
    // Clear toasts when modal opens
    if (show) {
      setToasts([]);
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
      const errorText = typeof errorMessage === "string"
          ? errorMessage
          : JSON.stringify(errorMessage);
      
      // Clear any existing toasts and show error toast
      setToasts([]);
      addToast('error', errorText);
      setError(errorText); // Keep for backward compatibility
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
    setToasts([]);
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

  const toastContainer = (
    <div style={{ 
      position: 'fixed', 
      top: '2rem', 
      left: '50%', 
      transform: 'translateX(-50%)',
      zIndex: 2000,
      minWidth: '400px',
      maxWidth: '600px'
    }}>
      {toasts.map((t) => (
        <div
          key={t.id}
          className={`alert ${t.type === 'success' ? 'alert-success' : 'alert-danger'} shadow-lg`}
          style={{ 
            marginBottom: '0.5rem', 
            opacity: 1, 
            transition: 'all 0.3s ease-in-out',
            fontSize: '1rem',
            fontWeight: '500',
            padding: '1rem 1.5rem',
            borderRadius: '0.5rem',
            border: 'none',
            animation: 'slideDown 0.3s ease-out'
          }}
        >
          <div className="d-flex align-items-center">
            {t.type === 'success' ? (
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" className="me-3" viewBox="0 0 16 16">
                <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zm-3.97-3.03a.75.75 0 0 0-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-.01-1.05z"/>
              </svg>
            ) : (
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" className="me-3" viewBox="0 0 16 16">
                <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM8 4a.905.905 0 0 0-.9.995l.35 3.507a.552.552 0 0 0 1.1 0l.35-3.507A.905.905 0 0 0 8 4zm.002 6a1 1 0 1 0 0 2 1 1 0 0 0 0-2z"/>
              </svg>
            )}
            <span>{t.text}</span>
          </div>
        </div>
      ))}
    </div>
  );

  return (
    <>
      {toastContainer}
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
                <h6 className="mb-4">Select a Clinic</h6>
                
                {/* Search and Filter Section */}
                <div className="row g-3 mb-4">
                  {/* Search Bar */}
                  <div className="col-md-8">
                    <div className="input-group" style={{ border: '1px solid #dee2e6', borderRadius: '0.375rem', overflow: 'hidden' }}>
                      <span className="input-group-text bg-white border-0">
                        <i className="bi bi-search text-muted"></i>
                      </span>
                      <input
                        type="text"
                        className="form-control border-0"
                        placeholder="Search clinics by name..."
                        value={searchQuery}
                        onChange={(e) => {
                          setSearchQuery(e.target.value);
                          setCurrentPage(1); // Reset to first page on search
                        }}
                        style={{ 
                          boxShadow: 'none',
                          outline: 'none'
                        }}
                      />
                      {searchQuery && (
                        <button
                          className="btn border-0"
                          type="button"
                          onClick={() => {
                            setSearchQuery('');
                            setCurrentPage(1);
                          }}
                          style={{ backgroundColor: 'transparent' }}
                        >
                          <i className="bi bi-x-lg text-secondary"></i>
                        </button>
                      )}
                    </div>
                  </div>
                  
                  {/* Clinic Type Dropdown Filter */}
                  <div className="col-md-4">
                    <select
                      className="form-select"
                      value={clinicTypeFilter}
                      onChange={(e) => {
                        setClinicTypeFilter(e.target.value);
                        setCurrentPage(1); // Reset to first page on filter change
                      }}
                      style={{ 
                        cursor: 'pointer',
                        boxShadow: 'none'
                      }}
                    >
                      <option value="all">All Types</option>
                      <option value="G">General Practitioner</option>
                      <option value="S">Specialist</option>
                    </select>
                  </div>
                </div>

                {loading ? (
                  <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                      <span className="visually-hidden">Loading...</span>
                    </div>
                  </div>
                ) : (
                  <>
                    {/* Clinic Cards Grid */}
                    <div className="row g-3" style={{ minHeight: '300px' }}>
                      {(() => {
                        // Filter and search logic
                        const filteredClinics = clinics
                          .filter(clinic => clinicTypeFilter === 'all' || clinic.type === clinicTypeFilter)
                          .filter(clinic => 
                            searchQuery === '' || 
                            clinic.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                            clinic.address.toLowerCase().includes(searchQuery.toLowerCase())
                          );
                        
                        // Pagination logic
                        const indexOfLastClinic = currentPage * clinicsPerPage;
                        const indexOfFirstClinic = indexOfLastClinic - clinicsPerPage;
                        const currentClinics = filteredClinics.slice(indexOfFirstClinic, indexOfLastClinic);
                        const totalPages = Math.ceil(filteredClinics.length / clinicsPerPage);
                        
                        if (filteredClinics.length === 0) {
                          return (
                            <div className="col-12">
                              <div className="text-center py-5 text-muted">
                                <i className="bi bi-search" style={{ fontSize: '3rem' }}></i>
                                <p className="mt-3 mb-0">No clinics found matching your search.</p>
                                {(searchQuery || clinicTypeFilter !== 'all') && (
                                  <button 
                                    className="btn btn-sm btn-outline-primary mt-2"
                                    onClick={() => {
                                      setSearchQuery('');
                                      setClinicTypeFilter('all');
                                      setCurrentPage(1);
                                    }}
                                  >
                                    Clear Filters
                                  </button>
                                )}
                              </div>
                            </div>
                          );
                        }
                        
                        return (
                          <>
                            {currentClinics.map((clinic) => (
                              <div key={clinic.clinicId} className="col-12">
                                <div
                                  className="card border hover-shadow h-100"
                                  onClick={() => handleClinicSelect(clinic)}
                                  style={{ 
                                    cursor: "pointer", 
                                    transition: "all 0.2s",
                                    borderRadius: '8px'
                                  }}
                                  onMouseEnter={(e) => {
                                    e.currentTarget.style.transform = 'translateY(-2px)';
                                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                                  }}
                                  onMouseLeave={(e) => {
                                    e.currentTarget.style.transform = 'translateY(0)';
                                    e.currentTarget.style.boxShadow = '';
                                  }}
                                >
                                  <div className="card-body p-3">
                                    <div className="d-flex justify-content-between align-items-start">
                                      <div className="flex-grow-1 me-3">
                                        <h6 className="mb-1 fw-semibold">{clinic.name}</h6>
                                        <p className="text-muted small mb-2" style={{ lineHeight: '1.4' }}>
                                          <i className="bi bi-geo-alt me-1"></i>
                                          {clinic.address}
                                        </p>
                                        <p className="text-muted small mb-0">
                                          <i className="bi bi-telephone me-1"></i>
                                          {clinic.telephoneNumber || 'N/A'}
                                        </p>
                                      </div>
                                      <span 
                                        className="badge rounded-pill"
                                        style={{
                                          backgroundColor: clinic.type === 'G' ? '#e3f2fd' : '#f3e5f5',
                                          color: clinic.type === 'G' ? '#1976d2' : '#7b1fa2',
                                          padding: '6px 12px',
                                          fontSize: '0.75rem',
                                          fontWeight: '500'
                                        }}
                                      >
                                        {clinic.type === 'G' ? 'GP' : 'Specialist'}
                                      </span>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            ))}
                            
                            {/* Pagination Controls */}
                            {totalPages > 1 && (
                              <div className="col-12">
                                <nav aria-label="Clinic pagination">
                                  <ul className="pagination pagination-sm justify-content-center mb-0">
                                    <li className={`page-item ${currentPage === 1 ? 'disabled' : ''}`}>
                                      <button
                                        className="page-link"
                                        onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                                        disabled={currentPage === 1}
                                      >
                                        <i className="bi bi-chevron-left"></i>
                                      </button>
                                    </li>
                                    
                                    {[...Array(totalPages)].map((_, index) => {
                                      const pageNumber = index + 1;
                                      // Show first page, last page, current page, and pages around current
                                      if (
                                        pageNumber === 1 ||
                                        pageNumber === totalPages ||
                                        (pageNumber >= currentPage - 1 && pageNumber <= currentPage + 1)
                                      ) {
                                        return (
                                          <li 
                                            key={pageNumber} 
                                            className={`page-item ${currentPage === pageNumber ? 'active' : ''}`}
                                          >
                                            <button
                                              className="page-link"
                                              onClick={() => setCurrentPage(pageNumber)}
                                            >
                                              {pageNumber}
                                            </button>
                                          </li>
                                        );
                                      } else if (
                                        pageNumber === currentPage - 2 ||
                                        pageNumber === currentPage + 2
                                      ) {
                                        return <li key={pageNumber} className="page-item disabled"><span className="page-link">...</span></li>;
                                      }
                                      return null;
                                    })}
                                    
                                    <li className={`page-item ${currentPage === totalPages ? 'disabled' : ''}`}>
                                      <button
                                        className="page-link"
                                        onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                                        disabled={currentPage === totalPages}
                                      >
                                        <i className="bi bi-chevron-right"></i>
                                      </button>
                                    </li>
                                  </ul>
                                </nav>
                                
                                {/* Results count */}
                                <div className="text-center mt-2 small text-muted">
                                  Showing {indexOfFirstClinic + 1}-{Math.min(indexOfLastClinic, filteredClinics.length)} of {filteredClinics.length} clinics
                                </div>
                              </div>
                            )}
                          </>
                        );
                      })()}
                    </div>
                  </>
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
              useVersion === "v1" ? (
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
    </>
  );
};

export default BookAppointmentModal;
