package com.medusa.mall.mapper;

import com.medusa.mall.domain.MallPageConfig;
import java.util.List;

public interface MallPageConfigMapper {
    MallPageConfig selectMallPageConfigById(Long id);
    List<MallPageConfig> selectMallPageConfigList(MallPageConfig config);
    int insertMallPageConfig(MallPageConfig config);
    int updateMallPageConfig(MallPageConfig config);
    int deleteMallPageConfigById(Long id);
    int deleteMallPageConfigByIds(Long[] ids);
} 