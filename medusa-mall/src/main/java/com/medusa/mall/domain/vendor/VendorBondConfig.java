package com.medusa.mall.domain.vendor;

import java.math.BigDecimal;
import com.medusa.common.annotation.Excel;
import com.medusa.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Vendor Bond Configuration Entity
 */
@ApiModel("Vendor Bond Level Configuration")
public class VendorBondConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Config ID")
    private Long id;

    @ApiModelProperty("Vendor Level (1-10)")
    @Excel(name = "Level")
    private Integer level;

    @ApiModelProperty("Required Bond Amount (AUD)")
    @Excel(name = "Bond Amount")
    private BigDecimal bondAmount;

    @ApiModelProperty("CM Account Life (days)")
    @Excel(name = "Account Life Days")
    private Integer accountLifeDays;

    @ApiModelProperty("Cumulative XP/Lifetime Trade Volume (AUD)")
    @Excel(name = "Cumulative Trade Volume")
    private BigDecimal cumulativeTradeVolume;

    @ApiModelProperty("Trade Cap Multiplier (e.g., 100%, 200%)")
    @Excel(name = "Trade Cap Multiplier")
    private String tradeCapMultiplier;

    @ApiModelProperty("Total Trade Cap formula description")
    private String totalTradeCapFormula;

    @ApiModelProperty("CM Liquidation Payout ratio")
    private String liquidationPayout;

    @ApiModelProperty("CM Payout Time (T+0, T+1, T+2, T+3)")
    @Excel(name = "Payout Time")
    private String payoutTime;

    @ApiModelProperty("Tax Discount Multiplier percentage")
    @Excel(name = "Tax Discount %")
    private BigDecimal taxDiscountMultiplier;

    @ApiModelProperty("AM Bonus description")
    private String amBonusDescription;

    @ApiModelProperty("Level Perks description")
    private String levelPerks;

    @ApiModelProperty("Detailed Bond Rules (Markdown format)")
    private String rulesContent;

    @ApiModelProperty("Status (0=Disabled, 1=Active)")
    @Excel(name = "Status", readConverterExp = "0=Disabled,1=Active")
    private Integer status;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public BigDecimal getBondAmount() {
        return bondAmount;
    }

    public void setBondAmount(BigDecimal bondAmount) {
        this.bondAmount = bondAmount;
    }

    public Integer getAccountLifeDays() {
        return accountLifeDays;
    }

    public void setAccountLifeDays(Integer accountLifeDays) {
        this.accountLifeDays = accountLifeDays;
    }

    public BigDecimal getCumulativeTradeVolume() {
        return cumulativeTradeVolume;
    }

    public void setCumulativeTradeVolume(BigDecimal cumulativeTradeVolume) {
        this.cumulativeTradeVolume = cumulativeTradeVolume;
    }

    public String getTradeCapMultiplier() {
        return tradeCapMultiplier;
    }

    public void setTradeCapMultiplier(String tradeCapMultiplier) {
        this.tradeCapMultiplier = tradeCapMultiplier;
    }

    public String getTotalTradeCapFormula() {
        return totalTradeCapFormula;
    }

    public void setTotalTradeCapFormula(String totalTradeCapFormula) {
        this.totalTradeCapFormula = totalTradeCapFormula;
    }

    public String getLiquidationPayout() {
        return liquidationPayout;
    }

    public void setLiquidationPayout(String liquidationPayout) {
        this.liquidationPayout = liquidationPayout;
    }

    public String getPayoutTime() {
        return payoutTime;
    }

    public void setPayoutTime(String payoutTime) {
        this.payoutTime = payoutTime;
    }

    public BigDecimal getTaxDiscountMultiplier() {
        return taxDiscountMultiplier;
    }

    public void setTaxDiscountMultiplier(BigDecimal taxDiscountMultiplier) {
        this.taxDiscountMultiplier = taxDiscountMultiplier;
    }

    public String getAmBonusDescription() {
        return amBonusDescription;
    }

    public void setAmBonusDescription(String amBonusDescription) {
        this.amBonusDescription = amBonusDescription;
    }

    public String getLevelPerks() {
        return levelPerks;
    }

    public void setLevelPerks(String levelPerks) {
        this.levelPerks = levelPerks;
    }

    public String getRulesContent() {
        return rulesContent;
    }

    public void setRulesContent(String rulesContent) {
        this.rulesContent = rulesContent;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "VendorBondConfig{" +
                "id=" + id +
                ", level=" + level +
                ", bondAmount=" + bondAmount +
                ", accountLifeDays=" + accountLifeDays +
                ", cumulativeTradeVolume=" + cumulativeTradeVolume +
                ", tradeCapMultiplier='" + tradeCapMultiplier + '\'' +
                ", payoutTime='" + payoutTime + '\'' +
                ", taxDiscountMultiplier=" + taxDiscountMultiplier +
                ", status=" + status +
                '}';
    }
}
