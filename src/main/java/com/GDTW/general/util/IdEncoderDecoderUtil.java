package com.GDTW.general.util;

import org.hashids.Hashids;

public class IdEncoderDecoderUtil {

    private static final String SALT = "short.url";
    private static final int MIN_LENGTH = 4;
    private static final int MIN_ID = 11000000;
    private static final int MAX_ID = 25000000;

    private static final Hashids HASHIDS = new Hashids(SALT, MIN_LENGTH);

    private IdEncoderDecoderUtil() {}

    public static String encodeId(Integer id) {
        if (id < MIN_ID || id > MAX_ID) {
            throw new IllegalArgumentException("ID should be between " + MIN_ID + " and " + MAX_ID + ".");
        }
        return HASHIDS.encode(id);
    }

    public static Integer decodeId(String encodedId) {
        long[] decodedIds = HASHIDS.decode(encodedId);
        if (decodedIds.length == 0) {
            throw new IllegalArgumentException("Invalid encodedId.");
        }
        int decodedId = (int) decodedIds[0];
        if (decodedId < MIN_ID || decodedId > MAX_ID) {
            throw new IllegalArgumentException("Decoded ID is out of valid range.");
        }
        return decodedId;
    }

}
