package com.GDTW.general.util.codec;

import java.security.SecureRandom;

public class ImgIdEncoderDecoderUtil {

    private static final int MIN_ID = 11000000;
    private static final int MAX_ID = 25000000;
    private static final String ENCRYPT_CHARS_1 = "vt95fGqRexjN3iW6OKuMpwTAY1zc4BCydrQL7lksmhIHD0XbE8SZnagF2UPoJV";
    private static final String ENCRYPT_CHARS_2 = "hw4CQHXlBk3IAi1SGUEJ5ZRLNg6MrfozDqjPneFOuYytp970m8xTbvKVadW2cs";
    private static final String ENCRYPT_CHARS_3 = "YK4XVkfxbmpjW370oSz9wqLFPJOZN2er1CHvdTEuDAGtMlIhn5gscBUyaQi68R";
    private static final String ENCRYPT_CHARS_4 = "EBXfYupKRm3GOv1oQ7ZlyjgrDNMLq94F0VxAtW5dUJTHSnkICaiw628ePcsbzh";
    private static final int BASE = ENCRYPT_CHARS_1.length();
    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_BASE = RANDOM_CHARS.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    private ImgIdEncoderDecoderUtil() {
    }

    public static String encodeImgId(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should be between " + MIN_ID + " and " + MAX_ID + ".");
        }
        int normalizedId = id - MIN_ID;
        char[] encodedChars = new char[4];
        encodedChars[3] = ENCRYPT_CHARS_4.charAt(normalizedId % BASE);
        normalizedId /= BASE;
        encodedChars[2] = ENCRYPT_CHARS_3.charAt(normalizedId % BASE);
        normalizedId /= BASE;
        encodedChars[1] = ENCRYPT_CHARS_2.charAt(normalizedId % BASE);
        normalizedId /= BASE;
        encodedChars[0] = ENCRYPT_CHARS_1.charAt(normalizedId % BASE);
        return randomChar() + new String(encodedChars) + randomChar();
    }

    public static Integer decodeImgId(String encodedImgId) {
        if (encodedImgId == null) {
            throw new IllegalArgumentException("The encoded string cannot be null.");
        }
        if (encodedImgId.length() != 6) {
            throw new IllegalArgumentException("The encoded string should have exactly 6 characters.");
        }
        String encodedId = encodedImgId.substring(1, 5);
        int id = 0;
        id += ENCRYPT_CHARS_1.indexOf(encodedId.charAt(0)) * BASE * BASE * BASE;
        id += ENCRYPT_CHARS_2.indexOf(encodedId.charAt(1)) * BASE * BASE;
        id += ENCRYPT_CHARS_3.indexOf(encodedId.charAt(2)) * BASE;
        id += ENCRYPT_CHARS_4.indexOf(encodedId.charAt(3));
        return id + MIN_ID;
    }

    private static char randomChar() {
        return RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_BASE));
    }
}
