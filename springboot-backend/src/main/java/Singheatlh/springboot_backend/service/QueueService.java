package Singheatlh.springboot_backend.service;

import java.util.List;

import Singheatlh.springboot_backend.dto.QueueStatusDto;
import Singheatlh.springboot_backend.dto.QueueTicketDto;
import Singheatlh.springboot_backend.entity.enums.QueueStatus;

public interface QueueService {
    
    QueueTicketDto checkIn(String appointmentId);
    
    QueueTicketDto getQueueTicketById(Integer ticketId);
    
    QueueTicketDto getQueueTicketByAppointmentId(String appointmentId);
    
    QueueStatusDto getQueueStatus(Integer ticketId);
    
    List<QueueTicketDto> getActiveQueueByDoctor(String doctorId);
    
    List<QueueTicketDto> getActiveQueueByClinic(Integer clinicId);
    
    QueueTicketDto callNextQueue(String doctorId);
    
    QueueTicketDto updateQueueStatus(Integer ticketId, QueueStatus status);
    
    QueueTicketDto markAsCheckedIn(Integer ticketId);
    
    QueueTicketDto markAsNoShow(Integer ticketId);
    
    QueueTicketDto markAsCompleted(Integer ticketId);
    
    QueueTicketDto fastTrackPatient(Integer ticketId, String reason);
    
    List<QueueTicketDto> getQueueTicketsByPatientId(java.util.UUID patientId);
    
    void processQueueNotifications(String doctorId);
    
    Integer getCurrentServingTicketId(String doctorId);
    
    Long getActiveQueueCount(String doctorId);
    
    List<QueueTicketDto> getAllQueueTickets();
}
