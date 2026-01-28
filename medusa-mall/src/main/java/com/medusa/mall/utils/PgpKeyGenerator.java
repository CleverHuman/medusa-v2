package com.medusa.mall.utils;

import org.pgpainless.PGPainless;
import org.pgpainless.algorithm.KeyFlag;
import org.pgpainless.key.generation.KeySpec;
import org.pgpainless.key.generation.type.KeyType;
import org.pgpainless.key.generation.type.rsa.RsaLength;
import org.pgpainless.key.info.KeyRingInfo;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * PGP密钥生成工具类
 * 
 * @author medusa
 * @date 2025-01-16
 */
public class PgpKeyGenerator {

    private static final Logger log = LoggerFactory.getLogger(PgpKeyGenerator.class);

    /**
     * 生成PGP密钥对
     * 
     * @param keyName 密钥名称
     * @param email 邮箱地址
     * @param keySize 密钥长度
     * @return 密钥对信息
     */
    public static PgpKeyPair generateKeyPair(String keyName, String email, int keySize) throws Exception {
        log.info("Starting PGP key pair generation: " + keyName + ", email: " + email + ", length: " + keySize);
        
        // Determine RSA key length
        RsaLength rsaLength = getRsaLength(keySize);
        
        // Generate key pair - use simplified API
        PGPSecretKeyRing secretKeyRing = PGPainless.generateKeyRing()
                .simpleRsaKeyRing(keyName + " <" + email + ">", rsaLength);
        
        // Get key information
        KeyRingInfo keyInfo = new KeyRingInfo(secretKeyRing);
        
        // Public key - use Base64 encoding
        PGPPublicKeyRing publicKeyRing = PGPainless.extractCertificate(secretKeyRing);
        ByteArrayOutputStream publicKeyStream = new ByteArrayOutputStream();
        publicKeyRing.encode(publicKeyStream);
        String publicKeyData = Base64.getEncoder().encodeToString(publicKeyStream.toByteArray());
        
        // Private key - use Base64 encoding
        ByteArrayOutputStream privateKeyStream = new ByteArrayOutputStream();
        secretKeyRing.encode(privateKeyStream);
        String privateKeyData = Base64.getEncoder().encodeToString(privateKeyStream.toByteArray());
        
        // Get main key ID and fingerprint
        String keyId = Long.toHexString(secretKeyRing.getSecretKey().getKeyID()).toUpperCase();
        String fingerprint = keyInfo.getFingerprint().toString().toUpperCase();
        
        log.info("Key generation completed - key ID: " + keyId + ", fingerprint: " + fingerprint);
        
        return new PgpKeyPair(
                keyName + " (Public)", "public", keyId, fingerprint, publicKeyData, keySize, "RSA",
                keyName + " (Private)", "private", keyId, fingerprint, privateKeyData, keySize, "RSA"
        );
    }
    
    /**
     * 根据密钥长度获取RSA长度枚举
     */
    private static RsaLength getRsaLength(int keySize) {
        switch (keySize) {
            case 1024:
                return RsaLength._1024;
            case 2048:
                return RsaLength._2048;
            case 3072:
                return RsaLength._3072;
            case 4096:
                return RsaLength._4096;
            default:
                return RsaLength._2048; // Default 2048 bits
        }
    }
    
    /**
     * 将Base64编码的密钥数据转换为标准PGP格式
     * 
     * @param base64KeyData Base64编码的密钥数据
     * @param isPrivate 是否为私钥
     * @return 标准PGP格式的密钥字符串
     */
    public static String convertBase64ToPgpFormat(String base64KeyData, boolean isPrivate) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64KeyData);
            String keyString = new String(keyBytes, StandardCharsets.UTF_8);
            
            // If already in standard PGP format, return directly
            if (keyString.contains("-----BEGIN PGP")) {
                return keyString;
            }
            
            // Otherwise construct standard PGP format
            StringBuilder pgpFormat = new StringBuilder();
            if (isPrivate) {
                pgpFormat.append("-----BEGIN PGP PRIVATE KEY BLOCK-----\n");
                pgpFormat.append("Version: BCPG v1.68\n\n");
            } else {
                pgpFormat.append("-----BEGIN PGP PUBLIC KEY BLOCK-----\n");
                pgpFormat.append("Version: BCPG v1.68\n\n");
            }
            
            // 将Base64数据按64字符一行进行格式化
            String base64Data = base64KeyData;
            for (int i = 0; i < base64Data.length(); i += 64) {
                int end = Math.min(i + 64, base64Data.length());
                pgpFormat.append(base64Data.substring(i, end)).append("\n");
            }
            
            if (isPrivate) {
                pgpFormat.append("-----END PGP PRIVATE KEY BLOCK-----");
            } else {
                pgpFormat.append("-----END PGP PUBLIC KEY BLOCK-----");
            }
            
            return pgpFormat.toString();
        } catch (Exception e) {
            log.error("Exception occurred while converting key format: " + e.getMessage());
            return base64KeyData; // If conversion fails, return original data
        }
    }
    
    /**
     * 将标准PGP格式字符串转换为Base64编码
     * 
     * @param pgpKeyData 标准PGP格式的密钥数据
     * @return Base64编码的密钥数据
     */
    public static String convertPgpFormatToBase64(String pgpKeyData) {
        try {
            // Remove PGP header and footer markers
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
            return pgpKeyData; // If conversion fails, return original data
        }
    }
    
    /**
     * PGP密钥对数据类
     */
    public static class PgpKeyPair {
        private final String publicKeyName;
        private final String publicKeyType;
        private final String publicKeyId;
        private final String publicFingerprint;
        private final String publicKeyData;
        private final int publicKeySize;
        private final String publicAlgorithm;
        
        private final String privateKeyName;
        private final String privateKeyType;
        private final String privateKeyId;
        private final String privateFingerprint;
        private final String privateKeyData;
        private final int privateKeySize;
        private final String privateAlgorithm;
        
        public PgpKeyPair(
                String publicKeyName, String publicKeyType, String publicKeyId, String publicFingerprint,
                String publicKeyData, int publicKeySize, String publicAlgorithm,
                String privateKeyName, String privateKeyType, String privateKeyId, String privateFingerprint,
                String privateKeyData, int privateKeySize, String privateAlgorithm) {
            this.publicKeyName = publicKeyName;
            this.publicKeyType = publicKeyType;
            this.publicKeyId = publicKeyId;
            this.publicFingerprint = publicFingerprint;
            this.publicKeyData = publicKeyData;
            this.publicKeySize = publicKeySize;
            this.publicAlgorithm = publicAlgorithm;
            this.privateKeyName = privateKeyName;
            this.privateKeyType = privateKeyType;
            this.privateKeyId = privateKeyId;
            this.privateFingerprint = privateFingerprint;
            this.privateKeyData = privateKeyData;
            this.privateKeySize = privateKeySize;
            this.privateAlgorithm = privateAlgorithm;
        }
        
        // Getters for public key
        public String getPublicKeyName() { return publicKeyName; }
        public String getPublicKeyType() { return publicKeyType; }
        public String getPublicKeyId() { return publicKeyId; }
        public String getPublicFingerprint() { return publicFingerprint; }
        public String getPublicKeyData() { return publicKeyData; }
        public int getPublicKeySize() { return publicKeySize; }
        public String getPublicAlgorithm() { return publicAlgorithm; }
        
        // Getters for private key
        public String getPrivateKeyName() { return privateKeyName; }
        public String getPrivateKeyType() { return privateKeyType; }
        public String getPrivateKeyId() { return privateKeyId; }
        public String getPrivateFingerprint() { return privateFingerprint; }
        public String getPrivateKeyData() { return privateKeyData; }
        public int getPrivateKeySize() { return privateKeySize; }
        public String getPrivateAlgorithm() { return privateAlgorithm; }
    }
} 