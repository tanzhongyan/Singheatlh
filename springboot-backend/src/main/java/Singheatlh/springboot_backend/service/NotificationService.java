package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.entity.QueueTicket;

/**
 * Service interface for sending notifications to patients
 * Integrated with SMU Lab Notification Service for Email delivery
 */
public interface NotificationService {
    
    /**
     * Send email notification when patient is 3 positions away from being called
     * @param queueTicket The queue ticket of the patient to notify
     */
    void sendQueueNotification3Away(QueueTicket queueTicket);
    
    /**
     * Send email notification when patient is next in line
     * @param queueTicket The queue ticket of the patient to notify
     */
    void sendQueueNotificationNext(QueueTicket queueTicket);
    
    /**
     * Send email notification when patient's queue number is called
     * @param queueTicket The queue ticket of the patient to notify
     */
    void sendQueueCalledNotification(QueueTicket queueTicket);
    
    /**
     * Send email notification when patient has been fast-tracked with updated queue number
     * @param queueTicket The queue ticket of the fast-tracked patient to notify
     */
    void sendFastTrackNotification(QueueTicket queueTicket);
    
    /**
     * Send email notification when patient first checks in, informing them of their queue position
     * @param queueTicket The queue ticket of the patient who just checked in
     */
    void sendCheckInConfirmationNotification(QueueTicket queueTicket);
}
