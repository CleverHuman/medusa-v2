package com.medusa.mall.domain.member;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medusa.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员积分增加历史记录实体类
 */
public class MemberPointHistory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 历史记录ID */
    private Long historyId;

    /** 会员ID */
    private Long memberId;

    /** 金额 */
    private BigDecimal amount;

    /** 增加的积分(金额的1%) */
    private BigDecimal points;

    /** 备注 */
    private String note;

    /** 平台来源：0-OS 1-TG */
    private Integer platform;

    /** 年份 */
    private Integer year;

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "MemberPointHistory{" +
                "historyId=" + historyId +
                ", memberId=" + memberId +
                ", amount=" + amount +
                ", points=" + points +
                ", note='" + note + '\'' +
                ", platform=" + platform +
                ", year=" + year +
                '}';
    }
} 