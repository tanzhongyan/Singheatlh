package Singheatlh.springboot_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class SlotDto {
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
}
