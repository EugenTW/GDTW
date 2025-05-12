package com.gdtw.imgshare.model;

import com.gdtw.dailystatistic.model.DailyStatisticService;
import com.gdtw.general.util.codec.ImgFilenameEncoderDecoderUtil;
import com.gdtw.general.util.codec.ImgIdEncoderDecoderUtil;
import com.gdtw.general.exception.InsufficientDiskSpaceException;
import com.gdtw.general.util.jwt.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileStore;
import java.time.LocalDateTime;
import java.util.Map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
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

    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;
    private final JwtUtil jwtUtil;
    private final DailyStatisticService dailyStatisticService;
    private final RedisTemplate<String, String> redisStringStringTemplate;
    private final ObjectMapper objectMapper;

    public ImgShareService(ShareImgAlbumJpa shareImgAlbumJpa, ShareImgJpa shareImgJpa, JwtUtil jwtUtil, DailyStatisticService dailyStatisticService, @Qualifier("redisStringStringTemplate") RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
        this.jwtUtil = jwtUtil;
        this.dailyStatisticService = dailyStatisticService;
        this.redisStringStringTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // ==================================================================
    // Writing methods
    @Transactional
    public Map<String, String> createNewAlbumAndImage(AlbumCreationRequestDTO requestDTO) {
        long requiredSpace = parseDiskSpace(minDiskSpace);
        if (!hasSufficientDiskSpace(imageStoragePath, requiredSpace)) {
            logger.error("Insufficient disk space at path {}. At least {} is required.", imageStoragePath, minDiskSpace);
            throw new InsufficientDiskSpaceException("Insufficient disk space. The server has reached its capacity limit.");
        }

        ShareImgAlbumVO createdAlbumVO = createNewAlbumInMySQL(requestDTO);
        createNewImagesInMySQL(requestDTO, createdAlbumVO);
        Map<String, String> response = new HashMap<>();
        response.put("sia_code", createdAlbumVO.getSiaCode());
        return response;
    }

    @Transactional
    protected ShareImgAlbumVO createNewAlbumInMySQL(AlbumCreationRequestDTO requestDTO) {
        ShareImgAlbumVO shareImgAlbumVO = new ShareImgAlbumVO();
        shareImgAlbumVO.setSiaPassword(requestDTO.getPassword());
        shareImgAlbumVO.setSiaCreatedDate(LocalDateTime.now());
        shareImgAlbumVO.setSiaEndDate(LocalDate.now().plusDays(requestDTO.getExpiryDays()));
        shareImgAlbumVO.setSiaCreatedIp(requestDTO.getClientIp());
        shareImgAlbumVO.setSiaNsfw(requestDTO.isNsfw() ? (byte) 1 : (byte) 0);
        shareImgAlbumVO.setSiaStatus((byte) 0);

        ShareImgAlbumVO savedAlbum = shareImgAlbumJpa.saveAndFlush(shareImgAlbumVO);

        String encodedNewAlbumId = toEncodeId(savedAlbum.getSiaId());

        savedAlbum.setSiaCode(encodedNewAlbumId);
        shareImgAlbumJpa.save(savedAlbum);
        dailyStatisticService.incrementImgAlbumCreated();
        return savedAlbum;
    }

    @Transactional
    protected void createNewImagesInMySQL(AlbumCreationRequestDTO requestDTO, ShareImgAlbumVO createdAlbumVO) {
        List<MultipartFile> files = requestDTO.getFiles();
        List<Path> savedFilePaths = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                ShareImgVO shareImgVO = new ShareImgVO();
                shareImgVO.setSiPassword(createdAlbumVO.getSiaPassword());
                shareImgVO.setSiCreatedDate(createdAlbumVO.getSiaCreatedDate());
                shareImgVO.setSiCreatedIp(createdAlbumVO.getSiaCreatedIp());
                shareImgVO.setSiEndDate(createdAlbumVO.getSiaEndDate());
                shareImgVO.setSiNsfw(createdAlbumVO.getSiaNsfw());
                shareImgVO.setAlbum(createdAlbumVO);

                String originalFilename = file.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

                ShareImgVO savedImage = shareImgJpa.saveAndFlush(shareImgVO);
                String encodedSiId = toEncodeId(savedImage.getSiId());
                String encodedFilename = toEncodeFilename((savedImage.getSiId()));
                String newFilename = encodedFilename + fileExtension;

                savedImage.setSiCode(encodedSiId);
                savedImage.setSiName(newFilename);

                Path storageDirectory = Paths.get(imageStoragePath);
                Path filePath = storageDirectory.resolve(newFilename);

                Files.createDirectories(storageDirectory);
                file.transferTo(filePath.toFile());
                savedFilePaths.add(filePath);

                shareImgJpa.save(savedImage);
                dailyStatisticService.incrementImgCreated();
            }
        } catch (IOException e) {
            logger.error("Error saving files, rolling back transaction.", e);
            for (Path path : savedFilePaths) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    logger.error("Failed to delete file: {}.", path, ex);
                }
            }
            throw new RuntimeException("File saving failed.", e);
        }
    }

    // ==================================================================
    // Reading methods
    @Transactional(readOnly = true)
    public boolean isValidShareImageAlbum(String code) {
        Integer siaId = toDecodeId(code);
        return shareImgAlbumJpa.existsBySiaIdAndSiaStatusNot(siaId, (byte) 1);
    }

    @Transactional(readOnly = true)
    public boolean isValidShareImage(String code) {
        Integer siaId = toDecodeId(code);
        return shareImgJpa.existsBySiIdAndSiStatusNot(siaId, (byte) 1);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> isShareImageAlbumPasswordProtected(String code) {
        Map<String, Object> response = new HashMap<>();

        boolean isAlbumValid = isValidShareImageAlbum(code);
        response.put("isValid", isAlbumValid);

        if (!isAlbumValid) {
            return response;
        }

        Integer siaImageAlbumId = toDecodeId(code);
        Optional<ShareImgAlbumVO> albumOptional = shareImgAlbumJpa.findById(siaImageAlbumId);

        if (albumOptional.isPresent()) {
            ShareImgAlbumVO album = albumOptional.get();
            boolean requiresPassword = album.getSiaPassword() != null && !album.getSiaPassword().isEmpty();
            response.put("requiresPassword", requiresPassword);
            if (!requiresPassword) {
                String token = jwtUtil.generateToken(code, "noPassword");
                response.put("token", token);
            }
        } else {
            response.put("requiresPassword", false);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> isShareImagePasswordProtected(String code) {
        Map<String, Object> response = new HashMap<>();

        boolean isImageValid = isValidShareImage(code);
        response.put("isValid", isImageValid);

        if (!isImageValid) {
            return response;
        }

        Integer siImageId = toDecodeId(code);
        Optional<ShareImgVO> imageOptional = shareImgJpa.findById(siImageId);

        if (imageOptional.isPresent()) {
            ShareImgVO image = imageOptional.get();
            boolean requiresPassword = image.getSiPassword() != null && !image.getSiPassword().isEmpty();
            response.put("requiresPassword", requiresPassword);
            if (!requiresPassword) {
                String token = jwtUtil.generateToken(code, "noPassword");
                response.put("token", token);
            }
        } else {
            response.put("requiresPassword", false);
        }

        return response;
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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
                List<Map<String, Object>> imageList = (List<Map<String, Object>>) cachedResponse.get("images");
                if (imageList != null) {
                    for (Map<String, Object> imageMap : imageList) {
                        Integer siId = (Integer) imageMap.get("siId");
                        if (siId != null) {
                            dailyStatisticService.incrementImgUsed();
                            countImageUsage(siId);
                        }
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

    @Transactional(readOnly = true)
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

    // ==================================================================
    // Redis caching methods

    public void countAlbumUsage(Integer siaId) {
        String redisKey = "sia:usage:" + siaId; // prefix 'sia:usage:'
        redisStringStringTemplate.opsForValue().increment(redisKey, 1);
    }

    @Scheduled(cron = "${task.schedule.cron.albumUsageStatisticService}")
    @Transactional
    public void syncSiaUsageToMySQL() {
        Set<String> keys = redisStringStringTemplate.keys("sia:usage:*");
        if (keys != null) {
            for (String key : keys) {
                Integer siaId = Integer.parseInt(key.split(":")[2]);
                String value = redisStringStringTemplate.opsForValue().get(key);
                if (value == null) {
                    logger.warn("Missing Redis value for key '{}', skipping...", key);
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

        }
        logger.info("Sync 'Image Album' usage to MySQL!");
    }

    public void countImageUsage(Integer siId) {
        String redisKey = "si:usage:" + siId; // prefix 'si:usage:'
        redisStringStringTemplate.opsForValue().increment(redisKey, 1);
    }

    @Scheduled(cron = "${task.schedule.cron.imageUsageStatisticService}")
    @Transactional
    public void syncSiUsageToMySQL() {
        Set<String> keys = redisStringStringTemplate.keys("si:usage:*");
        if (keys != null) {
            for (String key : keys) {
                Integer siId = Integer.parseInt(key.split(":")[2]);
                Integer usageCount = Integer.parseInt(redisStringStringTemplate.opsForValue().get(key));

                Optional<ShareImgVO> optionalSiObject = shareImgJpa.findById(siId);
                if (optionalSiObject.isPresent()) {
                    ShareImgVO shareImgVO = optionalSiObject.get();
                    shareImgVO.setSiTotalVisited(shareImgVO.getSiTotalVisited() + usageCount);
                    shareImgJpa.save(shareImgVO);
                    // delete recorded data in Redis
                    redisStringStringTemplate.delete(key);
                }
            }
        }
        logger.info("Sync 'Single Image' usage to MySQL!");
    }

    // ==================================================================
    // Supporting methods

    public static String toEncodeId(Integer id) {
        return ImgIdEncoderDecoderUtil.encodeImgId(id);
    }

    public static Integer toDecodeId(String encodeId) {
        return ImgIdEncoderDecoderUtil.decodeImgId(encodeId);
    }

    public static String toEncodeFilename(Integer id) {
        return ImgFilenameEncoderDecoderUtil.encodeImgFilename(id);
    }

    public static Integer toDecodeFilename(String encodeFilename) {
        return ImgFilenameEncoderDecoderUtil.decodeImgFilename(encodeFilename);
    }

    private boolean hasSufficientDiskSpace(String path, long requiredSpace) {
        try {
            FileStore fileStore = Files.getFileStore(Paths.get(path));
            long usableSpace = fileStore.getUsableSpace();
            return usableSpace >= requiredSpace;
        } catch (IOException e) {
            logger.error("Failed to check disk space for path: {}.", path, e);
            return false;
        }
    }

    private long parseDiskSpace(String space) {
        try {
            if (space.endsWith("GB")) {
                return Long.parseLong(space.replace("GB", "").trim()) * 1024 * 1024 * 1024;
            } else if (space.endsWith("MB")) {
                return Long.parseLong(space.replace("MB", "").trim()) * 1024 * 1024;
            } else {
                throw new IllegalArgumentException("Invalid disk space format: " + space);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid disk space value: {}.", space, e);
            throw new IllegalArgumentException("Invalid disk space format: " + space);
        }
    }

    // Save Json to Redis
    public void saveResponseToRedis(String key, Map<String, Object> response) throws JsonProcessingException {
        String jsonResponse = objectMapper.writeValueAsString(response);
        redisStringStringTemplate.opsForValue().set(key, jsonResponse, TTL_DURATION);
    }

    // Reverse Redis to Json
    public Map<String, Object> getResponseFromRedis(String key) throws JsonProcessingException {
        String jsonResponse = redisStringStringTemplate.opsForValue().get(key);
        if (jsonResponse == null) return null;
        return objectMapper.readValue(jsonResponse, Map.class);
    }

}
