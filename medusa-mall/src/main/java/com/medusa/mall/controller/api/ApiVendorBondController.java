package com.medusa.mall.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.vendor.VendorBondConfig;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.service.IVendorBondConfigService;
import com.medusa.mall.service.IVendorApplicationService;
import com.medusa.mall.service.IProduct2Service;
import com.medusa.mall.service.IVendorWithdrawalService;
import com.medusa.mall.service.IOrderService;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.vendor.VendorWithdrawalAddress;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.mapper.VendorWithdrawalAddressMapper;
import com.medusa.mall.mapper.Product2Mapper;
import com.medusa.mall.util.VendorAuthUtils;
import com.medusa.common.utils.spring.SpringUtils;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Vendor Bond API Controller (Public API for Vendors)
 */
@RestController
@RequestMapping("/api/mall/vendor/bond")
public class ApiVendorBondController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiVendorBondController.class);
    
    @Autowired
    private IVendorBondConfigService bondConfigService;
    
    @Autowired
    private IVendorApplicationService applicationService;
    
    @Autowired
    private IProduct2Service product2Service;
    
    @Autowired
    private IVendorWithdrawalService withdrawalService;
    
    @Autowired
    private VendorWithdrawalAddressMapper addressMapper;
    
    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private Product2Mapper product2Mapper;
    
    /**
     * Get bond summary for current logged-in vendor
     * 获取当前vendor的保证金和等级摘要信息
     */
    @GetMapping("/summary")
    public AjaxResult getBondSummary() {
        try {
            // Get current member ID
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login first");
            }
            
            // Get vendor ID
            Long vendorId = VendorAuthUtils.getCurrentVendorId();
            if (vendorId == null) {
                return AjaxResult.error("Your Co-op Member account is not approved yet");
            }
            
            logger.info("[Bond Summary] Fetching summary for vendor: {}", vendorId);
            
            // Get vendor info to access balance fields and level (admin configured)
            com.medusa.mall.domain.vendor.Vendor vendor = SpringUtils.getBean(com.medusa.mall.mapper.VendorMapper.class).selectVendorById(vendorId);
            if (vendor == null) {
                return AjaxResult.error("Member account not found");
            }
            
            // Get application
            VendorApplication application = applicationService.selectApprovedByMemberId(memberId);
            if (application == null) {
                return AjaxResult.error("No approved application found");
            }
            
            // Priority: Use level from Vendor table (admin configured), fallback to VendorApplication.bondLevel
            Integer level = vendor.getLevel();
            if (level == null || level < 1) {
                // Fallback to application bondLevel
                level = application.getBondLevel();
                if (level == null || level < 1) {
                    level = 1;
                }
                logger.info("[Bond Summary] Using level from VendorApplication: {}", level);
            } else {
                logger.info("[Bond Summary] Using level from Vendor (admin configured): {}", level);
            }
            
            // Get bond config
            VendorBondConfig config = bondConfigService.selectVendorBondConfigByLevel(level);
            if (config == null) {
                return AjaxResult.error("Bond configuration not found");
            }
            
            // Get sales points (from order history - simplified)
            // TODO: Calculate from actual order data
            Long salesPoints = 0L;
            
            // Get actual paid bond amount from order if available, otherwise use configured amount
            java.math.BigDecimal bondAmount = config.getBondAmount();
            if (application.getBondOrderId() != null && !application.getBondOrderId().isEmpty()) {
                try {
                    Order bondOrder = orderService.selectOrderById(application.getBondOrderId());
                    if (bondOrder != null && bondOrder.getTotalAmount() != null) {
                        // Use actual paid amount from order
                        bondAmount = bondOrder.getTotalAmount();
                        logger.info("[Bond Summary] Using actual paid amount {} from order {} for vendor {}", 
                                bondAmount, application.getBondOrderId(), vendorId);
                    }
                } catch (Exception e) {
                    logger.warn("[Bond Summary] Failed to get bond order {}, using configured amount: {}", 
                            application.getBondOrderId(), e.getMessage());
                    // Fall back to configured amount
                }
            }
            // Calculate Max Sales Limit as Bond × Level (according to admin configured level)
            java.math.BigDecimal maxSalesLimit = bondAmount.multiply(new java.math.BigDecimal(level.toString()));
            
            // Get current pending sales (orders in pending/processing status)
            // Calculate from pending balance
            java.math.BigDecimal currentPendingSales = vendor.getPendingBalance() != null ? 
                vendor.getPendingBalance() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal availableCapacity = maxSalesLimit.subtract(currentPendingSales);
            
            // Get withdrawal balance from vendor object
            java.math.BigDecimal withdrawableBalance = vendor.getWithdrawableBalance() != null ? 
                vendor.getWithdrawableBalance() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal pendingBalance = vendor.getPendingBalance() != null ? 
                vendor.getPendingBalance() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalWithdrawn = vendor.getTotalWithdrawn() != null ? 
                vendor.getTotalWithdrawn() : java.math.BigDecimal.ZERO;
            
            // Build response
            Map<String, Object> summary = new HashMap<>();
            summary.put("level", level);
            summary.put("bond", bondAmount);
            summary.put("salesPoints", salesPoints);
            summary.put("maxSalesLimit", maxSalesLimit);
            summary.put("availableCapacity", availableCapacity);
            summary.put("currentPendingSales", currentPendingSales);
            summary.put("withdrawableBalance", withdrawableBalance);
            summary.put("pendingBalance", pendingBalance);
            summary.put("totalWithdrawn", totalWithdrawn);
            
            // Calculate next level info
            Map<String, Object> nextLevel = new HashMap<>();
            if (level >= 5) {
                nextLevel.put("isMaxLevel", true);
            } else {
                int nextLevelNum = level + 1;
                VendorBondConfig nextConfig = bondConfigService.selectVendorBondConfigByLevel(nextLevelNum);
                if (nextConfig != null) {
                    // TODO: Get actual threshold from config or calculation
                    long nextLevelThreshold = nextLevelNum * 1000L; // Simplified
                    long pointsToNextLevel = Math.max(0, nextLevelThreshold - salesPoints);
                    double progressPercentage = salesPoints >= nextLevelThreshold ? 100.0 : 
                        (salesPoints * 100.0 / nextLevelThreshold);
                    
                    nextLevel.put("nextLevel", nextLevelNum);
                    nextLevel.put("nextLevelThreshold", nextLevelThreshold);
                    nextLevel.put("pointsToNextLevel", pointsToNextLevel);
                    nextLevel.put("progressPercentage", Math.round(progressPercentage * 100.0) / 100.0);
                    nextLevel.put("isMaxLevel", false);
                } else {
                    nextLevel.put("isMaxLevel", true);
                }
            }
            summary.put("nextLevel", nextLevel);
            
            return AjaxResult.success(summary);
            
        } catch (Exception e) {
            logger.error("[Bond Summary] Failed to get bond summary", e);
            return AjaxResult.error("Failed to load bond information: " + e.getMessage());
        }
    }
    
    /**
     * Get level upgrade history for current vendor
     * 获取等级升级历史记录
     */
    @GetMapping("/level/history")
    public AjaxResult getLevelHistory() {
        try {
            // Get current member ID
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login first");
            }
            
            // Get vendor ID
            Long vendorId = VendorAuthUtils.getCurrentVendorId();
            if (vendorId == null) {
                return AjaxResult.error("Your Co-op Member account is not approved yet");
            }
            
            logger.info("[Level History] Fetching history for vendor: {}", vendorId);
            
            // TODO: Query from mall_vendor_level_history table when it's created
            // For now, return empty list
            java.util.List<Map<String, Object>> history = new java.util.ArrayList<>();
            
            Map<String, Object> result = new HashMap<>();
            result.put("rows", history);
            result.put("total", history.size());
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            logger.error("[Level History] Failed to get level history", e);
            return AjaxResult.error("Failed to load level history: " + e.getMessage());
        }
    }
    
    /**
     * Get bond rules by level
     */
    @GetMapping("/rules/{level}")
    public AjaxResult getBondRules(@PathVariable Integer level) {
        try {
            logger.info("[Bond Rules] Fetching rules for level: {}", level);
            VendorBondConfig config = bondConfigService.selectVendorBondConfigByLevel(level);
            
            if (config == null) {
                return AjaxResult.error("Bond configuration not found for level: " + level);
            }
            
            return AjaxResult.success(config);
        } catch (Exception e) {
            logger.error("[Bond Rules] Failed to get bond rules for level: " + level, e);
            return AjaxResult.error("Failed to load bond rules");
        }
    }
    
    /**
     * Get required bond amount for current user
     */
    @GetMapping("/required-amount")
    public AjaxResult getRequiredBondAmount() {
        try {
            // Get current member ID
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login first");
            }
            
            logger.info("[Bond Amount] Fetching required bond for member: {}", memberId);
            
            // Get vendor ID
            Long vendorId = VendorAuthUtils.getCurrentVendorId();
            if (vendorId == null) {
                return AjaxResult.error("Your Co-op Member account is not approved yet");
            }
            
            // Get vendor info to access level (admin configured)
            com.medusa.mall.domain.vendor.Vendor vendor = SpringUtils.getBean(com.medusa.mall.mapper.VendorMapper.class).selectVendorById(vendorId);
            if (vendor == null) {
                return AjaxResult.error("Member account not found");
            }
            
            // Get application info
            VendorApplication application = applicationService.selectApprovedByMemberId(memberId);
            if (application == null) {
                return AjaxResult.error("No approved application found");
            }
            
            // Priority: Use level from Vendor table (admin configured), fallback to VendorApplication.bondLevel
            Integer level = vendor.getLevel();
            if (level == null || level < 1) {
                // Fallback to application bondLevel
                level = application.getBondLevel();
                if (level == null || level < 1) {
                    level = 1;
                }
            }
            
            logger.info("[Bond Amount] Member {} assigned bond level: {} (from {})", 
                    memberId, level, (vendor.getLevel() != null && vendor.getLevel() >= 1) ? "Vendor" : "VendorApplication");
            
            // Get bond config
            VendorBondConfig config = bondConfigService.selectVendorBondConfigByLevel(level);
            if (config == null) {
                return AjaxResult.error("Bond configuration not found");
            }
            
            // Build response data map - 将数据包装在 data 字段中
            Map<String, Object> bondData = new HashMap<>();
            bondData.put("level", level);
            bondData.put("bondAmount", config.getBondAmount());
            bondData.put("rulesContent", config.getRulesContent());
            bondData.put("levelPerks", config.getLevelPerks());
            bondData.put("payoutTime", config.getPayoutTime());
            bondData.put("taxDiscountMultiplier", config.getTaxDiscountMultiplier());
            bondData.put("amBonusDescription", config.getAmBonusDescription());
            bondData.put("applicationId", application.getId());
            bondData.put("applicationNumber", application.getApplicationId());
            
            return AjaxResult.success(bondData);
                
        } catch (Exception e) {
            logger.error("[Bond Amount] Failed to get required bond amount", e);
            return AjaxResult.error("Failed to load bond information");
        }
    }
    
    /**
     * Get all active bond levels (for display purposes)
     */
    @GetMapping("/levels")
    public AjaxResult getAllBondLevels() {
        try {
            logger.info("[Bond Levels] Fetching all active bond levels");
            return AjaxResult.success(bondConfigService.selectActiveConfigs());
        } catch (Exception e) {
            logger.error("[Bond Levels] Failed to get bond levels", e);
            return AjaxResult.error("Failed to load bond levels");
        }
    }
    
    /**
     * Get Bond Product SKU ID for redirecting to OS Store product page
     * 通过product_id查找BOND产品（而不是硬编码SKU），这样即使管理员更改了SKU名称也能找到
     */
    @GetMapping("/product-sku-id")
    public AjaxResult getBondProductSkuId() {
        try {
            // 通过product_id查找BOND产品（而不是硬编码SKU）
            // 这样即使管理员更改了SKU名称，只要product_id是'BOND-BASE'就能找到
            Product2 bondProduct = product2Mapper.selectProduct2ByProductId("BOND-BASE");
            
            if (bondProduct == null) {
                logger.warn("[Bond Product] Bond product not found for product_id: BOND-BASE");
                return AjaxResult.error("Bond product not found. Please contact administrator.");
            }
            
            logger.info("[Bond Product] Found bond product: SKU={}, productId={}, price={}, currency={}", 
                    bondProduct.getSku(), bondProduct.getProductId(), bondProduct.getPrice(), bondProduct.getCurrency());
            
            Map<String, Object> result = new HashMap<>();
            result.put("skuId", bondProduct.getId());
            result.put("productId", bondProduct.getProductId());
            result.put("sku", bondProduct.getSku());
            result.put("price", bondProduct.getPrice());
            result.put("currency", bondProduct.getCurrency());
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            logger.error("[Bond Product] Failed to get bond product SKU ID", e);
            return AjaxResult.error("Failed to get bond product SKU ID: " + e.getMessage());
        }
    }
    
    /**
     * Submit withdrawal wallet addresses after Bond payment
     * Triggers 1-day withdrawal lock when addresses are changed
     */
    @PostMapping("/submit-wallet-addresses")
    public AjaxResult submitWalletAddresses(@RequestBody WalletAddressRequest request) {
        try {
            // 1. Verify vendor member authentication
            Long vendorMemberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (vendorMemberId == null) {
                return AjaxResult.error("Please login first");
            }
            
            // 2. Verify application exists and Bond is paid
            VendorApplication application = applicationService.selectApprovedByMemberId(vendorMemberId);
            if (application == null) {
                return AjaxResult.error("No approved application found");
            }
            
            if (application.getBondOrderId() == null || application.getBondPaidTime() == null) {
                return AjaxResult.error("Bond payment not completed yet");
            }
            
            // 3. Get vendor ID from application
            Long vendorId = application.getVendorId();
            if (vendorId == null) {
                return AjaxResult.error("Your Co-op Member account is not created yet. Please contact admin.");
            }
            
            logger.info("[Wallet Address] Submitting wallet addresses for vendor: {}, application: {}", 
                vendorId, application.getId());
            
            // 4. Update VendorApplication wallet fields
            application.setWalletBtcProvided(request.getBtcAddress());
            application.setWalletXmrProvided(request.getXmrAddress());
            application.setWalletUsdtProvided(request.getUsdtAddress());
            applicationService.updateVendorApplication(application);
            
            // 5. Create or update VendorWithdrawalAddress records with 1-day lock
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);  // 1 day from now
            java.util.Date lockUntil = cal.getTime();
            
            // BTC Address
            VendorWithdrawalAddress btcAddress = addressMapper.selectByVendorIdAndCurrency(vendorId, "BTC");
            boolean isBtcNew = (btcAddress == null);
            boolean isBtcChanged = (btcAddress != null && !request.getBtcAddress().equals(btcAddress.getAddress()));
            
            if (btcAddress == null) {
                btcAddress = new VendorWithdrawalAddress();
                btcAddress.setVendorId(vendorId);
                btcAddress.setCurrency("BTC");
                btcAddress.setAddress(request.getBtcAddress());
                btcAddress.setIsActive(1);
                btcAddress.setVerifiedAt(new java.util.Date());
                btcAddress.setVerifiedMethod("BOND_PAYMENT");
                // Set 1-day lock for new addresses
                btcAddress.setWithdrawalLockUntil(lockUntil);
                btcAddress.setAddressChangedAt(new java.util.Date());
                addressMapper.insert(btcAddress);
            } else if (isBtcChanged) {
                // Address changed - trigger 1-day lock
                btcAddress.setPreviousAddress(btcAddress.getAddress());
                btcAddress.setAddress(request.getBtcAddress());
                btcAddress.setWithdrawalLockUntil(lockUntil);
                btcAddress.setAddressChangedAt(new java.util.Date());
                addressMapper.update(btcAddress);
                logger.info("[Wallet Address] BTC address changed for vendor {}. 1-day lock until: {}", vendorId, lockUntil);
            }
            
            // USDT (TRON) Address
            VendorWithdrawalAddress usdtAddress = addressMapper.selectByVendorIdAndCurrency(vendorId, "USDT_TRX");
            boolean isUsdtNew = (usdtAddress == null);
            boolean isUsdtChanged = (usdtAddress != null && !request.getUsdtAddress().equals(usdtAddress.getAddress()));
            
            if (usdtAddress == null) {
                usdtAddress = new VendorWithdrawalAddress();
                usdtAddress.setVendorId(vendorId);
                usdtAddress.setCurrency("USDT_TRX");
                usdtAddress.setAddress(request.getUsdtAddress());
                usdtAddress.setIsActive(1);
                usdtAddress.setVerifiedAt(new java.util.Date());
                usdtAddress.setVerifiedMethod("BOND_PAYMENT");
                usdtAddress.setWithdrawalLockUntil(lockUntil);
                usdtAddress.setAddressChangedAt(new java.util.Date());
                addressMapper.insert(usdtAddress);
            } else if (isUsdtChanged) {
                usdtAddress.setPreviousAddress(usdtAddress.getAddress());
                usdtAddress.setAddress(request.getUsdtAddress());
                usdtAddress.setWithdrawalLockUntil(lockUntil);
                usdtAddress.setAddressChangedAt(new java.util.Date());
                addressMapper.update(usdtAddress);
                logger.info("[Wallet Address] USDT address changed for vendor {}. 1-day lock until: {}", vendorId, lockUntil);
            }
            
            // XMR Address
            VendorWithdrawalAddress xmrAddress = addressMapper.selectByVendorIdAndCurrency(vendorId, "XMR");
            boolean isXmrNew = (xmrAddress == null);
            boolean isXmrChanged = (xmrAddress != null && !request.getXmrAddress().equals(xmrAddress.getAddress()));
            
            if (xmrAddress == null) {
                xmrAddress = new VendorWithdrawalAddress();
                xmrAddress.setVendorId(vendorId);
                xmrAddress.setCurrency("XMR");
                xmrAddress.setAddress(request.getXmrAddress());
                xmrAddress.setIsActive(1);
                xmrAddress.setVerifiedAt(new java.util.Date());
                xmrAddress.setVerifiedMethod("BOND_PAYMENT");
                xmrAddress.setWithdrawalLockUntil(lockUntil);
                xmrAddress.setAddressChangedAt(new java.util.Date());
                addressMapper.insert(xmrAddress);
            } else if (isXmrChanged) {
                xmrAddress.setPreviousAddress(xmrAddress.getAddress());
                xmrAddress.setAddress(request.getXmrAddress());
                xmrAddress.setWithdrawalLockUntil(lockUntil);
                xmrAddress.setAddressChangedAt(new java.util.Date());
                addressMapper.update(xmrAddress);
                logger.info("[Wallet Address] XMR address changed for vendor {}. 1-day lock until: {}", vendorId, lockUntil);
            }
            
            logger.info("[Wallet Address] Successfully saved wallet addresses for vendor: {}", vendorId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Wallet addresses saved successfully");
            result.put("lockUntil", lockUntil);
            result.put("hasLock", isBtcChanged || isUsdtChanged || isXmrChanged || isBtcNew || isUsdtNew || isXmrNew);
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            logger.error("[Wallet Address] Failed to save wallet addresses", e);
            return AjaxResult.error("Failed to save wallet addresses: " + e.getMessage());
        }
    }
    
    /**
     * Wallet Address Request DTO
     */
    public static class WalletAddressRequest {
        private String btcAddress;
        private String usdtAddress;
        private String xmrAddress;
        
        public String getBtcAddress() {
            return btcAddress;
        }
        
        public void setBtcAddress(String btcAddress) {
            this.btcAddress = btcAddress;
        }
        
        public String getUsdtAddress() {
            return usdtAddress;
        }
        
        public void setUsdtAddress(String usdtAddress) {
            this.usdtAddress = usdtAddress;
        }
        
        public String getXmrAddress() {
            return xmrAddress;
        }
        
        public void setXmrAddress(String xmrAddress) {
            this.xmrAddress = xmrAddress;
        }
    }
    
    /**
     * Get Bond payment order details
     */
    @GetMapping("/order/{orderId}")
    public AjaxResult getBondOrderDetails(@PathVariable String orderId) {
        try {
            // Verify vendor member authentication
            Long vendorMemberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (vendorMemberId == null) {
                return AjaxResult.error("Please login first");
            }
            
            // Get order
            Order order = orderService.selectOrderById(orderId);
            if (order == null) {
                return AjaxResult.error("Order not found");
            }
            
            // Verify this is a Bond payment order
            if (!"BOND_PAYMENT".equals(order.getOrderType())) {
                return AjaxResult.error("This is not a Bond payment order");
            }
            
            // Verify the order belongs to this vendor member
            VendorApplication application = applicationService.selectApprovedByMemberId(vendorMemberId);
            if (application == null || !orderId.equals(application.getBondOrderId())) {
                return AjaxResult.error("Order access denied");
            }
            
            // Get vendor ID
            Long vendorId = VendorAuthUtils.getCurrentVendorId();
            
            // Priority: Use level from Vendor table (admin configured), fallback to VendorApplication.bondLevel
            Integer level = null;
            if (vendorId != null) {
                com.medusa.mall.domain.vendor.Vendor vendor = SpringUtils.getBean(com.medusa.mall.mapper.VendorMapper.class).selectVendorById(vendorId);
                if (vendor != null) {
                    level = vendor.getLevel();
                }
            }
            
            if (level == null || level < 1) {
                // Fallback to application bondLevel
                level = application.getBondLevel();
                if (level == null || level < 1) {
                    level = 1;
                }
            }
            
            VendorBondConfig config = bondConfigService.selectVendorBondConfigByLevel(level);
            
            // Build response
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderSn", order.getOrderSn());
            result.put("totalAmount", order.getTotalAmount());
            result.put("status", order.getStatus());
            result.put("createTime", order.getCreateTime());
            result.put("bondLevel", level);
            result.put("bondAmount", config != null ? config.getBondAmount() : null);
            result.put("bondPaidTime", application.getBondPaidTime());
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            logger.error("[Bond Order] Failed to get bond order details", e);
            return AjaxResult.error("Failed to get bond order details: " + e.getMessage());
        }
    }
}
