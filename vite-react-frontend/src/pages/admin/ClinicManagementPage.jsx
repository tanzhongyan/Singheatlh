import React, { useState, useEffect } from 'react';
import { Row, Col, Button } from 'react-bootstrap';
import apiClient from '../../api/apiClient';
import ClinicCard from '../../components/admin/ClinicCard';
import AddClinicModal from '../../components/admin/AddClinicModal';

const ClinicManagementPage = () => {
    const [clinics, setClinics] = useState([]);
    const [showAddModal, setShowAddModal] = useState(false);

    useEffect(() => {
        fetchClinics();
    }, []);

    const fetchClinics = async () => {
        try {
            const response = await apiClient.get('/api/system-administrators/clinics');
            setClinics(response.data);
        } catch (error) {
            console.error('Failed to fetch clinics:', error);
        }
    };

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1>Clinic Management</h1>
                <Button variant="primary" onClick={() => setShowAddModal(true)}>
                    Add Clinic
                </Button>
            </div>

            <Row xs={1} md={2} lg={3} className="g-4">
                {clinics.map(clinic => (
                    <Col key={clinic.clinicId}>
                        <ClinicCard clinic={clinic} onClinicUpdated={fetchClinics} />
                    </Col>
                ))}
            </Row>

            <AddClinicModal
                show={showAddModal}
                onHide={() => setShowAddModal(false)}
                onClinicAdded={fetchClinics}
            />
        </div>
    );
};

export default ClinicManagementPage;
