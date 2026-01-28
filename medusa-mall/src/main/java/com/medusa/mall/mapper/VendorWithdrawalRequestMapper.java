package com.medusa.mall.mapper;

import com.medusa.mall.domain.vendor.VendorWithdrawalRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Vendor 提现请求 Mapper 接口
 * 
 * @author medusa
 * @date 2025-11-18
 */
public interface VendorWithdrawalRequestMapper {
    
    /**
     * 查询提现请求
     * 
     * @param id 提现请求ID
     * @return 提现请求
     */
    VendorWithdrawalRequest selectById(Long id);
    
    /**
     * 根据请求编号查询提现请求
     * 
     * @param requestCode 请求编号
     * @return 提现请求
     */
    VendorWithdrawalRequest selectByRequestCode(String requestCode);
    
    /**
     * 查询 Vendor 的提现请求列表
     * 
     * @param vendorId Vendor ID
     * @return 提现请求列表
     */
    List<VendorWithdrawalRequest> selectByVendorId(Long vendorId);
    
    /**
     * 查询提现请求列表（支持分页和条件）
     * 
     * @param request 提现请求查询条件
     * @return 提现请求列表
     */
    List<VendorWithdrawalRequest> selectList(VendorWithdrawalRequest request);
    
    /**
     * 新增提现请求
     * 
     * @param request 提现请求
     * @return 结果
     */
    int insert(VendorWithdrawalRequest request);
    
    /**
     * 修改提现请求
     * 
     * @param request 提现请求
     * @return 结果
     */
    int update(VendorWithdrawalRequest request);
    
    /**
     * 更新提现请求状态
     * 
     * @param id 提现请求ID
     * @param status 新状态
     * @return 结果
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}

