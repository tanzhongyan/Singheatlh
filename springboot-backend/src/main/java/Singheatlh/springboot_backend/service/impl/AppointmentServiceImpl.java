package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.mapper.AppointmentMapper;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.service.AppointmentService;
import Singheatlh.springboot_backend.strategy.AppointmentStrategyFactory;
import Singheatlh.springboot_backend.util.StreamMappingHelper;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentStrategyFactory strategyFactory;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                 AppointmentMapper appointmentMapper,
                                 AppointmentStrategyFactory strategyFactory) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.strategyFactory = strategyFactory;
    }
    
    @Override
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        // Use Strategy Pattern to select and execute the appropriate creation strategy
        // All validation rules (including new ones from main) are handled by the validators
        return strategyFactory.getStrategy(request).createAppointment(request);
    }

    /**
     * Convenience method for creating walk-in appointments.
     * Sets the isWalkIn flag and delegates to createAppointment.
     */
    public AppointmentDto createWalkInAppointment(CreateAppointmentRequest request) {
        request.setIsWalkIn(true);
        return createAppointment(request);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentById(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        return appointmentMapper.toDto(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByPatientId(UUID patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return StreamMappingHelper.mapToList(appointments, appointmentMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointmentsByPatientId(UUID patientId) {
        List<Appointment> appointments = appointmentRepository
            .findUpcomingAppointmentsByPatientId(patientId, LocalDateTime.now());
        return StreamMappingHelper.mapToList(appointments, appointmentMapper::toDto);
    }
    
    @Override
    public void cancelAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        
        // Check if appointment is at least 24 hours away
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime appointmentTime = appointment.getStartDatetime();
        // If appointment already in the past, disallow cancellation with a clear message
        if (appointmentTime.isBefore(now)) {
            throw new IllegalStateException("Cannot cancel past appointments");
        }

        if (appointmentTime.isBefore(now.plusHours(24))) {
            throw new IllegalArgumentException("Cannot cancel appointments less than 24 hours in advance");
        }
        
        // Update appointment status to Cancelled
        appointment.setStatus(AppointmentStatus.Cancelled);
        appointmentRepository.save(appointment);
    }
    
    @Override
    public AppointmentDto rescheduleAppointment(String appointmentId, LocalDateTime newDateTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        
        // Check if appointment is at least 24 hours away
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime appointmentTime = appointment.getStartDatetime();
        
        if (appointmentTime.isBefore(now.plusHours(24))) {
            throw new IllegalArgumentException("Cannot reschedule appointments less than 24 hours in advance");
        }
        
        // Validate new time is in the future
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New appointment time cannot be in the past");
        }
        
        // Validate appointment is not for today (must be at least next day)
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);
        if (newDateTime.isBefore(tomorrow)) {
            throw new IllegalArgumentException("Appointments must be rescheduled to at least one day in advance. Please select a date from tomorrow onwards.");
        }
        
        // Check if patient already has an appointment on the new date (excluding current appointment)
        LocalDateTime startOfDay = newDateTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        List<Appointment> patientAppointmentsOnDay = appointmentRepository
            .findByPatientIdAndStartDatetimeBetween(appointment.getPatientId(), startOfDay, endOfDay)
            .stream()
            .filter(apt -> !apt.getAppointmentId().equals(appointmentId)) // Exclude current appointment
            .filter(apt -> apt.getStatus() == AppointmentStatus.Upcoming || apt.getStatus() == AppointmentStatus.Ongoing)
            .collect(Collectors.toList());
        
        if (!patientAppointmentsOnDay.isEmpty()) {
            throw new IllegalArgumentException("You already have an appointment scheduled on this day. Please choose a different date.");
        }
        
        // Calculate new end time (assume same duration)
        long durationMinutes = java.time.Duration.between(
            appointment.getStartDatetime(), 
            appointment.getEndDatetime()
        ).toMinutes();
        LocalDateTime newEndTime = newDateTime.plusMinutes(durationMinutes);
        
        // Check for conflicts with the new time
        List<Appointment> conflicts = appointmentRepository
            .findByDoctorIdAndStartDatetimeBetween(
                appointment.getDoctorId(),
                newDateTime.minusMinutes(30),
                newEndTime
            )
            .stream()
            .filter(apt -> apt.getStatus() == AppointmentStatus.Upcoming || apt.getStatus() == AppointmentStatus.Ongoing)
            .collect(Collectors.toList());
        
        // Remove current appointment from conflicts
        conflicts.removeIf(a -> a.getAppointmentId().equals(appointmentId));
        
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Doctor is not available at the requested time");
        }
        
        appointment.setStartDatetime(newDateTime);
        appointment.setEndDatetime(newEndTime);
        appointment.setStatus(AppointmentStatus.Upcoming);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        return appointmentMapper.toDto(updatedAppointment);
    }

    // ========== Clinic Staff Methods ==========

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByClinicId(Integer clinicId) {
        List<Appointment> appointments = appointmentRepository.findByClinicId(clinicId);
        return StreamMappingHelper.mapToList(appointments, appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByClinicIdAndStatus(Integer clinicId, AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByClinicIdAndStatus(clinicId, status);
        return StreamMappingHelper.mapToList(appointments, appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getTodayAppointmentsByClinicId(Integer clinicId) {
        List<Appointment> appointments = appointmentRepository.findTodayAppointmentsByClinicId(
            clinicId, LocalDateTime.now()
        );
        return StreamMappingHelper.mapToList(appointments, appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByClinicIdAndDateRange(
            Integer clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Appointment> appointments = appointmentRepository.findByClinicIdAndDateRange(
            clinicId, startDate, endDate
        );
        return StreamMappingHelper.mapToList(appointments, appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointmentsByClinicId(Integer clinicId) {
        List<Appointment> appointments = appointmentRepository.findUpcomingAppointmentsByClinicId(
            clinicId, LocalDateTime.now()
        );
        return StreamMappingHelper.mapToList(appointments, appointmentMapper::toDto);
    }
}
