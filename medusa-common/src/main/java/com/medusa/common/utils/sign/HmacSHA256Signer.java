package com.medusa.common.utils.sign;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HmacSHA256Signer {

    public static String sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Unable to calculate signature", e);
        }
    }
    
    public static String signBytes(byte[] data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data);
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Unable to calculate signature", e);
        }
    }
    
    public static String signBytes(byte[] data, String secret, String charset) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(charset), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data);
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Unable to calculate signature", e);
        }
    }
    
    public static String signBase64(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Unable to calculate signature", e);
        }
    }
    
    public static String signSHA256(String data, String secret) {
        try {
            // 尝试使用不同的方法：先对数据进行SHA256哈希，然后再进行HMAC
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] dataHash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(dataHash);
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Unable to calculate signature", e);
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

