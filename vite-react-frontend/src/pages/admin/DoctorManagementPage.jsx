import React, { useState, useEffect } from "react";
import { Table, Button, Modal, Form, Row, Col } from "react-bootstrap";
import apiClient from "../../api/apiClient";
import AddDoctorModal from "../../components/admin/AddDoctorModal";
import EditDoctorModal from "../../components/admin/EditDoctorModal";
import DoctorScheduleModal from "../../components/admin/DoctorScheduleModal";

const DoctorManagementPage = () => {
  const [doctors, setDoctors] = useState([]);
  const [filteredDoctors, setFilteredDoctors] = useState([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [searchText, setSearchText] = useState("");
  const [sortBy, setSortBy] = useState("name");
  const [sortOrder, setSortOrder] = useState("asc");

  useEffect(() => {
    fetchDoctors();
  }, []);

  useEffect(() => {
    // Filter and sort doctors
    let result = [...doctors];

    // Apply search filter
    if (searchText.trim()) {
      const searchLower = searchText.toLowerCase();
      result = result.filter(
        (doctor) =>
          doctor.name.toLowerCase().includes(searchLower) ||
          (doctor.clinicName && doctor.clinicName.toLowerCase().includes(searchLower))
      );
    }

    // Apply sorting
    result.sort((a, b) => {
      let aValue, bValue;

      switch (sortBy) {
        case "name":
          aValue = a.name.toLowerCase();
          bValue = b.name.toLowerCase();
          break;
        case "clinic":
          aValue = (a.clinicName || "").toLowerCase();
          bValue = (b.clinicName || "").toLowerCase();
          break;
        case "duration":
          aValue = a.appointmentDurationInMinutes;
          bValue = b.appointmentDurationInMinutes;
          break;
        default:
          aValue = a.name.toLowerCase();
          bValue = b.name.toLowerCase();
      }

      if (aValue < bValue) {
        return sortOrder === "asc" ? -1 : 1;
      }
      if (aValue > bValue) {
        return sortOrder === "asc" ? 1 : -1;
      }
      return 0;
    });

    setFilteredDoctors(result);
  }, [doctors, searchText, sortBy, sortOrder]);

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

  const handleScheduleClick = (doctor) => {
    setSelectedDoctor(doctor);
    setShowScheduleModal(true);
  };

  const handleConfirmDelete = async () => {
    if (!selectedDoctor) return;
    try {
      await apiClient.delete(
        `/api/system-administrators/doctors/${selectedDoctor.doctorId}`
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

      {/* Filter and Search Controls */}
      <div className="card mb-4 p-3">
        <Row className="g-3">
          <Col md={5}>
            <Form.Group>
              <Form.Label className="small fw-semibold">Search by Doctor Name or Clinic</Form.Label>
              <Form.Control
                type="text"
                placeholder="Search..."
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
              />
            </Form.Group>
          </Col>
          <Col md={4}>
            <Form.Group>
              <Form.Label className="small fw-semibold">Sort By</Form.Label>
              <Form.Select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
              >
                <option value="name">Doctor Name</option>
                <option value="clinic">Clinic Name</option>
                <option value="duration">Appointment Duration</option>
              </Form.Select>
            </Form.Group>
          </Col>
          <Col md={3}>
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
            <th>Clinic</th>
            <th style={{ width: '100px' }}>Duration (min)</th>
            <th style={{ width: '200px' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {filteredDoctors.length > 0 ? (
            filteredDoctors.map((doctor) => (
              <tr key={doctor.doctorId}>
                <td style={{ wordBreak: 'break-word' }}>{doctor.name}</td>
                <td style={{ wordBreak: 'break-word' }}>{doctor.clinicName || `Clinic #${doctor.clinicId}`}</td>
                <td>{doctor.appointmentDurationInMinutes}</td>
                <td>
                  <Button
                    variant="info"
                    size="sm"
                    className="me-2"
                    onClick={() => handleScheduleClick(doctor)}
                  >
                    Schedule
                  </Button>
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
            ))
          ) : (
            <tr>
              <td colSpan="4" className="text-center text-muted py-4">
                No doctors found matching your filters.
              </td>
            </tr>
          )}
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

      {selectedDoctor && (
        <DoctorScheduleModal
          show={showScheduleModal}
          onHide={() => setShowScheduleModal(false)}
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
