package com.medusa.mall.domain.shop;

import java.util.Date;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 商店信息实体类
 */
@ApiModel("商店信息")
public class Shop extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("商店ID")
    private Long id;

    @ApiModelProperty("商店名称")
    private String shopName;

    @ApiModelProperty("商店代码")
    private String shopCode;

    @ApiModelProperty("商店描述")
    private String description;

    @ApiModelProperty("商户收款 store_id")
    private String storeId;

    @ApiModelProperty("商店logo图片URL")
    private String logoUrl;

    @ApiModelProperty("商店横幅图片URL")
    private String bannerUrl;

    @ApiModelProperty("联系邮箱")
    private String contactEmail;

    @ApiModelProperty("联系电话")
    private String contactPhone;

    @ApiModelProperty("官方网站URL")
    private String websiteUrl;

    @ApiModelProperty("商店地址")
    private String address;

    @ApiModelProperty("默认货币")
    private String currency;

    @ApiModelProperty("时区")
    private String timezone;

    @ApiModelProperty("状态：0=禁用，1=启用")
    private Integer status;

    @ApiModelProperty("排序顺序")
    private Integer sortOrder;

    @ApiModelProperty("是否推荐：0=否，1=是")
    private Integer isFeatured;

    @ApiModelProperty("SEO标题")
    private String metaTitle;

    @ApiModelProperty("SEO描述")
    private String metaDescription;

    @ApiModelProperty("SEO关键词")
    private String metaKeywords;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopCode() {
        return shopCode;
    }

    public void setShopCode(String shopCode) {
        this.shopCode = shopCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
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

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Integer isFeatured) {
        this.isFeatured = isFeatured;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "id=" + id +
                ", shopName='" + shopName + '\'' +
                ", shopCode='" + shopCode + '\'' +
                ", storeId='" + storeId + '\'' +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
} 