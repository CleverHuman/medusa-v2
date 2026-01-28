package com.medusa.mall.exception;

/**
 * BTCPay配置异常
 */
public class BtcPayConfigException extends BtcPayException {
    private static final long serialVersionUID = 1L;
    private final String configKey;
    public BtcPayConfigException(String message, String configKey) {
        super(message, "BTCPAY_CONFIG_ERROR", "CONFIG_VALIDATION");
        this.configKey = configKey;
    }
    public BtcPayConfigException(String message, String configKey, Throwable cause) {
        super(message, "BTCPAY_CONFIG_ERROR", "CONFIG_VALIDATION", null, cause);
        this.configKey = configKey;
    }
    public String getConfigKey() { return configKey; }
} 