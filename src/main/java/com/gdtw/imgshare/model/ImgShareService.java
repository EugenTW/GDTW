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
    private static final Duration TTL_DURATION = Duration.ofMinutes(10);

    private final ImgShareInternalService imgShareInternalService;
    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;
    private final JwtUtil jwtUtil;
    private final DailyStatisticService dailyStatisticService;
    private final RedisTemplate<String, String> redisStringStringTemplate;
    private final ObjectMapper objectMapper;

    public ImgShareService(ImgShareInternalService imgShareInternalService,
                           ShareImgAlbumJpa shareImgAlbumJpa,
                           ShareImgJpa shareImgJpa,
                           JwtUtil jwtUtil,
                           DailyStatisticService dailyStatisticService,
                           @Qualifier("redisStringStringTemplate") RedisTemplate<String, String> redisTemplate,
                           ObjectMapper objectMapper) {
        this.imgShareInternalService = imgShareInternalService;
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
        this.jwtUtil = jwtUtil;
        this.dailyStatisticService = dailyStatisticService;
        this.redisStringStringTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, String> createNewAlbumAndImage(AlbumCreationRequestDTO requestDTO) {
        long requiredSpace = parseDiskSpace(minDiskSpace);
        if (!hasSufficientDiskSpace(imageStoragePath, requiredSpace)) {
            logger.error("Insufficient disk space at path {}. At least {} is required.", imageStoragePath, minDiskSpace);
            throw new InsufficientDiskSpaceException("Insufficient disk space. The server has reached its capacity limit.");
        }
        ShareImgAlbumVO createdAlbumVO = imgShareInternalService.createNewAlbumInMySQL(requestDTO);
        imgShareInternalService.createNewImagesInMySQL(requestDTO, createdAlbumVO);
        Map<String, String> response = new HashMap<>();
        response.put("sia_code", createdAlbumVO.getSiaCode());
        return response;
    }

    public Map<String, Object> isShareImageAlbumPasswordProtected(String code) {
        return imgShareInternalService.isShareImageAlbumPasswordProtected(code);
    }

    public Map<String, Object> isShareImagePasswordProtected(String code) {
        return imgShareInternalService.isShareImagePasswordProtected(code);
    }
    
    public Map<String, Object> checkAlbumPassword(String code, String password) {
        Map<String, Object> response = new HashMap<>();
        Integer siaImageAlbumId = toDecodeId(code);
        Optional<ShareImgAlbumVO> albumOptional = shareImgAlbumJpa.findById(siaImageAlbumId);

        if (albumOptional.isPresent()) {
            ShareImgAlbumVO album = albumOptional.get();
            String storedPassword = album.getSiaPassword();
            if (storedPassword.equals(password)) {
                response.put("checkPassword", true);
                String token = jwtUtil.generateToken(code, "passwordPassed");
                response.put("token", token);
            } else {
                response.put("checkPassword", false);
            }
        } else {
            response.put("checkPassword", false);
        }
        return response;
    }
    
    public Map<String, Object> checkImagePassword(String code, String password) {
        Map<String, Object> response = new HashMap<>();
        Integer siImageId = toDecodeId(code);
        Optional<ShareImgVO> imageOptional = shareImgJpa.findById(siImageId);

        if (imageOptional.isPresent()) {
            ShareImgVO image = imageOptional.get();
            String storedPassword = image.getSiPassword();
            if (storedPassword.equals(password)) {
                response.put("checkPassword", true);
                String token = jwtUtil.generateToken(code, "passwordPassed");
                response.put("token", token);
            } else {
                response.put("checkPassword", false);
            }
        } else {
            response.put("checkPassword", false);
        }
        return response;
    }
    
    public Map<String, Object> getAlbumImages(String token) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> claims = jwtUtil.validateToken(token);
        if (claims == null) {
            response.put("error", "非法或失效的令牌。 - Invalid or expired token.");
            return response;
        }
        String stage = (String) claims.get("stage");
        if (!"passwordPassed".equals(stage) && !"noPassword".equals(stage)) {
            response.put("error", "未授權的訪問，請重新載入頁面。 - Unauthorized access, please refresh your browser.");
            return response;
        }
        String code = (String) claims.get("subject");
        Integer siaImageId = toDecodeId(code);
        String redisKey = "albumImages:" + siaImageId;

        try {
            Map<String, Object> cachedResponse = getResponseFromRedis(redisKey);
            if (cachedResponse != null) {
                countAlbumUsage(siaImageId);

                Object rawImages = cachedResponse.get("images");
                List<Map<String, Object>> imageList = new ArrayList<>();

                if (rawImages instanceof List<?>) {
                    for (Object item : (List<?>) rawImages) {
                        if (item instanceof Map<?, ?>) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> imageMap = (Map<String, Object>) item;
                            imageList.add(imageMap);
                        }
                    }
                }

                for (Map<String, Object> imageMap : imageList) {
                    Integer siId = (Integer) imageMap.get("siId");
                    if (siId != null) {
                        dailyStatisticService.incrementImgUsed();
                        countImageUsage(siId);
                    }
                }

                return cachedResponse;
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse cached response for key: {}", redisKey, e);
        }

        Optional<ShareImgAlbumVO> albumOptional = shareImgAlbumJpa.findBySiaId(siaImageId);
        if (albumOptional.isEmpty()) {
            response.put("error", "Album not found.");
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

        response.put("images", imageList);

        try {
            saveResponseToRedis(redisKey, response);
        } catch (JsonProcessingException e) {
            logger.error("Failed to cache response for key: {}", redisKey, e);
        }
        countAlbumUsage(siaImageId);
        return response;
    }
    
    public Map<String, Object> getSingleImage(String token) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> claims = jwtUtil.validateToken(token);
        if (claims == null) {
            response.put("error", "非法或失效的令牌。 - Invalid or expired token.");
            return response;
        }
        String stage = (String) claims.get("stage");
        if (!"passwordPassed".equals(stage) && !"noPassword".equals(stage)) {
            response.put("error", "未授權的訪問，請重新載入頁面。 - Unauthorized access, please refresh your browser.");
            return response;
        }
        String code = (String) claims.get("subject");
        Integer siImageId = toDecodeId(code);
        String redisKey = "singleImage:" + siImageId;

        try {
            Map<String, Object> cachedResponse = getResponseFromRedis(redisKey);
            if (cachedResponse != null) {
                countImageUsage(siImageId);
                return cachedResponse;
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse cached response for key: {}", redisKey, e);
        }

        Optional<ShareImgVO> imageOptional = shareImgJpa.findById(siImageId);
        if (imageOptional.isEmpty()) {
            response.put("error", "Image not found.");
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

        try {
            saveResponseToRedis(redisKey, response);
        } catch (JsonProcessingException e) {
            logger.error("Failed to cache response for key: {}.", redisKey, e);
        }
        countImageUsage(siImageId);
        return response;
    }

    public boolean isValidShareImageAlbum(String code) {
        Integer siaId = ImgIdEncoderDecoderUtil.decodeImgId(code);
        return shareImgAlbumJpa.existsBySiaIdAndSiaStatusNot(siaId, (byte) 1);
    }

    public boolean isValidShareImage(String code) {
        Integer siaId = ImgIdEncoderDecoderUtil.decodeImgId(code);
        return shareImgJpa.existsBySiIdAndSiStatusNot(siaId, (byte) 1);
    }

    public void countAlbumUsage(Integer siaId) {
        String redisKey = "sia:usage:" + siaId;
        redisStringStringTemplate.opsForValue().increment(redisKey, 1);
    }

    public void countImageUsage(Integer siId) {
        String redisKey = "si:usage:" + siId;
        redisStringStringTemplate.opsForValue().increment(redisKey, 1);
    }

    @Scheduled(cron = "${task.schedule.cron.albumUsageStatisticService}")
    @Transactional
    public void syncSiaUsageToMySQL() {
        Set<String> keys = redisStringStringTemplate.keys("sia:usage:*");
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
        Set<String> keys = redisStringStringTemplate.keys("si:usage:*");

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

    private Integer toDecodeId(String encodeId) {
        return ImgIdEncoderDecoderUtil.decodeImgId(encodeId);
    }

    private void saveResponseToRedis(String key, Map<String, Object> response) throws JsonProcessingException {
        String jsonResponse = objectMapper.writeValueAsString(response);
        redisStringStringTemplate.opsForValue().set(key, jsonResponse, TTL_DURATION);
    }

    private Map<String, Object> getResponseFromRedis(String key) throws JsonProcessingException {
        String jsonResponse = redisStringStringTemplate.opsForValue().get(key);
        if (jsonResponse == null) return Collections.emptyMap();
        return objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
    }

}