package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
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
        // Basic validation
        if (request.getPatientId() == null || request.getDoctorId() == null || 
            request.getClinicId() == null || request.getAppointmentDatetime() == null) {
            throw new IllegalArgumentException("All required fields must be provided");
        }
        
        if (request.getAppointmentDatetime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment cannot be scheduled in the past");
        }
        
        // Convert DTO to entity
        Appointment appointment = appointmentMapper.toEntity(request);
        
        // Save to database
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Convert back to DTO and return
        return appointmentMapper.toDto(savedAppointment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        return appointmentMapper.toDto(appointment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByPatientId(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByDoctorId(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByClinicId(Long clinicId) {
        List<Appointment> appointments = appointmentRepository.findByClinicId(clinicId);
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointmentsByPatientId(Long patientId) {
        List<Appointment> appointments = appointmentRepository
            .findUpcomingAppointmentsByPatientId(patientId, LocalDateTime.now());
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointmentsByDoctorId(Long doctorId) {
        List<Appointment> appointments = appointmentRepository
            .findUpcomingAppointmentsByDoctorId(doctorId, LocalDateTime.now());
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public AppointmentDto updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        
        appointment.setStatus(status);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        return appointmentMapper.toDto(updatedAppointment);
    }
    
    @Override
    public void cancelAppointment(Long appointmentId) {
        updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED);
    }
    
    @Override
    public AppointmentDto rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime) {
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New appointment time cannot be in the past");
        }
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        
        appointment.setAppointmentDatetime(newDateTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
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
}
