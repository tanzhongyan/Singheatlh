package Singheatlh.springboot_backend.mapper;

import org.springframework.stereotype.Component;

import Singheatlh.springboot_backend.dto.QueueTicketDto;
import Singheatlh.springboot_backend.entity.QueueTicket;

@Component
public class QueueTicketMapper {
    
    // Simplified mapper to match the minimal QueueTicketDto
    public QueueTicketDto toDto(QueueTicket queueTicket) {
        if (queueTicket == null) {
            return null;
        }
        
        QueueTicketDto dto = new QueueTicketDto();
        dto.setTicketId(queueTicket.getTicketId());
        dto.setAppointmentId(queueTicket.getAppointmentId());
        dto.setStatus(queueTicket.getStatus());
        dto.setCheckInTime(queueTicket.getCheckInTime());
        dto.setQueueNumber(queueTicket.getQueueNumber());
        dto.setIsFastTracked(queueTicket.getIsFastTracked());
        dto.setFastTrackReason(queueTicket.getFastTrackReason());
        dto.setTicketNumberForDay(queueTicket.getTicketNumberForDay());
        dto.setConsultationStartTime(queueTicket.getConsultationStartTime());
        dto.setConsultationCompleteTime(queueTicket.getConsultationCompleteTime());
        
        return dto;
    }
    
    public QueueTicket toEntity(QueueTicketDto dto) {
        if (dto == null) {
            return null;
        }
        
        // Use simplified constructor (no longer needs clinicId, doctorId, patientId)
        return new QueueTicket(
            dto.getAppointmentId(),
            dto.getCheckInTime(),
            dto.getQueueNumber()
        );
    }
}
