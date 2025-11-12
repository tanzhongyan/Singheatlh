package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.repository.QueueTicketRepository;
import Singheatlh.springboot_backend.service.CheckInValidator;

/**
 * Responsible ONLY for validating check-in operations
 */
@Service
public class CheckInValidatorImpl implements CheckInValidator {
    
    @Autowired
    private QueueTicketRepository queueTicketRepository;
    
    @Override
    public void validateCheckIn(Appointment appointment, LocalDateTime now) {
        validateAppointmentDate(appointment, now);
        validateAppointmentStatus(appointment);
        validateNoDuplicateCheckIn(appointment.getAppointmentId());
    }
    
    @Override
    public void validateAppointmentDate(Appointment appointment, LocalDateTime now) {
        LocalDateTime appointmentDate = appointment.getStartDatetime().toLocalDate().atStartOfDay();
        LocalDateTime currentDate = now.toLocalDate().atStartOfDay();
        
        if (currentDate.isAfter(appointmentDate)) {
            throw new IllegalStateException("Check-in failed: Appointment date has passed. " +
                "Appointment was scheduled for " + appointment.getStartDatetime().toLocalDate() + 
                " but today is " + now.toLocalDate() + ". Please reschedule for patient.");
        }
    }
    
    @Override
    public void validateAppointmentStatus(Appointment appointment) {
        switch (appointment.getStatus()) {
            case Completed:
                throw new IllegalStateException("Check-in failed: This appointment has already been completed on " + 
                    appointment.getStartDatetime().toLocalDate() + ". Cannot check in for a completed appointment.");
            
            case Ongoing:
                throw new IllegalStateException("Check-in failed: This appointment is already ongoing. " +
                    "Patient has already checked in and are currently being served or waiting in queue.");
            
            case Missed:
                throw new IllegalStateException("Check-in failed: This appointment was marked as missed on " + 
                    appointment.getStartDatetime().toLocalDate() + ". Please reschedule for patient.");
            
            case Cancelled:
                throw new IllegalStateException("Check-in failed: This appointment has been cancelled. " +
                    "Please reschedule for the patient.");
            
            case Upcoming:
                // positive flow, continue
                break;
            
            default:
                throw new IllegalStateException("Check-in failed: Invalid appointment status: " + appointment.getStatus());
        }
    }
    
    @Override
    public void validateNoDuplicateCheckIn(String appointmentId) {
        if (queueTicketRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new IllegalStateException("Check-in failed: Patient have already checked in for this appointment. " +
                "Please check patient queue status again or message the system operator if you need assistance.");
        }
    }
}

