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
        dto.setIsFastTracked(queueTicket.getIsFastTracked());
        dto.setFastTrackReason(queueTicket.getFastTrackReason());
        
        // Get data from related Appointment entity (using helper methods)
        dto.setClinicId(queueTicket.getClinicId());
        dto.setDoctorId(queueTicket.getDoctorId());
        dto.setPatientId(queueTicket.getPatientId());
        
        // Set related entity information if loaded
        if (queueTicket.getAppointment() != null) {
            dto.setAppointmentDatetime(queueTicket.getAppointment().getStartDatetime());
            
            // Get patient name
            if (queueTicket.getAppointment().getPatient() != null) {
                dto.setPatientName(queueTicket.getAppointment().getPatient().getName());
            }
            
            // Get doctor and clinic information
            if (queueTicket.getAppointment().getDoctor() != null) {
                dto.setDoctorName(queueTicket.getAppointment().getDoctor().getName());
                
                if (queueTicket.getAppointment().getDoctor().getClinic() != null) {
                    dto.setClinicName(queueTicket.getAppointment().getDoctor().getClinic().getName());
                }
            }
        }
        
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
