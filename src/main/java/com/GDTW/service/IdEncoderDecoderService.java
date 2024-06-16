package com.GDTW.service;

import org.springframework.stereotype.Service;

@Service
public class IdEncoderDecoderService {

    private static final String ENCRYPT_CHARS = "XgUweOlDKZlVAYNp4jq3FWMTQoEuP9I5nHbfCy2czv1mkdS60JxRGs78aBhLi";
    private static final int BASE = ENCRYPT_CHARS.length();
    private static final int MIN_ID = 10000000;
    private static final int MAX_ID = 23500000;

    private IdEncoderDecoderService(){}

    public static String encodeId(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should between " + MIN_ID + " and " + MAX_ID + ".");
        }
        int normalizedId = id - MIN_ID;
        char[] encodedChars = new char[4];
        for (int i = 3; i >= 0; i--) {
            encodedChars[i] = ENCRYPT_CHARS.charAt(normalizedId % BASE);
            normalizedId /= BASE;
        }
        return new String(encodedChars);
    }

    public static Integer decodeId(String encoded) {
        if (encoded.length() != 4) {
            throw new IllegalArgumentException("編碼字符串必須是4個字符長");
        }
        int id = 0;
        for (int i = 0; i < 4; i++) {
            id = id * BASE + ENCRYPT_CHARS.indexOf(encoded.charAt(i));
        }
        id += MIN_ID;
        return id;
    }
}
