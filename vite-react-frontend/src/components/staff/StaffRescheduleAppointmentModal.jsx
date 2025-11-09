import React, { useState } from 'react';
import apiClient from '../../api/apiClient';

const StaffRescheduleAppointmentModal = ({ show, onHide, appointment, onSuccess }) => {
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedTime, setSelectedTime] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  if (!show) return null;

  const handleReschedule = async () => {
    if (!selectedDate || !selectedTime) {
      setError('Please select both date and time');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      // Combine date and time into ISO string
      const newDateTime = `${selectedDate}T${selectedTime}:00`;

      await apiClient.put(`/api/appointments/${appointment.appointmentId}/reschedule-by-staff`, null, {
        params: { newDateTime }
      });

      setSuccess(true);
      // Small delay to show success message
      setTimeout(() => {
        setLoading(false);
        setSuccess(false);
        setSelectedDate('');
        setSelectedTime('');
        onSuccess && onSuccess();
        onHide && onHide();
      }, 1200);
    } catch (err) {
      console.error('Error rescheduling appointment:', err);
      const serverMessage = err.response?.data?.message || err.response?.data || err.message;
      setError(typeof serverMessage === 'string' ? serverMessage : JSON.stringify(serverMessage));
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setSelectedDate('');
      setSelectedTime('');
      setError(null);
      setSuccess(false);
      onHide && onHide();
    }
  };

  // Get tomorrow's date as minimum (for input)
  const today = new Date().toISOString().split('T')[0];

  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.4)', zIndex: 1060 }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 shadow-lg">
          <div className="modal-header border-0">
            <h5 className="modal-title">Reschedule Appointment (Staff)</h5>
            <button type="button" className="btn-close" onClick={handleClose} disabled={loading}></button>
          </div>
          <div className="modal-body">
            {success ? (
              <div className="alert alert-light border border-success d-flex align-items-center">
                <i className="bi bi-check-circle-fill text-success me-3" style={{ fontSize: '1.5rem' }}></i>
                <div>
                  <div className="fw-bold">Appointment rescheduled</div>
                  <div className="small text-muted">The appointment has been successfully rescheduled.</div>
                </div>
              </div>
            ) : (
              <div>
                <div className="alert alert-info mb-3">
                  <i className="bi bi-info-circle me-2"></i>
                  <strong>Staff Note:</strong> You can reschedule to any future time, including same-day appointments.
                </div>

                <div className="mb-3">
                  <p className="mb-2"><strong>Current Appointment:</strong></p>
                  <ul className="small text-muted mb-3">
                    <li>Patient: {appointment?.patient?.name || appointment?.patientName || 'Unknown'}</li>
                    <li>Doctor: {appointment?.doctor?.name || appointment?.doctorName || 'Unknown'}</li>
                    <li>Current Time: {new Date(appointment?.startDatetime).toLocaleString()}</li>
                  </ul>
                </div>

                <div className="mb-3">
                  <label htmlFor="rescheduleDate" className="form-label">
                    New Date <span className="text-danger">*</span>
                  </label>
                  <input
                    type="date"
                    id="rescheduleDate"
                    className="form-control"
                    value={selectedDate}
                    min={today}
                    onChange={(e) => setSelectedDate(e.target.value)}
                    disabled={loading}
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="rescheduleTime" className="form-label">
                    New Time <span className="text-danger">*</span>
                  </label>
                  <input
                    type="time"
                    id="rescheduleTime"
                    className="form-control"
                    value={selectedTime}
                    onChange={(e) => setSelectedTime(e.target.value)}
                    disabled={loading}
                  />
                  <small className="text-muted">
                    The appointment duration will remain the same.
                  </small>
                </div>

                {error && (
                  <div className="alert alert-danger">
                    <i className="bi bi-exclamation-triangle me-2"></i>
                    {error}
                  </div>
                )}
              </div>
            )}
          </div>
          <div className="modal-footer border-0">
            {!success && (
              <>
                <button className="btn btn-light" onClick={handleClose} disabled={loading}>
                  Cancel
                </button>
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
                    <>
                      <i className="bi bi-arrow-repeat me-1"></i>
                      Confirm Reschedule
                    </>
                  )}
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default StaffRescheduleAppointmentModal;
