package com.medusa.mall.domain.vendor;

import java.util.Date;
import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Vendor Application Entity
 */
@ApiModel("Vendor Application")
public class VendorApplication extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Application ID")
    private Long id;

    @ApiModelProperty("Vendor member ID")
    private Long memberId;

    @ApiModelProperty("Application Number")
    @Excel(name = "Application Number")
    private String applicationId;

    @ApiModelProperty("Vendor Name")
    @Excel(name = "Vendor Name")
    private String vendorName;

    @ApiModelProperty("Has Market Experience")
    private Integer hasMarketExperience;

    @ApiModelProperty("Existing Markets")
    private String existingMarkets;

    @ApiModelProperty("Experience Years")
    private Integer experienceYears;

    @ApiModelProperty("PGP Signature URL")
    private String pgpSignatureUrl;

    @ApiModelProperty("BTC Wallet Address")
    private String btcWallet;

    @ApiModelProperty("XMR Wallet Address")
    private String xmrWallet;

    @ApiModelProperty("USDT Wallet Address")
    private String usdtWallet;

    @ApiModelProperty("Geographic Location")
    private String location;

    @ApiModelProperty("Product Categories JSON")
    private String productCategories;

    @ApiModelProperty("Stock Volume")
    private String stockVolume;

    @ApiModelProperty("Supports Offline Delivery")
    private Integer offlineDelivery;

    @ApiModelProperty("Product Description")
    private String productDescription;

    @ApiModelProperty("Primary Telegram")
    private String primaryTelegram;

    @ApiModelProperty("Primary Signal")
    private String primarySignal;

    @ApiModelProperty("Primary Jabber")
    private String primaryJabber;

    @ApiModelProperty("Primary Email")
    private String primaryEmail;

    @ApiModelProperty("Primary Threema")
    private String primaryThreema;

    @ApiModelProperty("Secondary Telegram")
    private String secondaryTelegram;

    @ApiModelProperty("Secondary Signal")
    private String secondarySignal;

    @ApiModelProperty("Secondary Jabber")
    private String secondaryJabber;

    @ApiModelProperty("Secondary Email")
    private String secondaryEmail;

    @ApiModelProperty("Secondary Threema")
    private String secondaryThreema;

    @ApiModelProperty("Application Status")
    @Excel(name = "Status")
    private String status;

    @ApiModelProperty("Review Notes")
    private String reviewNotes;

    @ApiModelProperty("Reviewer ID")
    private Long reviewerId;

    @ApiModelProperty("Reviewed Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewedTime;

    @ApiModelProperty("Review Stage")
    private String reviewStage;

    @ApiModelProperty("Review Progress")
    private Integer reviewProgress;

    @ApiModelProperty("Associated Vendor ID")
    private Long vendorId;

    @ApiModelProperty("Assigned Bond Level (1-10)")
    private Integer bondLevel;

    @ApiModelProperty("Bond Payment Order ID")
    private String bondOrderId;

    @ApiModelProperty("Bond Payment Completed Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bondPaidTime;

    @ApiModelProperty("BTC Wallet for Withdrawal (Admin verified)")
    private String walletBtcProvided;

    @ApiModelProperty("XMR Wallet for Withdrawal (Admin verified)")
    private String walletXmrProvided;

    @ApiModelProperty("USDT Wallet for Withdrawal (Admin verified)")
    private String walletUsdtProvided;

    @ApiModelProperty("Wallet Verification Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date walletVerifiedTime;

    @ApiModelProperty("Verified By Admin Username")
    private String walletVerifiedBy;

    @ApiModelProperty("Has Scheduled Interview")
    private Integer hasScheduledInterview;

    @ApiModelProperty("Next Interview Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextInterviewTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Integer getHasMarketExperience() {
        return hasMarketExperience;
    }

    public void setHasMarketExperience(Integer hasMarketExperience) {
        this.hasMarketExperience = hasMarketExperience;
    }

    public String getExistingMarkets() {
        return existingMarkets;
    }

    public void setExistingMarkets(String existingMarkets) {
        this.existingMarkets = existingMarkets;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getPgpSignatureUrl() {
        return pgpSignatureUrl;
    }

    public void setPgpSignatureUrl(String pgpSignatureUrl) {
        this.pgpSignatureUrl = pgpSignatureUrl;
    }

    public String getBtcWallet() {
        return btcWallet;
    }

    public void setBtcWallet(String btcWallet) {
        this.btcWallet = btcWallet;
    }

    public String getXmrWallet() {
        return xmrWallet;
    }

    public void setXmrWallet(String xmrWallet) {
        this.xmrWallet = xmrWallet;
    }

    public String getUsdtWallet() {
        return usdtWallet;
    }

    public void setUsdtWallet(String usdtWallet) {
        this.usdtWallet = usdtWallet;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(String productCategories) {
        this.productCategories = productCategories;
    }

    public String getStockVolume() {
        return stockVolume;
    }

    public void setStockVolume(String stockVolume) {
        this.stockVolume = stockVolume;
    }

    public Integer getOfflineDelivery() {
        return offlineDelivery;
    }

    public void setOfflineDelivery(Integer offlineDelivery) {
        this.offlineDelivery = offlineDelivery;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getPrimaryTelegram() {
        return primaryTelegram;
    }

    public void setPrimaryTelegram(String primaryTelegram) {
        this.primaryTelegram = primaryTelegram;
    }

    public String getPrimarySignal() {
        return primarySignal;
    }

    public void setPrimarySignal(String primarySignal) {
        this.primarySignal = primarySignal;
    }

    public String getPrimaryJabber() {
        return primaryJabber;
    }

    public void setPrimaryJabber(String primaryJabber) {
        this.primaryJabber = primaryJabber;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public String getPrimaryThreema() {
        return primaryThreema;
    }

    public void setPrimaryThreema(String primaryThreema) {
        this.primaryThreema = primaryThreema;
    }

    public String getSecondaryTelegram() {
        return secondaryTelegram;
    }

    public void setSecondaryTelegram(String secondaryTelegram) {
        this.secondaryTelegram = secondaryTelegram;
    }

    public String getSecondarySignal() {
        return secondarySignal;
    }

    public void setSecondarySignal(String secondarySignal) {
        this.secondarySignal = secondarySignal;
    }

    public String getSecondaryJabber() {
        return secondaryJabber;
    }

    public void setSecondaryJabber(String secondaryJabber) {
        this.secondaryJabber = secondaryJabber;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }

    public String getSecondaryThreema() {
        return secondaryThreema;
    }

    public void setSecondaryThreema(String secondaryThreema) {
        this.secondaryThreema = secondaryThreema;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Date getReviewedTime() {
        return reviewedTime;
    }

    public void setReviewedTime(Date reviewedTime) {
        this.reviewedTime = reviewedTime;
    }

    public String getReviewStage() {
        return reviewStage;
    }

    public void setReviewStage(String reviewStage) {
        this.reviewStage = reviewStage;
    }

    public Integer getReviewProgress() {
        return reviewProgress;
    }

    public void setReviewProgress(Integer reviewProgress) {
        this.reviewProgress = reviewProgress;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getBondLevel() {
        return bondLevel;
    }

    public void setBondLevel(Integer bondLevel) {
        this.bondLevel = bondLevel;
    }

    public String getBondOrderId() {
        return bondOrderId;
    }

    public void setBondOrderId(String bondOrderId) {
        this.bondOrderId = bondOrderId;
    }

    public Date getBondPaidTime() {
        return bondPaidTime;
    }

    public void setBondPaidTime(Date bondPaidTime) {
        this.bondPaidTime = bondPaidTime;
    }

    public String getWalletBtcProvided() {
        return walletBtcProvided;
    }

    public void setWalletBtcProvided(String walletBtcProvided) {
        this.walletBtcProvided = walletBtcProvided;
    }

    public String getWalletXmrProvided() {
        return walletXmrProvided;
    }

    public void setWalletXmrProvided(String walletXmrProvided) {
        this.walletXmrProvided = walletXmrProvided;
    }

    public String getWalletUsdtProvided() {
        return walletUsdtProvided;
    }

    public void setWalletUsdtProvided(String walletUsdtProvided) {
        this.walletUsdtProvided = walletUsdtProvided;
    }

    public Date getWalletVerifiedTime() {
        return walletVerifiedTime;
    }

    public void setWalletVerifiedTime(Date walletVerifiedTime) {
        this.walletVerifiedTime = walletVerifiedTime;
    }

    public String getWalletVerifiedBy() {
        return walletVerifiedBy;
    }

    public void setWalletVerifiedBy(String walletVerifiedBy) {
        this.walletVerifiedBy = walletVerifiedBy;
    }

    public Integer getHasScheduledInterview() {
        return hasScheduledInterview;
    }

    public void setHasScheduledInterview(Integer hasScheduledInterview) {
        this.hasScheduledInterview = hasScheduledInterview;
    }

    public Date getNextInterviewTime() {
        return nextInterviewTime;
    }

    public void setNextInterviewTime(Date nextInterviewTime) {
        this.nextInterviewTime = nextInterviewTime;
    }

    @Override
    public String toString() {
        return "VendorApplication{" +
                "id=" + id +
                ", applicationId='" + applicationId + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", status='" + status + '\'' +
                ", location='" + location + '\'' +
                ", reviewProgress=" + reviewProgress +
                '}';
    }
}

