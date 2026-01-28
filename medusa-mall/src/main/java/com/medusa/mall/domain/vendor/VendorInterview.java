package com.medusa.mall.domain.vendor;

import java.util.Date;
import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Vendor Interview Entity
 */
@ApiModel("Vendor Interview")
public class VendorInterview extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Interview ID")
    private Long id;

    @ApiModelProperty("Application ID")
    @Excel(name = "Application ID")
    private Long applicationId;

    @ApiModelProperty("Slot ID")
    private Long slotId;

    @ApiModelProperty("Application Number")
    @Excel(name = "Application Number")
    private String applicationNumber;

    @ApiModelProperty("Vendor Name")
    @Excel(name = "Vendor Name")
    private String vendorName;

    @ApiModelProperty("Interview Date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "Interview Date", dateFormat = "yyyy-MM-dd")
    private Date interviewDate;

    @ApiModelProperty("Interview Time")
    @Excel(name = "Interview Time")
    private String interviewTime;

    @ApiModelProperty("Interview DateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date interviewDatetime;

    @ApiModelProperty("Duration (minutes)")
    @Excel(name = "Duration")
    private Integer durationMinutes;

    @ApiModelProperty("Timezone")
    private String timezone;

    @ApiModelProperty("Interview Type")
    @Excel(name = "Interview Type")
    private String interviewType;

    @ApiModelProperty("Platform")
    @Excel(name = "Platform")
    private String platform;

    @ApiModelProperty("Meeting Link")
    private String meetingLink;

    @ApiModelProperty("Meeting ID")
    private String meetingId;

    @ApiModelProperty("Meeting Password")
    private String meetingPassword;

    @ApiModelProperty("Status")
    @Excel(name = "Status")
    private String status;

    @ApiModelProperty("Vendor Confirmed")
    private Integer vendorConfirmed;

    @ApiModelProperty("Vendor Confirmed Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date vendorConfirmedTime;

    @ApiModelProperty("Interviewer ID")
    private Long interviewerId;

    @ApiModelProperty("CM ID")
    private Long cmId;

    @ApiModelProperty("CM Notified")
    private Integer cmNotified;

    @ApiModelProperty("Vendor Notified")
    private Integer vendorNotified;

    @ApiModelProperty("Interviewer Name")
    @Excel(name = "Interviewer")
    private String interviewerName;

    @ApiModelProperty("Interviewer Contact")
    private String interviewerContact;

    @ApiModelProperty("Preparation Notes")
    private String preparationNotes;

    @ApiModelProperty("Interview Notes")
    private String interviewNotes;

    @ApiModelProperty("Interview Result")
    @Excel(name = "Result")
    private String interviewResult;

    @ApiModelProperty("Interview Score")
    @Excel(name = "Score")
    private Integer interviewScore;

    @ApiModelProperty("Reminder Sent")
    private Integer reminderSent;

    @ApiModelProperty("Reminder Sent Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reminderSentTime;

    @ApiModelProperty("Notification Sent")
    private Integer notificationSent;

    @ApiModelProperty("Notification Sent Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date notificationSentTime;

    @ApiModelProperty("Reschedule Count")
    private Integer rescheduleCount;

    @ApiModelProperty("Reschedule Reason")
    private String rescheduleReason;

    @ApiModelProperty("Previous DateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date previousDatetime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Date getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(Date interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(String interviewTime) {
        this.interviewTime = interviewTime;
    }

    public Date getInterviewDatetime() {
        return interviewDatetime;
    }

    public void setInterviewDatetime(Date interviewDatetime) {
        this.interviewDatetime = interviewDatetime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingPassword() {
        return meetingPassword;
    }

    public void setMeetingPassword(String meetingPassword) {
        this.meetingPassword = meetingPassword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVendorConfirmed() {
        return vendorConfirmed;
    }

    public void setVendorConfirmed(Integer vendorConfirmed) {
        this.vendorConfirmed = vendorConfirmed;
    }

    public Date getVendorConfirmedTime() {
        return vendorConfirmedTime;
    }

    public void setVendorConfirmedTime(Date vendorConfirmedTime) {
        this.vendorConfirmedTime = vendorConfirmedTime;
    }

    public Long getInterviewerId() {
        return interviewerId;
    }

    public void setInterviewerId(Long interviewerId) {
        this.interviewerId = interviewerId;
    }

    public Long getCmId() {
        return cmId;
    }

    public void setCmId(Long cmId) {
        this.cmId = cmId;
    }

    public Integer getCmNotified() {
        return cmNotified;
    }

    public void setCmNotified(Integer cmNotified) {
        this.cmNotified = cmNotified;
    }

    public Integer getVendorNotified() {
        return vendorNotified;
    }

    public void setVendorNotified(Integer vendorNotified) {
        this.vendorNotified = vendorNotified;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getInterviewerContact() {
        return interviewerContact;
    }

    public void setInterviewerContact(String interviewerContact) {
        this.interviewerContact = interviewerContact;
    }

    public String getPreparationNotes() {
        return preparationNotes;
    }

    public void setPreparationNotes(String preparationNotes) {
        this.preparationNotes = preparationNotes;
    }

    public String getInterviewNotes() {
        return interviewNotes;
    }

    public void setInterviewNotes(String interviewNotes) {
        this.interviewNotes = interviewNotes;
    }

    public String getInterviewResult() {
        return interviewResult;
    }

    public void setInterviewResult(String interviewResult) {
        this.interviewResult = interviewResult;
    }

    public Integer getInterviewScore() {
        return interviewScore;
    }

    public void setInterviewScore(Integer interviewScore) {
        this.interviewScore = interviewScore;
    }

    public Integer getReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(Integer reminderSent) {
        this.reminderSent = reminderSent;
    }

    public Date getReminderSentTime() {
        return reminderSentTime;
    }

    public void setReminderSentTime(Date reminderSentTime) {
        this.reminderSentTime = reminderSentTime;
    }

    public Integer getNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(Integer notificationSent) {
        this.notificationSent = notificationSent;
    }

    public Date getNotificationSentTime() {
        return notificationSentTime;
    }

    public void setNotificationSentTime(Date notificationSentTime) {
        this.notificationSentTime = notificationSentTime;
    }

    public Integer getRescheduleCount() {
        return rescheduleCount;
    }

    public void setRescheduleCount(Integer rescheduleCount) {
        this.rescheduleCount = rescheduleCount;
    }

    public String getRescheduleReason() {
        return rescheduleReason;
    }

    public void setRescheduleReason(String rescheduleReason) {
        this.rescheduleReason = rescheduleReason;
    }

    public Date getPreviousDatetime() {
        return previousDatetime;
    }

    public void setPreviousDatetime(Date previousDatetime) {
        this.previousDatetime = previousDatetime;
    }

    @Override
    public String toString() {
        return "VendorInterview{" +
                "id=" + id +
                ", applicationId=" + applicationId +
                ", applicationNumber='" + applicationNumber + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", interviewDatetime=" + interviewDatetime +
                ", status='" + status + '\'' +
                ", platform='" + platform + '\'' +
                '}';
    }
}

