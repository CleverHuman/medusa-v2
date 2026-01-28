package com.medusa.mall.service;

import com.medusa.mall.domain.PgpKey;
import java.util.List;

/**
 * PGP密钥Service接口
 * 
 * @author medusa
 * @date 2025-01-16
 */
public interface IPgpKeyService 
{
    /**
     * 查询PGP密钥
     * 
     * @param id PGP密钥主键
     * @return PGP密钥
     */
    public PgpKey selectPgpKeyById(Long id);

    /**
     * 查询PGP密钥列表
     * 
     * @param pgpKey PGP密钥
     * @return PGP密钥集合
     */
    public List<PgpKey> selectPgpKeyList(PgpKey pgpKey);

    /**
     * 新增PGP密钥
     * 
     * @param pgpKey PGP密钥
     * @return 结果
     */
    public int insertPgpKey(PgpKey pgpKey);

    /**
     * 修改PGP密钥
     * 
     * @param pgpKey PGP密钥
     * @return 结果
     */
    public int updatePgpKey(PgpKey pgpKey);

    /**
     * 批量删除PGP密钥
     * 
     * @param ids 需要删除的PGP密钥主键集合
     * @return 结果
     */
    public int deletePgpKeyByIds(Long[] ids);

    /**
     * 删除PGP密钥信息
     * 
     * @param id PGP密钥主键
     * @return 结果
     */
    public int deletePgpKeyById(Long id);

    /**
     * 根据密钥类型查询密钥
     * 
     * @param keyType 密钥类型
     * @return PGP密钥
     */
    public PgpKey selectPgpKeyByType(String keyType);

    /**
     * 查询默认密钥
     * 
     * @return 默认密钥
     */
    public PgpKey selectDefaultPgpKey();

    /**
     * 查询激活的密钥列表
     * 
     * @return 激活的密钥列表
     */
    public List<PgpKey> selectActivePgpKeys();

    /**
     * 获取第一个可用的公钥
     * 
     * @return 第一个可用的公钥
     */
    public PgpKey selectFirstAvailablePublicKey();

    /**
     * 生成新的PGP密钥对
     * 
     * @param keyName 密钥名称
     * @param keySize 密钥长度
     * @return 生成的密钥对
     */
    public PgpKey generatePgpKeyPair(String keyName, Integer keySize);

    /**
     * 设置默认密钥
     * 
     * @param id 密钥ID
     * @return 结果
     */
    public int setDefaultPgpKey(Long id);

    /**
     * 激活/禁用密钥
     * 
     * @param id 密钥ID
     * @param isActive 是否激活
     * @return 结果
     */
    public int togglePgpKeyStatus(Long id, Integer isActive);
} 