package com.medusa.mall.service.impl;

import com.medusa.common.utils.DateUtils;
import com.medusa.common.utils.SecurityUtils;
import com.medusa.mall.domain.PgpKey;
import com.medusa.mall.mapper.PgpKeyMapper;
import com.medusa.mall.service.IPgpKeyService;
import com.medusa.mall.utils.PgpKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PGP密钥Service业务层处理
 * 
 * @author medusa
 * @date 2025-01-16
 */
@Service
public class PgpKeyServiceImpl implements IPgpKeyService 
{
    private static final Logger log = LoggerFactory.getLogger(PgpKeyServiceImpl.class);

    @Autowired
    private PgpKeyMapper pgpKeyMapper;

    /**
     * 查询PGP密钥
     * 
     * @param id PGP密钥主键
     * @return PGP密钥
     */
    @Override
    public PgpKey selectPgpKeyById(Long id)
    {
        return pgpKeyMapper.selectPgpKeyById(id);
    }

    /**
     * 查询PGP密钥列表
     * 
     * @param pgpKey PGP密钥
     * @return PGP密钥
     */
    @Override
    public List<PgpKey> selectPgpKeyList(PgpKey pgpKey)
    {
        return pgpKeyMapper.selectPgpKeyList(pgpKey);
    }

    /**
     * 新增PGP密钥
     * 
     * @param pgpKey PGP密钥
     * @return 结果
     */
    @Override
    public int insertPgpKey(PgpKey pgpKey)
    {
        try {
            pgpKey.setCreateTime(DateUtils.getNowDate());
            log.info("Preparing to insert PGP key: " + pgpKey.getKeyName() + ", type: " + pgpKey.getKeyType() + ", ID: " + pgpKey.getKeyId());
            int result = pgpKeyMapper.insertPgpKey(pgpKey);
            log.info("PGP key insertion result: " + result + ", key ID: " + pgpKey.getKeyId());
            return result;
        } catch (Exception e) {
            log.error("Exception occurred while inserting PGP key: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 修改PGP密钥
     * 
     * @param pgpKey PGP密钥
     * @return 结果
     */
    @Override
    public int updatePgpKey(PgpKey pgpKey)
    {
        pgpKey.setUpdateTime(DateUtils.getNowDate());
        return pgpKeyMapper.updatePgpKey(pgpKey);
    }

    /**
     * 批量删除PGP密钥
     * 
     * @param ids 需要删除的PGP密钥主键
     * @return 结果
     */
    @Override
    public int deletePgpKeyByIds(Long[] ids)
    {
        return pgpKeyMapper.deletePgpKeyByIds(ids);
    }

    /**
     * 删除PGP密钥信息
     * 
     * @param id PGP密钥主键
     * @return 结果
     */
    @Override
    public int deletePgpKeyById(Long id)
    {
        return pgpKeyMapper.deletePgpKeyById(id);
    }

    /**
     * 根据密钥type查询密钥
     * 
     * @param keyType 密钥type
     * @return PGP密钥
     */
    @Override
    public PgpKey selectPgpKeyByType(String keyType)
    {
        return pgpKeyMapper.selectPgpKeyByType(keyType);
    }

    /**
     * 查询默认密钥
     * 
     * @return 默认密钥
     */
    @Override
    public PgpKey selectDefaultPgpKey()
    {
        return pgpKeyMapper.selectDefaultPgpKey();
    }

    /**
     * 查询激活的密钥列表
     * 
     * @return 激活的密钥列表
     */
    @Override
    public List<PgpKey> selectActivePgpKeys()
    {
        return pgpKeyMapper.selectActivePgpKeys();
    }

    /**
     * 获取第一个可用的公钥
     * 
     * @return 第一个可用的公钥
     */
    @Override
    public PgpKey selectFirstAvailablePublicKey()
    {
        // 先尝试获取默认公钥
        PgpKey defaultKey = selectDefaultPgpKey();
        if (defaultKey != null && "public".equals(defaultKey.getKeyType())) {
            return defaultKey;
        }
        
        // 如果没有默认公钥，获取第一个激活的公钥
        List<PgpKey> activeKeys = selectActivePgpKeys();
        for (PgpKey key : activeKeys) {
            if ("public".equals(key.getKeyType())) {
                return key;
            }
        }
        
        // 如果没有任何激活的公钥，返回null
        return null;
    }

    /**
     * 生成新的PGP密钥对
     * 
     * @param keyName 密钥名称
     * @param keySize 密钥length
     * @return 生成的密钥对
     */
    @Override
    @Transactional
    public PgpKey generatePgpKeyPair(String keyName, Integer keySize)
    {
        try {
            log.info("Starting PGP key pair generation: " + keyName + ", length: " + keySize);
            
            // 使用PGPainless生成真实的PGP密钥对
            String email = "admin@medusa.com"; // 默认邮箱，可以从参数传入
            int actualKeySize = keySize != null ? keySize : 2048;
            
            PgpKeyGenerator.PgpKeyPair keyPair = PgpKeyGenerator.generateKeyPair(keyName, email, actualKeySize);
            
            log.info("PGPainless key generation completed，key ID: " + keyPair.getPublicKeyId() + ", fingerprint: " + keyPair.getPublicFingerprint());
            
            // 生成公钥
            PgpKey publicKey = new PgpKey();
            publicKey.setKeyName(keyPair.getPublicKeyName());
            publicKey.setKeyType(keyPair.getPublicKeyType());
            publicKey.setKeySize(keyPair.getPublicKeySize());
            publicKey.setAlgorithm(keyPair.getPublicAlgorithm());
            publicKey.setIsActive(1);
            publicKey.setIsDefault(0);
            publicKey.setCreateTime(DateUtils.getNowDate());
            publicKey.setKeyId(keyPair.getPublicKeyId());
            publicKey.setFingerprint(keyPair.getPublicFingerprint());
            publicKey.setKeyData(keyPair.getPublicKeyData());
            
            log.info("Preparing to insert public key: " + publicKey.getKeyName() + ", key ID: " + keyPair.getPublicKeyId());
            
            // 保存公钥到数据库
            int publicResult = insertPgpKey(publicKey);
            log.info("Public key insertion result: " + publicResult + ", key ID: " + keyPair.getPublicKeyId());
            
            // 生成私钥
            PgpKey privateKey = new PgpKey();
            privateKey.setKeyName(keyPair.getPrivateKeyName());
            privateKey.setKeyType(keyPair.getPrivateKeyType());
            privateKey.setKeySize(keyPair.getPrivateKeySize());
            privateKey.setAlgorithm(keyPair.getPrivateAlgorithm());
            privateKey.setIsActive(1);
            privateKey.setIsDefault(0);
            privateKey.setCreateTime(DateUtils.getNowDate());
            privateKey.setKeyId(keyPair.getPrivateKeyId());
            privateKey.setFingerprint(keyPair.getPrivateFingerprint());
            privateKey.setKeyData(keyPair.getPrivateKeyData());
            
            log.info("Preparing to insert private key: " + privateKey.getKeyName() + ", key ID: " + keyPair.getPrivateKeyId());
            
            // 保存私钥到数据库
            int privateResult = insertPgpKey(privateKey);
            log.info("Private key insertion result: " + privateResult + ", key ID: " + keyPair.getPrivateKeyId());
            
            log.info("Key pair generation completed, returning public key");
            
            // 返回公钥（私钥通常不返回给前端）
            return publicKey;
        } catch (Throwable e) {
            log.error("Exception occurred while generating key pair: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置默认密钥
     * 
     * @param id key ID
     * @return 结果
     */
    @Override
    public int setDefaultPgpKey(Long id)
    {
        // 先取消所有默认密钥
        PgpKey updateKey = new PgpKey();
        updateKey.setIsDefault(0);
        pgpKeyMapper.updatePgpKey(updateKey);
        
        // 设置指定密钥为默认
        PgpKey defaultKey = new PgpKey();
        defaultKey.setId(id);
        defaultKey.setIsDefault(1);
        defaultKey.setUpdateTime(DateUtils.getNowDate());
        defaultKey.setUpdateBy(SecurityUtils.getUsername());
        
        return pgpKeyMapper.updatePgpKey(defaultKey);
    }

    /**
     * 激活/禁用密钥
     * 
     * @param id key ID
     * @param isActive 是否激活
     * @return 结果
     */
    @Override
    public int togglePgpKeyStatus(Long id, Integer isActive)
    {
        PgpKey pgpKey = new PgpKey();
        pgpKey.setId(id);
        pgpKey.setIsActive(isActive);
        pgpKey.setUpdateTime(DateUtils.getNowDate());
        pgpKey.setUpdateBy(SecurityUtils.getUsername());
        
        return pgpKeyMapper.updatePgpKey(pgpKey);
    }


} 