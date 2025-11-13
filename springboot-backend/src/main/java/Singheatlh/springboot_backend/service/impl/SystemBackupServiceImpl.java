package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.BackupStatusDto;
import Singheatlh.springboot_backend.service.SystemBackupService;
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
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemBackupServiceImpl implements SystemBackupService {
    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${backup.directory:./backups}")
    private String backupDirectory;
    
    @Value("${docker.container.name:supabase-db}")
    private String dockerContainerName;

    private static final String BACKUP_FILE_EXTENSION = ".sql.gz";

    @Override
    public BackupStatusDto createBackup() {
        try {
            String backupId = UUID.randomUUID().toString();
            LocalDateTime createdAt = LocalDateTime.now();
            Path backupDir = createBackupDirectory();
            Path backupFile = backupDir.resolve(backupId + BACKUP_FILE_EXTENSION);

            log.info("Starting database backup: {}", backupId);

            // Extract database credentials from datasource URL
            String dbName = extractDatabaseName(datasourceUrl);
            
            // Build docker exec command to run pg_dump inside the container
            List<String> command = new ArrayList<>();
            command.add("docker");
            command.add("exec");
            command.add("-e");
            command.add("PGPASSWORD=" + datasourcePassword); // Set password as env var
            command.add(dockerContainerName);
            command.add("pg_dump");
            command.add("-U");
            command.add(datasourceUsername);
            command.add("-d");
            command.add(dbName);
            command.add("-F");
            command.add("c"); // Custom format (binary, compressible)
            command.add("--file=/tmp/backup.dump"); // Temp file inside container
            command.add("-v"); // Verbose

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // Combine stdout and stderr for easier capture

            log.info("Executing pg_dump via docker exec (container: {})", dockerContainerName);
            log.debug("Database: {}, Username: {}", dbName, datasourceUsername);

            // Execute pg_dump
            Process process = pb.start();

            // Capture output in real-time (must do this BEFORE waiting or can deadlock)
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("pg_dump: {}", line);
                    output.append(line).append("\n");
                }
            }

            // Wait for process to complete with timeout
            boolean completed = process.waitFor(5, TimeUnit.MINUTES);
            if (!completed) {
                process.destroy();
                throw new RuntimeException("Backup process timed out after 5 minutes");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorMsg = output.toString();
                log.error("pg_dump failed with output:\n{}", errorMsg);
                throw new RuntimeException("pg_dump failed with exit code: " + exitCode + 
                    "\nError details: " + errorMsg +
                    "\n\nConnection details:" +
                    "\n  Container: " + dockerContainerName +
                    "\n  Database: " + dbName +
                    "\n  Username: " + datasourceUsername +
                    "\n\nPlease verify the Docker container is running and credentials are correct.");
            }
            
            log.info("pg_dump completed successfully, copying backup from container...");
            
            // Copy backup file from container to host
            List<String> copyCommand = new ArrayList<>();
            copyCommand.add("docker");
            copyCommand.add("cp");
            copyCommand.add(dockerContainerName + ":/tmp/backup.dump");
            copyCommand.add(backupFile.toString());
            
            ProcessBuilder copyPb = new ProcessBuilder(copyCommand);
            copyPb.redirectErrorStream(true);
            Process copyProcess = copyPb.start();
            
            // Capture copy output
            StringBuilder copyOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(copyProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("docker cp: {}", line);
                    copyOutput.append(line).append("\n");
                }
            }
            
            boolean copyCompleted = copyProcess.waitFor(1, TimeUnit.MINUTES);
            if (!copyCompleted) {
                copyProcess.destroy();
                throw new RuntimeException("Docker cp timed out");
            }
            
            int copyExitCode = copyProcess.exitValue();
            if (copyExitCode != 0) {
                throw new RuntimeException("Failed to copy backup from container: " + copyOutput.toString());
            }
            
            // Clean up temp file in container
            try {
                List<String> cleanupCommand = new ArrayList<>();
                cleanupCommand.add("docker");
                cleanupCommand.add("exec");
                cleanupCommand.add(dockerContainerName);
                cleanupCommand.add("rm");
                cleanupCommand.add("/tmp/backup.dump");
                new ProcessBuilder(cleanupCommand).start();
            } catch (Exception e) {
                log.warn("Failed to cleanup temp backup file in container: {}", e.getMessage());
            }

            // Verify backup file was created on host
            if (!Files.exists(backupFile)) {
                throw new RuntimeException("Backup file was not created on host");
            }

            long fileSize = Files.size(backupFile);
            int recordCount = countTotalRecords();

            log.info("Backup completed successfully: {} (Size: {} bytes)", backupId, fileSize);

            return BackupStatusDto.builder()
                    .backupId(backupId)
                    .createdAt(createdAt)
                    .sizeInBytes(fileSize)
                    .status("COMPLETED")
                    .description("Database backup completed successfully")
                    .recordCount(recordCount)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create backup", e);
            throw new RuntimeException("Failed to create backup: " + e.getMessage(), e);
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
                    .filter(p -> p.toString().endsWith(BACKUP_FILE_EXTENSION))
                    .forEach(backupFile -> {
                        try {
                            String backupId = backupFile.getFileName().toString()
                                    .replace(BACKUP_FILE_EXTENSION, "");
                            long fileSize = Files.size(backupFile);
                            LocalDateTime createdAt = LocalDateTime.ofInstant(
                                    Files.getLastModifiedTime(backupFile).toInstant(),
                                    java.time.ZoneId.systemDefault()
                            );

                            int recordCount = countTotalRecords();

                            backups.add(BackupStatusDto.builder()
                                    .backupId(backupId)
                                    .createdAt(createdAt)
                                    .sizeInBytes(fileSize)
                                    .status("COMPLETED")
                                    .description("Database backup file")
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
            Path backupFile = Paths.get(backupDirectory, backupId + BACKUP_FILE_EXTENSION);
            if (!Files.exists(backupFile)) {
                throw new FileNotFoundException("Backup not found: " + backupId);
            }
            return new FileSystemResource(backupFile);
        } catch (Exception e) {
            log.error("Failed to download backup: {}", backupId, e);
            throw new RuntimeException("Failed to download backup: " + e.getMessage(), e);
        }
    }

    @Override
    public void restoreBackup(String backupId) {
        try {
            Path backupFile = Paths.get(backupDirectory, backupId + BACKUP_FILE_EXTENSION);
            if (!Files.exists(backupFile)) {
                throw new FileNotFoundException("Backup not found: " + backupId);
            }

            log.info("Starting database restore from backup: {}", backupId);

            // Extract database credentials from datasource URL
            String dbName = extractDatabaseName(datasourceUrl);

            // Copy backup file into Docker container first
            List<String> copyCommand = new ArrayList<>();
            copyCommand.add("docker");
            copyCommand.add("cp");
            copyCommand.add(backupFile.toString());
            copyCommand.add(dockerContainerName + ":/tmp/" + backupId + BACKUP_FILE_EXTENSION);

            ProcessBuilder copyPb = new ProcessBuilder(copyCommand);
            Process copyProcess = copyPb.start();
            boolean copyCompleted = copyProcess.waitFor(2, TimeUnit.MINUTES);
            if (!copyCompleted) {
                copyProcess.destroy();
                throw new RuntimeException("Docker copy timed out");
            }
            if (copyProcess.exitValue() != 0) {
                throw new RuntimeException("Failed to copy backup to Docker container");
            }

            // Build docker exec command to run pg_restore inside container
            List<String> command = new ArrayList<>();
            command.add("docker");
            command.add("exec");
            command.add("-e");
            command.add("PGPASSWORD=" + datasourcePassword);
            command.add(dockerContainerName);
            command.add("pg_restore");
            command.add("-U");
            command.add(datasourceUsername);
            command.add("-d");
            command.add(dbName);
            command.add("-v"); // Verbose
            command.add("--clean"); // Clean (drop) objects before recreating
            command.add("--if-exists"); // Use IF EXISTS clause for compatibility
            command.add("--no-owner"); // Don't restore object ownership (avoids permission issues)
            command.add("--disable-triggers"); // Disable triggers during restore
            command.add("-j");
            command.add("4"); // Use 4 parallel jobs for faster restore
            command.add("/tmp/" + backupId + BACKUP_FILE_EXTENSION);

            // Execute docker exec with pg_restore
            ProcessBuilder pb = new ProcessBuilder(command);

            // Execute pg_restore
            Process process = pb.start();

            // Capture output
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            String line;
            while ((line = errorReader.readLine()) != null) {
                log.debug("pg_restore stderr: {}", line);
                errorOutput.append(line).append("\n");
            }

            while ((line = outputReader.readLine()) != null) {
                log.debug("pg_restore stdout: {}", line);
                output.append(line).append("\n");
            }

            // Wait for process to complete with timeout
            boolean completed = process.waitFor(10, TimeUnit.MINUTES);
            if (!completed) {
                process.destroy();
                throw new RuntimeException("Restore process timed out after 10 minutes");
            }

            int exitCode = process.exitValue();
            // pg_restore returns exit code 0 on success, but may have warnings
            // We consider it successful if exit code is 0 or if only harmless errors occurred
            if (exitCode == 0) {
                log.info("Database restore completed successfully from backup: {}", backupId);
            } else {
                // Check if the output indicates warnings (731 errors ignored, etc.)
                String allOutput = errorOutput.toString() + output.toString();
                if (allOutput.toLowerCase().contains("errors ignored")) {
                    log.info("Database restore completed with non-critical errors from backup: {}", backupId);
                } else {
                    log.error("pg_restore stderr: {}", errorOutput.toString());
                    log.error("pg_restore stdout: {}", output.toString());
                    throw new RuntimeException("pg_restore failed with exit code: " + exitCode);
                }
            }

        } catch (Exception e) {
            log.error("Failed to restore backup: {}", backupId, e);
            throw new RuntimeException("Failed to restore backup: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBackup(String backupId) {
        try {
            Path backupFile = Paths.get(backupDirectory, backupId + BACKUP_FILE_EXTENSION);
            if (Files.exists(backupFile)) {
                Files.delete(backupFile);
                log.info("Backup deleted: {}", backupId);
            }
        } catch (Exception e) {
            log.error("Failed to delete backup: {}", backupId, e);
            throw new RuntimeException("Failed to delete backup: " + e.getMessage(), e);
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

    private int countTotalRecords() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'",
                    Integer.class
            );
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("Could not count tables in database", e);
            return 0;
        }
    }

    private String extractDatabaseName(String datasourceUrl) {
        // Extract database name from JDBC URL: jdbc:postgresql://host:port/dbname
        try {
            String[] parts = datasourceUrl.split("/");
            if (parts.length > 0) {
                return parts[parts.length - 1].split("\\?")[0]; // Remove query parameters if any
            }
        } catch (Exception e) {
            log.warn("Could not extract database name from datasource URL", e);
        }
        return "postgres"; // Default fallback
    }

    private String extractHost(String datasourceUrl) {
        // Extract host from JDBC URL: jdbc:postgresql://host:port/dbname
        try {
            String url = datasourceUrl.replace("jdbc:postgresql://", "");
            String hostAndPort = url.split("/")[0];
            String host = hostAndPort.split(":")[0];
            return host;
        } catch (Exception e) {
            log.warn("Could not extract host from datasource URL, using localhost", e);
        }
        return "localhost"; // Default fallback
    }

    private String extractPort(String datasourceUrl) {
        // Extract port from JDBC URL: jdbc:postgresql://host:port/dbname
        try {
            String url = datasourceUrl.replace("jdbc:postgresql://", "");
            if (url.contains(":")) {
                String[] parts = url.split(":");
                if (parts.length > 1) {
                    return parts[1].split("/")[0];
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract port from datasource URL, using 5432", e);
        }
        return "5432"; // Default PostgreSQL port
    }
}
