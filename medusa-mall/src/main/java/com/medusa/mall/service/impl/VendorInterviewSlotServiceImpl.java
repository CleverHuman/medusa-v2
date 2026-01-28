package com.medusa.mall.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.medusa.common.utils.SecurityUtils;
import com.medusa.mall.domain.vendor.VendorInterviewSlot;
import com.medusa.mall.mapper.VendorInterviewSlotMapper;
import com.medusa.mall.service.IVendorInterviewSlotService;

/**
 * Vendor Interview Slot Service Implementation
 */
@Service
public class VendorInterviewSlotServiceImpl implements IVendorInterviewSlotService {
    
    @Autowired
    private VendorInterviewSlotMapper slotMapper;

    @Override
    public List<VendorInterviewSlot> selectSlotList(VendorInterviewSlot slot) {
        return slotMapper.selectSlotList(slot);
    }

    @Override
    public VendorInterviewSlot selectSlotById(Long id) {
        return slotMapper.selectSlotById(id);
    }

    @Override
    public List<VendorInterviewSlot> selectAvailableSlotsByCmAndDateRange(Long cmId, Date startDate, Date endDate) {
        return slotMapper.selectAvailableSlotsByCmAndDateRange(cmId, startDate, endDate);
    }

    @Override
    public List<VendorInterviewSlot> selectAllAvailableSlots(Date startDate, Date endDate) {
        return slotMapper.selectAllAvailableSlots(startDate, endDate);
    }

    @Override
    public List<VendorInterviewSlot> selectSlotsByVendorId(Long vendorId) {
        return slotMapper.selectSlotsByVendorId(vendorId);
    }

    @Override
    public List<VendorInterviewSlot> selectSlotsByCmId(Long cmId) {
        return slotMapper.selectSlotsByCmId(cmId);
    }

    @Override
    @Transactional
    public int insertSlot(VendorInterviewSlot slot) {
        if (slot.getStatus() == null) {
            slot.setStatus("available");
        }
        if (slot.getCreateBy() == null) {
            try {
                String username = SecurityUtils.getUsername();
                slot.setCreateBy(username);
            } catch (Exception e) {
                // If not authenticated, use system
                slot.setCreateBy("system");
            }
        }
        return slotMapper.insertSlot(slot);
    }

    @Override
    @Transactional
    public int insertSlots(List<VendorInterviewSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return 0;
        }
        String username = "system";
        try {
            username = SecurityUtils.getUsername();
        } catch (Exception e) {
            // Use system if not authenticated
        }
        for (VendorInterviewSlot slot : slots) {
            if (slot.getStatus() == null) {
                slot.setStatus("available");
            }
            if (slot.getCreateBy() == null) {
                slot.setCreateBy(username);
            }
        }
        return slotMapper.insertSlots(slots);
    }

    @Override
    @Transactional
    public int updateSlot(VendorInterviewSlot slot) {
        if (slot.getUpdateBy() == null) {
            try {
                String username = SecurityUtils.getUsername();
                slot.setUpdateBy(username);
            } catch (Exception e) {
                slot.setUpdateBy("system");
            }
        }
        return slotMapper.updateSlot(slot);
    }

    @Override
    @Transactional
    public int reserveSlot(Long slotId, Long interviewId, Long vendorId) {
        return slotMapper.reserveSlot(slotId, interviewId, vendorId);
    }

    @Override
    @Transactional
    public int releaseSlot(Long slotId) {
        return slotMapper.releaseSlot(slotId);
    }

    @Override
    @Transactional
    public int deleteSlotById(Long id) {
        return slotMapper.deleteSlotById(id);
    }

    @Override
    @Transactional
    public int deleteSlotByIds(Long[] ids) {
        return slotMapper.deleteSlotByIds(ids);
    }

    @Override
    public boolean isSlotAvailable(Long slotId) {
        return slotMapper.checkSlotAvailable(slotId) > 0;
    }

    @Override
    @Transactional
    public int createSlotsForDateRange(Long cmId, Date startDate, Date endDate, String startTime, String endTime) {
        List<VendorInterviewSlot> slots = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        try {
            // Parse start and end times
            Date startTimeObj = timeFormat.parse(startTime);
            Date endTimeObj = timeFormat.parse(endTime);
            
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(startTimeObj);
            int startHour = timeCal.get(Calendar.HOUR_OF_DAY);
            int startMinute = timeCal.get(Calendar.MINUTE);
            
            timeCal.setTime(endTimeObj);
            int endHour = timeCal.get(Calendar.HOUR_OF_DAY);
            int endMinute = timeCal.get(Calendar.MINUTE);
            
            // Generate slots for each day
            Calendar currentDate = (Calendar) startCal.clone();
            while (!currentDate.after(endCal)) {
                Calendar slotStart = (Calendar) currentDate.clone();
                slotStart.set(Calendar.HOUR_OF_DAY, startHour);
                slotStart.set(Calendar.MINUTE, startMinute);
                slotStart.set(Calendar.SECOND, 0);
                slotStart.set(Calendar.MILLISECOND, 0);
                
                Calendar slotEnd = (Calendar) slotStart.clone();
                slotEnd.add(Calendar.HOUR, 1); // 1 hour slots
                
                Calendar dayEnd = (Calendar) currentDate.clone();
                dayEnd.set(Calendar.HOUR_OF_DAY, endHour);
                dayEnd.set(Calendar.MINUTE, endMinute);
                dayEnd.set(Calendar.SECOND, 0);
                dayEnd.set(Calendar.MILLISECOND, 0);
                
                // Create slots for this day
                while (!slotStart.after(dayEnd) && slotEnd.before(dayEnd) || slotEnd.equals(dayEnd)) {
                    VendorInterviewSlot slot = new VendorInterviewSlot();
                    slot.setCmId(cmId);
                    slot.setSlotStart(slotStart.getTime());
                    slot.setSlotEnd(slotEnd.getTime());
                    slot.setStatus("available");
                    slots.add(slot);
                    
                    // Move to next hour
                    slotStart.add(Calendar.HOUR, 1);
                    slotEnd.add(Calendar.HOUR, 1);
                }
                
                // Move to next day
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            if (!slots.isEmpty()) {
                return insertSlots(slots);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse time format", e);
        }
        
        return 0;
    }
}

