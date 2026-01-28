package com.medusa.mall.utils;

import com.medusa.common.utils.sign.HmacSHA256Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SignatureUtil {
    private static final Logger log = LoggerFactory.getLogger(SignatureUtil.class);

    /**
     * Verify BTCPay webhook signature
     * 
     * @param secret webhook secret key
     * @param body request body
     * @param signature signature header
     * @return verification result
     */
    public static boolean verify(String secret, byte[] body, String signature) {
        try {
            if (secret == null || body == null || signature == null) {
                log.warn("Invalid parameters for signature verification");
                return false;
            }

            // BTCPay uses sha256= prefix signature format
            if (!signature.startsWith("sha256=")) {
                log.warn("Invalid signature format: {}", signature);
                return false;
            }

            String expectedSignature = signature.substring(7); // Remove "sha256=" prefix
            
            // Add debug information
            String bodyString = new String(body, StandardCharsets.UTF_8);
            log.info("Secret: {}", secret);
            log.info("Body: {}", bodyString);
            log.info("Body bytes length: {}", body.length);
            
            // Try different signature calculation methods
            String calculatedSignature1 = HmacSHA256Signer.sign(bodyString, secret);
            String calculatedSignature2 = HmacSHA256Signer.sign(bodyString.trim(), secret);
            String calculatedSignature3 = HmacSHA256Signer.signBytes(body, secret);
            
            // Try different character encodings
            String calculatedSignature4 = HmacSHA256Signer.signBytes(body, secret, "ISO-8859-1");
            
            // Try Base64 encoded signature (although BTCPay usually uses hex, some implementations might be different)
            String calculatedSignature5 = HmacSHA256Signer.signBase64(bodyString, secret);
            
            // Try different signature format - BTCPay might use different algorithm
            String calculatedSignature6 = HmacSHA256Signer.signSHA256(bodyString, secret);
            
            log.info("Calculated signature (string): {}", calculatedSignature1);
            log.info("Calculated signature (trimmed): {}", calculatedSignature2);
            log.info("Calculated signature (bytes): {}", calculatedSignature3);
            log.info("Calculated signature (ISO-8859-1): {}", calculatedSignature4);
            log.info("Calculated signature (Base64): {}", calculatedSignature5);
            log.info("Calculated signature (SHA256): {}", calculatedSignature6);
            
            // Compare signatures
            boolean isValid1 = calculatedSignature1.equals(expectedSignature);
            boolean isValid2 = calculatedSignature2.equals(expectedSignature);
            boolean isValid3 = calculatedSignature3.equals(expectedSignature);
            boolean isValid4 = calculatedSignature4.equals(expectedSignature);
            boolean isValid5 = calculatedSignature5.equals(expectedSignature);
            boolean isValid6 = calculatedSignature6.equals(expectedSignature);
            
            if (isValid1 || isValid2 || isValid3 || isValid4 || isValid5 || isValid6) {
                log.info("Signature verification successful!");
                return true;
            } else {
                log.warn("Signature verification failed. Expected: {}, Calculated (string): {}, Calculated (trimmed): {}, Calculated (bytes): {}, Calculated (ISO-8859-1): {}, Calculated (Base64): {}, Calculated (SHA256): {}", 
                        expectedSignature, calculatedSignature1, calculatedSignature2, calculatedSignature3, calculatedSignature4, calculatedSignature5, calculatedSignature6);
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error during signature verification", e);
            return false;
        }
    }
} 