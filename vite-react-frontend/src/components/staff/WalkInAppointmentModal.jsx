import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const WalkInAppointmentModal = ({ show, onHide, onSuccess, clinicId }) => {
  const [formData, setFormData] = useState({
    patientId: '',
    doctorId: '',
    startDatetime: '',
    endDatetime: '',
  });
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Fetch patients and doctors when modal opens
  useEffect(() => {
    if (show) {
      fetchPatients();
      fetchDoctors();
      // Set default time to now
      const now = new Date();
      const endTime = new Date(now.getTime() + 30 * 60000); // 30 minutes later
      setFormData(prev => ({
        ...prev,
        startDatetime: formatDateTimeLocal(now),
        endDatetime: formatDateTimeLocal(endTime),
      }));
    }
  }, [show, clinicId]);

  const formatDateTimeLocal = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  const fetchPatients = async () => {
    try {
      const response = await apiClient.get('/api/users/role/P');
      setPatients(response.data);
    } catch (err) {
      console.error('Error fetching patients:', err);
      setError('Failed to load patients');
    }
  };

  const fetchDoctors = async () => {
    try {
      // Fetch doctors from the doctor table filtered by clinicId
      const response = await apiClient.get(`/api/doctor/clinic/${clinicId}`);
      setDoctors(response.data);
    } catch (err) {
      console.error('Error fetching doctors:', err);
      setError('Failed to load doctors');
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleStartTimeChange = (e) => {
    const startTime = e.target.value;
    const start = new Date(startTime);
    const end = new Date(start.getTime() + 30 * 60000); // 30 minutes later

    setFormData(prev => ({
      ...prev,
      startDatetime: startTime,
      endDatetime: formatDateTimeLocal(end)
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const payload = {
        patientId: formData.patientId,
        doctorId: formData.doctorId,
        startDatetime: new Date(formData.startDatetime).toISOString(),
        endDatetime: new Date(formData.endDatetime).toISOString(),
        isWalkIn: true
      };

      await apiClient.post('/api/appointments/walk-in', payload);

      // Reset form and close modal
      setFormData({
        patientId: '',
        doctorId: '',
        startDatetime: '',
        endDatetime: '',
      });
      onSuccess();
      onHide();
    } catch (err) {
      console.error('Error creating walk-in appointment:', err);
      setError(err.response?.data?.message || 'Failed to create walk-in appointment');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal show={show} onHide={onHide} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>Create Walk-in Appointment</Modal.Title>
      </Modal.Header>

      <Modal.Body>
        {error && <Alert variant="danger">{error}</Alert>}

        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label>Patient</Form.Label>
            <Form.Select
              name="patientId"
              value={formData.patientId}
              onChange={handleInputChange}
              required
            >
              <option value="">Select a patient...</option>
              {patients.map(patient => (
                <option key={patient.userId} value={patient.userId}>
                  {patient.name} - {patient.email}
                </option>
              ))}
            </Form.Select>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Doctor</Form.Label>
            <Form.Select
              name="doctorId"
              value={formData.doctorId}
              onChange={handleInputChange}
              required
            >
              <option value="">Select a doctor...</option>
              {doctors.map(doctor => (
                <option key={doctor.doctorId} value={doctor.doctorId}>
                  {doctor.name}
                </option>
              ))}
            </Form.Select>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Start Time</Form.Label>
            <Form.Control
              type="datetime-local"
              name="startDatetime"
              value={formData.startDatetime}
              onChange={handleStartTimeChange}
              required
            />
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>End Time</Form.Label>
            <Form.Control
              type="datetime-local"
              name="endDatetime"
              value={formData.endDatetime}
              onChange={handleInputChange}
              required
            />
          </Form.Group>

          <div className="d-flex justify-content-end gap-2">
            <Button variant="secondary" onClick={onHide} disabled={loading}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={loading}>
              {loading ? 'Creating...' : 'Create Walk-in Appointment'}
            </Button>
          </div>
        </Form>
      </Modal.Body>
    </Modal>
  );
};

export default WalkInAppointmentModal;
