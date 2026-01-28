package com.medusa.mall.service;

import java.util.List;
import com.medusa.mall.domain.vendor.VendorApplication;

/**
 * Vendor Application Service Interface
 */
public interface IVendorApplicationService {
    
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

    VendorApplication selectByApplicationIdAndMember(String applicationId, Long memberId);

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

    /**
     * Approve application
     * 
     * @param id Application ID
     * @param notes Review notes
     * @return Result
     */
    int approveApplication(Long id, String notes);

    /**
     * Reject application
     * 
     * @param id Application ID
     * @param notes Review notes
     * @return Result
     */
    int rejectApplication(Long id, String notes);

    /**
     * Update review progress
     * 
     * @param id Application ID
     * @param stage Review stage
     * @param progress Review progress percentage
     * @return Result
     */
    int updateReviewProgress(Long id, String stage, Integer progress);

    /**
     * Require interview for application
     * 
     * @param id Application ID
     * @param notes Review notes
     * @return Result
     */
    int requireInterview(Long id, String notes);
}

