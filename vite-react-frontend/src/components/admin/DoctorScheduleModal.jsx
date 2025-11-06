import React, { useState, useEffect } from 'react';
import { Modal, Button, Table, Badge, Alert } from 'react-bootstrap';
import apiClient from '../../api/apiClient';
import AddScheduleModal from './AddScheduleModal';
import EditScheduleModal from './EditScheduleModal';

const DoctorScheduleModal = ({ show, onHide, doctor }) => {
    const [schedules, setSchedules] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [showAddModal, setShowAddModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedSchedule, setSelectedSchedule] = useState(null);

    useEffect(() => {
        if (show && doctor) {
            fetchSchedules();
        }
    }, [show, doctor]);

    const fetchSchedules = async () => {
        if (!doctor?.doctorId) return;

        setLoading(true);
        setError('');
        try {
            const response = await apiClient.get(`/api/schedules/doctor/${doctor.doctorId}`);
            // Sort schedules by start time
            const sortedSchedules = response.data.sort((a, b) =>
                new Date(a.startDatetime) - new Date(b.startDatetime)
            );
            setSchedules(sortedSchedules);
        } catch (error) {
            console.error('Failed to fetch schedules:', error);
            setError('Failed to load schedules');
        } finally {
            setLoading(false);
        }
    };

    const handleEditClick = (schedule) => {
        setSelectedSchedule(schedule);
        setShowEditModal(true);
    };

    const handleDeleteClick = (schedule) => {
        setSelectedSchedule(schedule);
        setShowDeleteModal(true);
    };

    const handleConfirmDelete = async () => {
        if (!selectedSchedule) return;

        try {
            await apiClient.delete(`/api/schedules/${selectedSchedule.scheduleId}`);
            fetchSchedules();
            setShowDeleteModal(false);
            setSelectedSchedule(null);
        } catch (error) {
            console.error('Failed to delete schedule:', error);
            setError(error.response?.data?.message || 'Failed to delete schedule');
        }
    };

    const formatDateTime = (dateTimeString) => {
        const date = new Date(dateTimeString);
        return date.toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getTypeBadge = (type) => {
        return type === 'AVAILABLE' ? (
            <Badge bg="success">Available</Badge>
        ) : (
            <Badge bg="secondary">Unavailable</Badge>
        );
    };

    return (
        <>
            <Modal show={show} onHide={onHide} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>
                        Manage Schedule - {doctor?.name}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}

                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h6 className="mb-0">Schedule Blocks</h6>
                        <Button
                            variant="primary"
                            size="sm"
                            onClick={() => setShowAddModal(true)}
                        >
                            Add Schedule Block
                        </Button>
                    </div>

                    {loading ? (
                        <div className="text-center py-4">
                            <div className="spinner-border" role="status">
                                <span className="visually-hidden">Loading...</span>
                            </div>
                        </div>
                    ) : schedules.length === 0 ? (
                        <Alert variant="info">
                            No schedule blocks found. Click "Add Schedule Block" to create one.
                        </Alert>
                    ) : (
                        <Table striped bordered hover responsive>
                            <thead>
                                <tr>
                                    <th>Start Time</th>
                                    <th>End Time</th>
                                    <th>Type</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {schedules.map((schedule) => (
                                    <tr key={schedule.scheduleId}>
                                        <td>{formatDateTime(schedule.startDatetime)}</td>
                                        <td>{formatDateTime(schedule.endDatetime)}</td>
                                        <td>{getTypeBadge(schedule.type)}</td>
                                        <td>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="me-2"
                                                onClick={() => handleEditClick(schedule)}
                                            >
                                                Edit
                                            </Button>
                                            <Button
                                                variant="outline-danger"
                                                size="sm"
                                                onClick={() => handleDeleteClick(schedule)}
                                            >
                                                Delete
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onHide}>
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>

            <AddScheduleModal
                show={showAddModal}
                onHide={() => setShowAddModal(false)}
                onScheduleAdded={fetchSchedules}
                doctor={doctor}
            />

            {selectedSchedule && (
                <EditScheduleModal
                    show={showEditModal}
                    onHide={() => setShowEditModal(false)}
                    onScheduleUpdated={fetchSchedules}
                    schedule={selectedSchedule}
                    doctor={doctor}
                />
            )}

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Confirm Deletion</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Are you sure you want to delete this schedule block?
                    <div className="mt-2">
                        <strong>Start:</strong> {selectedSchedule && formatDateTime(selectedSchedule.startDatetime)}<br />
                        <strong>End:</strong> {selectedSchedule && formatDateTime(selectedSchedule.endDatetime)}<br />
                        <strong>Type:</strong> {selectedSchedule && getTypeBadge(selectedSchedule.type)}
                    </div>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={handleConfirmDelete}>
                        Delete
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
};

export default DoctorScheduleModal;
