package com.medusa.mall.service;

import java.util.List;
import com.medusa.mall.domain.vendor.VendorBotViolation;

/**
 * Vendor Bot Violation Service Interface
 */
public interface IVendorBotViolationService {
    
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
