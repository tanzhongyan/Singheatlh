import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const EditUserModal = ({ show, onHide, onUserUpdated, user }) => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [telephoneNumber, setTelephoneNumber] = useState('');
    const [clinicId, setClinicId] = useState('');
    const [clinics, setClinics] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (show) {
            fetchClinics();
        }
    }, [show]);

    useEffect(() => {
        if (user) {
            setName(user.name || '');
            setEmail(user.email || '');
            setTelephoneNumber(user.telephoneNumber || '');
            setClinicId(user.clinicId || '');
            setError('');
        }
    }, [user]);

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
        if (!user) return;
        setError('');
        setLoading(true);

        try {
            const userData = {
                userId: user.userId,
                name: name || null,  // Allow null for name
                email,
                role: user.role,
                telephoneNumber: telephoneNumber || null  // Allow null for telephone number
            };

            // Add clinicId for clinic staff
            if (user.role === 'C') {
                userData.clinicId = clinicId ? parseInt(clinicId.trim()) : null;
            }

            await apiClient.put(`/api/system-administrators/users/${user.userId}`, userData);
            onUserUpdated();
            onHide();
        } catch (error) {
            console.error('Failed to update user:', error);
            setError(error.response?.data?.message || 'Failed to update user. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const getRoleLabel = (role) => {
        switch (role) {
            case 'P': return 'Patient';
            case 'C': return 'Clinic Staff';
            case 'S': return 'System Administrator';
            default: return role;
        }
    };

    return (
        <Modal show={show} onHide={onHide}>
            <Modal.Header closeButton>
                <Modal.Title>Edit User</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}

                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>User ID</Form.Label>
                        <Form.Control
                            type="text"
                            value={user?.userId || ''}
                            disabled
                            readOnly
                        />
                        <Form.Text className="text-muted">
                            User ID cannot be changed
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Role</Form.Label>
                        <Form.Control
                            type="text"
                            value={user ? getRoleLabel(user.role) : ''}
                            disabled
                            readOnly
                        />
                        <Form.Text className="text-muted">
                            User role cannot be changed
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Full Name <span className="text-muted">(Optional)</span></Form.Label>
                        <Form.Control
                            type="text"
                            value={name}
                            onChange={e => setName(e.target.value.slice(0, 255))}
                            placeholder="Enter full name"
                            maxLength="255"
                        />
                        <Form.Text className="text-muted">
                            {name.length}/255 characters
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Email Address</Form.Label>
                        <Form.Control
                            type="email"
                            value={email}
                            disabled
                            readOnly
                        />
                        <Form.Text className="text-muted">
                            Email cannot be changed (linked to authentication)
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Telephone Number <span className="text-muted">(Optional)</span></Form.Label>
                        <Form.Control
                            type="tel"
                            value={telephoneNumber}
                            onChange={e => setTelephoneNumber(e.target.value)}
                            placeholder="Enter telephone number"
                        />
                    </Form.Group>

                    {user?.role === 'C' && (
                        <Form.Group className="mb-3">
                            <Form.Label>Assigned Clinic</Form.Label>
                            <Form.Select
                                value={clinicId}
                                onChange={e => setClinicId(e.target.value)}
                            >
                                <option value="">No clinic assigned</option>
                                {clinics.map(clinic => (
                                    <option key={clinic.clinicId} value={clinic.clinicId}>
                                        {clinic.name} ({clinic.type === 'G' ? 'General' : 'Specialist'})
                                    </option>
                                ))}
                            </Form.Select>
                            <Form.Text className="text-muted">
                                Update the clinic assignment for this staff member
                            </Form.Text>
                        </Form.Group>
                    )}

                    <div className="d-flex justify-content-end gap-2">
                        <Button variant="secondary" onClick={onHide} disabled={loading}>
                            Cancel
                        </Button>
                        <Button variant="primary" type="submit" disabled={loading}>
                            {loading ? 'Updating...' : 'Save Changes'}
                        </Button>
                    </div>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default EditUserModal;
