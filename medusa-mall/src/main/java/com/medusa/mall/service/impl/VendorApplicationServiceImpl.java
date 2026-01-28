package com.medusa.mall.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medusa.common.utils.SecurityUtils;
import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.mapper.VendorApplicationMapper;
import com.medusa.mall.mapper.VendorMapper;
import com.medusa.mall.service.IVendorApplicationService;

/**
 * Vendor Application Service Implementation
 */
@Service
public class VendorApplicationServiceImpl implements IVendorApplicationService {
    
    @Autowired
    private VendorApplicationMapper vendorApplicationMapper;
    
    @Autowired
    private VendorMapper vendorMapper;

    /**
     * Query vendor application list
     * 
     * @param vendorApplication Vendor application query params
     * @return Vendor application collection
     */
    @Override
    public List<VendorApplication> selectVendorApplicationList(VendorApplication vendorApplication) {
        return vendorApplicationMapper.selectVendorApplicationList(vendorApplication);
    }

    /**
     * Query vendor application by ID
     * 
     * @param id Application ID
     * @return Vendor application
     */
    @Override
    public VendorApplication selectVendorApplicationById(Long id) {
        return vendorApplicationMapper.selectVendorApplicationById(id);
    }

    /**
     * Query vendor application by application number
     * 
     * @param applicationId Application number
     * @return Vendor application
     */
    @Override
    public VendorApplication selectByApplicationId(String applicationId) {
        return vendorApplicationMapper.selectByApplicationId(applicationId);
    }

    @Override
    public VendorApplication selectByApplicationIdAndMember(String applicationId, Long memberId) {
        return vendorApplicationMapper.selectByApplicationIdAndMemberId(applicationId, memberId);
    }

    /**
     * Query approved vendor application by member ID
     * 
     * @param memberId Vendor member ID
     * @return Approved vendor application
     */
    @Override
    public VendorApplication selectApprovedByMemberId(Long memberId) {
        return vendorApplicationMapper.selectApprovedByMemberId(memberId);
    }

    /**
     * Insert vendor application
     * 
     * @param vendorApplication Vendor application
     * @return Result
     */
    @Override
    public int insertVendorApplication(VendorApplication vendorApplication) {
        if (vendorApplication.getMemberId() == null) {
            throw new IllegalArgumentException("Vendor member is required.");
        }
        // Generate application ID if not provided
        if (vendorApplication.getApplicationId() == null || vendorApplication.getApplicationId().isEmpty()) {
            vendorApplication.setApplicationId("VA" + System.currentTimeMillis());
        }
        
        // Set default status
        if (vendorApplication.getStatus() == null || vendorApplication.getStatus().isEmpty()) {
            vendorApplication.setStatus("pending");
        }
        
        // Set default progress
        if (vendorApplication.getReviewProgress() == null) {
            vendorApplication.setReviewProgress(0);
        }
        
        return vendorApplicationMapper.insertVendorApplication(vendorApplication);
    }

    /**
     * Update vendor application
     * 
     * @param vendorApplication Vendor application
     * @return Result
     */
    @Override
    public int updateVendorApplication(VendorApplication vendorApplication) {
        return vendorApplicationMapper.updateVendorApplication(vendorApplication);
    }

    /**
     * Delete vendor application by ID
     * 
     * @param id Application ID
     * @return Result
     */
    @Override
    public int deleteVendorApplicationById(Long id) {
        return vendorApplicationMapper.deleteVendorApplicationById(id);
    }

    /**
     * Batch delete vendor applications
     * 
     * @param ids Application IDs to delete
     * @return Result
     */
    @Override
    public int deleteVendorApplicationByIds(Long[] ids) {
        return vendorApplicationMapper.deleteVendorApplicationByIds(ids);
    }

    /**
     * Approve application and create vendor
     * 
     * @param id Application ID
     * @param notes Review notes
     * @return Result
     */
    @Override
    public int approveApplication(Long id, String notes) {
        VendorApplication application = vendorApplicationMapper.selectVendorApplicationById(id);
        if (application == null) {
            throw new RuntimeException("Application not found");
        }
        
        // Update application status
        application.setStatus("approved");
        application.setReviewNotes(notes);
        application.setReviewedTime(new Date());
        application.setReviewProgress(100);
        
        try {
            application.setReviewerId(SecurityUtils.getUserId());
        } catch (Exception e) {
            // If unable to get current user, set to null
            application.setReviewerId(null);
        }
        
        int result = vendorApplicationMapper.updateVendorApplication(application);
        
        // Create vendor record
        if (result > 0 && application.getVendorId() == null) {
            Vendor vendor = new Vendor();
            vendor.setVendorCode("VD" + System.currentTimeMillis());
            vendor.setVendorName(application.getVendorName());
            vendor.setContactTelegram(application.getPrimaryTelegram());
            vendor.setContactSignal(application.getPrimarySignal());
            vendor.setContactJabber(application.getPrimaryJabber());
            vendor.setContactEmail(application.getPrimaryEmail());
            vendor.setContactThreema(application.getPrimaryThreema());
            vendor.setSecondaryTelegram(application.getSecondaryTelegram());
            vendor.setSecondarySignal(application.getSecondarySignal());
            vendor.setSecondaryJabber(application.getSecondaryJabber());
            vendor.setSecondaryEmail(application.getSecondaryEmail());
            vendor.setSecondaryThreema(application.getSecondaryThreema());
            vendor.setPgpPublicKeyUrl(application.getPgpSignatureUrl());
            vendor.setBtcWallet(application.getBtcWallet());
            vendor.setXmrWallet(application.getXmrWallet());
            vendor.setUsdtWallet(application.getUsdtWallet());
            vendor.setProductCategories(application.getProductCategories());
            vendor.setLocation(application.getLocation());
            vendor.setStatus(1); // Enabled
            vendor.setApplicationId(application.getId());
            vendor.setApprovedTime(new Date());
            
            try {
                vendor.setApprovedBy(SecurityUtils.getUsername());
            } catch (Exception e) {
                vendor.setApprovedBy("system");
            }
            
            vendorMapper.insertVendor(vendor);
            
            // Update application with vendor ID
            application.setVendorId(vendor.getId());
            vendorApplicationMapper.updateVendorApplication(application);
        }
        
        return result;
    }

    /**
     * Reject application
     * 
     * @param id Application ID
     * @param notes Review notes
     * @return Result
     */
    @Override
    public int rejectApplication(Long id, String notes) {
        VendorApplication application = vendorApplicationMapper.selectVendorApplicationById(id);
        if (application == null) {
            throw new RuntimeException("Application not found");
        }
        
        application.setStatus("rejected");
        application.setReviewNotes(notes);
        application.setReviewedTime(new Date());
        application.setReviewProgress(100);
        
        try {
            application.setReviewerId(SecurityUtils.getUserId());
        } catch (Exception e) {
            application.setReviewerId(null);
        }
        
        return vendorApplicationMapper.updateVendorApplication(application);
    }

    /**
     * Update review progress
     * 
     * @param id Application ID
     * @param stage Review stage
     * @param progress Review progress percentage
     * @return Result
     */
    @Override
    public int updateReviewProgress(Long id, String stage, Integer progress) {
        VendorApplication application = vendorApplicationMapper.selectVendorApplicationById(id);
        if (application == null) {
            throw new RuntimeException("Application not found");
        }
        
        application.setReviewStage(stage);
        application.setReviewProgress(progress);
        
        if (progress > 0 && "pending".equals(application.getStatus())) {
            application.setStatus("under_review");
        }
        
        return vendorApplicationMapper.updateVendorApplication(application);
    }

    /**
     * Require interview for application
     * 
     * @param id Application ID
     * @param notes Review notes
     * @return Result
     */
    @Override
    public int requireInterview(Long id, String notes) {
        VendorApplication application = vendorApplicationMapper.selectVendorApplicationById(id);
        if (application == null) {
            throw new RuntimeException("Application not found");
        }
        
        application.setStatus("interview_required");
        application.setReviewNotes(notes);
        application.setReviewProgress(50);
        
        try {
            application.setReviewerId(SecurityUtils.getUserId());
        } catch (Exception e) {
            application.setReviewerId(null);
        }
        
        return vendorApplicationMapper.updateVendorApplication(application);
    }
}

