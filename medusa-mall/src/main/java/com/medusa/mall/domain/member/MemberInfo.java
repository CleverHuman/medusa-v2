package com.medusa.mall.domain.member;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medusa.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class MemberInfo extends BaseEntity {

    private Long memberId;

    private String username;

    private String primaryContact;

    private String secondaryContact;

    private String tgId;

    private String tgUsername;

    /** 0-os/1-tg*/
    private Integer sourceType;

    private Integer status;

    private Long totalOrders;

    private Integer currentLevel;

    private BigDecimal currentPoint;

    private BigDecimal currentAud;

    private Integer lastLevel;

    private BigDecimal lastPoint;

    private BigDecimal lastAud;

    private Integer lastSourceType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSeen;

    private BigDecimal fixDiscount;

    private BigDecimal percentDiscount;

    private BigDecimal shippingDiscount;

    private String levelName;

    /** 等级描述 */
    private String des;

    /** 关联账号ID */
    private Long linkedAccount;

    /** 关联账号用户名 */
    private String linkedAccountUsername;

    /** PCSP状态: 0-未购买, 1-有效, 2-已过期 */
    private Integer pcspStatus;

    /** PCSP过期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pcspExpiryDate;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }

    public BigDecimal getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(BigDecimal currentPoint) {
        this.currentPoint = currentPoint;
    }

    public BigDecimal getCurrentAud() {
        return currentAud;
    }

    public void setCurrentAud(BigDecimal currentAud) {
        this.currentAud = currentAud;
    }

    public Integer getLastLevel() {
        return lastLevel;
    }

    public void setLastLevel(Integer lastLevel) {
        this.lastLevel = lastLevel;
    }

    public BigDecimal getLastPoint() {
        return lastPoint;
    }

    public void setLastPoint(BigDecimal lastPoint) {
        this.lastPoint = lastPoint;
    }

    public BigDecimal getLastAud() {
        return lastAud;
    }

    public void setLastAud(BigDecimal lastAud) {
        this.lastAud = lastAud;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }


    public String getPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(String primaryContact) {
        this.primaryContact = primaryContact;
    }

    public String getSecondaryContact() {
        return secondaryContact;
    }

    public void setSecondaryContact(String secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    public String getTgId() {
        return tgId;
    }

    public void setTgId(String tgId) {
        this.tgId = tgId;
    }

    public String getTgUsername() {
        return tgUsername;
    }

    public void setTgUsername(String tgUsername) {
        this.tgUsername = tgUsername;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getLastSourceType() {
        return lastSourceType;
    }

    public void setLastSourceType(Integer lastSourceType) {
        this.lastSourceType = lastSourceType;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public BigDecimal getFixDiscount() {
        return fixDiscount;
    }

    public void setFixDiscount(BigDecimal fixDiscount) {
        this.fixDiscount = fixDiscount;
    }

    public BigDecimal getPercentDiscount() {
        return percentDiscount;
    }

    public void setPercentDiscount(BigDecimal percentDiscount) {
        this.percentDiscount = percentDiscount;
    }

    public BigDecimal getShippingDiscount() {
        return shippingDiscount;
    }

    public void setShippingDiscount(BigDecimal shippingDiscount) {
        this.shippingDiscount = shippingDiscount;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public Long getLinkedAccount() {
        return linkedAccount;
    }

    public void setLinkedAccount(Long linkedAccount) {
        this.linkedAccount = linkedAccount;
    }

    public String getLinkedAccountUsername() {
        return linkedAccountUsername;
    }

    public void setLinkedAccountUsername(String linkedAccountUsername) {
        this.linkedAccountUsername = linkedAccountUsername;
    }

    public Integer getPcspStatus() {
        return pcspStatus;
    }

    public void setPcspStatus(Integer pcspStatus) {
        this.pcspStatus = pcspStatus;
    }

    public Date getPcspExpiryDate() {
        return pcspExpiryDate;
    }

    public void setPcspExpiryDate(Date pcspExpiryDate) {
        this.pcspExpiryDate = pcspExpiryDate;
    }
}
