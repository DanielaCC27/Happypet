package com.uq.happypet.util;

import org.junit.jupiter.api.RepeatedTest;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class SecureTokenGeneratorTest {

    private static final Pattern URL_SAFE_BASE64 = Pattern.compile("^[A-Za-z0-9_-]+$");

    /**
     * 32 bytes sin padding en Base64 URL-safe suelen codificarse en 43 caracteres.
     */
    @RepeatedTest(5)
    void newToken_esBase64UrlSinPadding_yLongitudEsperada() {
        String token = SecureTokenGenerator.newToken();
        assertEquals(43, token.length());
        assertTrue(URL_SAFE_BASE64.matcher(token).matches());
        assertFalse(token.contains("="));
        Base64.getUrlDecoder().decode(token);
    }

    @RepeatedTest(3)
    void newToken_generaValoresDistintos() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            seen.add(SecureTokenGenerator.newToken());
        }
        assertTrue(seen.size() > 15, "expected few collisions across 20 tokens");
    }
}
