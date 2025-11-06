import React, { useState } from 'react';
import { Card, Button, Form, Col, Row, Alert, Modal } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const ClinicCard = ({ clinic, onClinicUpdated }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [name, setName] = useState(clinic.name || '');
    const [address, setAddress] = useState(clinic.address || '');
    const [type, setType] = useState(clinic.type || 'G');
    const [telephoneNumber, setTelephoneNumber] = useState(clinic.telephoneNumber || '');
    const [openingHours, setOpeningHours] = useState(clinic.openingHours || '');
    const [closingHours, setClosingHours] = useState(clinic.closingHours || '');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const formatTime = (time) => {
        if (!time) return '';
        return time.substring(0, 5); // Takes only HH:mm part
    };

    const handleSaveChanges = async () => {
        try {
            setError('');
            setLoading(true);

            const clinicData = {
                name,
                address,
                type,
                telephoneNumber: telephoneNumber || null,
                openingHours: formatTime(openingHours),
                closingHours: formatTime(closingHours)
            };

            await apiClient.put(`/api/system-administrators/clinics/${clinic.clinicId}`, clinicData);

            setIsEditing(false);
            onClinicUpdated();
        } catch (error) {
            console.error(`Failed to save changes for ${name}:`, error);
            setError(error.response?.data?.message || `Error saving changes for ${name}`);
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteClick = () => {
        setShowDeleteModal(true);
    };

    const handleCloseDeleteModal = () => {
        setShowDeleteModal(false);
    };

    const handleConfirmDelete = async () => {
        try {
            setError('');
            setLoading(true);

            await apiClient.delete(`/api/system-administrators/clinics/${clinic.clinicId}`);

            handleCloseDeleteModal();
            onClinicUpdated();
        } catch (error) {
            console.error(`Failed to delete clinic ${name}:`, error);
            setError(error.response?.data?.message || `Error deleting clinic ${name}`);
            setLoading(false);
        }
    };

    const handleCancel = () => {
        setIsEditing(false);
        setName(clinic.name || '');
        setAddress(clinic.address || '');
        setType(clinic.type || 'G');
        setTelephoneNumber(clinic.telephoneNumber || '');
        setOpeningHours(clinic.openingHours || '');
        setClosingHours(clinic.closingHours || '');
        setError('');
    };

    return (
        <Card>
            <Card.Body>
                {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}

                {!isEditing ? (
                    <>
                        <Card.Title>{clinic.name}</Card.Title>
                        <Card.Text>
                            <strong>Address:</strong> {clinic.address || 'N/A'}<br />
                            <strong>Type:</strong> {clinic.type === 'G' ? 'General' : 'Specialist'}<br />
                            <strong>Telephone:</strong> {clinic.telephoneNumber || 'N/A'}<br />
                            <strong>Hours:</strong> {clinic.openingHours && clinic.closingHours
                                ? `${formatTime(clinic.openingHours)} - ${formatTime(clinic.closingHours)}`
                                : 'N/A'}
                        </Card.Text>
                        <Button
                            variant="primary"
                            size="sm"
                            onClick={() => setIsEditing(true)}
                            className="me-2"
                        >
                            Edit
                        </Button>
                        <Button
                            variant="danger"
                            size="sm"
                            onClick={handleDeleteClick}
                            disabled={loading}
                        >
                            Delete
                        </Button>
                    </>
                ) : (
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>Clinic Name *</Form.Label>
                            <Form.Control
                                type="text"
                                value={name}
                                onChange={e => setName(e.target.value)}
                                placeholder="Enter clinic name"
                                required
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Address</Form.Label>
                            <Form.Control
                                type="text"
                                value={address}
                                onChange={e => setAddress(e.target.value)}
                                placeholder="Enter clinic address"
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Telephone Number</Form.Label>
                            <Form.Control
                                type="tel"
                                value={telephoneNumber}
                                onChange={e => setTelephoneNumber(e.target.value)}
                                placeholder="Enter telephone number"
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Type *</Form.Label>
                            <Form.Select value={type} onChange={e => setType(e.target.value)} required>
                                <option value="G">General</option>
                                <option value="S">Specialist</option>
                            </Form.Select>
                        </Form.Group>

                        <Form.Group as={Row} className="mb-3">
                            <Form.Label column sm="6">Opening Hours</Form.Label>
                            <Col sm="6">
                                <Form.Control
                                    type="time"
                                    value={openingHours}
                                    onChange={e => setOpeningHours(e.target.value)}
                                />
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} className="mb-3">
                            <Form.Label column sm="6">Closing Hours</Form.Label>
                            <Col sm="6">
                                <Form.Control
                                    type="time"
                                    value={closingHours}
                                    onChange={e => setClosingHours(e.target.value)}
                                />
                            </Col>
                        </Form.Group>

                        <div className="d-flex gap-2">
                            <Button
                                variant="primary"
                                onClick={handleSaveChanges}
                                disabled={loading || !name}
                            >
                                {loading ? 'Saving...' : 'Save Changes'}
                            </Button>
                            <Button
                                variant="secondary"
                                onClick={handleCancel}
                                disabled={loading}
                            >
                                Cancel
                            </Button>
                        </div>
                    </Form>
                )}
            </Card.Body>

            <Modal show={showDeleteModal} onHide={handleCloseDeleteModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Confirm Deletion</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Are you sure you want to delete the clinic <strong>{clinic.name}</strong>? This action cannot be undone.
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleCloseDeleteModal} disabled={loading}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={handleConfirmDelete} disabled={loading}>
                        {loading ? 'Deleting...' : 'Delete'}
                    </Button>
                </Modal.Footer>
            </Modal>
        </Card>
    );
};

export default ClinicCard;
