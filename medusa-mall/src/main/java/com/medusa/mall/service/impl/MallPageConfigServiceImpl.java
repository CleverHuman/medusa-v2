package com.medusa.mall.service.impl;

import com.medusa.mall.domain.MallPageConfig;
import com.medusa.mall.mapper.MallPageConfigMapper;
import com.medusa.mall.service.IMallPageConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MallPageConfigServiceImpl implements IMallPageConfigService {
    @Autowired
    private MallPageConfigMapper mallPageConfigMapper;

    @Override
    public MallPageConfig selectMallPageConfigById(Long id) {
        return mallPageConfigMapper.selectMallPageConfigById(id);
    }

    @Override
    public List<MallPageConfig> selectMallPageConfigList(MallPageConfig config) {
        return mallPageConfigMapper.selectMallPageConfigList(config);
    }

    @Override
    public int insertMallPageConfig(MallPageConfig config) {
        return mallPageConfigMapper.insertMallPageConfig(config);
    }

    @Override
    public int updateMallPageConfig(MallPageConfig config) {
        return mallPageConfigMapper.updateMallPageConfig(config);
    }

    @Override
    public int deleteMallPageConfigById(Long id) {
        return mallPageConfigMapper.deleteMallPageConfigById(id);
    }

    @Override
    public int deleteMallPageConfigByIds(Long[] ids) {
        return mallPageConfigMapper.deleteMallPageConfigByIds(ids);
    }
} 