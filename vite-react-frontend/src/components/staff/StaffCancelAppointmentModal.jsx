import React, { useState } from 'react';
import apiClient from '../../api/apiClient';

const StaffCancelAppointmentModal = ({ show, onHide, appointmentId, staffId, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [reason, setReason] = useState('');

  if (!show) return null;

  const handleConfirm = async () => {
    // Validate reason
    if (!reason.trim()) {
      setError('Cancellation reason is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await apiClient.put(`/api/appointments/${appointmentId}/cancel-by-staff`, {
        staffId: staffId,
        reason: reason.trim()
      });
      setSuccess(true);
      // Small delay to show success message
      setTimeout(() => {
        setLoading(false);
        setSuccess(false);
        setReason('');
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

  const handleClose = () => {
    if (!loading) {
      setReason('');
      setError(null);
      setSuccess(false);
      onHide && onHide();
    }
  };

  return (
    <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.4)', zIndex: 1060 }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 shadow-lg">
          <div className="modal-header border-0">
            <h5 className="modal-title">Cancel Appointment (Staff)</h5>
            <button type="button" className="btn-close" onClick={handleClose} disabled={loading}></button>
          </div>
          <div className="modal-body">
            {success ? (
              <div className="alert alert-light border border-success d-flex align-items-center">
                <i className="bi bi-check-circle-fill text-success me-3" style={{ fontSize: '1.5rem' }}></i>
                <div>
                  <div className="fw-bold">Appointment cancelled</div>
                  <div className="small text-muted">The appointment has been successfully cancelled.</div>
                </div>
              </div>
            ) : (
              <div>
                <p className="mb-3">
                  <strong>Note:</strong> As staff, you can cancel this appointment at any time on behalf of the patient.
                  Please provide a reason for the cancellation.
                </p>
                <div className="mb-3">
                  <label htmlFor="cancellationReason" className="form-label">
                    Cancellation Reason <span className="text-danger">*</span>
                  </label>
                  <textarea
                    id="cancellationReason"
                    className="form-control"
                    rows="4"
                    placeholder="e.g., Patient requested cancellation, Emergency situation, etc."
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    disabled={loading}
                  ></textarea>
                </div>
                {error && <div className="alert alert-danger">{error}</div>}
              </div>
            )}
          </div>
          <div className="modal-footer border-0">
            {!success && (
              <>
                <button className="btn btn-light" onClick={handleClose} disabled={loading}>
                  Close
                </button>
                <button className="btn btn-danger" onClick={handleConfirm} disabled={loading || !reason.trim()}>
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" />
                      Cancelling...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-x-circle me-1"></i>
                      Confirm Cancel
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

export default StaffCancelAppointmentModal;
