package Singheatlh.springboot_backend.entity.enums;

public enum QueueStatus {
    // flow goes CHECKED_IN > CALLED > COMPLETED
    // also can be NO_SHOW 
    // Notifications for "3 away" and "next" are triggered programmatically in QueueService
    // without changing the patient's status

    CHECKED_IN,        // Patient checked in and waiting in queue
    CALLED,            // Patient's number has been called
    IN_CONSULTATION,   // Patient is currently with the doctor
    COMPLETED,         // Patient has completed consultation
    NO_SHOW,           // Patient didn't show up when called
    FAST_TRACKED       // Patient has been fast-tracked (priority)
}
