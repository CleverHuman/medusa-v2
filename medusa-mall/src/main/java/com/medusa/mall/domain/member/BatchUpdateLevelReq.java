package com.medusa.mall.domain.member;

import com.medusa.mall.domain.IdsRequest;

public class BatchUpdateLevelReq extends IdsRequest {

    public Integer level;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
