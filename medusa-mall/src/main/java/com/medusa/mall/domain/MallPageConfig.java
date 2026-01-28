package com.medusa.mall.domain;

import com.medusa.common.core.domain.BaseEntity;
import java.util.Date;

public class MallPageConfig extends BaseEntity {
    private Long id;
    private String page;
    private String section;
    private String configKey;
    private String configValue;
    private Integer sort;
    private Date createTime;
    private Date updateTime;
    private String updateBy;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
} 