package com.gdtw.imgshare.model;

import com.gdtw.dailystatistic.model.DailyStatisticService;
import com.gdtw.general.exception.ImageStorageException;
import com.gdtw.general.helper.RedisObjectCacheHelper;
import com.gdtw.general.util.CodecImgFilenameUtil;
import com.gdtw.general.util.CodecImgIdUtil;
import com.gdtw.general.helper.JwtHelper;
import com.gdtw.imgshare.dto.AlbumCreationRequestDTO;
import com.gdtw.imgshare.dto.ShareImgAlbumInfoDTO;
import com.gdtw.imgshare.dto.ShareImgInfoDTO;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ImgSharePersistenceService {

    public static final String ALBUM_INFO_CACHE_PREFIX = "sia:info:";
    public static final String IMAGE_INFO_CACHE_PREFIX = "si:info:";
    public static final String RESP_IS_VALID = "isValid";
    public static final String RESP_REQUIRES_PASSWORD = "requiresPassword";
    public static final String STAGE_CHECK_PASSWORD = "checkPassword";
    public static final String STAGE_NO_PASSWORD = "noPassword";
    public static final String STAGE_PASSED_PASSWORD = "passwordPassed";
    public static final String DOWNLOAD_TOKEN = "token";
    private static final Duration TTL_DURATION = Duration.ofMinutes(3);
    private static final Logger logger = LoggerFactory.getLogger(ImgSharePersistenceService.class);

    private final RedisObjectCacheHelper redisCacheUtil;
    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;
    private final DailyStatisticService dailyStatisticService;
    private final JwtHelper jwtUtil;

    @Value("${app.imageStoragePath}")
    private String imageStoragePath;

    public ImgSharePersistenceService(
            RedisObjectCacheHelper redisCacheUtil,
            ShareImgAlbumJpa shareImgAlbumJpa,
            ShareImgJpa shareImgJpa,
            DailyStatisticService dailyStatisticService,
            JwtHelper jwtUtil
    ) {
        this.redisCacheUtil = redisCacheUtil;
        this.shareImgAlbumJpa = shareImgAlbumJpa;
        this.shareImgJpa = shareImgJpa;
        this.dailyStatisticService = dailyStatisticService;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public ShareImgAlbumVO createNewAlbumInMySQL(AlbumCreationRequestDTO requestDTO) {
        ShareImgAlbumVO shareImgAlbumVO = new ShareImgAlbumVO();
        shareImgAlbumVO.setSiaPassword(requestDTO.getPassword());
        shareImgAlbumVO.setSiaCreatedDate(LocalDateTime.now());
        shareImgAlbumVO.setSiaEndDate(LocalDate.now().plusDays(requestDTO.getExpiryDays()));
        shareImgAlbumVO.setSiaCreatedIp(requestDTO.getClientIp());
        shareImgAlbumVO.setSiaNsfw(requestDTO.isNsfw() ? (byte) 1 : (byte) 0);
        shareImgAlbumVO.setSiaStatus((byte) 0);

        ShareImgAlbumVO savedAlbum = shareImgAlbumJpa.saveAndFlush(shareImgAlbumVO);
        String encodedNewAlbumId = CodecImgIdUtil.encodeImgId(savedAlbum.getSiaId());

        savedAlbum.setSiaCode(encodedNewAlbumId);
        shareImgAlbumJpa.save(savedAlbum);

        ShareImgAlbumInfoDTO dto = new ShareImgAlbumInfoDTO(
                savedAlbum.getSiaId(),
                encodedNewAlbumId,
                savedAlbum.getSiaPassword(),
                savedAlbum.getSiaEndDate(),
                savedAlbum.getSiaTotalVisited(),
                savedAlbum.getSiaStatus(),
                savedAlbum.getSiaNsfw()
        );

        String redisKey = ALBUM_INFO_CACHE_PREFIX + savedAlbum.getSiaId();
        redisCacheUtil.setObject(redisKey, dto, TTL_DURATION);

        dailyStatisticService.incrementImgAlbumCreated();
        return savedAlbum;
    }

    @Transactional
    public void createNewImagesInMySQL(AlbumCreationRequestDTO requestDTO, ShareImgAlbumVO createdAlbumVO) {
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
                if (originalFilename == null || !originalFilename.contains(".")) {
                    throw new IllegalArgumentException("Invalid file name: filename is null or has no extension.");
                }
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

                ShareImgVO savedImage = shareImgJpa.saveAndFlush(shareImgVO);

                String encodedSiId = CodecImgIdUtil.encodeImgId(savedImage.getSiId());
                String encodedFilename = CodecImgFilenameUtil.encodeImgFilename(savedImage.getSiId());
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
            String filenameInfo = files.stream()
                    .filter(Objects::nonNull)
                    .map(MultipartFile::getOriginalFilename)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("unknown");
            cleanupSavedFiles(savedFilePaths);
            throw new ImageStorageException("Failed to store image file: " + filenameInfo, e);
        }
    }

    public Map<String, Object> isShareImageAlbumPasswordProtected(String code) {
        Map<String, Object> response = new HashMap<>();
        Integer siaId = CodecImgIdUtil.decodeImgId(code);
        String redisKey = ALBUM_INFO_CACHE_PREFIX + siaId;

        Optional<ShareImgAlbumInfoDTO> dtoOpt = redisCacheUtil.getObject(redisKey, ShareImgAlbumInfoDTO.class);

        ShareImgAlbumInfoDTO dto = dtoOpt.orElseGet(() -> {
            Optional<ShareImgAlbumVO> optional = shareImgAlbumJpa.findById(siaId);
            if (optional.isEmpty() || optional.get().getSiaStatus() == 1) {
                response.put(RESP_IS_VALID, false);
                return null;
            }

            ShareImgAlbumVO album = optional.get();
            ShareImgAlbumInfoDTO generatedDto = new ShareImgAlbumInfoDTO(
                    album.getSiaId(),
                    album.getSiaCode(),
                    album.getSiaPassword(),
                    album.getSiaEndDate(),
                    album.getSiaTotalVisited(),
                    album.getSiaStatus(),
                    album.getSiaNsfw()
            );
            redisCacheUtil.setObject(redisKey, generatedDto, TTL_DURATION);
            return generatedDto;
        });

        if (dto == null) return response;

        boolean requiresPassword = dto.getSiaPassword() != null && !dto.getSiaPassword().isEmpty();
        response.put(RESP_IS_VALID, true);
        response.put(RESP_REQUIRES_PASSWORD, requiresPassword);

        if (!requiresPassword) {
            String token = jwtUtil.generateToken(code, STAGE_NO_PASSWORD);
            response.put(DOWNLOAD_TOKEN, token);
        }

        return response;
    }

    public Map<String, Object> isShareImagePasswordProtected(String code) {
        Map<String, Object> response = new HashMap<>();
        Integer siId = CodecImgIdUtil.decodeImgId(code);
        String redisKey = IMAGE_INFO_CACHE_PREFIX + siId;

        Optional<ShareImgInfoDTO> dtoOpt = redisCacheUtil.getObject(redisKey, ShareImgInfoDTO.class);

        ShareImgInfoDTO dto = dtoOpt.orElseGet(() -> {
            Optional<ShareImgVO> optional = shareImgJpa.findById(siId);
            if (optional.isEmpty() || optional.get().getSiStatus() == 1) {
                response.put(RESP_IS_VALID, false);
                return null;
            }

            ShareImgVO image = optional.get();
            ShareImgInfoDTO generatedDto = new ShareImgInfoDTO(
                    image.getSiId(),
                    image.getSiCode(),
                    image.getSiName(),
                    image.getSiPassword(),
                    image.getSiEndDate(),
                    image.getSiTotalVisited(),
                    image.getSiStatus(),
                    image.getSiNsfw(),
                    image.getAlbum() != null ? image.getAlbum().getSiaId() : null
            );
            redisCacheUtil.setObject(redisKey, generatedDto, TTL_DURATION);
            return generatedDto;
        });

        if (dto == null) return response;

        boolean requiresPassword = dto.getSiPassword() != null && !dto.getSiPassword().isEmpty();
        response.put(RESP_IS_VALID, true);
        response.put(RESP_REQUIRES_PASSWORD, requiresPassword);

        if (!requiresPassword) {
            String token = jwtUtil.generateToken(code, STAGE_NO_PASSWORD);
            response.put(DOWNLOAD_TOKEN, token);
        }

        return response;
    }

    private void cleanupSavedFiles(List<Path> savedFilePaths) {
        for (Path path : savedFilePaths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ex) {
                logger.error("Failed to delete file: {}", path, ex);
            }
        }
    }

}