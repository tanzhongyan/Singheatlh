package Singheatlh.springboot_backend.dto;

/**
 * Context object containing all data needed for building notification messages
 * Encapsulates notification parameters (Data Transfer Object pattern)
 */
public class NotificationContext {
    private String ticketNumberForDay;
    private String patientName;
    private String doctorName;
    private String appointmentDetails;
    private Integer queuePosition;
    private Integer peopleAhead;
    private String fastTrackReason;
    private boolean isFirstPosition;
    
    // Constructor for general notifications
    public NotificationContext(String ticketNumberForDay, String patientName, 
                              String doctorName, String appointmentDetails) {
        this.ticketNumberForDay = ticketNumberForDay;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDetails = appointmentDetails;
    }
    
    // Getters and Setters
    public String getTicketNumberForDay() {
        return ticketNumberForDay;
    }
    
    public void setTicketNumberForDay(String ticketNumberForDay) {
        this.ticketNumberForDay = ticketNumberForDay;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    public String getAppointmentDetails() {
        return appointmentDetails;
    }
    
    public void setAppointmentDetails(String appointmentDetails) {
        this.appointmentDetails = appointmentDetails;
    }
    
    public Integer getQueuePosition() {
        return queuePosition;
    }
    
    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
    
    public Integer getPeopleAhead() {
        return peopleAhead;
    }
    
    public void setPeopleAhead(Integer peopleAhead) {
        this.peopleAhead = peopleAhead;
    }
    
    public String getFastTrackReason() {
        return fastTrackReason;
    }
    
    public void setFastTrackReason(String fastTrackReason) {
        this.fastTrackReason = fastTrackReason;
    }
    
    public boolean isFirstPosition() {
        return isFirstPosition;
    }
    
    public void setFirstPosition(boolean firstPosition) {
        isFirstPosition = firstPosition;
    }
}

