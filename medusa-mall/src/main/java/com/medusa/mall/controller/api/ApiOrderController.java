package com.medusa.mall.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.medusa.common.annotation.Anonymous;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.model.LoginMember;
import com.medusa.mall.domain.member.OrderHistoryVO;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.utils.ServletUtils;
import com.medusa.framework.web.service.MemberTokenService;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.OrderAddRequest;
import com.medusa.mall.domain.order.ShippingAddress;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.service.IOrderService;

import java.util.List;

/**
 * Order API Controller
 */
@RestController
@CrossOrigin
@RequestMapping("/api/mall/order")
public class ApiOrderController extends BaseController {
    @Autowired
    private MemberTokenService memberTokenService;

    @Autowired
    private IOrderService orderService;

    /**
     * Get order list
     */
    @GetMapping("/list")
    public TableDataInfo list() {
        startPage();  // Add pagination
        LoginMember loginMember = memberTokenService.getLoginMember(ServletUtils.getRequest());
        Long memberId = loginMember.getMemberId();
        List<Order> orders = orderService.selectOrderListById(memberId);
        return getDataTable(orders);
    }

    /**
     * Create order
     * 支持临时用户创建订单
     */
    @Anonymous
    @PostMapping("/add")
    public AjaxResult createOrder(@RequestBody OrderAddRequest request) {
        try {
            // Check if this is a Bond order and get bondApplicationId from session
            // ✅ 修改：支持多种 BOND SKU 格式
            // 1. BOND-BASE-UNIT 或 BOND-BASE-UNIT- 开头
            // 2. SKU-BOND-BASE- 开头（管理员修改后的格式，如 SKU-BOND-BASE-1000）
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                boolean isBondOrder = request.getItems().stream()
                    .anyMatch(item -> {
                        String sku = item.getSku();
                        return sku != null && (
                            sku.equals("BOND-BASE-UNIT") || 
                            sku.startsWith("BOND-BASE-UNIT-") || 
                            sku.startsWith("SKU-BOND-BASE-")
                        );
                    });
                
                if (isBondOrder && request.getBondApplicationId() == null) {
                    // Try to get from session
                    jakarta.servlet.http.HttpServletRequest httpRequest = ServletUtils.getRequest();
                    if (httpRequest != null) {
                        jakarta.servlet.http.HttpSession session = httpRequest.getSession(false);
                        if (session != null) {
                            Object bondAppIdObj = session.getAttribute("bondApplicationId");
                            if (bondAppIdObj instanceof Long) {
                                request.setBondApplicationId((Long) bondAppIdObj);
                            } else if (bondAppIdObj instanceof Number) {
                                request.setBondApplicationId(((Number) bondAppIdObj).longValue());
                            }
                            
                            Object bondAppNumberObj = session.getAttribute("bondApplicationNumber");
                            if (bondAppNumberObj instanceof String) {
                                request.setBondApplicationNumber((String) bondAppNumberObj);
                            }
                        }
                    }
                }
            }
            
            Payment payment = orderService.createOrder(request);
            return AjaxResult.success("order created successfully", payment);
        } catch (Exception e) {
            return AjaxResult.error("order creation failed: " + e.getMessage());
        }
    }



    /**
     * Get order detail
     */
    @GetMapping("/{id}")
    public AjaxResult getOrderDetail(@PathVariable("id") String id) {
        try {
            Object orderDetail = orderService.getOrderDetailById(id);
            return AjaxResult.success(orderDetail);
        } catch (Exception e) {
            return AjaxResult.error("get order detail failed: " + e.getMessage());
        }
    }

    /**
     * Update order shipping information
     */
    @PutMapping("/shipping")
    public AjaxResult updateShippingInfo(@RequestBody ShippingAddress shippingAddress) {
        try {
            int rows = orderService.updateShippingInfoByOrderId(shippingAddress);
            return rows > 0 ? AjaxResult.success("Update successful") : AjaxResult.error("Update failed");
        } catch (Exception e) {
            return AjaxResult.error("Failed to update shipping information: " + e.getMessage());
        }
    }

    /**
     * Get user order list with detailed information
     * 支持临时用户和正常用户订单查询，返回完整订单信息
     * 支持分页查询，每页默认10条
     */
    @Anonymous
    @GetMapping("/list/{memberId}")
    public AjaxResult getOrderList(
            @PathVariable("memberId") Long memberId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        try {
            List<OrderHistoryVO> allOrders = orderService.getOrderHistoryByMemberId(memberId);
            
            // 实现分页
            int totalOrders = allOrders.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalOrders);
            
            // 如果起始索引超出范围，返回空列表
            if (startIndex >= totalOrders) {
                return AjaxResult.success(new java.util.ArrayList<>());
            }
            
            List<OrderHistoryVO> pagedOrders = allOrders.subList(startIndex, endIndex);
            return AjaxResult.success(pagedOrders);
        } catch (Exception e) {
            return AjaxResult.error("Failed to get order list: " + e.getMessage());
        }
    }

    /**
     * Get order vendor ID
     */
    @Anonymous
    @GetMapping("/{orderId}/vendor-id")
    public AjaxResult getOrderVendorId(@PathVariable("orderId") String orderId) {
        try {
            Order order = orderService.selectOrderById(orderId);
            if (order == null) {
                return AjaxResult.error("Order not found");
            }
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("vendorId", order.getVendorId());
            return AjaxResult.success(data);
        } catch (Exception e) {
            return AjaxResult.error("Failed to retrieve order information: " + e.getMessage());
        }
    }
} 