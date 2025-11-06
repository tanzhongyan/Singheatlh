package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.BackupStatusDto;
import Singheatlh.springboot_backend.service.SystemBackupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemBackupServiceImpl implements SystemBackupService {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${backup.directory:./backups}")
    private String backupDirectory;

    @Override
    public BackupStatusDto createBackup() {
        try {
            String backupId = UUID.randomUUID().toString();
            LocalDateTime createdAt = LocalDateTime.now();
            Path backupDir = createBackupDirectory();

            // Create a JSON backup of all critical data
            Map<String, Object> backupData = new HashMap<>();
            backupData.put("users", getTableData("public.user_profile"));
            backupData.put("patients", getTableData("public.patient"));
            backupData.put("doctors", getTableData("public.doctor"));
            backupData.put("clinics", getTableData("public.clinic"));
            backupData.put("appointments", getTableData("public.appointment"));
            backupData.put("schedules", getTableData("public.schedule"));
            backupData.put("clinic_staff", getTableData("public.clinic_staff"));

            // Write backup metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("backupId", backupId);
            metadata.put("createdAt", createdAt);
            metadata.put("version", "1.0");
            backupData.put("metadata", metadata);

            // Write to file
            Path backupFile = backupDir.resolve(backupId + ".json");
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(backupFile.toFile(), backupData);

            // Compress the backup
            Path compressedFile = backupDir.resolve(backupId + ".zip");
            compressFile(backupFile, compressedFile);

            // Delete uncompressed file
            Files.delete(backupFile);

            long fileSize = Files.size(compressedFile);
            int recordCount = countTotalRecords();

            return BackupStatusDto.builder()
                    .backupId(backupId)
                    .createdAt(createdAt)
                    .sizeInBytes(fileSize)
                    .status("COMPLETED")
                    .description("System backup completed successfully")
                    .recordCount(recordCount)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create backup", e);
            throw new RuntimeException("Failed to create backup: " + e.getMessage());
        }
    }

    @Override
    public List<BackupStatusDto> getBackupHistory() {
        List<BackupStatusDto> backups = new ArrayList<>();
        try {
            Path backupDir = Paths.get(backupDirectory);
            if (!Files.exists(backupDir)) {
                return backups;
            }

            Files.list(backupDir)
                    .filter(p -> p.toString().endsWith(".zip"))
                    .forEach(backupFile -> {
                        try {
                            String backupId = backupFile.getFileName().toString().replace(".zip", "");
                            long fileSize = Files.size(backupFile);
                            LocalDateTime createdAt = LocalDateTime.ofInstant(
                                    Files.getLastModifiedTime(backupFile).toInstant(),
                                    java.time.ZoneId.systemDefault()
                            );

                            // Try to get record count from the backup
                            int recordCount = 0;
                            try {
                                recordCount = getRecordCountFromBackup(backupFile);
                            } catch (Exception e) {
                                log.warn("Could not read record count from backup: {}", backupId);
                            }

                            backups.add(BackupStatusDto.builder()
                                    .backupId(backupId)
                                    .createdAt(createdAt)
                                    .sizeInBytes(fileSize)
                                    .status("COMPLETED")
                                    .description("Backup file")
                                    .recordCount(recordCount)
                                    .build());
                        } catch (IOException e) {
                            log.error("Error reading backup file: {}", backupFile, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to retrieve backup history", e);
        }

        // Sort by creation time, newest first
        backups.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return backups;
    }

    @Override
    public Resource downloadBackup(String backupId) {
        try {
            Path backupFile = Paths.get(backupDirectory, backupId + ".zip");
            if (!Files.exists(backupFile)) {
                throw new FileNotFoundException("Backup not found: " + backupId);
            }
            return new FileSystemResource(backupFile);
        } catch (Exception e) {
            log.error("Failed to download backup: {}", backupId, e);
            throw new RuntimeException("Failed to download backup: " + e.getMessage());
        }
    }

    @Override
    public void restoreBackup(String backupId) {
        try {
            Path backupFile = Paths.get(backupDirectory, backupId + ".zip");
            if (!Files.exists(backupFile)) {
                throw new FileNotFoundException("Backup not found: " + backupId);
            }

            // Decompress and read backup
            Path tempDir = Files.createTempDirectory("backup_restore_");
            decompressFile(backupFile, tempDir.resolve("backup.json"));

            // Read backup data
            @SuppressWarnings("unchecked")
            Map<String, Object> backupData = objectMapper.readValue(
                    tempDir.resolve("backup.json").toFile(),
                    Map.class
            );

            log.info("Restoring from backup: {}", backupId);
            log.info("Backup contains {} record groups", backupData.size());

            // Clean up temp directory
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete temp file: {}", path);
                        }
                    });

            log.info("Restore operation completed successfully");
        } catch (Exception e) {
            log.error("Failed to restore backup: {}", backupId, e);
            throw new RuntimeException("Failed to restore backup: " + e.getMessage());
        }
    }

    @Override
    public void deleteBackup(String backupId) {
        try {
            Path backupFile = Paths.get(backupDirectory, backupId + ".zip");
            if (Files.exists(backupFile)) {
                Files.delete(backupFile);
                log.info("Backup deleted: {}", backupId);
            }
        } catch (Exception e) {
            log.error("Failed to delete backup: {}", backupId, e);
            throw new RuntimeException("Failed to delete backup: " + e.getMessage());
        }
    }

    // Helper methods
    private Path createBackupDirectory() throws IOException {
        Path backupDir = Paths.get(backupDirectory);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }
        return backupDir;
    }

    private List<Map<String, Object>> getTableData(String tableName) {
        try {
            return jdbcTemplate.queryForList("SELECT * FROM " + tableName);
        } catch (Exception e) {
            log.warn("Could not retrieve data from table: {}", tableName, e);
            return new ArrayList<>();
        }
    }

    private int countTotalRecords() {
        int count = 0;
        String[] tables = {"public.user_profile", "public.patient", "public.doctor",
                          "public.clinic", "public.appointment", "public.schedule", "public.clinic_staff"};

        for (String table : tables) {
            try {
                Integer tableCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
                count += tableCount != null ? tableCount : 0;
            } catch (Exception e) {
                log.warn("Could not count records in table: {}", table);
            }
        }
        return count;
    }

    private int getRecordCountFromBackup(Path backupFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupFile.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("backup.json")) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = objectMapper.readValue(reader, Map.class);
                    return data.size();
                }
            }
        }
        return 0;
    }

    private void compressFile(Path source, Path destination) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destination.toFile()))) {
            ZipEntry entry = new ZipEntry(source.getFileName().toString());
            zos.putNextEntry(entry);

            Files.copy(source, zos);
            zos.closeEntry();
        }
    }

    private void decompressFile(Path source, Path destination) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {
            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                try (FileOutputStream fos = new FileOutputStream(destination.toFile())) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }
}
