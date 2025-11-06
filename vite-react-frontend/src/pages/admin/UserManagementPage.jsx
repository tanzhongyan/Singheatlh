import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Row, Col } from 'react-bootstrap';
import apiClient from '../../api/apiClient';
import AddUserModal from '../../components/admin/AddUserModal';
import EditUserModal from '../../components/admin/EditUserModal';
import { useAuth } from '../../contexts/AuthContext';

const UserManagementPage = () => {
    const { user: currentUser } = useAuth();
    const [users, setUsers] = useState([]);
    const [filteredUsers, setFilteredUsers] = useState([]);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);
    const [showAddModal, setShowAddModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [userToEdit, setUserToEdit] = useState(null);
    const [searchText, setSearchText] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [sortBy, setSortBy] = useState('name');
    const [sortOrder, setSortOrder] = useState('asc');

    useEffect(() => {
        fetchUsers();
    }, []);

    useEffect(() => {
        // Filter and sort users
        let result = [...users];

        // Apply search filter
        if (searchText.trim()) {
            const searchLower = searchText.toLowerCase();
            result = result.filter(user =>
                user.name.toLowerCase().includes(searchLower) ||
                user.email.toLowerCase().includes(searchLower)
            );
        }

        // Apply role filter
        if (roleFilter) {
            result = result.filter(user => user.role === roleFilter);
        }

        // Apply sorting
        result.sort((a, b) => {
            let aValue, bValue;

            switch (sortBy) {
                case 'name':
                    aValue = a.name.toLowerCase();
                    bValue = b.name.toLowerCase();
                    break;
                case 'email':
                    aValue = a.email.toLowerCase();
                    bValue = b.email.toLowerCase();
                    break;
                case 'role':
                    aValue = a.role;
                    bValue = b.role;
                    break;
                default:
                    aValue = a.name.toLowerCase();
                    bValue = b.name.toLowerCase();
            }

            if (aValue < bValue) {
                return sortOrder === 'asc' ? -1 : 1;
            }
            if (aValue > bValue) {
                return sortOrder === 'asc' ? 1 : -1;
            }
            return 0;
        });

        setFilteredUsers(result);
    }, [users, searchText, roleFilter, sortBy, sortOrder]);

    const fetchUsers = async () => {
        try {
            const response = await apiClient.get('/api/system-administrators/users');
            setUsers(response.data);
        } catch (error) {
            console.error('Failed to fetch users:', error);
        }
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
            fetchUsers(); // Refresh the list
            handleCloseDeleteModal();
        } catch (error) {
            console.error('Failed to delete user:', error);
            alert('Failed to delete user. Please try again.');
        }
    };

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
                    <Col md={4}>
                        <Form.Group>
                            <Form.Label className="small fw-semibold">Search by Name or Email</Form.Label>
                            <Form.Control
                                type="text"
                                placeholder="Search..."
                                value={searchText}
                                onChange={(e) => setSearchText(e.target.value)}
                            />
                        </Form.Group>
                    </Col>
                    <Col md={3}>
                        <Form.Group>
                            <Form.Label className="small fw-semibold">Filter by Role</Form.Label>
                            <Form.Select
                                value={roleFilter}
                                onChange={(e) => setRoleFilter(e.target.value)}
                            >
                                <option value="">All Roles</option>
                                <option value="P">Patient</option>
                                <option value="C">Clinic Staff</option>
                                <option value="S">System Admin</option>
                            </Form.Select>
                        </Form.Group>
                    </Col>
                    <Col md={3}>
                        <Form.Group>
                            <Form.Label className="small fw-semibold">Sort By</Form.Label>
                            <Form.Select
                                value={sortBy}
                                onChange={(e) => setSortBy(e.target.value)}
                            >
                                <option value="name">Name</option>
                                <option value="email">Email</option>
                                <option value="role">Role</option>
                            </Form.Select>
                        </Form.Group>
                    </Col>
                    <Col md={2}>
                        <Form.Group>
                            <Form.Label className="small fw-semibold">Order</Form.Label>
                            <Form.Select
                                value={sortOrder}
                                onChange={(e) => setSortOrder(e.target.value)}
                            >
                                <option value="asc">Ascending</option>
                                <option value="desc">Descending</option>
                            </Form.Select>
                        </Form.Group>
                    </Col>
                </Row>
            </div>

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
                    {filteredUsers.length > 0 ? (
                        filteredUsers.map(user => (
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
                onUserAdded={fetchUsers}
            />

            <EditUserModal
                show={showEditModal}
                onHide={() => setShowEditModal(false)}
                onUserUpdated={fetchUsers}
                user={userToEdit}
            />
        </div>
    );
};

export default UserManagementPage;
