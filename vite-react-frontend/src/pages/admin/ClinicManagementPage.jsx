import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Row, Col, Button, Form } from 'react-bootstrap';
import apiClient from '../../api/apiClient';
import ClinicCard from '../../components/admin/ClinicCard';
import AddClinicModal from '../../components/admin/AddClinicModal';

const ClinicManagementPage = () => {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [showAddModal, setShowAddModal] = useState(false);
    const [searchText, setSearchText] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize] = useState(12);
    const searchTimeoutRef = useRef(null);

    useEffect(() => {
        fetchClinics(currentPage, searchText);
    }, [currentPage, searchText]);

    const fetchClinics = useCallback(async (page, search) => {
        setError(null);
        try {
            const params = new URLSearchParams({
                page,
                pageSize,
                ...(search && { search }),
            });

            const response = await apiClient.get(
                `/api/system-administrators/clinics/paginated?${params.toString()}`
            );
            setData(response.data);
        } catch (err) {
            setError(err.message || 'Failed to fetch clinics');
            console.error('Failed to fetch clinics:', err);
        }
    }, [pageSize]);

    const handleSearchChange = (e) => {
        const value = e.target.value;
        setSearchText(value);
        setCurrentPage(1);

        // Debounce search
        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }
        searchTimeoutRef.current = setTimeout(() => {
            fetchClinics(1, value);
        }, 500);
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

    const clinics = data?.content || [];
    const pageNumbers = getPageNumbers();

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1>Clinic Management</h1>
                <Button variant="primary" onClick={() => setShowAddModal(true)}>
                    Add Clinic
                </Button>
            </div>

            {/* Search Control */}
            <div className="card mb-4 p-3">
                <Form.Group>
                    <Form.Label className="small fw-semibold">Search by Clinic Name or Address</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="Search... (updates as you type)"
                        value={searchText}
                        onChange={handleSearchChange}
                    />
                </Form.Group>
            </div>

            {/* Error State */}
            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            {/* Clinics Grid */}
            {data && (
                <>
                    {/* Result counter */}
                    {data && (
                        <div className="mb-3 text-muted small">
                            Showing {clinics.length > 0 ? (currentPage - 1) * pageSize + 1 : 0}-
                            {Math.min(currentPage * pageSize, data.totalElements)} of {data.totalElements} clinics
                        </div>
                    )}

                    {clinics.length > 0 ? (
                        <>
                            <Row xs={1} md={2} lg={3} className="g-4">
                                {clinics.map(clinic => (
                                    <Col key={clinic.clinicId}>
                                        <ClinicCard
                                            clinic={clinic}
                                            onClinicUpdated={() => fetchClinics(currentPage, searchText)}
                                        />
                                    </Col>
                                ))}
                            </Row>

                            {/* Pagination Controls */}
                            {data && data.totalPages > 1 && (
                                <div className="d-flex justify-content-center align-items-center gap-2 mt-4 mb-3">
                                    <Button
                                        variant="outline-secondary"
                                        size="sm"
                                        onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                                        disabled={!data.hasPreviousPage}
                                    >
                                        ← Previous
                                    </Button>

                                    <div className="d-flex gap-1">
                                        {pageNumbers.map((pageNum, idx) => (
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
                    ) : (
                        <div className="alert alert-info" role="alert">
                            No clinics found matching your search.
                        </div>
                    )}
                </>
            )}

            <AddClinicModal
                show={showAddModal}
                onHide={() => setShowAddModal(false)}
                onClinicAdded={() => {
                    setCurrentPage(1);
                    fetchClinics(1, searchText);
                }}
            />
        </div>
    );
};

export default ClinicManagementPage;
