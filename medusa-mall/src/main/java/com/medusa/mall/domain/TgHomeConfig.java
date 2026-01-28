package com.medusa.mall.domain;

import com.medusa.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Date;

public class TgHomeConfig extends BaseEntity {
    private Long id;
    
    @JsonProperty("banner_image")
    private String bannerImage;
    
    private String title;
    private String description;
    private List<TgHomeButton> buttons;
    
    private Date createTime;
    private Date updateTime;
    private String updateBy;
    private String remark;

    public static class TgHomeButton {
        private String label;
        private String action;
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBannerImage() { return bannerImage; }
    public void setBannerImage(String bannerImage) { this.bannerImage = bannerImage; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<TgHomeButton> getButtons() { return buttons; }
    public void setButtons(List<TgHomeButton> buttons) { this.buttons = buttons; }
    
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
} 