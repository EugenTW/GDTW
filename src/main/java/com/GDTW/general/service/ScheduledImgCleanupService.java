package com.GDTW.general.service;

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

    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;

    public ScheduledImgCleanupService(ShareImgAlbumJpa shareImgAlbumJpa, ShareImgJpa shareImgJpa) {
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
    }

    // cleanup task for old image files and database records runs daily at 1 AM
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cleanupExpiredImages() {
        logger.info("Starting cleanup of expired images...");

        LocalDate today = LocalDate.now();

        List<ShareImgAlbumVO> expiredAlbums = shareImgAlbumJpa.findBySiaEndDateBeforeAndSiaStatus(today, (byte) 0);
        for (ShareImgAlbumVO album : expiredAlbums) {
            album.setSiaStatus((byte) 1);
            shareImgAlbumJpa.save(album);
        }
        logger.info("Expired albums status updated.");

        List<ShareImgVO> expiredImages = shareImgJpa.findBySiEndDateBeforeAndSiStatus(today, (byte) 0);
        for (ShareImgVO image : expiredImages) {
            image.setSiName(null);
            image.setSiStatus((byte) 1);
            shareImgJpa.save(image);
            deleteImageFile(image.getSiName());
        }
        logger.info("Expired images deleted and status updated.");
    }

    private void deleteImageFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(imageStoragePath, fileName);
            Files.deleteIfExists(filePath);
            logger.info("Deleted file: {}", fileName);
        } catch (Exception e) {
            logger.error("Failed to delete file: {}", fileName, e);
        }
    }
}
