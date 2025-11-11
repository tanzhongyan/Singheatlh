package Singheatlh.springboot_backend.entity.enums;

public enum QueueStatus {
    // flow goes CHECKED_IN > CALLED > COMPLETED
    // also can be CHECKED_IN > NO_SHOW 
    // or can also bE FAST_TRACKED > COMPLETED
    // Notifications for "3 away" and "next" are triggered programmatically in QueueService
    // without changing the patient's status

    CHECKED_IN,        // Patient checked in and waiting in queue
    CALLED,            // Patient's number has been called and is being seen by doctor
    COMPLETED,         // Patient has completed consultation
    NO_SHOW,           // Patient didn't show up when called
    FAST_TRACKED       // Patient has been fast-tracked (priority)
}
