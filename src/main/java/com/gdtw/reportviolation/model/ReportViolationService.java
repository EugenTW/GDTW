package com.gdtw.reportviolation.model;

import com.gdtw.general.util.CodecShortUrlIdUtil;
import com.gdtw.general.util.CodecImgIdUtil;
import com.gdtw.imgshare.model.ImgShareService;
import com.gdtw.reportviolation.dto.ReportRequestDTO;
import com.gdtw.shorturl.model.ShortUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReportViolationService {

    private static final Logger logger = LoggerFactory.getLogger(ReportViolationService.class);
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

        try {
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
                    result.put("response", "非定義的'舉報種類'！\nUndefined 'Reported Type'!");
                    return result;
            }

            if (!result.containsKey("reportStatus")) {
                result.put("reportStatus", "false");
                result.put("response", "舉報失敗，請稍後再試。\nReport failed, please try again.");
            }
        } catch (DataIntegrityViolationException ex) {
            result.put("reportStatus", "false");
            result.put("response", "您已經針對此資源舉報過，不能重複舉報。\nYou have already reported this resource and cannot report again.");
        } catch (Exception e) {
            logger.error("Create Violation Report Error: ", e);
            result.put("reportStatus", "false");
            result.put("response", "伺服器錯誤，請稍後再試。\nServer error, please try again. (" + e.getClass().getSimpleName() + ")");
        }
        return result;
    }

}
