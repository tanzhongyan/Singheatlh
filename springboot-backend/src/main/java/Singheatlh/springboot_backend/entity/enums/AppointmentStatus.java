package Singheatlh.springboot_backend.entity.enums;

public enum AppointmentStatus {
    SCHEDULED,    // Appointment is booked and confirmed
    CONFIRMED,    // Patient confirmed attendance
    IN_PROGRESS,  // Doctor is currently seeing the patient
    COMPLETED,    // Appointment finished successfully
    CANCELLED,    // Cancelled by patient or doctor
    NO_SHOW,      // Patient didn't show up
    RESCHEDULED   // Appointment moved to different time
}
