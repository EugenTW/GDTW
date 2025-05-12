package com.gdtw.general.util.codec;

public class IdEncoderDecoderUtil {

    private static final int MIN_ID = 11000000;
    private static final int MAX_ID = 25000000;

    private static final String[] ENCRYPT_CHARS = {
            "RFLPKWABzeo1j87gEqN6Cxdhf5uIpMn4XOGmiayc3vTStJZwsHU92QbDkVr0lY",
            "jFfG6JUCbQ0ndu34zARo789BZYrpxDOaKgMylHcENIXL1VvWhe2qSwi5PstTkm",
            "ons1i3VMvHDBPxupOzZTFw2R6YKGtXlQm8A7CeyW0I5qadkUg4SLfchrEbjJN9",
            "DRiYvP3U0OsTcS1ZKGmWp5IbwuX4eHJBFl7h9x2AgaEdnqjQVN8oyCzkfM6Ltr"
    };

    private static final int BASE = ENCRYPT_CHARS[0].length();

    private IdEncoderDecoderUtil() {}

    public static String encodeId(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should be between " + MIN_ID + " and " + MAX_ID + ".");
        }

        int normalizedId = id - MIN_ID;
        char[] encodedChars = new char[ENCRYPT_CHARS.length];

        for (int i = ENCRYPT_CHARS.length - 1; i >= 0; i--) {
            encodedChars[i] = ENCRYPT_CHARS[i].charAt(normalizedId % BASE);
            normalizedId /= BASE;
        }

        return new String(encodedChars);
    }

    public static Integer decodeId(String encodedId) {
        if (encodedId == null || encodedId.length() != ENCRYPT_CHARS.length) {
            throw new IllegalArgumentException("Encoded ID must be exactly " + ENCRYPT_CHARS.length + " characters.");
        }

        int value = 0;
        for (int i = 0; i < ENCRYPT_CHARS.length; i++) {
            value = value * BASE + ENCRYPT_CHARS[i].indexOf(encodedId.charAt(i));
        }

        return value + MIN_ID;
    }

}

