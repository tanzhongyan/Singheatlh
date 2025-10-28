import React, { useState } from 'react';
import apiClient from '../../api/apiClient';

const CancelAppointmentModal = ({ show, onHide, appointmentId, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  if (!show) return null;

  const handleConfirm = async () => {
    try {
      setLoading(true);
      setError(null);
      await apiClient.put(`/api/appointments/${appointmentId}/cancel`);
      setSuccess(true);
      // small delay to show success
      setTimeout(() => {
        setLoading(false);
        setSuccess(false);
        onSuccess && onSuccess();
        onHide && onHide();
      }, 1200);
    } catch (err) {
      console.error('Error cancelling appointment:', err);
      const serverMessage = err.response?.data?.message || err.response?.data || err.message;
      setError(typeof serverMessage === 'string' ? serverMessage : JSON.stringify(serverMessage));
      setLoading(false);
    }
  };

  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.4)', zIndex: 1060 }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 shadow-lg">
          <div className="modal-header border-0">
            <h5 className="modal-title">Cancel Appointment</h5>
            <button type="button" className="btn-close" onClick={onHide}></button>
          </div>
          <div className="modal-body">
            {success ? (
              <div className="alert alert-light border border-success d-flex align-items-center">
                <i className="bi bi-check-circle-fill text-success me-3" style={{ fontSize: '1.5rem' }}></i>
                <div>
                  <div className="fw-bold">Appointment cancelled</div>
                  <div className="small text-muted">Your appointment has been successfully cancelled.</div>
                </div>
              </div>
            ) : (
              <div>
                <p className="mb-3">Are you sure you want to cancel this appointment?</p>
                {error && <div className="alert alert-danger">{error}</div>}
              </div>
            )}
          </div>
          <div className="modal-footer border-0">
            {!success && (
              <>
                <button className="btn btn-light" onClick={onHide} disabled={loading}>Close</button>
                <button className="btn btn-danger" onClick={handleConfirm} disabled={loading}>
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" />
                      Cancelling...
                    </>
                  ) : (
                    'Confirm Cancel'
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

export default CancelAppointmentModal;
