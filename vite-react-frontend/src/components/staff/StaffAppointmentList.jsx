import { useState } from 'react';

const StaffAppointmentList = ({ appointments, loading, filterType = 'all' }) => {
  const [sortOrder, setSortOrder] = useState('asc'); // asc = oldest first, desc = newest first

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

  const formatTime = (dateTimeString) => {
    const date = new Date(dateTimeString);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const sortedAppointments = [...appointments].sort((a, b) => {
    const dateA = new Date(a.startDatetime);
    const dateB = new Date(b.startDatetime);
    return sortOrder === 'asc' ? dateA - dateB : dateB - dateA;
  });

  if (loading) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <p className="mt-3 text-muted">Loading appointments...</p>
      </div>
    );
  }

  return (
    <div>
      {/* Sort Controls */}
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div className="text-muted">
          <i className="bi bi-calendar-check me-2"></i>
          {appointments.length} appointment{appointments.length !== 1 ? 's' : ''} found
        </div>
        <div className="btn-group btn-group-sm">
          <button
            className={`btn ${sortOrder === 'asc' ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => setSortOrder('asc')}
          >
            <i className="bi bi-sort-up"></i> Oldest First
          </button>
          <button
            className={`btn ${sortOrder === 'desc' ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => setSortOrder('desc')}
          >
            <i className="bi bi-sort-down"></i> Newest First
          </button>
        </div>
      </div>

      {/* Appointments List */}
      {sortedAppointments.length === 0 ? (
        <div className="empty-state text-center py-5">
          <i className="bi bi-calendar-x" style={{ fontSize: '3rem', color: '#dee2e6' }}></i>
          <p className="mt-3 text-muted">
            {filterType === 'all'
              ? 'No appointments found for this clinic.'
              : `No ${filterType} appointments found.`}
          </p>
        </div>
      ) : (
        <div className="row g-4">
          {sortedAppointments.map((appointment) => (
            <div key={appointment.appointmentId} className="col-md-6 col-lg-4">
              <div className="card h-100 border shadow-sm">
                <div className="card-body p-4">
                  {/* Status Badge */}
                  <div className="d-flex justify-content-between align-items-start mb-3">
                    <span className={`badge ${getStatusBadge(appointment.status)}`}>
                      {appointment.status}
                    </span>
                    <small className="text-muted">
                      #{appointment.appointmentId.substring(0, 8)}
                    </small>
                  </div>

                  {/* Patient Info */}
                  <div className="mb-3">
                    <h5 className="card-title mb-2 fw-bold">
                      <i className="bi bi-person-fill me-2 text-primary"></i>
                      {appointment?.patient?.name || 'Unknown Patient'}
                    </h5>
                    <small className="text-muted">
                      Patient ID: {appointment?.patientId?.substring(0, 8)}
                    </small>
                  </div>

                  {/* Doctor Info */}
                  <div className="mb-3">
                    <div className="d-flex align-items-center mb-2 text-muted">
                      <i className="bi bi-person-badge me-2"></i>
                      <span className="small">
                        Dr. {appointment?.doctor?.name || appointment?.doctorName || 'Unknown'}
                      </span>
                    </div>
                  </div>

                  {/* Date & Time */}
                  <div className="mb-3">
                    <div className="d-flex align-items-center mb-2">
                      <i className="bi bi-calendar-event me-2 text-primary"></i>
                      <span className="small">{formatDateTime(appointment.startDatetime)}</span>
                    </div>

                    <div className="d-flex align-items-center text-muted">
                      <i className="bi bi-clock me-2"></i>
                      <span className="small">
                        {appointment.endDatetime
                          ? `${formatTime(appointment.startDatetime)} - ${formatTime(appointment.endDatetime)}`
                          : 'Duration not set'}
                      </span>
                    </div>
                  </div>

                  {/* Action Buttons - Will be implemented in later PRs */}
                  {(appointment.status || '').toUpperCase() === 'UPCOMING' && (
                    <div className="d-grid gap-2 mt-3">
                      <button
                        className="btn btn-outline-secondary btn-sm"
                        disabled
                        title="Cancel feature coming in PR #3"
                      >
                        <i className="bi bi-x-circle me-1"></i>
                        Cancel (Coming Soon)
                      </button>
                      <button
                        className="btn btn-outline-secondary btn-sm"
                        disabled
                        title="Reschedule feature coming in PR #4"
                      >
                        <i className="bi bi-arrow-repeat me-1"></i>
                        Reschedule (Coming Soon)
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

export default StaffAppointmentList;
