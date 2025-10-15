import React, { useState, useEffect } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const EditUserModal = ({ show, onHide, onUserUpdated, user }) => {
    const [name, setName] = useState('');
    const [role, setRole] = useState('PATIENT');

    useEffect(() => {
        if (user) {
            setName(user.name || '');
            setRole(user.role || 'PATIENT');
        }
    }, [user]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!user) return;

        const updatedUserData = {
            ...user,
            name,
            role,
        };

        try {
            await apiClient.put(`/api/system-administrators/users/${user.id}`, updatedUserData);
            onUserUpdated();
            onHide();
        } catch (error) {
            console.error('Failed to update user:', error);
        }
    };

    return (
        <Modal show={show} onHide={onHide}>
            <Modal.Header closeButton>
                <Modal.Title>Edit User</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Name</Form.Label>
                        <Form.Control type="text" value={name} onChange={e => setName(e.target.value)} required />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Email (read-only)</Form.Label>
                        <Form.Control type="email" value={user?.email || ''} readOnly />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Role</Form.Label>
                        <Form.Select value={role} onChange={e => setRole(e.target.value)}>
                            <option value="PATIENT">Patient</option>
                            <option value="CLINIC_STAFF">Clinic Staff</option>
                            <option value="SYSTEM_ADMINISTRATOR">System Administrator</option>
                        </Form.Select>
                    </Form.Group>
                    <Button variant="primary" type="submit">
                        Save Changes
                    </Button>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default EditUserModal;
