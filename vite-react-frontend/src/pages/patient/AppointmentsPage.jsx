import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import apiClient from '../../api/apiClient';
import AppointmentList from '../../components/patient/AppointmentList';
import BookAppointmentModal from '../../components/patient/BookAppointmentModal';

const AppointmentsPage = () => {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showBookModal, setShowBookModal] = useState(false);

  const fetchAppointments = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      if (user?.id) {
        const response = await apiClient.get(`/api/appointments/patient/${user.id}`);
        setAppointments(response.data);
      } else {
        setError('User ID not found. Please log in again.');
      }
    } catch (err) {
      console.error('Error fetching appointments:', err);
      setError('Failed to load appointments. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    if (user) {
      fetchAppointments();
    }
  }, [user, fetchAppointments]);

  const handleAppointmentBooked = () => {
    setShowBookModal(false);
    fetchAppointments(); // Refresh the list
  };

  const handleCancelAppointment = async (appointmentId) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) {
      return;
    }

    try {
      await apiClient.delete(`/api/appointments/${appointmentId}`);
      fetchAppointments(); // Refresh the list
    } catch (err) {
      console.error('Error cancelling appointment:', err);
      alert('Failed to cancel appointment. Please try again.');
    }
  };

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-vh-100 bg-white">
      <div className="container py-5">
        {/* Page Header */}
        <div className="row mb-4 align-items-center">
          <div className="col">
            <h1 className="page-title mb-2">
              <i className="bi bi-calendar-check me-3"></i>
              My Appointments
            </h1>
            <p className="page-subtitle">View and manage your medical appointments</p>
          </div>
          <div className="col-auto">
            <button
              className="btn btn-primary px-4 py-2"
              onClick={() => setShowBookModal(true)}
            >
              <i className="bi bi-plus-circle me-2"></i>
              Book Appointment
            </button>
          </div>
        </div>

        {error && (
          <div className="alert alert-danger d-flex align-items-center" role="alert">
            <i className="bi bi-exclamation-triangle-fill me-2"></i>
            {error}
          </div>
        )}

        <AppointmentList
          appointments={appointments}
          onCancel={handleCancelAppointment}
        />

        {showBookModal && (
          <BookAppointmentModal
            show={showBookModal}
            onHide={() => setShowBookModal(false)}
            onSuccess={handleAppointmentBooked}
          />
        )}
      </div>
    </div>
  );
};

export default AppointmentsPage;
