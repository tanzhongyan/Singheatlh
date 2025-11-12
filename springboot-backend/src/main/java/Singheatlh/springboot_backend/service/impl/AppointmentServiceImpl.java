package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.dto.request.RescheduleAppointmentRequest;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.mapper.AppointmentMapper;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.service.AppointmentService;
import Singheatlh.springboot_backend.strategy.AppointmentStrategyFactory;
import Singheatlh.springboot_backend.strategy.cancellation.CancellationContext;
import Singheatlh.springboot_backend.strategy.cancellation.CancellationStrategyFactory;
import Singheatlh.springboot_backend.strategy.reschedule.RescheduleContext;
import Singheatlh.springboot_backend.strategy.reschedule.RescheduleStrategyFactory;
import Singheatlh.springboot_backend.util.StreamMappingHelper;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentStrategyFactory strategyFactory;
    private final CancellationStrategyFactory cancellationFactory;
    private final RescheduleStrategyFactory rescheduleFactory;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                 AppointmentMapper appointmentMapper,
                                 AppointmentStrategyFactory strategyFactory,
                                 CancellationStrategyFactory cancellationFactory,
                                 RescheduleStrategyFactory rescheduleFactory) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.strategyFactory = strategyFactory;
        this.cancellationFactory = cancellationFactory;
        this.rescheduleFactory = rescheduleFactory;
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

        // Build cancellation context for patient
        CancellationContext context = CancellationContext.builder()
            .isStaff(false)
            .cancelledBy(appointment.getPatientId())
            .now(LocalDateTime.now())
            .build();

        // Delegate to strategy pattern
        cancellationFactory.getStrategy(context).cancel(appointment, context);
    }

    @Override
    public void cancelAppointmentByStaff(String appointmentId, UUID staffId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));

        // Build cancellation context for staff
        CancellationContext context = CancellationContext.builder()
            .isStaff(true)
            .cancelledBy(staffId)
            .reason(reason)
            .now(LocalDateTime.now())
            .build();

        // Delegate to strategy pattern
        cancellationFactory.getStrategy(context).cancel(appointment, context);
    }

    @Override
    public AppointmentDto rescheduleAppointment(String appointmentId, LocalDateTime newDateTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));

        // Build reschedule context for patient
        RescheduleContext context = RescheduleContext.builder()
            .isStaff(false)
            .newDateTime(newDateTime)
            .now(LocalDateTime.now())
            .build();

        // Delegate to strategy pattern
        return rescheduleFactory.getStrategy(context).reschedule(appointment, context);
    }

    @Override
    public AppointmentDto rescheduleAppointmentByStaff(String appointmentId, LocalDateTime newDateTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));

        // Build reschedule context for staff
        RescheduleContext context = RescheduleContext.builder()
            .isStaff(true)
            .newDateTime(newDateTime)
            .now(LocalDateTime.now())
            .build();

        // Delegate to strategy pattern
        return rescheduleFactory.getStrategy(context).reschedule(appointment, context);
    }

    @Override
    public AppointmentDto rescheduleAppointment(String appointmentId, RescheduleAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));

        // Build reschedule context for patient with optional doctor/clinic changes
        RescheduleContext context = RescheduleContext.builder()
            .isStaff(false)
            .newDateTime(request.getNewDateTime())
            .newDoctorId(request.getNewDoctorId())
            .newClinicId(request.getNewClinicId())
            .now(LocalDateTime.now())
            .build();

        // Delegate to strategy pattern
        return rescheduleFactory.getStrategy(context).reschedule(appointment, context);
    }

    @Override
    public AppointmentDto rescheduleAppointmentByStaff(String appointmentId, RescheduleAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));

        // Build reschedule context for staff with optional doctor/clinic changes
        RescheduleContext context = RescheduleContext.builder()
            .isStaff(true)
            .newDateTime(request.getNewDateTime())
            .newDoctorId(request.getNewDoctorId())
            .newClinicId(request.getNewClinicId())
            .now(LocalDateTime.now())
            .build();

        // Delegate to strategy pattern
        return rescheduleFactory.getStrategy(context).reschedule(appointment, context);
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
