package com.medusa.mall.service;

import java.math.BigDecimal;

/**
 * CoinGecko汇率服务接口
 */
public interface CoinGeckoService {
    
    /**
     * 获取AUD到USD的汇率
     * @return AUD到USD的汇率
     */
    BigDecimal getAudToUsdRate();
    
    /**
     * 获取AUD到USDT的汇率
     * @return AUD到USDT的汇率
     */
    BigDecimal getAudToUsdtRate();
    
    /**
     * 将AUD金额转换为USD金额
     * @param audAmount AUD金额
     * @return USD金额
     */
    BigDecimal convertAudToUsd(BigDecimal audAmount);
    
    /**
     * 将AUD金额转换为USDT金额
     * @param audAmount AUD金额
     * @return USDT金额
     */
    BigDecimal convertAudToUsdt(BigDecimal audAmount);
}
