package com.gdtw.imgshare.service;

import com.gdtw.dailystatistic.service.DailyStatisticService;
import com.gdtw.general.exception.InsufficientDiskSpaceException;
import com.gdtw.general.helper.ServiceUsageCounterHelper;
import com.gdtw.general.helper.RedisObjectCacheHelper;
import com.gdtw.general.util.CodecImgIdUtil;
import com.gdtw.general.helper.JwtHelper;
import com.gdtw.imgshare.dto.AlbumCreationRequestDTO;
import com.gdtw.imgshare.dto.ShareImgAlbumInfoDTO;
import com.gdtw.imgshare.dto.ShareImgInfoDTO;
import com.gdtw.imgshare.model.ShareImgAlbumJpa;
import com.gdtw.imgshare.model.ShareImgAlbumVO;
import com.gdtw.imgshare.model.ShareImgJpa;
import com.gdtw.imgshare.model.ShareImgVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

@Service
public class ImgShareService {

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Value("${app.baseUrlForImageDownload}")
    private String baseUrlForImageDownload;

    @Value("${app.imageStoragePath}")
    private String imageStoragePath;

    @Value("${app.imageNginxStaticPath}")
    private String imageNginxStaticPath;

    @Value("${app.min-disk-space}")
    private String minDiskSpace;

    private static final Logger logger = LoggerFactory.getLogger(ImgShareService.class);
    private static final String ALBUM_RETURN_SIA_CODE_KEY = "sia_code";
    private static final String CACHE_KEY_ALBUM_IMAGES_PREFIX = "is_resp:album:";
    private static final String CACHE_KEY_SINGLE_IMAGE_PREFIX = "is_resp:single:";
    private static final String CACHE_KEY_IMAGES = "images";
    private static final String CACHE_KEY_ERROR = "error";
    private static final String ERROR_INVALID_TOKEN = "非法或失效的令牌。 - Invalid or expired token.";
    private static final String ERROR_UNAUTHORIZED = "未授權的訪問，請重新載入頁面。 - Unauthorized access, please refresh your browser.";
    private static final String ERROR_ALBUM_NOT_FOUND = "Album not found.";
    private static final String ERROR_IMAGE_NOT_FOUND = "Image not found.";
    private static final String USAGE_SIA_KEY_PREFIX = "sia:usage:";
    private static final String USAGE_SI_KEY_PREFIX = "si:usage:";
    private static final Duration TTL_DURATION = Duration.ofMinutes(3);

    private final ImgSharePersistenceService imgSharePersistenceService;
    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;
    private final ServiceUsageCounterHelper serviceUsageCounterHelper;
    private final JwtHelper jwtHelper;
    private final RedisObjectCacheHelper redisObjectCacheUtil;
    private final DailyStatisticService dailyStatisticService;
    private final RedisTemplate<String, Integer> redisStringIntegerTemplate;
    private static final Random RANDOM = new Random();

    public ImgShareService(ImgSharePersistenceService imgSharePersistenceService, ShareImgAlbumJpa shareImgAlbumJpa, ShareImgJpa shareImgJpa, ServiceUsageCounterHelper serviceUsageCounterHelper, JwtHelper jwtHelper, RedisObjectCacheHelper redisObjectCacheHelper, DailyStatisticService dailyStatisticService, @Qualifier("redisStringIntegerTemplate") RedisTemplate<String, Integer> redisStringIntegerTemplate) {
        this.imgSharePersistenceService = imgSharePersistenceService;
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
        this.serviceUsageCounterHelper = serviceUsageCounterHelper;
        this.jwtHelper = jwtHelper;
        this.redisObjectCacheUtil = redisObjectCacheHelper;
        this.dailyStatisticService = dailyStatisticService;
        this.redisStringIntegerTemplate = redisStringIntegerTemplate;
    }

    @Transactional
    public Map<String, String> createNewAlbumAndImage(AlbumCreationRequestDTO requestDTO) {
        long requiredSpace = parseDiskSpace(minDiskSpace);
        if (!hasSufficientDiskSpace(imageStoragePath, requiredSpace)) {
            logger.error("Insufficient disk space at path {}. At least {} is required.", imageStoragePath, minDiskSpace);
            throw new InsufficientDiskSpaceException("Insufficient disk space. The server has reached its capacity limit.");
        }

        ShareImgAlbumVO createdAlbumVO = imgSharePersistenceService.createNewAlbumInMySQL(requestDTO);
        imgSharePersistenceService.createNewImagesInMySQL(requestDTO, createdAlbumVO);

        Map<String, String> response = new HashMap<>();
        response.put(ALBUM_RETURN_SIA_CODE_KEY, createdAlbumVO.getSiaCode());
        return response;
    }

    public Map<String, Object> isShareImageAlbumPasswordProtected(String code) {
        return imgSharePersistenceService.isShareImageAlbumPasswordProtected(code);
    }

    public Map<String, Object> isShareImagePasswordProtected(String code) {
        return imgSharePersistenceService.isShareImagePasswordProtected(code);
    }

    public Map<String, Object> checkAlbumPassword(String code, String password) {
        Map<String, Object> response = new HashMap<>();
        Integer siaId = CodecImgIdUtil.decodeImgId(code);
        String redisKey = ImgSharePersistenceService.ALBUM_INFO_CACHE_PREFIX + siaId;

        Optional<ShareImgAlbumInfoDTO> dtoOpt = redisObjectCacheUtil.getObject(redisKey, ShareImgAlbumInfoDTO.class);
        if (dtoOpt.isEmpty()) {
            imgSharePersistenceService.isShareImageAlbumPasswordProtected(code);
            dtoOpt = redisObjectCacheUtil.getObject(redisKey, ShareImgAlbumInfoDTO.class);
        }

        ShareImgAlbumInfoDTO dto = dtoOpt.orElse(null);
        if (dto != null && password.equalsIgnoreCase(dto.getSiaPassword())) {
            response.put(ImgSharePersistenceService.STAGE_CHECK_PASSWORD, true);
            String token = jwtHelper.generateToken(code, ImgSharePersistenceService.STAGE_PASSED_PASSWORD);
            response.put(ImgSharePersistenceService.DOWNLOAD_TOKEN, token);
        } else {
            try {
                Thread.sleep(1500L + RANDOM.nextInt(1000));
            } catch (InterruptedException ignored) {
                // just for increasing guessing time
            }
            response.put(ImgSharePersistenceService.STAGE_CHECK_PASSWORD, false);
        }
        return response;
    }

    public Map<String, Object> checkImagePassword(String code, String password) {
        Map<String, Object> response = new HashMap<>();
        Integer siId = CodecImgIdUtil.decodeImgId(code);
        String redisKey = ImgSharePersistenceService.IMAGE_INFO_CACHE_PREFIX + siId;

        Optional<ShareImgInfoDTO> dtoOpt = redisObjectCacheUtil.getObject(redisKey, ShareImgInfoDTO.class);
        if (dtoOpt.isEmpty()) {
            imgSharePersistenceService.isShareImagePasswordProtected(code);
            dtoOpt = redisObjectCacheUtil.getObject(redisKey, ShareImgInfoDTO.class);
        }

        ShareImgInfoDTO dto = dtoOpt.orElse(null);
        if (dto != null && password.equals(dto.getSiPassword())) {
            response.put(ImgSharePersistenceService.STAGE_CHECK_PASSWORD, true);
            String token = jwtHelper.generateToken(code, ImgSharePersistenceService.STAGE_PASSED_PASSWORD);
            response.put(ImgSharePersistenceService.DOWNLOAD_TOKEN, token);
        } else {
            try {
                Thread.sleep(1000L + RANDOM.nextInt(1000));
            } catch (InterruptedException ignored) {
                // just for increasing guessing time
            }
            response.put(ImgSharePersistenceService.STAGE_CHECK_PASSWORD, false);
        }
        return response;
    }

    public Map<String, Object> getAlbumImages(String token) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> claims = validateTokenAndStage(token);
        if (claims.containsKey(CACHE_KEY_ERROR)) return claims;

        String code = (String) claims.get("subject");
        Integer siaImageId = CodecImgIdUtil.decodeImgId(code);
        String redisKey = CACHE_KEY_ALBUM_IMAGES_PREFIX + siaImageId;

        Map<String, Object> cachedResponse = redisObjectCacheUtil.getMap(redisKey);
        if (!cachedResponse.isEmpty()) {
            Object images = cachedResponse.get(CACHE_KEY_IMAGES);
            if (images instanceof List<?> list && !list.isEmpty()) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> imageMap) {
                        Object siIdObj = imageMap.get("siId");
                        if (siIdObj instanceof Number siId) {
                            serviceUsageCounterHelper.countServiceUsage(USAGE_SI_KEY_PREFIX, siId.intValue());
                            dailyStatisticService.incrementImgUsed();
                        }
                    }
                }
                serviceUsageCounterHelper.countServiceUsage(USAGE_SIA_KEY_PREFIX, siaImageId);
                return cachedResponse;
            }
        }

        Optional<ShareImgAlbumVO> albumOptional = shareImgAlbumJpa.findBySiaId(siaImageId);
        if (albumOptional.isEmpty()) {
            response.put(CACHE_KEY_ERROR, ERROR_ALBUM_NOT_FOUND);
            return response;
        }

        ShareImgAlbumVO album = albumOptional.get();
        response.put("siaNsfw", album.getSiaNsfw());
        response.put("siaEndDate", album.getSiaEndDate().toString());
        response.put("siaTotalVisited", album.getSiaTotalVisited());

        List<ShareImgVO> images = shareImgJpa.findByAlbum_SiaIdOrderBySiIdAsc(siaImageId);
        List<Map<String, Object>> imageList = new ArrayList<>();

        for (ShareImgVO image : images) {
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("siId", image.getSiId());
            imageMap.put("siCode", image.getSiCode());
            imageMap.put("siName", image.getSiName());
            String imageUrl = baseUrlForImageDownload + imageNginxStaticPath + image.getSiName();
            imageMap.put("imageUrl", imageUrl);
            String imageSingleModeUrl = baseUrl + "i/" + image.getSiCode();
            imageMap.put("imageSingleModeUrl", imageSingleModeUrl);
            imageList.add(imageMap);
            serviceUsageCounterHelper.countServiceUsage(USAGE_SI_KEY_PREFIX, image.getSiId());
            dailyStatisticService.incrementImgUsed();
        }

        response.put(CACHE_KEY_IMAGES, imageList);
        redisObjectCacheUtil.setObject(redisKey, response, TTL_DURATION);
        serviceUsageCounterHelper.countServiceUsage(USAGE_SIA_KEY_PREFIX, siaImageId);
        return response;
    }

    public Map<String, Object> getSingleImage(String token) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> claims = validateTokenAndStage(token);
        if (claims.containsKey(CACHE_KEY_ERROR)) return claims;

        String code = (String) claims.get("subject");
        Integer siImageId = CodecImgIdUtil.decodeImgId(code);
        String redisKey = CACHE_KEY_SINGLE_IMAGE_PREFIX + siImageId;

        Map<String, Object> cachedResponse = redisObjectCacheUtil.getMap(redisKey);
        if (!cachedResponse.isEmpty() && cachedResponse.get("siId") instanceof Number siId) {

            serviceUsageCounterHelper.countServiceUsage(USAGE_SI_KEY_PREFIX, siId.intValue());
            return cachedResponse;
        }

        Optional<ShareImgVO> imageOptional = shareImgJpa.findById(siImageId);
        if (imageOptional.isEmpty()) {
            response.put(CACHE_KEY_ERROR, ERROR_IMAGE_NOT_FOUND);
            return response;
        }

        ShareImgVO image = imageOptional.get();
        response.put("siId", image.getSiId());
        response.put("siCode", image.getSiCode());
        response.put("siName", image.getSiName());
        response.put("siNsfw", image.getSiNsfw());
        response.put("siEndDate", image.getSiEndDate().toString());
        response.put("siTotalVisited", image.getSiTotalVisited());
        String imageUrl = baseUrlForImageDownload + imageNginxStaticPath + image.getSiName();
        response.put("imageUrl", imageUrl);

        redisObjectCacheUtil.setObject(redisKey, response, TTL_DURATION);
        serviceUsageCounterHelper.countServiceUsage(USAGE_SI_KEY_PREFIX, siImageId);
        return response;
    }

    public void reportImgAlbum(Integer albumId, Map<String, String> result) {
        Optional<ShareImgAlbumVO> optional = shareImgAlbumJpa.findBySiaId(albumId);
        if (optional.isEmpty()) {
            result.put("reportStatus", "false");
            result.put("response", "查無此相簿。\nAlbum not found.");
            return;
        }

        ShareImgAlbumVO album = optional.get();
        if (album.getSiaStatus() != null && album.getSiaStatus() == 1) {
            result.put("reportStatus", "false");
            result.put("response", "該相簿已封鎖，無法再次舉報。\nThis album has been blocked, cannot report again.");
            return;
        }

        album.setSiaReported(album.getSiaReported() == null ? 1 : album.getSiaReported() + 1);
        shareImgAlbumJpa.save(album);

        List<ShareImgVO> images = shareImgJpa.findByAlbum_SiaIdOrderBySiIdAsc(albumId);
        for (ShareImgVO img : images) {
            if (img.getSiStatus() != null && img.getSiStatus() == 0) {
                img.setSiReported(img.getSiReported() == null ? 1 : img.getSiReported() + 1);
                shareImgJpa.save(img);
            }
        }

        result.put("reportStatus", "true");
        result.put("response", "舉報成功！\nReport successful!");
    }

    public void reportImage(Integer imageId, Map<String, String> result) {
        int updatedRows = shareImgJpa.incrementReportIfNotBlocked(imageId);

        if (updatedRows == 1) {
            result.put("reportStatus", "true");
            result.put("response", "舉報成功！\nReport successful!");
        } else {
            boolean exists = shareImgJpa.existsById(imageId);
            result.put("reportStatus", "false");
            if (!exists) {
                result.put("response", "查無此圖片。\nImage not found.");
            } else {
                result.put("response", "該圖片已封鎖，無法再次舉報。\nThis image has been blocked, cannot report again.");
            }
        }
    }

    @Transactional
    public void syncSiaUsageToMySQL() {
        Set<String> keys = redisStringIntegerTemplate.keys(USAGE_SIA_KEY_PREFIX + "*");
        for (String key : keys) {
            Integer siaId = Integer.parseInt(key.split(":")[2]);
            Integer usageCount = redisStringIntegerTemplate.opsForValue().get(key);
            int usage = (usageCount != null) ? usageCount : 0;

            Optional<ShareImgAlbumVO> optionalSiaObject = shareImgAlbumJpa.findById(siaId);
            if (optionalSiaObject.isPresent()) {
                ShareImgAlbumVO shareImgAlbumVO = optionalSiaObject.get();
                shareImgAlbumVO.setSiaTotalVisited(shareImgAlbumVO.getSiaTotalVisited() + usage);
                shareImgAlbumJpa.save(shareImgAlbumVO);
                redisStringIntegerTemplate.delete(key);
            }
        }
    }

    @Transactional
    public void syncSiUsageToMySQL() {
        Set<String> keys = redisStringIntegerTemplate.keys(USAGE_SI_KEY_PREFIX + "*");
        for (String key : keys) {
            Integer siId = Integer.parseInt(key.split(":")[2]);
            Integer usageCount = redisStringIntegerTemplate.opsForValue().get(key);
            int usage = (usageCount != null) ? usageCount : 0;

            Optional<ShareImgVO> optionalSiObject = shareImgJpa.findById(siId);
            if (optionalSiObject.isPresent()) {
                ShareImgVO shareImgVO = optionalSiObject.get();
                shareImgVO.setSiTotalVisited(shareImgVO.getSiTotalVisited() + usage);
                shareImgJpa.save(shareImgVO);
                redisStringIntegerTemplate.delete(key);
            }
        }
    }

    private boolean hasSufficientDiskSpace(String path, long requiredSpace) {
        try {
            FileStore fileStore = Files.getFileStore(Paths.get(path));
            return fileStore.getUsableSpace() >= requiredSpace;
        } catch (IOException e) {
            logger.error("Failed to check disk space for path: {}.", path, e);
            return false;
        }
    }

    private long parseDiskSpace(String space) {
        try {
            if (space.endsWith("GB")) return Long.parseLong(space.replace("GB", "").trim()) * 1024 * 1024 * 1024;
            if (space.endsWith("MB")) return Long.parseLong(space.replace("MB", "").trim()) * 1024 * 1024;
            throw new IllegalArgumentException("Invalid disk space format: " + space);
        } catch (NumberFormatException e) {
            logger.error("Invalid disk space value: {}.", space, e);
            throw new IllegalArgumentException("Invalid disk space format: " + space);
        }
    }

    private Map<String, Object> validateTokenAndStage(String token) {
        Map<String, Object> claims = jwtHelper.validateToken(token);
        if (claims == null) return Map.of(CACHE_KEY_ERROR, ERROR_INVALID_TOKEN);

        String stage = (String) claims.get("stage");
        if (!ImgSharePersistenceService.STAGE_PASSED_PASSWORD.equals(stage) &&
                !ImgSharePersistenceService.STAGE_NO_PASSWORD.equals(stage)) {
            return Map.of(CACHE_KEY_ERROR, ERROR_UNAUTHORIZED);
        }
        return claims;
    }

}
