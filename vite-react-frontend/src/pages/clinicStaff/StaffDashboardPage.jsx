import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import apiClient from '../../api/apiClient';
import StaffAppointmentList from '../../components/staff/StaffAppointmentList';
import WalkInAppointmentModal from '../../components/staff/WalkInAppointmentModal';

const StaffDashboardPage = () => {
  const { userProfile } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeFilter, setActiveFilter] = useState('all'); // all, today, upcoming, status, dateRange
  const [selectedStatus, setSelectedStatus] = useState('Upcoming');
  const [dateRange, setDateRange] = useState({
    startDate: '',
    endDate: '',
  });
  const [showWalkInModal, setShowWalkInModal] = useState(false);

  const clinicId = userProfile?.clinicId;

  // Fetch appointments based on active filter
  const fetchAppointments = useCallback(async () => {
    if (!clinicId) {
      setError('Clinic ID not found. Please contact administrator.');
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);

      let response;
      switch (activeFilter) {
        case 'today':
          response = await apiClient.get(`/api/appointments/clinic/${clinicId}/today`);
          break;
        case 'upcoming':
          response = await apiClient.get(`/api/appointments/clinic/${clinicId}/upcoming`);
          break;
        case 'status':
          response = await apiClient.get(`/api/appointments/clinic/${clinicId}/status/${selectedStatus}`);
          break;
        case 'dateRange':
          if (dateRange.startDate && dateRange.endDate) {
            response = await apiClient.get(
              `/api/appointments/clinic/${clinicId}/date-range?startDate=${dateRange.startDate}&endDate=${dateRange.endDate}`
            );
          } else {
            setError('Please select both start and end dates');
            setLoading(false);
            return;
          }
          break;
        default: // 'all'
          response = await apiClient.get(`/api/appointments/clinic/${clinicId}`);
      }

      setAppointments(response.data);
    } catch (err) {
      console.error('Error fetching appointments:', err);
      setError('Failed to load appointments. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [clinicId, activeFilter, selectedStatus, dateRange]);

  useEffect(() => {
    if (userProfile?.clinicId) {
      fetchAppointments();
    }
  }, [userProfile, fetchAppointments]);

  const handleFilterChange = (filter) => {
    setActiveFilter(filter);
  };

  const handleStatusChange = (status) => {
    setSelectedStatus(status);
    setActiveFilter('status');
  };

  const handleDateRangeSubmit = (e) => {
    e.preventDefault();
    if (dateRange.startDate && dateRange.endDate) {
      fetchAppointments();
    }
  };

  const handleWalkInSuccess = () => {
    // Refresh appointments after creating walk-in
    fetchAppointments();
  };

  const getFilterLabel = () => {
    switch (activeFilter) {
      case 'today':
        return "Today's Appointments";
      case 'upcoming':
        return 'Upcoming Appointments';
      case 'status':
        return `${selectedStatus} Appointments`;
      case 'dateRange':
        return 'Date Range';
      default:
        return 'All Appointments';
    }
  };

  if (!userProfile) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-vh-100 bg-light">
      <div className="container py-5">
        {/* Page Header */}
        <div className="row mb-4">
          <div className="col">
            <h1 className="page-title mb-2">
              <i className="bi bi-building me-3"></i>
              Clinic Staff Dashboard
            </h1>
            <p className="page-subtitle text-muted">
              Managing appointments for {userProfile?.clinic?.name || 'your clinic'}
            </p>
          </div>
        </div>

        {/* Filter Tabs */}
        <div className="card shadow-sm mb-4">
          <div className="card-body">
            <ul className="nav nav-pills mb-3">
              <li className="nav-item">
                <button
                  className={`nav-link ${activeFilter === 'all' ? 'active' : ''}`}
                  onClick={() => handleFilterChange('all')}
                >
                  <i className="bi bi-list-ul me-2"></i>
                  All
                </button>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeFilter === 'today' ? 'active' : ''}`}
                  onClick={() => handleFilterChange('today')}
                >
                  <i className="bi bi-calendar-day me-2"></i>
                  Today
                </button>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeFilter === 'upcoming' ? 'active' : ''}`}
                  onClick={() => handleFilterChange('upcoming')}
                >
                  <i className="bi bi-calendar-check me-2"></i>
                  Upcoming
                </button>
              </li>
              <li className="nav-item dropdown">
                <button
                  className={`nav-link dropdown-toggle ${activeFilter === 'status' ? 'active' : ''}`}
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
                >
                  <i className="bi bi-filter me-2"></i>
                  By Status
                </button>
                <ul className="dropdown-menu">
                  <li>
                    <button className="dropdown-item" onClick={() => handleStatusChange('Upcoming')}>
                      <span className="badge bg-primary me-2">Upcoming</span>
                    </button>
                  </li>
                  <li>
                    <button className="dropdown-item" onClick={() => handleStatusChange('Ongoing')}>
                      <span className="badge bg-success me-2">Ongoing</span>
                    </button>
                  </li>
                  <li>
                    <button className="dropdown-item" onClick={() => handleStatusChange('Completed')}>
                      <span className="badge bg-secondary me-2">Completed</span>
                    </button>
                  </li>
                  <li>
                    <button className="dropdown-item" onClick={() => handleStatusChange('Cancelled')}>
                      <span className="badge bg-danger me-2">Cancelled</span>
                    </button>
                  </li>
                  <li>
                    <button className="dropdown-item" onClick={() => handleStatusChange('Missed')}>
                      <span className="badge bg-warning text-dark me-2">Missed</span>
                    </button>
                  </li>
                </ul>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeFilter === 'dateRange' ? 'active' : ''}`}
                  onClick={() => handleFilterChange('dateRange')}
                >
                  <i className="bi bi-calendar-range me-2"></i>
                  Date Range
                </button>
              </li>
            </ul>

            {/* Date Range Filter Form */}
            {activeFilter === 'dateRange' && (
              <form onSubmit={handleDateRangeSubmit} className="row g-3 mt-2">
                <div className="col-md-5">
                  <label htmlFor="startDate" className="form-label small text-muted">
                    Start Date
                  </label>
                  <input
                    type="datetime-local"
                    className="form-control"
                    id="startDate"
                    value={dateRange.startDate}
                    onChange={(e) => setDateRange({ ...dateRange, startDate: e.target.value })}
                    required
                  />
                </div>
                <div className="col-md-5">
                  <label htmlFor="endDate" className="form-label small text-muted">
                    End Date
                  </label>
                  <input
                    type="datetime-local"
                    className="form-control"
                    id="endDate"
                    value={dateRange.endDate}
                    onChange={(e) => setDateRange({ ...dateRange, endDate: e.target.value })}
                    required
                  />
                </div>
                <div className="col-md-2 d-flex align-items-end">
                  <button type="submit" className="btn btn-primary w-100">
                    <i className="bi bi-search me-2"></i>
                    Search
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>

        {/* Current Filter Display */}
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h4 className="mb-0">
            <i className="bi bi-funnel me-2 text-primary"></i>
            {getFilterLabel()}
          </h4>
          <div className="d-flex gap-2">
            <button
              className="btn btn-primary btn-sm"
              onClick={() => setShowWalkInModal(true)}
            >
              <i className="bi bi-person-plus me-2"></i>
              Add Walk-in
            </button>
            <button className="btn btn-outline-secondary btn-sm" onClick={fetchAppointments}>
              <i className="bi bi-arrow-clockwise me-2"></i>
              Refresh
            </button>
          </div>
        </div>

        {/* Error Display */}
        {error && (
          <div className="alert alert-danger d-flex align-items-center" role="alert">
            <i className="bi bi-exclamation-triangle-fill me-2"></i>
            {error}
          </div>
        )}

        {/* Appointments List */}
        <StaffAppointmentList
          appointments={appointments}
          loading={loading}
          filterType={activeFilter}
        />

        {/* Walk-in Appointment Modal */}
        <WalkInAppointmentModal
          show={showWalkInModal}
          onHide={() => setShowWalkInModal(false)}
          onSuccess={handleWalkInSuccess}
          clinicId={clinicId}
        />
      </div>
    </div>
  );
};

export default StaffDashboardPage;
