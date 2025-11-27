package com.example;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Library helper 
 */
public class Lib {

    /**
     * Decodes a Base64 encoded string and returns
     *  decoded string
     */
    public static String decode_from_base64(String encoded) throws IllegalArgumentException {
        if (encoded == null) {
            return "";
        }
        
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}