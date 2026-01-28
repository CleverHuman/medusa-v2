package com.medusa.mall.domain;

import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class VOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "Order Number")
    private String orderSn;

    @Excel(name = "Username")
    private String username;

    @Excel(name = "Product Code")
    private String procode;

    @Excel(name = "Amount")
    private String amt;

    @Excel(name = "Quantity")
    private String qty;

    @Excel(name = "Postage")
    private String postage;

    @Excel(name = "Receiver Name")
    private String receiverName;

    @Excel(name = "Address Line 1")
    private String addressLine1;

    @Excel(name = "Address Line 2")
    private String addressLine2;

    @Excel(name = "Address Line 3")
    private String addressLine3;

    @Excel(name = "City")
    private String city;

    @Excel(name = "State")
    private String state;

    @Excel(name = "Post Code")
    private String postCode;

    @Excel(name = "Total Coin")
    private String tcoin;

    @Excel(name = "Paid Coin")
    private String paidcoin;

    @Excel(name = "Total Amount")
    private String tamt;

    @Excel(name = "Coin Type", readConverterExp = "0=BTC,1=USDT,2=XMR")
    private String cointype;

    @Excel(name = "Status", readConverterExp = "0=Pending,1=Paid,2=Fulfilled,3=Shipped,4=Cancelled,5=isDeleted")
    private Integer status;

    // Getters and Setters
    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProcode() {
        return procode;
    }

    public void setProcode(String procode) {
        this.procode = procode;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getPostage() {
        return postage;
    }

    public void setPostage(String postage) {
        this.postage = postage;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getTcoin() {
        return tcoin;
    }

    public void setTcoin(String tcoin) {
        this.tcoin = tcoin;
    }

    public String getPaidcoin() {
        return paidcoin;
    }

    public void setPaidcoin(String paidcoin) {
        this.paidcoin = paidcoin;
    }

    public String getTamt() {
        return tamt;
    }

    public void setTamt(String tamt) {
        this.tamt = tamt;
    }

    public String getCointype() {
        return cointype;
    }

    public void setCointype(String cointype) {
        this.cointype = cointype;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("orderSn", getOrderSn())
            .append("username", getUsername())
            .append("procode", getProcode())
            .append("amt", getAmt())
            .append("qty", getQty())
            .append("postage", getPostage())
            .append("receiverName", getReceiverName())
            .append("addressLine1", getAddressLine1())
            .append("addressLine2", getAddressLine2())
            .append("city", getCity())
            .append("state", getState())
            .append("postCode", getPostCode())
            .append("tcoin", getTcoin())
            .append("paidcoin", getPaidcoin())
            .append("tamt", getTamt())
            .append("cointype", getCointype())
            .append("status", getStatus())
            .toString();
    }
} 