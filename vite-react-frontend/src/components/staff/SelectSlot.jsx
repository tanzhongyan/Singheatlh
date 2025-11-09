import { useEffect, useState } from "react";
import apiClient from "../../api/apiClient";
import Calendar from "../Calendar";

export default function SelectSlot({
  selectedClinic,
  selectedDoctor,
  selectedDate,
  selectedTime,
  setSelectedDate,
  setSelectedTime,
  onSlotSelected, // optional: notify parent with full slot
}) {
  const [availableSlots, setAvailableSlots] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedDay, setSelectedDay] = useState(null);
  const [viewMode, setViewMode] = useState("list"); // "list" or "calendar"

  // Get minimum date (tomorrow) in YYYY-MM-DD (UTC date portion)
  const getMinDate = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split("T")[0];
  };

  useEffect(() => {
    if (selectedDoctor?.doctorId) {
      fetchDoctorSlots(selectedDoctor.doctorId);
    }
  }, [selectedDoctor?.doctorId]);

  useEffect(() => {
    const allDates = Object.keys(availableSlots);
    if (allDates.length === 0) return;

    const minDateStr = getMinDate();
    const minThreshold = new Date(minDateStr);
    const filtered = allDates.filter((d) => new Date(d) >= minThreshold).sort();

    if (filtered.length === 0) {
      setSelectedDay(null);
      return;
    }

    if (!selectedDay || !filtered.includes(selectedDay)) {
      setSelectedDay(filtered[0]);
    }
  }, [availableSlots]);

  const fetchDoctorSlots = async (doctorId) => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get(
        `/api/schedules/doctor/${doctorId}/slot`
      );
      setAvailableSlots(response.data);
    } catch (err) {
      console.error("Error fetching doctor slots:", err);
      setError("Failed to load doctor slots.");
      setAvailableSlots({});
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
    });
  };

  const formatFullDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const groupSlotsByTime = (slots) => {
    const groups = { morning: [], afternoon: [], evening: [] };

    // Guard against undefined or null slots
    if (!slots || !Array.isArray(slots)) {
      return groups;
    }

    slots.forEach((slot) => {
      const hour = new Date(slot.startDatetime).getHours();
      if (hour < 12) groups.morning.push(slot);
      else if (hour < 17) groups.afternoon.push(slot);
      else groups.evening.push(slot);
    });

    Object.values(groups).forEach((group) => {
      group.sort(
        (a, b) => new Date(a.startDatetime) - new Date(b.startDatetime)
      );
    });

    return groups;
  };

  const handleSlotClick = (slot) => {
    const startDate = new Date(slot.startDatetime);
    const dateStr = startDate.toISOString().split("T")[0];
    const timeStr = startDate.toTimeString().slice(0, 5);
    setSelectedDate(dateStr);
    setSelectedTime(timeStr);
    if (onSlotSelected) onSlotSelected(slot);
  };

  const isSlotSelected = (slot) => {
    if (!selectedDate || !selectedTime) return false;
    const slotStart = new Date(slot.startDatetime);
    const slotDateStr = slotStart.toISOString().split("T")[0];
    const slotTimeStr = slotStart.toTimeString().slice(0, 5);
    return selectedDate === slotDateStr && selectedTime === slotTimeStr;
  };

  const sortedDates = (() => {
    const all = Object.keys(availableSlots);
    if (all.length === 0) return [];
    const minDateStr = getMinDate();
    const minThreshold = new Date(minDateStr);
    return all.filter((d) => new Date(d) >= minThreshold).sort();
  })();

  const selectedDaySlots =
    selectedDay && availableSlots[selectedDay]
      ? availableSlots[selectedDay]
      : [];
  const groupedSlots = groupSlotsByTime(selectedDaySlots);

  // Handler for calendar date selection
  const handleCalendarDateSelect = (date) => {
    // Find the matching key in availableSlots
    const matchingKey = Object.keys(availableSlots).find((key) => {
      const keyDate = new Date(key);
      return (
        keyDate.getFullYear() === date.getFullYear() &&
        keyDate.getMonth() === date.getMonth() &&
        keyDate.getDate() === date.getDate()
      );
    });

    if (matchingKey) {
      setSelectedDay(matchingKey);
    }
  };

  return (
    <div className="select-slot-container">
      <div className="header-card mb-3">
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            gap: 12,
          }}
        >
          <div>
            <div
              style={{ fontSize: "1.1rem", fontWeight: 700, color: "#111827" }}
            >
              Select Time Slot
            </div>
            <div style={{ color: "#6b7280", marginTop: 4 }}>
              Choose an available time for the walk-in
            </div>
          </div>
          <div style={{ textAlign: "right" }}>
            <div style={{ fontSize: 12, color: "#6b7280" }}>
              Selected Doctor
            </div>
            <div style={{ fontWeight: 600, color: "#111827" }}>
              {selectedDoctor?.name || "-"}
            </div>
            {selectedClinic?.name && (
              <div style={{ fontSize: 12, color: "#4b5563" }}>
                {selectedClinic?.name}
              </div>
            )}
          </div>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: "center", padding: "2rem 0" }}>
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <div style={{ color: "#6b7280", marginTop: 12 }}>
            Loading available slots...
          </div>
        </div>
      ) : error ? (
        <div className="alert alert-danger text-center" role="alert">
          {error}
        </div>
      ) : sortedDates.length === 0 ? (
        <div className="empty-state">
          <div style={{ fontSize: "2rem", marginBottom: 8 }}>⏰</div>
          No available slots. Please select a different doctor or try later.
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
          <div className="date-card">
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: 10,
              }}
            >
              <div style={{ fontWeight: 600, color: "#111827" }}>
                Select Date
              </div>
              <div
                style={{
                  display: "flex",
                  gap: "4px",
                  background: "#f3f4f6",
                  padding: "4px",
                  borderRadius: "8px",
                }}
              >
                <button
                  type="button"
                  onClick={() => setViewMode("list")}
                  style={{
                    padding: "6px 12px",
                    border: "none",
                    borderRadius: "6px",
                    background: viewMode === "list" ? "white" : "transparent",
                    color: viewMode === "list" ? "#111827" : "#6b7280",
                    fontWeight: viewMode === "list" ? 600 : 400,
                    fontSize: "13px",
                    cursor: "pointer",
                    transition: "all 0.2s",
                    boxShadow:
                      viewMode === "list"
                        ? "0 1px 3px rgba(0,0,0,0.1)"
                        : "none",
                  }}
                >
                  📋 List
                </button>
                <button
                  type="button"
                  onClick={() => setViewMode("calendar")}
                  style={{
                    padding: "6px 12px",
                    border: "none",
                    borderRadius: "6px",
                    background:
                      viewMode === "calendar" ? "white" : "transparent",
                    color: viewMode === "calendar" ? "#111827" : "#6b7280",
                    fontWeight: viewMode === "calendar" ? 600 : 400,
                    fontSize: "13px",
                    cursor: "pointer",
                    transition: "all 0.2s",
                    boxShadow:
                      viewMode === "calendar"
                        ? "0 1px 3px rgba(0,0,0,0.1)"
                        : "none",
                  }}
                >
                  📅 Calendar
                </button>
              </div>
            </div>

            {viewMode === "list" ? (
              <div
                style={{ display: "flex", overflowX: "auto", paddingBottom: 8 }}
              >
                {sortedDates.map((date) => (
                  <button
                    key={date}
                    type="button"
                    onClick={() => setSelectedDay(date)}
                    className={`slot-date-chip ${
                      selectedDay === date ? "active" : ""
                    }`}
                  >
                    <div>{formatDate(date)}</div>
                    <div className="subtext">
                      {availableSlots[date].length} slots
                    </div>
                  </button>
                ))}
              </div>
            ) : (
              <Calendar
                availableDates={sortedDates}
                onDateSelect={handleCalendarDateSelect}
                selectedDate={selectedDay}
              />
            )}
          </div>

          {selectedDay && (
            <div className="times-card">
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  marginBottom: 12,
                }}
              >
                <div>
                  <div style={{ fontWeight: 600, color: "#111827" }}>
                    {formatFullDate(selectedDay)}
                  </div>
                  <div style={{ color: "#6b7280", fontSize: 13, marginTop: 4 }}>
                    {selectedDaySlots.length} available time slots
                  </div>
                </div>
              </div>

              <div
                style={{ display: "flex", flexDirection: "column", gap: 16 }}
              >
                {groupedSlots.morning.length > 0 && (
                  <TimeSection
                    title="Morning"
                    slots={groupedSlots.morning}
                    isSlotSelected={isSlotSelected}
                    onSlotClick={handleSlotClick}
                    color="blue"
                  />
                )}
                {groupedSlots.afternoon.length > 0 && (
                  <TimeSection
                    title="Afternoon"
                    slots={groupedSlots.afternoon}
                    isSlotSelected={isSlotSelected}
                    onSlotClick={handleSlotClick}
                    color="amber"
                  />
                )}
                {groupedSlots.evening.length > 0 && (
                  <TimeSection
                    title="Evening"
                    slots={groupedSlots.evening}
                    isSlotSelected={isSlotSelected}
                    onSlotClick={handleSlotClick}
                    color="purple"
                  />
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function TimeSection({ title, slots, isSlotSelected, onSlotClick, color }) {
  const variant =
    color === "amber"
      ? "variant-amber"
      : color === "purple"
      ? "variant-purple"
      : "variant-blue";
  const dotClass =
    color === "amber"
      ? "dot-amber"
      : color === "purple"
      ? "dot-purple"
      : "dot-blue";

  return (
    <div className="time-section">
      <div className="section-title" style={{ marginBottom: 10 }}>
        <div className={`dot ${dotClass}`}></div>
        <span>{title}</span>
        <span style={{ color: "#6b7280", fontSize: 13, marginLeft: 8 }}>
          ({slots.length} slots)
        </span>
      </div>
      <div className="slot-grid">
        {slots.map((slot, index) => {
          const selected = isSlotSelected(slot);
          return (
            <button
              key={index}
              type="button"
              onClick={() => onSlotClick(slot)}
              className={`slot-button ${selected ? `selected ${variant}` : ""}`}
            >
              {new Date(slot.startDatetime).toLocaleTimeString("en-US", {
                hour: "2-digit",
                minute: "2-digit",
                hour12: true,
              })}
            </button>
          );
        })}
      </div>
    </div>
  );
}
