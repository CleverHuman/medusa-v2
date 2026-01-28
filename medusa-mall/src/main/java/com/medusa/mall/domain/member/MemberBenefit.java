package com.medusa.mall.domain.member;

import com.medusa.common.core.domain.BaseEntity;

import java.math.BigDecimal;

public class MemberBenefit extends BaseEntity {

    private Long levelId;

    private String levelName;

    private BigDecimal point;

    private String des;

    private BigDecimal fixedDiscount;

    private BigDecimal percentDiscount;

    private BigDecimal shippingDiscount;

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public BigDecimal getPoint() {
        return point;
    }

    public void setPoint(BigDecimal point) {
        this.point = point;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public BigDecimal getFixedDiscount() {
        return fixedDiscount;
    }

    public void setFixedDiscount(BigDecimal fixedDiscount) {
        this.fixedDiscount = fixedDiscount;
    }

    public BigDecimal getPercentDiscount() {
        return percentDiscount;
    }

    public void setPercentDiscount(BigDecimal percentDiscount) {
        this.percentDiscount = percentDiscount;
    }

    public BigDecimal getShippingDiscount() {
        return shippingDiscount;
    }

    public void setShippingDiscount(BigDecimal shippingDiscount) {
        this.shippingDiscount = shippingDiscount;
    }
}
