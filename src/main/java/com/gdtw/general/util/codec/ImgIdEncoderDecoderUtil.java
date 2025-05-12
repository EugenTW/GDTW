package com.gdtw.general.util.codec;

import java.security.SecureRandom;

public class ImgIdEncoderDecoderUtil {

    private static final int MIN_ID = 11000000;
    private static final int MAX_ID = 25000000;

    private static final String[] ENCRYPT_CHARS = {
            "vt95fGqRexjN3iW6OKuMpwTAY1zc4BCydrQL7lksmhIHD0XbE8SZnagF2UPoJV",
            "hw4CQHXlBk3IAi1SGUEJ5ZRLNg6MrfozDqjPneFOuYytp970m8xTbvKVadW2cs",
            "YK4XVkfxbmpjW370oSz9wqLFPJOZN2er1CHvdTEuDAGtMlIhn5gscBUyaQi68R",
            "EBXfYupKRm3GOv1oQ7ZlyjgrDNMLq94F0VxAtW5dUJTHSnkICaiw628ePcsbzh"
    };

    private static final int BASE = ENCRYPT_CHARS[0].length();
    private static final int ENCODED_LENGTH = ENCRYPT_CHARS.length;
    private static final int TOTAL_LENGTH = ENCODED_LENGTH + 2;
    private static final int RANDOM_PADDING = 1;

    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_BASE = RANDOM_CHARS.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    private ImgIdEncoderDecoderUtil() {}

    public static String encodeImgId(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should be between " + MIN_ID + " and " + MAX_ID + ".");
        }
        return randomChar() + encodeIdCore(id) + randomChar();
    }

    public static Integer decodeImgId(String encodedImgId) {
        if (encodedImgId == null || encodedImgId.length() != TOTAL_LENGTH) {
            throw new IllegalArgumentException("The encoded string must be exactly " + TOTAL_LENGTH + " characters.");
        }

        String encodedCore = encodedImgId.substring(RANDOM_PADDING, RANDOM_PADDING + ENCODED_LENGTH);
        return decodeIdCore(encodedCore);
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
