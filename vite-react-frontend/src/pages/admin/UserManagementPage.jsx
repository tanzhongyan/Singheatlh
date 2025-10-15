import React, { useState, useEffect } from 'react';
import { Table, Button, Modal } from 'react-bootstrap';
import apiClient from '../../api/apiClient';
import AddUserModal from '../../components/admin/AddUserModal';

const UserManagementPage = () => {
    const [users, setUsers] = useState([]);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);
    const [showAddModal, setShowAddModal] = useState(false);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            const response = await apiClient.get('/api/system-administrators/users');
            setUsers(response.data);
        } catch (error) {
            console.error('Failed to fetch users:', error);
        }
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
            await apiClient.delete(`/api/system-administrators/users/${userToDelete.id}`);
            fetchUsers(); // Refresh the list
            handleCloseDeleteModal();
        } catch (error) {
            console.error('Failed to delete user:', error);
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
            <Table striped bordered hover>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.id}</td>
                            <td>{user.name}</td>
                            <td>{user.email}</td>
                            <td>{user.role}</td>
                            <td>
                                <Button variant="primary" size="sm" className="me-2" onClick={() => handleEditClick(user)}>Edit</Button>
                                <Button variant="danger" size="sm" onClick={() => handleDeleteClick(user)}>Delete</Button>
                            </td>
                        </tr>
                    ))}
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
        </div>
    );
};

export default UserManagementPage;
