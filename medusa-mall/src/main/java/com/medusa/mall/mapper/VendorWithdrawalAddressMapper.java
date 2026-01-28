package com.medusa.mall.mapper;

import com.medusa.mall.domain.vendor.VendorWithdrawalAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Vendor 提现地址 Mapper 接口
 * 
 * @author medusa
 * @date 2025-11-18
 */
public interface VendorWithdrawalAddressMapper {
    
    /**
     * 查询提现地址
     * 
     * @param id 提现地址ID
     * @return 提现地址
     */
    VendorWithdrawalAddress selectById(Long id);
    
    /**
     * 查询 Vendor 的提现地址列表
     * 
     * @param vendorId Vendor ID
     * @return 提现地址列表
     */
    List<VendorWithdrawalAddress> selectByVendorId(Long vendorId);
    
    /**
     * 查询 Vendor 特定币种的提现地址
     * 
     * @param vendorId Vendor ID
     * @param currency 币种
     * @return 提现地址
     */
    VendorWithdrawalAddress selectByVendorIdAndCurrency(@Param("vendorId") Long vendorId, @Param("currency") String currency);
    
    /**
     * 新增提现地址
     * 
     * @param address 提现地址
     * @return 结果
     */
    int insert(VendorWithdrawalAddress address);
    
    /**
     * 修改提现地址
     * 
     * @param address 提现地址
     * @return 结果
     */
    int update(VendorWithdrawalAddress address);
    
    /**
     * 删除提现地址
     * 
     * @param id 提现地址ID
     * @return 结果
     */
    int deleteById(Long id);
}

