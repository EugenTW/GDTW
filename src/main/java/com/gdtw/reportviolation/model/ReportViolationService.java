package com.gdtw.reportviolation.model;

import com.gdtw.general.util.codec.IdEncoderDecoderUtil;
import com.gdtw.general.util.codec.ImgIdEncoderDecoderUtil;
import com.gdtw.imgshare.model.ImgShareService;
import com.gdtw.shorturl.model.ShortUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportViolationService {

    private static final Logger logger = LoggerFactory.getLogger(ReportViolationService.class);
    private final ReportViolationPersistenceService reportViolationPersistenceService;
    private final ShortUrlService shortUrlService;
    private final ImgShareService imgShareService;
    private final RedisTemplate<String, String> redisStringStringTemplate;

    public ReportViolationService(ReportViolationPersistenceService reportViolationPersistenceService, ShortUrlService shortUrlService, ImgShareService imgShareService, RedisTemplate<String, String> redisStringStringTemplate) {
        this.reportViolationPersistenceService = reportViolationPersistenceService;
        this.shortUrlService = shortUrlService;
        this.imgShareService = imgShareService;
        this.redisStringStringTemplate = redisStringStringTemplate;
    }

    public Map<String, String> createViolationReport(ReportRequestDTO dto, String originalIp) {
        Map<String, String> result = new HashMap<>();

        if (isRateLimitExceeded(originalIp)) {
            result.put("reportStatus", "false");
            result.put("response", "您每分鐘只能舉報一次，請稍後再試！\nYou can only report once per minute, please try again later!");
            return result;
        }

        try {
            String reportedUrl = dto.getTargetUrl();
            int reportedType = dto.getReportType();

            reportViolationPersistenceService.saveReportViolationTransactional(dto, originalIp);

            switch (reportedType) {
                case 1:
                    Integer shortUrlId = IdEncoderDecoderUtil.decodeId(reportedUrl);
                    shortUrlService.reportShortUrl(shortUrlId, result);
                    break;
                case 2:
                    Integer albumId = ImgIdEncoderDecoderUtil.decodeImgId(reportedUrl);
                    imgShareService.reportImgAlbum(albumId, result);
                    break;
                case 3:
                    Integer imageId = ImgIdEncoderDecoderUtil.decodeImgId(reportedUrl);
                    imgShareService.reportImage(imageId, result);
                    break;
                default:
                    result.put("reportStatus", "false");
                    result.put("response", "非定義的'舉報種類'！\nUndefined 'Reported Type'!");
                    return result;
            }

            if (!result.containsKey("reportStatus")) {
                setRateLimitForIp(originalIp);
                result.put("reportStatus", "true");
                result.put("response", "舉報成功！\nReport submitted!");
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

    private boolean isRateLimitExceeded(String ip) {
        return !isLocalhost(ip) && redisStringStringTemplate.hasKey("reportlimit:" + ip);
    }

    private void setRateLimitForIp(String ip) {
        if (isLocalhost(ip)) {
            return;
        }
        String redisKey = "reportlimit:" + ip;
        redisStringStringTemplate.opsForValue().set(redisKey, "1", Duration.ofMinutes(1));
    }

    private boolean isLocalhost(String ip) {
        return "localhost".equals(ip) || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

}
