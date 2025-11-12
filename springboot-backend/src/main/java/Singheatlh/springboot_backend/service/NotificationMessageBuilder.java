package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.NotificationContext;

/**
 * Builder class responsible for creating notification message content
 * Separates message template logic from notification sending logic
 */
public interface NotificationMessageBuilder {
    
    /**
     * Build message for 3-away notification
     */
    String buildQueueNotification3AwayMessage(NotificationContext context);
    
    /**
     * Build message for next-in-line notification
     */
    String buildQueueNotificationNextMessage(NotificationContext context);
    
    /**
     * Build message for queue called notification
     */
    String buildQueueCalledMessage(NotificationContext context);
    
    /**
     * Build message for fast-track notification
     */
    String buildFastTrackMessage(NotificationContext context);
    
    /**
     * Build message for check-in confirmation
     */
    String buildCheckInConfirmationMessage(NotificationContext context);
    
    /**
     * Get subject line for notification type
     */
    String getSubject(NotificationType type);
}

