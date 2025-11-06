package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.SystemStatisticsDto;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.repository.*;
import Singheatlh.springboot_backend.service.SystemMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SystemMonitoringServiceImpl implements SystemMonitoringService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicRepository clinicRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ClinicStaffRepository clinicStaffRepository;
    private final SystemAdministratorRepository systemAdministratorRepository;

    @Override
    public SystemStatisticsDto getSystemStatistics() {
        // Count total users and their breakdown by role
        int totalPatients = (int) patientRepository.count();
        int totalClinicStaff = (int) clinicStaffRepository.count();
        int totalAdministrators = (int) systemAdministratorRepository.count();
        int totalUsers = totalPatients + totalClinicStaff + totalAdministrators;

        // Count doctors and clinics
        int totalDoctors = (int) doctorRepository.count();
        int totalClinics = (int) clinicRepository.count();

        // Count appointments by status
        int totalAppointments = (int) appointmentRepository.count();
        long completedCount = appointmentRepository.findByStatus(AppointmentStatus.Completed).size();
        long pendingCount = appointmentRepository.findByStatus(AppointmentStatus.Upcoming).size();
        long cancelledCount = appointmentRepository.findByStatus(AppointmentStatus.Cancelled).size();

        return SystemStatisticsDto.builder()
                .totalUsers(totalUsers)
                .totalDoctors(totalDoctors)
                .totalClinics(totalClinics)
                .totalAppointments(totalAppointments)
                .completedAppointments((int) completedCount)
                .pendingAppointments((int) pendingCount)
                .cancelledAppointments((int) cancelledCount)
                .totalPatients(totalPatients)
                .totalClinicStaff(totalClinicStaff)
                .totalAdministrators(totalAdministrators)
                .lastBackupTime(LocalDateTime.now())
                .systemUptime(calculateSystemUptime())
                .activeUsers(calculateActiveUsers())
                .build();
    }

    private double calculateSystemUptime() {
        // Placeholder: In a real system, track actual uptime
        // For now returning 99.9% uptime
        return 99.9;
    }

    private int calculateActiveUsers() {
        // Placeholder: In a real system, track login sessions
        // For now returning approximate active users (users created in last 24 hours)
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        return (int) userRepository.count(); // Simplified for now
    }
}
