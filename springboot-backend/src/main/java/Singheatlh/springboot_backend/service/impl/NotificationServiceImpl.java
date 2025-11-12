package Singheatlh.springboot_backend.service.impl;

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
import Singheatlh.springboot_backend.dto.NotificationContext;
import Singheatlh.springboot_backend.entity.QueueTicket;
import Singheatlh.springboot_backend.service.NotificationMessageBuilder;
import Singheatlh.springboot_backend.service.NotificationService;
import Singheatlh.springboot_backend.service.NotificationType;
import Singheatlh.springboot_backend.service.QueuePatientInfoService;

/**
 * Implementation of NotificationService
 * Integrated with SMU Lab Notification Service for sending Email notifications
 * REFACTORED: Now follows SRP - only responsible for coordinating notification sending
 * Uses Template Method pattern to eliminate DRY violations
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private QueuePatientInfoService patientInfoService;
    
    @Autowired
    private NotificationMessageBuilder messageBuilder;
    
    @Value("${smu.notification.api.base-url}")
    private String apiBaseUrl;
    
    @Value("${smu.notification.api.send-email-endpoint}")
    private String sendEmailEndpoint;
    
    // Template Method Pattern - eliminates DRY violations
    // All notification methods follow this same pattern
    
    @Override
    public void sendQueueNotification3Away(QueueTicket queueTicket) {
        sendNotification(queueTicket, NotificationType.THREE_AWAY);
    }
    
    @Override
    public void sendQueueNotificationNext(QueueTicket queueTicket) {
        sendNotification(queueTicket, NotificationType.NEXT);
    }
    
    @Override
    public void sendQueueCalledNotification(QueueTicket queueTicket) {
        sendNotification(queueTicket, NotificationType.CALLED);
    }
    
    @Override
    public void sendFastTrackNotification(QueueTicket queueTicket) {
        NotificationContext context = buildNotificationContext(queueTicket);
        context.setFastTrackReason(queueTicket.getFastTrackReason());
        
        String subject = messageBuilder.getSubject(NotificationType.FAST_TRACK);
        String message = messageBuilder.buildFastTrackMessage(context);
        
        sendEmail(queueTicket, subject, message);
    }
    
    @Override
    public void sendCheckInConfirmationNotification(QueueTicket queueTicket) {
        NotificationContext context = buildNotificationContext(queueTicket);
        
        Integer queuePosition = queueTicket.getQueueNumber();
        context.setQueuePosition(queuePosition);
        context.setFirstPosition(queuePosition != null && queuePosition == 1);
        
        // Calculate estimated people ahead
        int peopleAhead = (queuePosition != null && queuePosition > 0) ? queuePosition - 1 : 0;
        context.setPeopleAhead(peopleAhead);
        
        String subject = messageBuilder.getSubject(NotificationType.CHECK_IN_CONFIRMATION);
        String message = messageBuilder.buildCheckInConfirmationMessage(context);
        
        sendEmail(queueTicket, subject, message);
    }
    
    /**
     * Template Method - common notification sending pattern
     * Eliminates duplication across all notification methods
     */
    private void sendNotification(QueueTicket queueTicket, NotificationType type) {
        NotificationContext context = buildNotificationContext(queueTicket);
        String subject = messageBuilder.getSubject(type);
        String message = buildMessageForType(type, context);
        sendEmail(queueTicket, subject, message);
    }
    
    /**
     * Build notification context from queue ticket
     * Delegates data extraction to QueuePatientInfoService (SRP)
     */
    private NotificationContext buildNotificationContext(QueueTicket queueTicket) {
        String ticketNumber = queueTicket.getTicketNumberForDay() != null 
            ? queueTicket.getTicketNumberForDay().toString() 
            : "N/A";
        String patientName = patientInfoService.getPatientName(queueTicket);
        String doctorName = patientInfoService.getDoctorName(queueTicket);
        String appointmentDetails = patientInfoService.getAppointmentDetails(queueTicket);
        
        return new NotificationContext(ticketNumber, patientName, doctorName, appointmentDetails);
    }
    
    /**
     * Build message based on notification type
     * Delegates message building to NotificationMessageBuilder (SRP)
     */
    private String buildMessageForType(NotificationType type, NotificationContext context) {
        switch (type) {
            case THREE_AWAY:
                return messageBuilder.buildQueueNotification3AwayMessage(context);
            case NEXT:
                return messageBuilder.buildQueueNotificationNextMessage(context);
            case CALLED:
                return messageBuilder.buildQueueCalledMessage(context);
            case FAST_TRACK:
                return messageBuilder.buildFastTrackMessage(context);
            case CHECK_IN_CONFIRMATION:
                return messageBuilder.buildCheckInConfirmationMessage(context);
            default:
                return "Queue notification";
        }
    }
    
    /**
     * Send Email using SMU Lab Notification Service API
     * Now ONLY responsible for email sending (SRP)
     * @param queueTicket QueueTicket object containing appointment information
     * @param subject Email subject line
     * @param message Email message content
     */
    private void sendEmail(QueueTicket queueTicket, String subject, String message) {
        try {
            // Get patient email using the info service
            String email = patientInfoService.getPatientEmail(queueTicket);
            
            // Skip sending email if no valid email address found
            if (email == null || email.trim().isEmpty()) {
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
            
            ResponseEntity<EmailResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                EmailResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Email sent successfully
            }
            
        } catch (Exception e) {
            // Don't throw exception - notification failure shouldn't break queue operations
        }
    }
}
