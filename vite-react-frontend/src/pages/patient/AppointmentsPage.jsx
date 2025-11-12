import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import apiClient from '../../api/apiClient';
import AppointmentList from '../../components/patient/AppointmentList';
import BookAppointmentModal from '../../components/patient/BookAppointmentModal';
import CancelAppointmentModal from '../../components/patient/CancelAppointmentModal';
import RescheduleAppointmentModal from '../../components/patient/RescheduleAppointmentModal';
import ViewMedicalSummaryModal from '../../components/patient/ViewMedicalSummaryModal';

const AppointmentsPage = () => {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showBookModal, setShowBookModal] = useState(false);
  const [pendingCancelId, setPendingCancelId] = useState(null);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [pendingRescheduleAppointment, setPendingRescheduleAppointment] = useState(null);
  const [showRescheduleModal, setShowRescheduleModal] = useState(false);
  const [showSummaryModal, setShowSummaryModal] = useState(false);
  const [selectedAppointmentForSummary, setSelectedAppointmentForSummary] = useState(null);
  const [medicalSummary, setMedicalSummary] = useState(null);
  const [loadingSummary, setLoadingSummary] = useState(false);

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

  const handleCancelAppointment = (appointmentId) => {
    // open modal instead of immediate confirm
    setPendingCancelId(appointmentId);
    setShowCancelModal(true);
  };

  const handleCancelModalHide = () => {
    setShowCancelModal(false);
    setPendingCancelId(null);
  };

  const handleRequestReschedule = (appointment) => {
    setPendingRescheduleAppointment(appointment);
    setShowRescheduleModal(true);
  };

  const handleRescheduleSuccess = () => {
    setShowRescheduleModal(false);
    setPendingRescheduleAppointment(null);
    // Add small delay to ensure backend has persisted changes
    setTimeout(() => {
      fetchAppointments();
    }, 500);
  };

  const handleViewSummary = async (appointment) => {
    setSelectedAppointmentForSummary(appointment);
    setShowSummaryModal(true);
    setLoadingSummary(true);
    setMedicalSummary(null);

    try {
      const response = await apiClient.get(`/api/appointments/${appointment.appointmentId}/medical-summary`);
      setMedicalSummary(response.data);
    } catch (err) {
      console.error('Error fetching medical summary:', err);
      // Show modal anyway, it will display "No summary available"
    } finally {
      setLoadingSummary(false);
    }
  };

  const handleCloseSummaryModal = () => {
    setShowSummaryModal(false);
    setSelectedAppointmentForSummary(null);
    setMedicalSummary(null);
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
          onRequestReschedule={handleRequestReschedule}
          onViewSummary={handleViewSummary}
        />

        {showCancelModal && (
          <CancelAppointmentModal
            show={showCancelModal}
            onHide={handleCancelModalHide}
            appointmentId={pendingCancelId}
            onSuccess={() => {
              setShowCancelModal(false);
              setPendingCancelId(null);
              fetchAppointments();
            }}
          />
        )}

        {showRescheduleModal && pendingRescheduleAppointment && (
          <RescheduleAppointmentModal
            show={showRescheduleModal}
            onHide={() => setShowRescheduleModal(false)}
            appointment={pendingRescheduleAppointment}
            onSuccess={handleRescheduleSuccess}
          />
        )}

        {showBookModal && (
          <BookAppointmentModal
            show={showBookModal}
            onHide={() => setShowBookModal(false)}
            onSuccess={handleAppointmentBooked}
          />
        )}

        {showSummaryModal && (
          <ViewMedicalSummaryModal
            show={showSummaryModal}
            onHide={handleCloseSummaryModal}
            appointment={selectedAppointmentForSummary}
            summary={loadingSummary ? null : medicalSummary}
            loading={loadingSummary}
          />
        )}
      </div>
    </div>
  );
};

export default AppointmentsPage;
