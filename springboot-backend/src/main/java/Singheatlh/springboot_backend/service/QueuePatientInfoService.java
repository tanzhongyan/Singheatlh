package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.entity.QueueTicket;

/**
 * Separates data extraction logic from notification logic 
 */
public interface QueuePatientInfoService {
    
    /**
     * Get patient name from queue ticket
     * @param queueTicket The queue ticket
     * @return Patient name or "Patient" as fallback
     */
    String getPatientName(QueueTicket queueTicket);
    
    /**
     * Get doctor name from queue ticket
     * @param queueTicket The queue ticket
     * @return Doctor name or "Unknown Doctor" as fallback
     */
    String getDoctorName(QueueTicket queueTicket);
    
    /**
     * Get formatted appointment details from queue ticket
     * @param queueTicket The queue ticket
     * @return Formatted appointment details string
     */
    String getAppointmentDetails(QueueTicket queueTicket);
    
    /**
     * Get patient email from queue ticket
     * @param queueTicket The queue ticket
     * @return Patient email or null if not found
     */
    String getPatientEmail(QueueTicket queueTicket);
}

