package Singheatlh.springboot_backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import Singheatlh.springboot_backend.dto.EmailRequest;
import Singheatlh.springboot_backend.dto.EmailResponse;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.Patient;
import Singheatlh.springboot_backend.entity.QueueTicket;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.repository.PatientRepository;
import Singheatlh.springboot_backend.service.NotificationService;

/**
 * Implementation of NotificationService
 * Integrated with SMU Lab Notification Service for sending Email notifications
 * Currently uses hardcoded email address for testing
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Value("${smu.notification.api.base-url}")
    private String apiBaseUrl;
    
    @Value("${smu.notification.api.send-email-endpoint}")
    private String sendEmailEndpoint;
    
    @Override
    public void sendQueueNotification3Away(QueueTicket queueTicket) {
        String subject = "Queue Update - 3 Patients Away";
        String message = String.format(
            "Dear Patient,\n\n" +
            "Queue #%d: You are currently 3 patients away from being called. " +
            "Please proceed closer to the consultation room.\n\n" +
            "Thank you for your patience.",
            queueTicket.getQueueNumber()
        );
        
        // Send Email via SMU Lab Notification Service
        sendEmail(queueTicket, subject, message);
    }
    
    @Override
    public void sendQueueNotificationNext(QueueTicket queueTicket) {
        String subject = "Queue Update - You're Next!";
        String message = String.format(
            "Dear Patient,\n\n" +
            "Queue Number #%d: You are next in line. " +
            "Please be ready and stay close to the consultation room.\n\n" +
            "Thank you for your patience.",
            queueTicket.getQueueNumber()
        );
        
        // Send Email via SMU Lab Notification Service
        sendEmail(queueTicket, subject, message);
    }
    
    @Override
    public void sendQueueCalledNotification(QueueTicket queueTicket) {
        String subject = "Queue Called - Your Turn Now!";
        String message = String.format(
            "Dear Patient,\n\n" +
            "Queue #%d: It's your turn now. " +
            "Please proceed to the consultation room immediately.\n\n" +
            "Thank you for your cooperation.",
            queueTicket.getQueueNumber()
        );
        
        // Send Email via SMU Lab Notification Service
        sendEmail(queueTicket, subject, message);
    }
    
    @Override
    public void sendFastTrackNotification(QueueTicket queueTicket) {
        String subject = "Queue Update - You've Been Fast-Tracked!";
        String message = String.format(
            "Dear Patient,\n\n" +
            "Due to your situation! You have been fast-tracked in the queue. " +
            "Your new queue number is #%d.\n\n" +
            "Reason: %s\n\n" +
            "Please be ready as you will be called soon. " +
            "Stay close to the consultation room.\n\n" +
            "Thank you for your patience.",
            queueTicket.getQueueNumber(),
            queueTicket.getFastTrackReason() != null ? queueTicket.getFastTrackReason() : "Priority/Emergency"
        );
        
        // Send Email via SMU Lab Notification Service
        sendEmail(queueTicket, subject, message);
    }
    
    /**
     * Send Email using SMU Lab Notification Service API
     * Fetches patient email from User_Profile, falls back to hardcoded email if not found
     * @param queueTicket QueueTicket object containing appointment information
     * @param subject Email subject line
     * @param message Email message content
     */
    private void sendEmail(QueueTicket queueTicket, String subject, String message) {
        try {
            // Get patient ID - try helper method first, then fetch from appointment directly
            java.util.UUID patientId = queueTicket.getPatientId();
            
            // If helper method returns null, fetch appointment directly
            if (patientId == null) {
                logger.warn("⚠️ QueueTicket.getPatientId() returned null, fetching appointment directly");
                Appointment appointment = appointmentRepository.findById(queueTicket.getAppointmentId()).orElse(null);
                if (appointment != null) {
                    patientId = appointment.getPatientId();
                    logger.info("✅ Retrieved patientId from appointment: {}", patientId);
                } else {
                    logger.error("❌ Appointment {} not found!", queueTicket.getAppointmentId());
                }
            }
            
            // Get patient email from database
            String email = null;
            String patientName = "Patient";
            
            if (patientId != null) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && patient.getEmail() != null && !patient.getEmail().isEmpty()) {
                    email = patient.getEmail();
                    patientName = patient.getName() != null ? patient.getName() : "Patient";
                    logger.info("📧 Retrieved email for Patient {} ({}): {}", patientId, patientName, email);
                } else {
                    logger.warn("⚠️ Patient {} not found or has no email address", patientId);
                }
            } else {
                logger.warn("⚠️ Patient ID is null, cannot retrieve email address");
            }
            
            // Skip sending email if no valid email address found
            if (email == null || email.trim().isEmpty()) {
                logger.warn("⚠️ No valid email address found for patient {}, skipping notification", patientId);
                return;
            }
            
            // Prepare Email request
            EmailRequest emailRequest = new EmailRequest(email, subject, message);
            
            // Prepare HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Add authentication header if needed
            // headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<EmailRequest> requestEntity = new HttpEntity<>(emailRequest, headers);
            
            // Call SMU Lab Notification Service API
            String apiUrl = apiBaseUrl + sendEmailEndpoint;
            
            logger.info("📤 Sending Email to {} ({}) via {}", email, patientName, apiUrl);
            logger.debug("📧 Email Request - To: {}, Subject: {}, Message: {}", email, subject, message);
            
            ResponseEntity<EmailResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                EmailResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                EmailResponse emailResponse = response.getBody();
                String apiStatus = emailResponse.getStatus();
                boolean ok = apiStatus != null && apiStatus.trim().equalsIgnoreCase("Email Sent");
                logger.info("📬 Email API Response: status={}", apiStatus);

                if (ok) {
                    logger.info("✅ Email sent successfully to Patient {} ({}) at {}", 
                        patientId, patientName, email);
                } else {
                    logger.warn("⚠️ Email API indicated failure. status={}", apiStatus);
                }
            } else {
                logger.error("❌ Email API returned non-2xx status: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to send Email to Patient: {}", e.getMessage(), e);
            // Don't throw exception - notification failure shouldn't break queue operations
        }
    }
}
