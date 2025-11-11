import React, { useState, useEffect, useCallback, useRef } from "react";
import { Table, Button, Modal, Form, Row, Col } from "react-bootstrap";
import apiClient from "../../api/apiClient";
import AddDoctorModal from "../../components/admin/AddDoctorModal";
import EditDoctorModal from "../../components/admin/EditDoctorModal";
import DoctorScheduleModal from "../../components/admin/DoctorScheduleModal";

const DoctorManagementPage = () => {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [searchText, setSearchText] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);
  const searchTimeoutRef = useRef(null);

  useEffect(() => {
    fetchDoctors(currentPage, searchText);
  }, [currentPage, searchText]);

  const fetchDoctors = useCallback(async (page, search) => {
    setError(null);
    try {
      const params = new URLSearchParams({
        page,
        pageSize,
        ...(search && { search }),
      });

      const response = await apiClient.get(
        `/api/system-administrators/doctors/paginated?${params.toString()}`
      );
      setData(response.data);
    } catch (err) {
      setError(err.message || "Failed to fetch doctors");
      console.error("Failed to fetch doctors:", err);
    }
  }, [pageSize]);

  const handleSearchChange = (e) => {
    const value = e.target.value;
    setSearchText(value);
    setCurrentPage(1);

    // Debounce search
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    searchTimeoutRef.current = setTimeout(() => {
      fetchDoctors(1, value);
    }, 500);
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
      setShowDeleteModal(false);
      setSelectedDoctor(null);
      fetchDoctors(currentPage, searchText);
    } catch (error) {
      console.error("Failed to delete doctor:", error);
      alert("Failed to delete doctor. Please try again.");
    }
  };

  // Pagination helper
  const getPageNumbers = () => {
    if (!data) return [];
    const totalPages = data.totalPages;
    const pages = [];

    pages.push(1);
    const startPage = Math.max(2, currentPage - 1);
    const endPage = Math.min(totalPages - 1, currentPage + 1);

    if (startPage > 2) {
      pages.push("...");
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    if (endPage < totalPages - 1) {
      pages.push("...");
    }

    if (totalPages > 1) {
      pages.push(totalPages);
    }

    return pages;
  };

  const doctors = data?.content || [];
  const pageNumbers = getPageNumbers();

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
          <Col md={12}>
            <Form.Group>
              <Form.Label className="small fw-semibold">Search by Doctor Name or Clinic</Form.Label>
              <Form.Control
                type="text"
                placeholder="Search... (updates as you type)"
                value={searchText}
                onChange={handleSearchChange}
              />
            </Form.Group>
          </Col>
        </Row>
      </div>

      {/* Error State */}
      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {/* Doctors Table */}
      {data && (
        <>
          {/* Result counter */}
          {data && (
            <div className="mb-3 text-muted small">
              Showing {doctors.length > 0 ? (currentPage - 1) * pageSize + 1 : 0}-
              {Math.min(currentPage * pageSize, data.totalElements)} of {data.totalElements} doctors
            </div>
          )}

          <Table striped bordered hover style={{ tableLayout: "auto" }}>
            <thead>
              <tr>
                <th>Name</th>
                <th>Clinic</th>
                <th style={{ width: "100px" }}>Duration (min)</th>
                <th style={{ width: "280px" }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {doctors.length > 0 ? (
                doctors.map((doctor) => (
                  <tr key={doctor.doctorId}>
                    <td style={{ wordBreak: "break-word" }}>{doctor.name}</td>
                    <td style={{ wordBreak: "break-word" }}>
                      {doctor.clinicName || `Clinic #${doctor.clinicId}`}
                    </td>
                    <td>{doctor.appointmentDurationInMinutes}</td>
                    <td style={{ display: "flex", gap: "6px", flexWrap: "nowrap" }}>
                      <Button
                        variant="info"
                        size="sm"
                        onClick={() => handleScheduleClick(doctor)}
                        style={{ flex: "0 0 auto" }}
                      >
                        Schedule
                      </Button>
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => handleEditClick(doctor)}
                        style={{ flex: "0 0 auto" }}
                      >
                        Edit
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => handleDeleteClick(doctor)}
                        style={{ flex: "0 0 auto" }}
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

          {/* Pagination Controls */}
          {data && data.totalPages > 1 && (
            <div className="d-flex justify-content-center align-items-center gap-2 mt-4 mb-3">
              <Button
                variant="outline-secondary"
                size="sm"
                onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                disabled={!data.hasPreviousPage}
              >
                ← Previous
              </Button>

              <div className="d-flex gap-1">
                {pageNumbers.map((pageNum, idx) => (
                  <React.Fragment key={idx}>
                    {pageNum === "..." ? (
                      <span className="px-2">...</span>
                    ) : (
                      <Button
                        variant={pageNum === currentPage ? "primary" : "outline-primary"}
                        size="sm"
                        onClick={() => setCurrentPage(pageNum)}
                      >
                        {pageNum}
                      </Button>
                    )}
                  </React.Fragment>
                ))}
              </div>

              <Button
                variant="outline-secondary"
                size="sm"
                onClick={() => setCurrentPage(prev => Math.min(data.totalPages, prev + 1))}
                disabled={!data.hasNextPage}
              >
                Next →
              </Button>
            </div>
          )}
        </>
      )}

      <AddDoctorModal
        show={showAddModal}
        onHide={() => setShowAddModal(false)}
        onDoctorAdded={() => {
          setCurrentPage(1);
          fetchDoctors(1, searchText);
        }}
      />

      {selectedDoctor && (
        <EditDoctorModal
          show={showEditModal}
          onHide={() => setShowEditModal(false)}
          onDoctorUpdated={() => fetchDoctors(currentPage, searchText)}
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
