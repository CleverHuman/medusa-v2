package com.medusa.mall.mapper;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.OrderAddRequest;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper {

    public List<Order> selectOrderListById(Long memberId);

    public List<Order> selectOrderList(Order order);

    public int createOrder(OrderAddRequest request);

    /**
     * 新增订单
     * 
     * @param order 订单信息
     * @return 结果
     */
    public int insertOrder(Order order);

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

    List<Order> selectOrderListByIdPaged(@Param("memberId") Long memberId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 检查订单号是否存在
     * 
     * @param orderId 订单号
     * @return 存在返回1，不存在返回0
     */
    public int checkOrderIdExists(@Param("orderId") String orderId);

    /**
     * 查询已发货但余额未释放且到期的订单
     * 
     * @return 订单列表
     */
    public List<Order> selectExpiredBalanceOrders();
}
