package com.medusa.mall.service;

import com.medusa.mall.domain.MallPageConfig;
import java.util.List;

public interface IMallPageConfigService {
    MallPageConfig selectMallPageConfigById(Long id);
    List<MallPageConfig> selectMallPageConfigList(MallPageConfig config);
    int insertMallPageConfig(MallPageConfig config);
    int updateMallPageConfig(MallPageConfig config);
    int deleteMallPageConfigById(Long id);
    int deleteMallPageConfigByIds(Long[] ids);
} 