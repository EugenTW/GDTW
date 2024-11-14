package com.GDTW.imgshare.model;

import com.GDTW.dailystatistic.model.DailyStatisticService;
import com.GDTW.general.service.ExtendedIdEncoderDecoderService;
import com.GDTW.general.service.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
public class ImgShareService {

    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;
    private final DailyStatisticService dailyStatisticService;
    private final RedisTemplate<String, String> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ImgShareService.class);
    private static final Duration TTL_DURATION = Duration.ofHours(36);

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Value("${app.imageStoragePath}")
    private String imageStoragePath;

    public ImgShareService(ShareImgAlbumJpa shareImgAlbumJpa, ShareImgJpa shareImgJpa, DailyStatisticService dailyStatisticService, RedisTemplate<String, String> redisTemplate) {
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
        this.dailyStatisticService = dailyStatisticService;
        this.redisTemplate = redisTemplate;
    }

    // ==================================================================
    // Writing methods
    @Transactional
    public Map<String, String> createNewAlbumAndImage(AlbumCreationRequestDTO requestDTO) {
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
        shareImgAlbumVO.setSiaCreatedDate(LocalDate.now());
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

                String newFileName = encodedSiId + fileExtension;
                savedImage.setSiCode(encodedSiId);
                savedImage.setSiName(newFileName);

                Path storageDirectory = Paths.get(imageStoragePath);
                Path filePath = storageDirectory.resolve(newFileName);

                Files.createDirectories(storageDirectory);
                file.transferTo(filePath.toFile());
                savedFilePaths.add(filePath);

                shareImgJpa.save(savedImage);
                dailyStatisticService.incrementImgCreated();
            }
        } catch (IOException e) {
            logger.error("Error saving files, rolling back transaction", e);
            for (Path path : savedFilePaths) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    logger.error("Failed to delete file: " + path, ex);
                }
            }
            throw new RuntimeException("File saving failed", e);
        }
    }

    // ==================================================================
    // Reading methods
    @Transactional(readOnly = true)
    public boolean isShareImageAlbumCodeValid(String code) {
        Integer siaImageAlbumId = toDecodeId(code);
        return shareImgAlbumJpa.existsBySiaIdAndSiaStatusNot(siaImageAlbumId, (byte) 1);
    }

    @Transactional(readOnly = true)
    public boolean isShareImageCodeValid(String code) {
        Integer siImageId = toDecodeId(code);
        return shareImgJpa.existsBySiIdAndSiStatusNot(siImageId, (byte) 1);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> isShareImageAlbumPasswordProtected(String code) {
        Map<String, Object> response = new HashMap<>();

        boolean isAlbumValid = isShareImageAlbumCodeValid(code);
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
                String token = JwtUtil.generateToken(code, "noPassword");
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

        boolean isImageValid = isShareImageCodeValid(code);
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
                String token = JwtUtil.generateToken(code, "noPassword");
                response.put("token", token);
            }
        } else {
            response.put("requiresPassword", false);
        }
        return response;
    }


    @Transactional(readOnly = true)
    public Map<String, Object> checkAlbumPassword(String code, String password){
        Map<String, Object> response = new HashMap<>();

        Integer siaImageAlbumId = toDecodeId(code);
        Optional<ShareImgAlbumVO> albumOptional = shareImgAlbumJpa.findById(siaImageAlbumId);

        if (albumOptional.isPresent()) {
            ShareImgAlbumVO album = albumOptional.get();
            String storedPassword = album.getSiaPassword();
            if (storedPassword.equals(password)) {
                response.put("checkPassword", true);
                String token = JwtUtil.generateToken(code, "passwordPassed");
                response.put("token", token);
            }else {
                response.put("checkPassword", false);
            }
        } else {
            response.put("checkPassword", false);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> checkImagePassword(String code, String password){
        Map<String, Object> response = new HashMap<>();

        Integer siImageId = toDecodeId(code);
        Optional<ShareImgVO> imageOptional = shareImgJpa.findById(siImageId);

        if (imageOptional.isPresent()) {
            ShareImgVO image = imageOptional.get();
            String storedPassword = image.getSiPassword();
            if (storedPassword.equals(password)) {
                response.put("checkPassword", true);
                String token = JwtUtil.generateToken(code, "passwordPassed");
                response.put("token", token);
            }else {
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

        Claims claims = JwtUtil.validateToken(token);
        if (claims == null) {
            response.put("error", "Invalid or expired token.");
            return response;
        }

        String code = claims.getSubject();
        Integer siaImageId = toDecodeId(code);

        Optional<ShareImgAlbumVO> albumOptional = shareImgAlbumJpa.findBySiaId(siaImageId);
        if (!albumOptional.isPresent()) {
            response.put("error", "Album not found.");
            return response;
        }

        ShareImgAlbumVO album = albumOptional.get();
        response.put("siaNsfw", album.getSiaNsfw());

        List<ShareImgVO> images = shareImgJpa.findByAlbum_SiaIdOrderBySiIdAsc(siaImageId);
        List<Map<String, Object>> imageList = new ArrayList<>();

        for (ShareImgVO image : images) {
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("siId", image.getSiId());
            imageMap.put("siCode", image.getSiCode());
            imageMap.put("siName", image.getSiName());

            String imageUrl = baseUrl + "/images/" + image.getSiName();
            imageMap.put("imageUrl", imageUrl);

            imageList.add(imageMap);
        }

        response.put("images", imageList);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSingleImage(String token) {
        Map<String, Object> response = new HashMap<>();

        Claims claims = JwtUtil.validateToken(token);
        if (claims == null) {
            response.put("error", "Invalid or expired token.");
            return response;
        }

        String code = claims.getSubject();
        Integer siImageId = toDecodeId(code);

        Optional<ShareImgVO> imageOptional = shareImgJpa.findById(siImageId);
        if (!imageOptional.isPresent()) {
            response.put("error", "Image not found.");
            return response;
        }

        ShareImgVO image = imageOptional.get();
        response.put("siId", image.getSiId());
        response.put("siCode", image.getSiCode());
        response.put("siName", image.getSiName());
        response.put("siNsfw", image.getSiNsfw());

        String imageUrl = baseUrl + "/images/" + image.getSiName();
        response.put("imageUrl", imageUrl);

        return response;
    }




    // ==================================================================
    // Redis caching methods

    // ==================================================================
    // Supporting methods

    public static String toEncodeId(Integer id) {
        return ExtendedIdEncoderDecoderService.encodeExtendedId(id);
    }

    public static Integer toDecodeId(String encodeId) {
        return ExtendedIdEncoderDecoderService.decodeExtendedId(encodeId);
    }

}