package com.medusa.mall.domain.export;

import com.medusa.common.annotation.Excel;

public class VOrderPhExport {
    @Excel(name = "Order Number", height = 70.87, sort = 1)
    private String orderSn;

    @Excel(name = "Username", height = 70.87, sort = 2)
    private String username;

    @Excel(name = "Product Code", height = 70.87, sort = 3, wrapText = true)
    private String procode;

    @Excel(name = "Amount", height = 70.87, sort = 4, wrapText = true)
    private String amt;

    @Excel(name = "Quantity", height = 70.87, sort = 5, wrapText = true)
    private String qty;

    @Excel(name = "Postage", height = 70.87, sort = 6)
    private String postage;

    // Receiver column includes name and full address lines with line breaks
    @Excel(name = "Receiver Name", wrapText = true, width = 40, height = 70.87, sort = 7)
    private String receiver;

    public String getOrderSn() { return orderSn; }
    public void setOrderSn(String orderSn) { this.orderSn = orderSn; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getProcode() { return procode; }
    public void setProcode(String procode) { this.procode = procode; }
    public String getAmt() { return amt; }
    public void setAmt(String amt) { this.amt = amt; }
    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; }
    public String getPostage() { return postage; }
    public void setPostage(String postage) { this.postage = postage; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
}


