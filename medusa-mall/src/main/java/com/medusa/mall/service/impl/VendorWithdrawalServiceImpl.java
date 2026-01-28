package com.medusa.mall.service.impl;

import com.medusa.common.utils.DateUtils;
import com.medusa.common.utils.uuid.IdUtils;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.domain.vendor.VendorBalanceLog;
import com.medusa.mall.domain.vendor.VendorWithdrawalAddress;
import com.medusa.mall.domain.vendor.VendorWithdrawalRequest;
import com.medusa.mall.mapper.OrderMapper;
import com.medusa.mall.mapper.VendorBalanceLogMapper;
import com.medusa.mall.mapper.VendorMapper;
import com.medusa.mall.mapper.VendorWithdrawalAddressMapper;
import com.medusa.mall.mapper.VendorWithdrawalRequestMapper;
import com.medusa.mall.service.IVendorNotificationService;
import com.medusa.mall.service.IVendorWithdrawalService;
import com.medusa.mall.utils.PgpEncryptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.Calendar;

/**
 * Vendor 提现服务实现
 * 
 * @author medusa
 * @date 2025-11-18
 */
@Service
public class VendorWithdrawalServiceImpl implements IVendorWithdrawalService {
    
    private static final Logger log = LoggerFactory.getLogger(VendorWithdrawalServiceImpl.class);
    
    @Autowired
    private VendorMapper vendorMapper;
    
    @Autowired
    private VendorBalanceLogMapper balanceLogMapper;
    
    @Autowired
    private VendorWithdrawalAddressMapper addressMapper;
    
    @Autowired
    private VendorWithdrawalRequestMapper requestMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private IVendorNotificationService notificationService;
    
    /**
     * 根据 Vendor Level 计算资金可用延迟天数
     * Level 1: 14 days
     * Level 2: 10 days
     * Level 3: 7 days
     * Level 4: 5 days
     * Level 5+: 3 days
     */
    private int getDelayDaysByLevel(int level) {
        if (level <= 1) return 14;
        if (level == 2) return 10;
        if (level == 3) return 7;
        if (level == 4) return 5;
        return 3; // Level 5+
    }
    
    /**
     * 生成提现请求编号
     */
    private String generateRequestCode() {
        return "WD" + System.currentTimeMillis() + IdUtils.fastSimpleUUID().substring(0, 6).toUpperCase();
    }
    
    /**
     * 生成6位数验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    
    // ============================================
    // 余额管理
    // ============================================
    
    @Override
    public Map<String, BigDecimal> getVendorBalance(Long vendorId) {
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        if (vendor == null) {
            throw new RuntimeException("Vendor not found: " + vendorId);
        }
        
        Map<String, BigDecimal> balance = new HashMap<>();
        balance.put("withdrawableBalance", vendor.getWithdrawableBalance() != null ? vendor.getWithdrawableBalance() : BigDecimal.ZERO);
        balance.put("pendingBalance", vendor.getPendingBalance() != null ? vendor.getPendingBalance() : BigDecimal.ZERO);
        balance.put("totalWithdrawn", vendor.getTotalWithdrawn() != null ? vendor.getTotalWithdrawn() : BigDecimal.ZERO);
        
        return balance;
    }
    
    @Override
    @Transactional
    public void addPendingBalanceOnShipment(Long vendorId, Long orderId, BigDecimal amount) {
        log.info("Adding pending balance for vendor {}, order {}, amount {}", vendorId, orderId, amount);
        
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        if (vendor == null) {
            throw new RuntimeException("Vendor not found: " + vendorId);
        }
        
        // 计算资金可用日期
        int delayDays = getDelayDaysByLevel(vendor.getLevel() != null ? vendor.getLevel() : 1);
        Date availableDate = DateUtils.addDays(new Date(), delayDays);
        
        // 更新 Vendor 待确认余额
        BigDecimal currentPending = vendor.getPendingBalance() != null ? vendor.getPendingBalance() : BigDecimal.ZERO;
        BigDecimal newPending = currentPending.add(amount);
        vendor.setPendingBalance(newPending);
        vendorMapper.updateVendor(vendor);
        
        // 记录余额变动
        VendorBalanceLog balanceLog = new VendorBalanceLog();
        balanceLog.setVendorId(vendorId);
        balanceLog.setChangeType("ORDER_SHIPPED");
        balanceLog.setAmount(amount);
        balanceLog.setBeforeBalance(currentPending);
        balanceLog.setAfterBalance(newPending);
        balanceLog.setRelatedOrderId(orderId);
        balanceLog.setAvailableDate(availableDate);
        balanceLog.setDescription("Order shipped, pending balance will be available on " + DateUtils.parseDateToStr("yyyy-MM-dd", availableDate));
        balanceLogMapper.insert(balanceLog);
        
        // 更新订单的余额可用日期
        Order order = new Order();
        order.setId(String.valueOf(orderId));
        order.setBalanceAvailableDate(availableDate);
        order.setIsBalanceReleased(0);
        orderMapper.updateOrder(order);
        
        log.info("Pending balance updated for vendor {}. New pending balance: {}, available date: {}", 
                vendorId, newPending, availableDate);
    }
    
    @Override
    @Transactional
    public void releaseExpiredPendingBalance() {
        log.info("Starting to release expired pending balance...");
        
        // 查询所有已发货但余额未释放且到期的订单
        List<Order> expiredOrders = orderMapper.selectExpiredBalanceOrders();
        
        for (Order order : expiredOrders) {
            Long vendorId = order.getVendorId();
            if (vendorId == null) {
                continue; // 平台自营订单跳过
            }
            
            Vendor vendor = vendorMapper.selectVendorById(vendorId);
            if (vendor == null) {
                log.warn("Vendor not found for order {}, skipping", order.getId());
                continue;
            }
            
            BigDecimal amount = order.getTotalAmount();
            
            // 从待确认余额转移到可提现余额
            BigDecimal currentPending = vendor.getPendingBalance() != null ? vendor.getPendingBalance() : BigDecimal.ZERO;
            BigDecimal currentWithdrawable = vendor.getWithdrawableBalance() != null ? vendor.getWithdrawableBalance() : BigDecimal.ZERO;
            
            BigDecimal newPending = currentPending.subtract(amount);
            BigDecimal newWithdrawable = currentWithdrawable.add(amount);
            
            vendor.setPendingBalance(newPending.max(BigDecimal.ZERO)); // 确保不为负
            vendor.setWithdrawableBalance(newWithdrawable);
            vendorMapper.updateVendor(vendor);
            
            // 记录余额变动
            VendorBalanceLog balanceLog = new VendorBalanceLog();
            balanceLog.setVendorId(vendorId);
            balanceLog.setChangeType("BALANCE_RELEASED");
            balanceLog.setAmount(amount);
            balanceLog.setBeforeBalance(currentWithdrawable);
            balanceLog.setAfterBalance(newWithdrawable);
            balanceLog.setRelatedOrderId(Long.parseLong(order.getId()));
            balanceLog.setDescription("Pending balance released to withdrawable balance for order " + order.getOrderSn());
            balanceLogMapper.insert(balanceLog);
            
            // 标记订单余额已释放
            Order updateOrder = new Order();
            updateOrder.setId(order.getId());
            updateOrder.setIsBalanceReleased(1);
            orderMapper.updateOrder(updateOrder);
            
            log.info("Released balance for vendor {}, order {}. Amount: {}, new withdrawable: {}",
                    vendorId, order.getOrderSn(), amount, newWithdrawable);
        }
        
        log.info("Finished releasing expired pending balance. Processed {} orders", expiredOrders.size());
    }
    
    @Override
    @Transactional
    public void deductBalanceOnDispute(Long vendorId, Long orderId, BigDecimal amount) {
        log.info("Deducting balance for vendor {}, order {}, amount {}", vendorId, orderId, amount);
        
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        if (vendor == null) {
            throw new RuntimeException("Vendor not found: " + vendorId);
        }
        
        // 先尝试从可提现余额扣除
        BigDecimal currentWithdrawable = vendor.getWithdrawableBalance() != null ? vendor.getWithdrawableBalance() : BigDecimal.ZERO;
        BigDecimal remainingToDeduct = amount;
        
        if (currentWithdrawable.compareTo(remainingToDeduct) >= 0) {
            // 可提现余额足够
            vendor.setWithdrawableBalance(currentWithdrawable.subtract(remainingToDeduct));
            remainingToDeduct = BigDecimal.ZERO;
        } else {
            // 扣光可提现余额，剩余从待确认余额扣除
            vendor.setWithdrawableBalance(BigDecimal.ZERO);
            remainingToDeduct = remainingToDeduct.subtract(currentWithdrawable);
            
            BigDecimal currentPending = vendor.getPendingBalance() != null ? vendor.getPendingBalance() : BigDecimal.ZERO;
            vendor.setPendingBalance(currentPending.subtract(remainingToDeduct).max(BigDecimal.ZERO));
        }
        
        vendorMapper.updateVendor(vendor);
        
        // 记录余额变动
        VendorBalanceLog balanceLog = new VendorBalanceLog();
        balanceLog.setVendorId(vendorId);
        balanceLog.setChangeType("DISPUTE");
        balanceLog.setAmount(amount.negate()); // 负数表示扣除
        balanceLog.setBeforeBalance(currentWithdrawable);
        balanceLog.setAfterBalance(vendor.getWithdrawableBalance());
        balanceLog.setRelatedOrderId(orderId);
        balanceLog.setDescription("Balance deducted due to dispute for order");
        balanceLogMapper.insert(balanceLog);
        
        log.info("Balance deducted for vendor {}. New withdrawable: {}, new pending: {}",
                vendorId, vendor.getWithdrawableBalance(), vendor.getPendingBalance());
    }
    
    @Override
    public List<VendorBalanceLog> getBalanceLogs(Long vendorId) {
        return balanceLogMapper.selectByVendorId(vendorId);
    }
    
    // ============================================
    // 提现地址管理
    // ============================================
    
    @Override
    public List<VendorWithdrawalAddress> getWithdrawalAddresses(Long vendorId) {
        return addressMapper.selectByVendorId(vendorId);
    }
    
    @Override
    public VendorWithdrawalAddress getWithdrawalAddress(Long vendorId, String currency) {
        VendorWithdrawalAddress address = addressMapper.selectByVendorIdAndCurrency(vendorId, currency);
        // 只返回激活的地址，避免显示未验证的地址
        if (address != null && address.getIsActive() != null && address.getIsActive() == 1) {
            return address;
        }
        return null;
    }
    
    /**
     * Get withdrawal address (including inactive ones) - for internal use
     */
    private VendorWithdrawalAddress getWithdrawalAddressInternal(Long vendorId, String currency) {
        return addressMapper.selectByVendorIdAndCurrency(vendorId, currency);
    }
    
    @Override
    @Transactional
    public boolean createOrUpdateWithdrawalAddress(VendorWithdrawalAddress address) {
        try {
            VendorWithdrawalAddress existing = addressMapper.selectByVendorIdAndCurrency(
                address.getVendorId(), address.getCurrency());
            
            if (existing == null) {
                // Create new address
                addressMapper.insert(address);
                log.info("Created new withdrawal address for vendor {}, currency {}", 
                    address.getVendorId(), address.getCurrency());
            } else {
                // Update existing address
                address.setId(existing.getId());
                addressMapper.update(address);
                log.info("Updated withdrawal address for vendor {}, currency {}", 
                    address.getVendorId(), address.getCurrency());
            }
            
            return true;
        } catch (Exception e) {
            log.error("Failed to create or update withdrawal address", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> requestAddressUpdate(Long vendorId, String currency, String newAddress) {
        log.info("Requesting address update for vendor {}, currency {}", vendorId, currency);
        
        // 生成验证码
        String verificationCode = generateVerificationCode();
        
        // 保存或更新地址记录（新地址保存到 pending_address，保留旧地址）
        VendorWithdrawalAddress address = addressMapper.selectByVendorIdAndCurrency(vendorId, currency);
        
        if (address == null) {
            // 首次创建地址：直接保存到 address，标记为未激活
            address = new VendorWithdrawalAddress();
            address.setVendorId(vendorId);
            address.setCurrency(currency);
            address.setAddress(newAddress);
            address.setIsActive(0); // 未验证前不激活
            address.setVerificationCode(verificationCode);
            addressMapper.insert(address);
        } else {
            // 更新现有地址：保留旧地址，新地址保存到 pending_address
            if (address.getIsActive() != null && address.getIsActive() == 1 && address.getAddress() != null) {
                // 如果当前地址已激活，保存旧地址到 previous_address
                address.setPreviousAddress(address.getAddress());
                log.info("Saving previous address for vendor {}, currency {}: {}", vendorId, currency, address.getAddress());
            }
            
            // 新地址保存到 pending_address，旧地址保留在 address
            address.setPendingAddress(newAddress);
            address.setPendingVerificationCode(verificationCode);
            address.setPendingRequestTime(new Date());
            // address 和 isActive 保持不变，确保旧地址仍然可用
            addressMapper.update(address);
            
            log.info("Address update request saved. Old address (still active): {}, New pending address: {}", 
                    address.getAddress(), newAddress);
        }
        
        // 获取Vendor信息以获取PGP公钥
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        // 尝试使用PGP公钥加密验证码
        String encryptedMessage = null;
        boolean pgpEncryptionSuccess = false;
        
        if (vendor == null) {
            log.warn("Vendor not found for vendorId: {}", vendorId);
        } else {
            String pgpPublicKey = vendor.getPgpPublicKeyUrl();
            log.info("Checking PGP public key for vendor {}: key present = {}", vendorId, StringUtils.isNotBlank(pgpPublicKey));
            
            if (StringUtils.isNotBlank(pgpPublicKey)) {
                // 处理转义的换行符：将字符串 "\n" 转换为真正的换行符
                // 数据库可能存储了转义的换行符（如 JSON 格式）
                String normalizedPgpKey = pgpPublicKey.replace("\\n", "\n").replace("\\r", "\r");
                
                // 记录PGP公钥的前100个字符用于调试（不记录完整密钥）
                String keyPreview = normalizedPgpKey.length() > 100 ? normalizedPgpKey.substring(0, 100) + "..." : normalizedPgpKey;
                log.info("Vendor {} PGP public key preview: {}", vendorId, keyPreview);
                log.info("PGP public key length: {} characters (original: {})", normalizedPgpKey.length(), pgpPublicKey.length());
                log.info("PGP public key starts with PGP header: {}", normalizedPgpKey.trim().startsWith("-----BEGIN PGP"));
                
                // 检查是否包含转义的换行符
                if (pgpPublicKey.contains("\\n") && !pgpPublicKey.contains("\n")) {
                    log.info("Detected escaped newlines in PGP key, converting to actual newlines");
                }
                
                try {
                    log.info("Attempting to encrypt verification code with PGP for vendor {}", vendorId);
                    
                    // 使用规范化后的PGP公钥加密验证码，返回ASCII armor格式
                    encryptedMessage = PgpEncryptionUtil.encryptToAsciiArmor(verificationCode, normalizedPgpKey);
                    pgpEncryptionSuccess = true;
                    
                    log.info("Successfully encrypted verification code with PGP for vendor {}. Encrypted message length: {}", 
                            vendorId, encryptedMessage != null ? encryptedMessage.length() : 0);
                    
                    result.put("encryptedMessage", encryptedMessage);
                    result.put("verificationMethod", "PGP");
                    result.put("message", "Verification code has been encrypted with your PGP public key. Please decrypt the message below and enter the verification code.");
                    
                    // 不返回明文验证码（安全考虑）
                    // 但在测试环境中，如果设置了环境变量，可以返回明文用于测试
                    String testMode = System.getProperty("vendor.withdrawal.test.mode", System.getenv("VENDOR_WITHDRAWAL_TEST_MODE"));
                    if ("true".equalsIgnoreCase(testMode)) {
                        log.warn("TEST MODE: Returning plaintext verification code for testing");
                        result.put("verificationCode", verificationCode);
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to encrypt verification code with PGP for vendor {}: {}", vendorId, e.getMessage(), e);
                    log.error("Exception details: {}", e.getClass().getName());
                    if (e.getCause() != null) {
                        log.error("Caused by: {}", e.getCause().getMessage());
                    }
                    // PGP加密失败，fallback到明文或邮件
                    pgpEncryptionSuccess = false;
                }
            } else {
                log.warn("Vendor {} does not have a PGP public key configured", vendorId);
            }
        }
        
        // 如果PGP加密失败或Vendor没有PGP公钥，返回明文验证码（不推荐，但为了兼容性）
        if (!pgpEncryptionSuccess) {
            log.warn("PGP encryption not available for vendor {}, returning plaintext verification code", vendorId);
            result.put("verificationCode", verificationCode);
            result.put("verificationMethod", "PLAINTEXT");
            result.put("message", "Verification code generated. Please verify using the code below. (Note: PGP encryption is recommended for security)");
            
            // TODO: 在实际生产环境中，应该发送验证码到Vendor的注册邮箱
            // 而不是返回明文验证码
        }
        
        log.info("Address update requested for vendor {}, verification code: {}, PGP encrypted: {}", 
                vendorId, verificationCode, pgpEncryptionSuccess);
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean verifyAndUpdateAddress(Long vendorId, String currency, String verificationCode) {
        log.info("Verifying address update for vendor {}, currency {}", vendorId, currency);
        
        VendorWithdrawalAddress address = addressMapper.selectByVendorIdAndCurrency(vendorId, currency);
        
        if (address == null) {
            log.warn("No address found for vendor {}, currency {}", vendorId, currency);
            return false;
        }
        
        // 检查是否有待验证的地址（新逻辑）
        if (address.getPendingAddress() != null && address.getPendingVerificationCode() != null) {
            // 验证 pending_verification_code
            if (!address.getPendingVerificationCode().equals(verificationCode)) {
                log.warn("Invalid pending verification code for vendor {}", vendorId);
                return false;
            }
            
            // 验证成功：将 pending_address 复制到 address，激活地址
            String oldAddress = address.getAddress();
            boolean isAddressChanged = !address.getPendingAddress().equals(oldAddress);
            
            address.setAddress(address.getPendingAddress());
            address.setIsActive(1);
            address.setVerifiedAt(new Date());
            address.setVerifiedMethod("PGP"); // 或 "EMAIL"
            
            // 如果地址更改，设置 1 天锁定
            if (isAddressChanged) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 1);
                address.setWithdrawalLockUntil(cal.getTime());
                address.setAddressChangedAt(new Date());
                address.setPreviousAddress(oldAddress);
                log.info("Address changed for vendor {}, currency {}. 1-day withdrawal lock applied until: {}", 
                    vendorId, currency, cal.getTime());
            }
            
            // 清空 pending 相关字段
            address.setPendingAddress(null);
            address.setPendingVerificationCode(null);
            address.setPendingRequestTime(null);
            address.setVerificationCode(null); // 清空旧的验证码（向后兼容）
            
            addressMapper.update(address);
            
            log.info("Pending address verified and activated for vendor {}, currency {}. Old address: {}, New address: {}", 
                    vendorId, currency, oldAddress, address.getAddress());
            
            return true;
        }
        
        // 向后兼容：检查旧的 verification_code（用于首次创建地址的情况）
        if (address.getVerificationCode() != null && address.getVerificationCode().equals(verificationCode)) {
            // 验证成功，激活地址并清空验证码
            address.setIsActive(1);
            address.setVerifiedAt(new Date());
            address.setVerifiedMethod("PGP"); // 或 "EMAIL"
            address.setVerificationCode(null); // 清空验证码
            addressMapper.update(address);
            
            log.info("Address verified and activated for vendor {}, currency {} (legacy mode)", vendorId, currency);
            
            return true;
        }
        
        log.warn("Invalid verification code for vendor {}, currency {}", vendorId, currency);
        return false;
    }
    
    // ============================================
    // 提现请求
    // ============================================
    
    @Override
    @Transactional
    public VendorWithdrawalRequest createWithdrawalRequest(Long vendorId, String currency, BigDecimal amount) {
        log.info("Creating withdrawal request for vendor {}, currency {}, amount {}", vendorId, currency, amount);
        
        // 验证金额：必须大于0
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be greater than 0. Provided: " + amount);
        }
        
        // 验证 Vendor 和余额
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        if (vendor == null) {
            throw new RuntimeException("Vendor not found: " + vendorId);
        }
        
        BigDecimal withdrawableBalance = vendor.getWithdrawableBalance() != null ? vendor.getWithdrawableBalance() : BigDecimal.ZERO;
        if (withdrawableBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient withdrawable balance. Available: " + withdrawableBalance + ", Requested: " + amount);
        }
        
        // 获取提现地址
        VendorWithdrawalAddress address = addressMapper.selectByVendorIdAndCurrency(vendorId, currency);
        if (address == null || address.getIsActive() != 1) {
            throw new RuntimeException("No active withdrawal address found for currency: " + currency);
        }
        
        // Check withdrawal lock (1-day lock after address change)
        if (address.getWithdrawalLockUntil() != null) {
            Date now = new Date();
            if (address.getWithdrawalLockUntil().after(now)) {
                long daysRemaining = (address.getWithdrawalLockUntil().getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
                throw new RuntimeException(String.format(
                    "Withdrawal is locked for %d more day(s) due to recent address change. Lock expires on: %s",
                    daysRemaining + 1,
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(address.getWithdrawalLockUntil())
                ));
            }
        }
        
        // 创建提现请求
        VendorWithdrawalRequest request = new VendorWithdrawalRequest();
        request.setRequestCode(generateRequestCode());
        request.setVendorId(vendorId);
        request.setCurrency(currency);
        request.setAmount(amount);
        request.setWithdrawalAddress(address.getAddress());
        request.setRequestStatus("PENDING");
        requestMapper.insert(request);
        
        // 冻结余额（从可提现余额扣除，但不加到已提现）
        vendor.setWithdrawableBalance(withdrawableBalance.subtract(amount));
        vendorMapper.updateVendor(vendor);
        
        // 记录余额变动
        VendorBalanceLog balanceLog = new VendorBalanceLog();
        balanceLog.setVendorId(vendorId);
        balanceLog.setChangeType("WITHDRAWAL");
        balanceLog.setAmount(amount.negate());
        balanceLog.setBeforeBalance(withdrawableBalance);
        balanceLog.setAfterBalance(vendor.getWithdrawableBalance());
        balanceLog.setRelatedWithdrawalId(request.getId());
        balanceLog.setDescription("Withdrawal request created: " + request.getRequestCode());
        balanceLogMapper.insert(balanceLog);
        
        log.info("Withdrawal request created: {}", request.getRequestCode());
        
        return request;
    }
    
    @Override
    public List<VendorWithdrawalRequest> getWithdrawalRequests(Long vendorId) {
        return requestMapper.selectByVendorId(vendorId);
    }
    
    @Override
    public List<VendorWithdrawalRequest> getPendingWithdrawalRequests() {
        VendorWithdrawalRequest query = new VendorWithdrawalRequest();
        query.setRequestStatus("PENDING");
        return requestMapper.selectList(query);
    }
    
    @Override
    @Transactional
    public boolean approveWithdrawalRequest(Long requestId, boolean approved, String approveBy, String remark) {
        log.info("Approving withdrawal request {}, approved: {}", requestId, approved);
        
        VendorWithdrawalRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            log.warn("Withdrawal request not found: {}", requestId);
            return false;
        }
        
        if (!"PENDING".equals(request.getRequestStatus())) {
            log.warn("Withdrawal request {} is not in PENDING status: {}", requestId, request.getRequestStatus());
            return false;
        }
        
        if (approved) {
            request.setRequestStatus("APPROVED");
            request.setApproveBy(approveBy);
            request.setApproveTime(new Date());
            request.setApproveRemark(remark);
        } else {
            // 拒绝，退还余额
            request.setRequestStatus("REJECTED");
            request.setApproveBy(approveBy);
            request.setApproveTime(new Date());
            request.setRejectReason(remark);
            
            Vendor vendor = vendorMapper.selectVendorById(request.getVendorId());
            BigDecimal currentBalance = vendor.getWithdrawableBalance() != null ? vendor.getWithdrawableBalance() : BigDecimal.ZERO;
            vendor.setWithdrawableBalance(currentBalance.add(request.getAmount()));
            vendorMapper.updateVendor(vendor);
            
            // 记录余额退还
            VendorBalanceLog balanceLog = new VendorBalanceLog();
            balanceLog.setVendorId(request.getVendorId());
            balanceLog.setChangeType("WITHDRAWAL");
            balanceLog.setAmount(request.getAmount());
            balanceLog.setBeforeBalance(currentBalance);
            balanceLog.setAfterBalance(vendor.getWithdrawableBalance());
            balanceLog.setRelatedWithdrawalId(request.getId());
            balanceLog.setDescription("Withdrawal request rejected, balance refunded: " + request.getRequestCode());
            balanceLogMapper.insert(balanceLog);
        }
        
        requestMapper.update(request);
        
        log.info("Withdrawal request {} {}", requestId, approved ? "approved" : "rejected");
        
        // 发送通知
        Vendor vendor = vendorMapper.selectVendorById(request.getVendorId());
        notificationService.notifyWithdrawalStatusChange(vendor, request, request.getRequestStatus());
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean markWithdrawalProcessing(Long requestId, String txHash) {
        VendorWithdrawalRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            log.error("Withdrawal request {} not found", requestId);
            return false;
        }
        
        // 只有已批准的请求才能标记为处理中
        if (!"APPROVED".equals(request.getRequestStatus())) {
            log.error("Withdrawal request {} is not approved, current status: {}", 
                requestId, request.getRequestStatus());
            return false;
        }
        
        // 更新请求状态为处理中
        request.setRequestStatus("PROCESSING");
        request.setTxHash(txHash);
        requestMapper.update(request);
        
        log.info("Withdrawal request {} marked as processing, txHash: {}", requestId, txHash);
        
        // 发送通知
        Vendor vendor = vendorMapper.selectVendorById(request.getVendorId());
        notificationService.notifyWithdrawalStatusChange(vendor, request, "PROCESSING");
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean markWithdrawalCompleted(Long requestId, BigDecimal txFee) {
        log.info("Marking withdrawal request {} as completed, txFee: {}", requestId, txFee);
        
        VendorWithdrawalRequest request = requestMapper.selectById(requestId);
        if (request == null || !"PROCESSING".equals(request.getRequestStatus())) {
            return false;
        }
        
        request.setRequestStatus("COMPLETED");
        request.setTxTime(new Date());
        request.setTxFee(txFee);
        requestMapper.update(request);
        
        // 更新 Vendor 的累计已提现金额
        Vendor vendor = vendorMapper.selectVendorById(request.getVendorId());
        BigDecimal totalWithdrawn = vendor.getTotalWithdrawn() != null ? vendor.getTotalWithdrawn() : BigDecimal.ZERO;
        vendor.setTotalWithdrawn(totalWithdrawn.add(request.getAmount()));
        vendorMapper.updateVendor(vendor);
        
        log.info("Withdrawal request {} completed", requestId);
        
        // 发送通知
        notificationService.notifyWithdrawalStatusChange(vendor, request, "COMPLETED");
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean markWithdrawalFailed(Long requestId, String reason) {
        log.info("Marking withdrawal request {} as failed, reason: {}", requestId, reason);
        
        VendorWithdrawalRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            return false;
        }
        
        request.setRequestStatus("FAILED");
        request.setRejectReason(reason);
        requestMapper.update(request);
        
        // 退还余额
        Vendor vendor = vendorMapper.selectVendorById(request.getVendorId());
        BigDecimal currentBalance = vendor.getWithdrawableBalance() != null ? vendor.getWithdrawableBalance() : BigDecimal.ZERO;
        vendor.setWithdrawableBalance(currentBalance.add(request.getAmount()));
        vendorMapper.updateVendor(vendor);
        
        // 记录余额退还
        VendorBalanceLog balanceLog = new VendorBalanceLog();
        balanceLog.setVendorId(request.getVendorId());
        balanceLog.setChangeType("WITHDRAWAL");
        balanceLog.setAmount(request.getAmount());
        balanceLog.setBeforeBalance(currentBalance);
        balanceLog.setAfterBalance(vendor.getWithdrawableBalance());
        balanceLog.setRelatedWithdrawalId(request.getId());
        balanceLog.setDescription("Withdrawal failed, balance refunded: " + request.getRequestCode());
        balanceLogMapper.insert(balanceLog);
        
        log.info("Withdrawal request {} marked as failed", requestId);
        
        // 发送通知
        notificationService.notifyWithdrawalStatusChange(vendor, request, "FAILED");
        
        return true;
    }
    
    // ============================================
    // 统计和报表
    // ============================================
    
    @Override
    public VendorWithdrawalRequest getWithdrawalRequestById(Long requestId) {
        return requestMapper.selectById(requestId);
    }
    
    @Override
    public List<VendorWithdrawalRequest> getAllWithdrawalRequests(VendorWithdrawalRequest request) {
        return requestMapper.selectList(request);
    }
    
    @Override
    public Map<String, Object> getWithdrawalStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计各状态的提现请求数量
        VendorWithdrawalRequest query = new VendorWithdrawalRequest();
        
        query.setRequestStatus("PENDING");
        List<VendorWithdrawalRequest> pendingRequests = requestMapper.selectList(query);
        stats.put("pendingCount", pendingRequests.size());
        BigDecimal pendingAmount = pendingRequests.stream()
            .map(VendorWithdrawalRequest::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("pendingAmount", pendingAmount);
        
        query.setRequestStatus("APPROVED");
        List<VendorWithdrawalRequest> approvedRequests = requestMapper.selectList(query);
        stats.put("approvedCount", approvedRequests.size());
        BigDecimal approvedAmount = approvedRequests.stream()
            .map(VendorWithdrawalRequest::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("approvedAmount", approvedAmount);
        
        query.setRequestStatus("PROCESSING");
        List<VendorWithdrawalRequest> processingRequests = requestMapper.selectList(query);
        stats.put("processingCount", processingRequests.size());
        BigDecimal processingAmount = processingRequests.stream()
            .map(VendorWithdrawalRequest::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("processingAmount", processingAmount);
        
        query.setRequestStatus("COMPLETED");
        List<VendorWithdrawalRequest> completedRequests = requestMapper.selectList(query);
        stats.put("completedCount", completedRequests.size());
        BigDecimal completedAmount = completedRequests.stream()
            .map(VendorWithdrawalRequest::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("completedAmount", completedAmount);
        
        query.setRequestStatus("REJECTED");
        List<VendorWithdrawalRequest> rejectedRequests = requestMapper.selectList(query);
        stats.put("rejectedCount", rejectedRequests.size());
        
        query.setRequestStatus("FAILED");
        List<VendorWithdrawalRequest> failedRequests = requestMapper.selectList(query);
        stats.put("failedCount", failedRequests.size());
        
        // 总计
        int totalCount = pendingRequests.size() + approvedRequests.size() + 
                        processingRequests.size() + completedRequests.size() + 
                        rejectedRequests.size() + failedRequests.size();
        stats.put("totalCount", totalCount);
        
        BigDecimal totalAmount = pendingAmount.add(approvedAmount)
            .add(processingAmount).add(completedAmount);
        stats.put("totalAmount", totalAmount);
        
        return stats;
    }
}

