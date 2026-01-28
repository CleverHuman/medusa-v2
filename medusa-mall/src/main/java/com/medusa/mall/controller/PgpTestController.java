package com.medusa.mall.controller;

import com.medusa.mall.utils.PgpEncryptionUtil;
import com.medusa.mall.utils.PgpKeyGenerator;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * PGP加解密功能测试控制器
 * 
 * @author medusa
 * @date 2025-01-16
 */
@RestController
@RequestMapping("/api/pgp/test")
public class PgpTestController {

    /**
     * 生成测试密钥对
     */
    @PostMapping("/generate-keys")
    public Map<String, Object> generateTestKeys(@RequestParam(defaultValue = "测试用户") String name,
                                               @RequestParam(defaultValue = "test@example.com") String email,
                                               @RequestParam(defaultValue = "2048") int keySize) {
        try {
            PgpKeyGenerator.PgpKeyPair keyPair = PgpKeyGenerator.generateKeyPair(name, email, keySize);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "密钥生成成功");
            
            Map<String, Object> publicKey = new HashMap<>();
            publicKey.put("id", keyPair.getPublicKeyId());
            publicKey.put("fingerprint", keyPair.getPublicFingerprint());
            publicKey.put("size", keyPair.getPublicKeySize());
            publicKey.put("algorithm", keyPair.getPublicAlgorithm());
            publicKey.put("data", keyPair.getPublicKeyData());
            publicKey.put("pgpFormat", PgpEncryptionUtil.convertBase64ToPgpFormat(keyPair.getPublicKeyData(), false));
            
            Map<String, Object> privateKey = new HashMap<>();
            privateKey.put("id", keyPair.getPrivateKeyId());
            privateKey.put("fingerprint", keyPair.getPrivateFingerprint());
            privateKey.put("size", keyPair.getPrivateKeySize());
            privateKey.put("algorithm", keyPair.getPrivateAlgorithm());
            privateKey.put("data", keyPair.getPrivateKeyData());
            privateKey.put("pgpFormat", PgpEncryptionUtil.convertBase64ToPgpFormat(keyPair.getPrivateKeyData(), true));
            
            result.put("publicKey", publicKey);
            result.put("privateKey", privateKey);
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "密钥生成失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 测试加密功能
     */
    @PostMapping("/encrypt")
    public Map<String, Object> testEncrypt(@RequestParam String plaintext,
                                          @RequestParam String publicKeyData) {
        try {
            String encryptedData = PgpEncryptionUtil.encrypt(plaintext, publicKeyData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "加密成功");
            result.put("originalText", plaintext);
            result.put("encryptedData", encryptedData);
            result.put("originalLength", plaintext.length());
            result.put("encryptedLength", encryptedData.length());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "加密失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 测试解密功能
     */
    @PostMapping("/decrypt")
    public Map<String, Object> testDecrypt(@RequestParam String encryptedData,
                                          @RequestParam String privateKeyData) {
        try {
            String decryptedText = PgpEncryptionUtil.decrypt(encryptedData, privateKeyData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "解密成功");
            result.put("encryptedData", encryptedData);
            result.put("decryptedText", decryptedText);
            result.put("encryptedLength", encryptedData.length());
            result.put("decryptedLength", decryptedText.length());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "解密失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 完整测试：生成密钥 -> 加密 -> 解密 -> 验证
     */
    @PostMapping("/full-test")
    public Map<String, Object> fullTest(@RequestParam(defaultValue = "这是一个PGP加密测试消息！Hello PGP Encryption!") String testText,
                                       @RequestParam(defaultValue = "测试用户") String name,
                                       @RequestParam(defaultValue = "test@example.com") String email,
                                       @RequestParam(defaultValue = "2048") int keySize) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. 生成密钥对
            PgpKeyGenerator.PgpKeyPair keyPair = PgpKeyGenerator.generateKeyPair(name, email, keySize);
            
            // 2. 加密
            String encryptedData = PgpEncryptionUtil.encrypt(testText, keyPair.getPublicKeyData());
            
            // 3. 解密
            String decryptedText = PgpEncryptionUtil.decrypt(encryptedData, keyPair.getPrivateKeyData());
            
            // 4. 验证一致性
            boolean isConsistent = testText.equals(decryptedText);
            
            result.put("success", true);
            result.put("message", "完整测试完成");
            result.put("testText", testText);
            result.put("encryptedData", encryptedData);
            result.put("decryptedText", decryptedText);
            result.put("isConsistent", isConsistent);
            result.put("keyId", keyPair.getPublicKeyId());
            result.put("keySize", keyPair.getPublicKeySize());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "完整测试失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 格式转换测试
     */
    @PostMapping("/format-convert")
    public Map<String, Object> formatConvert(@RequestParam String keyData,
                                            @RequestParam boolean isPrivate) {
        try {
            String pgpFormat = PgpEncryptionUtil.convertBase64ToPgpFormat(keyData, isPrivate);
            String base64Format = PgpEncryptionUtil.convertPgpFormatToBase64(pgpFormat);
            
            boolean isConsistent = keyData.equals(base64Format);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "格式转换成功");
            result.put("originalBase64", keyData);
            result.put("pgpFormat", pgpFormat);
            result.put("convertedBase64", base64Format);
            result.put("isConsistent", isConsistent);
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "格式转换失败: " + e.getMessage());
            return result;
        }
    }
} 