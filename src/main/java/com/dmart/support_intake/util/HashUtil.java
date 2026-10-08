package com.dmart.support_intake.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashUtil {

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                String hexValue = Integer.toHexString(0xff & b);
                if (hexValue.length() == 1) {
                    hex.append('0');
                }
                hex.append(hexValue);
            }

            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate SHA-256 hash", e);
        }
    }
}