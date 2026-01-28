package com.medusa.mall.service.impl;

import com.medusa.mall.service.IVOrderService;
import com.medusa.mall.mapper.VOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class VOrderServiceImpl implements IVOrderService {
    @Autowired
    private VOrderMapper vOrderMapper;

    @Override
    public List<Map<String, Object>> selectVOrderList(Map<String, Object> params) {
        return vOrderMapper.selectVOrderList(params);
    }
} 