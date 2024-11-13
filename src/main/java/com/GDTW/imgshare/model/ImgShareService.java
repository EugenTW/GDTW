package com.GDTW.imgshare.model;

import com.GDTW.general.service.ExtendedIdEncoderDecoderService;
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
    private final RedisTemplate<String, String> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ImgShareService.class);
    private static final Duration TTL_DURATION = Duration.ofHours(36);

    @Value("${app.imageStoragePath}")
    private String imageStoragePath;

    public ImgShareService(ShareImgAlbumJpa shareImgAlbumJpa, ShareImgJpa shareImgJpa, RedisTemplate<String, String> redisTemplate) {
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
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
        } else {
            response.put("requiresPassword", false);
        }
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
