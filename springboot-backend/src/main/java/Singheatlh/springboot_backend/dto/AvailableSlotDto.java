package Singheatlh.springboot_backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean available;
}
