package com.medusa.mall.service;

import java.util.List;
import java.util.Map;

public interface IVOrderService {
    List<Map<String, Object>> selectVOrderList(Map<String, Object> params);
} 