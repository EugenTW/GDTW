package com.GDTW.general.service.scheduled;

import com.GDTW.imgshare.model.ShareImgAlbumJpa;
import com.GDTW.imgshare.model.ShareImgAlbumVO;
import com.GDTW.imgshare.model.ShareImgJpa;
import com.GDTW.imgshare.model.ShareImgVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.Transactional;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduledImgCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledImgCleanupService.class);


    @Value("${app.imageStoragePath}")
    private String imageStoragePath;

    @Value("${app.imageTrashCanPath}")
    private String imageTrashCanPath;

    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;

    public ScheduledImgCleanupService(ShareImgAlbumJpa shareImgAlbumJpa, ShareImgJpa shareImgJpa) {
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
    }

    // daily task to move old image files to trash and update database records
    @Scheduled(cron = "${task.schedule.cron.dailyImgCleanupService}")
    @Transactional
    public void cleanupExpiredImages() {
        logger.info("Starting cleanup of expired images...");

        LocalDate today = LocalDate.now();

        List<ShareImgAlbumVO> expiredAlbums = shareImgAlbumJpa.findBySiaEndDateBeforeAndSiaStatus(today, (byte) 0);
        for (ShareImgAlbumVO album : expiredAlbums) {
            album.setSiaStatus((byte) 1);
            shareImgAlbumJpa.save(album);
        }
        logger.info("Expired MySQL album status updated");

        List<ShareImgVO> expiredImages = shareImgJpa.findBySiEndDateBeforeAndSiStatus(today, (byte) 0);
        for (ShareImgVO image : expiredImages) {
            String originalSiName = image.getSiName();
            image.setSiName(null);
            image.setSiStatus((byte) 1);
            shareImgJpa.save(image);
            moveImageFileToTrashCan(originalSiName);
        }
        logger.info("Expired MySQL image status updated and images removed.");
    }

    private void moveImageFileToTrashCan(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        try {
            Path sourcePath = Paths.get(imageStoragePath, fileName);
            Path targetPath = Paths.get(imageTrashCanPath, fileName);
            Files.createDirectories(targetPath.getParent());

            Files.move(sourcePath, targetPath);
        } catch (Exception e) {
            logger.error("Failed to move file to trash can: {}", fileName, e);
        }
    }

    // automatically empty trash monthly
    @Scheduled(cron = "${task.schedule.cron.weeklyImgTrashCleanupService}")
    public void clearTrashCan() {
        Path trashCanPath = Paths.get(imageTrashCanPath);
        try {
            if (Files.exists(trashCanPath)) {
                DirectoryStream<Path> directoryStream = Files.newDirectoryStream(trashCanPath);
                for (Path file : directoryStream) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (Exception e) {
                        logger.error("Failed to delete file: {}", file.getFileName(), e);
                    }
                }
                directoryStream.close();
                logger.info("Trash can cleaned up.");
            }
        } catch (Exception e) {
            logger.error("Failed to clear trash can directory. ", e);
        }
    }

}
