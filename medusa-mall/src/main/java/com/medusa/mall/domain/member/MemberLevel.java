package com.medusa.mall.domain.member;
import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;


public class MemberLevel extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long memberId;

    private Long totalOrders;

    private Integer currentLevel;

    private BigDecimal currentPoint;


    private BigDecimal currentAud;


    private Integer lastLevel;

    private BigDecimal lastPoint;

    private BigDecimal lastAud;


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

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }

    public BigDecimal getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(BigDecimal currentPoint) {
        this.currentPoint = currentPoint;
    }

    public BigDecimal getCurrentAud() {
        return currentAud;
    }

    public void setCurrentAud(BigDecimal currentAud) {
        this.currentAud = currentAud;
    }

    public Integer getLastLevel() {
        return lastLevel;
    }

    public void setLastLevel(Integer lastLevel) {
        this.lastLevel = lastLevel;
    }

    public BigDecimal getLastPoint() {
        return lastPoint;
    }

    public void setLastPoint(BigDecimal lastPoint) {
        this.lastPoint = lastPoint;
    }

    public BigDecimal getLastAud() {
        return lastAud;
    }

    public void setLastAud(BigDecimal lastAud) {
        this.lastAud = lastAud;
    }
}