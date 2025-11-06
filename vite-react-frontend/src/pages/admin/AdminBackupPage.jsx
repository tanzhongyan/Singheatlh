import React, { useState, useEffect } from "react";
import { Card, Button, Table, Modal, Spinner, Alert } from "react-bootstrap";
import apiClient from "../../api/apiClient";

const AdminBackupPage = () => {
  const [backups, setBackups] = useState([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showRestoreModal, setShowRestoreModal] = useState(false);
  const [selectedBackup, setSelectedBackup] = useState(null);

  useEffect(() => {
    fetchBackupHistory();
  }, []);

  const fetchBackupHistory = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get("/api/system-administrators/backup/history");
      setBackups(response.data);
      setError("");
    } catch (err) {
      console.error("Failed to fetch backup history:", err);
      setError("Failed to load backup history");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBackup = async () => {
    try {
      setCreating(true);
      setError("");
      const response = await apiClient.post("/api/system-administrators/backup/create");
      setSuccess(`Backup created successfully! Size: ${formatBytes(response.data.sizeInBytes)}`);
      fetchBackupHistory();
      // Clear success message after 5 seconds
      setTimeout(() => setSuccess(""), 5000);
    } catch (err) {
      console.error("Failed to create backup:", err);
      setError(err.response?.data?.message || "Failed to create backup");
    } finally {
      setCreating(false);
    }
  };

  const handleDownloadBackup = async (backupId) => {
    try {
      const response = await apiClient.get(
        `/api/system-administrators/backup/download/${backupId}`,
        { responseType: "blob" }
      );
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `${backupId}.zip`);
      document.body.appendChild(link);
      link.click();
      link.parentElement.removeChild(link);
    } catch (err) {
      console.error("Failed to download backup:", err);
      setError("Failed to download backup");
    }
  };

  const handleDeleteClick = (backup) => {
    setSelectedBackup(backup);
    setShowDeleteModal(true);
  };

  const handleConfirmDelete = async () => {
    try {
      setError("");
      await apiClient.delete(`/api/system-administrators/backup/${selectedBackup.backupId}`);
      setSuccess("Backup deleted successfully!");
      setShowDeleteModal(false);
      setSelectedBackup(null);
      fetchBackupHistory();
      setTimeout(() => setSuccess(""), 5000);
    } catch (err) {
      console.error("Failed to delete backup:", err);
      setError("Failed to delete backup");
    }
  };

  const handleRestoreClick = (backup) => {
    setSelectedBackup(backup);
    setShowRestoreModal(true);
  };

  const handleConfirmRestore = async () => {
    try {
      setError("");
      await apiClient.post(`/api/system-administrators/backup/restore/${selectedBackup.backupId}`);
      setSuccess("Backup restored successfully!");
      setShowRestoreModal(false);
      setSelectedBackup(null);
      setTimeout(() => setSuccess(""), 5000);
    } catch (err) {
      console.error("Failed to restore backup:", err);
      setError("Failed to restore backup");
    }
  };

  const formatBytes = (bytes) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="row mb-4">
        <div className="col-12">
          <div className="card border-0 shadow-sm bg-primary text-white">
            <div className="card-body p-4">
              <h1 className="display-5 fw-bold mb-2">
                <i className="bi bi-cloud-arrow-down me-3"></i>
                System Backup & Restore
              </h1>
              <p className="lead mb-0 opacity-90">
                Manage system backups and restore data from previous backups
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Alerts */}
      {error && (
        <div className="alert alert-danger alert-dismissible fade show" role="alert">
          {error}
          <button
            type="button"
            className="btn-close"
            onClick={() => setError("")}
          ></button>
        </div>
      )}
      {success && (
        <div className="alert alert-success alert-dismissible fade show" role="alert">
          {success}
          <button
            type="button"
            className="btn-close"
            onClick={() => setSuccess("")}
          ></button>
        </div>
      )}

      {/* Create Backup Section */}
      <div className="row mb-4">
        <div className="col-12">
          <Card className="border-0 shadow-sm">
            <Card.Body className="p-4">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <h5 className="fw-bold mb-2">Create New Backup</h5>
                  <p className="text-muted mb-0">
                    Create a complete backup of all system data including users, doctors, clinics, and appointments
                  </p>
                </div>
                <Button
                  variant="primary"
                  size="lg"
                  onClick={handleCreateBackup}
                  disabled={creating || loading}
                >
                  {creating ? (
                    <>
                      <Spinner animation="border" size="sm" className="me-2" />
                      Creating...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-cloud-arrow-up me-2"></i>
                      Create Backup
                    </>
                  )}
                </Button>
              </div>
            </Card.Body>
          </Card>
        </div>
      </div>

      {/* Backup History */}
      <div className="row">
        <div className="col-12">
          <Card className="border-0 shadow-sm">
            <Card.Body className="p-4">
              <h5 className="fw-bold mb-4">Backup History</h5>
              {loading ? (
                <div className="text-center py-5">
                  <Spinner animation="border" className="text-primary" />
                </div>
              ) : backups.length === 0 ? (
                <div className="alert alert-info" role="alert">
                  No backups available. Click "Create Backup" to create the first backup.
                </div>
              ) : (
                <div className="table-responsive">
                  <Table striped bordered hover>
                    <thead>
                      <tr>
                        <th>Backup ID</th>
                        <th>Created At</th>
                        <th>Size</th>
                        <th>Records</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {backups.map((backup) => (
                        <tr key={backup.backupId}>
                          <td>
                            <code className="small">{backup.backupId.substring(0, 12)}...</code>
                          </td>
                          <td>{formatDate(backup.createdAt)}</td>
                          <td>{formatBytes(backup.sizeInBytes)}</td>
                          <td>
                            <span className="badge bg-info">{backup.recordCount}</span>
                          </td>
                          <td>
                            <span className="badge bg-success">{backup.status}</span>
                          </td>
                          <td>
                            <Button
                              variant="info"
                              size="sm"
                              className="me-2"
                              onClick={() => handleDownloadBackup(backup.backupId)}
                              title="Download backup file"
                            >
                              <i className="bi bi-download me-1"></i>
                              Download
                            </Button>
                            <Button
                              variant="warning"
                              size="sm"
                              className="me-2"
                              onClick={() => handleRestoreClick(backup)}
                              title="Restore from this backup"
                            >
                              <i className="bi bi-arrow-counterclockwise me-1"></i>
                              Restore
                            </Button>
                            <Button
                              variant="danger"
                              size="sm"
                              onClick={() => handleDeleteClick(backup)}
                              title="Delete this backup"
                            >
                              <i className="bi bi-trash me-1"></i>
                              Delete
                            </Button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                </div>
              )}
            </Card.Body>
          </Card>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Confirm Deletion</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Are you sure you want to delete the backup <strong>{selectedBackup?.backupId.substring(0, 12)}...</strong>? This action cannot be undone.
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

      {/* Restore Confirmation Modal */}
      <Modal show={showRestoreModal} onHide={() => setShowRestoreModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Confirm Restore</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Alert variant="warning" className="mb-3">
            <i className="bi bi-exclamation-triangle me-2"></i>
            <strong>Warning!</strong> Restoring from a backup will replace all current data with the backup data. This action cannot be easily undone.
          </Alert>
          Are you sure you want to restore from backup <strong>{selectedBackup?.backupId.substring(0, 12)}...</strong> created on <strong>{selectedBackup ? formatDate(selectedBackup.createdAt) : ""}</strong>?
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowRestoreModal(false)}>
            Cancel
          </Button>
          <Button variant="warning" onClick={handleConfirmRestore}>
            Restore
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default AdminBackupPage;
