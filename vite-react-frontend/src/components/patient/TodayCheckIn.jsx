import React, { useEffect, useState } from 'react';
import apiClient from '../../api/apiClient';
import { useAuth } from '../../contexts/AuthContext';

const TodayCheckIn = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [appointment, setAppointment] = useState(null);
  const [checkInLoading, setCheckInLoading] = useState(false);
  // message and error state retained for backward-compat but toasts used for UI
  const [ticket, setTicket] = useState(null);
  const [toasts, setToasts] = useState([]);

  const TOAST_LIFETIME = 4000; // ms

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

  useEffect(() => {
    const fetchTodayAppointment = async () => {
      try {
        setLoading(true);
        // clear any existing toasts
        setToasts([]);

        if (!user?.id) return;

        const resp = await apiClient.get(`/api/appointments/patient/${user.id}/upcoming`);
        const upcoming = resp.data || [];

        const todayDate = new Date();
        todayDate.setHours(0, 0, 0, 0); // normalize to midnight
        
        const todays = upcoming.find((a) => {
          if (!a?.startDatetime) return false;
          const apptDate = new Date(a.startDatetime);
          apptDate.setHours(0, 0, 0, 0); // normalize to midnight
          return apptDate.getTime() === todayDate.getTime();
        });

        setAppointment(todays || null);
      } catch (err) {
        console.error('Failed to fetch today\'s appointment', err);
        addToast('error', 'Failed to load today\'s appointment');
      } finally {
        setLoading(false);
      }
    };

    fetchTodayAppointment();
  }, [user]);

  const handleCheckIn = async () => {
    if (!appointment?.appointmentId) return;
    try {
      setCheckInLoading(true);
      // clear toasts for fresh messages
      setToasts([]);

      const resp = await apiClient.post(`/api/queue/check-in/${appointment.appointmentId}`);
      setTicket(resp.data);
      // show success toast with ticket and queue numbers
      const ticketNum = resp.data.ticketNumberForDay ? `Ticket #${resp.data.ticketNumberForDay}` : '';
      const queueNum = resp.data.queueNumber ? `Queue #${resp.data.queueNumber}` : '';
      const message = ticketNum && queueNum ? `${ticketNum} • ${queueNum}` : (ticketNum || queueNum || 'Checked in successfully');
      addToast('success', `✓ Checked in! ${message}`);
    } catch (err) {
      console.error('Check-in failed', err);
      const msg = err.response?.data?.message || err.response?.data || err.message || 'Check-in failed';
      const text = typeof msg === 'string' ? msg : JSON.stringify(msg);
      addToast('error', text);
    } finally {
      setCheckInLoading(false);
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

  if (loading) {
    return (
      <>
        {toastContainer}
        <div className="card border-0 shadow-sm p-4">
          <div className="d-flex align-items-center">
            <div className="spinner-border text-primary me-3" role="status"></div>
            <div>Checking for today's appointment...</div>
          </div>
        </div>
      </>
    );
  }

  if (!appointment) {
    return (
      <>
        {toastContainer}
        <div className="card border-0 shadow-sm p-4">
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h6 className="mb-1">No appointments for today</h6>
              <p className="small text-muted mb-0">You have no upcoming appointments scheduled for today.</p>
            </div>
            <div>
              <a href="/appointments" className="btn btn-outline-primary">View Appointments</a>
            </div>
          </div>
        </div>
      </>
    );
  }

  // Determine if check-in is allowed based on appointment status and times
  const appointmentTime = new Date(appointment.startDatetime);
  const now = new Date();
  const appointmentDateStr = appointmentTime.toLocaleString();
  const isPast = appointmentTime.getTime() < now.getTime();
  const canCheckIn = (appointment.status || '').toUpperCase() === 'UPCOMING' && !isPast;

  return (
    <>
      {toastContainer}
      <div className="card border-0 shadow-sm p-4">
        {/* Info Banner */}
        <div className="alert alert-info border-0 mb-4 d-flex align-items-start" style={{ backgroundColor: '#e8f4f8', borderRadius: '8px' }}>
          <i className="bi bi-info-circle me-2 mt-1" style={{ fontSize: '1.2rem', color: '#0c5460' }}></i>
          <div className="small" style={{ color: '#0c5460' }}>
            <strong>How Check-In Works:</strong> 
            <ul className="mb-0 mt-1 ps-3">
              <li>Check in when you arrive at the clinic</li>
              <li>You'll receive a queue number to track your turn</li>
              <li>Please arrive at least 10 minutes before your appointment</li>
            </ul>
          </div>
        </div>

        {/* Appointment Info Section */}
        <div className="row mb-4">
          <div className="col-12">
            <p className="small text-muted mb-2 text-uppercase" style={{ letterSpacing: '0.05em', fontWeight: '600' }}>
              Today's Appointment
            </p>
            <div className="d-flex align-items-center mb-2">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" className="text-primary me-2" viewBox="0 0 16 16">
                <path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6zm2-3a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm4 8c0 1-1 1-1 1H3s-1 0-1-1 1-4 6-4 6 3 6 4zm-1-.004c-.001-.246-.154-.986-.832-1.664C11.516 10.68 10.289 10 8 10c-2.29 0-3.516.68-4.168 1.332-.678.678-.83 1.418-.832 1.664h10z"/>
              </svg>
              <h5 className="mb-0">{appointment?.doctor?.name || appointment.doctorName || 'Unknown'}</h5>
            </div>
            <div className="d-flex align-items-center mb-1">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="text-muted me-2" viewBox="0 0 16 16">
                <path d="M8.707 1.5a1 1 0 0 0-1.414 0L.646 8.146a.5.5 0 0 0 .708.708L8 2.207l6.646 6.647a.5.5 0 0 0 .708-.708L13 5.793V2.5a.5.5 0 0 0-.5-.5h-1a.5.5 0 0 0-.5.5v1.293L8.707 1.5Z"/>
                <path d="m8 3.293 6 6V13.5a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 2 13.5V9.293l6-6Z"/>
              </svg>
              <span className="text-muted">{appointment?.clinic?.name || appointment.clinicName || 'Unknown Clinic'}</span>
            </div>
            <div className="d-flex align-items-center">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="text-muted me-2" viewBox="0 0 16 16">
                <path d="M8 3.5a.5.5 0 0 0-1 0V9a.5.5 0 0 0 .252.434l3.5 2a.5.5 0 0 0 .496-.868L8 8.71V3.5z"/>
                <path d="M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16zm7-8A7 7 0 1 1 1 8a7 7 0 0 1 14 0z"/>
              </svg>
              <span className="text-muted small">{appointmentDateStr}</span>
            </div>
          </div>
        </div>

        {/* Ticket Display (if checked in) */}
        {ticket && (
          <div className="alert alert-success border-0 mb-4" style={{ backgroundColor: '#d1e7dd' }}>
            <div className="d-flex align-items-center justify-content-center flex-wrap">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" className="me-2" viewBox="0 0 16 16">
                <path d="M5.5 7a.5.5 0 0 0 0 1h5a.5.5 0 0 0 0-1h-5zM5 9.5a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5zm0 2a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5z"/>
                <path d="M3 0h10a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2v-1h1v1a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H3a1 1 0 0 0-1 1v1H1V2a2 2 0 0 1 2-2z"/>
                <path d="M1 5v-.5a.5.5 0 0 1 1 0V5h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1zm0 3v-.5a.5.5 0 0 1 1 0V8h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1zm0 3v-.5a.5.5 0 0 1 1 0v.5h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1z"/>
              </svg>
              <div className="text-center text-md-start">
                <strong className="d-block mb-1">✓ Checked In</strong>
                <div className="d-flex gap-3 flex-wrap justify-content-center justify-content-md-start">
                  {typeof ticket.ticketNumberForDay === 'number' && (
                    <span className="badge bg-primary" style={{ fontSize: '0.95rem', padding: '0.4rem 0.8rem' }}>
                      <i className="bi bi-ticket-perforated me-1"></i>
                      Ticket #{ticket.ticketNumberForDay}
                    </span>
                  )}
                  {typeof ticket.queueNumber === 'number' && (
                    <span className="badge bg-info text-dark" style={{ fontSize: '0.95rem', padding: '0.4rem 0.8rem' }}>
                      <i className="bi bi-list-ol me-1"></i>
                      Queue #{ticket.queueNumber}
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Check-in Button Section */}
        <div className="text-center">
          <button
            className="btn btn-primary btn-lg px-5 py-3"
            onClick={handleCheckIn}
            disabled={!canCheckIn || checkInLoading || ticket}
            style={{ 
              borderRadius: '0.5rem',
              fontSize: '1.1rem',
              fontWeight: '600',
              boxShadow: canCheckIn && !ticket ? '0 4px 12px rgba(13, 110, 253, 0.3)' : 'none',
              transition: 'all 0.3s ease'
            }}
          >
            {checkInLoading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status" />
                Checking in...
              </>
            ) : ticket ? (
              <>
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" className="me-2" viewBox="0 0 16 16">
                  <path d="M10.97 4.97a.75.75 0 0 1 1.07 1.05l-3.99 4.99a.75.75 0 0 1-1.08.02L4.324 8.384a.75.75 0 1 1 1.06-1.06l2.094 2.093 3.473-4.425a.267.267 0 0 1 .02-.022z"/>
                </svg>
                Already Checked In
              </>
            ) : (
              <>
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" className="me-2" viewBox="0 0 16 16">
                  <path fillRule="evenodd" d="M15 2a1 1 0 0 0-1-1H2a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V2zM0 2a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V2zm5.854 8.803a.5.5 0 1 1-.708-.707L9.243 6H6.475a.5.5 0 1 1 0-1h3.975a.5.5 0 0 1 .5.5v3.975a.5.5 0 1 1-1 0V6.707l-4.096 4.096z"/>
                </svg>
                Check In Now
              </>
            )}
          </button>
          {!canCheckIn && !ticket && (
            <p className="small text-muted mt-3 mb-0">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="currentColor" className="me-1" viewBox="0 0 16 16">
                <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                <path d="m8.93 6.588-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275 0-.375-.193-.304-.533L8.93 6.588zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
              </svg>
              Check-in is only available for upcoming appointments
            </p>
          )}
        </div>
      </div>
    </>
  );
};

export default TodayCheckIn;
