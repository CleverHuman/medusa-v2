package com.medusa.mall.domain.vendor;

import java.math.BigDecimal;
import java.util.Date;
import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Vendor Entity
 */
@ApiModel("Vendor Information")
public class Vendor extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Vendor ID")
    private Long id;

    @ApiModelProperty("Vendor Code")
    @Excel(name = "Vendor Code")
    private String vendorCode;

    @ApiModelProperty("Vendor Name")
    @Excel(name = "Vendor Name")
    private String vendorName;

    @ApiModelProperty("Vendor Description")
    private String description;

    @ApiModelProperty("Logo URL")
    private String logoUrl;

    @ApiModelProperty("Banner URL")
    private String bannerUrl;

    @ApiModelProperty("Contact Telegram")
    private String contactTelegram;

    @ApiModelProperty("Contact Signal")
    private String contactSignal;

    @ApiModelProperty("Contact Jabber")
    private String contactJabber;

    @ApiModelProperty("Contact Email")
    private String contactEmail;

    @ApiModelProperty("Contact Threema")
    private String contactThreema;

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

    @ApiModelProperty("PGP Public Key URL")
    private String pgpPublicKeyUrl;

    @ApiModelProperty("BTC Wallet")
    private String btcWallet;

    @ApiModelProperty("XMR Wallet")
    private String xmrWallet;

    @ApiModelProperty("USDT Wallet")
    private String usdtWallet;

    @ApiModelProperty("Product Categories")
    private String productCategories;

    @ApiModelProperty("Geographic Location")
    private String location;

    @ApiModelProperty("Vendor Rating")
    private BigDecimal rating;

    @ApiModelProperty("Total Sales Amount")
    private Long totalSales;

    @ApiModelProperty("Total Order Count")
    private Integer totalOrders;

    @ApiModelProperty("Associated Shop ID")
    private Long shopId;

    @ApiModelProperty("Status")
    @Excel(name = "Status", readConverterExp = "0=Disabled,1=Enabled")
    private Integer status;

    @ApiModelProperty("Is Featured")
    private Integer isFeatured;

    @ApiModelProperty("Sort Order")
    private Integer sortOrder;

    @ApiModelProperty("Application ID")
    private Long applicationId;

    @ApiModelProperty("Approval Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approvedTime;

    @ApiModelProperty("Approved By")
    private String approvedBy;

    @ApiModelProperty("Vendor Bond Amount (USD)")
    private BigDecimal bond;

    @ApiModelProperty("Vendor Level (starts at 1)")
    private Integer level;

    @ApiModelProperty("Sales Points (1 point per $1 sold)")
    private Long salesPoints;

    @ApiModelProperty("Withdrawable Balance (USD)")
    private BigDecimal withdrawableBalance;

    @ApiModelProperty("Pending Balance (USD)")
    private BigDecimal pendingBalance;

    @ApiModelProperty("Total Withdrawn (USD)")
    private BigDecimal totalWithdrawn;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getContactTelegram() {
        return contactTelegram;
    }

    public void setContactTelegram(String contactTelegram) {
        this.contactTelegram = contactTelegram;
    }

    public String getContactSignal() {
        return contactSignal;
    }

    public void setContactSignal(String contactSignal) {
        this.contactSignal = contactSignal;
    }

    public String getContactJabber() {
        return contactJabber;
    }

    public void setContactJabber(String contactJabber) {
        this.contactJabber = contactJabber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactThreema() {
        return contactThreema;
    }

    public void setContactThreema(String contactThreema) {
        this.contactThreema = contactThreema;
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

    public String getPgpPublicKeyUrl() {
        return pgpPublicKeyUrl;
    }

    public void setPgpPublicKeyUrl(String pgpPublicKeyUrl) {
        this.pgpPublicKeyUrl = pgpPublicKeyUrl;
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

    public String getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(String productCategories) {
        this.productCategories = productCategories;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Long totalSales) {
        this.totalSales = totalSales;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Integer isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Date getApprovedTime() {
        return approvedTime;
    }

    public void setApprovedTime(Date approvedTime) {
        this.approvedTime = approvedTime;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public BigDecimal getBond() {
        return bond;
    }

    public void setBond(BigDecimal bond) {
        this.bond = bond;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getSalesPoints() {
        return salesPoints;
    }

    public void setSalesPoints(Long salesPoints) {
        this.salesPoints = salesPoints;
    }

    public BigDecimal getWithdrawableBalance() {
        return withdrawableBalance;
    }

    public void setWithdrawableBalance(BigDecimal withdrawableBalance) {
        this.withdrawableBalance = withdrawableBalance;
    }

    public BigDecimal getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(BigDecimal pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public BigDecimal getTotalWithdrawn() {
        return totalWithdrawn;
    }

    public void setTotalWithdrawn(BigDecimal totalWithdrawn) {
        this.totalWithdrawn = totalWithdrawn;
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", vendorCode='" + vendorCode + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", location='" + location + '\'' +
                ", status=" + status +
                ", rating=" + rating +
                '}';
    }
}

