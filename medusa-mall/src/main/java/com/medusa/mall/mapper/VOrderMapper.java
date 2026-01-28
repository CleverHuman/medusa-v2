package com.medusa.mall.mapper;

import java.util.List;
import java.util.Map;

public interface VOrderMapper {
    List<Map<String, Object>> selectVOrderList(Map<String, Object> params);
} 