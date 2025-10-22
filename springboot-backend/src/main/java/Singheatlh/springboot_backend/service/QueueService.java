package Singheatlh.springboot_backend.service;

import java.util.List;

import Singheatlh.springboot_backend.dto.QueueStatusDto;
import Singheatlh.springboot_backend.dto.QueueTicketDto;
import Singheatlh.springboot_backend.entity.enums.QueueStatus;

public interface QueueService {
    
    QueueTicketDto checkIn(String appointmentId);
    
    QueueTicketDto getQueueTicketById(Long ticketId);
    
    QueueTicketDto getQueueTicketByAppointmentId(String appointmentId);
    
    QueueStatusDto getQueueStatus(Long ticketId);
    
    List<QueueTicketDto> getActiveQueueByDoctor(String doctorId);
    
    List<QueueTicketDto> getActiveQueueByClinic(Integer clinicId);
    
    QueueTicketDto callNextQueue(String doctorId);
    
    QueueTicketDto updateQueueStatus(Long ticketId, QueueStatus status);
    
    QueueTicketDto markAsCheckedIn(Long ticketId);
    
    QueueTicketDto markAsNoShow(Long ticketId);
    
    QueueTicketDto markAsCompleted(Long ticketId);
    
    QueueTicketDto fastTrackPatient(Long ticketId, String reason);
    
    void cancelQueueTicket(Long ticketId);
    
    List<QueueTicketDto> getQueueTicketsByPatientId(java.util.UUID patientId);
    
    void processQueueNotifications(String doctorId);
    
    Integer getCurrentServingNumber(String doctorId);
    
    Long getActiveQueueCount(String doctorId);
}
