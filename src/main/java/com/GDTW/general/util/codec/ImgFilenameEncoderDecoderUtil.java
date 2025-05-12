package com.GDTW.general.util.codec;

import java.security.SecureRandom;

public class ImgFilenameEncoderDecoderUtil {

    private static final int MIN_ID = 11000000;
    private static final int MAX_ID = 25000000;

    private static final String ENCRYPT_CHARS_1 = "8vjFZCTRtdegQih2SzkumEBlGH6X09w3afbOJIcPnL7NUYxVqoDr5KAyMpW1s4";
    private static final String ENCRYPT_CHARS_2 = "3AL7mWhHvDiEV82yX0gNzko16TOpbsPRIje54fZtnlJSqYKGwQrucCxFa9BdMU";
    private static final String ENCRYPT_CHARS_3 = "nxY96GFJCbAsWf0ajLNoeDp3XPdI8uVZvS41mwERKhUy2rqtMHzOQ7kBi5lgcT";
    private static final String ENCRYPT_CHARS_4 = "djQAwI1DUgJHF8f3KL52qpXTZnxNhYMemiE7o69kVGvC0WacztrSuRybP4BlsO";
    private static final int BASE = ENCRYPT_CHARS_1.length();

    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_BASE = RANDOM_CHARS.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    private ImgFilenameEncoderDecoderUtil() {}

    public static String encodeImgFilename(Integer id) {
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

        return randomChar() + randomChar() + new String(encodedChars) + randomChar() + randomChar();
    }

    public static Integer decodeImgFilename(String encodedImgFilename) {
        if (encodedImgFilename == null) {
            throw new IllegalArgumentException("The encoded string cannot be null.");
        }
        if (encodedImgFilename.length() != 8) {
            throw new IllegalArgumentException("The encoded string should have exactly 8 characters.");
        }

        String encodedId = encodedImgFilename.substring(2, 6);

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
