package com.medusa.mall.controller;

import java.util.Date;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.enums.BusinessType;
import com.medusa.common.utils.poi.ExcelUtil;
import com.medusa.mall.domain.vendor.VendorInterview;
import com.medusa.mall.domain.vendor.VendorInterviewSlot;
import com.medusa.mall.service.IVendorInterviewService;
import com.medusa.mall.service.IVendorInterviewSlotService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Vendor Interview Controller
 */
@RestController
@RequestMapping("/admin/mall/vendor/interview")
public class VendorInterviewController extends BaseController {
    
    @Autowired
    private IVendorInterviewService interviewService;
    
    @Autowired
    private IVendorInterviewSlotService slotService;

    /**
     * Query interview list
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:list')")
    @GetMapping("/list")
    public TableDataInfo list(VendorInterview interview) {
        startPage();
        List<VendorInterview> list = interviewService.selectInterviewList(interview);
        return getDataTable(list);
    }

    /**
     * Get calendar data
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:list')")
    @GetMapping("/calendar")
    public AjaxResult getCalendarData(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) String status) {
        List<VendorInterview> list = interviewService.getCalendarData(startDate, endDate, status);
        return AjaxResult.success(list);
    }

    /**
     * Get interview details
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(interviewService.selectInterviewById(id));
    }

    /**
     * Get interviews by application ID
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:query')")
    @GetMapping("/application/{applicationId}")
    public AjaxResult getByApplicationId(@PathVariable("applicationId") Long applicationId) {
        return AjaxResult.success(interviewService.selectInterviewsByApplicationId(applicationId));
    }

    /**
     * Schedule interview
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:add')")
    @Log(title = "Schedule Interview", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VendorInterview interview) {
        return toAjax(interviewService.insertInterview(interview));
    }

    /**
     * Update interview
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:edit')")
    @Log(title = "Update Interview", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VendorInterview interview) {
        return toAjax(interviewService.updateInterview(interview));
    }

    /**
     * Reschedule interview
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:edit')")
    @Log(title = "Reschedule Interview", businessType = BusinessType.UPDATE)
    @PostMapping("/reschedule/{id}")
    public AjaxResult reschedule(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date newDatetime,
            @RequestParam(required = false) String reason) {
        return toAjax(interviewService.rescheduleInterview(id, newDatetime, reason));
    }

    /**
     * Confirm interview (by vendor)
     */
    @PostMapping("/confirm/{id}")
    public AjaxResult confirm(@PathVariable Long id) {
        return toAjax(interviewService.confirmInterview(id));
    }

    /**
     * Complete interview
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:edit')")
    @Log(title = "Complete Interview", businessType = BusinessType.UPDATE)
    @PostMapping("/complete/{id}")
    public AjaxResult complete(
            @PathVariable Long id,
            @RequestParam String result,
            @RequestParam(required = false) Integer score,
            @RequestParam(required = false) String notes) {
        return toAjax(interviewService.completeInterview(id, result, score, notes));
    }

    /**
     * Cancel interview
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:remove')")
    @Log(title = "Cancel Interview", businessType = BusinessType.DELETE)
    @PostMapping("/cancel/{id}")
    public AjaxResult cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return toAjax(interviewService.cancelInterview(id, reason));
    }

    /**
     * Delete interview
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:remove')")
    @Log(title = "Delete Interview", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(interviewService.deleteInterviewByIds(ids));
    }

    /**
     * Export interviews
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:export')")
    @Log(title = "Export Interviews", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VendorInterview interview) {
        List<VendorInterview> list = interviewService.selectInterviewList(interview);
        ExcelUtil<VendorInterview> util = new ExcelUtil<VendorInterview>(VendorInterview.class);
        util.exportExcel(response, list, "Interview Data");
    }

    // ==================== Slot Management (CM) ====================

    /**
     * Query slot list (for CM)
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:list')")
    @GetMapping("/slots")
    public TableDataInfo getSlots(VendorInterviewSlot slot) {
        startPage();
        List<VendorInterviewSlot> list = slotService.selectSlotList(slot);
        return getDataTable(list);
    }

    /**
     * Get available slots by CM and date range
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:list')")
    @GetMapping("/slots/available")
    public AjaxResult getAvailableSlots(
            @RequestParam Long cmId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<VendorInterviewSlot> list = slotService.selectAvailableSlotsByCmAndDateRange(cmId, startDate, endDate);
        return AjaxResult.success(list);
    }

    /**
     * Get all slots by CM ID
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:list')")
    @GetMapping("/slots/cm/{cmId}")
    public AjaxResult getSlotsByCm(@PathVariable Long cmId) {
        List<VendorInterviewSlot> list = slotService.selectSlotsByCmId(cmId);
        return AjaxResult.success(list);
    }

    /**
     * Create slot
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:add')")
    @Log(title = "Create Interview Slot", businessType = BusinessType.INSERT)
    @PostMapping("/slots")
    public AjaxResult createSlot(@RequestBody VendorInterviewSlot slot) {
        return toAjax(slotService.insertSlot(slot));
    }

    /**
     * Batch create slots for date range
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:add')")
    @Log(title = "Batch Create Interview Slots", businessType = BusinessType.INSERT)
    @PostMapping("/slots/batch")
    public AjaxResult createSlots(
            @RequestParam Long cmId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        int count = slotService.createSlotsForDateRange(cmId, startDate, endDate, startTime, endTime);
        return AjaxResult.success("Created " + count + " slots");
    }

    /**
     * Update slot
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:edit')")
    @Log(title = "Update Interview Slot", businessType = BusinessType.UPDATE)
    @PutMapping("/slots")
    public AjaxResult updateSlot(@RequestBody VendorInterviewSlot slot) {
        return toAjax(slotService.updateSlot(slot));
    }

    /**
     * Delete slot
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:remove')")
    @Log(title = "Delete Interview Slot", businessType = BusinessType.DELETE)
    @DeleteMapping("/slots/{ids}")
    public AjaxResult deleteSlot(@PathVariable Long[] ids) {
        return toAjax(slotService.deleteSlotByIds(ids));
    }

    /**
     * Get booking history (reserved slots) for CM
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:list')")
    @GetMapping("/slots/bookings")
    public TableDataInfo getBookingHistory(
            @RequestParam Long cmId,
            @RequestParam(required = false) String status) {
        startPage();
        VendorInterviewSlot query = new VendorInterviewSlot();
        query.setCmId(cmId);
        if (status != null && !status.isEmpty()) {
            query.setStatus(status);
        } else {
            // Default to reserved and completed
            query.setStatus("reserved");
        }
        List<VendorInterviewSlot> list = slotService.selectSlotList(query);
        return getDataTable(list);
    }

    /**
     * Cancel booking (release slot)
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:interview:edit')")
    @Log(title = "Cancel Interview Booking", businessType = BusinessType.UPDATE)
    @PostMapping("/slots/{slotId}/release")
    public AjaxResult releaseSlot(@PathVariable Long slotId) {
        return toAjax(slotService.releaseSlot(slotId));
    }
}

