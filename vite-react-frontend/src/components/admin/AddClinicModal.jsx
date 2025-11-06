import React, { useState } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const AddClinicModal = ({ show, onHide, onClinicAdded }) => {
    const [name, setName] = useState('');
    const [address, setAddress] = useState('');
    const [telephoneNumber, setTelephoneNumber] = useState('');
    const [type, setType] = useState('G');
    const [openingHours, setOpeningHours] = useState('');
    const [closingHours, setClosingHours] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const clinicData = {
                name,
                address,
                telephoneNumber: telephoneNumber || null,
                type,
                openingHours: openingHours || null,
                closingHours: closingHours || null
            };

            await apiClient.post('/api/system-administrators/clinics', clinicData);

            // Reset form
            setName('');
            setAddress('');
            setTelephoneNumber('');
            setType('G');
            setOpeningHours('');
            setClosingHours('');

            onClinicAdded();
            onHide();
        } catch (error) {
            console.error('Failed to add clinic:', error);
            setError(error.response?.data?.message || 'Failed to add clinic. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal show={show} onHide={onHide}>
            <Modal.Header closeButton>
                <Modal.Title>Add New Clinic</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}

                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Clinic Name *</Form.Label>
                        <Form.Control
                            type="text"
                            value={name}
                            onChange={e => setName(e.target.value.slice(0, 255))}
                            placeholder="Enter clinic name"
                            maxLength="255"
                            required
                        />
                        <Form.Text className="text-muted">
                            {name.length}/255 characters
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Address *</Form.Label>
                        <Form.Control
                            type="text"
                            value={address}
                            onChange={e => setAddress(e.target.value.slice(0, 255))}
                            placeholder="Enter clinic address"
                            maxLength="255"
                            required
                        />
                        <Form.Text className="text-muted">
                            {address.length}/255 characters
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Telephone Number</Form.Label>
                        <Form.Control
                            type="tel"
                            value={telephoneNumber}
                            onChange={e => setTelephoneNumber(e.target.value.slice(0, 20))}
                            placeholder="Enter telephone number"
                            maxLength="20"
                        />
                        <Form.Text className="text-muted">
                            {telephoneNumber.length}/20 characters
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Clinic Type *</Form.Label>
                        <Form.Select value={type} onChange={e => setType(e.target.value)} required>
                            <option value="G">General</option>
                            <option value="S">Specialist</option>
                        </Form.Select>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Opening Hours</Form.Label>
                        <Form.Control
                            type="time"
                            value={openingHours}
                            onChange={e => setOpeningHours(e.target.value)}
                        />
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Closing Hours</Form.Label>
                        <Form.Control
                            type="time"
                            value={closingHours}
                            onChange={e => setClosingHours(e.target.value)}
                        />
                    </Form.Group>

                    <div className="d-flex justify-content-end gap-2">
                        <Button variant="secondary" onClick={onHide} disabled={loading}>
                            Cancel
                        </Button>
                        <Button variant="primary" type="submit" disabled={loading}>
                            {loading ? 'Adding Clinic...' : 'Add Clinic'}
                        </Button>
                    </div>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default AddClinicModal;
