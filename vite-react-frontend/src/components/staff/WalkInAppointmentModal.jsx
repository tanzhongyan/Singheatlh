import React, { useState, useEffect } from "react";
import { Modal, Button, Form, Alert } from "react-bootstrap";
import apiClient from "../../api/apiClient";
import SelectSlot from "./SelectSlot";
import SearchableDropdown from "./SearchableDropdown";

const WalkInAppointmentModal = ({ show, onHide, onSuccess, clinicId }) => {
  const [formData, setFormData] = useState({
    patientId: "",
    doctorId: "",
    startDatetime: "",
    endDatetime: "",
  });
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedTime, setSelectedTime] = useState("");
  const [searchBy, setSearchBy] = useState("name"); // "name" or "email"
  const [selectedPatient, setSelectedPatient] = useState(null);

  // Fetch patients and doctors when modal opens
  useEffect(() => {
    if (show) {
      fetchPatients();
      fetchDoctors();
      // Clear any previous selections when opening
      setSelectedDate("");
      setSelectedTime("");
      setSearchBy("name");
      setSelectedPatient(null);
      setFormData((prev) => ({
        ...prev,
        patientId: "",
        startDatetime: "",
        endDatetime: "",
      }));
    }
  }, [show, clinicId]);

  const formatDateTimeLocal = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  const fetchPatients = async () => {
    try {
      const response = await apiClient.get("/api/users/role/P");
      setPatients(response.data);
    } catch (err) {
      console.error("Error fetching patients:", err);
      setError("Failed to load patients");
    }
  };

  const fetchDoctors = async () => {
    try {
      // Fetch doctors from the doctor table filtered by clinicId
      const response = await apiClient.get(`/api/doctor/clinic/${clinicId}`);
      setDoctors(response.data);
      console.log("doctor", response.data);
    } catch (err) {
      console.error("Error fetching doctors:", err);
      setError("Failed to load doctors");
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSlotSelected = (slot) => {
    // slot contains startDatetime and endDatetime from backend
    setFormData((prev) => ({
      ...prev,
      startDatetime: slot.startDatetime,
      endDatetime: slot.endDatetime,
    }));
    const start = new Date(slot.startDatetime);
    setSelectedDate(start.toISOString().split("T")[0]);
    setSelectedTime(start.toTimeString().slice(0, 5));
  };

  const handlePatientSelect = (patient) => {
    setSelectedPatient(patient);
    setFormData((prev) => ({
      ...prev,
      patientId: patient ? patient.userId : "",
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const payload = {
        patientId: formData.patientId,
        doctorId: formData.doctorId,
        startDatetime: formData.startDatetime,
        // endDatetime is automatically calculated by backend using doctor's appointmentDurationInMinutes
        isWalkIn: true,
      };

      await apiClient.post("/api/appointments/walk-in", payload);

      // Reset form and close modal
      setFormData({
        patientId: "",
        doctorId: "",
        startDatetime: "",
        endDatetime: "",
      });
      onSuccess();
      onHide();
    } catch (err) {
      console.error("Error creating walk-in appointment:", err);
      setError(
        err.response?.data?.message || "Failed to create walk-in appointment"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal show={show} onHide={onHide} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>Create Walk-in Appointment</Modal.Title>
      </Modal.Header>

      <Modal.Body>
        {error && <Alert variant="danger">{error}</Alert>}

        <Form onSubmit={handleSubmit}>
          <SearchableDropdown
            items={patients}
            onSelect={handlePatientSelect}
            searchBy={searchBy}
            onSearchByChange={setSearchBy}
            selectedItem={selectedPatient}
            placeholder={`Search by ${searchBy}...`}
            label="Patient"
            required={true}
            searchByOptions={["name", "email"]}
            displayFormat={(patient) => `${patient.name} - ${patient.email}`}
            itemKey="userId"
          />

          <Form.Group className="mb-3">
            <Form.Label>Doctor</Form.Label>
            <Form.Select
              name="doctorId"
              value={formData.doctorId}
              onChange={handleInputChange}
              required
            >
              <option value="">Select a doctor...</option>
              {doctors.map((doctor) => (
                <option key={doctor.doctorId} value={doctor.doctorId}>
                  {doctor.name}
                </option>
              ))}
            </Form.Select>
          </Form.Group>

          {/* Slot Selector */}
          {!formData.doctorId ? (
            <Alert variant="warning" className="mb-3">
              Please select a doctor to view the available slots.
            </Alert>
          ) : (
            <div className="mb-3">
              <SelectSlot
                selectedClinic={null}
                selectedDoctor={doctors.find(
                  (d) => d.doctorId === formData.doctorId
                )}
                selectedDate={selectedDate}
                selectedTime={selectedTime}
                setSelectedDate={setSelectedDate}
                setSelectedTime={setSelectedTime}
                onSlotSelected={handleSlotSelected}
                isWalkIn={true}
              />
            </div>
          )}

          <div className="d-flex justify-content-end gap-2">
            <Button variant="secondary" onClick={onHide} disabled={loading}>
              Cancel
            </Button>
            <Button
              variant="primary"
              type="submit"
              disabled={
                loading || !formData.startDatetime || !formData.endDatetime
              }
            >
              {loading ? "Creating..." : "Create Walk-in Appointment"}
            </Button>
          </div>
        </Form>
      </Modal.Body>
    </Modal>
  );
};

export default WalkInAppointmentModal;
