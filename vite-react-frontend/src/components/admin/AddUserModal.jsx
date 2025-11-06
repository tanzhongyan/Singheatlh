import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const AddUserModal = ({ show, onHide, onUserAdded }) => {
    const [name, setName] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [telephoneNumber, setTelephoneNumber] = useState('');
    const [role, setRole] = useState('P');
    const [clinicId, setClinicId] = useState('');
    const [clinics, setClinics] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (show) {
            fetchClinics();
            // Reset form
            setName('');
            setUsername('');
            setEmail('');
            setPassword('');
            setTelephoneNumber('');
            setRole('P');
            setClinicId('');
            setError('');
        }
    }, [show]);

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
        setError('');
        setLoading(true);

        try {
            const userData = {
                name,
                username,
                email,
                password,
                telephoneNumber: telephoneNumber || null,
                role
            };

            // Add clinicId for clinic staff
            if (role === 'C') {
                userData.clinicId = parseInt(clinicId.trim());
            }

            // Backend will handle both Supabase auth user creation AND database record creation
            if (role === 'P') {
                await apiClient.post('/api/system-administrators/users/patient', userData);
            } else if (role === 'C') {
                await apiClient.post('/api/system-administrators/users/staff', userData);
            }

            onUserAdded();
            onHide();
        } catch (error) {
            console.error('Failed to add user:', error);
            // Use backend message which is more descriptive
            const errorMessage = error.response?.data?.message || error.message || 'Failed to add user. Please try again.';
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal show={show} onHide={onHide}>
            <Modal.Header closeButton>
                <Modal.Title>Add New User</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}

                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Full Name *</Form.Label>
                        <Form.Control
                            type="text"
                            value={name}
                            onChange={e => setName(e.target.value.slice(0, 255))}
                            placeholder="Enter full name"
                            maxLength="255"
                            required
                        />
                        <Form.Text className="text-muted">
                            {name.length}/255 characters
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Username *</Form.Label>
                        <Form.Control
                            type="text"
                            value={username}
                            onChange={e => setUsername(e.target.value.slice(0, 100))}
                            placeholder="Enter username"
                            maxLength="100"
                            required
                        />
                        <Form.Text className="text-muted">
                            {username.length}/100 characters
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Email Address *</Form.Label>
                        <Form.Control
                            type="email"
                            value={email}
                            onChange={e => setEmail(e.target.value.slice(0, 255))}
                            placeholder="user@example.com"
                            maxLength="255"
                            required
                        />
                        <Form.Text className="text-muted">
                            {email.length}/255 characters
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Password *</Form.Label>
                        <Form.Control
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value.slice(0, 72))}
                            placeholder="Enter password"
                            minLength="6"
                            maxLength="72"
                            required
                        />
                        <Form.Text className="text-muted">
                            {password.length}/72 characters (6-72 required)
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
                        <Form.Label>Role *</Form.Label>
                        <Form.Select value={role} onChange={e => setRole(e.target.value)} required>
                            <option value="P">Patient</option>
                            <option value="C">Clinic Staff</option>
                        </Form.Select>
                        <Form.Text className="text-muted">
                            Select the user's role in the system
                        </Form.Text>
                    </Form.Group>

                    {role === 'C' && (
                        <Form.Group className="mb-3">
                            <Form.Label>Assigned Clinic *</Form.Label>
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
                            <Form.Text className="text-muted">
                                Clinic staff must be assigned to a specific clinic
                            </Form.Text>
                        </Form.Group>
                    )}

                    <div className="d-flex justify-content-end gap-2">
                        <Button variant="secondary" onClick={onHide} disabled={loading}>
                            Cancel
                        </Button>
                        <Button variant="primary" type="submit" disabled={loading}>
                            {loading ? 'Adding User...' : 'Add User'}
                        </Button>
                    </div>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default AddUserModal;
