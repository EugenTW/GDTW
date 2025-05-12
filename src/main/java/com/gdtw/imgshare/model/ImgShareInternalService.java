package com.gdtw.imgshare.model;

import com.gdtw.dailystatistic.model.DailyStatisticService;
import com.gdtw.general.exception.ImageStorageException;
import com.gdtw.general.util.codec.ImgFilenameEncoderDecoderUtil;
import com.gdtw.general.util.codec.ImgIdEncoderDecoderUtil;
import com.gdtw.general.util.jwt.JwtUtil;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ImgShareInternalService {

    private static final Logger logger = LoggerFactory.getLogger(ImgShareInternalService.class);

    private final ShareImgAlbumJpa shareImgAlbumJpa;
    private final ShareImgJpa shareImgJpa;
    private final DailyStatisticService dailyStatisticService;
    private final JwtUtil jwtUtil;

    @Value("${app.imageStoragePath}")
    private String imageStoragePath;

    public ImgShareInternalService(
            ShareImgAlbumJpa shareImgAlbumJpa,
            ShareImgJpa shareImgJpa,
            DailyStatisticService dailyStatisticService,
            JwtUtil jwtUtil
    ) {
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
        String encodedNewAlbumId = ImgIdEncoderDecoderUtil.encodeImgId(savedAlbum.getSiaId());

        savedAlbum.setSiaCode(encodedNewAlbumId);
        shareImgAlbumJpa.save(savedAlbum);
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
                String encodedSiId = ImgIdEncoderDecoderUtil.encodeImgId(savedImage.getSiId());
                String encodedFilename = ImgFilenameEncoderDecoderUtil.encodeImgFilename(savedImage.getSiId());
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
            String filenameInfo = !files.isEmpty() && files.getFirst() != null
                    ? files.getFirst().getOriginalFilename()
                    : "unknown";
            logger.error("Error saving image file '{}', rolling back transaction.", filenameInfo, e);
            for (Path path : savedFilePaths) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    logger.error("Failed to delete file: {}", path, ex);
                }
            }
            throw new ImageStorageException("Failed to save image file: " + filenameInfo, e);
        }
    }

    @Transactional
    public Map<String, Object> isShareImageAlbumPasswordProtected(String code) {
        Map<String, Object> response = new HashMap<>();
        boolean isAlbumValid = isValidShareImageAlbum(code);
        response.put("isValid", isAlbumValid);

        if (!isAlbumValid) return response;

        Integer siaImageAlbumId = ImgIdEncoderDecoderUtil.decodeImgId(code);
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

    @Transactional
    public Map<String, Object> isShareImagePasswordProtected(String code) {
        Map<String, Object> response = new HashMap<>();
        boolean isImageValid = isValidShareImage(code);
        response.put("isValid", isImageValid);

        if (!isImageValid) return response;

        Integer siImageId = ImgIdEncoderDecoderUtil.decodeImgId(code);
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

    public boolean isValidShareImageAlbum(String code) {
        Integer siaId = ImgIdEncoderDecoderUtil.decodeImgId(code);
        return shareImgAlbumJpa.existsBySiaIdAndSiaStatusNot(siaId, (byte) 1);
    }

    public boolean isValidShareImage(String code) {
        Integer siaId = ImgIdEncoderDecoderUtil.decodeImgId(code);
        return shareImgJpa.existsBySiIdAndSiStatusNot(siaId, (byte) 1);
    }

}