package com.medusa.mall.domain.vendor;

import java.math.BigDecimal;
import java.util.Date;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Vendor Bot Entity
 */
public class VendorBot extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** Bot配置ID */
    private Long id;

    /** Vendor ID */
    private Long vendorId;

    /** Telegram Bot Token */
    private String botToken;

    /** Bot Username */
    private String botUsername;

    /** Support Group ID */
    private Long supportGroupId;

    /** 状态: 0=禁用, 1=启用 */
    private Integer status;

    /** 单次违规罚金(USD) */
    private BigDecimal penaltyAmount;

    /** 每日最大罚金次数 */
    private Integer maxPenaltiesPerDay;

    /** Vendor名称（关联查询） */
    private String vendorName;

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

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    public Long getSupportGroupId() {
        return supportGroupId;
    }

    public void setSupportGroupId(Long supportGroupId) {
        this.supportGroupId = supportGroupId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public Integer getMaxPenaltiesPerDay() {
        return maxPenaltiesPerDay;
    }

    public void setMaxPenaltiesPerDay(Integer maxPenaltiesPerDay) {
        this.maxPenaltiesPerDay = maxPenaltiesPerDay;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
}
