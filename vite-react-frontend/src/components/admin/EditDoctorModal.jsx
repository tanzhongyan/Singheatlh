import React, { useState, useEffect } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const EditDoctorModal = ({ show, onHide, onDoctorUpdated, doctor }) => {
    const [name, setName] = useState('');
    const [schedule, setSchedule] = useState('');
    const [clinicId, setClinicId] = useState('');

    useEffect(() => {
        if (doctor) {
            setName(doctor.name || '');
            setSchedule(doctor.schedule || '');
            setClinicId(doctor.clinicId || '');
        }
    }, [doctor]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!doctor) return;

        const updatedDoctorData = {
            ...doctor,
            name,
            schedule,
            clinicId: parseInt(clinicId)
        };

        try {
            await apiClient.put(`/api/system-administrators/doctors/${doctor.id}`, updatedDoctorData);
            onDoctorUpdated();
            onHide();
        } catch (error) {
            console.error('Failed to update doctor:', error);
        }
    };

    return (
        <Modal show={show} onHide={onHide}>
            <Modal.Header closeButton>
                <Modal.Title>Edit Doctor</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Name</Form.Label>
                        <Form.Control type="text" value={name} onChange={e => setName(e.target.value)} required />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Schedule</Form.Label>
                        <Form.Control type="text" value={schedule} onChange={e => setSchedule(e.target.value)} required />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Clinic ID</Form.Label>
                        <Form.Control type="number" value={clinicId} onChange={e => setClinicId(e.target.value)} required />
                    </Form.Group>
                    <Button variant="primary" type="submit">
                        Save Changes
                    </Button>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default EditDoctorModal;
