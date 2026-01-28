package com.medusa.mall.domain.order;

public class ShippingAddress {

    private String id;           // 收货地址ID
    private String orderId;      // 订单ID
    private String name;         // 收货人姓名
    private String address1;     // 地址行1
    private String address2;     // 地址行2
    private String address3;     // 地址行3
    private String city;         // 城市
    private String state;        // 省/州
    private String postalCode;   // 邮编
    private String country;      // 国家

    private String shippingMethod; // 配送方式
    private java.util.Date shippingTime; // 发货时间
    private String shippingNumber; // 运单号
    private String remark; // 备注

    // getter/setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }
    public String getAddress2() { return address2; }
    public void setAddress2(String address2) { this.address2 = address2; }
    public String getAddress3() { return address3; }
    public void setAddress3(String address3) { this.address3 = address3; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    public java.util.Date getShippingTime() { return shippingTime; }
    public void setShippingTime(java.util.Date shippingTime) { this.shippingTime = shippingTime; }
    public String getShippingNumber() { return shippingNumber; }
    public void setShippingNumber(String shippingNumber) { this.shippingNumber = shippingNumber; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
} 