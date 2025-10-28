package Singheatlh.springboot_backend.entity;

import Singheatlh.springboot_backend.entity.enums.ScheduleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_doctor_schedule",
        columnNames = {"doctor_id", "start_datetime", "end_datetime"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @Column(name = "schedule_id", length = 10)
    private String scheduleId;

    @Column(name = "doctor_id", nullable = false, length = 10)
    private String doctorId;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 11, nullable = false)
    private ScheduleType type;

    // Relationship to Doctor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Doctor doctor;
}
