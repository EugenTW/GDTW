package com.gdtw.general.util.codec;

import java.security.SecureRandom;

public class ImgFilenameEncoderDecoderUtil {

    private static final int MIN_ID = 11000000;
    private static final int MAX_ID = 25000000;

    private static final String[] ENCRYPT_CHARS = {
            "8vjFZCTRtdegQih2SzkumEBlGH6X09w3afbOJIcPnL7NUYxVqoDr5KAyMpW1s4",
            "3AL7mWhHvDiEV82yX0gNzko16TOpbsPRIje54fZtnlJSqYKGwQrucCxFa9BdMU",
            "nxY96GFJCbAsWf0ajLNoeDp3XPdI8uVZvS41mwERKhUy2rqtMHzOQ7kBi5lgcT",
            "djQAwI1DUgJHF8f3KL52qpXTZnxNhYMemiE7o69kVGvC0WacztrSuRybP4BlsO"
    };

    private static final int BASE = ENCRYPT_CHARS[0].length();

    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_BASE = RANDOM_CHARS.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int TOTAL_LENGTH = 8;
    private static final int ENCODED_LENGTH = ENCRYPT_CHARS.length;
    private static final int RANDOM_PADDING = (TOTAL_LENGTH - ENCODED_LENGTH) / 2;

    private ImgFilenameEncoderDecoderUtil() {}

    public static String encodeImgFilename(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should be between " + MIN_ID + " and " + MAX_ID + ".");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RANDOM_PADDING; i++) {
            sb.append(randomChar());
        }

        sb.append(encodeIdCore(id));

        for (int i = 0; i < RANDOM_PADDING; i++) {
            sb.append(randomChar());
        }

        return sb.toString();
    }

    public static Integer decodeImgFilename(String encodedImgFilename) {
        if (encodedImgFilename == null || encodedImgFilename.length() != TOTAL_LENGTH) {
            throw new IllegalArgumentException("The encoded string must be exactly " + TOTAL_LENGTH + " characters.");
        }

        String encodedPart = encodedImgFilename.substring(RANDOM_PADDING, RANDOM_PADDING + ENCODED_LENGTH);
        return decodeIdCore(encodedPart);
    }

    private static String encodeIdCore(Integer id) {
        int normalizedId = id - MIN_ID;
        char[] encodedChars = new char[ENCODED_LENGTH];

        for (int i = ENCODED_LENGTH - 1; i >= 0; i--) {
            encodedChars[i] = ENCRYPT_CHARS[i].charAt(normalizedId % BASE);
            normalizedId /= BASE;
        }

        return new String(encodedChars);
    }

    private static Integer decodeIdCore(String encodedId) {
        int value = 0;
        for (int i = 0; i < ENCODED_LENGTH; i++) {
            value = value * BASE + ENCRYPT_CHARS[i].indexOf(encodedId.charAt(i));
        }
        return value + MIN_ID;
    }

    private static char randomChar() {
        return RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_BASE));
    }

}