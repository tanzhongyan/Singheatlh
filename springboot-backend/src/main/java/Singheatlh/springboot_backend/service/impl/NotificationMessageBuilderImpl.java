package Singheatlh.springboot_backend.service.impl;

import org.springframework.stereotype.Component;

import Singheatlh.springboot_backend.dto.NotificationContext;
import Singheatlh.springboot_backend.service.NotificationMessageBuilder;
import Singheatlh.springboot_backend.service.NotificationType;

/**
 * Responsible ONLY for building notification message templates
 */
@Component
public class NotificationMessageBuilderImpl implements NotificationMessageBuilder {
    
    @Override
    public String buildQueueNotification3AwayMessage(NotificationContext context) {
        return String.format(
            "Ticket Number: %s\n\n" +
            "Dear %s,\n\n" +
            "%s\n\n" +
            "Doctor: %s\n\n" +
            "You are currently 3 patients away from being called. " +
            "Please proceed closer to the consultation room.\n\n" +
            "Thank you for your patience.",
            context.getTicketNumberForDay(),
            context.getPatientName(),
            context.getAppointmentDetails(),
            context.getDoctorName()
        );
    }
    
    @Override
    public String buildQueueNotificationNextMessage(NotificationContext context) {
        return String.format(
            "Ticket Number: %s\n\n" +
            "Dear %s,\n\n" +
            "%s\n\n" +
            "Doctor: %s\n\n" +
            "You are next in line. " +
            "Please be ready and stay close to the consultation room.\n\n" +
            "Thank you for your patience.",
            context.getTicketNumberForDay(),
            context.getPatientName(),
            context.getAppointmentDetails(),
            context.getDoctorName()
        );
    }
    
    @Override
    public String buildQueueCalledMessage(NotificationContext context) {
        return String.format(
            "Ticket Number: %s\n\n" +
            "Dear %s,\n\n" +
            "%s\n\n" +
            "Doctor: %s\n\n" +
            "It's your turn now. " +
            "Please proceed to the consultation room immediately.\n\n" +
            "Thank you for your cooperation.",
            context.getTicketNumberForDay(),
            context.getPatientName(),
            context.getAppointmentDetails(),
            context.getDoctorName()
        );
    }
    
    @Override
    public String buildFastTrackMessage(NotificationContext context) {
        return String.format(
            "Ticket Number: %s\n\n" +
            "Dear %s,\n\n" +
            "%s\n\n" +
            "Doctor: %s\n\n" +
            "Due to your situation! You have been fast-tracked in the queue.\n\n" +
            "Reason: %s\n\n" +
            "Please be ready as you will be called soon. " +
            "Stay close to the consultation room.\n\n" +
            "Thank you for your patience.",
            context.getTicketNumberForDay(),
            context.getPatientName(),
            context.getAppointmentDetails(),
            context.getDoctorName(),
            context.getFastTrackReason() != null ? context.getFastTrackReason() : "Priority/Emergency"
        );
    }
    
    @Override
    public String buildCheckInConfirmationMessage(NotificationContext context) {
        // Different message if patient is immediately called vs. waiting in queue
        if (context.isFirstPosition()) {
            return String.format(
                "Ticket Number: %s\n\n" +
                "Dear %s,\n\n" +
                "%s\n\n" +
                "You have successfully checked in!\n\n" +
                "Great news! The doctor is ready to see you now. " +
                "Please proceed to the consultation room immediately.\n\n" +
                "Thank you for your promptness.",
                context.getTicketNumberForDay(),
                context.getPatientName(),
                context.getAppointmentDetails()
            );
        } else {
            return String.format(
                "Ticket Number: %s\n\n" +
                "Dear %s,\n\n" +
                "%s\n\n" +
                "You have successfully checked in!\n\n" +
                "Current Queue Position: %d\n" +
                "Number of patients ahead: %d\n\n" +
                "You will receive notifications as your turn approaches. " +
                "Please stay nearby and wait for further updates.\n\n" +
                "Thank you for your patience.",
                context.getTicketNumberForDay(),
                context.getPatientName(),
                context.getAppointmentDetails(),
                context.getQueuePosition() != null ? context.getQueuePosition() : 0,
                context.getPeopleAhead() != null ? context.getPeopleAhead() : 0
            );
        }
    }
    
    @Override
    public String getSubject(NotificationType type) {
        switch (type) {
            case THREE_AWAY:
                return "Queue Update - 3 Patients Away";
            case NEXT:
                return "Queue Update - You're Next!";
            case CALLED:
                return "Queue Called - Your Turn Now!";
            case FAST_TRACK:
                return "Queue Update - You've Been Fast-Tracked!";
            case CHECK_IN_CONFIRMATION:
                return "Check-in Confirmation";
            default:
                return "Queue Notification";
        }
    }
}

