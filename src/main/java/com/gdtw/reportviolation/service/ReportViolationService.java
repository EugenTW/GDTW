package com.gdtw.reportviolation.service;

import com.gdtw.general.util.CodecShortUrlIdUtil;
import com.gdtw.general.util.CodecImgIdUtil;
import com.gdtw.imgshare.service.ImgShareService;
import com.gdtw.reportviolation.model.ReportRequestDTO;
import com.gdtw.shorturl.service.ShortUrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReportViolationService {

    private final ReportViolationPersistenceService reportViolationPersistenceService;
    private final ShortUrlService shortUrlService;
    private final ImgShareService imgShareService;

    public ReportViolationService(ReportViolationPersistenceService reportViolationPersistenceService, ShortUrlService shortUrlService, ImgShareService imgShareService) {
        this.reportViolationPersistenceService = reportViolationPersistenceService;
        this.shortUrlService = shortUrlService;
        this.imgShareService = imgShareService;
    }

    @Transactional
    public Map<String, String> createViolationReport(ReportRequestDTO dto, String originalIp) {
        Map<String, String> result = new HashMap<>();

        String reportedUrl = dto.getTargetUrl();
        int reportedType = dto.getReportType();

        reportViolationPersistenceService.saveReportViolationTransactional(dto, originalIp);

        switch (reportedType) {
            case 1:
                Integer shortUrlId = CodecShortUrlIdUtil.decodeId(reportedUrl);
                shortUrlService.reportShortUrl(shortUrlId, result);
                break;
            case 2:
                Integer albumId = CodecImgIdUtil.decodeImgId(reportedUrl);
                imgShareService.reportImgAlbum(albumId, result);
                break;
            case 3:
                Integer imageId = CodecImgIdUtil.decodeImgId(reportedUrl);
                imgShareService.reportImage(imageId, result);
                break;
            default:
                result.put("reportStatus", "false");
                result.put("response", "非定義的'舉報種類'！<br>Undefined 'Reported Type'!");
                return result;
        }

        if (!result.containsKey("reportStatus")) {
            result.put("reportStatus", "false");
            result.put("response", "舉報失敗，請稍後再試。<br>Report failed, please try again.");
        }
        return result;
    }

}


