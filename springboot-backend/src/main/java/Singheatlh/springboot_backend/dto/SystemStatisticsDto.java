package Singheatlh.springboot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemStatisticsDto {
    private int totalUsers;
    private int totalDoctors;
    private int totalClinics;
    private int totalAppointments;
    private int completedAppointments;
    private int pendingAppointments;
    private int cancelledAppointments;
    private int totalPatients;
    private int totalClinicStaff;
    private int totalAdministrators;
    private LocalDateTime lastBackupTime;
    private double systemUptime;
    private int activeUsers;
}
