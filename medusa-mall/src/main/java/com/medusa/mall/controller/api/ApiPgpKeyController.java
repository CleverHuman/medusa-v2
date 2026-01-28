package com.medusa.mall.controller.api;

import com.medusa.common.annotation.Anonymous;
import com.medusa.mall.domain.PgpKey;
import com.medusa.mall.service.IPgpKeyService;
import com.medusa.mall.utils.PgpKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PGP公钥下载 API Controller
 * 供前端页面下载公钥使用
 * 
 * @author medusa
 * @date 2025-10-21
 */
@RestController
@RequestMapping("/api/mall/pgp")
public class ApiPgpKeyController {
    
    @Autowired
    private IPgpKeyService pgpKeyService;
    
    /**
     * 下载默认公钥
     * 公开接口，无需权限验证
     * 
     * @return 公钥文件
     */
    @Anonymous
    @GetMapping("/download/default-public-key")
    public ResponseEntity<Resource> downloadDefaultPublicKey() {
        try {
            // 获取默认公钥
            PgpKey defaultKey = pgpKeyService.selectDefaultPgpKey();
            
            if (defaultKey == null) {
                // 如果没有默认公钥，尝试获取第一个可用的公钥
                defaultKey = pgpKeyService.selectFirstAvailablePublicKey();
            }
            
            if (defaultKey == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取公钥数据（Base64格式）
            String keyData = defaultKey.getKeyData();
            if (keyData == null || keyData.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            // 转换为标准 PGP 格式（带 -----BEGIN PGP PUBLIC KEY BLOCK----- 头部）
            String pgpFormattedKey = PgpKeyGenerator.convertBase64ToPgpFormat(keyData, false);
            
            // 准备文件下载
            byte[] keyBytes = pgpFormattedKey.getBytes("UTF-8");
            ByteArrayResource resource = new ByteArrayResource(keyBytes);
            
            // 设置文件名
            String filename = "public-key.asc";
            if (defaultKey.getKeyName() != null && !defaultKey.getKeyName().isEmpty()) {
                filename = defaultKey.getKeyName().replaceAll("[^a-zA-Z0-9-_]", "_") + "-public-key.asc";
            }
            
            // 返回文件
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(keyBytes.length)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取默认公钥内容（文本形式）
     * 公开接口，无需权限验证
     * 
     * @return 公钥内容
     */
    @Anonymous
    @GetMapping("/default-public-key")
    public ResponseEntity<String> getDefaultPublicKey() {
        try {
            // 获取默认公钥
            PgpKey defaultKey = pgpKeyService.selectDefaultPgpKey();
            
            if (defaultKey == null) {
                // 如果没有默认公钥，尝试获取第一个可用的公钥
                defaultKey = pgpKeyService.selectFirstAvailablePublicKey();
            }
            
            if (defaultKey == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取公钥数据（Base64格式）
            String keyData = defaultKey.getKeyData();
            if (keyData == null || keyData.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            // 转换为标准 PGP 格式（带 -----BEGIN PGP PUBLIC KEY BLOCK----- 头部）
            String pgpFormattedKey = PgpKeyGenerator.convertBase64ToPgpFormat(keyData, false);
            
            // 返回公钥内容
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(pgpFormattedKey);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

