package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.Patient;
import Singheatlh.springboot_backend.entity.Doctor;
import Singheatlh.springboot_backend.entity.QueueTicket;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.repository.PatientRepository;
import Singheatlh.springboot_backend.repository.DoctorRepository;
import Singheatlh.springboot_backend.service.QueuePatientInfoService;

/**
 * Responsible ONLY for extracting patient/doctor information
 */
@Service
public class QueuePatientInfoServiceImpl implements QueuePatientInfoService {
    private static final Logger log = LoggerFactory.getLogger(QueuePatientInfoServiceImpl.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    public String getPatientName(QueueTicket queueTicket) {
        try {
            if (queueTicket == null) {
                return "Patient";
            }

            java.util.UUID patientId = queueTicket.getPatientId();

            if (patientId == null && queueTicket.getAppointment() != null) {
                patientId = queueTicket.getAppointment().getPatientId();
            }

            // If appointment relation wasn't initialized, look up Appointment directly by
            // ID
            if (patientId == null && queueTicket.getAppointmentId() != null) {
                try {
                    Appointment appt = appointmentRepository.findById(queueTicket.getAppointmentId()).orElse(null);
                    if (appt != null) {
                        patientId = appt.getPatientId();
                    }
                } catch (Exception repoEx) {
                    log.warn("Failed to fetch appointment for appointmentId={} to resolve patientId: {}",
                            queueTicket.getAppointmentId(), repoEx.getMessage());
                }
            }

            if (patientId == null) {
                log.warn("Patient ID missing for ticketId={} appointmentId={}",
                        queueTicket.getTicketId(), queueTicket.getAppointmentId());
                return "Patient";
            }

            Patient patient = patientRepository.findById(patientId).orElse(null);
            if (patient == null) {
                log.warn("Patient entity not found for patientId={}", patientId);
                return "Patient";
            }

            String name = patient.getName();
            if (name != null && !name.isBlank()) {
                return name;
            }

            log.warn("Patient name blank for patientId={}", patientId);
            return "Patient";
        } catch (Exception e) {
            log.error("Error resolving patient name for ticketId={} appointmentId={} : {}",
                    queueTicket != null ? queueTicket.getTicketId() : null,
                    queueTicket != null ? queueTicket.getAppointmentId() : null,
                    e.getMessage(), e);
            return "Patient";
        }
    }

    @Override
    public String getDoctorName(QueueTicket queueTicket) {
        try {
            if (queueTicket == null) {
                return "Unknown Doctor";
            }

            // Retrieve doctorId from helper or appointment
            String doctorId = queueTicket.getDoctorId();
            if (doctorId == null && queueTicket.getAppointment() != null) {
                doctorId = queueTicket.getAppointment().getDoctorId();
            }
            // If appointment relation wasn't initialized, look up Appointment directly by
            // ID
            if ((doctorId == null || doctorId.isBlank()) && queueTicket.getAppointmentId() != null) {
                try {
                    Appointment appt = appointmentRepository.findById(queueTicket.getAppointmentId()).orElse(null);
                    if (appt != null) {
                        doctorId = appt.getDoctorId();
                    }
                } catch (Exception repoEx) {
                    log.warn("Failed to fetch appointment for appointmentId={} to resolve doctorId: {}",
                            queueTicket.getAppointmentId(), repoEx.getMessage());
                }
            }
            if (doctorId == null || doctorId.isBlank()) {
                log.warn("Doctor ID missing for ticketId={} appointmentId={}",
                        queueTicket.getTicketId(), queueTicket.getAppointmentId());
                return "Unknown Doctor";
            }

            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            if (doctor == null) {
                log.warn("Doctor entity not found for doctorId={}", doctorId);
                return "Unknown Doctor";
            }

            String name = doctor.getName();
            if (name != null && !name.isBlank()) {
                if (log.isDebugEnabled()) {
                    log.debug("Resolved doctor name '{}' for doctorId={}", name, doctorId);
                }
                return name;
            }

            log.warn("Doctor name blank for doctorId={}", doctorId);
            return "Unknown Doctor";
        } catch (Exception e) {
            log.error("Error resolving doctor name for ticketId={} appointmentId={} : {}",
                    queueTicket != null ? queueTicket.getTicketId() : null,
                    queueTicket != null ? queueTicket.getAppointmentId() : null,
                    e.getMessage(), e);
            return "Unknown Doctor";
        }
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
