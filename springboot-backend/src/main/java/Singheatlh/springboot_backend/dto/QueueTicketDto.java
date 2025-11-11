package Singheatlh.springboot_backend.dto;

import java.time.LocalDateTime;

import Singheatlh.springboot_backend.entity.enums.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueueTicketDto {
    
    private Integer ticketId;
    private String appointmentId;
    private QueueStatus status;
    private LocalDateTime checkInTime;
    private Integer queueNumber;
    private Boolean isFastTracked;
    private String fastTrackReason;
    private Integer ticketNumberForDay;
    private LocalDateTime consultationStartTime;
    private LocalDateTime consultationCompleteTime;
}
