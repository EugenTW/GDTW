package com.GDTW.general.util;

import org.hashids.Hashids;

import java.security.SecureRandom;

public class ImgFilenameEncoderDecoderUtil {

    private static final String SALT = "img.file.name";
    private static final int MIN_LENGTH = 4;
    private static final int MIN_ID = 11000000;
    private static final int MAX_ID = 25000000;
    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_BASE = RANDOM_CHARS.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Hashids HASHIDS = new Hashids(SALT, MIN_LENGTH);

    private ImgFilenameEncoderDecoderUtil() {}

    public static String encodeImgFilename(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should be between " + MIN_ID + " and " + MAX_ID + ".");
        }

        String encodedId = HASHIDS.encode(id);

        return randomChar() + randomChar() + encodedId + randomChar() + randomChar();
    }

    public static Integer decodeImgFilename(String encodedImgFilename) {
        if (encodedImgFilename == null) {
            throw new IllegalArgumentException("The encoded string cannot be null.");
        }
        if (encodedImgFilename.length() != 8) {
            throw new IllegalArgumentException("The encoded string should have exactly 8 characters.");
        }

        String extractedId = encodedImgFilename.substring(2, 6);

        long[] decodedIds = HASHIDS.decode(extractedId);
        if (decodedIds.length == 0) {
            throw new IllegalArgumentException("Invalid encodedImgFilename.");
        }
        return (int) decodedIds[0];
    }

    private static char randomChar() {
        return RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_BASE));
    }
}
