package com.medusa.mall.mapper;

import com.medusa.mall.domain.PgpKey;
import java.util.List;

/**
 * PGP密钥Mapper接口
 * 
 * @author medusa
 * @date 2025-01-16
 */
public interface PgpKeyMapper 
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
     * 删除PGP密钥
     * 
     * @param id PGP密钥主键
     * @return 结果
     */
    public int deletePgpKeyById(Long id);

    /**
     * 批量删除PGP密钥
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePgpKeyByIds(Long[] ids);

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
} 