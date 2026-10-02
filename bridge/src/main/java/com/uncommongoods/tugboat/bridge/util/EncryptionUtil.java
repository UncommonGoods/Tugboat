// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.prefs.Preferences;

/**
 * Utility class for encrypting and decrypting sensitive settings data.
 * Uses AES encryption with a generated key stored in system preferences.
 */
public class EncryptionUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final String KEY_PREF = "tugboat.encryption.key";
    
    private static SecretKey getOrCreateKey() {
        Preferences prefs = Preferences.userNodeForPackage(EncryptionUtil.class);
        String keyString = prefs.get(KEY_PREF, null);
        
        if (keyString == null) {
            // Generate new key
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
                keyGen.init(256);
                SecretKey key = keyGen.generateKey();
                
                // Store the key
                String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
                prefs.put(KEY_PREF, encodedKey);
                
                return key;
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate encryption key", e);
            }
        } else {
            // Load existing key
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            return new SecretKeySpec(keyBytes, ALGORITHM);
        }
    }
    
    /**
     * Encrypts a string value.
     * @param plainText The text to encrypt
     * @return Base64 encoded encrypted text, or empty string if input is null/empty
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return "";
        }
        
        try {
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.err.println("Encryption failed: " + e.getMessage());
            return ""; // Return empty string instead of plain text on failure
        }
    }
    
    /**
     * Decrypts a string value.
     * @param encryptedText Base64 encoded encrypted text
     * @return Decrypted plain text, or empty string if input is null/empty
     */
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return "";
        }
        
        try {
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            // Re-throw the exception so calling code can handle it
            throw new RuntimeException("Decryption failed", e);
        }
    }
}