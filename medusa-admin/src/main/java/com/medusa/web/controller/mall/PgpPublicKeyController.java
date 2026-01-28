package com.medusa.web.controller.mall;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.PgpKey;
import com.medusa.mall.service.IPgpKeyService;
import com.medusa.mall.utils.PgpEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * PGP public key API Controller
 * Provides public PGP key retrieval service for users, no permission verification required
 * 
 * @author medusa
 * @date 2025-01-16
 */
@RestController
@RequestMapping("/mall/api/pgp")
public class PgpPublicKeyController extends BaseController
{
    @Autowired
    private IPgpKeyService pgpKeyService;

    /**
     * Get default public key (public interface, no permission required)
     */
    @GetMapping("/default-public-key")
    public AjaxResult getDefaultPublicKey()
    {
        try {
            // Directly get default public key
            PgpKey defaultKey = pgpKeyService.selectDefaultPgpKey();
            if (defaultKey != null && "public".equals(defaultKey.getKeyType())) {
                // Convert Base64 format to PGP format
                String pgpFormat = PgpEncryptionUtil.convertBase64ToPgpFormat(defaultKey.getKeyData(), false);
                
                // Create a response object containing PGP format data
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("id", defaultKey.getId());
                responseData.put("keyName", defaultKey.getKeyName());
                responseData.put("keyType", defaultKey.getKeyType());
                responseData.put("keyId", defaultKey.getKeyId());
                responseData.put("fingerprint", defaultKey.getFingerprint());
                responseData.put("keySize", defaultKey.getKeySize());
                responseData.put("algorithm", defaultKey.getAlgorithm());
                responseData.put("keyData", pgpFormat); // Use PGP format data
                responseData.put("isActive", defaultKey.getIsActive());
                responseData.put("isDefault", defaultKey.getIsDefault());
                
                return success(responseData);
            }
            
            // If no default public key, return empty
            return success(null);
        } catch (Exception e) {
            return error("Failed to get default public key: " + e.getMessage());
        }
    }

    /**
     * Get the first available public key (public interface, no permission required)
     */
    @GetMapping("/first-public-key")
    public AjaxResult getFirstPublicKey()
    {
        try {
            // First try to get default public key
            PgpKey defaultKey = pgpKeyService.selectDefaultPgpKey();
            if (defaultKey != null && "public".equals(defaultKey.getKeyType())) {
                // Convert Base64 format to PGP format
                String pgpFormat = PgpEncryptionUtil.convertBase64ToPgpFormat(defaultKey.getKeyData(), false);
                
                // Create a response object containing PGP format data
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("id", defaultKey.getId());
                responseData.put("keyName", defaultKey.getKeyName());
                responseData.put("keyType", defaultKey.getKeyType());
                responseData.put("keyId", defaultKey.getKeyId());
                responseData.put("fingerprint", defaultKey.getFingerprint());
                responseData.put("keySize", defaultKey.getKeySize());
                responseData.put("algorithm", defaultKey.getAlgorithm());
                responseData.put("keyData", pgpFormat); // Use PGP format data
                responseData.put("isActive", defaultKey.getIsActive());
                responseData.put("isDefault", defaultKey.getIsDefault());
                
                return success(responseData);
            }
            
            // If no default public key, get first active public key
            List<PgpKey> activeKeys = pgpKeyService.selectActivePgpKeys();
            for (PgpKey key : activeKeys) {
                if ("public".equals(key.getKeyType())) {
                    // Convert Base64 format to PGP format
                    String pgpFormat = PgpEncryptionUtil.convertBase64ToPgpFormat(key.getKeyData(), false);
                    
                    // Create a response object containing PGP format data
                    java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                    responseData.put("id", key.getId());
                    responseData.put("keyName", key.getKeyName());
                    responseData.put("keyType", key.getKeyType());
                    responseData.put("keyId", key.getKeyId());
                    responseData.put("fingerprint", key.getFingerprint());
                    responseData.put("keySize", key.getKeySize());
                    responseData.put("algorithm", key.getAlgorithm());
                    responseData.put("keyData", pgpFormat); // Use PGP format data
                    responseData.put("isActive", key.getIsActive());
                    responseData.put("isDefault", key.getIsDefault());
                    
                    return success(responseData);
                }
            }
            
            // If no active public keys, return empty
            return success(null);
        } catch (Exception e) {
            return error("Failed to get public key: " + e.getMessage());
        }
    }

    /**
     * Get all active public key lists (public interface, no permission required)
     */
    @GetMapping("/active-public-keys")
    public AjaxResult getActivePublicKeys()
    {
        try {
            List<PgpKey> activeKeys = pgpKeyService.selectActivePgpKeys();
            // Filter out public keys
            List<PgpKey> publicKeys = activeKeys.stream()
                .filter(key -> "public".equals(key.getKeyType()))
                .collect(java.util.stream.Collectors.toList());
            
            return success(publicKeys);
        } catch (Exception e) {
            return error("Failed to get public key list: " + e.getMessage());
        }
    }

    /**
     * Download default public key file (public interface, no permission required)
     */
    @GetMapping("/download/default-public-key")
    public void downloadDefaultPublicKey(HttpServletResponse response)
    {
        try {
            // Get default public key
            PgpKey defaultKey = pgpKeyService.selectDefaultPgpKey();
            if (defaultKey == null || !"public".equals(defaultKey.getKeyType())) {
                // If no default public key, get first active public key
                List<PgpKey> activeKeys = pgpKeyService.selectActivePgpKeys();
                for (PgpKey key : activeKeys) {
                    if ("public".equals(key.getKeyType())) {
                        defaultKey = key;
                        break;
                    }
                }
            }
            
            if (defaultKey == null) {
                response.sendError(404, "No public key found");
                return;
            }
            
            // Convert Base64 format to standard PGP format
            String pgpFormat = PgpEncryptionUtil.convertBase64ToPgpFormat(defaultKey.getKeyData(), false);
            
            // Set filename
            String filename = "medusa-public-key-" + defaultKey.getKeyId() + ".asc";
            
            // Set response headers
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            
            // Write file content
            response.getWriter().write(pgpFormat);
            
        } catch (Exception e) {
            try {
                response.sendError(500, "Failed to download public key: " + e.getMessage());
            } catch (Exception ex) {
                // Ignore exception
            }
        }
    }

    /**
     * Download specified public key file (public interface, no permission required)
     */
    @GetMapping("/download/public-key/{keyId}")
    public void downloadPublicKey(@PathVariable String keyId, HttpServletResponse response)
    {
        try {
            // Find public key by keyId
            List<PgpKey> activeKeys = pgpKeyService.selectActivePgpKeys();
            PgpKey targetKey = null;
            
            for (PgpKey key : activeKeys) {
                if ("public".equals(key.getKeyType()) && keyId.equals(key.getKeyId())) {
                    targetKey = key;
                    break;
                }
            }
            
            if (targetKey == null) {
                response.sendError(404, "Public key not found");
                return;
            }
            
            // Convert Base64 format to standard PGP format
            String pgpFormat = PgpEncryptionUtil.convertBase64ToPgpFormat(targetKey.getKeyData(), false);
            
            // Set filename
            String filename = "medusa-public-key-" + targetKey.getKeyId() + ".asc";
            
            // Set response headers
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            
            // Write file content
            response.getWriter().write(pgpFormat);
            
        } catch (Exception e) {
            try {
                response.sendError(500, "Failed to download public key: " + e.getMessage());
            } catch (Exception ex) {
                // Ignore exception
            }
        }
    }
} 