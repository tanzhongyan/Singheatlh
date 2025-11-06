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
public class BackupStatusDto {
    private String backupId;
    private LocalDateTime createdAt;
    private long sizeInBytes;
    private String status; // COMPLETED, FAILED, IN_PROGRESS
    private String description;
    private int recordCount;
}
