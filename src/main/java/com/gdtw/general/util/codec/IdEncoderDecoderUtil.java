package com.gdtw.general.util.codec;

public class IdEncoderDecoderUtil {

    private static final int MIN_ID = 11000000;
    private static final int MAX_ID = 25000000;

    private static final String ENCRYPT_CHARS_1 = "RFLPKWABzeo1j87gEqN6Cxdhf5uIpMn4XOGmiayc3vTStJZwsHU92QbDkVr0lY";
    private static final String ENCRYPT_CHARS_2 = "jFfG6JUCbQ0ndu34zARo789BZYrpxDOaKgMylHcENIXL1VvWhe2qSwi5PstTkm";
    private static final String ENCRYPT_CHARS_3 = "ons1i3VMvHDBPxupOzZTFw2R6YKGtXlQm8A7CeyW0I5qadkUg4SLfchrEbjJN9";
    private static final String ENCRYPT_CHARS_4 = "DRiYvP3U0OsTcS1ZKGmWp5IbwuX4eHJBFl7h9x2AgaEdnqjQVN8oyCzkfM6Ltr";

    private static final int BASE = ENCRYPT_CHARS_1.length();

    private IdEncoderDecoderUtil() {}

    public static String encodeId(Integer id) {
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

        return new String(encodedChars);
    }

    public static Integer decodeId(String encodedId) {
        if (encodedId == null || encodedId.length() != 4) {
            throw new IllegalArgumentException("Encoded ID must be exactly 4 characters.");
        }

        int id = 0;

        id += ENCRYPT_CHARS_1.indexOf(encodedId.charAt(0)) * BASE * BASE * BASE;
        id += ENCRYPT_CHARS_2.indexOf(encodedId.charAt(1)) * BASE * BASE;
        id += ENCRYPT_CHARS_3.indexOf(encodedId.charAt(2)) * BASE;
        id += ENCRYPT_CHARS_4.indexOf(encodedId.charAt(3));

        return id + MIN_ID;
    }


}
