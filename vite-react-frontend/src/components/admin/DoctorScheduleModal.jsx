import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Modal, Button, Table, Badge, Alert, Form, Row, Col } from 'react-bootstrap';
import apiClient from '../../api/apiClient';
import AddScheduleModal from './AddScheduleModal';
import EditScheduleModal from './EditScheduleModal';

const DoctorScheduleModal = ({ show, onHide, doctor }) => {
    const [data, setData] = useState(null);
    const [error, setError] = useState('');
    const [showAddModal, setShowAddModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedSchedule, setSelectedSchedule] = useState(null);
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize] = useState(10);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const dateFilterTimeoutRef = useRef(null);

    const fetchSchedules = useCallback(async (page) => {
        if (!doctor?.doctorId) return;

        setError('');
        try {
            const params = new URLSearchParams({
                page,
                pageSize,
            });

            let url = `/api/schedules/doctor/${doctor.doctorId}/paginated`;

            // Use date range endpoint if at least one date is provided
            if (startDate || endDate) {
                url = `/api/schedules/doctor/${doctor.doctorId}/paginated/date-range`;
                // Use provided dates or fallback to extreme values for open-ended ranges
                const start = startDate ? new Date(startDate).toISOString() : new Date('1900-01-01').toISOString();
                const end = endDate ? new Date(endDate).toISOString() : new Date('2099-12-31').toISOString();
                params.append('startDate', start);
                params.append('endDate', end);
            }

            const response = await apiClient.get(`${url}?${params.toString()}`);
            setData(response.data);
        } catch (error) {
            console.error('Failed to fetch schedules:', error);
            setError('Failed to load schedules');
        }
    }, [doctor?.doctorId, pageSize, startDate, endDate]);

    // Initialize when modal opens
    useEffect(() => {
        if (show && doctor) {
            setCurrentPage(1);
            setStartDate('');
            setEndDate('');
        }
    }, [show, doctor]);

    // Fetch schedules when currentPage, or dates change
    useEffect(() => {
        if (show && doctor) {
            fetchSchedules(currentPage);
        }
    }, [currentPage, startDate, endDate, show, doctor, fetchSchedules]);

    // Auto-reset to page 1 when any date is selected (real-time filter with debounce)
    useEffect(() => {
        if (show && doctor && (startDate || endDate)) {
            // Clear previous timeout
            if (dateFilterTimeoutRef.current) {
                clearTimeout(dateFilterTimeoutRef.current);
            }

            // Set new timeout to avoid multiple fetches while user is still selecting
            dateFilterTimeoutRef.current = setTimeout(() => {
                setCurrentPage(1);
            }, 500);
        }

        return () => {
            if (dateFilterTimeoutRef.current) {
                clearTimeout(dateFilterTimeoutRef.current);
            }
        };
    }, [startDate, endDate, show, doctor]);

    const handleClearFilter = () => {
        setStartDate('');
        setEndDate('');
        setCurrentPage(1);
        setError('');
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
            setShowDeleteModal(false);
            setSelectedSchedule(null);
            fetchSchedules(currentPage);
        } catch (error) {
            console.error('Failed to delete schedule:', error);
            setError(error.response?.data?.message || 'Failed to delete schedule');
        }
    };

    // Pagination helper
    const getPageNumbers = () => {
        if (!data) return [];
        const totalPages = data.totalPages;
        const pages = [];

        pages.push(1);
        const startPage = Math.max(2, currentPage - 1);
        const endPage = Math.min(totalPages - 1, currentPage + 1);

        if (startPage > 2) {
            pages.push('...');
        }

        for (let i = startPage; i <= endPage; i++) {
            pages.push(i);
        }

        if (endPage < totalPages - 1) {
            pages.push('...');
        }

        if (totalPages > 1) {
            pages.push(totalPages);
        }

        return pages;
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

                    {/* Date Filter Controls */}
                    {(startDate || endDate) && (
                        <div className="alert alert-info d-flex justify-content-between align-items-center mb-3">
                            <div>
                                <strong>Filtering schedules:</strong>
                                {startDate && !endDate && <span> On or after {new Date(startDate).toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' })}</span>}
                                {endDate && !startDate && <span> On or before {new Date(endDate).toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' })}</span>}
                                {startDate && endDate && (
                                    <span> From {new Date(startDate).toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' })} to {new Date(endDate).toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' })}</span>
                                )}
                            </div>
                            <Button
                                variant="outline-info"
                                size="sm"
                                onClick={handleClearFilter}
                            >
                                Clear Filter
                            </Button>
                        </div>
                    )}
                    <div className="mb-4">
                        <Row className="g-3">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-semibold">Start Date</Form.Label>
                                    <Form.Control
                                        type="datetime-local"
                                        value={startDate}
                                        onChange={(e) => setStartDate(e.target.value)}
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-semibold">End Date</Form.Label>
                                    <Form.Control
                                        type="datetime-local"
                                        value={endDate}
                                        onChange={(e) => setEndDate(e.target.value)}
                                    />
                                </Form.Group>
                            </Col>
                        </Row>
                    </div>

                    {!data ? (
                        <div className="text-center py-4">
                            <div className="spinner-border" role="status">
                                <span className="visually-hidden">Loading...</span>
                            </div>
                            <p className="mt-3 text-muted">Loading schedules...</p>
                        </div>
                    ) : (
                        <>
                            {/* Result counter */}
                            <div className="mb-3 text-muted small">
                                Showing {data.content.length > 0 ? (currentPage - 1) * pageSize + 1 : 0}-
                                {Math.min(currentPage * pageSize, data.totalElements)} of {data.totalElements} schedules
                            </div>

                            {data.content.length === 0 ? (
                                <Alert variant="info">
                                    No schedule blocks found. Click "Add Schedule Block" to create one.
                                </Alert>
                            ) : (
                                <>
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
                                            {data.content.map((schedule) => (
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

                                    {/* Pagination Controls */}
                                    {data.totalPages > 1 && (
                                        <div className="d-flex justify-content-center align-items-center gap-2 mt-4">
                                            <Button
                                                variant="outline-secondary"
                                                size="sm"
                                                onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                                                disabled={!data.hasPreviousPage}
                                            >
                                                ← Previous
                                            </Button>

                                            <div className="d-flex gap-1">
                                                {getPageNumbers().map((pageNum, idx) => (
                                                    <React.Fragment key={idx}>
                                                        {pageNum === '...' ? (
                                                            <span className="px-2">...</span>
                                                        ) : (
                                                            <Button
                                                                variant={pageNum === currentPage ? 'primary' : 'outline-primary'}
                                                                size="sm"
                                                                onClick={() => setCurrentPage(pageNum)}
                                                            >
                                                                {pageNum}
                                                            </Button>
                                                        )}
                                                    </React.Fragment>
                                                ))}
                                            </div>

                                            <Button
                                                variant="outline-secondary"
                                                size="sm"
                                                onClick={() => setCurrentPage(prev => Math.min(data.totalPages, prev + 1))}
                                                disabled={!data.hasNextPage}
                                            >
                                                Next →
                                            </Button>
                                        </div>
                                    )}
                                </>
                            )}
                        </>
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
                onScheduleAdded={() => fetchSchedules(currentPage)}
                doctor={doctor}
            />

            {selectedSchedule && (
                <EditScheduleModal
                    show={showEditModal}
                    onHide={() => setShowEditModal(false)}
                    onScheduleUpdated={() => fetchSchedules(currentPage)}
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
