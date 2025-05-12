package com.gdtw.general.service.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ScheduledCleanTemporaryFilesService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledCleanTemporaryFilesService.class);

    @Scheduled(cron = "${task.schedule.cron.dailyTemporaryFilesCleanupService}")
    public void scheduledTemporaryFilesCleanupService() {

        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        if (tempDir.exists() && tempDir.isDirectory() && tempDir.listFiles() != null && tempDir.listFiles().length > 0) {
            boolean filesDeleted = false;
            for (File file : tempDir.listFiles()) {
                if (file.isFile() && file.getName().startsWith("upload_")) {
                    file.delete();
                    filesDeleted = true;
                }
            }
            if (filesDeleted) {
                logger.info("Temporary files cleanup completed. Files were deleted.");
            } else {
                logger.info("No matching temporary files to clean.");
            }
        } else {
            logger.info("Temporary directory is empty or does not contain matching files.");
        }
    }


}
