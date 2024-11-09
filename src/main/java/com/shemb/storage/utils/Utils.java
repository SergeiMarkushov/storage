package com.shemb.storage.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

//@UtilityClass
public class Utils {
    public static String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String encodeFileName(String fileName, String uuid) {
        return Base64.getEncoder().encodeToString(Objects.requireNonNull(fileName)
                .concat("_").concat(uuid).getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeFileName(String fileName) {
        return parseFileName(new String(Base64.getDecoder().decode(fileName), StandardCharsets.UTF_8));
    }

    public static String parseFileName(String fileName) {
        int index = fileName.lastIndexOf("_");
        if (index != -1) {
            return fileName.substring(0, index);
        }
        return fileName;
    }

    public static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] keyToBytes(SecretKey key) {
        return key.getEncoded();
    }

    public static SecretKey bytesToKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }
}
