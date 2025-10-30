import { useEffect, useState } from "react";
import apiClient from "../../api/apiClient";

export default function SelectSlot({
  selectedClinic,
  selectedDoctor,
  selectedDate,
  selectedTime,
  setSelectedDate,
  setSelectedTime,
}) {
  const [availableSlots, setAvailableSlots] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedDay, setSelectedDay] = useState(null);

  // Get minimum date (tomorrow) in YYYY-MM-DD (UTC date portion)
  const getMinDate = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split("T")[0];
  };

  useEffect(() => {
    if (selectedDoctor) {
      fetchDoctorSlots(selectedDoctor.doctorId);
    }
  }, [selectedDoctor]);

  useEffect(() => {
    const allDates = Object.keys(availableSlots);
    if (allDates.length === 0) return;

    const minDateStr = getMinDate();
    const minThreshold = new Date(minDateStr + "T00:00:00Z");
    const filtered = allDates.filter((d) => new Date(d) >= minThreshold).sort();

    if (filtered.length === 0) {
      // No valid dates after min date
      setSelectedDay(null);
      return;
    }

    // Ensure selectedDay is valid and not before min date
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
    const groups = {
      morning: [],
      afternoon: [],
      evening: [],
    };

    slots.forEach((slot) => {
      const hour = new Date(slot.startDatetime).getHours();
      if (hour < 12) {
        groups.morning.push(slot);
      } else if (hour < 17) {
        groups.afternoon.push(slot);
      } else {
        groups.evening.push(slot);
      }
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
  };

  const isSlotSelected = (slot) => {
    if (!selectedDate || !selectedTime) return false;

    const slotStart = new Date(slot.startDatetime);
    const slotDateStr = slotStart.toISOString().split("T")[0];
    const slotTimeStr = slotStart.toTimeString().slice(0, 5);

    return selectedDate === slotDateStr && selectedTime === slotTimeStr;
  };

  // Only show dates on/after the minimum date
  const sortedDates = (() => {
    const all = Object.keys(availableSlots);
    if (all.length === 0) return [];
    const minDateStr = getMinDate();
    const minThreshold = new Date(minDateStr + "T00:00:00Z");
    return all.filter((d) => new Date(d) >= minThreshold).sort();
  })();
  const selectedDaySlots = selectedDay ? availableSlots[selectedDay] : [];
  const groupedSlots = groupSlotsByTime(selectedDaySlots);

  return (
    <div className="select-slot-container">
      {/* Header Card */}
      <div className="header-card mb-4">
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
              style={{ fontSize: "1.25rem", fontWeight: 700, color: "#111827" }}
            >
              Select Time Slot
            </div>
            <div style={{ color: "#6b7280", marginTop: 4 }}>
              Choose your preferred appointment time
            </div>
          </div>
          <div style={{ textAlign: "right" }}>
            <div style={{ fontSize: 12, color: "#6b7280" }}>
              Selected Doctor
            </div>
            <div style={{ fontWeight: 600, color: "#111827" }}>
              Dr. {selectedDoctor?.name}
            </div>
            <div style={{ fontSize: 12, color: "#4b5563" }}>
              {selectedClinic?.name}
            </div>
          </div>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: "center", padding: "3rem 0" }}>
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
          No available slots. Please check back later.
        </div>
      ) : (
        <div
          style={{ display: "flex", flexDirection: "column", gap: "1.25rem" }}
        >
          {/* Date Selection */}
          <div className="date-card">
            <div
              style={{ fontWeight: 600, color: "#111827", marginBottom: 12 }}
            >
              Select Date
            </div>
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
          </div>

          {/* Time Slots */}
          {selectedDay && (
            <div className="times-card">
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  marginBottom: 16,
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
                {selectedDate && selectedTime && (
                  <div
                    style={{
                      background: "#ecfdf5",
                      border: "1px solid #a7f3d0",
                      color: "#065f46",
                      borderRadius: 8,
                      padding: "6px 10px",
                      fontSize: 13,
                      fontWeight: 600,
                    }}
                  >
                    Slot Selected
                  </div>
                )}
              </div>

              <div
                style={{ display: "flex", flexDirection: "column", gap: 20 }}
              >
                {/* Morning */}
                {groupedSlots.morning.length > 0 && (
                  <TimeSection
                    title="Morning"
                    slots={groupedSlots.morning}
                    isSlotSelected={isSlotSelected}
                    onSlotClick={handleSlotClick}
                    color="blue"
                  />
                )}

                {/* Afternoon */}
                {groupedSlots.afternoon.length > 0 && (
                  <TimeSection
                    title="Afternoon"
                    slots={groupedSlots.afternoon}
                    isSlotSelected={isSlotSelected}
                    onSlotClick={handleSlotClick}
                    color="amber"
                  />
                )}

                {/* Evening */}
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

      {/* Selected Appointment Summary */}
      {selectedDate && selectedTime && (
        <div
          className="summary-card"
          style={{
            marginTop: 16,
            borderRadius: 12,
            boxShadow: "0 10px 20px rgba(37,99,235,0.25)",
          }}
        >
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
            }}
          >
            <div>
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 8,
                  marginBottom: 8,
                }}
              >
                <div
                  style={{
                    width: 8,
                    height: 8,
                    borderRadius: 9999,
                    background: "#34d399",
                  }}
                ></div>
                <div style={{ fontWeight: 600 }}>Slot Selected</div>
              </div>
              <div className="summary-meta">
                <div style={{ fontWeight: 600 }}>{selectedClinic?.name}</div>
                <div>Dr. {selectedDoctor?.name}</div>
                <div style={{ marginTop: 8 }}>
                  {new Date(`${selectedDate}T${selectedTime}`).toLocaleString(
                    "en-US",
                    {
                      weekday: "long",
                      month: "long",
                      day: "numeric",
                      hour: "2-digit",
                      minute: "2-digit",
                    }
                  )}
                </div>
                <div style={{ fontSize: 13, opacity: 0.9, marginTop: 4 }}>
                  Duration: {selectedDoctor?.appointmentDurationInMinutes || 15}{" "}
                  minutes
                </div>
              </div>
            </div>
            <div style={{ fontSize: "1.75rem" }}>✅</div>
          </div>
        </div>
      )}
    </div>
  );
}

// Time Section Component
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
      <div className="section-title" style={{ marginBottom: 12 }}>
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
