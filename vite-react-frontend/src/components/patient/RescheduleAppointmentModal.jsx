import React, { useState, useEffect } from 'react';
import apiClient from '../../api/apiClient';
import SelectSlot from './SelectSlot';

const RescheduleAppointmentModal = ({ show, onHide, appointment, onSuccess }) => {
  const [step, setStep] = useState(1); // 1: Choose option, 2: Select Clinic, 3: Select Doctor, 4: Select Time
  const [clinics, setClinics] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [selectedClinic, setSelectedClinic] = useState(null);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedTime, setSelectedTime] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [clinicTypeFilter, setClinicTypeFilter] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const clinicsPerPage = 6;
  const [toasts, setToasts] = useState([]);
  
  const TOAST_LIFETIME = 5000; // ms

  // Inject keyframes for slide-down animation
  useEffect(() => {
    const styleId = 'toast-animation-styles-reschedule';
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

  // Reset form when modal is opened/closed or appointment changes
  useEffect(() => {
    if (show && appointment) {
      setStep(1);
      setSelectedClinic(null);
      setSelectedDoctor(null);
      setSelectedDate('');
      setSelectedTime('');
      setError(null);
      setSuccess(false);
      setSearchQuery('');
      setClinicTypeFilter('all');
      setCurrentPage(1);
      setToasts([]);
    }
  }, [show, appointment]);

  useEffect(() => {
    if (step === 2 && show) {
      fetchClinics();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [step, show]);

  useEffect(() => {
    if (step === 3 && selectedClinic && show) {
      fetchDoctorsByClinic(selectedClinic.clinicId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [step, selectedClinic, show]);

  const fetchClinics = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get('/api/clinic');
      setClinics(response.data || []);
    } catch (err) {
      const errorMsg = 'Failed to load clinics';
      setError(errorMsg);
      addToast('error', errorMsg);
      console.error('Error fetching clinics:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchDoctorsByClinic = async (clinicId) => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get(`/api/doctor/clinic/${clinicId}`);
      setDoctors(response.data || []);
    } catch (err) {
      const errorMsg = 'Failed to load doctors';
      setError(errorMsg);
      addToast('error', errorMsg);
      console.error('Error fetching doctors:', err);
    } finally {
      setLoading(false);
    }
  };

  if (!show) return null;

  const canReschedule = () => {
    if (!appointment?.startDatetime) return false;
    const appointmentTime = new Date(appointment.startDatetime);
    const now = new Date();
    const hoursDiff = (appointmentTime.getTime() - now.getTime()) / (1000 * 60 * 60);
    return hoursDiff >= 24;
  };

  const isNotReschedulable = !canReschedule();

  const handleKeepSameDoctor = () => {
    setSelectedClinic({ clinicId: appointment.clinicId, name: appointment.clinicName });
    setSelectedDoctor({ doctorId: appointment.doctorId, name: appointment.doctorName });
    setStep(4);
  };

  const handleChangeDoctor = () => {
    setStep(2);
  };

  const handleClinicSelect = (clinic) => {
    setSelectedClinic(clinic);
    setStep(3);
  };

  const handleDoctorSelect = (doctor) => {
    setSelectedDoctor(doctor);
    setStep(4);
  };

  const handleBack = () => {
    if (step === 4) {
      // Check if they kept the same doctor or changed
      if (selectedDoctor.doctorId === appointment.doctorId) {
        setStep(1);
      } else {
        setStep(3);
      }
    } else if (step === 3) {
      setStep(2);
    } else if (step === 2) {
      setStep(1);
    }
    setSelectedDate('');
    setSelectedTime('');
    setError(null);
  };

  const handleReschedule = async () => {
    setError(null);
    setToasts([]); // Clear existing toasts

    if (!canReschedule()) {
      const errorMsg = 'You can only reschedule appointments that are at least 24 hours away.';
      setError(errorMsg);
      addToast('error', errorMsg);
      return;
    }

    if (!selectedDate || !selectedTime) {
      const errorMsg = 'Please select a new date and time slot.';
      setError(errorMsg);
      addToast('error', errorMsg);
      return;
    }

    try {
      setLoading(true);
      const newDateTime = `${selectedDate}T${selectedTime}:00`;
      await apiClient.put(`/api/appointments/${appointment.appointmentId}/reschedule`, null, {
        params: { newDateTime }
      });
      setSuccess(true);
      addToast('success', 'Appointment rescheduled successfully!');
      setTimeout(() => {
        setLoading(false);
        setSuccess(false);
        onSuccess && onSuccess();
        onHide && onHide();
      }, 1200);
    } catch (err) {
      console.error('Reschedule error:', err);
      const message = err.response?.data?.message || err.response?.data || err.message || 'Failed to reschedule. Please try again.';
      const errorText = typeof message === 'string' ? message : JSON.stringify(message);
      setError(errorText);
      setToasts([]); // Clear any existing toasts
      addToast('error', errorText);
      setLoading(false);
    }
  };

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
      <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1060 }}>
        <div className="modal-dialog modal-dialog-centered modal-lg">
          <div className="modal-content border-0 shadow-lg" style={{ borderRadius: '12px' }}>
            <div className="modal-header border-0 pb-0">
              <div>
                <h5 className="modal-title mb-1">Reschedule Appointment</h5>
                <p className="text-muted small mb-0">
                  {step === 1 && 'Choose how to reschedule'}
                  {step === 2 && 'Select a new clinic'}
                  {step === 3 && 'Select a new doctor'}
                  {step === 4 && 'Select a new time slot'}
                </p>
              </div>
              <button type="button" className="btn-close" onClick={onHide}></button>
            </div>

          <div className="modal-body px-4" style={{ minHeight: '400px' }}>
            {/* Progress Bar */}
            {step > 1 && (
              <div className="mb-4">
                <div className="progress" style={{ height: '3px' }}>
                  <div
                    className="progress-bar bg-primary"
                    style={{
                      width: `${((step - 1) / 3) * 100}%`,
                      transition: 'width 0.3s'
                    }}
                  ></div>
                </div>
              </div>
            )}

            {success ? (
              <div className="alert alert-light border border-success d-flex align-items-center">
                <i className="bi bi-check-circle-fill text-success me-3" style={{ fontSize: '1.5rem' }}></i>
                <div>
                  <div className="fw-bold">Appointment Rescheduled Successfully</div>
                  <div className="small text-muted">Your appointment has been updated.</div>
                </div>
              </div>
            ) : (
              <div>
                {!canReschedule() ? (
                  <div className="alert alert-warning">
                    <i className="bi bi-exclamation-triangle me-2"></i>
                    This appointment is less than 24 hours away and cannot be rescheduled.
                  </div>
                ) : (
                  <>
                    {error && (
                      <div className="alert alert-danger border-0 d-flex align-items-start mb-3" role="alert">
                        <i className="bi bi-exclamation-circle me-2 mt-1"></i>
                        <div className="flex-grow-1">{error}</div>
                      </div>
                    )}

                    {/* Current Appointment Info */}
                    <div className="alert alert-light border mb-4">
                      <div className="small text-muted mb-1">Current Appointment</div>
                      <div className="d-flex justify-content-between align-items-start">
                        <div>
                          <div className="fw-semibold">{appointment?.clinicName || 'Unknown Clinic'}</div>
                          <div className="small">{appointment?.doctorName || 'Unknown Doctor'}</div>
                        </div>
                        <div className="text-end">
                          <div className="fw-semibold">
                            {new Date(appointment?.startDatetime).toLocaleDateString('en-US', {
                              month: 'short',
                              day: 'numeric',
                              year: 'numeric'
                            })}
                          </div>
                          <div className="small">
                            {new Date(appointment?.startDatetime).toLocaleTimeString('en-US', {
                              hour: '2-digit',
                              minute: '2-digit'
                            })}
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Step 1: Choose Option */}
                    {step === 1 && (
                      <div>
                        <h6 className="mb-4">How would you like to reschedule?</h6>
                        <div className="row g-3">
                          <div className="col-12">
                            <button
                              className="card border w-100 text-start p-0"
                              style={{
                                cursor: 'pointer',
                                transition: 'all 0.2s'
                              }}
                              onClick={handleKeepSameDoctor}
                              onMouseEnter={(e) => {
                                e.currentTarget.style.transform = 'translateY(-2px)';
                                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                              }}
                              onMouseLeave={(e) => {
                                e.currentTarget.style.transform = 'translateY(0)';
                                e.currentTarget.style.boxShadow = '';
                              }}
                            >
                              <div className="card-body p-4">
                                <div className="d-flex align-items-center">
                                  <div
                                    className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center me-3"
                                    style={{ width: '48px', height: '48px' }}
                                  >
                                    <i className="bi bi-calendar-check text-primary" style={{ fontSize: '1.5rem' }}></i>
                                  </div>
                                  <div className="flex-grow-1">
                                    <h6 className="mb-1">Keep Same Doctor</h6>
                                    <p className="text-muted small mb-0">
                                      Reschedule with {appointment?.doctorName} at a different time
                                    </p>
                                  </div>
                                  <i className="bi bi-chevron-right text-muted"></i>
                                </div>
                              </div>
                            </button>
                          </div>
                          <div className="col-12">
                            <button
                              className="card border w-100 text-start p-0"
                              style={{
                                cursor: 'pointer',
                                transition: 'all 0.2s'
                              }}
                              onClick={handleChangeDoctor}
                              onMouseEnter={(e) => {
                                e.currentTarget.style.transform = 'translateY(-2px)';
                                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                              }}
                              onMouseLeave={(e) => {
                                e.currentTarget.style.transform = 'translateY(0)';
                                e.currentTarget.style.boxShadow = '';
                              }}
                            >
                              <div className="card-body p-4">
                                <div className="d-flex align-items-center">
                                  <div
                                    className="rounded-circle bg-info bg-opacity-10 d-flex align-items-center justify-content-center me-3"
                                    style={{ width: '48px', height: '48px' }}
                                  >
                                    <i className="bi bi-hospital text-info" style={{ fontSize: '1.5rem' }}></i>
                                  </div>
                                  <div className="flex-grow-1">
                                    <h6 className="mb-1">Change Clinic or Doctor</h6>
                                    <p className="text-muted small mb-0">
                                      Book with a different doctor or clinic
                                    </p>
                                  </div>
                                  <i className="bi bi-chevron-right text-muted"></i>
                                </div>
                              </div>
                            </button>
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Step 2: Select Clinic (Same UI as BookAppointmentModal) */}
                    {step === 2 && (
                      <div>
                        <h6 className="mb-4">Select a Clinic</h6>

                        {/* Search and Filter Section */}
                        <div className="row g-3 mb-4">
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
                                  setCurrentPage(1);
                                }}
                                style={{ boxShadow: 'none', outline: 'none' }}
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
                          <div className="col-md-4">
                            <select
                              className="form-select"
                              value={clinicTypeFilter}
                              onChange={(e) => {
                                setClinicTypeFilter(e.target.value);
                                setCurrentPage(1);
                              }}
                              style={{ cursor: 'pointer', boxShadow: 'none' }}
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
                            <div className="row g-3" style={{ minHeight: '300px' }}>
                              {(() => {
                                const filteredClinics = clinics
                                  .filter(clinic => clinicTypeFilter === 'all' || clinic.type === clinicTypeFilter)
                                  .filter(clinic =>
                                    searchQuery === '' ||
                                    clinic.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                                    clinic.address.toLowerCase().includes(searchQuery.toLowerCase())
                                  );

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
                                            cursor: 'pointer',
                                            transition: 'all 0.2s',
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

                                    {/* Pagination */}
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
                                                return (
                                                  <li key={pageNumber} className="page-item disabled">
                                                    <span className="page-link">...</span>
                                                  </li>
                                                );
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

                    {/* Step 3: Select Doctor */}
                    {step === 3 && selectedClinic && (
                      <div>
                        <h6 className="mb-3">Select a Doctor at {selectedClinic.name}</h6>
                        {loading ? (
                          <div className="text-center py-5">
                            <div className="spinner-border text-primary" role="status">
                              <span className="visually-hidden">Loading...</span>
                            </div>
                          </div>
                        ) : (
                          <div className="row g-3">
                            {doctors.length === 0 ? (
                              <div className="col-12 text-center py-4 text-muted">
                                <i className="bi bi-person-x" style={{ fontSize: '3rem' }}></i>
                                <p className="mt-2">No doctors available at this clinic</p>
                              </div>
                            ) : (
                              doctors.map((doctor) => (
                                <div key={doctor.doctorId} className="col-12">
                                  <div
                                    className="card border hover-shadow h-100"
                                    onClick={() => handleDoctorSelect(doctor)}
                                    style={{
                                      cursor: 'pointer',
                                      transition: 'all 0.2s',
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
                                      <div className="d-flex align-items-center">
                                        <div
                                          className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center me-3"
                                          style={{ width: '40px', height: '40px' }}
                                        >
                                          <i className="bi bi-person-fill text-primary"></i>
                                        </div>
                                        <div>
                                          <h6 className="mb-0 fw-semibold">{doctor.name}</h6>
                                          <small className="text-muted">
                                            {doctor.appointmentDurationInMinutes} min appointments
                                          </small>
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              ))
                            )}
                          </div>
                        )}
                      </div>
                    )}

                    {/* Step 4: Select Slot */}
                    {step === 4 && selectedClinic && selectedDoctor && (
                      <div>
                        <SelectSlot
                          selectedClinic={selectedClinic}
                          selectedDoctor={selectedDoctor}
                          selectedDate={selectedDate}
                          selectedTime={selectedTime}
                          setSelectedDate={setSelectedDate}
                          setSelectedTime={setSelectedTime}
                        />
                      </div>
                    )}
                  </>
                )}
              </div>
            )}
          </div>

          <div className="modal-footer border-0 pt-0">
            {!success && !isNotReschedulable && (
              <>
                {step > 1 && (
                  <button className="btn btn-light" onClick={handleBack} disabled={loading}>
                    <i className="bi bi-arrow-left me-1"></i> Back
                  </button>
                )}
                <button className="btn btn-light" onClick={onHide} disabled={loading}>
                  Cancel
                </button>
                {step === 4 && (
                  <button
                    className="btn btn-primary"
                    onClick={handleReschedule}
                    disabled={loading || !selectedDate || !selectedTime}
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status" />
                        Rescheduling...
                      </>
                    ) : (
                      'Confirm Reschedule'
                    )}
                  </button>
                )}
              </>
            )}
            {!success && isNotReschedulable && (
              <button className="btn btn-light" onClick={onHide}>
                Close
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
    </>
  );
};

export default RescheduleAppointmentModal;
