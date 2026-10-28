package com.simul_tech.netgenius.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** Отдельный ключ AES-GCM, производный от секрета приложения; токены не хранятся открыто. */
@Component
public class MailTokenCipher {
    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public MailTokenCipher(@Value("${app.jwt.secret}") String secret) {
        try {
            Mac kdf = Mac.getInstance("HmacSHA256");
            kdf.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            key = new SecretKeySpec(kdf.doFinal("netgenius/mail-outbox/v1".getBytes(StandardCharsets.UTF_8)), "AES");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot initialize mail encryption", e);
        }
    }

    public String encrypt(String token, String context) {
        byte[] nonce = new byte[12];
        random.nextBytes(nonce);
        try {
            Cipher cipher = cipher(Cipher.ENCRYPT_MODE, nonce, context);
            byte[] encrypted = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            byte[] result = Arrays.copyOf(nonce, nonce.length + encrypted.length);
            System.arraycopy(encrypted, 0, result, nonce.length, encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot encrypt mail token", e);
        }
    }

    public String decrypt(String encrypted, String context) {
        byte[] bytes = Base64.getDecoder().decode(encrypted);
        if (bytes.length < 28) throw new IllegalArgumentException("Invalid encrypted mail token");
        try {
            return new String(cipher(Cipher.DECRYPT_MODE, Arrays.copyOf(bytes, 12), context)
                    .doFinal(Arrays.copyOfRange(bytes, 12, bytes.length)), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot decrypt mail token", e);
        }
    }

    private Cipher cipher(int mode, byte[] nonce, String context) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, key, new GCMParameterSpec(128, nonce));
        cipher.updateAAD(context.getBytes(StandardCharsets.UTF_8));
        return cipher;
    }
}
