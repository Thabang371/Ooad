package com.bank.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hashes passwords with SHA-256 so plain-text passwords are never stored in the database. */
public final class PasswordUtil {

    private PasswordUtil() { }

    public static String hash(String plainText) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(plainText.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
