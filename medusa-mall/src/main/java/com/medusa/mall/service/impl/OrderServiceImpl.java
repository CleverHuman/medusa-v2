package com.medusa.mall.service.impl;

import com.medusa.mall.domain.order.Order;
import com.medusa.mall.mapper.OrderMapper;
import com.medusa.mall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.medusa.mall.utils.OrderNumberGenerator;
import com.medusa.mall.utils.OrderNumberGeneratorWithUniqueness;
import com.medusa.mall.service.PaymentService;
import com.medusa.mall.service.InventoryLockService;

import com.medusa.mall.mapper.OrderItemMapper;
import com.medusa.mall.mapper.ShippingAddressMapper;
import com.medusa.mall.mapper.MemberBenefitMapper;
import com.medusa.mall.domain.member.MemberBenefit;
import com.medusa.mall.mapper.ShippingMethodMapper;
import com.medusa.mall.domain.order.ShippingMethod;

import com.medusa.mall.service.IMemberService;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.common.core.domain.entity.Member;
import com.medusa.mall.mapper.CouponMapper;
/* 
import com.medusa.mall.domain.OrderItem;
import com.medusa.mall.domain.vo.OrderAddRequest;
*/

import java.util.List;
import com.medusa.mall.domain.order.OrderAddRequest;
import com.medusa.mall.domain.order.OrderItem;
import com.medusa.mall.domain.order.OrderItemSimple;
import com.medusa.mall.domain.order.ShippingAddress;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import com.medusa.mall.service.ICouponService;
import com.medusa.mall.domain.coupon.Coupon;
import java.math.BigDecimal;

import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.mapper.PaymentMapper;

import java.util.Date;

import com.medusa.mall.service.IProduct2Service;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.Product;
import com.medusa.mall.service.IProductService;

import com.medusa.mall.enums.CurrencyEnum;

import com.medusa.mall.domain.coupon.CouponHistory;
import com.medusa.mall.mapper.CouponHistoryMapper;
import com.medusa.mall.domain.PgpKey;
import com.medusa.mall.service.IPgpKeyService;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.medusa.mall.service.member.IMemberPointHistoryService;
import com.medusa.mall.service.IMemberLevelService;
import com.medusa.mall.domain.member.MemberLevel;
import com.medusa.mall.domain.member.OrderHistoryVO;
import com.medusa.mall.service.IVendorBondService;
import com.medusa.mall.service.IVendorWithdrawalService;
import com.medusa.mall.service.IVendorApplicationService;
import com.medusa.mall.domain.vendor.VendorApplication;

/**
 * 订单Service业务层处理
 */
@Service
public class OrderServiceImpl implements IOrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ShippingAddressMapper shippingAddressMapper;

    @Autowired
    private ICouponService couponService;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    @Lazy
    private PaymentService paymentService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IProduct2Service product2Service;

    @Autowired
    private IProductService productService;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private MemberBenefitMapper memberBenefitMapper;

    @Autowired
    private ShippingMethodMapper shippingMethodMapper;

    @Autowired
    private com.medusa.mall.service.IShippingMethodService shippingMethodService;

    @Autowired
    private InventoryLockService inventoryLockService;

    @Autowired
    private CouponHistoryMapper couponHistoryMapper;
    
    @Autowired
    private IPgpKeyService pgpKeyService;
    
    @Autowired
    private com.medusa.mall.service.member.IMemberPcspService memberPcspService;

    @Autowired
    private IMemberPointHistoryService memberPointHistoryService;

    @Autowired
    private IMemberLevelService memberLevelService;

    @Autowired
    private IVendorBondService vendorBondService;
    
    @Autowired(required = false)
    private IVendorWithdrawalService vendorWithdrawalService;
    
    @Autowired(required = false)
    private IVendorApplicationService vendorApplicationService;

    @Override
    public List<Order> selectOrderListById(Long memberId) {
        return orderMapper.selectOrderListById(memberId);
    }

    @Override
    public List<Order> selectOrderList(Order order) {
        return orderMapper.selectOrderList(order);
    }

    /**
     * 创建订单
     * 
     * @param request 订单创建请求
     * @return 支付信息
     */
    @Override
    @Transactional
    public Payment createOrder(OrderAddRequest request) {
        // 1. 插入订单主表
        Order order = new Order();
        
        // 生成新的订单号
        String orderNumber;
        
        // 检查订单中的产品来源，决定使用哪种订单号生成方式
        boolean hasVendorProduct = false;
        boolean hasPlatformProduct = false;
        Integer vendorId = null;
        
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderAddRequest.Item item : request.getItems()) {
                // 通过SKU获取产品信息
                Product2 product2 = product2Service.selectProduct2BySku(item.getSku());
                if (product2 != null) {
                    Product product = productService.selectProductByProductId(product2.getProductId());
                    if (product != null) {
                        if (product.isVendorProduct()) {
                            hasVendorProduct = true;
                            Integer currentVendorId = product.getOriginId() != null ? product.getOriginId().intValue() : null;
                            
                            // 如果之前已经设置了vendorId，验证是否一致
                            if (vendorId != null && currentVendorId != null && !vendorId.equals(currentVendorId)) {
                                throw new RuntimeException("Order cannot contain products from different vendors. " +
                                    "Expected vendor ID: " + vendorId + ", found vendor ID: " + currentVendorId);
                            }
                            
                            if (vendorId == null && currentVendorId != null) {
                                vendorId = currentVendorId;
                            }
                        } else {
                            hasPlatformProduct = true;
                        }
                    }
                }
            }
        }
        
        // 验证产品来源一致性：不允许混合平台产品和Vendor产品
        if (hasVendorProduct && hasPlatformProduct) {
            throw new RuntimeException("Order cannot contain both platform products and vendor products. " +
                "Please separate them into different orders.");
        }
        
        // 验证：如果包含Vendor产品，所有产品必须来自同一个Vendor
        if (hasVendorProduct && vendorId == null) {
            throw new RuntimeException("Vendor product found but vendor ID is missing. Please check product configuration.");
        }
        
        // 根据产品来源生成订单号
        if (hasVendorProduct && vendorId != null) {
            orderNumber = OrderNumberGeneratorWithUniqueness.generateVendorOrderNumber(
                vendorId, 
                this::isOrderIdExists
            );
        } else {
            orderNumber = OrderNumberGeneratorWithUniqueness.generatePlatformOrderNumber(
                this::isOrderIdExists
            );
        }
        
        order.setId(orderNumber);
        order.setOrderSn(orderNumber);
        

        
        // 判断订单来源类型
        String sourceType = request.getSourceType();
        // Admin Panel 手动创建的订单（sourceType >= 10）都是 simple order
        // 10: OS (Admin), 11: Telegram (Admin), 12: Market (Admin), 99: Other/Custom (Admin)
        boolean isSimpleOrder = sourceType != null && Integer.parseInt(sourceType) >= 10;
        // 根据sourceType的奇偶性判断是否为TG订单
        boolean isTg = sourceType != null && Integer.parseInt(sourceType) % 2 == 1;

        for (OrderAddRequest.Item item : request.getItems()) {
        }
    
        

        Member member = null;
        Long tgId = null;
        String username = null;

        if (!isSimpleOrder) {
            if (isTg) {
                tgId = Long.valueOf(request.getUserId());
                member = memberService.selectMemberByTgId(tgId);
            } else {
                //for Order from web
                username = request.getUserName();
                if (username != null && username.startsWith("Guest_")) {
                    member = null; // 明确标记为Guest，不查询数据库
                } else {
                    member = memberService.selectMemberByUsername(username);
                }
                // 检查是否为Guest用户
                if (member != null) {
                    order.setMemberId(member.getMemberId());
                    // 对于用户提交的订单，设置 sourceType 为 0 (OS)
                    order.setSourceType(0);
                }
                // 如果member为null，说明是Guest用户，不在这里设置memberId
            }
        } else {
            //get member by username (Admin Panel created order)
            member = memberService.selectMemberByUsername(request.getUserName());
        }

        if (member != null) {
            order.setMemberId(member.getMemberId());
            
            // 对于 Admin Panel 创建的订单（isSimpleOrder），直接使用前端传递的 sourceType
            // 对于用户提交的订单，根据 isTg 判断设置为 0 或 1
            if (isSimpleOrder) {
                order.setSourceType(Integer.parseInt(sourceType));
            } else {
                if (isTg) {
                    order.setSourceType(1);
                } else {
                    order.setSourceType(0);
                }
            }
            
            
            // 获取会员信息
            MemberInfo memberInfo = memberService.selectMemberInfoById(member.getMemberId());
            if (memberInfo != null) {
                order.setMemberLevel(memberInfo.getCurrentLevel());
                
                // 只有非简化订单才计算会员折扣
                if (!isSimpleOrder) {
                    // 根据会员等级查询会员权益
                    MemberBenefit memberBenefit = memberBenefitMapper.selectMemberBenefitByLevelId(memberInfo.getCurrentLevel().longValue());
                    if (memberBenefit != null && memberBenefit.getFixedDiscount() != null) {
                        // 设置会员折扣金额
                        BigDecimal discountAmount = memberBenefit.getFixedDiscount();
                        order.setDiscountAmount(discountAmount);
                    }
                }
            } else {
                //Do nothing
                order.setMemberLevel(0);
            }
        } else {
            // 当member为null时，处理临时用户和Guest用户
            if (tgId != null) {
                // TG临时用户：使用负数memberId（-1000000 - tgId）
                Long tempMemberId = -1000000L - tgId;
                order.setMemberId(tempMemberId);
                // 对于用户提交的订单，设置 sourceType 为 1 (TG)
                order.setSourceType(1); // TG来源
                log.info("Created order for TG temporary user: tgId={}, tempMemberId={}", tgId, tempMemberId);
            } else {
                // Web Guest用户 或 Admin Panel 创建的订单
                if (isSimpleOrder) {
                    // Admin Panel 创建的订单：使用前端传递的 sourceType (10/11/12/99)
                    // 使用默认的 memberId，因为 Admin Panel 创建的订单可能没有关联真实用户
                    order.setMemberId(-999999L);
                    order.setSourceType(Integer.parseInt(sourceType));
                    log.info("Created order from Admin Panel: sourceType={}", sourceType);
                } else {
                    // Web Guest用户：使用负数memberId来标识Guest订单
                    // 从username中提取Guest ID并转换为负数
                    if (username != null && username.startsWith("Guest_")) {
                        try {
                            String guestIdStr = username.substring(6); // 去掉"Guest_"前缀
                            Long guestId = Long.parseLong(guestIdStr);
                            // 使用与TG Bot一致的memberId生成逻辑：-1000000 - guestId
                            order.setMemberId(-1000000L - guestId);
                            log.info("Created order for OS Guest user: guestId={}, memberId={}", guestId, -1000000L - guestId);
                        } catch (NumberFormatException e) {
                            // 如果解析失败，使用一个默认的负数
                            order.setMemberId(-999999L);
                            log.warn("Failed to parse guest ID from username: {}, using default memberId: -999999", username);
                        }
                    } else {
                        // 使用默认的负数memberId，避免null值
                        order.setMemberId(-999999L);
                    }
                    order.setSourceType(0); // Web来源
                }
            }
            order.setMemberLevel(0); // 临时用户默认等级为0
        }

        // Initialize total amount to zero
        order.setTotalAmount(BigDecimal.ZERO);
        order.setStatus(0);//default status is 0
        order.setCreateTime(new Date());
        
        // Set remark if provided
        if (request.getRemark() != null && !request.getRemark().trim().isEmpty()) {
            order.setRemark(request.getRemark());
        }
        
        // Set customer comment if provided
        if (request.getCustomerComment() != null && !request.getCustomerComment().trim().isEmpty()) {
            order.setCustomerComment(request.getCustomerComment());
        }
        
        // Check if this is a Bond payment order
        // ✅ 修改：支持多种 BOND SKU 格式
        // 1. BOND-BASE-UNIT 或 BOND-BASE-UNIT- 开头
        // 2. SKU-BOND-BASE- 开头（管理员修改后的格式，如 SKU-BOND-BASE-1000）
        boolean isBondOrder = false;
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderAddRequest.Item item : request.getItems()) {
                String sku = item.getSku();
                if (sku != null) {
                    if (sku.equals("BOND-BASE-UNIT") || 
                        sku.startsWith("BOND-BASE-UNIT-") || 
                        sku.startsWith("SKU-BOND-BASE-")) {
                        isBondOrder = true;
                        break;
                    }
                }
            }
        }
        
        // If Bond order, set Bond-specific fields
        if (isBondOrder) {
            // Try to get bondApplicationId from request first, then from session
            Long bondApplicationId = request.getBondApplicationId();
            String bondApplicationNumber = request.getBondApplicationNumber();
            
            if (bondApplicationId == null) {
                // Try to get from session via RequestContextHolder
                try {
                    jakarta.servlet.http.HttpServletRequest httpRequest = 
                        org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes() instanceof 
                        org.springframework.web.context.request.ServletRequestAttributes ?
                        ((org.springframework.web.context.request.ServletRequestAttributes) 
                         org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest() : null;
                    
                    if (httpRequest != null) {
                        jakarta.servlet.http.HttpSession session = httpRequest.getSession(false);
                        if (session != null) {
                            Object bondAppIdObj = session.getAttribute("bondApplicationId");
                            if (bondAppIdObj instanceof Long) {
                                bondApplicationId = (Long) bondAppIdObj;
                            } else if (bondAppIdObj instanceof Number) {
                                bondApplicationId = ((Number) bondAppIdObj).longValue();
                            }
                            
                            // Also try to get application number from session
                            Object bondAppNumberObj = session.getAttribute("bondApplicationNumber");
                            if (bondAppNumberObj instanceof String) {
                                bondApplicationNumber = (String) bondAppNumberObj;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to get bondApplicationId from session: {}", e.getMessage());
                }
            }
            
            // ✅ 修改：即使 bondApplicationId 为 null，也设置 orderType 为 BOND_PAYMENT
            // 因为订单项中包含 BOND-BASE-UNIT 开头的 SKU，说明这是 BOND 订单
            order.setOrderType("BOND_PAYMENT");
            
            if (bondApplicationId != null) {
                order.setBondApplicationId(bondApplicationId);
                if (bondApplicationNumber != null) {
                    order.setBondApplicationNumber(bondApplicationNumber);
                }
                log.info("[Bond Order] Creating Bond payment order for application: {}", bondApplicationId);
            } else {
                log.warn("[Bond Order] BOND-BASE-UNIT product found but bondApplicationId is missing. " +
                        "OrderType set to BOND_PAYMENT but bondApplicationId is null. " +
                        "This may cause issues with bond payment processing.");
            }
        }
        
        // 只有非简化订单才设置优惠券ID
        if (!isSimpleOrder) {
            order.setCouponId(request.getCouponId() == null ? null : Long.valueOf(request.getCouponId()));
        }

        // calculate freight amount
        if (request.getShippingMethod() != null) {
            ShippingMethod shippingMethod = shippingMethodMapper.selectShippingMethodByCode(request.getShippingMethod());
            if (shippingMethod != null) {
                order.setFreightAmount(shippingMethod.getFee());
            }
        }

        // Calculate total amount for items
        if (request.getItems() != null) {
            for (OrderAddRequest.Item item : request.getItems()) {
                Product2 product = product2Service.selectProduct2BySku(item.getSku());
                if (product == null) {
                    throw new RuntimeException("when didn't find SKU: " + item.getSku() + " in product2 table");
                }
                BigDecimal itemTotal = product.getPrice().multiply(new java.math.BigDecimal(item.getQuantity()));
                order.setTotalAmount(order.getTotalAmount().add(itemTotal));
            }
        }

        // 只有非简化订单才计算折扣
        if (!isSimpleOrder) {
            // 第二阶段：重新计算会员折扣（包括百分比折扣和Free Shipping权益）
            if (order.getMemberLevel() != null && order.getMemberLevel() > 0) {
                MemberBenefit memberBenefit = memberBenefitMapper.selectMemberBenefitByLevelId(order.getMemberLevel().longValue());
                if (memberBenefit != null) {
                    BigDecimal totalDiscountAmount = BigDecimal.ZERO;
                    
                    // 固定折扣
                    if (memberBenefit.getFixedDiscount() != null && memberBenefit.getFixedDiscount().compareTo(BigDecimal.ZERO) > 0) {
                        totalDiscountAmount = totalDiscountAmount.add(memberBenefit.getFixedDiscount());
                    }
                    
                    // 百分比折扣（基于当前商品总价）
                    if (memberBenefit.getPercentDiscount() != null && memberBenefit.getPercentDiscount().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal percentDiscount = order.getTotalAmount().multiply(memberBenefit.getPercentDiscount())
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                        totalDiscountAmount = totalDiscountAmount.add(percentDiscount);
                    }
                    
                    // Free Shipping权益（Platinum和Diamond等级）
                    // 注意：Free Shipping权益不在这里计算，而是在运费添加逻辑中处理
                    // 这里只计算商品折扣，运费和Free Shipping权益相互抵消
                    
                    // 更新折扣金额
                    order.setDiscountAmount(totalDiscountAmount);
                }
            }
            
            //subtract discount amount if exists
            if (order.getDiscountAmount() != null) {
                order.setTotalAmount(order.getTotalAmount().subtract(order.getDiscountAmount()));
            }

            // if order has coupon id, calculate coupon amount
            if (order.getCouponId() != null) {  
                Coupon coupon = couponMapper.selectCouponById(order.getCouponId());
                if (coupon != null) {
                    // according to coupon type to calculate coupon amount  
                    BigDecimal couponAmount = BigDecimal.ZERO;
                    if (coupon.getType() == 1) { // full reduction coupon
                        if (order.getTotalAmount().compareTo(coupon.getMinPoint()) >= 0) {
                            couponAmount = coupon.getAmount();      
                        }
                    } else if (coupon.getType() == 2) { // discount
                        // 修复：直接使用折扣比例计算折扣金额
                        couponAmount = order.getTotalAmount().multiply(coupon.getDiscount())
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                    } else if (coupon.getType() == 3) { // free shipping
                        // Free shipping should equal the freight amount
                        couponAmount = order.getFreightAmount() != null ? order.getFreightAmount() : BigDecimal.ZERO;
                    }
                    
                    // set coupon amount
                    order.setCouponAmount(couponAmount);
                }
            }

            // Subtract coupon amount if exists
            if (order.getCouponAmount() != null) {
                order.setTotalAmount(order.getTotalAmount().subtract(order.getCouponAmount()));
            }
        }

        // Add freight amount to total (always add, then offset with member free shipping benefit)
        if (order.getFreightAmount() != null) {
            // Always add freight amount first
            order.setTotalAmount(order.getTotalAmount().add(order.getFreightAmount()));
            
            // Then check if member has free shipping benefit and offset it
            if (order.getMemberLevel() != null && order.getMemberLevel() > 0) {
                MemberBenefit memberBenefit = memberBenefitMapper.selectMemberBenefitByLevelId(order.getMemberLevel().longValue());
                if (memberBenefit != null && memberBenefit.getShippingDiscount() != null && 
                    memberBenefit.getShippingDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    // Member has free shipping benefit, offset the freight amount
                    order.setTotalAmount(order.getTotalAmount().subtract(order.getFreightAmount()));
                }
            }
        }

        if (isSimpleOrder) {
            order.setTotalAmount(request.getTotal());
        }

        // Set vendor_id if order contains vendor products
        if (hasVendorProduct && vendorId != null) {
            order.setVendorId(vendorId.longValue());
            
            // Check vendor sales limit before creating order
            BigDecimal orderAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            if (!vendorBondService.checkSalesLimit(vendorId.longValue(), orderAmount)) {
                throw new RuntimeException("Order exceeds vendor sales limit. Maximum allowed: $" + 
                    vendorBondService.calculateMaxSalesLimit(vendorId.longValue()));
            }
        }
        
        // First insert the order
        int rows = orderMapper.insertOrder(order);
        if (rows <= 0) throw new RuntimeException("create order failed");

        // Then insert order items
        if (request.getItems() != null) {
            for (OrderAddRequest.Item item : request.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setId(UUID.randomUUID().toString());
                orderItem.setOrderId(orderNumber);
                orderItem.setOrderSn(orderNumber);
                orderItem.setSku(item.getSku());
                
                // get product info by sku
                Product2 product2 = product2Service.selectProduct2BySku(item.getSku());
                if (product2 == null) {
                    throw new RuntimeException("when didn't find SKU: " + item.getSku() + " in product2 table");
                }
                
                // get product master info
                Product product = productService.selectProductByProductId(product2.getProductId());
                if (product == null) {
                    throw new RuntimeException("when didn't find product: " + product2.getProductId() + " in product table");
                }
                
                orderItem.setProductId(product2.getProductId());
                orderItem.setProductSpec(product2.getModel().setScale(2, java.math.RoundingMode.HALF_UP).toString());
                orderItem.setProductName(product.getName());
                orderItem.setProductImage(product.getImageUrl());
                
                // Set product origin and origin_id
                if (product.isVendorProduct()) {
                    orderItem.setProductOrigin(1);
                    orderItem.setOriginId(product.getOriginId());
                } else {
                    orderItem.setProductOrigin(0);
                    orderItem.setOriginId(null);
                }
                
                orderItem.setQuantity(item.getQuantity());
                
                orderItem.setPrice(product2.getPrice());
                orderItem.setTotalPrice(product2.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())));
                // Set price and total price based on order type
                if (isSimpleOrder) {
                    // For simple orders, get price directly from request
                    // totalPrice is in coin in this case
                    log.info("Simple order - Setting totalCoin from item.getTotalPrice(): {}", item.getTotalPrice());
                    orderItem.setTotalCoin(item.getTotalPrice());
                    log.info("OrderItem totalCoin set to: {}", orderItem.getTotalCoin());
                } else {
                    // For normal orders, get price from product and calculate
                    orderItem.setTotalCoin(BigDecimal.ZERO); 
                }

                // Initialize totalCoin to zero, will be updated later with real-time exchange rate
                
                orderItem.setCreateTime(new Date());
                
                int itemRows = orderItemMapper.insertOrderItem(orderItem);
                if (itemRows <= 0) throw new RuntimeException("create order item failed");
            }
        }

        // shipping address
        ShippingAddress addr = request.getShippingAddress();
        if (addr != null) {
            addr.setId(java.util.UUID.randomUUID().toString());
            addr.setOrderId(orderNumber);

            // 保持原有的remark字段（如果设置了的话）
            String originalRemark = addr.getRemark();
            
            // 检查是否是加密地址
            if (originalRemark != null && originalRemark.contains("PGP Encrypted Address:")) {
                try {
                    // 提取加密的地址数据
                    String encryptedData = originalRemark.replace("PGP Encrypted Address:", "").trim();
                    
                    // 获取私钥（优先使用默认私钥，如果没有则使用第一个激活的私钥）
                    PgpKey privateKey = null;
                    
                    // 先尝试获取默认密钥，如果是私钥就使用
                    PgpKey defaultKey = pgpKeyService.selectDefaultPgpKey();
                    if (defaultKey != null && "private".equals(defaultKey.getKeyType())) {
                        privateKey = defaultKey;
                    } else {
                        // 如果没有默认私钥，获取第一个激活的私钥
                        List<PgpKey> activeKeys = pgpKeyService.selectActivePgpKeys();
                        for (PgpKey key : activeKeys) {
                            if ("private".equals(key.getKeyType())) {
                                privateKey = key;
                                break;
                            }
                        }
                    }
                    
                    if (privateKey != null) {
                        // 使用私钥解密
                        String decryptedAddress = com.medusa.mall.utils.PgpEncryptionUtil.decrypt(encryptedData, privateKey.getKeyData());
                        
                        // 解析解密后的地址信息（假设格式为JSON或特定格式）
                        // 这里需要根据实际的地址格式来解析
                        String[] addressParts = parseDecryptedAddress(decryptedAddress);
                        
                        // 设置解密后的地址信息
                        addr.setName(addressParts[0]); // name
                        addr.setAddress1(addressParts[1]); // address1
                        addr.setAddress2(addressParts[2]); // address2 (可选)
                        addr.setAddress3(addressParts[3]); // address3 (可选)
                        addr.setCity(addressParts[4]); // city
                        addr.setState(addressParts[5]); // state
                        addr.setPostalCode(addressParts[6]); // postalCode
                        addr.setCountry(addressParts[7]); // country
                        
                        // 保留原始加密数据在remark中，但添加解密标识
                        addr.setRemark("PGP Decrypted Address (Original: " + originalRemark + ")");
                    } else {
                        // 如果解密失败，保持原有信息
                        addr.setRemark(originalRemark);
                    }
                } catch (Exception e) {
                    // 如果解密失败，保持原有信息
                    addr.setRemark(originalRemark);
                }
            } else {
                // 普通地址，直接设置
                addr.setName(addr.getName());
                addr.setAddress1(addr.getAddress1());
                addr.setAddress2(addr.getAddress2());
                addr.setCity(addr.getCity());
                addr.setState(addr.getState());
                addr.setPostalCode(addr.getPostalCode());
                addr.setCountry(addr.getCountry());
                
                // 恢复remark字段
                if (originalRemark != null && !originalRemark.trim().isEmpty()) {
                    addr.setRemark(originalRemark);
                }
            }

            addr.setShippingMethod(request.getShippingMethod());

            int addrRows = shippingAddressMapper.insertShippingAddress(addr);
            if (addrRows <= 0) throw new RuntimeException("Insert shipping address failed");
        }

        // create payment record
        BigDecimal totalAmount = order.getTotalAmount();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrderId(orderNumber);
        payment.setAmount(totalAmount);
        payment.setPayStatus(0);

        // Convert PayType string to currency code using CurrencyEnum
        if (request.getPayType() != null) {
            Integer currencyCode = CurrencyEnum.getCodeByName(request.getPayType());
            if (currencyCode != null) {
                payment.setPayType(currencyCode.toString());
            } else {
                // If currency not found, keep original value and log warning
                payment.setPayType(request.getPayType());
            }
        } else {
            payment.setPayType(null);
        }
        
        payment.setPayTime(new Date());
        //set currency
        payment.setCurrency(request.getCurrency());
        
        // use PaymentService to create payment record
        Payment createdPayment = paymentService.createPayment(payment);
        if (createdPayment == null) {
            throw new RuntimeException("Failed to create payment");
        }
        
        // Update order item total coin using new formula based on member level and coupon type
        log.info("Checking if need to update totalCoin with exchange rate - Rate: {}, PayType: {}", 
                 createdPayment.getRate(), createdPayment.getPayType());
        if (createdPayment.getRate() != null && request.getItems() != null) {
            log.info("Exchange rate found, will update order items totalCoin with new formula");
            BigDecimal exchangeRate = createdPayment.getRate();
            
            // Get all order items for this order
            List<OrderItem> orderItems = orderItemMapper.selectOrderItemsByOrderId(orderNumber);
            
            // Use the existing order object (already created and inserted to database)
            // Calculate total item count (N = number of products), not total quantity
            int totalItemCount = orderItems.size(); // Number of distinct order items (products)
            BigDecimal totalProductAmount = BigDecimal.ZERO; // A (total product AUD)
            for (OrderItem item : orderItems) {
                totalProductAmount = totalProductAmount.add(item.getTotalPrice());
            }
            
            // Get shipping amount (S)
            BigDecimal S = order.getFreightAmount() != null ? order.getFreightAmount() : BigDecimal.ZERO;
            
            // Get member level and benefit from database
            Integer memberLevel = order.getMemberLevel() != null ? order.getMemberLevel() : 1; // Default Bronze
            MemberBenefit memberBenefit = null;
            BigDecimal fixedDiscount = BigDecimal.ZERO; // Fixed discount from member benefit
            BigDecimal percentDiscount = BigDecimal.ZERO; // Percent discount from member benefit (already divided by 100)
            boolean hasFreeShipping = false; // Free shipping from member benefit
            
            if (memberLevel > 0) {
                memberBenefit = memberBenefitMapper.selectMemberBenefitByLevelId(memberLevel.longValue());
                if (memberBenefit != null) {
                    fixedDiscount = memberBenefit.getFixedDiscount() != null ? 
                                   memberBenefit.getFixedDiscount() : BigDecimal.ZERO;
                    percentDiscount = memberBenefit.getPercentDiscount() != null ? 
                                     memberBenefit.getPercentDiscount() : BigDecimal.ZERO;
                    hasFreeShipping = memberBenefit.getShippingDiscount() != null && 
                                     memberBenefit.getShippingDiscount().compareTo(BigDecimal.ZERO) > 0;
                    log.info("Member level {}: fixedDiscount={}, percentDiscount={}, hasFreeShipping={}", 
                            memberLevel, fixedDiscount, percentDiscount, hasFreeShipping);
                }
            }
            
            // Get coupon information
            Coupon coupon = null;
            Integer couponType = null; // 1=GF, 2=GP, 3=GS
            BigDecimal GF = BigDecimal.ZERO; // Giftcard Fixed amount
            BigDecimal GP = BigDecimal.ZERO; // Giftcard Percentage (already divided by 100)
            boolean hasGS = false; // Giftcard Shipping Free
            
            if (order.getCouponId() != null) {
                coupon = couponMapper.selectCouponById(order.getCouponId());
                if (coupon != null) {
                    couponType = coupon.getType();
                    if (couponType == 1) { // Fixed amount
                        GF = coupon.getAmount();
                        log.info("Coupon type 1 (Fixed): GF={}", GF);
                    } else if (couponType == 2) { // Percentage
                        GP = coupon.getDiscount(); // Already in decimal form (e.g., 0.1 = 10%)
                        log.info("Coupon type 2 (Percentage): GP={}", GP);
                    } else if (couponType == 3) { // Free shipping
                        hasGS = true;
                        log.info("Coupon type 3 (Free Shipping): hasGS=true");
                    }
                }
            }
            
            // Check if this is USDT payment with currency conversion
            boolean isUsdtPayment = "1".equals(payment.getPayType());
            boolean hasConversion = "AUD".equalsIgnoreCase(payment.getCurrency());
            
            // Calculate total coin for each order item
            for (OrderItem orderItem : orderItems) {
                BigDecimal itemTotalCoin;
                BigDecimal A = orderItem.getTotalPrice(); // Current item product amount (AUD)
                
                // For simple orders, use totalCoin directly without calculation
                if (isSimpleOrder) {
                    itemTotalCoin = orderItem.getTotalCoin();
                    log.info("Simple order - Using totalCoin directly: {}", itemTotalCoin);
                } else {
                    // Calculate base amount based on coupon type and member level
                    BigDecimal baseAmount;
                    
                    if (couponType != null && couponType == 1) {
                        // Giftcard (Fixed amount) - GF
                        // Calculate per-product allocation (not per-item quantity)
                        BigDecimal shippingPerProduct = totalItemCount > 0 ? 
                            S.divide(new BigDecimal(totalItemCount), 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        BigDecimal fixedDiscountPerProduct = totalItemCount > 0 ? 
                            fixedDiscount.divide(new BigDecimal(totalItemCount), 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        BigDecimal couponPerProduct = totalItemCount > 0 ? 
                            GF.divide(new BigDecimal(totalItemCount), 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        
                        if (percentDiscount.compareTo(BigDecimal.ZERO) > 0) {
                            // Platinum or Diamond: A × (1 - percent_discount) - (GF/N)
                            BigDecimal discountFactor = BigDecimal.ONE.subtract(percentDiscount);
                            baseAmount = A.multiply(discountFactor).subtract(couponPerProduct);
                            log.debug("Platinum/Diamond with GF: A={} × {} - GF/{}={} = {}", 
                                    A, discountFactor, totalItemCount, couponPerProduct, baseAmount);
                        } else {
                            // Bronze, Silver, Gold: (A + S/N) - (fixed_discount/N) - (GF/N)
                            baseAmount = A.add(shippingPerProduct).subtract(fixedDiscountPerProduct).subtract(couponPerProduct);
                            log.debug("Bronze/Silver/Gold with GF: A={} + S/{}={} - fixed/{}={} - GF/{}={} = {}", 
                                    A, totalItemCount, shippingPerProduct, totalItemCount, fixedDiscountPerProduct,
                                    totalItemCount, couponPerProduct, baseAmount);
                        }
                        
                    } else if (couponType != null && couponType == 2) {
                        // Giftcard (Percentage) - GP
                        // Calculate per-product allocation (not per-item quantity)
                        BigDecimal shippingPerProduct = totalItemCount > 0 ? 
                            S.divide(new BigDecimal(totalItemCount), 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        BigDecimal fixedDiscountPerProduct = totalItemCount > 0 ? 
                            fixedDiscount.divide(new BigDecimal(totalItemCount), 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        
                        if (percentDiscount.compareTo(BigDecimal.ZERO) > 0) {
                            // Platinum or Diamond: (A × (1 - percent_discount)) × GP
                            BigDecimal discountFactor = BigDecimal.ONE.subtract(percentDiscount);
                            baseAmount = A.multiply(discountFactor).multiply(GP);
                            log.debug("Platinum/Diamond with GP: A={} × {} × GP={} = {}", A, discountFactor, GP, baseAmount);
                        } else {
                            // Bronze, Silver, Gold: ((A + S/N) - (fixed_discount/N)) × GP
                            baseAmount = A.add(shippingPerProduct).subtract(fixedDiscountPerProduct).multiply(GP);
                            log.debug("Bronze/Silver/Gold with GP: (A={} + S/{}={} - fixed/{}={}) × GP={} = {}", 
                                    A, totalItemCount, shippingPerProduct, totalItemCount, fixedDiscountPerProduct, GP, baseAmount);
                        }
                        
                    } else if (hasGS || (couponType != null && couponType == 3)) {
                        // Giftcard (Shipping Free) - GS
                        // Calculate per-product allocation (not per-item quantity)
                        BigDecimal fixedDiscountPerProduct = totalItemCount > 0 ? 
                            fixedDiscount.divide(new BigDecimal(totalItemCount), 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        
                        if (percentDiscount.compareTo(BigDecimal.ZERO) > 0) {
                            // Platinum or Diamond: A × (1 - percent_discount)
                            BigDecimal discountFactor = BigDecimal.ONE.subtract(percentDiscount);
                            baseAmount = A.multiply(discountFactor);
                            log.debug("Platinum/Diamond with GS: A={} × {} = {}", A, discountFactor, baseAmount);
                        } else {
                            // Bronze, Silver, Gold: A - (fixed_discount/N)
                            baseAmount = A.subtract(fixedDiscountPerProduct);
                            log.debug("Bronze/Silver/Gold with GS: A={} - fixed/{}={} = {}", 
                                    A, totalItemCount, fixedDiscountPerProduct, baseAmount);
                        }
                        
                    } else {
                        // No coupon
                        // Calculate per-product allocation (not per-item quantity)
                        BigDecimal shippingPerProduct = (hasFreeShipping || hasGS) ? BigDecimal.ZERO : 
                            (totalItemCount > 0 ? S.divide(new BigDecimal(totalItemCount), 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);
                        BigDecimal fixedDiscountPerProduct = totalItemCount > 0 ? 
                            fixedDiscount.divide(new BigDecimal(totalItemCount), 8, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        
                        if (percentDiscount.compareTo(BigDecimal.ZERO) > 0) {
                            // Platinum or Diamond: A × (1 - percent_discount)
                            BigDecimal discountFactor = BigDecimal.ONE.subtract(percentDiscount);
                            baseAmount = A.multiply(discountFactor);
                            log.debug("Platinum/Diamond (no coupon): A={} × {} = {}", A, discountFactor, baseAmount);
                        } else {
                            // Bronze, Silver, Gold: (A + S/N) - (fixed_discount/N)
                            baseAmount = A.add(shippingPerProduct).subtract(fixedDiscountPerProduct);
                            log.debug("Bronze/Silver/Gold (no coupon): A={} + S/{}={} - fixed/{}={} = {}", 
                                    A, totalItemCount, shippingPerProduct, totalItemCount, fixedDiscountPerProduct, baseAmount);
                        }
                    }
                    
                    // Convert to coin amount
                    if (isUsdtPayment && hasConversion) {
                        // USDT payment: AUD * rate = USDT
                        itemTotalCoin = baseAmount.multiply(exchangeRate).setScale(8, java.math.RoundingMode.HALF_UP);
                        log.info("USDT conversion for item {}: {} AUD * {} rate = {} USDT", 
                                orderItem.getSku(), baseAmount, exchangeRate, itemTotalCoin);
                    } else {
                        // Other payment types: AUD / rate = Coin amount
                        itemTotalCoin = baseAmount.divide(exchangeRate, 8, java.math.RoundingMode.HALF_UP);
                        log.info("Coin conversion for item {}: {} AUD / {} rate = {} coin", 
                                orderItem.getSku(), baseAmount, exchangeRate, itemTotalCoin);
                    }
                }
                
                orderItem.setTotalCoin(itemTotalCoin);
                
                // Update the order item in database
                int updateRows = orderItemMapper.updateOrderItem(orderItem);
                if (updateRows <= 0) {
                    throw new RuntimeException("Failed to update order item total coin");
                }
            }
        } else {
            log.info("No exchange rate found (rate is null) - For LTC and manual payments, totalCoin was already set correctly during order item creation");
        }

        // Lock inventory after order creation
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<Map<String, Object>> itemsForLock = new ArrayList<>();
            for (OrderAddRequest.Item item : request.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("sku", item.getSku());
                itemMap.put("quantity", item.getQuantity());
                itemsForLock.add(itemMap);
            }
            
            boolean lockSuccess = inventoryLockService.lockInventory(orderNumber, itemsForLock);
            if (!lockSuccess) {
                throw new RuntimeException("Failed to lock inventory for order: " + orderNumber);
            }
        }

        // 如果订单使用了优惠券，增加优惠券使用次数
        if (order.getCouponId() != null) {
            try {
                // 1. 增加优惠券使用次数
                int updateResult = couponMapper.incrementUsedCount(order.getCouponId());
                if (updateResult > 0) {
                } else {
                }
                
                // 2. 向优惠券历史记录表添加记录
                CouponHistory couponHistory = new CouponHistory();
                couponHistory.setCouponId(order.getCouponId());
                couponHistory.setMemberId(order.getMemberId());
                couponHistory.setOrderId(Long.valueOf(order.getId())); // 订单ID
                couponHistory.setOrderSn(order.getOrderSn()); // 订单编号
                couponHistory.setUseStatus(1); // 1表示已使用
                couponHistory.setUseTime(new Date()); // 使用时间
                couponHistory.setCreateTime(new Date());
                couponHistory.setUpdateTime(new Date());
                couponHistory.setRemark("Order created with coupon");
                
                int historyResult = couponHistoryMapper.insertCouponHistory(couponHistory);
                if (historyResult > 0) {
                } else {
                }
                
            } catch (Exception e) {
                // 不抛出异常，避免影响订单创建流程
            }
        }

        return createdPayment;
    }

    /**
     * 插入订单
     * 
     * @param order 订单信息
     * @return 结果
     */
    @Override
    public int insertOrder(Order order) {
        return orderMapper.insertOrder(order);
    }

    @Override
    public Order selectOrderById(String id) {
        return orderMapper.selectOrderById(id);
    }

    @Override
    @Transactional
    public int updateOrder(Order order) {
        // 如果订单状态更新为已支付(1)，同时更新支付状态
        if (order.getStatus() != null && order.getStatus() == 1) {
            // 获取订单的旧状态
            Order oldOrder = orderMapper.selectOrderById(order.getId());
            Integer oldStatus = oldOrder != null ? oldOrder.getStatus() : null;
            
            Payment payment = paymentService.getPaymentByOrderId(order.getId());
            if (payment != null) {
                // ✅ 调试日志：打印payment检查前的状态
                log.info("=== OrderServiceImpl.updateOrder - Payment check - orderId: {}, payStatus: {}, paidcoin: {}, remark: {} ===",
                        order.getId(), payment.getPayStatus(), payment.getPaidcoin(), payment.getRemark());
                
                // ✅ 检查支付是否已经被PaymentMonitorService正确处理过
                // 条件：payStatus == 1 且 paidcoin > 0 且 remark包含"BTCPay Monitor"或"NOWPayments"
                boolean isProcessedByMonitor = payment.getPayStatus() == 1 
                        && payment.getPaidcoin() != null 
                        && payment.getPaidcoin().compareTo(BigDecimal.ZERO) > 0
                        && payment.getRemark() != null
                        && (payment.getRemark().contains("BTCPay Monitor") 
                            || payment.getRemark().contains("NOWPayments")
                            || payment.getRemark().contains("BTCPay Webhook"));
                
                if (isProcessedByMonitor) {
                    log.info("Payment already processed by monitor for order: {}, paidcoin: {}, skipping duplicate payment processing", 
                            order.getId(), payment.getPaidcoin());
                    // 支付已经处理完成，只更新订单状态，不重复处理支付和积分
                    // 但是仍然需要检查 Bond 订单，以防 PaymentMonitorService 没有正确处理
                    int updateResult = orderMapper.updateOrder(order);
                    if (oldOrder != null && "BOND_PAYMENT".equals(oldOrder.getOrderType()) && oldOrder.getBondApplicationId() != null) {
                        // 检查是否已经更新过（幂等性检查）
                        if (vendorApplicationService != null) {
                            try {
                                VendorApplication app = vendorApplicationService.selectVendorApplicationById(oldOrder.getBondApplicationId());
                                if (app != null && (app.getBondOrderId() == null || !app.getBondOrderId().equals(oldOrder.getId()))) {
                                    log.info("[Bond Payment] Monitor processed payment but application not updated, updating now: order {}, application {}", 
                                            oldOrder.getId(), oldOrder.getBondApplicationId());
                                    processBondPaymentComplete(oldOrder);
                                }
                            } catch (Exception e) {
                                log.warn("[Bond Payment] Failed to check/update bond payment for order {}: {}", oldOrder.getId(), e.getMessage());
                            }
                        }
                    }
                    return updateResult;
                }
                
                // ✅ 调试日志：如果继续处理，说明是管理员手动操作
                log.info("=== OrderServiceImpl.updateOrder - Manual payment processing - orderId: {} ===", order.getId());
                
                // 直接更新支付状态，避免重复计算paidcoin
                payment.setPayStatus(1); // 1 means paid
                payment.setPayTime(new Date());
                
                // ✅ 设置paidcoin为换算后的币种金额（管理员手动标记时需要根据汇率换算）
                if (payment.getPaidcoin() == null || payment.getPaidcoin().compareTo(BigDecimal.ZERO) == 0) {
                    // 根据支付类型和汇率计算实际的币种金额
                    BigDecimal calculatedPaidcoin = calculatePaidCoinFromAmount(payment);
                    payment.setPaidcoin(calculatedPaidcoin);
                    log.info("Setting paidcoin to calculated amount: {} for manual payment (original amount: {})", 
                            calculatedPaidcoin, payment.getAmount());
                }
                
                // 检查是否是从Cancelled状态重新激活
                if (oldStatus != null && oldStatus == 4) {
                    log.info("Reactivating cancelled order to Paid, Order ID: {}", order.getId());
                    payment.setRemark("Order reactivated from cancelled status by admin");
                    
                    // 重新激活订单：需要重新锁定库存
                    List<OrderItem> orderItems = orderItemMapper.selectOrderItemsByOrderId(order.getId());
                    if (orderItems != null && !orderItems.isEmpty()) {
                        List<Map<String, Object>> itemsForLock = new ArrayList<>();
                        for (OrderItem item : orderItems) {
                            Map<String, Object> itemMap = new HashMap<>();
                            itemMap.put("sku", item.getSku());
                            itemMap.put("quantity", item.getQuantity());
                            itemsForLock.add(itemMap);
                        }
                        
                        // 重新锁定库存（会检查库存并扣减）
                        boolean lockSuccess = inventoryLockService.lockInventory(order.getId(), itemsForLock);
                        if (!lockSuccess) {
                            log.error("Failed to lock inventory for reactivated order: {}", order.getId());
                            throw new RuntimeException("Insufficient inventory, cannot reactivate order. Order ID: " + order.getId());
                        }
                        log.info("Successfully locked inventory for reactivated order: {}", order.getId());
                    }
                    
                } else {
                    // 正常支付流程
                    payment.setRemark("Payment marked as paid by admin");
                    
                    // 确认库存扣减（支付成功）
                    boolean inventoryConfirmed = inventoryLockService.confirmInventory(order.getId());
                    if (inventoryConfirmed) {
                        log.info("Successfully confirmed inventory for order: {}", order.getId());
                    } else {
                        log.warn("Failed to confirm inventory for order: {}", order.getId());
                    }
                }
                
                // 直接更新数据库，不调用updatePaymentStatus避免重复计算paidcoin
                paymentMapper.updatePayment(payment);
                
                // 管理员手动标记订单为已支付时添加积分（适用于其他平台支付场景）
                // 幂等性检查已在addPointHistoryForOrder中实现，可以安全调用
                // ✅ 积分计算应该基于AUD金额，而不是paidcoin（paidcoin是币种金额）
                BigDecimal amountForPoints = payment.getAmount(); // 始终使用订单的AUD金额
                
                // ✅ 调试日志：打印即将传递给积分处理的金额
                log.info("=== OrderServiceImpl.updateOrder - Calling processMemberPointsAndLevel - orderId: {}, payment.paidcoin: {}, payment.amount: {}, amountForPoints (AUD): {}, scale: {} ===",
                        order.getId(), payment.getPaidcoin(), payment.getAmount(), amountForPoints, amountForPoints.scale());
                
                processMemberPointsAndLevel(oldOrder, amountForPoints);
                
                // 处理PCSP数字产品激活
                processPcspPurchase(oldOrder);
                
                // 检查是否是 Bond 支付订单，如果是则更新 Vendor Application
                if (oldOrder != null && "BOND_PAYMENT".equals(oldOrder.getOrderType()) && oldOrder.getBondApplicationId() != null) {
                    log.info("[Bond Payment] Admin manually marked Bond order as paid: {}, applicationId: {}", 
                            oldOrder.getId(), oldOrder.getBondApplicationId());
                    processBondPaymentComplete(oldOrder);
                }
            }
        }
        
        // 如果订单状态更新为 Shipped (3)，且是 Vendor 订单，需要更新 Sales Points 和 Pending Balance
        if (order.getStatus() != null && order.getStatus() == 3) {
            // 获取原始订单信息
            Order oldOrder = orderMapper.selectOrderById(order.getId());
            if (oldOrder != null) {
                Integer oldStatus = oldOrder.getStatus();
                Long vendorId = oldOrder.getVendorId();
                
                // 检查是否是从其他状态更新为 Shipped，且是 Vendor 订单，且尚未处理
                if (oldStatus != null && oldStatus != 3 && vendorId != null && vendorWithdrawalService != null) {
                    // 检查是否已经处理过（通过 balance_available_date 判断）
                    if (oldOrder.getBalanceAvailableDate() == null) {
                        BigDecimal orderAmount = oldOrder.getTotalAmount();
                        if (orderAmount != null && orderAmount.compareTo(BigDecimal.ZERO) > 0) {
                            try {
                                log.info("Vendor order {} shipped via admin panel, updating sales points and pending balance", order.getId());
                                
                                // 更新销售点数和等级
                                vendorBondService.updateSalesPointsAndLevel(vendorId, order.getId(), orderAmount);
                                
                                // 添加待确认余额
                                vendorWithdrawalService.addPendingBalanceOnShipment(vendorId, Long.parseLong(order.getId()), orderAmount);
                                
                                log.info("Successfully updated vendor metrics for order {}", order.getId());
                            } catch (Exception e) {
                                log.error("Failed to update vendor metrics for order {}: {}", order.getId(), e.getMessage(), e);
                                // 不阻止订单状态更新，但记录错误
                            }
                        }
                    } else {
                        log.debug("Order {} already processed for vendor metrics (balance_available_date is set), skipping", order.getId());
                    }
                }
            }
        }
        
        // 如果订单有优惠券ID，计算优惠金额
        if (order.getCouponId() != null) {
            Coupon coupon = couponService.selectCouponById(order.getCouponId());
            if (coupon != null) {
                // 根据优惠券类型计算优惠金额
                BigDecimal couponAmount = BigDecimal.ZERO;
                if (coupon.getType() == 1) { // 满减券
                    if (order.getTotalAmount().compareTo(coupon.getMinPoint()) >= 0) {
                        couponAmount = coupon.getAmount();
                    }
                } else if (coupon.getType() == 2) { // discount
                    BigDecimal discount = BigDecimal.ONE.subtract(coupon.getDiscount());
                    couponAmount = order.getTotalAmount().multiply(discount);
                } else if (coupon.getType() == 3) { // free shipping
                    couponAmount = coupon.getAmount();
                }
                
                // 设置优惠金额
                order.setCouponAmount(couponAmount);
            }
        }
        return orderMapper.updateOrder(order);
    }

    @Override
    public int deleteOrderByIds(String[] ids) {
        return orderMapper.deleteOrderByIds(ids);
    }

    @Override
    public Object getOrderDetailById(String id) {
        Order order = orderMapper.selectOrderById(id);
        List<OrderItem> items = orderItemMapper.selectOrderItemsByOrderId(id);
        ShippingAddress shipping = shippingAddressMapper.selectShippingAddressByOrderId(id);
        
        // Get payment details
        Payment payment = paymentMapper.selectPaymentByOrderId(id);
        
        // Get member info if memberId exists
        Object member = null;
        if (order != null && order.getMemberId() != null) {
            try {
                member = memberService.selectMemberInfoById(order.getMemberId());
            } catch (Exception e) {
                log.error("Error getting member info for memberId: {}", order.getMemberId(), e);
            }
        }
        
        // Get shipping method name from database if available
        if (shipping != null && shipping.getShippingMethod() != null) {
            try {
                com.medusa.mall.domain.order.ShippingMethod shippingMethod = 
                    shippingMethodService.selectShippingMethodByCode(shipping.getShippingMethod());
                if (shippingMethod != null && shippingMethod.getName() != null) {
                    // Store the name in remark field temporarily for display (or add a new field)
                    // Actually, let's add shippingMethodName to the result
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("order", order);
                    result.put("items", items);
                    result.put("shipping", shipping);
                    result.put("payment", payment);
                    result.put("member", member);
                    result.put("shippingMethodName", shippingMethod.getName());
                    return result;
                }
            } catch (Exception e) {
                log.error("Error getting shipping method name for code: {}", shipping.getShippingMethod(), e);
            }
        }
        
        HashMap<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        result.put("shipping", shipping);
        result.put("payment", payment);
        result.put("member", member);
        return result;
    }


    public int updateShippingInfoByOrderId(ShippingAddress shippingAddress) {
        // 更新订单表中的备注
        Order order = new Order();
        order.setId(shippingAddress.getOrderId());
        order.setRemark(shippingAddress.getRemark());
        int rows = orderMapper.updateOrder(order);
        
        // 更新物流表中的运单号
        if (shippingAddress.getShippingNumber() != null) {
            // 设置发货时间为当前时间
            shippingAddress.setShippingTime(new java.util.Date());
            rows += shippingAddressMapper.updateShippingInfoByOrderId(shippingAddress);
        }
        
        return rows;
    }

    @Override
    public int resolveDispute(String id) {
        Order order = new Order();
        order.setId(id);
        order.setIsdispute(0); // 设置为0表示解决争议
        return orderMapper.updateOrder(order);
    }
    
    /**
     * 解析解密后的地址信息
     * 支持多种格式：
     * - 换行符格式（推荐）: name\naddress1\naddress2\naddress3\ncity\nstate\npostal
     * - 分隔符格式: name|address1|address2|address3|city|state|postal|country
     * - JSON 格式: {"name":"...", "address1":"...", ...}
     * 
     * @param decryptedAddress 解密后的地址字符串
     * @return String[8]: [name, address1, address2, address3, city, state, postal, country]
     */
    private String[] parseDecryptedAddress(String decryptedAddress) {
        try {
            // 尝试解析JSON格式
            if (decryptedAddress.startsWith("{") && decryptedAddress.endsWith("}")) {
                return parseJsonAddress(decryptedAddress);
            }
            
            // 尝试解析分隔符格式
            if (decryptedAddress.contains("|")) {
                String[] parts = decryptedAddress.split("\\|");
                // 确保至少有 8 个元素
                String[] result = new String[8];
                for (int i = 0; i < 8; i++) {
                    result[i] = i < parts.length ? parts[i] : "";
                }
                // 如果没有 country，默认设置为 AU
                if (result[7].isEmpty()) {
                    result[7] = "AU";
                }
                return result;
            }
            
            // 尝试解析换行符格式（推荐格式）
            if (decryptedAddress.contains("\n")) {
                return parseNewlineAddress(decryptedAddress);
            }
            
            // 默认格式：假设是简单的文本格式
            return parseSimpleAddress(decryptedAddress);
            
        } catch (Exception e) {
            // 返回默认值
            log.error("解析解密地址失败", e);
            return new String[]{"Encrypted User", "Encrypted Address", "", "", "Encrypted City", "Encrypted State", "Encrypted Postal", "AU"};
        }
    }
    
    /**
     * 解析JSON格式的地址
     */
    private String[] parseJsonAddress(String jsonAddress) {
        try {
            // 简单的JSON解析（这里可以根据需要扩展）
            String[] parts = new String[8];
            parts[0] = "Encrypted User"; // name
            parts[1] = "Encrypted Address"; // address1
            parts[2] = ""; // address2
            parts[3] = ""; // address3
            parts[4] = "Encrypted City"; // city
            parts[5] = "Encrypted State"; // state
            parts[6] = "Encrypted Postal"; // postalCode
            parts[7] = "AU"; // country
            
            // 这里可以添加更复杂的JSON解析逻辑
            return parts;
        } catch (Exception e) {
            return new String[]{"Encrypted User", "Encrypted Address", "", "", "Encrypted City", "Encrypted State", "Encrypted Postal", "AU"};
        }
    }
    
    /**
     * 解析换行符分隔的地址
     * 支持 5、6、7 行格式：
     * - 5 行: name, address1, city, state, postal (address2 和 address3 为空)
     * - 6 行: name, address1, address2, city, state, postal (address3 为空)
     * - 7 行: name, address1, address2, address3, city, state, postal
     * 
     * 逻辑：最后 3 行总是 city, state, postal
     *       第 1 行总是 name
     *       第 2 行总是 address1
     *       中间的行（如果有）是 address2 和/或 address3
     * 
     * @return String[8]: [name, address1, address2, address3, city, state, postal, country]
     */
    private String[] parseNewlineAddress(String newlineAddress) {
        try {
            String[] lines = newlineAddress.split("\n");
            String[] parts = new String[8]; // 修改为 8 个元素
            
            // 清理每行数据并过滤空行
            java.util.List<String> nonEmptyLines = new java.util.ArrayList<>();
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    nonEmptyLines.add(trimmed);
                }
            }
            
            int lineCount = nonEmptyLines.size();
            
            if (lineCount < 5) {
                // 少于 5 行，格式不完整，返回默认值
                log.warn("解密地址行数不足，期望至少 5 行，实际 {} 行", lineCount);
                return new String[]{"Encrypted User", "Encrypted Address", "", "", "Encrypted City", "Encrypted State", "Encrypted Postal", "AU"};
            }
            
            // 根据行数判断格式
            if (lineCount == 5) {
                // 5 行格式: name, address1, city, state, postal
                parts[0] = nonEmptyLines.get(0); // name
                parts[1] = nonEmptyLines.get(1); // address1
                parts[2] = ""; // address2 (空)
                parts[3] = ""; // address3 (空)
                parts[4] = nonEmptyLines.get(2); // city
                parts[5] = nonEmptyLines.get(3); // state
                parts[6] = nonEmptyLines.get(4); // postal
                parts[7] = "AU"; // country (默认澳大利亚)
            } else if (lineCount == 6) {
                // 6 行格式: name, address1, address2, city, state, postal
                parts[0] = nonEmptyLines.get(0); // name
                parts[1] = nonEmptyLines.get(1); // address1
                parts[2] = nonEmptyLines.get(2); // address2
                parts[3] = ""; // address3 (空)
                parts[4] = nonEmptyLines.get(3); // city
                parts[5] = nonEmptyLines.get(4); // state
                parts[6] = nonEmptyLines.get(5); // postal
                parts[7] = "AU"; // country (默认澳大利亚)
            } else if (lineCount >= 7) {
                // 7 行或更多格式: name, address1, address2, address3, city, state, postal
                parts[0] = nonEmptyLines.get(0); // name
                parts[1] = nonEmptyLines.get(1); // address1
                parts[2] = nonEmptyLines.get(2); // address2
                parts[3] = nonEmptyLines.get(3); // address3
                parts[4] = nonEmptyLines.get(4); // city
                parts[5] = nonEmptyLines.get(5); // state
                parts[6] = nonEmptyLines.get(6); // postal
                parts[7] = "AU"; // country (默认澳大利亚)
                
                // 如果有第 8 行，使用它作为 country
                if (lineCount >= 8) {
                    parts[7] = nonEmptyLines.get(7); // country
                }
            }
            
            log.info("成功解析 {} 行地址: name={}, address1={}, address2={}, address3={}, city={}, state={}, postal={}, country={}", 
                     lineCount, parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
            
            return parts;
        } catch (Exception e) {
            log.error("解析地址失败", e);
            return new String[]{"Encrypted User", "Encrypted Address", "", "", "Encrypted City", "Encrypted State", "Encrypted Postal", "AU"};
        }
    }
    
    /**
     * 解析简单文本格式的地址
     */
    private String[] parseSimpleAddress(String simpleAddress) {
        try {
            String[] parts = new String[8];
            parts[0] = "Encrypted User"; // name
            parts[1] = simpleAddress.length() > 50 ? simpleAddress.substring(0, 50) : simpleAddress; // address1
            parts[2] = ""; // address2
            parts[3] = ""; // address3
            parts[4] = "Encrypted City"; // city
            parts[5] = "Encrypted State"; // state
            parts[6] = "Encrypted Postal"; // postalCode
            parts[7] = "AU"; // country
            
            return parts;
        } catch (Exception e) {
            return new String[]{"Encrypted User", "Encrypted Address", "", "", "Encrypted City", "Encrypted State", "Encrypted Postal", "AU"};
        }
    }

    /**
     * 处理会员积分和等级更新
     * @param order 订单信息
     * @param paidAmount 实付金额
     */
    private void processMemberPointsAndLevel(Order order, BigDecimal paidAmount) {
        try {
            // ✅ 调试日志：打印方法调用栈
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            log.info("=== processMemberPointsAndLevel called from: {} ===", stackTrace.length > 2 ? stackTrace[2].getMethodName() : "unknown");
            
            // ✅ 调试日志：打印输入参数
            log.info("Input parameters - orderId: {}, orderSn: {}, memberId: {}, paidAmount: {}, paidAmount.scale: {}",
                    order.getId(), order.getOrderSn(), order.getMemberId(), paidAmount, paidAmount.scale());
            
            // 检查是否为有效会员订单（非Guest订单）
            if (order.getMemberId() == null || order.getMemberId() <= 0) {
                log.info("Order {} is a guest order, skipping member points and level update", order.getId());
                return;
            }

            log.info("Processing member points and level update for order: {}, memberId: {}, paidAmount: {}",
                    order.getId(), order.getMemberId(), paidAmount);

            // 1. 添加积分历史记录（金额的1%作为积分）
            String formattedAmount = paidAmount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
            String note = "Order payment: " + order.getOrderSn() + ", Amount: " + formattedAmount + " AUD";
            Integer platform = order.getSourceType(); // 0: OS, 1: TG

            // ✅ Admin手动标记为Paid时：只更新积分，不更新等级和订单总数
            // 等级将在每天凌晨的定时任务中统一更新
            int result = memberPointHistoryService.addPointHistoryWithoutLevelUpdate(
                order.getMemberId(),
                paidAmount,
                note,
                platform
            );

            if (result > 0) {
                log.info("Successfully added point history for member: {}, order: {}", order.getMemberId(), order.getId());

                // 2. 查询更新后的会员积分信息（等级不会实时更新）
                MemberLevel memberLevel = memberLevelService.selectMemberLevelByMemberId(order.getMemberId());
                if (memberLevel != null) {
                    log.info("Member {} points updated (level will be updated by scheduled task) - Current Level: {}, Current Points: {}",
                            order.getMemberId(), memberLevel.getCurrentLevel(), memberLevel.getCurrentPoint());
                } else {
                    log.warn("Failed to retrieve member level for member: {}", order.getMemberId());
                }
            } else {
                log.error("Failed to add point history for member: {}, order: {}", order.getMemberId(), order.getId());
            }

        } catch (Exception e) {
            log.error("Error processing member points and level update for order: {}", order.getId(), e);
        }
    }
    
    /**
     * 处理PCSP数字产品购买
     * 当订单包含PCSP产品时，自动激活PCSP服务
     * 支持Guest用户购买PCSP（使用负数memberId）
     * 
     * @param order 订单信息
     */
    private void processPcspPurchase(Order order) {
        try {
            log.info("Checking PCSP products for order: {}", order.getId());
            
            // 检查是否有memberId（包括Guest用户的负数memberId）
            if (order.getMemberId() == null) {
                log.info("Order {} has no memberId, skipping PCSP processing", order.getId());
                return;
            }
            
            // Guest用户也可以购买PCSP（负数memberId < -1000000）
            if (order.getMemberId() < -1000000L) {
                log.info("Order {} is a guest/temp user order (memberId={}), PCSP will be activated for guest user", 
                        order.getId(), order.getMemberId());
            }
            
            // 获取订单中的所有商品
            List<OrderItem> orderItems = orderItemMapper.selectOrderItemsByOrderId(order.getId());
            
            if (orderItems == null || orderItems.isEmpty()) {
                log.info("No order items found for order: {}", order.getId());
                return;
            }
            
            log.info("Found {} order items for order: {}", orderItems.size(), order.getId());
            
            for (OrderItem item : orderItems) {
                try {
                    // 获取SKU信息
                    Product2 sku = product2Service.selectProduct2BySku(item.getSku());
                    if (sku == null) {
                        log.warn("SKU not found: {}", item.getSku());
                        continue;
                    }
                    
                    log.info("Checking product: productId={}, sku={}, model={}, unit={}", 
                            sku.getProductId(), sku.getSku(), sku.getModel(), sku.getUnit());
                    
                    // 【方案A】通过product_id识别PCSP产品
                    if (isPcspProduct(sku)) {
                        log.info("PCSP product detected: productId={}, sku={}", sku.getProductId(), sku.getSku());
                        activatePcsp(order, sku, item);
                    }
                    
                } catch (Exception e) {
                    log.error("Error processing order item for PCSP: orderId={}, sku={}", 
                            order.getId(), item.getSku(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing PCSP purchase for order: {}", order.getId(), e);
        }
    }
    
    /**
     * 判断是否是PCSP产品
     * 方案A：通过product_id约定识别
     * 
     * @param sku SKU信息
     * @return true-是PCSP产品, false-不是
     */
    private boolean isPcspProduct(Product2 sku) {
        // 方案A: 使用product_id约定
        boolean isPcsp = "PCSP".equals(sku.getProductId());
        
        log.debug("isPcspProduct check: productId={}, result={}", sku.getProductId(), isPcsp);
        
        return isPcsp;
    }
    
    /**
     * 激活PCSP服务
     * 
     * @param order 订单信息
     * @param sku SKU信息
     * @param item 订单项
     */
    private void activatePcsp(Order order, Product2 sku, OrderItem item) {
        try {
            // 获取有效期月数（从model字段）
            Integer validityMonths = getPcspValidityMonths(sku);
            
            if (validityMonths == null) {
                log.warn("PCSP validity months not found for SKU: {}, model: {}", 
                        sku.getSku(), sku.getModel());
                return;
            }
            
            log.info("PCSP validity months: {} for SKU: {}", validityMonths, sku.getSku());
            
            // 确定套餐类型
            Integer packageType = getPcspPackageType(validityMonths);
            
            if (packageType == null) {
                log.warn("Unknown PCSP validity months: {}, skipping activation", validityMonths);
                return;
            }
            
            log.info("PCSP package type: {} ({}个月)", packageType, validityMonths);
            
            // 调用PCSP Service创建/续费
            Long pcspId = memberPcspService.renewPcsp(
                order.getMemberId(),
                packageType,
                validityMonths,
                sku.getId(),
                order.getOrderSn()
            );
            
            log.info("✅ PCSP activated successfully: member={}, orderId={}, orderSn={}, months={}, pcspId={}", 
                    order.getMemberId(), order.getId(), order.getOrderSn(), validityMonths, pcspId);
            
        } catch (Exception e) {
            log.error("Failed to activate PCSP for order: {}, sku: {}", 
                    order.getId(), sku.getSku(), e);
        }
    }
    
    /**
     * 获取PCSP有效期月数
     * 从SKU的model字段读取
     * 
     * @param sku SKU信息
     * @return 有效期月数
     */
    private Integer getPcspValidityMonths(Product2 sku) {
        if (sku.getModel() != null) {
            try {
                return sku.getModel().intValue();
            } catch (Exception e) {
                log.error("Failed to parse PCSP validity months from model: {}", sku.getModel(), e);
            }
        }
        return null;
    }
    
    /**
     * 根据有效期月数确定套餐类型
     * 
     * @param validityMonths 有效期月数
     * @return 套餐类型: 1-3个月, 2-6个月, 3-12个月
     */
    private Integer getPcspPackageType(Integer validityMonths) {
        if (validityMonths == null) {
            return null;
        }
        
        switch (validityMonths) {
            case 3:
                return 1;
            case 6:
                return 2;
            case 12:
                return 3;
            default:
                log.warn("Unknown PCSP validity months: {}, cannot determine package type", validityMonths);
                return null;
        }
    }

    /**
     * 检查订单号是否已存在
     * @param orderId 订单号
     * @return 存在返回true，不存在返回false
     */
    private boolean isOrderIdExists(String orderId) {
        int count = orderMapper.checkOrderIdExists(orderId);
        return count > 0;
    }
    
    /**
     * 根据支付金额和汇率计算实际的币种金额
     * @param payment 支付信息
     * @return 换算后的币种金额
     */
    private BigDecimal calculatePaidCoinFromAmount(Payment payment) {
        try {
            BigDecimal amount = payment.getAmount();
            BigDecimal rate = payment.getRate();
            String payType = payment.getPayType();
            
            log.info("Calculating paid coin - amount: {}, rate: {}, payType: {}", amount, rate, payType);
            
            // 检查是否有汇率
            if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Rate is null or zero for payment, using amount directly: {}", amount);
                return amount;
            }
            
            // 检查是否为USDT支付且货币为AUD（需要特殊处理）
            boolean isUsdtPayment = "1".equals(payType);
            boolean hasConversion = "AUD".equalsIgnoreCase(payment.getCurrency());
            
            if (isUsdtPayment && hasConversion) {
                // USDT支付：AUD * rate = USDT (rate是AUD到USDT的汇率，如0.65)
                BigDecimal calculatedPaidcoin = amount.multiply(rate).setScale(8, java.math.RoundingMode.HALF_UP);
                log.info("USDT conversion: {} AUD * {} rate = {} USDT", amount, rate, calculatedPaidcoin);
                return calculatedPaidcoin;
            } else {
                // 其他币种：AUD / rate = 币种金额 (rate是AUD到币种的汇率)
                BigDecimal calculatedPaidcoin = amount.divide(rate, 8, java.math.RoundingMode.HALF_UP);
                log.info("Coin conversion: {} AUD / {} rate = {} coins", amount, rate, calculatedPaidcoin);
                return calculatedPaidcoin;
            }
            
        } catch (Exception e) {
            log.error("Error calculating paid coin for payment: {}", payment.getOrderId(), e);
            // 出错时返回原金额
            return payment.getAmount();
        }
    }
    
    /**
     * 根据会员等级和发货时间决定是否显示tracking number
     * @param order 订单信息
     * @param shippingAddress 物流信息
     * @return tracking number或隐藏信息
     */
    private String determineTrackingNumberVisibility(Order order, ShippingAddress shippingAddress) {
        try {
            // 获取会员等级
            Integer memberLevel = order.getMemberLevel();
            if (memberLevel == null) {
                memberLevel = 1; // 默认为Bronze等级
            }
            
            // 检查是否有发货时间
            if (shippingAddress.getShippingTime() == null) {
                return "Not Available";
            }
            
            // 计算发货后的天数
            java.util.Date shippingTime = shippingAddress.getShippingTime();
            java.util.Date currentTime = new java.util.Date();
            long diffInMillies = currentTime.getTime() - shippingTime.getTime();
            long daysSinceShipped = diffInMillies / (24 * 60 * 60 * 1000);
            
            // 根据会员等级决定显示规则
            if (memberLevel >= 4) {
                // Platinum (4) 和 Diamond (5): 发货后立即显示
                return shippingAddress.getShippingNumber();
            } else if (memberLevel >= 1 && memberLevel <= 3) {
                // Bronze (1), Silver (2), Gold (3): 发货后3天显示
                if (daysSinceShipped >= 3) {
                    return shippingAddress.getShippingNumber();
                } else {
                    long remainingDays = 3 - daysSinceShipped;
                    return "Available in " + remainingDays + " day(s)";
                }
            } else {
                // 其他等级按Bronze处理
                if (daysSinceShipped >= 3) {
                    return shippingAddress.getShippingNumber();
                } else {
                    long remainingDays = 3 - daysSinceShipped;
                    return "Available in " + remainingDays + " day(s)";
                }
            }
            
        } catch (Exception e) {
            log.error("Error determining tracking number visibility for order: {}", order.getId(), e);
            return "Not Available";
        }
    }

    @Override
    public List<OrderHistoryVO> getOrderHistoryByMemberId(Long memberId) {
        try {
            List<Order> orders = orderMapper.selectOrderListById(memberId);
            List<OrderHistoryVO> result = new java.util.ArrayList<>();
            
            for (Order order : orders) {
                OrderHistoryVO vo = new OrderHistoryVO();
                vo.setOrderId(Long.valueOf(order.getOrderSn()));
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
                vo.setOrderDate(sdf.format(order.getCreateTime()));
                
                // 获取所有订单项
                List<OrderItem> items = orderItemMapper.selectOrderItemsByOrderId(order.getId());
                if (items != null && !items.isEmpty()) {
                    // 设置第一个商品信息用于向后兼容
                    vo.setProductName(items.get(0).getProductName());
                    vo.setAmount(items.get(0).getProductSpec());
                    
                    // 设置所有订单项信息
                    List<OrderHistoryVO.OrderItemVO> itemVOs = new java.util.ArrayList<>();
                    for (OrderItem item : items) {
                        OrderHistoryVO.OrderItemVO itemVO = new OrderHistoryVO.OrderItemVO();
                        itemVO.setProductName(item.getProductName());
                        itemVO.setProductSpec(item.getProductSpec());
                        itemVO.setPrice(item.getPrice() != null ? item.getPrice().toString() : "0");
                        itemVO.setQuantity(item.getQuantity());
                        itemVO.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().toString() : "0");
                        itemVOs.add(itemVO);
                    }
                    vo.setItems(itemVOs);
                } else {
                    vo.setProductName("-");
                    vo.setAmount("-");
                    vo.setItems(new java.util.ArrayList<>());
                }
                
                // 获取订单状态
                vo.setOrderStatus(order.getStatus());

                // 获取支付状态
                Payment payment = paymentService.getPaymentByOrderId(order.getId());
                if (payment != null) {
                    vo.setPaymentStatus(payment.getPayStatus());
                } else {
                    vo.setPaymentStatus(0); // 默认未支付
                }
                
                // 设置Vendor ID（如果有）
                if (order.getVendorId() != null) {
                    vo.setVendorId(order.getVendorId());
                }
                
                // 查物流单号和发货时间
                ShippingAddress addr = shippingAddressMapper.selectShippingAddressByOrderId(order.getId());
                if (addr != null) {
                    // 设置tracking number（前端会根据等级和PCSP状态决定是否显示）
                    if (addr.getShippingNumber() != null && !addr.getShippingNumber().isEmpty()) {
                        vo.setTrackingNo(addr.getShippingNumber());
                    } else {
                        vo.setTrackingNo("Not Available");
                    }
                    
                    // 设置发货时间（用于前端判断是否可以显示tracking）
                    if (addr.getShippingTime() != null) {
                        java.text.SimpleDateFormat sdfShipping = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        vo.setShippingTime(sdfShipping.format(addr.getShippingTime()));
                    }
                } else {
                    vo.setTrackingNo("Not Available");
                }
                result.add(vo);
            }
            
            // 按创建时间倒序排序
            result.sort((o1, o2) -> {
                // 转换日期格式进行比较
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
                    java.util.Date date1 = sdf.parse(o1.getOrderDate());
                    java.util.Date date2 = sdf.parse(o2.getOrderDate());
                    return date2.compareTo(date1); // 倒序
                } catch (Exception e) {
                    return 0;
                }
            });
            
            return result;
        } catch (Exception e) {
            log.error("Error getting order history for memberId: " + memberId, e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Process bond payment completion when admin manually marks order as paid
     * Update vendor application with bond order information
     */
    private void processBondPaymentComplete(Order order) {
        try {
            if (vendorApplicationService == null) {
                log.warn("[Bond Payment] IVendorApplicationService not available, skipping bond payment processing");
                return;
            }
            
            Long applicationId = order.getBondApplicationId();
            String orderId = order.getId();
            
            if (applicationId == null) {
                log.warn("[Bond Payment] Bond application ID is null for order: {}", orderId);
                return;
            }
            
            log.info("[Bond Payment] Processing bond payment completion for application: {}, order: {}", 
                    applicationId, orderId);
            
            // Get application
            VendorApplication application = vendorApplicationService.selectVendorApplicationById(applicationId);
            
            if (application == null) {
                log.error("[Bond Payment] Application not found: {}", applicationId);
                return;
            }
            
            // Check if already processed (idempotency)
            if (application.getBondOrderId() != null && application.getBondOrderId().equals(orderId)) {
                log.info("[Bond Payment] Bond payment already processed for application: {}", applicationId);
                return;
            }
            
            // Update application with bond payment info
            application.setBondOrderId(orderId);
            application.setBondPaidTime(new Date());
            
            int result = vendorApplicationService.updateVendorApplication(application);
            
            if (result > 0) {
                log.info("[Bond Payment] Successfully updated application {} with bond payment info. OrderId: {}", 
                        applicationId, orderId);
            } else {
                log.error("[Bond Payment] Failed to update application {} with bond payment info", applicationId);
            }
            
        } catch (Exception e) {
            log.error("[Bond Payment] Error processing bond payment for order: {}", order.getId(), e);
        }
    }
}

