package com.gdtw.general.service.scheduled;

import com.gdtw.shorturl.model.ShortUrlJpa;
import com.gdtw.imgshare.model.ShareImgAlbumJpa;
import com.gdtw.imgshare.model.ShareImgJpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduledViolationService {

    @Value("${violationReport.reportedThreshold}")
    private int violationReportReportedThreshold;

    @Value("${violationReport.reportedProportion}")
    private double violationReportReportedProportion;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledViolationService.class);

    private final ShortUrlJpa shortUrlJpa;
    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;

    public ScheduledViolationService(
            ShortUrlJpa shortUrlJpa,
            ShareImgAlbumJpa shareImgAlbumJpa,
            ShareImgJpa shareImgJpa
    ) {
        this.shortUrlJpa = shortUrlJpa;
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
    }

    @Scheduled(cron = "${task.schedule.cron.dailyViolationCheckService}")
    public void checkDailyViolation() {

        int blockedShortUrls = shortUrlJpa.blockReportedShortUrls(violationReportReportedThreshold, violationReportReportedProportion);
        List<Integer> blockedAlbumIds = shareImgAlbumJpa.findBlockedAlbumIds(violationReportReportedThreshold, violationReportReportedProportion);
        int blockedAlbums = shareImgAlbumJpa.blockReportedAlbums(violationReportReportedThreshold, violationReportReportedProportion);
        int blockedImagesByAlbum = 0;
        if (!blockedAlbumIds.isEmpty()) {
            blockedImagesByAlbum = shareImgJpa.blockImagesByAlbumIds(blockedAlbumIds);
        }
        int blockedImages = shareImgJpa.blockReportedImages(violationReportReportedThreshold, violationReportReportedProportion);

        logger.info("[ViolationCheck] Blocked {} short URLs, {} albums, {} album's images, {} single images.",
                blockedShortUrls, blockedAlbums, blockedImagesByAlbum, blockedImages);
    }

}