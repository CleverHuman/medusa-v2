package com.medusa.mall.mapper;

import java.util.Date;
import java.util.List;
import com.medusa.mall.domain.vendor.VendorInterview;
import org.apache.ibatis.annotations.Param;

/**
 * Vendor Interview Mapper Interface
 */
public interface VendorInterviewMapper {
    
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
    List<VendorInterview> selectInterviewsByDateRange(@Param("startDate") Date startDate, 
                                                        @Param("endDate") Date endDate,
                                                        @Param("status") String status);

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
     * Check if application has scheduled interview
     * 
     * @param applicationId Application ID
     * @return Count
     */
    int countScheduledInterviews(@Param("applicationId") Long applicationId);
}

