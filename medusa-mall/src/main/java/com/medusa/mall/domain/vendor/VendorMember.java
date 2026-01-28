package com.medusa.mall.domain.vendor;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medusa.common.core.domain.BaseEntity;

/**
 * Vendor portal member entity.
 */
public class VendorMember extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Username for login */
    private String username;

    /** BCrypt password hash */
    private String passwordHash;

    /** Email address */
    private String email;

    /** Phone number */
    private String phone;

    /** Status: 1=active, 0=disabled */
    private Integer status;

    /** Last login time */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}

