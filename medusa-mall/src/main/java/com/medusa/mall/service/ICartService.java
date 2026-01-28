package com.medusa.mall.service;

import com.medusa.mall.domain.cart.Cart;
import com.medusa.mall.domain.cart.CartItem;

public interface ICartService {

    public Cart getCart(Long memberId);

    public Cart getCartWithDetails(Long memberId);

    //add to cart
    public int addOrUpdateCartItem(Long memberId, CartItem cartItem);

    public int removeCartItem(Long memberId, Long productId);

    // Guest购物车相关方法
    public Cart getGuestCart(String guestId);

    public int addToGuestCart(String guestId, CartItem cartItem);

    public int removeFromGuestCart(String guestId, Long productId);
}
