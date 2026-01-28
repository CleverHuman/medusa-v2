package com.medusa.mall.controller;

import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.model.LoginMember;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.utils.ServletUtils;
import com.medusa.framework.web.service.MemberTokenService;
import com.medusa.mall.domain.cart.Cart;
import com.medusa.mall.domain.cart.CartItem;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mall/cart")
public class ApiCartController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private MemberTokenService memberTokenService;

    @GetMapping()
    public AjaxResult list() {
        LoginMember loginMember = memberTokenService.getLoginMember(ServletUtils.getRequest());
        Long memberId = loginMember.getMemberId();
        Cart cart = cartService.getCart(memberId);
        return AjaxResult.success(cart);
    }

    @PostMapping("/update")
    public AjaxResult update(@RequestBody CartItem cartItem) {
        LoginMember loginMember = memberTokenService.getLoginMember(ServletUtils.getRequest());
        Long memberId = loginMember.getMemberId();
        cartService.addOrUpdateCartItem(memberId, cartItem);
        return AjaxResult.success();
    }

    @PostMapping(value = "/remove/{productId}")
    public AjaxResult remove(@PathVariable Long productId) {
        LoginMember loginMember = memberTokenService.getLoginMember(ServletUtils.getRequest());
        Long memberId = loginMember.getMemberId();
        cartService.removeCartItem(memberId, productId);
        return AjaxResult.success();
    }
}
