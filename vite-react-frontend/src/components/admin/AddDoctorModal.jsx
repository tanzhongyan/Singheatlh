import React, { useState } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const AddDoctorModal = ({ show, onHide, onDoctorAdded }) => {
    const [name, setName] = useState('');
    const [schedule, setSchedule] = useState('');
    const [clinicId, setClinicId] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();

        const doctorData = { name, schedule, clinicId: parseInt(clinicId) };

        try {
            await apiClient.post('/api/system-administrators/doctors', doctorData);
            onDoctorAdded();
            onHide();
        } catch (error) {
            console.error('Failed to add doctor:', error);
        }
    };

    return (
        <Modal show={show} onHide={onHide}>
            <Modal.Header closeButton>
                <Modal.Title>Add New Doctor</Modal.Title>
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
                        Add Doctor
                    </Button>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default AddDoctorModal;
