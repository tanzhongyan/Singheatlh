export default function DateTimeSelector({
  selectedClinic,
  selectedDoctor,
  selectedDate,
  selectedTime,
  setSelectedDate,
  setSelectedTime,
  handleBookAppointment,
}) {
  // Get minimum date (tomorrow)
  const getMinDate = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split("T")[0];
  };
  return (
    <div>
      <div className="alert alert-light border mb-3">
        <small className="text-muted d-block">Selected Clinic & Doctor</small>
        <div className="fw-semibold">{selectedClinic?.name}</div>
        <div className="text-muted small">Dr. {selectedDoctor?.name}</div>
      </div>
      <h6 className="mb-3">Choose Date & Time</h6>
      <form onSubmit={handleBookAppointment}>
        <div className="mb-3">
          <label htmlFor="appointmentDate" className="form-label">
            Date
          </label>
          <input
            type="date"
            className="form-control"
            id="appointmentDate"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            min={getMinDate()}
            required
          />
          <div className="form-text">
            Appointments must be booked at least 24 hours in advance
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="appointmentTime" className="form-label">
            Time
          </label>
          <input
            type="time"
            className="form-control"
            id="appointmentTime"
            value={selectedTime}
            onChange={(e) => setSelectedTime(e.target.value)}
            required
          />
          <div className="form-text">Each appointment is 15 minutes</div>
        </div>

        {selectedDate && selectedTime && (
          <div className="alert alert-light border">
            <strong>Appointment Summary</strong>
            <div className="mt-2 text-muted">
              <div>{selectedClinic?.name}</div>
              <div>Dr. {selectedDoctor?.name}</div>
              <div>
                {new Date(`${selectedDate}T${selectedTime}`).toLocaleString(
                  "en-US",
                  {
                    weekday: "long",
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                    hour: "2-digit",
                    minute: "2-digit",
                  }
                )}
              </div>
              <div className="small">Duration: 15 minutes</div>
            </div>
          </div>
        )}
      </form>
    </div>
  );
}
