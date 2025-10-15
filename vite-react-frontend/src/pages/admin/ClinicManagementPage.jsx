import React, { useState, useEffect } from 'react';
import { Row, Col, Form, Button, Accordion } from 'react-bootstrap';
import apiClient from '../../api/apiClient';
import ClinicCard from '../../components/admin/ClinicCard';

const ClinicManagementPage = () => {
    const [clinics, setClinics] = useState([]);
    const [importJson, setImportJson] = useState('');

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

    const handleImport = async () => {
        try {
            const clinicsToImport = JSON.parse(importJson);
            await apiClient.post('/api/system-administrators/clinics/import', clinicsToImport);
            setImportJson('');
            fetchClinics();
            alert('Clinics imported successfully!');
        } catch (error) {
            console.error('Failed to import clinics:', error);
            alert('Failed to import clinics. Please check the JSON format.');
        }
    };

    return (
        <div>
            <h1>Clinic Management</h1>

            <Accordion className="mb-4">
                <Accordion.Item eventKey="0">
                    <Accordion.Header>Bulk Import Clinics</Accordion.Header>
                    <Accordion.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Paste Clinic JSON Array</Form.Label>
                            <Form.Control 
                                as="textarea" 
                                rows={10}
                                value={importJson}
                                onChange={e => setImportJson(e.target.value)}
                                placeholder='[{"name": "Clinic A", "address": "123 Main St", ...}]'
                            />
                        </Form.Group>
                        <Button variant="success" onClick={handleImport}>Import</Button>
                    </Accordion.Body>
                </Accordion.Item>
            </Accordion>

            <h2>Existing Clinics</h2>
            <Row xs={1} md={2} lg={3} className="g-4">
                {clinics.map(clinic => (
                    <Col key={clinic.id}>
                        <ClinicCard clinic={clinic} />
                    </Col>
                ))}
            </Row>
        </div>
    );
};

export default ClinicManagementPage;
