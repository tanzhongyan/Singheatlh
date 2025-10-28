import React, { useState } from 'react';
import apiClient from '../../api/apiClient';

const RescheduleAppointmentModal = ({ show, onHide, appointment, onSuccess }) => {
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  if (!show) return null;

  const canReschedule = () => {
    // Check if current appointment is at least 24 hours away
    if (!appointment?.startDatetime) return false;
    const appointmentTime = new Date(appointment.startDatetime);
    const now = new Date();
    const hoursDiff = (appointmentTime.getTime() - now.getTime()) / (1000 * 60 * 60);
    return hoursDiff >= 24;
  };

  const isNotReschedulable = !canReschedule();

  const getMinDateISO = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  };

  const handleSubmit = async () => {
    setError(null);
    
    // Validate that we're rescheduling at least 24h before the appointment
    if (!canReschedule()) {
      setError('You can only reschedule appointments that are at least 24 hours away.');
      return;
    }

    if (!date || !time) {
      setError('Please select both date and time.');
      return;
    }

    try {
      setLoading(true);
      const newDateTime = `${date}T${time}:00`;
      await apiClient.put(`/api/appointments/${appointment.appointmentId}/reschedule`, null, { params: { newDateTime } });
      setSuccess(true);
      setTimeout(() => {
        setLoading(false);
        setSuccess(false);
        onSuccess && onSuccess();
        onHide && onHide();
      }, 1200);
    } catch (err) {
      console.error('Reschedule error:', err);
      const message = err.response?.data?.message || err.response?.data || err.message || 'Failed to reschedule. Please try again.';
      setError(typeof message === 'string' ? message : JSON.stringify(message));
      setLoading(false);
    }
  };

  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.4)', zIndex: 1060 }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 shadow-lg">
          <div className="modal-header border-0">
            <h5 className="modal-title">Reschedule Appointment</h5>
            <button type="button" className="btn-close" onClick={onHide}></button>
          </div>
          <div className="modal-body">
            {success ? (
              <div className="alert alert-light border border-success d-flex align-items-center">
                <i className="bi bi-check-circle-fill text-success me-3" style={{ fontSize: '1.5rem' }}></i>
                <div>
                  <div className="fw-bold">Appointment rescheduled</div>
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
                    <p className="mb-3">Select a new date and time for your appointment.</p>
                    {error && <div className="alert alert-danger">{error}</div>}

                    <div className="mb-3">
                      <label className="form-label">Date</label>
                      <input type="date" className="form-control" value={date} onChange={(e) => setDate(e.target.value)} min={getMinDateISO()} />
                    </div>

                    <div className="mb-3">
                      <label className="form-label">Time</label>
                      <input type="time" className="form-control" value={time} onChange={(e) => setTime(e.target.value)} />
                    </div>
                  </>
                )}
              </div>
            )}
          </div>
          <div className="modal-footer border-0">
            {!success && !isNotReschedulable && (
              <>
                <button className="btn btn-light" onClick={onHide} disabled={loading}>Cancel</button>
                <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
                  {loading ? (
                    <><span className="spinner-border spinner-border-sm me-2" role="status" />Rescheduling...</>
                  ) : (
                    'Reschedule'
                  )}
                </button>
              </>
            )}
            {!success && isNotReschedulable && (
              <button className="btn btn-light" onClick={onHide}>Close</button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RescheduleAppointmentModal;
