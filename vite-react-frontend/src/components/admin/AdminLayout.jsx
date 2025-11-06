import React from 'react';
import { Outlet } from 'react-router-dom';
import { Container } from 'react-bootstrap';

const AdminLayout = () => {
    return (
        <Container className="mt-4">
            <Outlet />
        </Container>
    );
};

export default AdminLayout;
