package com.medusa.mall.domain.order;

import java.math.BigDecimal;
import java.util.Date;

public class Payment {
    private String id;
    private String orderId;
    private String paymentNo;
    private String payType;
    private BigDecimal amount;
    private String currency;
    private Long userId;
    private String username;
    private Integer payStatus;
    private Date payTime;
    private String remark;
    private String paymentInfo;  // 用于存储Telegram友好的支付信息
    private BigDecimal rate;     // 新增汇率字段
    private BigDecimal totalcoin; // 新增 totalcoin 字段
    private String orderChannel;  // 新增订单渠道字段，用于区分用户来源（Telegram/OS）
    private BigDecimal paidcoin;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getPayStatus() { return payStatus; }
    public void setPayStatus(Integer payStatus) { this.payStatus = payStatus; }
    public Date getPayTime() { return payTime; }
    public void setPayTime(Date payTime) { this.payTime = payTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getPaymentInfo() { return paymentInfo; }
    public void setPaymentInfo(String paymentInfo) { this.paymentInfo = paymentInfo; }
    public BigDecimal getRate() { return rate; }           // 新增 getter
    public void setRate(BigDecimal rate) { this.rate = rate; }  // 新增 setter
    public BigDecimal getTotalcoin() { return totalcoin; }           // 新增 getter
    public void setTotalcoin(BigDecimal totalcoin) { this.totalcoin = totalcoin; }  // 新增 setter
    public BigDecimal getTotalCoin() { return totalcoin; }           // 别名方法，保持代码一致性
    public String getOrderChannel() { return orderChannel; }           // 新增 getter
    public void setOrderChannel(String orderChannel) { this.orderChannel = orderChannel; }  // 新增 setter
    public BigDecimal getPaidcoin() { return paidcoin; }
    public void setPaidcoin(BigDecimal paidcoin) { this.paidcoin = paidcoin; }
} 