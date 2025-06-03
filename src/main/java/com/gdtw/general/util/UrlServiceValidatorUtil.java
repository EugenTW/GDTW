package com.gdtw.general.util;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class UrlServiceValidatorUtil {

    private static final int MAX_URL_LENGTH = 200;
    private static final Pattern SHORT_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4}$");
    private static final Set<String> RESERVED_CODES = Set.of("short_url_redirection");

    UrlServiceValidatorUtil() {}

    public static Optional<String> validateOriginalUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return Optional.of("輸入網址為空，請重新填寫！\nThe input URL is empty. Please try again!");
        }
        if (url.length() > MAX_URL_LENGTH) {
            return Optional.of("輸入網址過長，請縮短後再試！\nThe input URL is too long. Please shorten it and try again!");
        }
        if (!url.startsWith("https://")) {
            return Optional.of("本站只接受 HTTPS 網址，請重新填寫！\nOnly HTTPS URLs are accepted. Please try again!");
        }
        return Optional.empty();
    }

    public static Optional<String> validateShortCode(String code) {
        if (code == null || !SHORT_CODE_PATTERN.matcher(code).matches()) {
            return Optional.of("短碼格式錯誤! Invalid short code format!");
        }
        if (RESERVED_CODES.contains(code.toLowerCase())) {
            return Optional.of("無原始網址! No original URL!");
        }
        return Optional.empty();
    }

}
