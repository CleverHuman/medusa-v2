package com.medusa.mall.mapper;

import com.medusa.mall.domain.TgPolicyConfig;

public interface TgPolicyConfigMapper {
    TgPolicyConfig selectConfig();
    int insertConfig(TgPolicyConfig config);
    int updateConfig(TgPolicyConfig config);
} 