package com.medusa.mall.service;

import com.medusa.mall.domain.PgpEncryptedAddress;
import java.util.List;

/**
 * PGP加密地址Service接口
 * 
 * @author medusa
 * @date 2025-01-16
 */
public interface IPgpEncryptedAddressService 
{
    /**
     * 查询PGP加密地址
     * 
     * @param id PGP加密地址主键
     * @return PGP加密地址
     */
    public PgpEncryptedAddress selectPgpEncryptedAddressById(Long id);

    /**
     * 查询PGP加密地址列表
     * 
     * @param pgpEncryptedAddress PGP加密地址
     * @return PGP加密地址集合
     */
    public List<PgpEncryptedAddress> selectPgpEncryptedAddressList(PgpEncryptedAddress pgpEncryptedAddress);

    /**
     * 新增PGP加密地址
     * 
     * @param pgpEncryptedAddress PGP加密地址
     * @return 结果
     */
    public int insertPgpEncryptedAddress(PgpEncryptedAddress pgpEncryptedAddress);

    /**
     * 修改PGP加密地址
     * 
     * @param pgpEncryptedAddress PGP加密地址
     * @return 结果
     */
    public int updatePgpEncryptedAddress(PgpEncryptedAddress pgpEncryptedAddress);

    /**
     * 批量删除PGP加密地址
     * 
     * @param ids 需要删除的PGP加密地址主键集合
     * @return 结果
     */
    public int deletePgpEncryptedAddressByIds(Long[] ids);

    /**
     * 删除PGP加密地址信息
     * 
     * @param id PGP加密地址主键
     * @return 结果
     */
    public int deletePgpEncryptedAddressById(Long id);

    /**
     * 根据订单ID查询加密地址
     * 
     * @param orderId 订单ID
     * @return PGP加密地址
     */
    public PgpEncryptedAddress selectPgpEncryptedAddressByOrderId(String orderId);

    /**
     * 根据用户ID查询加密地址列表
     * 
     * @param userId 用户ID
     * @return PGP加密地址列表
     */
    public List<PgpEncryptedAddress> selectPgpEncryptedAddressByUserId(Long userId);

    /**
     * 根据密钥ID查询加密地址列表
     * 
     * @param keyId 密钥ID
     * @return PGP加密地址列表
     */
    public List<PgpEncryptedAddress> selectPgpEncryptedAddressByKeyId(Long keyId);

    /**
     * 加密地址信息
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param address 原始地址
     * @return 加密结果
     */
    public PgpEncryptedAddress encryptAddress(String orderId, Long userId, String address);

    /**
     * 解密地址信息
     * 
     * @param encryptedAddress 加密地址
     * @return 解密结果
     */
    public String decryptAddress(String encryptedAddress);
} 