package com.GDTW.general.util;

import org.hashids.Hashids;

import java.security.SecureRandom;

public class ImgIdEncoderDecoderUtil {

    private static final String SALT = "img.id";
    private static final int MIN_LENGTH = 4;
    private static final int MIN_ID = 10000000;
    private static final int MAX_ID = 23500000;
    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_BASE = RANDOM_CHARS.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Hashids HASHIDS = new Hashids(SALT, MIN_LENGTH);

    private ImgIdEncoderDecoderUtil() {
    }

    public static String encodeImgId(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should be between " + MIN_ID + " and " + MAX_ID + ".");
        }

        String encodedId = HASHIDS.encode(id);

        return randomChar() + encodedId + randomChar();
    }

    public static Integer decodeImgId(String encodedImgId) {
        if (encodedImgId == null) {
            throw new IllegalArgumentException("The encoded string cannot be null.");
        }
        if (encodedImgId.length() != 6) {
            throw new IllegalArgumentException("The encoded string should have exactly 6 characters.");
        }

        String extractedId = encodedImgId.substring(1, 5);

        long[] decodedIds = HASHIDS.decode(extractedId);
        if (decodedIds.length == 0) {
            throw new IllegalArgumentException("Invalid encodedImgId.");
        }
        return (int) decodedIds[0];
    }

    private static char randomChar() {
        return RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_BASE));
    }
}
