package com.gdtw.general.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class ImgServiceValidatorUtil {

    private static final Pattern DIGIT_PASSWORD_PATTERN = Pattern.compile("^\\d{4,10}$");
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024L;
    private static final long MAX_FILES_IN_PACKAGE = 50;
    private static final long MAX_TOTAL_SIZE = 500 * 1024 * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{6}$");

    private ImgServiceValidatorUtil() {}

    public static Optional<String> validatePassword(String password) {
        if (password == null || password.isEmpty()) return Optional.empty();
        if (!DIGIT_PASSWORD_PATTERN.matcher(password).matches()) {
            return Optional.of("Password must be 4~10 digits.");
        }
        return Optional.empty();
    }

    public static Optional<String> validateFiles(List<MultipartFile> files) {
        if (files.size() > MAX_FILES_IN_PACKAGE) {
            return Optional.of("一次最多僅能上傳50張圖片，請減少數量後再試。\nYou can upload up to 50 images at a time. Please reduce the number of files and try again.");
        }

        long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalSize > MAX_TOTAL_SIZE) {
            return Optional.of("單次上傳總容量超過限制，請縮小或分批上傳。\nThe total upload size exceeds the limit. Please reduce or split the files and upload again.");
        }

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                return Optional.of("單檔大小超過限制，請縮小檔案或分次上傳。\nThe file exceeds the maximum size limit. Please resize or split and upload again.");
            }

            String contentType = file.getContentType();
            if (contentType != null) {
                contentType = contentType.split(";")[0].toLowerCase().trim();
            }
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                return Optional.of("不支援的檔案格式，請僅上傳 JPG/JPEG/PNG/GIF/WEBP 格式的圖片。\nUnsupported file format. Please upload only JPG/JPEG/PNG/GIF/WEBP images.");
            }

            if (!isValidMagicNumber(file)) {
                return Optional.of("檔案格式驗證失敗，請上傳正確的圖片檔案。\nFile format validation failed. Please upload a valid image file.");
            }
        }

        return Optional.empty();
    }

    private static boolean isValidMagicNumber(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header);
            if (bytesRead < 4) return false;

            // JPEG
            if (header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF) {
                return true;
            }

            // PNG
            if (header[0] == (byte)0x89 && header[1] == (byte)0x50 && header[2] == (byte)0x4E && header[3] == (byte)0x47) {
                return true;
            }

            // GIF
            if (header[0] == (byte)0x47 && header[1] == (byte)0x49 && header[2] == (byte)0x46 && header[3] == (byte)0x38) {
                return true;
            }

            // WebP
            if (bytesRead >= 12 &&
                    header[0] == (byte)0x52 && header[1] == (byte)0x49 &&
                    header[2] == (byte)0x46 && header[3] == (byte)0x46 &&
                    header[8] == (byte)0x57 && header[9] == (byte)0x45 &&
                    header[10] == (byte)0x42 && header[11] == (byte)0x50) {
                return true;
            }

            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static Optional<String> validateShareCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.of("Code is required.");
        }
        if (!CODE_PATTERN.matcher(code).matches()) {
            return Optional.of("Invalid code format. Must be 6 characters: letters or digits.");
        }
        return Optional.empty();
    }

}


