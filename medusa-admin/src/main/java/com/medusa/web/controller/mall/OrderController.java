package com.medusa.web.controller.mall;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.core.domain.AjaxResult;

import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.ShippingAddress;
import com.medusa.mall.domain.order.OrderAddRequest;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.service.IOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/mall/order")
public class OrderController extends BaseController {
    @Autowired
    private IOrderService orderService;

    @GetMapping("/list")
    public TableDataInfo list(Order order) {
        startPage();
        List<Order> orders = orderService.selectOrderList(order);
        return getDataTable(orders);
    }

    /**
     * Get order details
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return success(orderService.selectOrderById(id));
    }

    /**
     * Update order
     */
    @PutMapping
    public AjaxResult edit(@RequestBody Order order) {
        return toAjax(orderService.updateOrder(order));
    }

    /**
     * Delete order
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(orderService.deleteOrderByIds(ids));
    }

    /**
     * Get order detail aggregation
     */
    @GetMapping("/detail/{id}")
    public AjaxResult getOrderDetail(@PathVariable("id") String id) {
        return success(orderService.getOrderDetailById(id));
    }

    @PostMapping("/updateShippingInfo")
    public AjaxResult updateShippingInfo(@RequestBody ShippingAddress shippingAddress) {
        int rows = orderService.updateShippingInfoByOrderId(shippingAddress);
        return toAjax(rows);
    }

    /**
     * Create new order (admin)
     */
    @PostMapping("/add")
    public AjaxResult createOrder(@RequestBody OrderAddRequest request) {
        try {
            Payment payment = orderService.createOrder(request);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("msg", "Order created successfully");
            ajax.put("data", payment);
            return ajax;
        } catch (Exception e) {
            return error("Failed to create order: " + e.getMessage());
        }
    }

    /**
     * Resolve order dispute
     */
    @PutMapping("/resolveDispute/{id}")
    public AjaxResult resolveDispute(@PathVariable("id") String id) {
        try {
            int rows = orderService.resolveDispute(id);
            if (rows > 0) {
                return success("Dispute resolved successfully");
            } else {
                return error("Failed to resolve dispute");
            }
        } catch (Exception e) {
            return error("Failed to resolve dispute: " + e.getMessage());
        }
    }

    /**
     * Update customer comment for an order
     */
    @PostMapping("/updateCustomerComment")
    public AjaxResult updateCustomerComment(@RequestParam("orderId") String orderId,
                                            @RequestParam("customerComment") String customerComment) {
        try {
            Order order = new Order();
            order.setId(orderId);
            order.setCustomerComment(customerComment);
            int rows = orderService.updateOrder(order);
            if (rows > 0) {
                return success("Comment saved successfully");
            } else {
                return error("Failed to save comment");
            }
        } catch (Exception e) {
            return error("Failed to save comment: " + e.getMessage());
        }
    }
}
