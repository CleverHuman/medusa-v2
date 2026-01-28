package com.medusa.mall.service;

import java.util.Date;
import java.util.List;
import com.medusa.mall.domain.vendor.VendorInterview;

/**
 * Vendor Interview Service Interface
 */
public interface IVendorInterviewService {
    
    /**
     * Query interview list
     * 
     * @param interview Interview query params
     * @return Interview collection
     */
    List<VendorInterview> selectInterviewList(VendorInterview interview);
    
    /**
     * Query interview by ID
     * 
     * @param id Interview ID
     * @return Interview
     */
    VendorInterview selectInterviewById(Long id);
    
    /**
     * Query interviews by application ID
     * 
     * @param applicationId Application ID
     * @return Interview collection
     */
    List<VendorInterview> selectInterviewsByApplicationId(Long applicationId);
    
    /**
     * Query interviews by date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @param status Status filter (optional)
     * @return Interview collection
     */
    List<VendorInterview> selectInterviewsByDateRange(Date startDate, Date endDate, String status);
    
    /**
     * Insert interview
     * 
     * @param interview Interview
     * @return Result
     */
    int insertInterview(VendorInterview interview);
    
    /**
     * Update interview
     * 
     * @param interview Interview
     * @return Result
     */
    int updateInterview(VendorInterview interview);
    
    /**
     * Delete interview by ID
     * 
     * @param id Interview ID
     * @return Result
     */
    int deleteInterviewById(Long id);
    
    /**
     * Batch delete interviews
     * 
     * @param ids Interview IDs to delete
     * @return Result
     */
    int deleteInterviewByIds(Long[] ids);
    
    /**
     * Schedule interview for application
     * 
     * @param applicationId Application ID
     * @param interview Interview details
     * @return Result
     */
    int scheduleInterview(Long applicationId, VendorInterview interview);
    
    /**
     * Reschedule interview
     * 
     * @param id Interview ID
     * @param newDatetime New datetime
     * @param reason Reschedule reason
     * @return Result
     */
    int rescheduleInterview(Long id, Date newDatetime, String reason);
    
    /**
     * Confirm interview (by vendor)
     * 
     * @param id Interview ID
     * @return Result
     */
    int confirmInterview(Long id);
    
    /**
     * Complete interview with result
     * 
     * @param id Interview ID
     * @param result Interview result
     * @param score Interview score
     * @param notes Interview notes
     * @return Result
     */
    int completeInterview(Long id, String result, Integer score, String notes);
    
    /**
     * Cancel interview
     * 
     * @param id Interview ID
     * @param reason Cancel reason
     * @return Result
     */
    int cancelInterview(Long id, String reason);
    
    /**
     * Get calendar data for date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @param status Status filter (optional)
     * @return Interview collection
     */
    List<VendorInterview> getCalendarData(Date startDate, Date endDate, String status);
}

