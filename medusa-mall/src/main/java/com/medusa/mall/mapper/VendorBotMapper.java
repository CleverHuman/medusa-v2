package com.medusa.mall.mapper;

import java.util.List;
import com.medusa.mall.domain.vendor.VendorBot;

/**
 * Vendor Bot Mapper Interface
 */
public interface VendorBotMapper {
    
    /**
     * Query vendor bot list
     */
    List<VendorBot> selectVendorBotList(VendorBot vendorBot);

    /**
     * Query vendor bot by ID
     */
    VendorBot selectVendorBotById(Long id);

    /**
     * Query vendor bot by vendor ID
     */
    VendorBot selectVendorBotByVendorId(Long vendorId);

    /**
     * Insert vendor bot
     */
    int insertVendorBot(VendorBot vendorBot);

    /**
     * Update vendor bot
     */
    int updateVendorBot(VendorBot vendorBot);

    /**
     * Delete vendor bot by ID
     */
    int deleteVendorBotById(Long id);
}
