package com.medusa.mall.mapper;

import java.util.List;
import com.medusa.mall.domain.vendor.VendorLevelHistory;

/**
 * Vendor Level History Mapper Interface
 */
public interface VendorLevelHistoryMapper {
    
    /**
     * Insert level history record
     * 
     * @param history Level history record
     * @return Result
     */
    int insertVendorLevelHistory(VendorLevelHistory history);

    /**
     * Query level history by vendor ID
     * 
     * @param vendorId Vendor ID
     * @return History list
     */
    List<VendorLevelHistory> selectVendorLevelHistoryByVendorId(Long vendorId);
}

