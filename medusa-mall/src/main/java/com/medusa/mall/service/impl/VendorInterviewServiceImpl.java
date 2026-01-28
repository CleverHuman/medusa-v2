package com.medusa.mall.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.medusa.common.utils.SecurityUtils;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.domain.vendor.VendorInterview;
import com.medusa.mall.mapper.VendorApplicationMapper;
import com.medusa.mall.mapper.VendorInterviewMapper;
import com.medusa.mall.service.IVendorInterviewService;

/**
 * Vendor Interview Service Implementation
 */
@Service
public class VendorInterviewServiceImpl implements IVendorInterviewService {
    
    @Autowired
    private VendorInterviewMapper interviewMapper;
    
    @Autowired
    private VendorApplicationMapper applicationMapper;

    /**
     * Query interview list
     */
    @Override
    public List<VendorInterview> selectInterviewList(VendorInterview interview) {
        return interviewMapper.selectInterviewList(interview);
    }

    /**
     * Query interview by ID
     */
    @Override
    public VendorInterview selectInterviewById(Long id) {
        return interviewMapper.selectInterviewById(id);
    }

    /**
     * Query interviews by application ID
     */
    @Override
    public List<VendorInterview> selectInterviewsByApplicationId(Long applicationId) {
        return interviewMapper.selectInterviewsByApplicationId(applicationId);
    }

    /**
     * Query interviews by date range
     */
    @Override
    public List<VendorInterview> selectInterviewsByDateRange(Date startDate, Date endDate, String status) {
        return interviewMapper.selectInterviewsByDateRange(startDate, endDate, status);
    }

    /**
     * Insert interview
     */
    @Override
    @Transactional
    public int insertInterview(VendorInterview interview) {
        // Set default values
        if (interview.getStatus() == null || interview.getStatus().isEmpty()) {
            interview.setStatus("scheduled");
        }
        if (interview.getDurationMinutes() == null) {
            interview.setDurationMinutes(60);
        }
        if (interview.getVendorConfirmed() == null) {
            interview.setVendorConfirmed(0);
        }
        if (interview.getRescheduleCount() == null) {
            interview.setRescheduleCount(0);
        }
        if (interview.getTimezone() == null) {
            interview.setTimezone("UTC");
        }
        
        // Get application info if applicationId is provided
        if (interview.getApplicationId() != null) {
            VendorApplication application = applicationMapper.selectVendorApplicationById(interview.getApplicationId());
            if (application != null) {
                interview.setApplicationNumber(application.getApplicationId());
                interview.setVendorName(application.getVendorName());
                
                // Update application with interview flag
                application.setHasScheduledInterview(1);
                application.setNextInterviewTime(interview.getInterviewDatetime());
                applicationMapper.updateVendorApplication(application);
            }
        }
        
        return interviewMapper.insertInterview(interview);
    }

    /**
     * Update interview
     */
    @Override
    public int updateInterview(VendorInterview interview) {
        return interviewMapper.updateInterview(interview);
    }

    /**
     * Delete interview by ID
     */
    @Override
    public int deleteInterviewById(Long id) {
        return interviewMapper.deleteInterviewById(id);
    }

    /**
     * Batch delete interviews
     */
    @Override
    public int deleteInterviewByIds(Long[] ids) {
        return interviewMapper.deleteInterviewByIds(ids);
    }

    /**
     * Schedule interview for application
     */
    @Override
    @Transactional
    public int scheduleInterview(Long applicationId, VendorInterview interview) {
        interview.setApplicationId(applicationId);
        return insertInterview(interview);
    }

    /**
     * Reschedule interview
     */
    @Override
    @Transactional
    public int rescheduleInterview(Long id, Date newDatetime, String reason) {
        VendorInterview interview = interviewMapper.selectInterviewById(id);
        if (interview == null) {
            throw new RuntimeException("Interview not found");
        }
        
        // Save previous datetime
        interview.setPreviousDatetime(interview.getInterviewDatetime());
        
        // Update to new datetime
        interview.setInterviewDatetime(newDatetime);
        
        // Extract date and time
        java.sql.Date sqlDate = new java.sql.Date(newDatetime.getTime());
        java.sql.Time sqlTime = new java.sql.Time(newDatetime.getTime());
        interview.setInterviewDate(sqlDate);
        interview.setInterviewTime(sqlTime.toString());
        
        // Update reschedule info
        interview.setRescheduleCount(interview.getRescheduleCount() + 1);
        interview.setRescheduleReason(reason);
        interview.setStatus("rescheduled");
        interview.setVendorConfirmed(0); // Reset confirmation
        
        // Update application
        if (interview.getApplicationId() != null) {
            VendorApplication application = applicationMapper.selectVendorApplicationById(interview.getApplicationId());
            if (application != null) {
                application.setNextInterviewTime(newDatetime);
                applicationMapper.updateVendorApplication(application);
            }
        }
        
        return interviewMapper.updateInterview(interview);
    }

    /**
     * Confirm interview (by vendor)
     */
    @Override
    public int confirmInterview(Long id) {
        VendorInterview interview = interviewMapper.selectInterviewById(id);
        if (interview == null) {
            throw new RuntimeException("Interview not found");
        }
        
        interview.setVendorConfirmed(1);
        interview.setVendorConfirmedTime(new Date());
        interview.setStatus("confirmed");
        
        return interviewMapper.updateInterview(interview);
    }

    /**
     * Complete interview with result
     */
    @Override
    @Transactional
    public int completeInterview(Long id, String result, Integer score, String notes) {
        VendorInterview interview = interviewMapper.selectInterviewById(id);
        if (interview == null) {
            throw new RuntimeException("Interview not found");
        }
        
        interview.setStatus("completed");
        interview.setInterviewResult(result);
        interview.setInterviewScore(score);
        interview.setInterviewNotes(notes);
        
        int updateResult = interviewMapper.updateInterview(interview);
        
        // Update application based on interview result
        if (interview.getApplicationId() != null && "passed".equals(result)) {
            VendorApplication application = applicationMapper.selectVendorApplicationById(interview.getApplicationId());
            if (application != null && "interview_required".equals(application.getStatus())) {
                // Move application to under_review after passing interview
                application.setStatus("under_review");
                application.setReviewProgress(60);
                applicationMapper.updateVendorApplication(application);
            }
        }
        
        return updateResult;
    }

    /**
     * Cancel interview
     */
    @Override
    @Transactional
    public int cancelInterview(Long id, String reason) {
        VendorInterview interview = interviewMapper.selectInterviewById(id);
        if (interview == null) {
            throw new RuntimeException("Interview not found");
        }
        
        interview.setStatus("cancelled");
        interview.setRescheduleReason(reason);
        
        // Update application
        if (interview.getApplicationId() != null) {
            // Check if there are other scheduled interviews
            int scheduledCount = interviewMapper.countScheduledInterviews(interview.getApplicationId());
            if (scheduledCount <= 1) { // Current interview will be cancelled, so <= 1
                VendorApplication application = applicationMapper.selectVendorApplicationById(interview.getApplicationId());
                if (application != null) {
                    application.setHasScheduledInterview(0);
                    application.setNextInterviewTime(null);
                    applicationMapper.updateVendorApplication(application);
                }
            }
        }
        
        return interviewMapper.updateInterview(interview);
    }

    /**
     * Get calendar data for date range
     */
    @Override
    public List<VendorInterview> getCalendarData(Date startDate, Date endDate, String status) {
        return interviewMapper.selectInterviewsByDateRange(startDate, endDate, status);
    }
}

