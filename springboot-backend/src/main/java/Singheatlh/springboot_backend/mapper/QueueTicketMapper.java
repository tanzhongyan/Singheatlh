package Singheatlh.springboot_backend.mapper;

import org.springframework.stereotype.Component;

import Singheatlh.springboot_backend.dto.QueueTicketDto;
import Singheatlh.springboot_backend.entity.QueueTicket;

@Component
public class QueueTicketMapper {
    
    // queueTicketDto is built here, the QueueStatusDto is populated in QueueServiceImpl instead due to flow
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
        dto.setClinicId(queueTicket.getClinicId());
        dto.setDoctorId(queueTicket.getDoctorId());
        dto.setPatientId(queueTicket.getPatientId());
        dto.setIsFastTracked(queueTicket.getIsFastTracked());
        dto.setFastTrackReason(queueTicket.getFastTrackReason());
        
        // set related entity information if loaded
        if (queueTicket.getAppointment() != null) {
            dto.setAppointmentDatetime(queueTicket.getAppointment().getAppointmentDatetime());
            
            if (queueTicket.getAppointment().getPatient() != null) {
                dto.setPatientName(queueTicket.getAppointment().getPatient().getName());
            }
        }
        
        return dto;
    }
    
    public QueueTicket toEntity(QueueTicketDto dto) {
        if (dto == null) {
            return null;
        }
        
        return new QueueTicket(
            dto.getAppointmentId(),
            dto.getCheckInTime(),
            dto.getQueueNumber(),
            dto.getClinicId(),
            dto.getDoctorId(),
            dto.getPatientId()
        );
    }
}
