package com.medusa.mall.domain;

import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * PGP加密地址对象 mall_pgp_encrypted_address
 * 
 * @author medusa
 * @date 2025-01-16
 */
public class PgpEncryptedAddress extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 订单ID */
    @Excel(name = "订单ID")
    private String orderId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 使用的PGP密钥ID */
    @Excel(name = "PGP密钥ID")
    private Long keyId;

    /** 加密后的地址信息 */
    @Excel(name = "加密地址")
    private String encryptedAddress;

    /** 原始地址的哈希值 */
    @Excel(name = "原始哈希")
    private String originalHash;

    /** 加密时间 */
    @Excel(name = "加密时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date encryptionTime;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setOrderId(String orderId) 
    {
        this.orderId = orderId;
    }

    public String getOrderId() 
    {
        return orderId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setKeyId(Long keyId) 
    {
        this.keyId = keyId;
    }

    public Long getKeyId() 
    {
        return keyId;
    }

    public void setEncryptedAddress(String encryptedAddress) 
    {
        this.encryptedAddress = encryptedAddress;
    }

    public String getEncryptedAddress() 
    {
        return encryptedAddress;
    }

    public void setOriginalHash(String originalHash) 
    {
        this.originalHash = originalHash;
    }

    public String getOriginalHash() 
    {
        return originalHash;
    }

    public void setEncryptionTime(Date encryptionTime) 
    {
        this.encryptionTime = encryptionTime;
    }

    public Date getEncryptionTime() 
    {
        return encryptionTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderId", getOrderId())
            .append("userId", getUserId())
            .append("keyId", getKeyId())
            .append("encryptedAddress", getEncryptedAddress())
            .append("originalHash", getOriginalHash())
            .append("encryptionTime", getEncryptionTime())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
} 