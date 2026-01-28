package com.medusa.mall.service;

import java.util.Date;
import java.util.List;
import com.medusa.mall.domain.vendor.VendorInterviewSlot;

/**
 * Vendor Interview Slot Service Interface
 */
public interface IVendorInterviewSlotService {
    
    /**
     * Query slot list
     * 
     * @param slot Slot query params
     * @return Slot collection
     */
    List<VendorInterviewSlot> selectSlotList(VendorInterviewSlot slot);
    
    /**
     * Query slot by ID
     * 
     * @param id Slot ID
     * @return Slot
     */
    VendorInterviewSlot selectSlotById(Long id);
    
    /**
     * Query available slots by CM ID and date range
     * 
     * @param cmId CM user ID
     * @param startDate Start date
     * @param endDate End date
     * @return Slot collection
     */
    List<VendorInterviewSlot> selectAvailableSlotsByCmAndDateRange(Long cmId, Date startDate, Date endDate);
    
    /**
     * Query all available slots (for vendors to browse)
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Slot collection
     */
    List<VendorInterviewSlot> selectAllAvailableSlots(Date startDate, Date endDate);
    
    /**
     * Query slots reserved by vendor
     * 
     * @param vendorId Vendor ID (from application)
     * @return Slot collection
     */
    List<VendorInterviewSlot> selectSlotsByVendorId(Long vendorId);
    
    /**
     * Query slots by CM ID
     * 
     * @param cmId CM user ID
     * @return Slot collection
     */
    List<VendorInterviewSlot> selectSlotsByCmId(Long cmId);
    
    /**
     * Insert slot
     * 
     * @param slot Slot
     * @return Result
     */
    int insertSlot(VendorInterviewSlot slot);
    
    /**
     * Batch insert slots
     * 
     * @param slots Slot list
     * @return Result
     */
    int insertSlots(List<VendorInterviewSlot> slots);
    
    /**
     * Update slot
     * 
     * @param slot Slot
     * @return Result
     */
    int updateSlot(VendorInterviewSlot slot);
    
    /**
     * Reserve slot (link to interview)
     * 
     * @param slotId Slot ID
     * @param interviewId Interview ID
     * @param vendorId Vendor ID
     * @return Result
     */
    int reserveSlot(Long slotId, Long interviewId, Long vendorId);
    
    /**
     * Release slot (make it available again)
     * 
     * @param slotId Slot ID
     * @return Result
     */
    int releaseSlot(Long slotId);
    
    /**
     * Delete slot by ID
     * 
     * @param id Slot ID
     * @return Result
     */
    int deleteSlotById(Long id);
    
    /**
     * Batch delete slots
     * 
     * @param ids Slot IDs to delete
     * @return Result
     */
    int deleteSlotByIds(Long[] ids);
    
    /**
     * Check if slot is available
     * 
     * @param slotId Slot ID
     * @return true if available
     */
    boolean isSlotAvailable(Long slotId);
    
    /**
     * Create slots for a date range (1 hour each)
     * 
     * @param cmId CM user ID
     * @param startDate Start date
     * @param endDate End date
     * @param startTime Start time (HH:mm format)
     * @param endTime End time (HH:mm format)
     * @return Number of slots created
     */
    int createSlotsForDateRange(Long cmId, Date startDate, Date endDate, String startTime, String endTime);
}

