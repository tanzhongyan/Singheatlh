package Singheatlh.springboot_backend.service.impl;

import java.util.Optional;
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
    public QueueTicketDto checkIn(String appointmentId) {

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
        
        // Create queue ticket with simplified constructor
        QueueTicket queueTicket = new QueueTicket(
            appointmentId,
            now,
            newQueueNumber
        );
        
        // If patient is first in queue (queue_number = 1), automatically set to CALLED
        if (newQueueNumber == 1) {
            queueTicket.setStatus(QueueStatus.CALLED);
        }
        
        queueTicket = queueTicketRepository.save(queueTicket);
        
        appointment.setStatus(AppointmentStatus.Ongoing);
        appointmentRepository.save(appointment);
        
        if (newQueueNumber == 1 && notificationService != null) {
            notificationService.sendQueueCalledNotification(queueTicket);
        }
        
        // Process notifications for other patients in queue. tO COMPLETE IN NOTIFICATIONSERVICE
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
    public QueueTicketDto getQueueTicketByAppointmentId(String appointmentId) {
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
    public List<QueueTicketDto> getActiveQueueByDoctor(String doctorId) {
        LocalDateTime today = LocalDateTime.now();
        List<QueueTicket> activeQueue = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);
        return activeQueue.stream()
            .map(queueTicketMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<QueueTicketDto> getActiveQueueByClinic(Integer clinicId) {
        LocalDateTime today = LocalDateTime.now();
        List<QueueTicket> activeQueue = queueTicketRepository.findActiveQueueByClinicIdAndDate(clinicId, today);
        return activeQueue.stream()
            .map(queueTicketMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public QueueTicketDto callNextQueue(String doctorId) {
        LocalDateTime today = LocalDateTime.now();
        
        List<QueueTicket> activeQueue = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);
        
        if (activeQueue.isEmpty()) {
            throw new IllegalStateException("No patients in queue");
        }
        
        // Mark current patient as completed if exists
        List<QueueTicket> currentlyServing = queueTicketRepository.findCurrentQueueNumberByDoctorIdAndDate(doctorId, today);
        for (QueueTicket serving : currentlyServing) {
            if (serving.getStatus() == QueueStatus.CALLED) {
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
        
        // to prevent deadlock check if theres others in q if not return null
        Optional<QueueTicket> nextTicketOptional = activeQueue.stream()
            .filter(ticket -> ticket.getStatus() == QueueStatus.CHECKED_IN)
            .findFirst();
        
        if (nextTicketOptional.isPresent()) {
            QueueTicket nextTicket = nextTicketOptional.get();
            nextTicket.setStatus(QueueStatus.CALLED);
            nextTicket = queueTicketRepository.save(nextTicket);
            
            processQueueNotifications(doctorId);
            
            return queueTicketMapper.toDto(nextTicket);
        }
        
        // q empty
        return null;
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
        return updateQueueStatus(ticketId, QueueStatus.CHECKED_IN);
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
        if (queueTicket.getStatus() != QueueStatus.CHECKED_IN) {
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
        
        // Set status to NO_SHOW to indicate patient is no longer in queue
        queueTicket.setStatus(QueueStatus.NO_SHOW);
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
    public List<QueueTicketDto> getQueueTicketsByPatientId(java.util.UUID patientId) {
        List<QueueTicket> queueTickets = queueTicketRepository.findByPatientId(patientId);
        return queueTickets.stream()
            .map(queueTicketMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public void processQueueNotifications(String doctorId) {
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
            // Send notification without changing status
            // Notification service will handle sending alerts to patients
            if (notificationService != null) {
                notificationService.sendQueueNotification3Away(ticket); 
            }
        }
        
        // Notify next patient (without changing status)
        Integer nextQueueNumber = currentServingNumber + 1;
        queueTicketRepository.findTicketToNotifyNext(doctorId, today, nextQueueNumber)
            .ifPresent(ticket -> {
                // Notification service will handle sending alerts to patients
                if (notificationService != null) {
                    notificationService.sendQueueNotificationNext(ticket);
                }
            });
    }

    @Override
    public Integer getCurrentServingNumber(String doctorId) {
        LocalDateTime today = LocalDateTime.now();
        List<QueueTicket> currentlyServing = queueTicketRepository.findCurrentQueueNumberByDoctorIdAndDate(doctorId, today);
        
        if (!currentlyServing.isEmpty()) {

            return currentlyServing.get(0).getQueueNumber();
        }
        
        // If no one is currently being served, check if queue has started today
        // by looking for any completed patients
        List<QueueTicket> allToday = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);
        if (allToday.isEmpty()) {
            return 0;
        }
        
        // Queue exists but no one is actively being served
        // Return 0 to indicate waiting for first patient to be called
        return 0;
    }

    @Override
    public Long getActiveQueueCount(String doctorId) {
        LocalDateTime today = LocalDateTime.now();
        return queueTicketRepository.countActiveQueueByDoctorIdAndDate(doctorId, today);
    }
    
    // to work in conjuction with notification service, message is returned already
    private String buildQueueStatusMessage(QueueTicket queueTicket, Integer currentNumber, int position) {
        switch (queueTicket.getStatus()) {
            case CHECKED_IN:
                if (position <= 3) {
                    return "You are " + position + " patient(s) away. Please proceed closer to the consultation room.";
                }
                return "You are Queue #" + queueTicket.getQueueNumber() + ", currently serving #" + currentNumber;
            
            case CALLED:
                return "It's your turn. Kindly enter the consultation room.";
            
            case COMPLETED:
                return "Your consultation is completed. Thank you!";
            
            case NO_SHOW:
                return "You were marked as no-show. Please check in again if you're present.";
            
            case FAST_TRACKED:
                return "You have been fast-tracked. You will be called soon.";
            
            default:
                return "Queue #" + queueTicket.getQueueNumber();
        }
    }
}
