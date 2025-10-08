package Singheatlh.springboot_backend.service;

import java.util.List;

import Singheatlh.springboot_backend.dto.QueueStatusDto;
import Singheatlh.springboot_backend.dto.QueueTicketDto;
import Singheatlh.springboot_backend.entity.enums.QueueStatus;

public interface QueueService {
    
    QueueTicketDto checkIn(Long appointmentId);
    
    QueueTicketDto getQueueTicketById(Long ticketId);
    
    QueueTicketDto getQueueTicketByAppointmentId(Long appointmentId);
    
    QueueStatusDto getQueueStatus(Long ticketId);
    
    List<QueueTicketDto> getActiveQueueByDoctor(Long doctorId);
    
    List<QueueTicketDto> getActiveQueueByClinic(Long clinicId);
    
    QueueTicketDto callNextQueue(Long doctorId);
    
    QueueTicketDto updateQueueStatus(Long ticketId, QueueStatus status);
    
    QueueTicketDto markAsCheckedIn(Long ticketId);
    
    QueueTicketDto markAsNoShow(Long ticketId);
    
    QueueTicketDto markAsCompleted(Long ticketId);
    
    QueueTicketDto fastTrackPatient(Long ticketId, String reason);
    
    void cancelQueueTicket(Long ticketId);
    
    List<QueueTicketDto> getQueueTicketsByPatientId(Long patientId);
    
    void processQueueNotifications(Long doctorId);
    
    Integer getCurrentServingNumber(Long doctorId);
    
    Long getActiveQueueCount(Long doctorId);
}
