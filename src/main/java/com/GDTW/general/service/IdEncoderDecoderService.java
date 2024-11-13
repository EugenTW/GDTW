package com.GDTW.general.service;

import org.springframework.stereotype.Service;

@Service
public class IdEncoderDecoderService {

    public static final String ENCRYPT_CHARS_A = "XgUweOlDKZlVAYNp4jq3FWMTQoEuP9I5nHbfCy2czv1mkdS60JxRGs78aBhLi";
    public static final String ENCRYPT_CHARS_B = "TyVM7HzaSlXE4mnL8Kowu2hjgqdIliDFG6vpPQOA0cC9Z1sBU35fbkxeNRJYW";
    private static final int BASE_A = ENCRYPT_CHARS_A.length();
    private static final int BASE_B = ENCRYPT_CHARS_B.length();
    private static final int MIN_ID = 10000000;
    private static final int MAX_ID = 23500000;

    private IdEncoderDecoderService(){}

    public static String encodeId(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should be between " + MIN_ID + " and " + MAX_ID + ".");
        }
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

    public static Integer decodeId(String encodedId) {
        if (encodedId.length() != 4) {
            throw new IllegalArgumentException("The encodedId string should have exactly 4 characters.");
        }
        int id = 0;
        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0) {
                id = id * BASE_A + ENCRYPT_CHARS_A.indexOf(encodedId.charAt(i));
            } else {
                id = id * BASE_B + ENCRYPT_CHARS_B.indexOf(encodedId.charAt(i));
            }
        }
        id += MIN_ID;
        return id;
    }
}
