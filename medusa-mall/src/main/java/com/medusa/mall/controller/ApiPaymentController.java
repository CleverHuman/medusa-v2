package com.medusa.mall.controller;

import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "PaymentController", description = "payment management")
@RequestMapping("/api/payments")
public class ApiPaymentController {
    @Autowired
    private PaymentService paymentService;

    @ApiOperation("create BTC payment invoice")
    @PostMapping("/btc/create-invoice")
    public AjaxResult createBtcInvoice(@RequestBody Payment payment) {
        // 设置支付类型为 BTC
        payment.setPayType("0");//0 means BTC

        Payment result = paymentService.createPayment(payment);
        return AjaxResult.success(result);
    }

    @ApiOperation("create payment record")
    @PostMapping("/create")
    public AjaxResult createPayment(@RequestBody Payment payment) {
        Payment result = paymentService.createPayment(payment);
        return AjaxResult.success(result);
    }

    @ApiOperation("query payment status")
    @GetMapping("/status/{orderId}")
    public AjaxResult getPaymentStatus(@PathVariable String orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return AjaxResult.success(payment);
    }

    @ApiOperation("update payment status")
    @PostMapping("/update-status")
    public AjaxResult updatePaymentStatus(@RequestBody Payment payment) {
        Payment result = paymentService.updatePaymentStatus(payment);
        return AjaxResult.success(result);
    }
} 