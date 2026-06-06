package com.pibank.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    @Value("${pibank.encryption.aes-key}")
    private String aesKey;

    public String encrypt(String plainText) {
        try {
            byte[] keyBytes = getKeyBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar datos", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

            byte[] keyBytes = getKeyBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar datos", e);
        }
    }

    private byte[] getKeyBytes() {
        byte[] rawKey = aesKey.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[32]; // 256 bits
        System.arraycopy(rawKey, 0, keyBytes, 0, Math.min(rawKey.length, 32));
        return keyBytes;
    }
}
