package Singheatlh.springboot_backend.dto;

import Singheatlh.springboot_backend.entity.enums.ScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDto {

    private String scheduleId;

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotNull(message = "Start datetime is required")
    private LocalDateTime startDatetime;

    @NotNull(message = "End datetime is required")
    private LocalDateTime endDatetime;

    @NotNull(message = "Schedule type is required")
    private ScheduleType type;

    // For response convenience - doctor name
    private String doctorName;
}
