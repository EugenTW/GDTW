package com.GDTW.general.util;

import java.security.SecureRandom;

public class ImgFilenameEncoderDecoderUtil {

    public static final String ENCRYPT_CHARS_A = "q3FWMTQoEuP9I5nHbfCy2czAYNs78aBp4jv1mkdS6XgUweOlDKZlV0JxRGhLi";
    public static final String ENCRYPT_CHARS_B = "5fbkxeNTyVM7mnL8Kowu2hjgqdIliDFG6vpPQOA0cCHzaSlXE49Z1sBU3RJYW";
    private static final int BASE_A = ENCRYPT_CHARS_A.length();
    private static final int BASE_B = ENCRYPT_CHARS_B.length();
    private static final int MIN_ID = 10000000;
    private static final int MAX_ID = 23500000;
    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_BASE = RANDOM_CHARS.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    private ImgFilenameEncoderDecoderUtil() {
    }

    public static String encodeImgFilename(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should be between " + MIN_ID + " and " + MAX_ID + ".");
        }
        String encodedId = encodeBase(id);
        return insertRandomCharacters(encodedId);
    }

    public static Integer decodeImgFilename(String encodedImgFilename) {
        if (encodedImgFilename == null) {
            throw new IllegalArgumentException("The encoded string cannot be null.");
        }
        if (encodedImgFilename.length() != 12) {
            throw new IllegalArgumentException("The encoded string should have exactly 12 characters.");
        }
        String extractedId = extractEncodedId(encodedImgFilename);
        return decodeBase(extractedId);
    }

    private static String encodeBase(int id) {
        int normalizedId = id - MIN_ID;
        char[] encodedChars = new char[4];
        for (int i = 3; i >= 0; i--) {
            if (i % 2 == 0) {
                encodedChars[i] = ENCRYPT_CHARS_A.charAt(normalizedId % BASE_A);
            } else {
                encodedChars[i] = ENCRYPT_CHARS_B.charAt(normalizedId % BASE_B);
            }
            normalizedId /= (i % 2 == 0) ? BASE_A : BASE_B;
        }
        return new String(encodedChars);
    }

    private static Integer decodeBase(String encoded) {
        int id = 0;
        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0) {
                id = id * BASE_A + ENCRYPT_CHARS_A.indexOf(encoded.charAt(i));
            } else {
                id = id * BASE_B + ENCRYPT_CHARS_B.indexOf(encoded.charAt(i));
            }
        }
        return id + MIN_ID;
    }

    private static String insertRandomCharacters(String encodedId) {
        StringBuilder result = new StringBuilder(12);
        result.append(randomChar()).append(randomChar())
                .append(encodedId.charAt(0))
                .append(randomChar()).append(encodedId.charAt(1))
                .append(randomChar()).append(randomChar())
                .append(encodedId.charAt(2))
                .append(randomChar()).append(encodedId.charAt(3))
                .append(randomChar()).append(randomChar());
        return result.toString();
    }

    private static String extractEncodedId(String encoded) {
        return "" + encoded.charAt(2) + encoded.charAt(4) + encoded.charAt(7) + encoded.charAt(9);
    }

    private static char randomChar() {
        return RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_BASE));
    }

}
