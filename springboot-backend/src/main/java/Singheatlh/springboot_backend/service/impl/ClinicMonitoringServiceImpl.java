package Singheatlh.springboot_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import Singheatlh.springboot_backend.dto.ClinicStatisticsDto;
import Singheatlh.springboot_backend.entity.Clinic;
import Singheatlh.springboot_backend.entity.Doctor;
import Singheatlh.springboot_backend.entity.QueueTicket;
import Singheatlh.springboot_backend.entity.enums.QueueStatus;
import Singheatlh.springboot_backend.repository.ClinicRepository;
import Singheatlh.springboot_backend.repository.DoctorRepository;
import Singheatlh.springboot_backend.repository.QueueTicketRepository;
import Singheatlh.springboot_backend.service.ClinicMonitoringService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicMonitoringServiceImpl implements ClinicMonitoringService {
    
    private final QueueTicketRepository queueTicketRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    
    @Override
    public ClinicStatisticsDto getClinicStatistics(Integer clinicId, LocalDate date) {
        final LocalDate reportDate = (date == null) ? LocalDate.now() : date;
        
        Clinic clinic = clinicRepository.findById(clinicId).orElse(null);
        String clinicName = clinic != null ? clinic.getName() : "Unknown Clinic";
        
        LocalDateTime startOfDay = reportDate.atStartOfDay();
        
        //get all tickets for today
        final List<QueueTicket> todayTickets = queueTicketRepository.findAllQueueTicketsByClinicIdAndDate(clinicId, startOfDay);
        
        // Calculate metrics
        int totalCheckIns = todayTickets.size();
        int totalCompleted = (int) todayTickets.stream()
            .filter(t -> t.getStatus() == QueueStatus.COMPLETED)
            .count();
        int totalNoShows = (int) todayTickets.stream()
            .filter(t -> t.getStatus() == QueueStatus.NO_SHOW)
            .count();
        int totalPending = (int) todayTickets.stream()
            .filter(t -> t.getStatus() != QueueStatus.COMPLETED && t.getStatus() != QueueStatus.NO_SHOW)
            .count();
        
        // Calculate waiting times (only for tickets with consultation start time)
        // Excludes NO_SHOW tickets (they won't have consultation_start_time)
        List<Double> waitingTimes = todayTickets.stream()
            .filter(t -> t.getConsultationStartTime() != null && t.getCheckInTime() != null)
            .map(t -> {
                Duration duration = Duration.between(t.getCheckInTime(), t.getConsultationStartTime());
                return (double) duration.toMinutes(); // Duration in minutes
            })
            .collect(Collectors.toList());
        
        Double avgWaitingTime = waitingTimes.isEmpty() ? 0.0 : 
            waitingTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        Double minWaitingTime = waitingTimes.isEmpty() ? 0.0 :
            waitingTimes.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        Double maxWaitingTime = waitingTimes.isEmpty() ? 0.0 :
            waitingTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        
        // Calculate consultation times (only for completed tickets with both timestamps)
        // Excludes NO_SHOW tickets (they won't have consultation_complete_time)
        List<Double> consultationTimes = todayTickets.stream()
            .filter(t -> t.getConsultationStartTime() != null && t.getConsultationCompleteTime() != null)
            .map(t -> {
                Duration duration = Duration.between(t.getConsultationStartTime(), t.getConsultationCompleteTime());
                return (double) duration.toMinutes(); // Duration in minutes
            })
            .collect(Collectors.toList());
        
        Double avgConsultationTime = consultationTimes.isEmpty() ? 0.0 :
            consultationTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // Get per-doctor statistics
        List<Doctor> clinicDoctors = doctorRepository.findByClinicId(clinicId);
        List<ClinicStatisticsDto.DoctorDailyStats> doctorStats = clinicDoctors.stream()
            .map(doctor -> {
                List<QueueTicket> doctorTickets = todayTickets.stream()
                    .filter(t -> doctor.getDoctorId().equals(t.getDoctorId()))
                    .collect(Collectors.toList());
                
                int patientsSeenToday = (int) doctorTickets.stream()
                    .filter(t -> t.getStatus() == QueueStatus.COMPLETED)
                    .count();
                
                List<Double> doctorWaitingTimes = doctorTickets.stream()
                    .filter(t -> t.getConsultationStartTime() != null && t.getCheckInTime() != null)
                    .map(t -> {
                        Duration duration = Duration.between(t.getCheckInTime(), t.getConsultationStartTime());
                        return (double) duration.toMinutes(); // Duration in minutes
                    })
                    .collect(Collectors.toList());
                
                Double doctorAvgWaitTime = doctorWaitingTimes.isEmpty() ? 0.0 :
                    doctorWaitingTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                
                // Calculate average consultation time per doctor
                List<Double> doctorConsultationTimes = doctorTickets.stream()
                    .filter(t -> t.getConsultationStartTime() != null && t.getConsultationCompleteTime() != null)
                    .map(t -> {
                        Duration duration = Duration.between(t.getConsultationStartTime(), t.getConsultationCompleteTime());
                        return (double) duration.toMinutes(); // Duration in minutes
                    })
                    .collect(Collectors.toList());
                
                Double doctorAvgConsultationTime = doctorConsultationTimes.isEmpty() ? 0.0 :
                    doctorConsultationTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                
                int currentQueueSize = (int) doctorTickets.stream()
                    .filter(t -> t.getStatus() != QueueStatus.COMPLETED && t.getStatus() != QueueStatus.NO_SHOW)
                    .count();
                
                return ClinicStatisticsDto.DoctorDailyStats.builder()
                    .doctorId(doctor.getDoctorId())
                    .doctorName(doctor.getName())
                    .patientsSeenToday(patientsSeenToday)
                    .averageWaitingTime(doctorAvgWaitTime)
                    .averageConsultationTime(doctorAvgConsultationTime)
                    .currentQueueSize(currentQueueSize)
                    .build();
            })
            .collect(Collectors.toList());
        
        // Queue status breakdown
        ClinicStatisticsDto.QueueStatusBreakdown queueBreakdown = ClinicStatisticsDto.QueueStatusBreakdown.builder()
            .checkedIn((int) todayTickets.stream().filter(t -> t.getStatus() == QueueStatus.CHECKED_IN).count())
            .called((int) todayTickets.stream().filter(t -> t.getStatus() == QueueStatus.CALLED).count())
            .completed((int) todayTickets.stream().filter(t -> t.getStatus() == QueueStatus.COMPLETED).count())
            .noShow((int) todayTickets.stream().filter(t -> t.getStatus() == QueueStatus.NO_SHOW).count())
            .fastTracked((int) todayTickets.stream().filter(t -> t.getStatus() == QueueStatus.FAST_TRACKED).count())
            .build();
        
        return ClinicStatisticsDto.builder()
            .clinicId(clinicId)
            .clinicName(clinicName)
            .reportDate(reportDate)
            .totalPatientsSeenToday(totalCompleted)
            .totalPatientsPendingToday(totalPending)
            .totalCheckInsToday(totalCheckIns)
            .totalCompletedToday(totalCompleted)
            .totalNoShowsToday(totalNoShows)
            .averageWaitingTime(avgWaitingTime)
            .minWaitingTime(minWaitingTime)
            .maxWaitingTime(maxWaitingTime)
            .averageConsultationTime(avgConsultationTime)
            .doctorStats(doctorStats)
            .queueBreakdown(queueBreakdown)
            .build();
    }
}

