package com.medusa.mall.domain.member;

import java.util.List;

public class OrderHistoryVO {
    private Long orderId;
    private String orderDate;
    private String productName;  // 保留用于向后兼容，显示第一个商品
    private String amount;       // 保留用于向后兼容，显示第一个商品的价格
    private String trackingNo;
    private List<OrderItemVO> items;  // 新增：存储所有订单项
    private Integer orderStatus;      // 新增：订单状态
    private Integer paymentStatus;    // 新增：支付状态
    private String shippingTime;      // 新增：发货时间
    private Long vendorId;            // 新增：Vendor ID（如果有）

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public List<OrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVO> items) {
        this.items = items;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getShippingTime() {
        return shippingTime;
    }

    public void setShippingTime(String shippingTime) {
        this.shippingTime = shippingTime;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    // 内部类：订单项信息
    public static class OrderItemVO {
        private String productName;
        private String productSpec;
        private String price;
        private Integer quantity;
        private String totalPrice;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductSpec() {
            return productSpec;
        }

        public void setProductSpec(String productSpec) {
            this.productSpec = productSpec;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(String totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}
