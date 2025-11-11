import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Table, Button, Modal, Form, Row, Col } from 'react-bootstrap';
import apiClient from '../../api/apiClient';
import AddUserModal from '../../components/admin/AddUserModal';
import EditUserModal from '../../components/admin/EditUserModal';
import { useAuth } from '../../contexts/AuthContext';

const UserManagementPage = () => {
    const { user: currentUser } = useAuth();
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);
    const [showAddModal, setShowAddModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [userToEdit, setUserToEdit] = useState(null);
    const [searchText, setSearchText] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize] = useState(10);
    const searchTimeoutRef = useRef(null);

    useEffect(() => {
        fetchUsers(currentPage, searchText, roleFilter);
    }, [currentPage, searchText, roleFilter]);

    const fetchUsers = useCallback(async (page, search, role) => {
        setError(null);
        try {
            const params = new URLSearchParams({
                page,
                pageSize,
                ...(search && { search }),
                ...(role && { role }),
            });

            const response = await apiClient.get(`/api/system-administrators/users/paginated?${params.toString()}`);
            setData(response.data);
        } catch (err) {
            setError(err.message || 'Failed to fetch users');
            console.error('Failed to fetch users:', err);
        }
    }, [pageSize]);

    const handleSearchChange = (e) => {
        const value = e.target.value;
        setSearchText(value);
        setCurrentPage(1); // Reset to first page on search

        // Debounce search
        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }
        searchTimeoutRef.current = setTimeout(() => {
            fetchUsers(1, value, roleFilter);
        }, 500);
    };

    const handleRoleFilterChange = (e) => {
        const value = e.target.value;
        setRoleFilter(value);
        setCurrentPage(1); // Reset to first page on filter change
    };

    const handleEditClick = (user) => {
        setUserToEdit(user);
        setShowEditModal(true);
    };

    const handleDeleteClick = (user) => {
        setUserToDelete(user);
        setShowDeleteModal(true);
    };

    const handleCloseDeleteModal = () => {
        setShowDeleteModal(false);
        setUserToDelete(null);
    };

    const handleConfirmDelete = async () => {
        if (!userToDelete) return;

        try {
            await apiClient.delete(`/api/system-administrators/users/${userToDelete.userId}`);
            handleCloseDeleteModal();
            fetchUsers(currentPage, searchText, roleFilter);
        } catch (error) {
            console.error('Failed to delete user:', error);
            alert('Failed to delete user. Please try again.');
        }
    };

    // Pagination helper
    const getPageNumbers = () => {
        if (!data) return [];
        const totalPages = data.totalPages;
        const pages = [];

        // Always show first page
        pages.push(1);

        // Show pages around current page
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

        // Always show last page if more than 1
        if (totalPages > 1) {
            pages.push(totalPages);
        }

        return pages;
    };

    const users = data?.content || [];
    const pageNumbers = getPageNumbers();

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1>User Management</h1>
                <Button variant="primary" onClick={() => setShowAddModal(true)}>
                    Add User
                </Button>
            </div>

            {/* Filter and Search Controls */}
            <div className="card mb-4 p-3">
                <Row className="g-3">
                    <Col md={6}>
                        <Form.Group>
                            <Form.Label className="small fw-semibold">Search by Name or Email</Form.Label>
                            <Form.Control
                                type="text"
                                placeholder="Search... (updates as you type)"
                                value={searchText}
                                onChange={handleSearchChange}
                            />
                        </Form.Group>
                    </Col>
                    <Col md={6}>
                        <Form.Group>
                            <Form.Label className="small fw-semibold">Filter by Role</Form.Label>
                            <Form.Select
                                value={roleFilter}
                                onChange={handleRoleFilterChange}
                            >
                                <option value="">All Roles</option>
                                <option value="P">Patient</option>
                                <option value="C">Clinic Staff</option>
                                <option value="S">System Admin</option>
                            </Form.Select>
                        </Form.Group>
                    </Col>
                </Row>
            </div>

            {/* Error State */}
            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            {/* Users Table */}
            {data && (
                <>
                    {/* Result counter */}
                    {data && (
                        <div className="mb-3 text-muted small">
                            Showing {users.length > 0 ? (currentPage - 1) * pageSize + 1 : 0}-
                            {Math.min(currentPage * pageSize, data.totalElements)} of {data.totalElements} users
                        </div>
                    )}

                    <Table striped bordered hover style={{ tableLayout: 'auto' }}>
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th style={{ width: '140px' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.length > 0 ? (
                                users.map(user => (
                                    <tr key={user.userId}>
                                        <td style={{ wordBreak: 'break-word' }}>{user.name}</td>
                                        <td style={{ wordBreak: 'break-word' }}>{user.email}</td>
                                        <td>{user.role === 'P' ? 'Patient' : user.role === 'C' ? 'Clinic Staff' : 'System Admin'}</td>
                                        <td>
                                            <Button variant="primary" size="sm" className="me-2" onClick={() => handleEditClick(user)}>Edit</Button>
                                            <Button
                                                variant="danger"
                                                size="sm"
                                                onClick={() => handleDeleteClick(user)}
                                                disabled={currentUser?.id === user.userId}
                                                title={currentUser?.id === user.userId ? "You cannot delete your own account" : ""}
                                            >
                                                Delete
                                            </Button>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="4" className="text-center text-muted py-4">
                                        No users found matching your filters.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </Table>

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
            )}

            <Modal show={showDeleteModal} onHide={handleCloseDeleteModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Confirm Deletion</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Are you sure you want to delete the user <strong>{userToDelete?.name}</strong>?
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleCloseDeleteModal}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={handleConfirmDelete}>
                        Delete
                    </Button>
                </Modal.Footer>
            </Modal>

            <AddUserModal
                show={showAddModal}
                onHide={() => setShowAddModal(false)}
                onUserAdded={() => {
                    setCurrentPage(1);
                    fetchUsers(1, searchText, roleFilter);
                }}
            />

            <EditUserModal
                show={showEditModal}
                onHide={() => setShowEditModal(false)}
                onUserUpdated={() => fetchUsers(currentPage, searchText, roleFilter)}
                user={userToEdit}
            />
        </div>
    );
};

export default UserManagementPage;
