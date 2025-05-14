package com.gdtw.imgshare.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdtw.dailystatistic.model.DailyStatisticService;
import com.gdtw.general.exception.InsufficientDiskSpaceException;
import com.gdtw.general.util.codec.ImgIdEncoderDecoderUtil;
import com.gdtw.general.util.jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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
    private static final String CACHE_KEY_ALBUM_IMAGES_PREFIX = "albumImages:";
    private static final String CACHE_KEY_SINGLE_IMAGE_PREFIX = "singleImage:";
    private static final String CACHE_KEY_IMAGES = "images";
    private static final String CACHE_KEY_ERROR = "error";
    private static final String ERROR_INVALID_TOKEN = "非法或失效的令牌。 - Invalid or expired token.";
    private static final String ERROR_UNAUTHORIZED = "未授權的訪問，請重新載入頁面。 - Unauthorized access, please refresh your browser.";
    private static final String ERROR_ALBUM_NOT_FOUND = "Album not found.";
    private static final String ERROR_IMAGE_NOT_FOUND = "Image not found.";
    private static final String USAGE_SIA_KEY_PREFIX = "sia:usage:";
    private static final String USAGE_SI_KEY_PREFIX = "si:usage:";
    private static final Duration TTL_DURATION = Duration.ofMinutes(10);

    private final ImgSharePersistenceService imgSharePersistenceService;
    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;
    private final JwtUtil jwtUtil;
    private final DailyStatisticService dailyStatisticService;
    private final RedisTemplate<String, String> redisStringStringTemplate;
    private final RedisTemplate<String, Object> universalRedisTemplate;
    private final ObjectMapper objectMapper;

    public ImgShareService(
            ImgSharePersistenceService imgSharePersistenceService,
            ShareImgAlbumJpa shareImgAlbumJpa,
            ShareImgJpa shareImgJpa,
            JwtUtil jwtUtil,
            DailyStatisticService dailyStatisticService,
            @Qualifier("redisStringStringTemplate") RedisTemplate<String, String> redisStringStringTemplate,
            @Qualifier("universalRedisTemplate") RedisTemplate<String, Object> universalRedisTemplate,
            ObjectMapper objectMapper
    ) {
        this.imgSharePersistenceService = imgSharePersistenceService;
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
        this.jwtUtil = jwtUtil;
        this.dailyStatisticService = dailyStatisticService;
        this.redisStringStringTemplate = redisStringStringTemplate;
        this.universalRedisTemplate = universalRedisTemplate;
        this.objectMapper = objectMapper;
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
        Integer siaId = ImgIdEncoderDecoderUtil.decodeImgId(code);
        String redisKey = ImgSharePersistenceService.ALBUM_INFO_CACHE_PREFIX + siaId;

        ShareImgAlbumInfoDTO dto = (ShareImgAlbumInfoDTO) universalRedisTemplate.opsForValue().get(redisKey);

        if (dto == null) {
            imgSharePersistenceService.isShareImageAlbumPasswordProtected(code);
            dto = (ShareImgAlbumInfoDTO) universalRedisTemplate.opsForValue().get(redisKey);
        }

        if (dto != null && password.equalsIgnoreCase(dto.getSiaPassword())) {
            response.put(ImgSharePersistenceService.STAGE_CHECK_PASSWORD, true);
            String token = jwtUtil.generateToken(code, ImgSharePersistenceService.STAGE_PASSED_PASSWORD);
            response.put(ImgSharePersistenceService.DOWNLOAD_TOKEN, token);
        } else {
            response.put(ImgSharePersistenceService.STAGE_CHECK_PASSWORD, false);
        }

        return response;
    }

    public Map<String, Object> checkImagePassword(String code, String password) {

        Map<String, Object> response = new HashMap<>();
        Integer siId = ImgIdEncoderDecoderUtil.decodeImgId(code);
        String redisKey = ImgSharePersistenceService.IMAGE_INFO_CACHE_PREFIX + siId;

        ShareImgInfoDTO dto = (ShareImgInfoDTO) universalRedisTemplate.opsForValue().get(redisKey);

        if (dto == null) {
            imgSharePersistenceService.isShareImagePasswordProtected(code);
            dto = (ShareImgInfoDTO) universalRedisTemplate.opsForValue().get(redisKey);
        }

        if (dto != null && password.equals(dto.getSiPassword())) {
            response.put(ImgSharePersistenceService.STAGE_CHECK_PASSWORD, true);
            String token = jwtUtil.generateToken(code, ImgSharePersistenceService.STAGE_PASSED_PASSWORD);
            response.put(ImgSharePersistenceService.DOWNLOAD_TOKEN, token);
        } else {
            response.put(ImgSharePersistenceService.STAGE_CHECK_PASSWORD, false);
        }

        return response;
    }

    public Map<String, Object> getAlbumImages(String token) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> claims = validateTokenAndStage(token);
        if (claims.containsKey(CACHE_KEY_ERROR)) return claims;

        String code = (String) claims.get("subject");
        Integer siaImageId = ImgIdEncoderDecoderUtil.decodeImgId(code);
        String redisKey = CACHE_KEY_ALBUM_IMAGES_PREFIX + siaImageId;

        Map<String, Object> cachedResponse = getResponseFromRedis(redisKey);
        if (!cachedResponse.isEmpty()) {
            Object images = cachedResponse.get(CACHE_KEY_IMAGES);
            if (images instanceof List<?> list && !list.isEmpty()) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> imageMap) {
                        Object siIdObj = imageMap.get("siId");
                        if (siIdObj instanceof Number siId) {
                            countImageUsage(siId.intValue());
                            dailyStatisticService.incrementImgUsed();
                        }
                    }
                }
                countAlbumUsage(siaImageId);
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

            countImageUsage(image.getSiId());
            dailyStatisticService.incrementImgUsed();
        }

        response.put(CACHE_KEY_IMAGES, imageList);
            saveResponseToRedis(redisKey, response);
        countAlbumUsage(siaImageId);
        return response;
    }

    public Map<String, Object> getSingleImage(String token) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> claims = validateTokenAndStage(token);
        if (claims.containsKey(CACHE_KEY_ERROR)) return claims;

        String code = (String) claims.get("subject");
        Integer siImageId = ImgIdEncoderDecoderUtil.decodeImgId(code);
        String redisKey = CACHE_KEY_SINGLE_IMAGE_PREFIX + siImageId;

        Map<String, Object> cachedResponse = getResponseFromRedis(redisKey);
        if (!cachedResponse.isEmpty() && cachedResponse.get("siId") instanceof Number siId) {
            countImageUsage(siId.intValue());
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

        saveResponseToRedis(redisKey, response);
        countImageUsage(siImageId);
        return response;
    }

    public boolean isValidShareImageAlbum(String code) {
        return Boolean.TRUE.equals(
                imgSharePersistenceService.isShareImageAlbumPasswordProtected(code)
                        .get(ImgSharePersistenceService.RESP_IS_VALID)
        );
    }

    public boolean isValidShareImage(String code) {
        return Boolean.TRUE.equals(
                imgSharePersistenceService.isShareImagePasswordProtected(code)
                        .get(ImgSharePersistenceService.RESP_IS_VALID)
        );
    }

    public void countAlbumUsage(Integer siaId) {
        String redisKey = USAGE_SIA_KEY_PREFIX + siaId;
        redisStringStringTemplate.opsForValue().increment(redisKey, 1);
    }

    public void countImageUsage(Integer siId) {
        String redisKey = USAGE_SI_KEY_PREFIX + siId;
        redisStringStringTemplate.opsForValue().increment(redisKey, 1);
    }

    @Scheduled(cron = "${task.schedule.cron.albumUsageStatisticService}")
    @Transactional
    public void syncSiaUsageToMySQL() {
        Set<String> keys = redisStringStringTemplate.keys(USAGE_SIA_KEY_PREFIX + "*");
        for (String key : keys) {
            Integer siaId = Integer.parseInt(key.split(":")[2]);
            String value = redisStringStringTemplate.opsForValue().get(key);
            if (value == null) {
                logger.warn("Missing Redis value for 'album' key '{}', skipping...", key);
                continue;
            }
            Integer usageCount = Integer.parseInt(value);

            Optional<ShareImgAlbumVO> optionalSiaObject = shareImgAlbumJpa.findById(siaId);
            if (optionalSiaObject.isPresent()) {
                ShareImgAlbumVO shareImgAlbumVO = optionalSiaObject.get();
                shareImgAlbumVO.setSiaTotalVisited(shareImgAlbumVO.getSiaTotalVisited() + usageCount);
                shareImgAlbumJpa.save(shareImgAlbumVO);
                // delete recorded data in Redis
                redisStringStringTemplate.delete(key);
            }
        }
        logger.info("Sync 'Image Album' usage to MySQL!");
    }

    @Scheduled(cron = "${task.schedule.cron.imageUsageStatisticService}")
    @Transactional
    public void syncSiUsageToMySQL() {
        Set<String> keys = redisStringStringTemplate.keys(USAGE_SI_KEY_PREFIX + "*");

        for (String key : keys) {
            String rawValue = redisStringStringTemplate.opsForValue().get(key);
            if (rawValue == null) {
                logger.warn("Missing Redis value for 'image' key '{}', skipping...", key);
                continue;
            }

            Integer siId = Integer.parseInt(key.split(":")[2]);
            Integer usageCount = Integer.parseInt(rawValue);

            Optional<ShareImgVO> optionalSiObject = shareImgJpa.findById(siId);
            if (optionalSiObject.isPresent()) {
                ShareImgVO shareImgVO = optionalSiObject.get();
                shareImgVO.setSiTotalVisited(shareImgVO.getSiTotalVisited() + usageCount);
                shareImgJpa.save(shareImgVO);
                redisStringStringTemplate.delete(key);
            }
        }
        logger.info("Sync 'Single Image' usage to MySQL!");
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
        Map<String, Object> claims = jwtUtil.validateToken(token);
        if (claims == null) return Map.of(CACHE_KEY_ERROR, ERROR_INVALID_TOKEN);

        String stage = (String) claims.get("stage");
        if (!ImgSharePersistenceService.STAGE_PASSED_PASSWORD.equals(stage) &&
                !ImgSharePersistenceService.STAGE_NO_PASSWORD.equals(stage)) {
            return Map.of(CACHE_KEY_ERROR, ERROR_UNAUTHORIZED);
        }
        return claims;
    }

    private void saveResponseToRedis(String key, Map<String, Object> response) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(response);
            redisStringStringTemplate.opsForValue().set(key, jsonResponse, TTL_DURATION);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize and cache response for key: {}", key, e);
        }
    }

    private Map<String, Object> getResponseFromRedis(String key) {
        try {
            String jsonResponse = redisStringStringTemplate.opsForValue().get(key);
            if (jsonResponse == null) {
                logger.debug("Redis cache miss for key: {}", key);
                return Collections.emptyMap();
            }
            return objectMapper.readValue(jsonResponse, new TypeReference<>() {
            });
        } catch (Exception e) {
            logger.warn("Failed to parse Redis cache value for key: {}, fallback to DB.", key, e);
            return Collections.emptyMap();
        }
    }

}