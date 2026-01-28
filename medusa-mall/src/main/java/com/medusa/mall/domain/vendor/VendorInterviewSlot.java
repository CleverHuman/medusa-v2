package com.medusa.mall.domain.vendor;

import java.util.Date;
import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Vendor Interview Slot Entity
 */
@ApiModel("Vendor Interview Slot")
public class VendorInterviewSlot extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Slot ID")
    private Long id;

    @ApiModelProperty("CM (interviewer) user ID")
    @Excel(name = "CM ID")
    private Long cmId;

    @ApiModelProperty("Slot start time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "Start Time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date slotStart;

    @ApiModelProperty("Slot end time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "End Time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date slotEnd;

    @ApiModelProperty("Slot status")
    @Excel(name = "Status")
    private String status; // available, reserved, completed, cancelled

    @ApiModelProperty("Linked interview ID when reserved")
    private Long reservedInterviewId;

    @ApiModelProperty("Vendor ID who booked this slot")
    private Long reservedVendorId;

    @ApiModelProperty("Vendor name who booked this slot")
    private String reservedVendorName;

    @ApiModelProperty("Application primary ID linked when reserved")
    private Long reservedApplicationId;

    @ApiModelProperty("Application code when reserved")
    private String reservedApplicationCode;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCmId() {
        return cmId;
    }

    public void setCmId(Long cmId) {
        this.cmId = cmId;
    }

    public Date getSlotStart() {
        return slotStart;
    }

    public void setSlotStart(Date slotStart) {
        this.slotStart = slotStart;
    }

    public Date getSlotEnd() {
        return slotEnd;
    }

    public void setSlotEnd(Date slotEnd) {
        this.slotEnd = slotEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReservedInterviewId() {
        return reservedInterviewId;
    }

    public void setReservedInterviewId(Long reservedInterviewId) {
        this.reservedInterviewId = reservedInterviewId;
    }

    public Long getReservedVendorId() {
        return reservedVendorId;
    }

    public void setReservedVendorId(Long reservedVendorId) {
        this.reservedVendorId = reservedVendorId;
    }

    public String getReservedVendorName() {
        return reservedVendorName;
    }

    public void setReservedVendorName(String reservedVendorName) {
        this.reservedVendorName = reservedVendorName;
    }

    public Long getReservedApplicationId() {
        return reservedApplicationId;
    }

    public void setReservedApplicationId(Long reservedApplicationId) {
        this.reservedApplicationId = reservedApplicationId;
    }

    public String getReservedApplicationCode() {
        return reservedApplicationCode;
    }

    public void setReservedApplicationCode(String reservedApplicationCode) {
        this.reservedApplicationCode = reservedApplicationCode;
    }

    @Override
    public String toString() {
        return "VendorInterviewSlot{" +
                "id=" + id +
                ", cmId=" + cmId +
                ", slotStart=" + slotStart +
                ", slotEnd=" + slotEnd +
                ", status='" + status + '\'' +
                ", reservedInterviewId=" + reservedInterviewId +
                ", reservedVendorId=" + reservedVendorId +
                ", reservedVendorName='" + reservedVendorName + '\'' +
                ", reservedApplicationId=" + reservedApplicationId +
                ", reservedApplicationCode='" + reservedApplicationCode + '\'' +
                '}';
    }
}

