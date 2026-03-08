package br.com.supportchat.util;

import java.util.Base64;

public class CryptoUtils {

    private static final String KEY = "CHAT_SEGREDO_2026";

    public static String encrypt(String message) {
        byte[] messageBytes = message.getBytes();
        byte[] keyBytes = KEY.getBytes();

        byte[] encrypted = new byte[messageBytes.length];

        for (int i = 0; i < messageBytes.length; i++) {
            encrypted[i] = (byte) (messageBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedMessage) {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
        byte[] keyBytes = KEY.getBytes();

        byte[] decrypted = new byte[encryptedBytes.length];

        for (int i = 0; i < encryptedBytes.length; i++) {
            decrypted[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return new String(decrypted);
    }
}