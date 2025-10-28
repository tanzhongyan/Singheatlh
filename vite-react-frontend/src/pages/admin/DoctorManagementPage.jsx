import React, { useState, useEffect } from "react";
import { Table, Button, Modal } from "react-bootstrap";
import apiClient from "../../api/apiClient";
import AddDoctorModal from "../../components/admin/AddDoctorModal";
import EditDoctorModal from "../../components/admin/EditDoctorModal";

const DoctorManagementPage = () => {
  const [doctors, setDoctors] = useState([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState(null);

  useEffect(() => {
    fetchDoctors();
  }, []);

  const fetchDoctors = async () => {
    try {
      const response = await apiClient.get(
        "/api/system-administrators/doctors"
      );
      setDoctors(response.data);
    } catch (error) {
      console.error("Failed to fetch doctors:", error);
    }
  };

  const handleEditClick = (doctor) => {
    setSelectedDoctor(doctor);
    setShowEditModal(true);
  };

  const handleDeleteClick = (doctor) => {
    setSelectedDoctor(doctor);
    setShowDeleteModal(true);
  };

  const handleConfirmDelete = async () => {
    if (!selectedDoctor) return;
    try {
      await apiClient.delete(
        `/api/system-administrators/doctors/${selectedDoctor.id}`
      );
      fetchDoctors();
      setShowDeleteModal(false);
      setSelectedDoctor(null);
    } catch (error) {
      console.error("Failed to delete doctor:", error);
    }
  };

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1>Doctor Management</h1>
        <Button variant="primary" onClick={() => setShowAddModal(true)}>
          Add Doctor
        </Button>
      </div>
      <Table striped bordered hover>
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Schedule</th>
            <th>Clinic ID</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {doctors.map((doctor) => (
            <tr key={doctor.id}>
              <td>{doctor.id}</td>
              <td>{doctor.name}</td>
              <td>{doctor.schedule}</td>
              <td>{doctor.clinicId}</td>
              <td>
                <Button
                  variant="primary"
                  size="sm"
                  className="me-2"
                  onClick={() => handleEditClick(doctor)}
                >
                  Edit
                </Button>
                <Button
                  variant="danger"
                  size="sm"
                  onClick={() => handleDeleteClick(doctor)}
                >
                  Delete
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>

      <AddDoctorModal
        show={showAddModal}
        onHide={() => setShowAddModal(false)}
        onDoctorAdded={fetchDoctors}
      />

      {selectedDoctor && (
        <EditDoctorModal
          show={showEditModal}
          onHide={() => setShowEditModal(false)}
          onDoctorUpdated={fetchDoctors}
          doctor={selectedDoctor}
        />
      )}

      <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Confirm Deletion</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Are you sure you want to delete the doctor{" "}
          <strong>{selectedDoctor?.name}</strong>?
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
            Cancel
          </Button>
          <Button variant="danger" onClick={handleConfirmDelete}>
            Delete
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default DoctorManagementPage;
