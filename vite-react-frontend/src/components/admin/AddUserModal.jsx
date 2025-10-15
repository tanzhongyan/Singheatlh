import React, { useState } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import apiClient from '../../api/apiClient';

const AddUserModal = ({ show, onHide, onUserAdded }) => {
    const [name, setName] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('PATIENT');
    const [clinicId, setClinicId] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();

        // ** 1. MOCK SUPABASE USER CREATION **
        // In a real application, you would call Supabase here to create the user.
        // const { data: supabaseUser, error: supabaseError } = await supabase.auth.signUp({ email, password });
        // if (supabaseError) { /* handle error */ return; }
        // For this example, we'll generate a fake Supabase UID.
        const fakeSupabaseUid = `fake-uid-${Date.now()}`;

        // ** 2. CALL YOUR BACKEND **
        const userData = {
            id: fakeSupabaseUid,
            name,
            username,
            email,
            role,
            clinicId: role === 'CLINIC_STAFF' ? clinicId : null
        };

        try {
            if (role === 'PATIENT') {
                await apiClient.post('/api/system-administrators/users/patient', userData);
            } else if (role === 'CLINIC_STAFF') {
                await apiClient.post('/api/system-administrators/users/staff', userData);
            }
            onUserAdded(); // Callback to refresh the user list
            onHide(); // Close the modal
        } catch (error) {
            console.error('Failed to add user:', error);
            // TODO: Show an error message to the user
        }
    };

    return (
        <Modal show={show} onHide={onHide}>
            <Modal.Header closeButton>
                <Modal.Title>Add New User</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Name</Form.Label>
                        <Form.Control type="text" value={name} onChange={e => setName(e.target.value)} required />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Username</Form.Label>
                        <Form.Control type="text" value={username} onChange={e => setUsername(e.target.value)} required />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Email</Form.Label>
                        <Form.Control type="email" value={email} onChange={e => setEmail(e.target.value)} required />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Password</Form.Label>
                        <Form.Control type="password" value={password} onChange={e => setPassword(e.target.value)} required />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Role</Form.Label>
                        <Form.Select value={role} onChange={e => setRole(e.target.value)}>
                            <option value="PATIENT">Patient</option>
                            <option value="CLINIC_STAFF">Clinic Staff</option>
                        </Form.Select>
                    </Form.Group>
                    {role === 'CLINIC_STAFF' && (
                        <Form.Group className="mb-3">
                            <Form.Label>Clinic ID</Form.Label>
                            <Form.Control type="number" value={clinicId} onChange={e => setClinicId(e.target.value)} required />
                        </Form.Group>
                    )}
                    <Button variant="primary" type="submit">
                        Add User
                    </Button>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default AddUserModal;
