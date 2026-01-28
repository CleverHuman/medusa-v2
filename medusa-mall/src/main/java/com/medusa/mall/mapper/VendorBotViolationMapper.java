package com.medusa.mall.mapper;

import java.util.List;
import java.util.Date;
import com.medusa.mall.domain.vendor.VendorBotViolation;

/**
 * Vendor Bot Violation Mapper Interface
 */
public interface VendorBotViolationMapper {
    
    /**
     * Query violation list
     */
    List<VendorBotViolation> selectVendorBotViolationList(VendorBotViolation violation);

    /**
     * Query violation by ID
     */
    VendorBotViolation selectVendorBotViolationById(Long id);

    /**
     * Count today's violations for a vendor
     */
    int countTodayViolationsByVendorId(Long vendorId);

    /**
     * Insert violation
     */
    int insertVendorBotViolation(VendorBotViolation violation);

    /**
     * Update violation
     */
    int updateVendorBotViolation(VendorBotViolation violation);

    /**
     * Update violation status
     */
    int updateVendorBotViolationStatus(Long id, String status);
}
