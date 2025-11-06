import { useState } from 'react';

const AppointmentList = ({ appointments, onCancel, onRequestReschedule, onViewSummary }) => {
  const [filter, setFilter] = useState('all'); // all, upcoming, completed, cancelled

  const getStatusBadge = (status) => {
    const badges = {
      UPCOMING: 'bg-primary',
      ONGOING: 'bg-success',
      COMPLETED: 'bg-secondary',
      CANCELLED: 'bg-danger',
      MISSED: 'bg-warning text-dark',
    };
    const key = (status || '').toUpperCase();
    return badges[key] || 'bg-secondary';
  };

  const formatDateTime = (dateTimeString) => {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const filteredAppointments = appointments.filter((apt) => {
    if (filter === 'all') return true;
    return (apt.status || '').toLowerCase() === filter.toLowerCase();
  });

  const sortedAppointments = [...filteredAppointments].sort((a, b) => {
    return new Date(b.startDatetime) - new Date(a.startDatetime);
  });

  return (
    <div>
      {/* Filter Tabs */}
      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button
            className={`nav-link ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            All <span className="badge bg-secondary ms-1">{appointments.length}</span>
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${filter === 'upcoming' ? 'active' : ''}`}
            onClick={() => setFilter('upcoming')}
          >
            Upcoming <span className="badge bg-primary ms-1">{appointments.filter((a) => (a.status || '').toLowerCase() === 'upcoming').length}</span>
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${filter === 'completed' ? 'active' : ''}`}
            onClick={() => setFilter('completed')}
          >
            Completed <span className="badge bg-secondary ms-1">{appointments.filter((a) => (a.status || '').toLowerCase() === 'completed').length}</span>
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${filter === 'cancelled' ? 'active' : ''}`}
            onClick={() => setFilter('cancelled')}
          >
            Cancelled <span className="badge bg-danger ms-1">{appointments.filter((a) => (a.status || '').toLowerCase() === 'cancelled').length}</span>
          </button>
        </li>
      </ul>

      {/* Appointments List */}
      {sortedAppointments.length === 0 ? (
        <div className="empty-state">
          <i className="bi bi-calendar-x"></i>
          <p className="mt-3">
            {filter === 'all'
              ? 'No appointments found. Book your first appointment!'
              : `No ${filter} appointments.`}
          </p>
        </div>
      ) : (
        <div className="row g-4">
          {sortedAppointments.map((appointment) => (
            <div key={appointment.appointmentId} className="col-md-6 col-lg-4">
              <div className="card h-100 border">
                <div className="card-body p-4">
                  <div className="d-flex justify-content-between align-items-start mb-3">
                    <h5 className="card-title mb-0 fw-bold">
                      {(() => {
                        const doctorName = appointment?.doctor?.name || appointment?.doctorName || appointment?.doctorId || 'Unknown';
                        return `${doctorName}`;
                      })()}
                    </h5>
                    <span className={`badge ${getStatusBadge(appointment.status)}`}>
                      {appointment.status}
                    </span>
                  </div>

                  <div className="mb-3">
                    <div className="d-flex align-items-center mb-2 text-muted">
                      <i className="bi bi-hospital me-2"></i>
                      <span className="small">{appointment?.clinic?.name || appointment.clinicName || 'Unknown Clinic'}</span>
                    </div>

                    <div className="d-flex align-items-center mb-2">
                      <i className="bi bi-calendar-event me-2 text-primary"></i>
                      <span className="small">{formatDateTime(appointment.startDatetime)}</span>
                    </div>

                    <div className="d-flex align-items-center text-muted">
                      <i className="bi bi-clock me-2"></i>
                      <span className="small">
                        {appointment.endDatetime
                          ? new Date(appointment.endDatetime).toLocaleTimeString('en-US', {
                              hour: '2-digit',
                              minute: '2-digit',
                            })
                          : 'Duration not set'}
                      </span>
                    </div>
                  </div>

                  {(appointment.status || '').toUpperCase() === 'UPCOMING' && (() => {
                    // Check if appointment is at least 24 hours away
                    const appointmentTime = new Date(appointment.startDatetime);
                    const now = new Date();
                    const hoursDiff = (appointmentTime.getTime() - now.getTime()) / (1000 * 60 * 60);
                    const canReschedule = hoursDiff >= 24;

                    return (
                      <div className="d-grid gap-2">
                        <button
                          className="btn btn-outline-danger btn-sm"
                          onClick={() => onCancel(appointment.appointmentId)}
                        >
                          <i className="bi bi-x-circle me-1"></i>
                          Cancel Appointment
                        </button>
                        {canReschedule && (
                          <button
                            className="btn btn-outline-primary btn-sm"
                            onClick={() => onRequestReschedule && onRequestReschedule(appointment)}
                          >
                            <i className="bi bi-arrow-repeat me-1"></i>
                            Reschedule
                          </button>
                        )}
                      </div>
                    );
                  })()}

                  {/* View Summary button for COMPLETED appointments */}
                  {(appointment.status || '').toUpperCase() === 'COMPLETED' && (
                    <div className="d-grid">
                      <button
                        className="btn btn-outline-success btn-sm"
                        onClick={() => onViewSummary && onViewSummary(appointment)}
                        style={{
                          borderColor: '#10b981',
                          color: '#10b981',
                          fontWeight: '500',
                          transition: 'all 0.2s ease'
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.backgroundColor = '#10b981';
                          e.currentTarget.style.color = 'white';
                          e.currentTarget.style.transform = 'translateY(-1px)';
                          e.currentTarget.style.boxShadow = '0 4px 8px rgba(16, 185, 129, 0.2)';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.backgroundColor = 'transparent';
                          e.currentTarget.style.color = '#10b981';
                          e.currentTarget.style.transform = 'translateY(0)';
                          e.currentTarget.style.boxShadow = 'none';
                        }}
                        aria-label={`View medical summary for appointment with Dr. ${appointment?.doctorName}`}
                      >
                        <i className="bi bi-file-medical me-1"></i>
                        View Medical Summary
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AppointmentList;
