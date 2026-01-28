package com.medusa.mall.domain.member;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.medusa.common.core.domain.BaseEntity;

/**
 * PCSP服务记录实体
 * Premium Customer Service Package
 * 
 * @author medusa
 */
public class MemberPcsp extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    /** ID */
    private Long id;
    
    /** 会员ID */
    private Long memberId;
    
    /** 购买的PCSP产品ID */
    private Long productId;
    
    /** 订单号 */
    private String orderSn;
    
    /** 套餐类型: 1-3个月, 2-6个月, 3-12个月 */
    private Integer packageType;
    
    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;
    
    /** 过期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiryDate;
    
    /** 状态: 0-已过期, 1-有效, 2-已取消 */
    private Integer status;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getMemberId() {
        return memberId;
    }
    
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getOrderSn() {
        return orderSn;
    }
    
    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }
    
    public Integer getPackageType() {
        return packageType;
    }
    
    public void setPackageType(Integer packageType) {
        this.packageType = packageType;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    // Business Methods
    
    /**
     * 判断PCSP是否有效
     * @return true-有效, false-无效
     */
    public boolean isValid() {
        return status != null && status == 1 
               && expiryDate != null 
               && new Date().before(expiryDate);
    }
    
    /**
     * 获取剩余天数
     * @return 剩余天数
     */
    public long getRemainingDays() {
        if (expiryDate == null) {
            return 0;
        }
        long diff = expiryDate.getTime() - new Date().getTime();
        long days = diff / (1000 * 60 * 60 * 24);
        return days > 0 ? days : 0;
    }
    
    /**
     * 获取套餐类型名称
     * @return 套餐类型名称
     */
    public String getPackageTypeName() {
        if (packageType == null) {
            return "Unknown";
        }
        switch (packageType) {
            case 1: return "3 Months";
            case 2: return "6 Months";
            case 3: return "12 Months";
            default: return "Unknown";
        }
    }
    
    /**
     * 获取状态名称
     * @return 状态名称
     */
    public String getStatusName() {
        if (status == null) {
            return "Unknown";
        }
        switch (status) {
            case 0: return "Expired";
            case 1: return "Active";
            case 2: return "Cancelled";
            default: return "Unknown";
        }
    }
    
    @Override
    public String toString() {
        return "MemberPcsp{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", productId=" + productId +
                ", orderSn='" + orderSn + '\'' +
                ", packageType=" + packageType +
                ", startDate=" + startDate +
                ", expiryDate=" + expiryDate +
                ", status=" + status +
                ", remainingDays=" + getRemainingDays() +
                '}';
    }
}



