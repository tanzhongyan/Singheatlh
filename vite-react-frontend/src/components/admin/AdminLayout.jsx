import React from 'react';
import { Link, Outlet } from 'react-router-dom';
import { Container, Nav, Navbar, NavDropdown } from 'react-bootstrap';

const AdminLayout = () => {
    return (
        <div>
            <Navbar bg="dark" variant="dark" expand="lg">
                <Container>
                    <Navbar.Brand as={Link} to="/admin">Admin Dashboard</Navbar.Brand>
                    <Navbar.Toggle aria-controls="basic-navbar-nav" />
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="me-auto">
                            <Nav.Link as={Link} to="/admin/users">User Management</Nav.Link>
                            <Nav.Link as={Link} to="/admin/doctors">Doctor Management</Nav.Link>
                            <Nav.Link as={Link} to="/admin/clinics">Clinic Management</Nav.Link>
                        </Nav>
                    </Navbar.Collapse>
                </Container>
            </Navbar>
            <Container className="mt-4">
                <Outlet />
            </Container>
        </div>
    );
};

export default AdminLayout;
