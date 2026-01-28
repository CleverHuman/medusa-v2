package com.medusa.mall.domain;

import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class TgPolicyConfig extends BaseEntity {
    private Long id;
    
    @JsonProperty("privacy_policy")
    private String privacyPolicy;
    
    @JsonProperty("terms_of_service")
    private String termsOfService;
    
    @JsonProperty("refund_policy")
    private String refundPolicy;
    
    @JsonProperty("shipping_policy")
    private String shippingPolicy;
    
    @JsonProperty("contact_info")
    private String contactInfo;
    
    private Date createTime;
    private Date updateTime;
    private String updateBy;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPrivacyPolicy() { return privacyPolicy; }
    public void setPrivacyPolicy(String privacyPolicy) { this.privacyPolicy = privacyPolicy; }
    
    public String getTermsOfService() { return termsOfService; }
    public void setTermsOfService(String termsOfService) { this.termsOfService = termsOfService; }
    
    public String getRefundPolicy() { return refundPolicy; }
    public void setRefundPolicy(String refundPolicy) { this.refundPolicy = refundPolicy; }
    
    public String getShippingPolicy() { return shippingPolicy; }
    public void setShippingPolicy(String shippingPolicy) { this.shippingPolicy = shippingPolicy; }
    
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
} 