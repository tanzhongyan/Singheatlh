import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const EditDoctorModal = ({ show, onHide, onDoctorUpdated, doctor }) => {
    const [name, setName] = useState('');
    const [clinicId, setClinicId] = useState('');
    const [appointmentDuration, setAppointmentDuration] = useState('15');
    const [clinics, setClinics] = useState([]);
    const [error, setError] = useState('');

    useEffect(() => {
        if (show) {
            fetchClinics();
        }
    }, [show]);

    useEffect(() => {
        if (doctor) {
            setName(doctor.name || '');
            setClinicId(doctor.clinicId || '');
            setAppointmentDuration(doctor.appointmentDurationInMinutes || '15');
            setError('');
        }
    }, [doctor]);

    const fetchClinics = async () => {
        try {
            const response = await apiClient.get('/api/system-administrators/clinics');
            setClinics(response.data);
        } catch (error) {
            console.error('Failed to fetch clinics:', error);
            setError('Failed to load clinics');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!doctor) return;
        setError('');

        const updatedDoctorData = {
            name,
            clinicId: parseInt(clinicId.trim()),
            appointmentDurationInMinutes: parseInt(appointmentDuration.trim())
        };

        try {
            await apiClient.put(`/api/system-administrators/doctors/${doctor.doctorId}`, updatedDoctorData);
            onDoctorUpdated();
            onHide();
        } catch (error) {
            console.error('Failed to update doctor:', error);
            setError(error.response?.data?.message || 'Failed to update doctor');
        }
    };

    return (
        <Modal show={show} onHide={onHide}>
            <Modal.Header closeButton>
                <Modal.Title>Edit Doctor</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {error && <Alert variant="danger">{error}</Alert>}
                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Doctor ID</Form.Label>
                        <Form.Control
                            type="text"
                            value={doctor?.doctorId || ''}
                            disabled
                            readOnly
                        />
                        <Form.Text className="text-muted">
                            Doctor ID cannot be changed
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Doctor Name *</Form.Label>
                        <Form.Control
                            type="text"
                            value={name}
                            onChange={e => setName(e.target.value.slice(0, 255))}
                            placeholder="Enter doctor name"
                            maxLength="255"
                            required
                        />
                        <Form.Text className="text-muted">
                            {name.length}/255 characters
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Clinic *</Form.Label>
                        <Form.Select
                            value={clinicId}
                            onChange={e => setClinicId(e.target.value)}
                            required
                        >
                            <option value="">Select a clinic</option>
                            {clinics.map(clinic => (
                                <option key={clinic.clinicId} value={clinic.clinicId}>
                                    {clinic.name} ({clinic.type === 'G' ? 'General' : 'Specialist'})
                                </option>
                            ))}
                        </Form.Select>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Appointment Duration (minutes) *</Form.Label>
                        <Form.Select
                            value={appointmentDuration}
                            onChange={e => setAppointmentDuration(e.target.value)}
                            required
                        >
                            <option value="15">15 minutes</option>
                            <option value="20">20 minutes</option>
                            <option value="30">30 minutes</option>
                            <option value="45">45 minutes</option>
                            <option value="60">60 minutes</option>
                        </Form.Select>
                        <Form.Text className="text-muted">
                            This is the default duration for appointments with this doctor.
                        </Form.Text>
                    </Form.Group>

                    <div className="d-flex justify-content-end gap-2">
                        <Button variant="secondary" onClick={onHide}>
                            Cancel
                        </Button>
                        <Button variant="primary" type="submit">
                            Save Changes
                        </Button>
                    </div>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default EditDoctorModal;
