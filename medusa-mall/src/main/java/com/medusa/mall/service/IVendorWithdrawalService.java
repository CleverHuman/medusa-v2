package com.medusa.mall.service;

import com.medusa.mall.domain.vendor.VendorBalanceLog;
import com.medusa.mall.domain.vendor.VendorWithdrawalAddress;
import com.medusa.mall.domain.vendor.VendorWithdrawalRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Vendor 提现服务接口
 * 
 * @author medusa
 * @date 2025-11-18
 */
public interface IVendorWithdrawalService {
    
    // ============================================
    // 余额管理
    // ============================================
    
    /**
     * 查询 Vendor 余额信息
     * 
     * @param vendorId Vendor ID
     * @return 余额信息 {withdrawableBalance, pendingBalance, totalWithdrawn}
     */
    Map<String, BigDecimal> getVendorBalance(Long vendorId);
    
    /**
     * 订单发货后，将订单金额加入待确认余额
     * 根据 vendor level 计算可用日期
     * 
     * @param vendorId Vendor ID
     * @param orderId 订单ID
     * @param amount 订单金额（含运费）
     */
    void addPendingBalanceOnShipment(Long vendorId, Long orderId, BigDecimal amount);
    
    /**
     * 释放到期的待确认余额到可提现余额
     * 由定时任务调用
     */
    void releaseExpiredPendingBalance();
    
    /**
     * 投诉时扣除余额
     * 
     * @param vendorId Vendor ID
     * @param orderId 订单ID
     * @param amount 扣除金额
     */
    void deductBalanceOnDispute(Long vendorId, Long orderId, BigDecimal amount);
    
    /**
     * 查询 Vendor 余额变动记录
     * 
     * @param vendorId Vendor ID
     * @return 余额变动记录列表
     */
    List<VendorBalanceLog> getBalanceLogs(Long vendorId);
    
    // ============================================
    // 提现地址管理
    // ============================================
    
    /**
     * 查询 Vendor 的所有提现地址
     * 
     * @param vendorId Vendor ID
     * @return 提现地址列表
     */
    List<VendorWithdrawalAddress> getWithdrawalAddresses(Long vendorId);
    
    /**
     * 查询特定币种的提现地址
     * 
     * @param vendorId Vendor ID
     * @param currency 币种
     * @return 提现地址
     */
    VendorWithdrawalAddress getWithdrawalAddress(Long vendorId, String currency);
    
    /**
     * 请求更新提现地址（发起 PGP 验证流程）
     * 生成验证码并发送
     * 
     * @param vendorId Vendor ID
     * @param currency 币种
     * @param newAddress 新地址
     * @return 验证流程信息
     */
    Map<String, Object> requestAddressUpdate(Long vendorId, String currency, String newAddress);
    
    /**
     * 验证并更新提现地址
     * 
     * @param vendorId Vendor ID
     * @param currency 币种
     * @param verificationCode 验证码
     * @return 是否成功
     */
    boolean verifyAndUpdateAddress(Long vendorId, String currency, String verificationCode);
    
    /**
     * 创建或更新提现地址（用于 Bond 支付后的地址设置）
     * 
     * @param address 提现地址对象
     * @return 是否成功
     */
    boolean createOrUpdateWithdrawalAddress(VendorWithdrawalAddress address);
    
    // ============================================
    // 提现请求
    // ============================================
    
    /**
     * 创建提现请求
     * 
     * @param vendorId Vendor ID
     * @param currency 币种
     * @param amount 提现金额
     * @return 提现请求
     */
    VendorWithdrawalRequest createWithdrawalRequest(Long vendorId, String currency, BigDecimal amount);
    
    /**
     * 查询 Vendor 的提现请求列表
     * 
     * @param vendorId Vendor ID
     * @return 提现请求列表
     */
    List<VendorWithdrawalRequest> getWithdrawalRequests(Long vendorId);
    
    /**
     * 查询所有待审批的提现请求（Admin用）
     * 
     * @return 提现请求列表
     */
    List<VendorWithdrawalRequest> getPendingWithdrawalRequests();
    
    /**
     * 审批提现请求
     * 
     * @param requestId 提现请求ID
     * @param approved 是否批准
     * @param approveBy 审批人
     * @param remark 备注/拒绝原因
     * @return 是否成功
     */
    boolean approveWithdrawalRequest(Long requestId, boolean approved, String approveBy, String remark);
    
    /**
     * 标记提现请求为处理中
     * 
     * @param requestId 提现请求ID
     * @param txHash 交易哈希
     * @return 是否成功
     */
    boolean markWithdrawalProcessing(Long requestId, String txHash);
    
    /**
     * 标记提现请求为已完成
     * 
     * @param requestId 提现请求ID
     * @param txFee 交易手续费
     * @return 是否成功
     */
    boolean markWithdrawalCompleted(Long requestId, BigDecimal txFee);
    
    /**
     * 标记提现请求为失败
     * 
     * @param requestId 提现请求ID
     * @param reason 失败原因
     * @return 是否成功
     */
    boolean markWithdrawalFailed(Long requestId, String reason);
    
    // ============================================
    // 统计和报表
    // ============================================
    
    /**
     * 根据ID查询提现请求
     * 
     * @param requestId 提现请求ID
     * @return 提现请求
     */
    VendorWithdrawalRequest getWithdrawalRequestById(Long requestId);
    
    /**
     * 查询所有提现请求（支持条件查询）
     * 
     * @param request 查询条件
     * @return 提现请求列表
     */
    List<VendorWithdrawalRequest> getAllWithdrawalRequests(VendorWithdrawalRequest request);
    
    /**
     * 获取提现统计信息
     * 
     * @return 统计信息 Map
     */
    Map<String, Object> getWithdrawalStatistics();
}

