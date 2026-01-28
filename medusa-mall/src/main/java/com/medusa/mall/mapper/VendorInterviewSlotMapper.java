package com.medusa.mall.mapper;

import java.util.Date;
import java.util.List;
import com.medusa.mall.domain.vendor.VendorInterviewSlot;
import org.apache.ibatis.annotations.Param;

/**
 * Vendor Interview Slot Mapper Interface
 */
public interface VendorInterviewSlotMapper {
    
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
    List<VendorInterviewSlot> selectAvailableSlotsByCmAndDateRange(
        @Param("cmId") Long cmId,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate);

    /**
     * Query all available slots (for vendors to browse)
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Slot collection
     */
    List<VendorInterviewSlot> selectAllAvailableSlots(
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate);

    /**
     * Query slots reserved by vendor
     * 
     * @param vendorId Vendor ID
     * @return Slot collection
     */
    List<VendorInterviewSlot> selectSlotsByVendorId(@Param("vendorId") Long vendorId);

    /**
     * Query slots by CM ID
     * 
     * @param cmId CM user ID
     * @return Slot collection
     */
    List<VendorInterviewSlot> selectSlotsByCmId(@Param("cmId") Long cmId);

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
     * Reserve slot (update status and link to interview)
     * 
     * @param slotId Slot ID
     * @param interviewId Interview ID
     * @param vendorId Vendor ID
     * @return Result
     */
    int reserveSlot(@Param("slotId") Long slotId,
                    @Param("interviewId") Long interviewId,
                    @Param("vendorId") Long vendorId);

    /**
     * Release slot (make it available again)
     * 
     * @param slotId Slot ID
     * @return Result
     */
    int releaseSlot(@Param("slotId") Long slotId);

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
     * @return Count (should be 1 if available)
     */
    int checkSlotAvailable(@Param("slotId") Long slotId);
}

