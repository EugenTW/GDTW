package com.GDTW.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdEncoderDecoderServiceTest {

    @Test
    public void testValidEncodingAndDecoding() {
        int[] validIds = {10000000, 12000000, 15000000, 18000000, 20000000, 23500000};
        for (int id : validIds) {
            System.out.println("ID: " + id);
            String encoded = IdEncoderDecoderService.encodeId(id);
            System.out.println("Encoded Id: " + encoded);
            int decoded = IdEncoderDecoderService.decodeId(encoded);
            System.out.println("Decoded Id: " + decoded);
            Assertions.assertEquals(id, decoded, "ID：" + id + ", encoding/decoding failed.");
        }
    }
}
