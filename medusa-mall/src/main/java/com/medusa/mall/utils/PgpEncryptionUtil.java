package com.medusa.mall.utils;

import org.bouncycastle.openpgp.*;
import org.pgpainless.PGPainless;
import org.pgpainless.encryption_signing.ProducerOptions;
import org.pgpainless.decryption_verification.ConsumerOptions;
import org.pgpainless.encryption_signing.EncryptionOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * PGP encryption and decryption utility class
 * 
 * @author medusa
 * @date 2025-01-16
 */
public class PgpEncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(PgpEncryptionUtil.class);

    /**
     * Encrypt data using PGP public key
     * 
     * @param plaintext Plaintext data
     * @param publicKeyData Base64 encoded public key data
     * @return Base64 encoded encrypted data
     */
    public static String encrypt(String plaintext, String publicKeyData) throws Exception {
        log.debug("Starting PGP encryption, plaintext length: " + plaintext.length());
        
        try {
            // Decode public key data
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyData);
            PGPPublicKeyRing cert = PGPainless.readKeyRing().publicKeyRing(publicKeyBytes);
            
            // Create encryption output stream
            ByteArrayOutputStream cipherBuffer = new ByteArrayOutputStream();
            
            // Use PGPainless encryption API - use correct API call method
            try (OutputStream encOut = PGPainless.encryptAndOrSign()
                    .onOutputStream(cipherBuffer)
                    .withOptions(
                        ProducerOptions.encrypt(
                            new EncryptionOptions().addRecipient(cert)
                        ).setAsciiArmor(false)
                    )) { // Don't use ASCII armor, return binary directly
                
                encOut.write(plaintext.getBytes(StandardCharsets.UTF_8));
            }
            
            // Get encrypted data and convert to Base64
            byte[] encryptedBytes = cipherBuffer.toByteArray();
            String base64Encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
            
            log.debug("PGP encryption completed, encrypted data length: " + base64Encrypted.length());
            return base64Encrypted;
            
        } catch (Exception e) {
            log.error("PGP encryption failed: " + e.getMessage());
            throw new Exception("PGP encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypt data using PGP public key and return ASCII armor format
     * 
     * @param plaintext Plaintext data
     * @param publicKeyData PGP public key in ASCII armor format or Base64 encoded
     * @return PGP encrypted message in ASCII armor format
     */
    public static String encryptToAsciiArmor(String plaintext, String publicKeyData) throws Exception {
        log.info("Starting PGP encryption to ASCII armor, plaintext length: {}", plaintext.length());
        log.debug("Public key data length: {}, starts with: {}", 
                publicKeyData != null ? publicKeyData.length() : 0,
                publicKeyData != null && publicKeyData.length() > 50 ? publicKeyData.substring(0, 50) : publicKeyData);
        
        if (publicKeyData == null || publicKeyData.trim().isEmpty()) {
            throw new IllegalArgumentException("Public key data is null or empty");
        }
        
        try {
            byte[] publicKeyBytes;
            PGPPublicKeyRing cert;
            String trimmedKey = publicKeyData.trim();
            
            // Handle different public key formats
            if (trimmedKey.startsWith("-----BEGIN PGP PUBLIC KEY BLOCK-----")) {
                // Already in ASCII armor format
                log.info("Public key is in ASCII armor format");
                try {
                    cert = PGPainless.readKeyRing().publicKeyRing(trimmedKey);
                    log.info("Successfully parsed PGP public key from ASCII armor format");
                } catch (Exception e) {
                    log.error("Failed to parse PGP public key from ASCII armor: {}", e.getMessage(), e);
                    throw new Exception("Invalid PGP public key format (ASCII armor): " + e.getMessage(), e);
                }
            } else {
                // Try to decode as Base64
                log.info("Attempting to decode public key as Base64");
                try {
                    publicKeyBytes = Base64.getDecoder().decode(trimmedKey);
                    log.info("Base64 decode successful, key bytes length: {}", publicKeyBytes.length);
                    cert = PGPainless.readKeyRing().publicKeyRing(publicKeyBytes);
                    log.info("Successfully parsed PGP public key from Base64 format");
                } catch (IllegalArgumentException e) {
                    // Base64 decode failed, try treating as ASCII armor (might be missing header)
                    log.warn("Base64 decode failed ({}), trying to parse as ASCII armor format", e.getMessage());
                    try {
                        cert = PGPainless.readKeyRing().publicKeyRing(trimmedKey);
                        log.info("Successfully parsed PGP public key as ASCII armor (without header)");
                    } catch (Exception e2) {
                        log.error("Failed to parse PGP public key in any format. Base64 error: {}, ASCII armor error: {}", 
                                e.getMessage(), e2.getMessage());
                        throw new Exception("Invalid PGP public key format. Neither Base64 nor ASCII armor format is valid: " + e2.getMessage(), e2);
                    }
                } catch (Exception e) {
                    log.error("Failed to parse PGP public key from Base64: {}", e.getMessage(), e);
                    throw new Exception("Failed to parse PGP public key: " + e.getMessage(), e);
                }
            }
            
            // Create encryption output stream with ASCII armor
            ByteArrayOutputStream cipherBuffer = new ByteArrayOutputStream();
            
            log.info("Starting PGP encryption with ASCII armor enabled");
            // Use PGPainless encryption API with ASCII armor enabled
            try (OutputStream encOut = PGPainless.encryptAndOrSign()
                    .onOutputStream(cipherBuffer)
                    .withOptions(
                        ProducerOptions.encrypt(
                            new EncryptionOptions().addRecipient(cert)
                        ).setAsciiArmor(true)  // Enable ASCII armor format
                    )) {
                
                encOut.write(plaintext.getBytes(StandardCharsets.UTF_8));
            }
            
            // Get encrypted data as ASCII armor string
            String encryptedMessage = cipherBuffer.toString(StandardCharsets.UTF_8);
            
            log.info("PGP encryption to ASCII armor completed successfully, encrypted message length: {}", encryptedMessage.length());
            if (encryptedMessage.length() > 100) {
                log.debug("Encrypted message preview: {}", encryptedMessage.substring(0, 100) + "...");
            }
            
            return encryptedMessage;
            
        } catch (Exception e) {
            log.error("PGP encryption to ASCII armor failed: {}", e.getMessage(), e);
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage(), e.getCause());
            }
            throw new Exception("PGP encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt data using PGP private key
     * 
     * @param encryptedData PGP ASCII armor format or Base64 encoded encrypted data
     * @param privateKeyData Base64 encoded private key data
     * @return Decrypted plaintext data
     */
    public static String decrypt(String encryptedData, String privateKeyData) throws Exception {
        log.debug("Starting PGP decryption, encrypted data length: " + encryptedData.length());
        
        try {
            // Decode private key data
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyData);
            PGPSecretKeyRing secretRing = PGPainless.readKeyRing().secretKeyRing(privateKeyBytes);
            
            // Process encrypted data - support multiple formats
            byte[] encryptedBytes;
            String trimmedData = encryptedData.trim();
            
            // Add detailed debug information
            log.debug("=== PGP Decryption Format Detection ===");
            log.debug("Original data length: " + encryptedData.length());
            log.debug("Length after trimming: " + trimmedData.length());
            log.debug("Contains '-----': " + trimmedData.contains("-----"));
            log.debug("Contains '=': " + trimmedData.contains("="));
            log.debug("Starts with '-----BEGIN PGP MESSAGE-----': " + trimmedData.startsWith("-----BEGIN PGP MESSAGE-----"));
            log.debug("First 50 characters: " + (trimmedData.length() > 50 ? trimmedData.substring(0, 50) + "..." : trimmedData));
            
            if (trimmedData.startsWith("-----BEGIN PGP MESSAGE-----")) {
                // Complete PGP ASCII armor format
                log.debug("Detected complete PGP ASCII armor format");
                encryptedBytes = trimmedData.getBytes(StandardCharsets.UTF_8);
            } else if (trimmedData.contains("=") && !trimmedData.contains("-----")) {
                // Only Base64 data part (with checksum)
                log.debug("Detected Base64 data format (with checksum)");
                
                // First clean data, remove all whitespace characters
                String cleanData = trimmedData.replaceAll("\\s+", "");
                log.debug("Cleaned data length: " + cleanData.length());
                log.debug("First 50 characters after cleaning: " + (cleanData.length() > 50 ? cleanData.substring(0, 50) + "..." : cleanData));
                log.debug("Last 20 characters after cleaning: " + (cleanData.length() > 20 ? cleanData.substring(cleanData.length() - 20) : cleanData));
                
                // Check if data ends with =, which is Base64 checksum
                if (cleanData.endsWith("=")) {
                    log.debug("Data ends with =, this is Base64 checksum");
                } else {
                    log.debug("Data doesn't end with =, may have issues");
                }
                
                // Check if data length is multiple of 4 (Base64 requirement)
                int remainder = cleanData.length() % 4;
                if (remainder != 0) {
                    log.debug("Warning: Data length is not multiple of 4, need to pad. Current length: " + cleanData.length() + ", remainder: " + remainder);
                    // Pad to multiple of 4
                    while (cleanData.length() % 4 != 0) {
                        cleanData += "=";
                    }
                    log.debug("Padded data length: " + cleanData.length());
                }
                
                try {
                    log.debug("Attempting Base64 decoding...");
                    encryptedBytes = Base64.getDecoder().decode(cleanData);
                    log.debug("Base64 decoding successful, data length: " + encryptedBytes.length);
                } catch (IllegalArgumentException e) {
                    log.error("Base64 decoding failed: " + e.getMessage());
                    
                    // Try removing checksum part
                    log.debug("Attempting to remove checksum part...");
                    String dataWithoutChecksum = cleanData;
                    if (cleanData.contains("=")) {
                        int lastEqualsIndex = cleanData.lastIndexOf("=");
                        dataWithoutChecksum = cleanData.substring(0, lastEqualsIndex);
                        log.debug("Data length after removing checksum: " + dataWithoutChecksum.length());
                        log.debug("Last 20 characters after removing checksum: " + (dataWithoutChecksum.length() > 20 ? dataWithoutChecksum.substring(dataWithoutChecksum.length() - 20) : dataWithoutChecksum));
                    }
                    
                    try {
                        encryptedBytes = Base64.getDecoder().decode(dataWithoutChecksum);
                        log.debug("Base64 decoding successful after removing checksum, data length: " + encryptedBytes.length);
                    } catch (IllegalArgumentException e2) {
                        log.error("Base64 decoding still failed after removing checksum: " + e2.getMessage());
                        throw new Exception("Base64 decoding failed: " + e.getMessage() + ", still failed after removing checksum: " + e2.getMessage(), e2);
                    }
                }
            } else {
                // Pure Base64 format (without checksum)
                log.debug("Detected pure Base64 format");
                
                // First clean data, remove all whitespace characters
                String cleanData = trimmedData.replaceAll("\\s+", "");
                log.debug("Cleaned data length: " + cleanData.length());
                log.debug("First 50 characters after cleaning: " + (cleanData.length() > 50 ? cleanData.substring(0, 50) + "..." : cleanData));
                log.debug("Last 20 characters after cleaning: " + (cleanData.length() > 20 ? cleanData.substring(cleanData.length() - 20) : cleanData));
                
                // Check if data length is multiple of 4 (Base64 requirement)
                int remainder = cleanData.length() % 4;
                if (remainder != 0) {
                    log.debug("Warning: Data length is not multiple of 4, need to pad. Current length: " + cleanData.length() + ", remainder: " + remainder);
                    // Pad to multiple of 4
                    while (cleanData.length() % 4 != 0) {
                        cleanData += "=";
                    }
                    log.debug("Padded data length: " + cleanData.length());
                }
                
                try {
                    log.debug("Attempting Base64 decoding...");
                    encryptedBytes = Base64.getDecoder().decode(cleanData);
                    log.debug("Base64 decoding successful, data length: " + encryptedBytes.length);
                } catch (IllegalArgumentException e) {
                    log.error("Base64 decoding failed: " + e.getMessage());
                    throw new Exception("Base64 decoding failed: " + e.getMessage(), e);
                }
            }
            
            log.debug("=== Format detection completed ===");
            
            // Use PGPainless decryption API
            ByteArrayInputStream cipherIn = new ByteArrayInputStream(encryptedBytes);
            ByteArrayOutputStream plainBuffer = new ByteArrayOutputStream();
            
            ConsumerOptions decOpts = ConsumerOptions.get()
                    .addDecryptionKey(secretRing);
            
            try (InputStream dec = PGPainless.decryptAndOrVerify()
                    .onInputStream(cipherIn)
                    .withOptions(decOpts)) {
                
                // Read decrypted data
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = dec.read(buffer)) != -1) {
                    plainBuffer.write(buffer, 0, bytesRead);
                }
            }
            
            // Get decrypted plaintext
            String decryptedText = plainBuffer.toString(StandardCharsets.UTF_8);
            
            log.debug("PGP decryption completed, decrypted data length: " + decryptedText.length());
            return decryptedText;
            
        } catch (Exception e) {
            log.error("PGP decryption failed: " + e.getMessage());
            throw new Exception("PGP decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Convert Base64 encoded key data to standard PGP format
     * 
     * @param base64KeyData Base64 encoded key data
     * @param isPrivate Whether it's a private key
     * @return Standard PGP format key string
     */
    public static String convertBase64ToPgpFormat(String base64KeyData, boolean isPrivate) {
        try {
            // Decode Base64 data
            byte[] keyBytes = Base64.getDecoder().decode(base64KeyData);
            
            // Try to convert binary data to PGP keyring
            if (isPrivate) {
                // Handle private key
                PGPSecretKeyRing secretKeyRing = PGPainless.readKeyRing().secretKeyRing(keyBytes);
                return convertKeyRingToAsciiArmor(secretKeyRing, true);
            } else {
                // Handle public key
                PGPPublicKeyRing publicKeyRing = PGPainless.readKeyRing().publicKeyRing(keyBytes);
                return convertKeyRingToAsciiArmor(publicKeyRing, false);
            }
        } catch (Exception e) {
            log.error("Exception occurred while converting key format: " + e.getMessage());
            // If conversion fails, return original Base64 data
            return base64KeyData;
        }
    }

    /**
     * Convert PGP keyring to ASCII armor format
     */
    private static String convertKeyRingToAsciiArmor(PGPPublicKeyRing keyRing, boolean isPrivate) throws Exception {
        // Use PGPainless asciiArmor method for direct conversion
        return PGPainless.asciiArmor(keyRing);
    }

    /**
     * Convert PGP keyring to ASCII armor format
     */
    private static String convertKeyRingToAsciiArmor(PGPSecretKeyRing keyRing, boolean isPrivate) throws Exception {
        // Use PGPainless asciiArmor method for direct conversion
        return PGPainless.asciiArmor(keyRing);
    }

    /**
     * Convert standard PGP format string to Base64 encoding
     * 
     * @param pgpKeyData Standard PGP format key data
     * @return Base64 encoded key data
     */
    public static String convertPgpFormatToBase64(String pgpKeyData) {
        try {
            String[] lines = pgpKeyData.split("\n");
            StringBuilder base64Data = new StringBuilder();
            boolean inKeyData = false;
            for (String line : lines) {
                if (line.startsWith("-----BEGIN")) {
                    inKeyData = false;
                    continue;
                }
                if (line.startsWith("-----END")) {
                    break;
                }
                if (line.startsWith("Version:")) {
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (!inKeyData) {
                    inKeyData = true;
                }
                base64Data.append(line.trim());
            }
            return base64Data.toString();
        } catch (Exception e) {
            log.error("Exception occurred while converting key format: " + e.getMessage());
            return pgpKeyData;
        }
    }
} 