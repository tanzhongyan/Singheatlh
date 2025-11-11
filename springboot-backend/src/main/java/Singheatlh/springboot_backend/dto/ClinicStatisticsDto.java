package Singheatlh.springboot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClinicStatisticsDto {

    private Integer clinicId;
    private String clinicName;
    private LocalDate reportDate;
    
    // Daily metrics
    private int totalPatientsSeenToday;
    private int totalPatientsPendingToday;
    private int totalCheckInsToday;
    private int totalCompletedToday;
    private int totalNoShowsToday;
    
    // (in minutes)
    private Double averageWaitingTime;
    private Double minWaitingTime;
    private Double maxWaitingTime;
    
    // (in minutes)
    private Double averageConsultationTime;
    
    private List<DoctorDailyStats> doctorStats;
    
    // Queue status breakdown
    private QueueStatusBreakdown queueBreakdown;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DoctorDailyStats {
        private String doctorId;
        private String doctorName;
        private int patientsSeenToday;
        private Double averageWaitingTime;
        private Double averageConsultationTime;
        private int currentQueueSize;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class QueueStatusBreakdown {
        private int checkedIn;
        private int called;
        private int completed;
        private int noShow;
        private int fastTracked;
    }
}

