package com.medusa.mall.domain.vendor;

import java.math.BigDecimal;
import java.util.Date;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Vendor Level History Entity
 */
@ApiModel("Vendor Level History")
public class VendorLevelHistory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Record ID")
    private Long id;

    @ApiModelProperty("Vendor ID")
    private Long vendorId;

    @ApiModelProperty("Old Level")
    private Integer oldLevel;

    @ApiModelProperty("New Level")
    private Integer newLevel;

    @ApiModelProperty("Old Sales Points")
    private Long oldPoints;

    @ApiModelProperty("New Sales Points")
    private Long newPoints;

    @ApiModelProperty("Order ID that triggered the upgrade")
    private String triggerOrderId;

    @ApiModelProperty("Order amount that contributed to points")
    private BigDecimal triggerAmount;

    @ApiModelProperty("Upgrade Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getOldLevel() {
        return oldLevel;
    }

    public void setOldLevel(Integer oldLevel) {
        this.oldLevel = oldLevel;
    }

    public Integer getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(Integer newLevel) {
        this.newLevel = newLevel;
    }

    public Long getOldPoints() {
        return oldPoints;
    }

    public void setOldPoints(Long oldPoints) {
        this.oldPoints = oldPoints;
    }

    public Long getNewPoints() {
        return newPoints;
    }

    public void setNewPoints(Long newPoints) {
        this.newPoints = newPoints;
    }

    public String getTriggerOrderId() {
        return triggerOrderId;
    }

    public void setTriggerOrderId(String triggerOrderId) {
        this.triggerOrderId = triggerOrderId;
    }

    public BigDecimal getTriggerAmount() {
        return triggerAmount;
    }

    public void setTriggerAmount(BigDecimal triggerAmount) {
        this.triggerAmount = triggerAmount;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

