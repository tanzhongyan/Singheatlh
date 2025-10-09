package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Singheatlh.springboot_backend.dto.QueueStatusDto;
import Singheatlh.springboot_backend.dto.QueueTicketDto;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.QueueTicket;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.entity.enums.QueueStatus;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.QueueTicketMapper;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.repository.QueueTicketRepository;
import Singheatlh.springboot_backend.service.NotificationService;
import Singheatlh.springboot_backend.service.QueueService;

@Service
@Transactional
public class QueueServiceImpl implements QueueService {
    
    @Autowired
    private QueueTicketRepository queueTicketRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private QueueTicketMapper queueTicketMapper;
    
    @Autowired(required = false)
    private NotificationService notificationService;

    @Override
    public QueueTicketDto checkIn(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundExecption("Appointment not found with id: " + appointmentId));
        
        if (appointment.getStatus() != AppointmentStatus.Upcoming) {
            throw new IllegalStateException("Cannot check in. Appointment status is: " + appointment.getStatus());
        }
        
        if (queueTicketRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new IllegalStateException("Appointment already checked in");
        }
        
        // Generate queue number if appointment is valid (appt found and not checked in)
        LocalDateTime now = LocalDateTime.now();
        Integer maxQueueNumber = queueTicketRepository.findMaxQueueNumberByDoctorIdAndDate(
            appointment.getDoctorId(), now);
        Integer newQueueNumber = (maxQueueNumber == null) ? 1 : maxQueueNumber + 1;
        
        // Create queue ticket
        QueueTicket queueTicket = new QueueTicket(
            appointmentId,
            now,
            newQueueNumber,
            appointment.getClinicId(),
            appointment.getDoctorId(),
            appointment.getPatientId()
        );
        
        queueTicket = queueTicketRepository.save(queueTicket);
        
        appointment.setStatus(AppointmentStatus.Ongoing);
        appointmentRepository.save(appointment);
        
        processQueueNotifications(appointment.getDoctorId());
        
        return queueTicketMapper.toDto(queueTicket);
    }

    @Override
    public QueueTicketDto getQueueTicketById(Long ticketId) {
        QueueTicket queueTicket = queueTicketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundExecption("Queue ticket not found with id: " + ticketId));
        return queueTicketMapper.toDto(queueTicket);
    }

    @Override
    public QueueTicketDto getQueueTicketByAppointmentId(Long appointmentId) {
        QueueTicket queueTicket = queueTicketRepository.findByAppointmentId(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundExecption("Queue ticket not found for appointment id: " + appointmentId));
        return queueTicketMapper.toDto(queueTicket);
    }

    @Override
    public QueueStatusDto getQueueStatus(Long ticketId) {
        QueueTicket queueTicket = queueTicketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundExecption("Queue ticket not found with id: " + ticketId));
        
        Integer currentServingNumber = getCurrentServingNumber(queueTicket.getDoctorId());
        
        List<QueueTicket> activeQueue = queueTicketRepository.findActiveQueueByDoctorIdAndDate(
            queueTicket.getDoctorId(), queueTicket.getCheckInTime());
        
        // Find position in queue (considering fast-tracked patients)
        int position = 0;
        for (QueueTicket ticket : activeQueue) {
            if (ticket.getTicketId().equals(ticketId)) {
                position++;
                break;
            }
            position++;
        }
        
        // status message DTO built here 
        String message = buildQueueStatusMessage(queueTicket, currentServingNumber, position);
        
        QueueStatusDto statusDto = new QueueStatusDto();
        statusDto.setTicketId(ticketId);
        statusDto.setQueueNumber(queueTicket.getQueueNumber());
        statusDto.setCurrentQueueNumber(currentServingNumber);
        statusDto.setPositionInQueue(position);
        statusDto.setStatus(queueTicket.getStatus().toString());
        statusDto.setMessage(message);
        
        return statusDto;
    }

    @Override
    public List<QueueTicketDto> getActiveQueueByDoctor(Long doctorId) {
        LocalDateTime today = LocalDateTime.now();
        List<QueueTicket> activeQueue = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);
        return activeQueue.stream()
            .map(queueTicketMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<QueueTicketDto> getActiveQueueByClinic(Long clinicId) {
        LocalDateTime today = LocalDateTime.now();
        List<QueueTicket> activeQueue = queueTicketRepository.findActiveQueueByClinicIdAndDate(clinicId, today);
        return activeQueue.stream()
            .map(queueTicketMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public QueueTicketDto callNextQueue(Long doctorId) {
        LocalDateTime today = LocalDateTime.now();
        
        List<QueueTicket> activeQueue = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);
        
        if (activeQueue.isEmpty()) {
            throw new IllegalStateException("No patients in queue");
        }
        
        // mark current patient as completted if exists, move queue by 1
        List<QueueTicket> currentlyServing = queueTicketRepository.findCurrentQueueNumberByDoctorIdAndDate(doctorId, today);
        for (QueueTicket serving : currentlyServing) {
            if (serving.getStatus() == QueueStatus.IN_CONSULTATION) {
                serving.setStatus(QueueStatus.COMPLETED);
                queueTicketRepository.save(serving);
                
                // Update appointment status
                Appointment appointment = appointmentRepository.findById(serving.getAppointmentId()).orElse(null);
                if (appointment != null) {
                    appointment.setStatus(AppointmentStatus.Completed);
                    appointmentRepository.save(appointment);
                }
            }
        }
        
        QueueTicket nextTicket = activeQueue.stream()
            .filter(ticket -> ticket.getStatus() == QueueStatus.WAITING)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No waiting patients in queue"));
        
        nextTicket.setStatus(QueueStatus.CALLED);
        nextTicket = queueTicketRepository.save(nextTicket);
        
        // Process notifications for remaining patients. TO COMPLETE NOTIFICATIONS
        processQueueNotifications(doctorId);
        
        return queueTicketMapper.toDto(nextTicket);
    }

    @Override
    public QueueTicketDto updateQueueStatus(Long ticketId, QueueStatus status) {
        QueueTicket queueTicket = queueTicketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundExecption("Queue ticket not found with id: " + ticketId));
        
        queueTicket.setStatus(status);
        queueTicket = queueTicketRepository.save(queueTicket);
        
        // Update appointment status if needed
        if (status == QueueStatus.COMPLETED || status == QueueStatus.NO_SHOW) {
            Appointment appointment = appointmentRepository.findById(queueTicket.getAppointmentId()).orElse(null);
            if (appointment != null) {
                appointment.setStatus(status == QueueStatus.COMPLETED 
                    ? AppointmentStatus.Completed 
                    : AppointmentStatus.Missed);
                appointmentRepository.save(appointment);
            }
        }
        
        // Process notifications if queue progressed. . TO COMPLETE NOTIFICATIONS
        if (status == QueueStatus.COMPLETED || status == QueueStatus.NO_SHOW) {
            processQueueNotifications(queueTicket.getDoctorId());
        }
        
        return queueTicketMapper.toDto(queueTicket);
    }

    @Override
    public QueueTicketDto markAsCheckedIn(Long ticketId) {
        return updateQueueStatus(ticketId, QueueStatus.WAITING);
    }

    @Override
    public QueueTicketDto markAsNoShow(Long ticketId) {
        return updateQueueStatus(ticketId, QueueStatus.NO_SHOW);
    }

    @Override
    public QueueTicketDto markAsCompleted(Long ticketId) {
        return updateQueueStatus(ticketId, QueueStatus.COMPLETED);
    }

    @Override
    public QueueTicketDto fastTrackPatient(Long ticketId, String reason) {
        QueueTicket queueTicket = queueTicketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundExecption("Queue ticket not found with id: " + ticketId));
        
        // Validate status
        if (queueTicket.getStatus() != QueueStatus.WAITING) {
            throw new IllegalStateException("Cannot fast-track patient with status: " + queueTicket.getStatus());
        }
        
        queueTicket.setIsFastTracked(true);
        queueTicket.setFastTrackReason(reason);
        queueTicket.setStatus(QueueStatus.FAST_TRACKED);
        queueTicket = queueTicketRepository.save(queueTicket);
        
        // Reprocess notifications as queue order changed. TO COMPLETE NOTIFICATIONS
        processQueueNotifications(queueTicket.getDoctorId());
        
        return queueTicketMapper.toDto(queueTicket);
    }

    @Override
    public void cancelQueueTicket(Long ticketId) {
        QueueTicket queueTicket = queueTicketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundExecption("Queue ticket not found with id: " + ticketId));
        
        queueTicket.setStatus(QueueStatus.CANCELLED);
        queueTicketRepository.save(queueTicket);
        
        // Update appointment status
        Appointment appointment = appointmentRepository.findById(queueTicket.getAppointmentId()).orElse(null);
        if (appointment != null) {
            appointment.setStatus(AppointmentStatus.Cancelled);
            appointmentRepository.save(appointment);
        }
        
        // Reprocess notifications. TO COMPLETE NOTIFICATIONS
        processQueueNotifications(queueTicket.getDoctorId());
    }

    @Override
    public List<QueueTicketDto> getQueueTicketsByPatientId(Long patientId) {
        List<QueueTicket> queueTickets = queueTicketRepository.findByPatientId(patientId);
        return queueTickets.stream()
            .map(queueTicketMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public void processQueueNotifications(Long doctorId) {
        LocalDateTime today = LocalDateTime.now();
        
        // Get current serving number
        Integer currentServingNumber = getCurrentServingNumber(doctorId);
        if (currentServingNumber == null) {
            return;
        }
        
        // Notify patients 3 away (without changing status)
        Integer notify3AwayNumber = currentServingNumber + 3;
        List<QueueTicket> ticketsToNotify3Away = queueTicketRepository.findTicketsToNotify3Away(
            doctorId, today, notify3AwayNumber);
        
        for (QueueTicket ticket : ticketsToNotify3Away) {

            // Notification service will handle sending alerts to patients
            if (notificationService != null) {
                // notificationService.sendQueueNotification3Away(ticket); 
            }
        }
        
        // Notify next patient (without changing status)
        Integer nextQueueNumber = currentServingNumber + 1;
        queueTicketRepository.findTicketToNotifyNext(doctorId, today, nextQueueNumber)
            .ifPresent(ticket -> {
                // Send notification without changing status
                // Notification service will handle sending alerts to patients
                if (notificationService != null) {
                    // notificationService.sendQueueNotificationNext(ticket);
                }
            });
    }

    @Override
    public Integer getCurrentServingNumber(Long doctorId) {
        LocalDateTime today = LocalDateTime.now();
        List<QueueTicket> currentlyServing = queueTicketRepository.findCurrentQueueNumberByDoctorIdAndDate(doctorId, today);
        
        if (!currentlyServing.isEmpty()) {
            return currentlyServing.get(0).getQueueNumber();
        }
        
        // If no one is currently being served, return the last completed number
        List<QueueTicket> allToday = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);
        if (allToday.isEmpty()) {
            return 0;
        }
        
        // if queue is empty or not started
        return 0;
    }

    @Override
    public Long getActiveQueueCount(Long doctorId) {
        LocalDateTime today = LocalDateTime.now();
        return queueTicketRepository.countActiveQueueByDoctorIdAndDate(doctorId, today);
    }
    
    // to work in conjuction with notification service, message is returned already
    private String buildQueueStatusMessage(QueueTicket queueTicket, Integer currentNumber, int position) {
        switch (queueTicket.getStatus()) {
            case WAITING:
                if (position <= 3) {
                    return "You are " + position + " patient(s) away. Please proceed closer to the consultation room.";
                }
                return "You are Queue #" + queueTicket.getQueueNumber() + ", currently serving #" + currentNumber;
            
            case CALLED:
                return "It's your turn. Kindly enter the consultation room.";
            
            case IN_CONSULTATION:
                return "You are currently with the doctor.";
            
            case COMPLETED:
                return "Your consultation is completed. Thank you!";
            
            case NO_SHOW:
                return "You were marked as no-show. Please check in again if you're present.";
            
            case CANCELLED:
                return "Your queue ticket has been cancelled.";
            
            case FAST_TRACKED:
                return "You have been fast-tracked. You will be called soon.";
            
            default:
                return "Queue #" + queueTicket.getQueueNumber();
        }
    }
}
