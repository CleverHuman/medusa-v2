package com.medusa.mall.domain.export;

import com.medusa.common.annotation.Excel;

public class VOrderAccExport {
    @Excel(name = "Date", dateFormat = "yyyy-MM-dd")
    private String date;

    @Excel(name = "Order Number")
    private String orderSn;

    @Excel(name = "Username")
    private String username;

    @Excel(name = "Product Code")
    private String procode;

    @Excel(name = "Product Amount")
    private String productAmount;

    @Excel(name = "Quantity")
    private String qty;

    @Excel(name = "Total Coin")
    private String tcoin;

    @Excel(name = "Paid Coin")
    private String paidcoin;

    @Excel(name = "Coin Type", readConverterExp = "0=BTC,1=USDT,2=XMR")
    private String cointype;

    @Excel(name = "Postage")
    private String postage;

    @Excel(name = "Shipping Cost")
    private String shippingCost;

    @Excel(name = "Comment")
    private String comment;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getOrderSn() { return orderSn; }
    public void setOrderSn(String orderSn) { this.orderSn = orderSn; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getProcode() { return procode; }
    public void setProcode(String procode) { this.procode = procode; }
    
    public String getProductAmount() { return productAmount; }
    public void setProductAmount(String productAmount) { this.productAmount = productAmount; }
    
    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; }
    
    public String getTcoin() { return tcoin; }
    public void setTcoin(String tcoin) { this.tcoin = tcoin; }
    
    public String getPaidcoin() { return paidcoin; }
    public void setPaidcoin(String paidcoin) { this.paidcoin = paidcoin; }
    
    public String getCointype() { return cointype; }
    public void setCointype(String cointype) { this.cointype = cointype; }
    
    public String getPostage() { return postage; }
    public void setPostage(String postage) { this.postage = postage; }
    
    public String getShippingCost() { return shippingCost; }
    public void setShippingCost(String shippingCost) { this.shippingCost = shippingCost; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
