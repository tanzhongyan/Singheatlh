package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.BackupStatusDto;
import org.springframework.core.io.Resource;

import java.util.List;

public interface SystemBackupService {
    BackupStatusDto createBackup();
    List<BackupStatusDto> getBackupHistory();
    Resource downloadBackup(String backupId);
    void restoreBackup(String backupId);
    void deleteBackup(String backupId);
}
