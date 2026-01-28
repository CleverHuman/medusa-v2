package com.medusa.common.core.domain.entity;
import com.medusa.common.core.domain.BaseEntity;

public class Member extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long memberId;

    private String username;

    private String password;

    private String primaryContact;

    private String secondaryContact;

    private String tgId;

    private String tgUsername;

    /** 0-os/1-tg*/
    private Integer sourceType;

    private Integer status;

    /** 关联账号ID */
    private Long linkedAccount;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getLinkedAccount() {
        return linkedAccount;
    }

    public void setLinkedAccount(Long linkedAccount) {
        this.linkedAccount = linkedAccount;
    }
}