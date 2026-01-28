package com.medusa.mall.controller.api;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.domain.vendor.VendorInterview;
import com.medusa.mall.domain.vendor.VendorInterviewSlot;
import com.medusa.mall.service.IVendorInterviewService;
import com.medusa.mall.service.IVendorInterviewSlotService;
import com.medusa.mall.service.IVendorApplicationService;
import com.medusa.mall.util.VendorAuthUtils;

/**
 * Vendor Interview API Controller (Public API for Vendors)
 */
@RestController
@RequestMapping("/api/mall/vendor/interview")
public class ApiVendorInterviewController extends BaseController {
    
    @Autowired
    private IVendorInterviewService interviewService;
    
    @Autowired
    private IVendorInterviewSlotService slotService;
    
    @Autowired
    private IVendorApplicationService vendorApplicationService;

    /**
     * Get interviews by application number (Public API)
     * Vendors can use this to check their interview schedule
     */
    @GetMapping("/application/{applicationNumber}")
    public AjaxResult getByApplicationNumber(@PathVariable("applicationNumber") String applicationNumber) {
        VendorInterview query = new VendorInterview();
        query.setApplicationNumber(applicationNumber);
        List<VendorInterview> list = interviewService.selectInterviewList(query);
        return AjaxResult.success(list);
    }

    /**
     * Confirm interview (Public API)
     * Vendors can confirm their interview attendance
     */
    @PostMapping("/confirm/{id}")
    public AjaxResult confirm(@PathVariable Long id) {
        return toAjax(interviewService.confirmInterview(id));
    }

    // ==================== Slot Booking (Vendor) ====================

    /**
     * Get all available slots (for vendors to browse)
     */
    @GetMapping("/slots/available")
    public AjaxResult getAvailableSlots(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            logger.info("[VendorSlots] Requesting available slots. startDate={}, endDate={}",
                    DateFormatUtils.format(startDate, "yyyy-MM-dd HH:mm:ss"),
                    DateFormatUtils.format(endDate, "yyyy-MM-dd HH:mm:ss"));
            List<VendorInterviewSlot> slots = slotService.selectAllAvailableSlots(startDate, endDate);
            logger.info("[VendorSlots] Available slots found: count={}, slotIds={}",
                    slots == null ? 0 : slots.size(),
                    slots == null ? "[]" : slots.stream()
                            .map(slot -> slot.getId() + ":" + DateFormatUtils.format(slot.getSlotStart(), "yyyy-MM-dd HH:mm"))
                            .collect(Collectors.toList()));
            return AjaxResult.success(slots);
        } catch (Exception e) {
            logger.error("Failed to get available slots", e);
            return AjaxResult.error("Failed to get available slots: " + e.getMessage());
        }
    }

    /**
     * Book a slot (create interview)
     */
    @PostMapping("/slots/{slotId}/book")
    public AjaxResult bookSlot(
            @PathVariable Long slotId,
            @RequestParam String applicationNumber) {
        try {
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login first");
            }

            // Get application by application number
            VendorApplication application = vendorApplicationService.selectByApplicationIdAndMember(applicationNumber, memberId);
            if (application == null) {
                return AjaxResult.error("Application not found");
            }

            // Verify application belongs to current user (optional check)
            // For now, we allow booking if application number matches

            // Check if slot is available
            if (!slotService.isSlotAvailable(slotId)) {
                return AjaxResult.error("Slot is no longer available");
            }

            // Get slot details
            VendorInterviewSlot slot = slotService.selectSlotById(slotId);
            if (slot == null) {
                return AjaxResult.error("Slot not found");
            }

            // Create interview
            VendorInterview interview = new VendorInterview();
            interview.setApplicationId(application.getId());
            interview.setApplicationNumber(applicationNumber);
            interview.setVendorName(application.getVendorName());
            interview.setSlotId(slotId);
            interview.setCmId(slot.getCmId());
            interview.setInterviewDatetime(slot.getSlotStart());
            interview.setInterviewDate(new java.sql.Date(slot.getSlotStart().getTime()));
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(slot.getSlotStart());
            interview.setInterviewTime(String.format("%02d:%02d", 
                cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE)));
            interview.setDurationMinutes(60); // Fixed 1 hour
            interview.setStatus("scheduled");
            interview.setInterviewType("video"); // Default
            interview.setVendorConfirmed(0);
            interview.setCmNotified(0);
            interview.setVendorNotified(0);

            // Insert interview
            int result = interviewService.insertInterview(interview);
            if (result > 0) {
                // Reserve the slot
                slotService.reserveSlot(slotId, interview.getId(), application.getId());
                
                // TODO: Send notifications to CM and Vendor
                // This would be implemented in a notification service
                
                return AjaxResult.success("Slot booked successfully", interview);
            } else {
                return AjaxResult.error("Failed to book slot");
            }
        } catch (Exception e) {
            logger.error("Failed to book slot", e);
            return AjaxResult.error("Failed to book slot: " + e.getMessage());
        }
    }

    /**
     * Get my bookings (interviews for current vendor)
     */
    @GetMapping("/my-bookings")
    public AjaxResult getMyBookings(@RequestParam String applicationNumber) {
        try {
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login first");
            }
            // Get application
            VendorApplication application = vendorApplicationService.selectByApplicationIdAndMember(applicationNumber, memberId);
            if (application == null) {
                return AjaxResult.error("Application not found");
            }

            // Get interviews for this application
            List<VendorInterview> interviews = interviewService.selectInterviewsByApplicationId(application.getId());
            return AjaxResult.success(interviews);
        } catch (Exception e) {
            logger.error("Failed to get my bookings", e);
            return AjaxResult.error("Failed to get bookings: " + e.getMessage());
        }
    }

    /**
     * Get my booked slots (with slot details)
     */
    @GetMapping("/my-slots")
    public AjaxResult getMySlots(@RequestParam String applicationNumber) {
        try {
            Long memberId = VendorAuthUtils.getCurrentVendorMemberId();
            if (memberId == null) {
                return AjaxResult.error("Please login first");
            }
            // Get application
            VendorApplication application = vendorApplicationService.selectByApplicationIdAndMember(applicationNumber, memberId);
            if (application == null) {
                return AjaxResult.error("Application not found");
            }

            // Get slots reserved by this vendor
            List<VendorInterviewSlot> slots = slotService.selectSlotsByVendorId(application.getId());
            return AjaxResult.success(slots);
        } catch (Exception e) {
            logger.error("Failed to get my slots", e);
            return AjaxResult.error("Failed to get slots: " + e.getMessage());
        }
    }
}

