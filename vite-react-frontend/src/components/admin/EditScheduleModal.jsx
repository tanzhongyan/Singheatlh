import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const EditScheduleModal = ({ show, onHide, onScheduleUpdated, schedule, doctor }) => {
    const [startDatetime, setStartDatetime] = useState('');
    const [endDatetime, setEndDatetime] = useState('');
    const [type, setType] = useState('AVAILABLE');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (schedule) {
            // Convert ISO string to datetime-local format
            const formatDatetimeLocal = (isoString) => {
                if (!isoString) return '';
                const date = new Date(isoString);
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                const hours = String(date.getHours()).padStart(2, '0');
                const minutes = String(date.getMinutes()).padStart(2, '0');
                return `${year}-${month}-${day}T${hours}:${minutes}`;
            };

            setStartDatetime(formatDatetimeLocal(schedule.startDatetime));
            setEndDatetime(formatDatetimeLocal(schedule.endDatetime));
            setType(schedule.type || 'AVAILABLE');
            setError('');
        }
    }, [schedule]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!schedule) return;

        setError('');
        setLoading(true);

        // Validation
        const start = new Date(startDatetime);
        const end = new Date(endDatetime);

        if (end <= start) {
            setError('End time must be after start time');
            setLoading(false);
            return;
        }

        const updatedScheduleData = {
            scheduleId: schedule.scheduleId,
            doctorId: doctor.doctorId,
            startDatetime: startDatetime,
            endDatetime: endDatetime,
            type: type
        };

        try {
            await apiClient.put(`/api/schedules/${schedule.scheduleId}`, updatedScheduleData);
            onScheduleUpdated();
            onHide();
        } catch (error) {
            console.error('Failed to update schedule:', error);
            if (error.response?.status === 409) {
                // Use backend message for conflict errors
                const backendMessage = error.response?.data?.message;
                setError(backendMessage || 'Schedule Conflict: This time slot overlaps with another schedule. Please choose a different time range.');
            } else {
                setError(error.response?.data?.message || 'Failed to update schedule block');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal show={show} onHide={onHide}>
            <Modal.Header closeButton>
                <Modal.Title>Edit Schedule Block</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {error && <Alert variant="danger">{error}</Alert>}

                <Alert variant="info">
                    <strong>Doctor:</strong> {doctor?.name}<br />
                    <strong>Clinic:</strong> {doctor?.clinicName || `Clinic #${doctor?.clinicId}`}<br />
                    <strong>Schedule ID:</strong> {schedule?.scheduleId}
                </Alert>

                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Start Date & Time *</Form.Label>
                        <Form.Control
                            type="datetime-local"
                            value={startDatetime}
                            onChange={e => setStartDatetime(e.target.value)}
                            required
                        />
                        <Form.Text className="text-muted">
                            When does this schedule block start?
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>End Date & Time *</Form.Label>
                        <Form.Control
                            type="datetime-local"
                            value={endDatetime}
                            onChange={e => setEndDatetime(e.target.value)}
                            required
                        />
                        <Form.Text className="text-muted">
                            When does this schedule block end?
                        </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Schedule Type *</Form.Label>
                        <Form.Select
                            value={type}
                            onChange={e => setType(e.target.value)}
                            required
                        >
                            <option value="AVAILABLE">Available (Doctor can see patients)</option>
                            <option value="UNAVAILABLE">Unavailable (Doctor is off/busy)</option>
                        </Form.Select>
                        <Form.Text className="text-muted">
                            Select "Available" for times when the doctor can accept appointments.
                            Select "Unavailable" for breaks, meetings, or time off.
                        </Form.Text>
                    </Form.Group>

                    <div className="d-flex justify-content-end gap-2">
                        <Button variant="secondary" onClick={onHide} disabled={loading}>
                            Cancel
                        </Button>
                        <Button variant="primary" type="submit" disabled={loading}>
                            {loading ? 'Saving...' : 'Save Changes'}
                        </Button>
                    </div>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default EditScheduleModal;
