package com.medusa.mall.domain;

import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * PGP密钥对象 mall_pgp_keys
 * 
 * @author medusa
 * @date 2025-01-16
 */
public class PgpKey extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 密钥名称 */
    @Excel(name = "密钥名称")
    private String keyName;

    /** 密钥类型 */
    @Excel(name = "密钥类型", readConverterExp = "public=公钥,private=私钥")
    private String keyType;

    /** PGP密钥ID */
    @Excel(name = "PGP密钥ID")
    private String keyId;

    /** PGP密钥指纹 */
    @Excel(name = "PGP密钥指纹")
    private String fingerprint;

    /** 完整的PGP密钥数据 */
    @Excel(name = "PGP密钥数据")
    private String keyData;

    /** 密钥长度 */
    @Excel(name = "密钥长度")
    private Integer keySize;

    /** 加密算法 */
    @Excel(name = "加密算法")
    private String algorithm;

    /** 是否激活 */
    @Excel(name = "是否激活", readConverterExp = "1=激活,0=禁用")
    private Integer isActive;

    /** 是否为默认密钥 */
    @Excel(name = "是否为默认密钥", readConverterExp = "1=默认,0=非默认")
    private Integer isDefault;

    /** 密钥创建时间 */
    @Excel(name = "密钥创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 密钥过期时间 */
    @Excel(name = "密钥过期时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expiresAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setKeyName(String keyName) 
    {
        this.keyName = keyName;
    }

    public String getKeyName() 
    {
        return keyName;
    }

    public void setKeyType(String keyType) 
    {
        this.keyType = keyType;
    }

    public String getKeyType() 
    {
        return keyType;
    }

    public void setKeyId(String keyId) 
    {
        this.keyId = keyId;
    }

    public String getKeyId() 
    {
        return keyId;
    }

    public void setFingerprint(String fingerprint) 
    {
        this.fingerprint = fingerprint;
    }

    public String getFingerprint() 
    {
        return fingerprint;
    }

    public void setKeyData(String keyData) 
    {
        this.keyData = keyData;
    }

    public String getKeyData() 
    {
        return keyData;
    }

    public void setKeySize(Integer keySize) 
    {
        this.keySize = keySize;
    }

    public Integer getKeySize() 
    {
        return keySize;
    }

    public void setAlgorithm(String algorithm) 
    {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() 
    {
        return algorithm;
    }

    public void setIsActive(Integer isActive) 
    {
        this.isActive = isActive;
    }

    public Integer getIsActive() 
    {
        return isActive;
    }

    public void setIsDefault(Integer isDefault) 
    {
        this.isDefault = isDefault;
    }

    public Integer getIsDefault() 
    {
        return isDefault;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setExpiresAt(Date expiresAt) 
    {
        this.expiresAt = expiresAt;
    }

    public Date getExpiresAt() 
    {
        return expiresAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("keyName", getKeyName())
            .append("keyType", getKeyType())
            .append("keyId", getKeyId())
            .append("fingerprint", getFingerprint())
            .append("keyData", getKeyData())
            .append("keySize", getKeySize())
            .append("algorithm", getAlgorithm())
            .append("isActive", getIsActive())
            .append("isDefault", getIsDefault())
            .append("createdAt", getCreatedAt())
            .append("expiresAt", getExpiresAt())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .toString();
    }
} 