package com.gdtw.general.service.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ScheduledTmpCleanService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTmpCleanService.class);

    @Scheduled(cron = "${task.schedule.cron.dailyTemporaryFilesCleanupService}")
    public void scheduledTemporaryFilesCleanupService() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File[] files = tempDir.listFiles();

        if (!tempDir.exists() || !tempDir.isDirectory() || files == null || files.length == 0) {
            logger.info("Temporary directory is empty or does not contain matching files.");
            return;
        }

        boolean filesDeleted = deleteMatchingTempFiles(files);
        if (filesDeleted) {
            logger.info("Temporary files cleanup completed. Files were deleted.");
        } else {
            logger.info("No matching temporary files to clean.");
        }
    }

    private boolean deleteMatchingTempFiles(File[] files) {
        boolean deletedAny = false;
        for (File file : files) {
            if (shouldDeleteFile(file) && deleteFileSafely(file)) {
                deletedAny = true;
            }
        }
        return deletedAny;
    }


    private boolean shouldDeleteFile(File file) {
        return file.isFile() && file.getName().startsWith("upload_");
    }

    private boolean deleteFileSafely(File file) {
        Path path = file.toPath();
        try {
            boolean deleted = Files.deleteIfExists(path);
            if (!deleted) {
                logger.warn("File not found during deletion: {}.", path);
            }
            return deleted;
        } catch (IOException e) {
            logger.error("Failed to delete file: {}.", path, e);
            return false;
        }
    }

}
