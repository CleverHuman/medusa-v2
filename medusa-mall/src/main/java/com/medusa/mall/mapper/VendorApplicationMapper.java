package com.medusa.mall.mapper;

import java.util.List;
import com.medusa.mall.domain.vendor.VendorApplication;

/**
 * Vendor Application Mapper Interface
 */
public interface VendorApplicationMapper {
    
    /**
     * Query vendor application list
     * 
     * @param vendorApplication Vendor application query params
     * @return Vendor application collection
     */
    List<VendorApplication> selectVendorApplicationList(VendorApplication vendorApplication);

    /**
     * Query vendor application by ID
     * 
     * @param id Application ID
     * @return Vendor application
     */
    VendorApplication selectVendorApplicationById(Long id);

    /**
     * Query vendor application by application number
     * 
     * @param applicationId Application number
     * @return Vendor application
     */
    VendorApplication selectByApplicationId(String applicationId);

    VendorApplication selectByApplicationIdAndMemberId(String applicationId, Long memberId);

    /**
     * Query approved vendor application by member ID
     * 
     * @param memberId Vendor member ID
     * @return Approved vendor application
     */
    VendorApplication selectApprovedByMemberId(Long memberId);

    /**
     * Insert vendor application
     * 
     * @param vendorApplication Vendor application
     * @return Result
     */
    int insertVendorApplication(VendorApplication vendorApplication);

    /**
     * Update vendor application
     * 
     * @param vendorApplication Vendor application
     * @return Result
     */
    int updateVendorApplication(VendorApplication vendorApplication);

    /**
     * Delete vendor application by ID
     * 
     * @param id Application ID
     * @return Result
     */
    int deleteVendorApplicationById(Long id);

    /**
     * Batch delete vendor applications
     * 
     * @param ids Application IDs to delete
     * @return Result
     */
    int deleteVendorApplicationByIds(Long[] ids);
}

