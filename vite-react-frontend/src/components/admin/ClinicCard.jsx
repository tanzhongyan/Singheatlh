import React, { useState } from 'react';
import { Card, Button, Form, Col, Row } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const ClinicCard = ({ clinic }) => {
    const [openingHours, setOpeningHours] = useState(clinic.openingHours || '');
    const [closingHours, setClosingHours] = useState(clinic.closingHours || '');
    const [slotDuration, setSlotDuration] = useState(clinic.appointmentSlotDuration || 15);

    const handleSaveChanges = async () => {
        try {
            // Update hours
            await apiClient.put(`/api/system-administrators/clinics/${clinic.id}/hours`, {
                openingHours,
                closingHours
            });

            // Update slot duration
            await apiClient.put(`/api/system-administrators/clinics/${clinic.id}/slot-duration`, {
                slotDuration
            });

            alert(`Changes saved for ${clinic.name}`);
        } catch (error) {
            console.error(`Failed to save changes for ${clinic.name}:`, error);
            alert(`Error saving changes for ${clinic.name}`);
        }
    };

    return (
        <Card>
            <Card.Body>
                <Card.Title>{clinic.name}</Card.Title>
                <Card.Text>
                    <strong>Address:</strong> {clinic.address}<br />
                    <strong>Type:</strong> {clinic.type}
                </Card.Text>
                <Form>
                    <Form.Group as={Row} className="mb-3">
                        <Form.Label column sm="6">Opening Hours</Form.Label>
                        <Col sm="6">
                            <Form.Control type="time" value={openingHours} onChange={e => setOpeningHours(e.target.value)} />
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-3">
                        <Form.Label column sm="6">Closing Hours</Form.Label>
                        <Col sm="6">
                            <Form.Control type="time" value={closingHours} onChange={e => setClosingHours(e.target.value)} />
                        </Col>
                    </Form.Group>
                    <Form.Group as={Row} className="mb-3">
                        <Form.Label column sm="6">Slot Duration (min)</Form.Label>
                        <Col sm="6">
                            <Form.Control type="number" value={slotDuration} onChange={e => setSlotDuration(parseInt(e.target.value))}/>
                        </Col>
                    </Form.Group>
                    <Button variant="primary" onClick={handleSaveChanges}>Save Changes</Button>
                </Form>
            </Card.Body>
        </Card>
    );
};

export default ClinicCard;
