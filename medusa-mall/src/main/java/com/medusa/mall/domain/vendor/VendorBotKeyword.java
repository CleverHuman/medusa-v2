package com.medusa.mall.domain.vendor;

import java.util.Date;
import com.medusa.common.core.domain.BaseEntity;

/**
 * Vendor Bot Keyword Entity
 */
public class VendorBotKeyword extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 关键字ID */
    private Long id;

    /** Vendor ID (NULL表示全局规则) */
    private Long vendorId;

    /** 关键字 */
    private String keyword;

    /** 匹配类型: exact/fuzzy/regex */
    private String keywordType;

    /** 严重程度: 1=警告, 2=罚金 */
    private Integer severity;

    /** 是否启用 */
    private Integer isActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getKeywordType() {
        return keywordType;
    }

    public void setKeywordType(String keywordType) {
        this.keywordType = keywordType;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }
}
