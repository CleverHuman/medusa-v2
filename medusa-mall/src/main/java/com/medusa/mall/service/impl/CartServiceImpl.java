package com.medusa.mall.service.impl;

import com.medusa.common.core.redis.RedisCache;
import com.medusa.mall.domain.cart.Cart;
import com.medusa.mall.domain.cart.CartItem;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.vo.CartItemVO;
import com.medusa.mall.service.ICartService;
import com.medusa.mall.service.IProductService;
import com.medusa.mall.service.IProduct2Service;
import com.medusa.mall.service.IMemberService;
import com.medusa.mall.service.IMemberBenefitService;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.mall.domain.member.MemberBenefit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements ICartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IProductService productService;

    @Autowired
    private IProduct2Service product2Service;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IMemberBenefitService memberBenefitService;

    private final String CART_KEY_PREFIX = "cart:member:";
    private final String GUEST_CART_KEY_PREFIX = "cart:guest:";

    private String getCartKey(Long memberId) {
        return CART_KEY_PREFIX + memberId;
    }

    private String getGuestCartKey(String guestId) {
        return GUEST_CART_KEY_PREFIX + guestId;
    }

    /**
     * Calculate discount based on member level
     */
    private BigDecimal calculateMemberDiscount(Long memberId, BigDecimal subtotal) {
        try {
            // Get member info
            MemberInfo memberInfo = memberService.selectMemberInfoById(memberId);
            if (memberInfo == null || memberInfo.getCurrentLevel() == null) {
                return BigDecimal.ZERO;
            }

            // Get member benefit based on level
            MemberBenefit memberBenefit = memberBenefitService.selectMemberBenefitByLevelId(memberInfo.getCurrentLevel().longValue());
            if (memberBenefit == null) {
                return BigDecimal.ZERO;
            }

            // Calculate discount based on member level
            BigDecimal discount = BigDecimal.ZERO;
            String levelName = memberBenefit.getLevelName();
            
            if (levelName != null) {
                switch (levelName.toLowerCase()) {
                    case "platinum":
                    case "diamond":
                        // Platinum and Diamond: use percent_discount from database
                        if (memberBenefit.getPercentDiscount() != null && memberBenefit.getPercentDiscount().compareTo(BigDecimal.ZERO) > 0) {
                            discount = subtotal.multiply(memberBenefit.getPercentDiscount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                        }
                        break;
                    default:
                        // Gold and below: keep existing logic (fixed discount + percentage discount)
                        // Fixed discount
                        if (memberBenefit.getFixedDiscount() != null && memberBenefit.getFixedDiscount().compareTo(BigDecimal.ZERO) > 0) {
                            discount = discount.add(memberBenefit.getFixedDiscount());
                        }
                        
                        // Percentage discount
                        if (memberBenefit.getPercentDiscount() != null && memberBenefit.getPercentDiscount().compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal percentDiscount = subtotal.multiply(memberBenefit.getPercentDiscount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                            discount = discount.add(percentDiscount);
                        }
                        break;
                }
            } else {
                // Fallback to existing logic if level name is null
                // Fixed discount
                if (memberBenefit.getFixedDiscount() != null && memberBenefit.getFixedDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    discount = discount.add(memberBenefit.getFixedDiscount());
                }
                
                // Percentage discount
                if (memberBenefit.getPercentDiscount() != null && memberBenefit.getPercentDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal percentDiscount = subtotal.multiply(memberBenefit.getPercentDiscount()).setScale(2, BigDecimal.ROUND_HALF_UP);
                    discount = discount.add(percentDiscount);
                }
            }

            return discount;
        } catch (Exception e) {
            // Log error and return zero discount
            log.error("Error calculating member discount: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Cart getCart(Long memberId) {
        String cartKey = getCartKey(memberId);
        Map<String, Object> entries = redisCache.getCacheMap(cartKey);

        List<CartItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String productIdStr = (String) entry.getKey();
            Long productId = Long.parseLong(productIdStr);

            CartItem itemDTO = (CartItem) entry.getValue();
            
            // Get the SKU (Product2) information for price
            Product2 sku = product2Service.selectProduct2ById(productId);
            if (sku != null) {
                BigDecimal price = sku.getPrice();
                Integer quantity = itemDTO.getQuantity();
                BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
                subtotal = subtotal.add(itemTotal);
                
                CartItem itemVO = new CartItem();
                itemVO.setProductId(productId);
                itemVO.setPrice(price);
                itemVO.setQuantity(itemDTO.getQuantity());

                items.add(itemVO);
            }
        }

        Cart cartVO = new Cart();
        cartVO.setItems(items);

        BigDecimal discount = calculateMemberDiscount(memberId, subtotal);
        total = subtotal.subtract(discount);

        cartVO.setSubtotal(subtotal);
        cartVO.setDiscount(discount);
        cartVO.setTotal(total);
        return cartVO;
    }

    /**
     * Get cart with complete product information for static pages
     */
    public Cart getCartWithDetails(Long memberId) {
        String cartKey = getCartKey(memberId);
        Map<String, Object> entries = redisCache.getCacheMap(cartKey);

        List<CartItemVO> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String productIdStr = (String) entry.getKey();
            Long skuId = Long.parseLong(productIdStr);

            CartItem itemDTO = (CartItem) entry.getValue();
            
            // Get the SKU (Product2) information
            Product2 sku = product2Service.selectProduct2ById(skuId);
            if (sku != null) {
                // Get the product information
                Product product = productService.selectProductByProductId(sku.getProductId());
                
                if (product != null) {
                    BigDecimal price = sku.getPrice();
                    Integer quantity = itemDTO.getQuantity();
                    BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
                    subtotal = subtotal.add(itemTotal);
                    
                    CartItemVO itemVO = new CartItemVO();
                    itemVO.setProductId(skuId);
                    itemVO.setProductName(product.getName());
                    itemVO.setImageUrl(product.getImageUrl());
                    itemVO.setSku(sku.getSku());
                    itemVO.setModel(sku.getModel() != null ? sku.getModel().setScale(2, java.math.RoundingMode.HALF_UP).toString() : "");
                    itemVO.setUnit(sku.getUnit());
                    itemVO.setPrice(price);
                    itemVO.setCurrency(sku.getCurrency());
                    itemVO.setQuantity(quantity);
                    itemVO.setTotalPrice(itemTotal);

                    items.add(itemVO);
                }
            }
        }

        Cart cartVO = new Cart();
        // Convert CartItemVO list to CartItem list for compatibility
        List<CartItem> cartItems = new ArrayList<>();
        for (CartItemVO itemVO : items) {
            CartItem cartItem = new CartItem();
            cartItem.setProductId(itemVO.getProductId());
            cartItem.setPrice(itemVO.getPrice());
            cartItem.setQuantity(itemVO.getQuantity());
            cartItems.add(cartItem);
        }
        cartVO.setItems(cartItems);
        
        // Set detailed items for static pages
        List<Object> detailedItems = new ArrayList<>(items);
        cartVO.setDetailedItems(detailedItems);

        // Calculate member discount
        BigDecimal memberDiscount = calculateMemberDiscount(memberId, subtotal);
        
        // Set currency from first item if available
        String currency = "AUD"; // default
        if (!items.isEmpty()) {
            currency = items.get(0).getCurrency();
        }
        
        // Calculate total (subtotal - member discount, coupon discount will be added later)
        total = subtotal.subtract(memberDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        cartVO.setSubtotal(subtotal);
        cartVO.setDiscount(memberDiscount); // 保持向后兼容
        cartVO.setMemberDiscount(memberDiscount); // 设置会员折扣
        cartVO.setCouponDiscount(BigDecimal.ZERO); // 默认优惠券折扣为0
        cartVO.setCurrency(currency);
        cartVO.setTotal(total);
        
        return cartVO;
    }

    @Override
    public int addOrUpdateCartItem(Long memberId, CartItem cartItem) {
        String cartKey = getCartKey(memberId);
        redisCache.setCacheMapValue(cartKey,cartItem.getProductId().toString(), cartItem);
        return 0;
    }

    @Override
    public int removeCartItem(Long memberId, Long productId) {
        String cartKey = getCartKey(memberId);
        redisCache.deleteCacheMapValue(cartKey, productId.toString());
        return 0;
    }

    @Override
    public Cart getGuestCart(String guestId) {
        String cartKey = getGuestCartKey(guestId);
        Map<String, Object> entries = redisCache.getCacheMap(cartKey);

        List<CartItemVO> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String productIdStr = (String) entry.getKey();
            Long skuId = Long.parseLong(productIdStr);

            CartItem itemDTO = (CartItem) entry.getValue();
            
            // Get the SKU (Product2) information
            Product2 sku = product2Service.selectProduct2ById(skuId);
            if (sku != null) {
                // Get the product information
                Product product = productService.selectProductByProductId(sku.getProductId());
                
                if (product != null) {
                    BigDecimal price = sku.getPrice();
                    Integer quantity = itemDTO.getQuantity();
                    BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
                    subtotal = subtotal.add(itemTotal);
                    
                    CartItemVO itemVO = new CartItemVO();
                    itemVO.setProductId(skuId);
                    itemVO.setProductName(product.getName());
                    itemVO.setImageUrl(product.getImageUrl());
                    itemVO.setSku(sku.getSku());
                    itemVO.setModel(sku.getModel() != null ? sku.getModel().setScale(2, java.math.RoundingMode.HALF_UP).toString() : "");
                    itemVO.setUnit(sku.getUnit());
                    itemVO.setPrice(price);
                    itemVO.setCurrency(sku.getCurrency());
                    itemVO.setQuantity(quantity);
                    itemVO.setTotalPrice(itemTotal);

                    items.add(itemVO);
                }
            }
        }

        Cart cartVO = new Cart();
        // Convert CartItemVO list to CartItem list for compatibility
        List<CartItem> cartItems = new ArrayList<>();
        for (CartItemVO itemVO : items) {
            CartItem cartItem = new CartItem();
            cartItem.setProductId(itemVO.getProductId());
            cartItem.setPrice(itemVO.getPrice());
            cartItem.setQuantity(itemVO.getQuantity());
            cartItems.add(cartItem);
        }
        cartVO.setItems(cartItems);
        
        // Set detailed items for static pages
        List<Object> detailedItems = new ArrayList<>(items);
        cartVO.setDetailedItems(detailedItems);

        // Guest没有会员折扣
        BigDecimal memberDiscount = BigDecimal.ZERO;
        
        // Set currency from first item if available
        String currency = "AUD"; // default
        if (!items.isEmpty()) {
            currency = items.get(0).getCurrency();
        }
        
        // Calculate total (Guest没有折扣)
        total = subtotal;

        cartVO.setSubtotal(subtotal);
        cartVO.setDiscount(memberDiscount);
        cartVO.setMemberDiscount(memberDiscount);
        cartVO.setCouponDiscount(BigDecimal.ZERO);
        cartVO.setCurrency(currency);
        cartVO.setTotal(total);
        
        return cartVO;
    }

    @Override
    public int addToGuestCart(String guestId, CartItem cartItem) {
        String cartKey = getGuestCartKey(guestId);
        redisCache.setCacheMapValue(cartKey, cartItem.getProductId().toString(), cartItem);
        
        // 设置24小时过期时间
        redisCache.expire(cartKey, 24, java.util.concurrent.TimeUnit.HOURS);
        return 1;
    }

    @Override
    public int removeFromGuestCart(String guestId, Long productId) {
        String cartKey = getGuestCartKey(guestId);
        redisCache.deleteCacheMapValue(cartKey, productId.toString());
        return 1;
    }

}
