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
    
    private Long ticketId;
    private String appointmentId;
    private QueueStatus status;
    private LocalDateTime checkInTime;
    private Integer queueNumber;
    private Integer clinicId;
    private String clinicName;
    private String doctorId;
    private String doctorName;
    private java.util.UUID patientId;
    private String patientName;
    private Boolean isFastTracked;
    private String fastTrackReason;
    private LocalDateTime appointmentDatetime;
}
