import React, { useState, useEffect } from "react";
import { Card, Row, Col } from "react-bootstrap";
import { useAuth } from "../../contexts/AuthContext";
import apiClient from "../../api/apiClient";
import { BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";

const ClinicMonitoringPage = () => {
  const { userProfile } = useAuth();
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);

  const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#8884D8", "#FF6B9D"];

  // Helper function to format decimal minutes to "M min S sec" format
  const formatTime = (decimalMinutes) => {
    if (!decimalMinutes || decimalMinutes === 0) return "0 min 0 sec";
    
    const totalSeconds = Math.round(decimalMinutes * 60);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    
    return `${minutes} min ${seconds} sec`;
  };

  // Custom tooltip for waiting time chart
  const CustomTimeTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white border border-secondary p-3 rounded shadow-sm">
          <p className="fw-bold mb-2">{label}</p>
          <p className="text-warning mb-0">
            <i className="bi bi-hourglass-split me-2"></i>
            Avg Wait Time: {formatTime(payload[0].value)}
          </p>
        </div>
      );
    }
    return null;
  };

  // Custom tooltip for consultation time chart
  const CustomConsultationTimeTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white border border-secondary p-3 rounded shadow-sm">
          <p className="fw-bold mb-2">{label}</p>
          <p className="text-info mb-0">
            <i className="bi bi-clock-history me-2"></i>
            Avg Consultation Time: {formatTime(payload[0].value)}
          </p>
        </div>
      );
    }
    return null;
  };

  useEffect(() => {
    if (userProfile?.clinicId) {
      fetchData();
    }
  }, [userProfile, selectedDate]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        `/api/clinic-staff/monitoring/statistics/${userProfile.clinicId}?date=${selectedDate}`
      );
      setStatistics(response.data);
      setError("");
    } catch (err) {
      console.error("Failed to fetch clinic statistics:", err);
      setError("Failed to load clinic statistics");
    } finally {
      setLoading(false);
    }
  };

  if (loading && !statistics) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (!statistics) {
    return (
      <div className="alert alert-danger" role="alert">
        {error || "Unable to load clinic statistics"}
      </div>
    );
  }

  // Prepare data for patients seen chart (by doctor)
  const patientsByDoctorData = statistics.doctorStats.map(doctor => ({
    name: doctor.doctorName,
    patients: doctor.patientsSeenToday,
    avgWaitTime: doctor.averageWaitingTime
  }));

  // Prepare data for queue status pie chart
  const queueStatusData = [
    { name: "Checked In", value: statistics.queueBreakdown.checkedIn },
    { name: "Called (Being Seen)", value: statistics.queueBreakdown.called },
    { name: "Completed", value: statistics.queueBreakdown.completed },
    { name: "No Show", value: statistics.queueBreakdown.noShow },
    { name: "Fast Tracked", value: statistics.queueBreakdown.fastTracked }
  ].filter(item => item.value > 0);

  // Prepare data for waiting time by doctor
  const waitingTimeByDoctorData = statistics.doctorStats
    .filter(doctor => doctor.averageWaitingTime > 0)
    .map(doctor => ({
      name: doctor.doctorName,
      waitTime: doctor.averageWaitingTime
    }));

  // Prepare data for consultation time by doctor
  const consultationTimeByDoctorData = statistics.doctorStats
    .filter(doctor => doctor.averageConsultationTime > 0)
    .map(doctor => ({
      name: doctor.doctorName,
      consultationTime: doctor.averageConsultationTime
    }));

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="row mb-4">
        <div className="col-12">
          <div className="card border-0 shadow-sm bg-primary text-white">
            <div className="card-body p-4">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <h1 className="display-5 fw-bold mb-2">
                    <i className="bi bi-graph-up me-3"></i>
                    Clinic Monitoring Dashboard
                  </h1>
                  <p className="lead mb-0 opacity-90">
                    {statistics.clinicName} - Daily Performance Report
                  </p>
                </div>
                <div>
                  <input
                    type="date"
                    className="form-control"
                    value={selectedDate}
                    onChange={(e) => setSelectedDate(e.target.value)}
                    max={new Date().toISOString().split('T')[0]}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="row g-4 mb-4">
        <div className="col-md-3">
          <Card className="border-0 shadow-sm">
            <Card.Body className="p-4 text-center">
              <div
                className="rounded-circle bg-success bg-opacity-10 d-flex align-items-center justify-content-center mx-auto mb-3"
                style={{ width: "80px", height: "80px" }}
              >
                <i className="bi bi-check-circle text-success" style={{ fontSize: "2rem" }}></i>
              </div>
              <p className="text-muted mb-1 text-uppercase small fw-semibold">Patients Seen Today</p>
              <h2 className="fw-bold mb-0">{statistics.totalPatientsSeenToday}</h2>
              <small className="text-muted">Completed consultations</small>
            </Card.Body>
          </Card>
        </div>

        <div className="col-md-3">
          <Card className="border-0 shadow-sm">
            <Card.Body className="p-4 text-center">
              <div
                className="rounded-circle bg-warning bg-opacity-10 d-flex align-items-center justify-content-center mx-auto mb-3"
                style={{ width: "80px", height: "80px" }}
              >
                <i className="bi bi-hourglass-split text-warning" style={{ fontSize: "2rem" }}></i>
              </div>
              <p className="text-muted mb-1 text-uppercase small fw-semibold">Avg Waiting Time</p>
              <h2 className="fw-bold mb-0">{formatTime(statistics.averageWaitingTime)}</h2>
              <small className="text-muted">&nbsp;</small>
            </Card.Body>
          </Card>
        </div>

        <div className="col-md-3">
          <Card className="border-0 shadow-sm">
            <Card.Body className="p-4 text-center">
              <div
                className="rounded-circle bg-info bg-opacity-10 d-flex align-items-center justify-content-center mx-auto mb-3"
                style={{ width: "80px", height: "80px" }}
              >
                <i className="bi bi-clock-history text-info" style={{ fontSize: "2rem" }}></i>
              </div>
              <p className="text-muted mb-1 text-uppercase small fw-semibold">Avg Consultation Time</p>
              <h2 className="fw-bold mb-0">{formatTime(statistics.averageConsultationTime)}</h2>
              <small className="text-muted">&nbsp;</small>
            </Card.Body>
          </Card>
        </div>

        <div className="col-md-3">
          <Card className="border-0 shadow-sm">
            <Card.Body className="p-4 text-center">
              <div
                className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center mx-auto mb-3"
                style={{ width: "80px", height: "80px" }}
              >
                <i className="bi bi-people text-primary" style={{ fontSize: "2rem" }}></i>
              </div>
              <p className="text-muted mb-1 text-uppercase small fw-semibold">Total Check-ins</p>
              <h2 className="fw-bold mb-0">{statistics.totalCheckInsToday}</h2>
              <small className="text-muted">patients today</small>
            </Card.Body>
          </Card>
        </div>
      </div>

      {/* Charts Row 1: Patients Seen by Doctor & Queue Status */}
      <div className="row g-4 mb-4">
        <div className="col-lg-7">
          <Card className="border-0 shadow-sm h-100">
            <Card.Body className="p-4">
              <h5 className="fw-bold mb-4">
                <i className="bi bi-bar-chart me-2"></i>
                Patients Seen by Doctor
              </h5>
              <ResponsiveContainer width="100%" height={350}>
                <BarChart data={patientsByDoctorData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis 
                    dataKey="name" 
                    angle={-45} 
                    textAnchor="end" 
                    height={100}
                    interval={0}
                  />
                  <YAxis label={{ value: 'Patients', angle: -90, position: 'insideLeft' }} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="patients" fill="#0088FE" name="Patients Seen" />
                </BarChart>
              </ResponsiveContainer>
              <div className="mt-3">
                <p className="small text-muted mb-0">
                  <strong>Total:</strong> {statistics.totalPatientsSeenToday} patients completed today
                </p>
              </div>
            </Card.Body>
          </Card>
        </div>

        <div className="col-lg-5">
          <Card className="border-0 shadow-sm h-100">
            <Card.Body className="p-4">
              <h5 className="fw-bold mb-4">
                <i className="bi bi-pie-chart me-2"></i>
                Queue Status Distribution
              </h5>
              <ResponsiveContainer width="100%" height={350}>
                <PieChart>
                  <Pie
                    data={queueStatusData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={(entry) => `${entry.name}: ${entry.value}`}
                    outerRadius={100}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {queueStatusData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </Card.Body>
          </Card>
        </div>
      </div>

      {/* Charts Row 2: Waiting Time by Doctor */}
      {waitingTimeByDoctorData.length > 0 && (
        <div className="row g-4 mb-4">
          <div className="col-12">
            <Card className="border-0 shadow-sm">
              <Card.Body className="p-4">
                <h5 className="fw-bold mb-4">
                  <i className="bi bi-hourglass me-2"></i>
                  Average Waiting Time by Doctor
                </h5>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={waitingTimeByDoctorData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis 
                      dataKey="name" 
                      angle={-45} 
                      textAnchor="end" 
                      height={100}
                      interval={0}
                    />
                    <YAxis label={{ value: 'Minutes', angle: -90, position: 'insideLeft' }} />
                    <Tooltip content={<CustomTimeTooltip />} />
                    <Legend />
                    <Bar dataKey="waitTime" fill="#FFBB28" name="Avg Wait Time" />
                  </BarChart>
                </ResponsiveContainer>
                <div className="mt-3 d-flex justify-content-around text-center">
                  <div>
                    <p className="text-muted small mb-0">Min Wait Time</p>
                    <h6 className="fw-bold">{formatTime(statistics.minWaitingTime)}</h6>
                  </div>
                  <div>
                    <p className="text-muted small mb-0">Avg Wait Time</p>
                    <h6 className="fw-bold">{formatTime(statistics.averageWaitingTime)}</h6>
                  </div>
                  <div>
                    <p className="text-muted small mb-0">Max Wait Time</p>
                    <h6 className="fw-bold">{formatTime(statistics.maxWaitingTime)}</h6>
                  </div>
                </div>
              </Card.Body>
            </Card>
          </div>
        </div>
      )}

      {/* Average Consultation Time by Doctor */}
      {consultationTimeByDoctorData.length > 0 && (
        <div className="row g-4 mb-4">
          <div className="col-12">
            <Card className="border-0 shadow-sm">
              <Card.Body className="p-4">
                <h5 className="fw-bold mb-4">
                  <i className="bi bi-clock-history me-2 text-info"></i>
                  Average Consultation Time by Doctor
                </h5>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={consultationTimeByDoctorData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis 
                      dataKey="name" 
                      angle={-45} 
                      textAnchor="end" 
                      height={100}
                      interval={0}
                    />
                    <YAxis label={{ value: 'Minutes', angle: -90, position: 'insideLeft' }} />
                    <Tooltip content={<CustomConsultationTimeTooltip />} />
                    <Legend />
                    <Bar dataKey="consultationTime" fill="#17a2b8" name="Avg Consultation Time" />
                  </BarChart>
                </ResponsiveContainer>
                <div className="mt-3 text-center">
                  <p className="text-muted small mb-0">Overall Average Consultation Time</p>
                  <h6 className="fw-bold">{formatTime(statistics.averageConsultationTime)}</h6>
                </div>
              </Card.Body>
            </Card>
          </div>
        </div>
      )}

      {/* Doctor Performance Table */}
      <div className="row g-4 mb-4">
        <div className="col-12">
          <Card className="border-0 shadow-sm">
            <Card.Body className="p-4">
              <h5 className="fw-bold mb-4">
                <i className="bi bi-people me-2"></i>
                Doctor Performance Details
              </h5>
              <div className="table-responsive">
                <table className="table table-hover">
                  <thead className="table-light">
                    <tr>
                      <th>Doctor Name</th>
                      <th className="text-center">Patients Seen</th>
                      <th className="text-center">Avg Waiting Time</th>
                      <th className="text-center">Current Queue Size</th>
                    </tr>
                  </thead>
                  <tbody>
                    {statistics.doctorStats.map((doctor) => (
                      <tr key={doctor.doctorId}>
                        <td className="fw-semibold">{doctor.doctorName}</td>
                        <td className="text-center">
                          <span className="badge bg-success">{doctor.patientsSeenToday}</span>
                        </td>
                        <td className="text-center">{formatTime(doctor.averageWaitingTime)}</td>
                        <td className="text-center">
                          <span className="badge bg-info">{doctor.currentQueueSize}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card.Body>
          </Card>
        </div>
      </div>

      {/* Last Update */}
      <div className="row">
        <div className="col-12">
          <Card className="border-0 shadow-sm bg-light">
            <Card.Body className="p-3 text-center text-muted small">
              Last updated: {new Date().toLocaleString()} |
              <button
                className="btn btn-sm btn-link ms-2"
                onClick={fetchData}
                disabled={loading}
              >
                {loading ? "Refreshing..." : "Refresh Now"}
              </button>
            </Card.Body>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default ClinicMonitoringPage;

