package com.medusa.mall.service;
import com.medusa.mall.domain.order.Order;
import org.springframework.stereotype.Service;
import com.medusa.mall.domain.order.OrderAddRequest;
import com.medusa.mall.domain.order.ShippingAddress;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.domain.member.OrderHistoryVO;

import java.util.List;

public interface IOrderService {
    public List<Order> selectOrderListById(Long memberId);


    public List<Order> selectOrderList(Order order);


    public int insertOrder(Order order);

    /**
     * 创建订单
     * 
     * @param request 订单创建请求
     * @return 支付信息
     */
    public Payment createOrder(OrderAddRequest request);

    /**
     * 查询订单信息
     * 
     * @param id 订单ID
     * @return 订单信息
     */
    public Order selectOrderById(String id);

    /**
     * 修改订单
     * 
     * @param order 订单信息
     * @return 结果
     */
    public int updateOrder(Order order);

    /**
     * 批量删除订单
     * 
     * @param ids 需要删除的订单ID数组
     * @return 结果
     */
    public int deleteOrderByIds(String[] ids);

    /**
     * 聚合订单详情
     * @param id 订单ID
     * @return 订单详情聚合对象
     */
    public Object getOrderDetailById(String id);

    /**
     * 更新订单收货信息（备注、运单号）
     * @param shippingAddress 收货信息
     * @return 影响行数
     */
    public int updateShippingInfoByOrderId(ShippingAddress shippingAddress);

    /**
     * 解决订单争议
     * @param id 订单ID
     * @return 影响行数
     */
    public int resolveDispute(String id);

    /**
     * 根据memberId获取订单历史详情
     * @param memberId 会员ID（支持负数临时用户ID）
     * @return 订单历史列表
     */
    public List<OrderHistoryVO> getOrderHistoryByMemberId(Long memberId);
}
