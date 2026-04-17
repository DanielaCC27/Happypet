package com.uq.happypet.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class SecureTokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private SecureTokenGenerator() {}

    public static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}