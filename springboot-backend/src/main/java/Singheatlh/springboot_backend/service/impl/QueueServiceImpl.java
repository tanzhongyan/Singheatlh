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
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundExecption("Appointment not found with id: " + appointmentId));
            
            LocalDateTime now = LocalDateTime.now();

            // ENFORCEMENT OF CHECK FOR DATE OF APPOINTMENT
            int test = 1;
            if(test == 1){
                LocalDateTime appointmentDate = appointment.getStartDatetime().toLocalDate().atStartOfDay();
                LocalDateTime currentDate = now.toLocalDate().atStartOfDay();
                
                if (currentDate.isAfter(appointmentDate)) {
                    throw new IllegalStateException("Check-in failed: Appointment date has passed. " +
                        "Appointment was scheduled for " + appointment.getStartDatetime().toLocalDate() + 
                        " but today is " + now.toLocalDate() + ". Please contact the clinic to reschedule.");
                }
            }
            
            // Check appointment status
            switch (appointment.getStatus()) {
                case Completed:
                    throw new IllegalStateException("Check-in failed: This appointment has already been completed on " + 
                        appointment.getStartDatetime().toLocalDate() + ". Cannot check in for a completed appointment.");
                
                case Ongoing:
                    throw new IllegalStateException("Check-in failed: This appointment is already ongoing. " +
                        "You have already checked in and are currently being served or waiting in queue.");
                
                case Missed:
                    throw new IllegalStateException("Check-in failed: This appointment was marked as missed on " + 
                        appointment.getStartDatetime().toLocalDate() + ". Please contact the clinic to reschedule.");
                
                case Cancelled:
                    throw new IllegalStateException("Check-in failed: This appointment has been cancelled. " +
                        "Please contact the clinic to reschedule if you still need medical attention.");
                
                case Upcoming:
                    // Valid status, continue with time validation
                    break;
                
                default:
                    throw new IllegalStateException("Check-in failed: Invalid appointment status: " + appointment.getStatus());
            }
            
            // if already have ticket
            if (queueTicketRepository.findByAppointmentId(appointmentId).isPresent()) {
                throw new IllegalStateException("Check-in failed: You have already checked in for this appointment. " +
                    "Please check your queue status or contact clinic staff if you need assistance.");
            }
            
            Integer maxQueueNumber = queueTicketRepository.findMaxQueueNumberByDoctorIdAndDate(
                appointment.getDoctorId(), now);
            Integer newQueueNumber = (maxQueueNumber == null) ? 1 : maxQueueNumber + 1;

            QueueTicket queueTicket = new QueueTicket(
                appointmentId,
                now,
                newQueueNumber
            );
            
            if (newQueueNumber == 1) {
                queueTicket.setStatus(QueueStatus.CALLED);
            }
            
            queueTicket = queueTicketRepository.save(queueTicket);
            
            appointment.setStatus(AppointmentStatus.Ongoing);
            appointmentRepository.save(appointment);
            
            // Reload the queue ticket with appointment relationship eagerly loaded
            queueTicket = queueTicketRepository.findByIdWithAppointment(queueTicket.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundExecption("Queue ticket not found after save"));
            
            if (newQueueNumber == 1 || newQueueNumber == 2 || newQueueNumber == 4) {
                processQueueNotifications(appointment.getDoctorId());
            }
            
            return queueTicketMapper.toDto(queueTicket);
            
        } catch (ResourceNotFoundExecption e) {
            throw e; 
        } catch (IllegalStateException e) {
            throw e; 
        } catch (Exception e) {
            throw new IllegalStateException("Check-in failed due to an unexpected error: " + e.getMessage() + 
                ". Please contact clinic staff for assistance.", e);
        }
    }

    @Override
    public QueueTicketDto getQueueTicketById(Integer ticketId) {
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
    public QueueStatusDto getQueueStatus(Integer ticketId) {
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
        
        // Populate names from appointment relationship
        if (queueTicket.getAppointment() != null) {
            if (queueTicket.getAppointment().getPatient() != null) {
                statusDto.setPatientName(queueTicket.getAppointment().getPatient().getName());
            }
            if (queueTicket.getAppointment().getDoctor() != null) {
                statusDto.setDoctorName(queueTicket.getAppointment().getDoctor().getName());
                if (queueTicket.getAppointment().getDoctor().getClinic() != null) {
                    statusDto.setClinicName(queueTicket.getAppointment().getDoctor().getClinic().getName());
                }
            }
        }
        
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
        try {
            LocalDateTime today = LocalDateTime.now();
            
            if (doctorId == null || doctorId.trim().isEmpty()) {
                throw new IllegalArgumentException("Call next failed: Doctor ID cannot be null or empty.");
            }
            
            List<QueueTicket> activeQueue = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);
            
            if (activeQueue.isEmpty()) {
                throw new IllegalStateException("Call next failed: No patients are currently in the queue for doctor " + doctorId + 
                    " on " + today.toLocalDate() + ". Please ensure patients have checked in before calling next.");
            }
            
            // Mark current patient as completed if exists
            List<QueueTicket> currentlyServing = queueTicketRepository.findCurrentQueueNumberByDoctorIdAndDate(doctorId, today);
            for (QueueTicket serving : currentlyServing) {
                if (serving.getStatus() == QueueStatus.CALLED) {
                    serving.setStatus(QueueStatus.COMPLETED);
                    serving.setQueueNumber(0); // if no one in q = 0
                    queueTicketRepository.save(serving);
                    
                    Appointment appointment = appointmentRepository.findById(serving.getAppointmentId()).orElse(null);
                    if (appointment != null) {
                        appointment.setStatus(AppointmentStatus.Completed);
                        appointmentRepository.save(appointment);
                    }
                }
            }
            
            // Get all tickets for this doctor today to decrement queue numbers
            List<QueueTicket> allTicketsToday = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);
            
            for (QueueTicket ticket : allTicketsToday) {
                if (ticket.getStatus() != QueueStatus.COMPLETED && ticket.getStatus() != QueueStatus.NO_SHOW) {
                    ticket.setQueueNumber(ticket.getQueueNumber() - 1);
                    queueTicketRepository.save(ticket);
                }
            }
            
            // Refresh the active queue list after updates
            activeQueue = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);
            
            // Find the next patient to call (either CHECKED_IN or FAST_TRACKED status)
            Optional<QueueTicket> nextTicketOptional = activeQueue.stream()
                .filter(ticket -> ticket.getStatus() == QueueStatus.CHECKED_IN || ticket.getStatus() == QueueStatus.FAST_TRACKED)
                .findFirst();
            

                
            if (nextTicketOptional.isPresent()) {
                QueueTicket nextTicket = nextTicketOptional.get();
                nextTicket.setStatus(QueueStatus.CALLED);
                nextTicket = queueTicketRepository.save(nextTicket);
                
                // Process notifications
                processQueueNotifications(doctorId);
                
                return queueTicketMapper.toDto(nextTicket);
            }
            
            // No more patients to call
            return null;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Call next failed due to an unexpected error: " + e.getMessage() + 
                ". Please contact system administrator for assistance.", e);
        }
    }

    @Override
    public QueueTicketDto updateQueueStatus(Integer ticketId, QueueStatus status) {
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
        
        if (status == QueueStatus.COMPLETED || status == QueueStatus.NO_SHOW) {
            processQueueNotifications(queueTicket.getDoctorId());
        }
        
        return queueTicketMapper.toDto(queueTicket);
    }

    @Override
    public QueueTicketDto markAsCheckedIn(Integer ticketId) {
        return updateQueueStatus(ticketId, QueueStatus.CHECKED_IN);
    }

    @Override
    public QueueTicketDto markAsNoShow(Integer ticketId) {
        return updateQueueStatus(ticketId, QueueStatus.NO_SHOW);
    }

    @Override
    public QueueTicketDto markAsCompleted(Integer ticketId) {
        return updateQueueStatus(ticketId, QueueStatus.COMPLETED);
    }

    @Override
    public QueueTicketDto fastTrackPatient(Integer ticketId, String reason) {
        QueueTicket queueTicket = queueTicketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundExecption("Queue ticket not found with id: " + ticketId));
        
        // Validate status: allow fast-track when CHECKED_IN or already FAST_TRACKED (reordering)
        if (queueTicket.getStatus() != QueueStatus.CHECKED_IN && queueTicket.getStatus() != QueueStatus.FAST_TRACKED) {
            throw new IllegalStateException("Cannot fast-track patient with status: " + queueTicket.getStatus());
        }
        
        String doctorId = queueTicket.getDoctorId();
        LocalDateTime today = LocalDateTime.now();

        List<QueueTicket> activeQueue = queueTicketRepository.findActiveQueueByDoctorIdAndDate(doctorId, today);

        // Build list of existing fast-tracked tickets (excluding current)
        List<QueueTicket> existingFastTracked = activeQueue.stream()
            .filter(ticket -> ticket.getIsFastTracked() != null && ticket.getIsFastTracked())
            .filter(ticket -> !ticket.getTicketId().equals(ticketId)) // Exclude current ticket
            .sorted((t1, t2) -> Integer.compare(t1.getQueueNumber(), t2.getQueueNumber()))
            .collect(Collectors.toList());
        
        boolean hasCurrentlyServed = activeQueue.stream()
            .anyMatch(ticket -> ticket.getStatus() == QueueStatus.CALLED);
        
        Integer desiredFront = hasCurrentlyServed ? 2 : 1;

        Integer currentNumber = queueTicket.getQueueNumber();
        boolean isAlreadyFastTracked = Boolean.TRUE.equals(queueTicket.getIsFastTracked());

        Integer newQueueNumber;
        if (isAlreadyFastTracked) {
            // If already fast-tracked, only move to front of fast-tracked block if not already there
            if (currentNumber != null && currentNumber.equals(desiredFront)) {
                newQueueNumber = currentNumber; // No reordering needed
            } else {
                newQueueNumber = desiredFront;
                // Shift ONLY other fast-tracked tickets between the target and the old position back by 1
                for (QueueTicket ticket : existingFastTracked) {
                    Integer num = ticket.getQueueNumber();
                    if (num != null && num >= newQueueNumber && (currentNumber == null || num < currentNumber)) {
                        ticket.setQueueNumber(num + 1);
                        queueTicketRepository.save(ticket);
                    }
                }
            }
        } else {
            // Not yet fast-tracked: insert at the front of the fast-tracked block
            // Shift ALL tickets at or after the insertion point (including non-fast-tracked)
            newQueueNumber = desiredFront;
            for (QueueTicket ticket : activeQueue) {
                if (ticket.getTicketId().equals(ticketId)) {
                    continue; // skip current ticket
                }
                Integer num = ticket.getQueueNumber();
                if (num != null && num >= newQueueNumber
                    && ticket.getStatus() != QueueStatus.COMPLETED
                    && ticket.getStatus() != QueueStatus.NO_SHOW) {
                    ticket.setQueueNumber(num + 1);
                    queueTicketRepository.save(ticket);
                }
            }
        }
        
        queueTicket.setIsFastTracked(true);
        queueTicket.setFastTrackReason(reason);
        queueTicket.setStatus(QueueStatus.FAST_TRACKED);
        queueTicket.setQueueNumber(newQueueNumber);
        queueTicket = queueTicketRepository.save(queueTicket);
        
        // Send fast-track notification to the patient
        if (notificationService != null) {
            notificationService.sendFastTrackNotification(queueTicket);
        }
        
        processQueueNotifications(queueTicket.getDoctorId());
        
        return queueTicketMapper.toDto(queueTicket);
    }

    @Override
    public void cancelQueueTicket(Integer ticketId) {
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
        
        List<QueueTicket> currentlyServing = queueTicketRepository.findCurrentQueueNumberByDoctorIdAndDate(doctorId, today);
        for (QueueTicket serving : currentlyServing) {
            if (serving.getStatus() == QueueStatus.CALLED && notificationService != null) {
                notificationService.sendQueueCalledNotification(serving);
            }
        }
        
        Integer notify3AwayNumber = currentServingNumber + 3;
        queueTicketRepository.findTicketToNotify3Away(doctorId, today, notify3AwayNumber)
            .ifPresent(ticket -> {
                if (notificationService != null) {
                    notificationService.sendQueueNotification3Away(ticket); 
                }
            });

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
    public Integer getCurrentServingTicketId(String doctorId) {
        LocalDateTime today = LocalDateTime.now();
        List<QueueTicket> currentlyServing = queueTicketRepository.findCurrentQueueNumberByDoctorIdAndDate(doctorId, today);
        
        if (!currentlyServing.isEmpty()) {
            return currentlyServing.get(0).getTicketId();
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
    
    // Private helper method to get current serving queue number (for internal use)
    private Integer getCurrentServingNumber(String doctorId) {
        LocalDateTime today = LocalDateTime.now();
        List<QueueTicket> currentlyServing = queueTicketRepository.findCurrentQueueNumberByDoctorIdAndDate(doctorId, today);
        
        if (!currentlyServing.isEmpty()) {
            return currentlyServing.get(0).getQueueNumber();
        }
        
        // If no one is currently being served, return 0
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
