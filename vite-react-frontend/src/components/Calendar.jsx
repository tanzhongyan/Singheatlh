import React, { useState } from "react";
import PropTypes from "prop-types";

const Calendar = ({
  availableDates = [],
  onDateSelect,
  selectedDate = null,
}) => {
  const [currentMonth, setCurrentMonth] = useState(new Date());

  // Helper function to check if a date is available
  const isDateAvailable = (date) => {
    return availableDates.some((availableDate) => {
      const available = new Date(availableDate);
      return (
        available.getFullYear() === date.getFullYear() &&
        available.getMonth() === date.getMonth() &&
        available.getDate() === date.getDate()
      );
    });
  };

  // Helper function to check if a date is selected
  const isDateSelected = (date) => {
    if (!selectedDate) return false;
    const selected = new Date(selectedDate);
    return (
      selected.getFullYear() === date.getFullYear() &&
      selected.getMonth() === date.getMonth() &&
      selected.getDate() === date.getDate()
    );
  };

  // Get days in month
  const getDaysInMonth = (date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    return { daysInMonth, startingDayOfWeek };
  };

  // Navigate months
  const previousMonth = () => {
    setCurrentMonth(
      new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1)
    );
  };

  const nextMonth = () => {
    setCurrentMonth(
      new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1)
    );
  };

  // Handle date click
  const handleDateClick = (date) => {
    if (isDateAvailable(date)) {
      // Create a proper date string in local timezone
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const dateObj = new Date(year, date.getMonth(), date.getDate());
      onDateSelect(dateObj);
    }
  };

  // Render calendar
  const renderCalendar = () => {
    const { daysInMonth, startingDayOfWeek } = getDaysInMonth(currentMonth);
    const days = [];

    // Empty cells for days before month starts
    for (let i = 0; i < startingDayOfWeek; i++) {
      days.push(
        <div key={`empty-${i}`} style={{ background: "transparent" }}></div>
      );
    }

    // Actual days
    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(
        currentMonth.getFullYear(),
        currentMonth.getMonth(),
        day
      );
      const available = isDateAvailable(date);
      const selected = isDateSelected(date);

      days.push(
        <div
          key={day}
          style={getDayStyle(available, selected)}
          onClick={() => handleDateClick(date)}
          onMouseEnter={(e) => {
            if (available && !selected) {
              e.target.style.background = "#1976d2";
              e.target.style.color = "white";
              e.target.style.transform = "scale(1.05)";
            }
          }}
          onMouseLeave={(e) => {
            if (available && !selected) {
              e.target.style.background = "#e3f2fd";
              e.target.style.color = "#1976d2";
              e.target.style.transform = "scale(1)";
            }
          }}
        >
          {day}
        </div>
      );
    }

    return days;
  };

  const monthNames = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ];

  const styles = {
    container: {
      maxWidth: "400px",
      margin: "0 auto",
      padding: "20px",
      background: "white",
      borderRadius: "8px",
      boxShadow: "0 2px 8px rgba(0, 0, 0, 0.1)",
    },
    header: {
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      marginBottom: "20px",
    },
    monthYear: {
      margin: 0,
      fontSize: "1.25rem",
      fontWeight: 600,
      color: "#333",
    },
    navBtn: {
      background: "#f0f0f0",
      border: "none",
      borderRadius: "4px",
      padding: "8px 12px",
      cursor: "pointer",
      fontSize: "1rem",
      transition: "background 0.2s",
    },
    weekdays: {
      display: "grid",
      gridTemplateColumns: "repeat(7, 1fr)",
      gap: "8px",
      marginBottom: "10px",
    },
    weekday: {
      textAlign: "center",
      fontWeight: 600,
      fontSize: "0.875rem",
      color: "#666",
      padding: "8px 0",
    },
    grid: {
      display: "grid",
      gridTemplateColumns: "repeat(7, 1fr)",
      gap: "8px",
    },
  };

  const getDayStyle = (available, selected) => {
    const baseStyle = {
      aspectRatio: "1",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      borderRadius: "4px",
      fontSize: "0.875rem",
      transition: "all 0.2s",
    };

    if (selected) {
      return {
        ...baseStyle,
        background: "#1976d2",
        color: "white",
        fontWeight: 600,
        boxShadow: "0 2px 4px rgba(25, 118, 210, 0.3)",
        cursor: "pointer",
      };
    }

    if (available) {
      return {
        ...baseStyle,
        background: "#e3f2fd",
        color: "#1976d2",
        cursor: "pointer",
        fontWeight: 500,
      };
    }

    return {
      ...baseStyle,
      background: "#f5f5f5",
      color: "#bdbdbd",
      cursor: "not-allowed",
    };
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <button
          onClick={previousMonth}
          style={styles.navBtn}
          onMouseEnter={(e) => (e.target.style.background = "#e0e0e0")}
          onMouseLeave={(e) => (e.target.style.background = "#f0f0f0")}
        >
          &lt;
        </button>
        <h3 style={styles.monthYear}>
          {monthNames[currentMonth.getMonth()]} {currentMonth.getFullYear()}
        </h3>
        <button
          onClick={nextMonth}
          style={styles.navBtn}
          onMouseEnter={(e) => (e.target.style.background = "#e0e0e0")}
          onMouseLeave={(e) => (e.target.style.background = "#f0f0f0")}
        >
          &gt;
        </button>
      </div>

      <div style={styles.weekdays}>
        <div style={styles.weekday}>Sun</div>
        <div style={styles.weekday}>Mon</div>
        <div style={styles.weekday}>Tue</div>
        <div style={styles.weekday}>Wed</div>
        <div style={styles.weekday}>Thu</div>
        <div style={styles.weekday}>Fri</div>
        <div style={styles.weekday}>Sat</div>
      </div>

      <div style={styles.grid}>{renderCalendar()}</div>
    </div>
  );
};

Calendar.propTypes = {
  availableDates: PropTypes.arrayOf(
    PropTypes.oneOfType([PropTypes.string, PropTypes.instanceOf(Date)])
  ).isRequired,
  onDateSelect: PropTypes.func.isRequired,
  selectedDate: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.instanceOf(Date),
  ]),
};

export default Calendar;
