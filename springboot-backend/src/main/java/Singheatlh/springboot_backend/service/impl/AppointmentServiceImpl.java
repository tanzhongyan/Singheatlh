package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    
    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, 
                                 AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }
    
    @Override
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        // Validate required fields
        if (request.getPatientId() == null || request.getDoctorId() == null || 
            request.getStartDatetime() == null || request.getEndDatetime() == null) {
            throw new IllegalArgumentException("All required fields must be provided");
        }
        
        // Validate appointment is in the future
        if (request.getStartDatetime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment cannot be scheduled in the past");
        }
        
        // Validate start time is before end time
        if (request.getStartDatetime().isAfter(request.getEndDatetime()) || 
            request.getStartDatetime().isEqual(request.getEndDatetime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        // Check for conflicting appointments with the same doctor
        List<Appointment> conflictingAppointments = appointmentRepository
            .findByDoctorIdAndStartDatetimeBetween(
                request.getDoctorId(),
                request.getStartDatetime().minusMinutes(30),
                request.getEndDatetime()
            );
        
        if (!conflictingAppointments.isEmpty()) {
            throw new IllegalArgumentException("Doctor is not available at the requested time");
        }
        
        // Generate appointment ID
        String appointmentId = generateAppointmentId();
        
        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setAppointmentId(appointmentId);
        appointment.setStatus(AppointmentStatus.Upcoming);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        return appointmentMapper.toDto(savedAppointment);
    }
    
    private String generateAppointmentId() {
        // Get the count of existing appointments and increment
        long count = appointmentRepository.count();
        // Format as A000000001, A000000002, etc.
        return String.format("A%09d", count + 1);
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
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByDoctorId(String doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointmentsByPatientId(UUID patientId) {
        List<Appointment> appointments = appointmentRepository
            .findUpcomingAppointmentsByPatientId(patientId, LocalDateTime.now());
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointmentsByDoctorId(String doctorId) {
        List<Appointment> appointments = appointmentRepository
            .findUpcomingAppointmentsByDoctorId(doctorId, LocalDateTime.now());
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public AppointmentDto updateAppointmentStatus(String appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        
        appointment.setStatus(status);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        return appointmentMapper.toDto(updatedAppointment);
    }
    
    @Override
    public void cancelAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        
        // Check if appointment is at least 24 hours away
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime appointmentTime = appointment.getStartDatetime();
        
        if (appointmentTime.isBefore(now.plusHours(24))) {
            throw new IllegalArgumentException("Cannot cancel appointments less than 24 hours in advance");
        }
        
        updateAppointmentStatus(appointmentId, AppointmentStatus.Cancelled);
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
            );
        
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
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByStatus(AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByStatus(status);
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> getAvailableSlots(String doctorId, LocalDateTime date) {
        // Define clinic operating hours (9 AM to 5 PM)
        LocalDateTime dayStart = date.toLocalDate().atTime(9, 0);
        LocalDateTime dayEnd = date.toLocalDate().atTime(17, 0);
        
        // Get all appointments for this doctor on this date
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorIdAndStartDatetimeBetween(
                        doctorId, 
                        dayStart.minusMinutes(1), 
                        dayEnd.plusMinutes(1)
                );
        
        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalDateTime currentSlot = dayStart;
        
        // Generate 30-minute slots
        while (currentSlot.isBefore(dayEnd)) {
            final LocalDateTime slotTime = currentSlot;
            LocalDateTime slotEnd = currentSlot.plusMinutes(30);
            
            // Check if this slot conflicts with any existing appointment
            boolean isAvailable = existingAppointments.stream()
                    .noneMatch(apt -> 
                        (slotTime.isBefore(apt.getEndDatetime()) && 
                         slotEnd.isAfter(apt.getStartDatetime()))
                    );
            
            if (isAvailable) {
                availableSlots.add(slotTime);
            }
            
            currentSlot = currentSlot.plusMinutes(30);
        }
        
        return availableSlots;
    }
}
