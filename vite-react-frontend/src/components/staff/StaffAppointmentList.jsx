import { useState, useEffect } from 'react';
import apiClient from '../../api/apiClient';

const StaffAppointmentList = ({ appointments, loading, filterType = 'all' }) => {
  const [sortOrder, setSortOrder] = useState('asc'); // asc = oldest first, desc = newest first
  const [checkInLoading, setCheckInLoading] = useState({}); // { [appointmentId]: boolean }
  const [checkInResult, setCheckInResult] = useState({}); // { [appointmentId]: { success: boolean, message: string, queueNumber?: number } }
  const [queueByAppointment, setQueueByAppointment] = useState({}); // { [appointmentId]: QueueTicketDto-like }
  const [fastTrackLoading, setFastTrackLoading] = useState({}); // { [appointmentId]: boolean }
  const [completedLoading, setCompletedLoading] = useState({}); // { [appointmentId]: boolean }
  const [noShowLoading, setNoShowLoading] = useState({}); // { [appointmentId]: boolean }

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

  // Group appointments by doctor for sectioned rendering
  const groupedByDoctor = sortedAppointments.reduce((acc, appt) => {
    const doctorId = appt?.doctorId || appt?.doctor?.doctorId || 'UNKNOWN_DOCTOR';
    const doctorName = appt?.doctor?.name || appt?.doctorName || 'Unknown Doctor';
    if (!acc[doctorId]) {
      acc[doctorId] = { name: doctorName, items: [] };
    }
    acc[doctorId].items.push(appt);
    return acc;
  }, {});

  // Fetch queue ticket for each appointment; ignore 404 (means no ticket yet)
  useEffect(() => {
    let isCancelled = false;
    const fetchQueues = async () => {
      const ids = appointments.map((a) => a.appointmentId).filter(Boolean);
      if (ids.length === 0) return;
      const toFetch = ids.filter((id) => typeof queueByAppointment[id] === 'undefined');
      if (toFetch.length === 0) return;
      try {
        await Promise.all(
          toFetch.map(async (id) => {
            const res = await apiClient.get(`/api/queue/appointment/${id}` , {
              validateStatus: (status) => (status >= 200 && status < 300) || status === 404,
            });
            if (res?.status === 404) {
              return; // not checked-in; do nothing
            }
            const ticket = res?.data;
            if (!isCancelled && ticket && typeof ticket?.queueNumber === 'number') {
              setQueueByAppointment((prev) => ({ ...prev, [id]: ticket }));
            }
          })
        );
      } catch {
        // suppress batch errors
      }
    };
    fetchQueues();
    return () => {
      isCancelled = true;
    };
  }, [appointments, queueByAppointment]);

  const handleCheckIn = async (appointmentId) => {
    // Prevent double-click: check if already loading
    if (checkInLoading[appointmentId]) {
      return;
    }

    try {
      setCheckInLoading((prev) => ({ ...prev, [appointmentId]: true }));
      setCheckInResult((prev) => ({ ...prev, [appointmentId]: undefined }));

      const res = await apiClient.post(`/api/queue/check-in/${appointmentId}`);
      const ticket = res?.data || {};
      const queueNumber = ticket?.queueNumber;
      setCheckInResult((prev) => ({
        ...prev,
        [appointmentId]: {
          success: true,
          message: 'Check-in successful',
          queueNumber,
        },
      }));
      if (typeof queueNumber === 'number') {
        setQueueByAppointment((prev) => ({ ...prev, [appointmentId]: ticket }));
      }
      // Refresh the page to reflect updated queue state across all cards
      window.location.reload();
    } catch (err) {
      // Backend sends { error, message, status } for BAD_REQUEST
      const backendMsg = err?.response?.data?.message || 'Check-in failed. Please try again.';
      setCheckInResult((prev) => ({
        ...prev,
        [appointmentId]: {
          success: false,
          message: backendMsg,
        },
      }));
      setCheckInLoading((prev) => ({ ...prev, [appointmentId]: false }));
    }
  };

  const handleFastTrack = async (appointmentId) => {
    // Prevent double-click: check if already loading
    if (fastTrackLoading[appointmentId] || checkInLoading[appointmentId]) {
      return;
    }

    // Prompt for reason first; if none, exit without check-in
    const reason = window.prompt('Enter fast-track reason (e.g., Emergency/Priority):', 'Emergency/Priority');
    if (reason === null) return; // user cancelled
    const trimmed = String(reason).trim();
    if (!trimmed) return;

    try {
      // Disable both Fast-track and Check-in while processing
      setFastTrackLoading((prev) => ({ ...prev, [appointmentId]: true }));
      setCheckInLoading((prev) => ({ ...prev, [appointmentId]: true }));

      // Ensure a ticket exists (check-in first if needed)
      let ticket = queueByAppointment[appointmentId];
      if (!ticket || !ticket.ticketId) {
        try {
          const checkInRes = await apiClient.post(`/api/queue/check-in/${appointmentId}`);
          ticket = checkInRes?.data;
          if (ticket) {
            setQueueByAppointment((prev) => ({ ...prev, [appointmentId]: ticket }));
            setCheckInResult((prev) => ({
              ...prev,
              [appointmentId]: {
                success: true,
                message: 'Check-in successful',
                queueNumber: ticket.queueNumber,
              },
            }));
          }
        } catch (err) {
          const backendMsg = err?.response?.data?.message || 'Fast-track failed: check-in required but failed.';
          setCheckInResult((prev) => ({
            ...prev,
            [appointmentId]: {
              success: false,
              message: backendMsg,
            },
          }));
          return;
        }
      }

      // Execute fast-track
      const res = await apiClient.put(`/api/queue/ticket/${ticket.ticketId}/fast-track`, { reason: trimmed });
      const updated = res?.data;
      if (updated) {
        setQueueByAppointment((prev) => ({ ...prev, [appointmentId]: updated }));
        setCheckInResult((prev) => ({
          ...prev,
          [appointmentId]: {
            success: true,
            message: 'Patient fast-tracked successfully',
            queueNumber: updated.queueNumber,
          },
        }));
        // Refresh the page to reflect updated queue numbers across the list
        window.location.reload();
      }
    } catch (err) {
      const backendMsg = err?.response?.data?.message || 'Fast-track failed. Please try again.';
      setCheckInResult((prev) => ({
        ...prev,
        [appointmentId]: {
          success: false,
          message: backendMsg,
        },
      }));
    } finally {
      setFastTrackLoading((prev) => ({ ...prev, [appointmentId]: false }));
      setCheckInLoading((prev) => ({ ...prev, [appointmentId]: false }));
    }
  };

  const handleComplete = async (appointmentId, doctorId) => {
    // Prevent double-click: check if already loading
    if (completedLoading[appointmentId]) {
      return;
    }

    if (!doctorId) {
      setCheckInResult((prev) => ({
        ...prev,
        [appointmentId]: {
          success: false,
          message: 'Complete failed: Missing doctor ID.',
        },
      }));
      return;
    }
    try {
      setCompletedLoading((prev) => ({ ...prev, [appointmentId]: true }));
      await apiClient.post(`/api/queue/call-next/${doctorId}`);
      window.location.reload();
    } catch (err) {
      const backendMsg = err?.response?.data?.message || 'Complete failed. Please try again.';
      setCheckInResult((prev) => ({
        ...prev,
        [appointmentId]: {
          success: false,
          message: backendMsg,
        },
      }));
      setCompletedLoading((prev) => ({ ...prev, [appointmentId]: false }));
    }
  };

  const handleNoShow = async (appointmentId) => {
    // Prevent double-click: check if already loading
    if (noShowLoading[appointmentId]) {
      return;
    }

    const ticket = queueByAppointment[appointmentId];
    if (!ticket || !ticket.ticketId) {
      return;
    }

    const confirmed = window.confirm('Mark this patient as no-show? This will update the queue positions.');
    if (!confirmed) {
      return;
    }

    try {
      setNoShowLoading((prev) => ({ ...prev, [appointmentId]: true }));
      await apiClient.put(`/api/queue/ticket/${ticket.ticketId}/no-show`);
      window.location.reload();
    } catch (err) {
      const backendMsg = err?.response?.data?.message || 'Marking no-show failed. Please try again.';
      setCheckInResult((prev) => ({
        ...prev,
        [appointmentId]: {
          success: false,
          message: backendMsg,
        },
      }));
      setNoShowLoading((prev) => ({ ...prev, [appointmentId]: false }));
    }
  };

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
        <div className="d-flex flex-column gap-4">
          {Object.entries(groupedByDoctor).map(([doctorKey, group]) => (
            <div key={doctorKey}>
              <div className="d-flex align-items-center justify-content-between mb-3">
                <h5 className="mb-0">
                  <i className="bi bi-person-badge me-2"></i>
                  {group.name}
                </h5>
                <span className="badge bg-light text-dark">{group.items.length} appointment{group.items.length !== 1 ? 's' : ''}</span>
              </div>
              <div className="row g-4">
                {group.items.map((appointment) => (
                  <div key={appointment.appointmentId} className="col-md-6 col-lg-4">
              <div className="card h-100 border shadow-sm">
                <div className="card-body p-4">
                  {/* Status Badge */}
                  <div className="d-flex justify-content-between align-items-start mb-3">
                    <div className="d-flex align-items-center gap-2">
                      <span className={`badge ${getStatusBadge(appointment.status)}`}>
                        {appointment.status}
                      </span>
                      {typeof queueByAppointment[appointment.appointmentId]?.queueNumber === 'number' && queueByAppointment[appointment.appointmentId]?.queueNumber !== 0 && (
                        <span className="badge bg-info text-dark">
                          Queue #{queueByAppointment[appointment.appointmentId].queueNumber}
                        </span>
                      )}
                      {queueByAppointment[appointment.appointmentId]?.status === 'FAST_TRACKED' && (
                        <span className="badge bg-warning text-dark">
                          Fast-tracked
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Patient Info */}
                  <div className="mb-3">
                    <h5 className="card-title mb-2 fw-bold">
                      <i className="bi bi-person-fill me-2 text-primary"></i>
                      {appointment?.patient?.name || appointment?.patientName || 'Unknown Patient'}
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
                        {appointment?.doctor?.name || appointment?.doctorName || 'Unknown'}
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
                    {(() => {
                      const ticket = queueByAppointment[appointment.appointmentId];
                      const reason = ticket?.fastTrackReason || ticket?.fast_track_reason;
                      if (!reason) return null;
                      return (
                        <div className="mt-2">
                          <small className="text-muted">Fast-track reason: {reason}</small>
                        </div>
                      );
                    })()}
                  </div>

                  {/* Actions */}
                  {['UPCOMING', 'ONGOING'].includes((appointment.status || '').toUpperCase()) && (
                    <div className="d-grid gap-2 mt-3">
                      {(appointment.status || '').toUpperCase() === 'UPCOMING' && (
                        <>
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() => handleCheckIn(appointment.appointmentId)}
                            disabled={!!checkInLoading[appointment.appointmentId] || !!checkInResult[appointment.appointmentId]?.success || !!fastTrackLoading[appointment.appointmentId] || !!queueByAppointment[appointment.appointmentId]?.ticketId}
                          >
                            {checkInLoading[appointment.appointmentId] ? (
                              <>
                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                Checking in...
                              </>
                            ) : (
                              <>
                                <i className="bi bi-person-walking me-1"></i>
                                Check-in
                              </>
                            )}
                          </button>

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
                        </>
                      )}

                      {(() => {
                        const qNum = queueByAppointment[appointment.appointmentId]?.queueNumber;
                        const allowFastTrack = typeof qNum === 'undefined' || qNum > 2;
                        if (!allowFastTrack) return null;
                        return (
                          <button
                            className="btn btn-warning btn-sm"
                            onClick={() => handleFastTrack(appointment.appointmentId)}
                            disabled={!!fastTrackLoading[appointment.appointmentId]}
                            title={!queueByAppointment[appointment.appointmentId]?.ticketId ? 'Available after check-in' : ''}
                          >
                            {fastTrackLoading[appointment.appointmentId] ? (
                              <>
                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                Fast-tracking...
                              </>
                            ) : (
                              <>
                                <i className="bi bi-lightning-charge-fill me-1"></i>
                                Fast-track
                              </>
                            )}
                          </button>
                        );
                      })()}

                      {(() => {
                        const statusUpper = (appointment.status || '').toUpperCase();
                        const qNum = queueByAppointment[appointment.appointmentId]?.queueNumber;
                        const isOngoingAndFirst = statusUpper === 'ONGOING' && qNum === 1;
                        if (!isOngoingAndFirst) return null;
                        return (
                          <button
                            className="btn btn-outline-danger btn-sm"
                            onClick={() => handleNoShow(appointment.appointmentId)}
                            disabled={!!noShowLoading[appointment.appointmentId]}
                          >
                            {noShowLoading[appointment.appointmentId] ? (
                              <>
                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                Marking...
                              </>
                            ) : (
                              <>
                                <i className="bi bi-x-octagon me-1"></i>
                                Mark as No-Show
                              </>
                            )}
                          </button>
                        );
                      })()}

                      {(() => {
                        const statusUpper = (appointment.status || '').toUpperCase();
                        const qNum = queueByAppointment[appointment.appointmentId]?.queueNumber;
                        const isOngoingAndFirst = statusUpper === 'ONGOING' && qNum === 1;
                        if (!isOngoingAndFirst) return null;
                        const doctorId = appointment?.doctorId || appointment?.doctor?.doctorId;
                        return (
                          <button
                            className="btn btn-success btn-sm"
                            onClick={() => handleComplete(appointment.appointmentId, doctorId)}
                            disabled={!!completedLoading[appointment.appointmentId]}
                          >
                            {completedLoading[appointment.appointmentId] ? (
                              <>
                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                Completing...
                              </>
                            ) : (
                              <>
                                <i className="bi bi-check2-circle me-1"></i>
                                Mark as Completed
                              </>
                            )}
                          </button>
                        );
                      })()}

                      {checkInResult[appointment.appointmentId] && (
                        <div
                          className={`alert ${checkInResult[appointment.appointmentId].success ? 'alert-success' : 'alert-warning'} py-2 mb-0`}
                          role="alert"
                        >
                          <div className="d-flex align-items-center">
                            <i className={`bi ${checkInResult[appointment.appointmentId].success ? 'bi-check-circle-fill text-success' : 'bi-exclamation-triangle-fill text-warning'} me-2`}></i>
                            <div>
                              <div className="small fw-semibold">{checkInResult[appointment.appointmentId].message}</div>
                              {checkInResult[appointment.appointmentId].success && typeof checkInResult[appointment.appointmentId].queueNumber === 'number' && (
                                <div className="small text-muted">
                                  Queue Number: <span className="fw-bold">#{checkInResult[appointment.appointmentId].queueNumber}</span>
                                </div>
                              )}
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
                      </div>
                    </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default StaffAppointmentList;
