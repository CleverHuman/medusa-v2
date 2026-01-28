package com.medusa.mall.domain.vendor;

import java.math.BigDecimal;
import java.util.Date;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Vendor Bot Violation Entity
 */
public class VendorBotViolation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 违规记录ID */
    private Long id;

    /** Vendor ID */
    private Long vendorId;

    /** 关联订单ID */
    private String orderId;

    /** 用户Telegram ID */
    private Long userId;

    /** 消息ID */
    private Long messageId;

    /** 违规类型: keyword/other */
    private String violationType;

    /** 匹配的关键字 */
    private String matchedKeyword;

    /** 违规消息内容（仅存储违规片段） */
    private String messageContent;

    /** 罚金金额 */
    private BigDecimal penaltyAmount;

    /** 罚金状态: pending/processed/failed */
    private String penaltyStatus;

    /** 违规时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date violationTime;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date processedTime;

    /** 处理备注 */
    private String processNotes;

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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getMatchedKeyword() {
        return matchedKeyword;
    }

    public void setMatchedKeyword(String matchedKeyword) {
        this.matchedKeyword = matchedKeyword;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public String getPenaltyStatus() {
        return penaltyStatus;
    }

    public void setPenaltyStatus(String penaltyStatus) {
        this.penaltyStatus = penaltyStatus;
    }

    public Date getViolationTime() {
        return violationTime;
    }

    public void setViolationTime(Date violationTime) {
        this.violationTime = violationTime;
    }

    public Date getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(Date processedTime) {
        this.processedTime = processedTime;
    }

    public String getProcessNotes() {
        return processNotes;
    }

    public void setProcessNotes(String processNotes) {
        this.processNotes = processNotes;
    }
}
