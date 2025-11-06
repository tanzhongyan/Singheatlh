import React, { useState, useEffect } from "react";
import { Card, Row, Col, ProgressBar } from "react-bootstrap";
import apiClient from "../../api/apiClient";
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";

const AdminMonitoringPage = () => {
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#8884D8"];

  useEffect(() => {
    fetchSystemStatistics();
    // Auto-refresh every 30 seconds
    const interval = setInterval(fetchSystemStatistics, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchSystemStatistics = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get("/api/system-administrators/monitoring/statistics");
      setStatistics(response.data);
      setError("");
    } catch (err) {
      console.error("Failed to fetch system statistics:", err);
      setError("Failed to load system statistics");
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
        {error || "Unable to load system statistics"}
      </div>
    );
  }

  // Prepare data for appointment status chart
  const appointmentStatusData = [
    { name: "Completed", value: statistics.completedAppointments },
    { name: "Pending", value: statistics.pendingAppointments },
    { name: "Cancelled", value: statistics.cancelledAppointments }
  ];

  // Prepare data for user breakdown chart
  const userBreakdownData = [
    { name: "Patients", value: statistics.totalPatients },
    { name: "Clinic Staff", value: statistics.totalClinicStaff },
    { name: "Administrators", value: statistics.totalAdministrators }
  ];

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="row mb-4">
        <div className="col-12">
          <div className="card border-0 shadow-sm bg-primary text-white">
            <div className="card-body p-4">
              <h1 className="display-5 fw-bold mb-2">
                <i className="bi bi-graph-up me-3"></i>
                System Monitoring Dashboard
              </h1>
              <p className="lead mb-0 opacity-90">
                Real-time overview of system usage and performance
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* System Health Status */}
      <div className="row g-4 mb-4">
        <div className="col-md-6">
          <Card className="border-0 shadow-sm h-100">
            <Card.Body className="p-4">
              <div className="d-flex justify-content-between align-items-start mb-3">
                <div>
                  <p className="text-muted mb-1 text-uppercase small fw-semibold">
                    System Uptime
                  </p>
                  <h3 className="fw-bold mb-0">{statistics.systemUptime}%</h3>
                </div>
                <div
                  className="rounded-circle bg-success bg-opacity-10 d-flex align-items-center justify-content-center"
                  style={{ width: "60px", height: "60px" }}
                >
                  <i
                    className="bi bi-check-circle text-success"
                    style={{ fontSize: "1.5rem" }}
                  ></i>
                </div>
              </div>
              <ProgressBar variant="success" now={statistics.systemUptime} label={`${statistics.systemUptime}%`} />
            </Card.Body>
          </Card>
        </div>

        <div className="col-md-6">
          <Card className="border-0 shadow-sm h-100">
            <Card.Body className="p-4">
              <div className="d-flex justify-content-between align-items-start mb-3">
                <div>
                  <p className="text-muted mb-1 text-uppercase small fw-semibold">
                    Active Users
                  </p>
                  <h3 className="fw-bold mb-0">{statistics.activeUsers}</h3>
                </div>
                <div
                  className="rounded-circle bg-info bg-opacity-10 d-flex align-items-center justify-content-center"
                  style={{ width: "60px", height: "60px" }}
                >
                  <i
                    className="bi bi-people text-info"
                    style={{ fontSize: "1.5rem" }}
                  ></i>
                </div>
              </div>
              <p className="text-muted mb-0 small">
                {Math.round((statistics.activeUsers / statistics.totalUsers) * 100)}% of total users
              </p>
            </Card.Body>
          </Card>
        </div>
      </div>

      {/* Key Statistics */}
      <div className="row g-4 mb-4">
        <div className="col-md-3">
          <Card className="border-0 shadow-sm">
            <Card.Body className="p-4 text-center">
              <div
                className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center mx-auto mb-3"
                style={{ width: "80px", height: "80px" }}
              >
                <i className="bi bi-people text-primary" style={{ fontSize: "2rem" }}></i>
              </div>
              <p className="text-muted mb-1 text-uppercase small fw-semibold">Total Users</p>
              <h2 className="fw-bold mb-0">{statistics.totalUsers}</h2>
            </Card.Body>
          </Card>
        </div>

        <div className="col-md-3">
          <Card className="border-0 shadow-sm">
            <Card.Body className="p-4 text-center">
              <div
                className="rounded-circle bg-success bg-opacity-10 d-flex align-items-center justify-content-center mx-auto mb-3"
                style={{ width: "80px", height: "80px" }}
              >
                <i className="bi bi-heart-pulse text-success" style={{ fontSize: "2rem" }}></i>
              </div>
              <p className="text-muted mb-1 text-uppercase small fw-semibold">Total Doctors</p>
              <h2 className="fw-bold mb-0">{statistics.totalDoctors}</h2>
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
                <i className="bi bi-hospital text-warning" style={{ fontSize: "2rem" }}></i>
              </div>
              <p className="text-muted mb-1 text-uppercase small fw-semibold">Total Clinics</p>
              <h2 className="fw-bold mb-0">{statistics.totalClinics}</h2>
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
                <i className="bi bi-calendar-check text-info" style={{ fontSize: "2rem" }}></i>
              </div>
              <p className="text-muted mb-1 text-uppercase small fw-semibold">Total Appointments</p>
              <h2 className="fw-bold mb-0">{statistics.totalAppointments}</h2>
            </Card.Body>
          </Card>
        </div>
      </div>

      {/* User Breakdown and Appointment Status */}
      <div className="row g-4 mb-4">
        <div className="col-lg-6">
          <Card className="border-0 shadow-sm h-100">
            <Card.Body className="p-4">
              <h5 className="fw-bold mb-4">User Breakdown</h5>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={userBreakdownData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={(entry) => `${entry.name}: ${entry.value}`}
                    outerRadius={100}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {userBreakdownData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
              <div className="mt-4">
                <div className="mb-3">
                  <div className="d-flex justify-content-between mb-1">
                    <span className="small fw-semibold">Patients: {statistics.totalPatients}</span>
                  </div>
                </div>
                <div className="mb-3">
                  <div className="d-flex justify-content-between mb-1">
                    <span className="small fw-semibold">Clinic Staff: {statistics.totalClinicStaff}</span>
                  </div>
                </div>
                <div>
                  <div className="d-flex justify-content-between mb-1">
                    <span className="small fw-semibold">Administrators: {statistics.totalAdministrators}</span>
                  </div>
                </div>
              </div>
            </Card.Body>
          </Card>
        </div>

        <div className="col-lg-6">
          <Card className="border-0 shadow-sm h-100">
            <Card.Body className="p-4">
              <h5 className="fw-bold mb-4">Appointment Status Distribution</h5>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={appointmentStatusData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="value" fill="#8884d8" />
                </BarChart>
              </ResponsiveContainer>
              <div className="mt-4">
                <div className="mb-3">
                  <span className="badge bg-success me-2">Completed: {statistics.completedAppointments}</span>
                </div>
                <div className="mb-3">
                  <span className="badge bg-warning me-2">Pending: {statistics.pendingAppointments}</span>
                </div>
                <div>
                  <span className="badge bg-danger me-2">Cancelled: {statistics.cancelledAppointments}</span>
                </div>
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
                onClick={fetchSystemStatistics}
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

export default AdminMonitoringPage;
