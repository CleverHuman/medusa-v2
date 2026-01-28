package com.medusa.mall.mapper;

import com.medusa.mall.domain.TgHomeConfig;

public interface TgHomeConfigMapper {
    TgHomeConfig selectConfig();
    int insertConfig(TgHomeConfig config);
    int updateConfig(TgHomeConfig config);
} 