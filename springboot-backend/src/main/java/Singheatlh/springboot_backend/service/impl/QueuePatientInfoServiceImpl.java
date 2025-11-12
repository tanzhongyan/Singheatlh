package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.Patient;
import Singheatlh.springboot_backend.entity.QueueTicket;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.repository.PatientRepository;
import Singheatlh.springboot_backend.service.QueuePatientInfoService;

/**
 * Responsible ONLY for extracting patient/doctor information
 */
@Service
public class QueuePatientInfoServiceImpl implements QueuePatientInfoService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Override
    public String getPatientName(QueueTicket queueTicket) {
        try {
            java.util.UUID patientId = queueTicket.getPatientId();
            
            if (patientId == null && queueTicket.getAppointment() != null) {
                patientId = queueTicket.getAppointment().getPatientId();
            }
            
            if (patientId != null) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && patient.getName() != null && !patient.getName().trim().isEmpty()) {
                    return patient.getName();
                }
            }
        } catch (Exception e) {
        }
        return "Patient";
    }
    
    @Override
    public String getDoctorName(QueueTicket queueTicket) {
        if (queueTicket.getAppointment() != null && queueTicket.getAppointment().getDoctor() != null) {
            String name = queueTicket.getAppointment().getDoctor().getName();
            return name != null && !name.trim().isEmpty() ? name : "Unknown Doctor";
        }
        return "Unknown Doctor";
    }
    
    @Override
    public String getAppointmentDetails(QueueTicket queueTicket) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        StringBuilder details = new StringBuilder();
        
        try {
            // Check-in time
            LocalDateTime checkInTime = queueTicket.getCheckInTime();
            if (checkInTime != null) {
                details.append("Check-in Time: ").append(checkInTime.format(formatter)).append("\n");
            }
            
            // Appointment date & time
            if (queueTicket.getAppointment() != null) {
                LocalDateTime appointmentTime = queueTicket.getAppointment().getStartDatetime();
                if (appointmentTime != null) {
                    details.append("Appointment Time: ").append(appointmentTime.format(formatter));
                }
            }
        } catch (Exception e) {
            // Return minimal details if formatting fails
            return "Appointment Details: Available in your records";
        }
        
        return details.length() > 0 ? details.toString() : "Appointment Details: Available in your records";
    }
    
    @Override
    public String getPatientEmail(QueueTicket queueTicket) {
        try {
            // Get patient Id, try helper method first, then fetch from appointment directly
            java.util.UUID patientId = queueTicket.getPatientId();
            
            // If helper method returns null, fetch appointment directly
            if (patientId == null) {
                Appointment appointment = appointmentRepository.findById(queueTicket.getAppointmentId()).orElse(null);
                if (appointment != null) {
                    patientId = appointment.getPatientId();
                }
            }
            
            // Get patient email from db
            if (patientId != null) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && patient.getEmail() != null && !patient.getEmail().isEmpty()) {
                    return patient.getEmail();
                }
            }
        } catch (Exception e) {
        }
        
        return null;
    }
}

