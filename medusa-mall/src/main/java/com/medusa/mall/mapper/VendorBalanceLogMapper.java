package com.medusa.mall.mapper;

import com.medusa.mall.domain.vendor.VendorBalanceLog;

import java.util.List;

/**
 * Vendor 余额变动记录 Mapper 接口
 * 
 * @author medusa
 * @date 2025-11-18
 */
public interface VendorBalanceLogMapper {
    
    /**
     * 查询余额变动记录
     * 
     * @param id 记录ID
     * @return 余额变动记录
     */
    VendorBalanceLog selectById(Long id);
    
    /**
     * 查询 Vendor 的余额变动记录列表
     * 
     * @param vendorId Vendor ID
     * @return 余额变动记录列表
     */
    List<VendorBalanceLog> selectByVendorId(Long vendorId);
    
    /**
     * 查询余额变动记录列表（支持分页和条件）
     * 
     * @param log 余额变动记录查询条件
     * @return 余额变动记录列表
     */
    List<VendorBalanceLog> selectList(VendorBalanceLog log);
    
    /**
     * 新增余额变动记录
     * 
     * @param log 余额变动记录
     * @return 结果
     */
    int insert(VendorBalanceLog log);
}

