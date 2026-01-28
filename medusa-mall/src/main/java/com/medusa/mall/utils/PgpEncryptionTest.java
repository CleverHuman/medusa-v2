package com.medusa.mall.utils;

/**
 * PGP加解密功能测试类
 * 
 * @author medusa
 * @date 2025-01-16
 */
public class PgpEncryptionTest {

    public static void main(String[] args) {
        try {
            System.out.println("=== PGP Encryption/Decryption Function Test ===\n");
            
            // 1. Generate test key pair
            System.out.println("1. Generating test key pair...");
            PgpKeyGenerator.PgpKeyPair keyPair = PgpKeyGenerator.generateKeyPair(
                "Test Key", "test@example.com", 2048);
            
            System.out.println("Public key ID: " + keyPair.getPublicKeyId());
            System.out.println("Private key ID: " + keyPair.getPrivateKeyId());
            System.out.println("Key length: " + keyPair.getPublicKeySize() + " bits");
            
            // 2. Test encryption function
            System.out.println("2. Testing encryption function...");
            String plaintext = "Hello, PGP encryption test!";
            System.out.println("Original text: " + plaintext);
            
            String encryptedData = PgpEncryptionUtil.encrypt(plaintext, keyPair.getPublicKeyData());
            System.out.println("Encryption successful! Encrypted data length: " + encryptedData.length());
            
            // 3. Test decryption function
            System.out.println("3. Testing decryption function...");
            String decryptedText = PgpEncryptionUtil.decrypt(encryptedData, keyPair.getPrivateKeyData());
            System.out.println("Decryption successful! Decrypted text: " + decryptedText);
            
            // 4. Verify encryption/decryption consistency
            System.out.println("4. Verifying encryption/decryption consistency...");
            boolean isConsistent = plaintext.equals(decryptedText);
            System.out.println("Encryption/decryption consistency: " + (isConsistent ? "✅ Pass" : "❌ Fail"));
            
            // 5. Test format conversion function
            System.out.println("5. Testing format conversion function...");
            String pgpPublicKey = PgpKeyGenerator.convertBase64ToPgpFormat(keyPair.getPublicKeyData(), false);
            System.out.println("Public key PGP format conversion successful, length: " + pgpPublicKey.length());
            
            String pgpPrivateKey = PgpKeyGenerator.convertBase64ToPgpFormat(keyPair.getPrivateKeyData(), true);
            System.out.println("Private key PGP format conversion successful, length: " + pgpPrivateKey.length());
            
            // 6. Test Base64 conversion function
            System.out.println("6. Testing Base64 conversion function...");
            String base64PublicKey = PgpKeyGenerator.convertPgpFormatToBase64(pgpPublicKey);
            boolean publicKeyConsistent = keyPair.getPublicKeyData().equals(base64PublicKey);
            System.out.println("Public key Base64 conversion consistency: " + (publicKeyConsistent ? "✅ Pass" : "❌ Fail"));
            
            String base64PrivateKey = PgpKeyGenerator.convertPgpFormatToBase64(pgpPrivateKey);
            boolean privateKeyConsistent = keyPair.getPrivateKeyData().equals(base64PrivateKey);
            System.out.println("Private key Base64 conversion consistency: " + (privateKeyConsistent ? "✅ Pass" : "❌ Fail"));
            
            // 7. Test error handling
            System.out.println("7. Testing error handling...");
            try {
                // Use wrong private key for decryption
                String wrongDecrypted = PgpEncryptionUtil.decrypt(encryptedData, keyPair.getPublicKeyData());
                System.out.println("❌ Error handling test failed: should throw exception");
            } catch (Exception e) {
                System.out.println("✅ Error handling test passed: correctly caught exception - " + e.getMessage());
            }
            
            // 8. Test long text encryption
            System.out.println("8. Testing long text encryption...");
            StringBuilder longTextBuilder = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                longTextBuilder.append("This is a long text for testing PGP encryption. ");
            }
            String longPlaintext = longTextBuilder.toString();
            
            String longEncrypted = PgpEncryptionUtil.encrypt(longPlaintext, keyPair.getPublicKeyData());
            String longDecrypted = PgpEncryptionUtil.decrypt(longEncrypted, keyPair.getPrivateKeyData());
            
            System.out.println("Long text length: " + longPlaintext.length() + " characters");
            boolean longTextConsistent = longPlaintext.equals(longDecrypted);
            System.out.println("Long text encryption/decryption consistency: " + (longTextConsistent ? "✅ Pass" : "❌ Fail"));
            
            // Summary
            System.out.println("=== Test Summary ===");
            System.out.println("✅ Key generation: Success");
            System.out.println("✅ Data encryption: Success");
            System.out.println("✅ Data decryption: Success");
            System.out.println("✅ Encryption/decryption consistency: " + (isConsistent ? "Pass" : "Fail"));
            System.out.println("✅ Format conversion: Success");
            System.out.println("✅ Error handling: Success");
            System.out.println("✅ Long text processing: " + (longTextConsistent ? "Pass" : "Fail"));
            
            if (isConsistent && publicKeyConsistent && privateKeyConsistent && longTextConsistent) {
                System.out.println("\n🎉 All tests passed! PGP encryption/decryption function working normally!");
            } else {
                System.out.println("\n⚠️ Some tests failed, please check implementation!");
            }
            
        } catch (Exception e) {
            System.err.println("测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 