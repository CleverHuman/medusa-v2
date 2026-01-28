package com.medusa.mall.exception;

/**
 * BTCPay API调用异常
 */
public class BtcPayApiException extends BtcPayException {
    private static final long serialVersionUID = 1L;
    private final int httpStatus;
    private final String apiUrl;
    public BtcPayApiException(String message, String apiUrl, int httpStatus) {
        super(message, "BTCPAY_API_ERROR", "API_CALL");
        this.httpStatus = httpStatus;
        this.apiUrl = apiUrl;
    }
    public BtcPayApiException(String message, String apiUrl, int httpStatus, String invoiceId) {
        super(message, "BTCPAY_API_ERROR", "API_CALL", invoiceId);
        this.httpStatus = httpStatus;
        this.apiUrl = apiUrl;
    }
    public BtcPayApiException(String message, String apiUrl, int httpStatus, Throwable cause) {
        super(message, "BTCPAY_API_ERROR", "API_CALL", null, cause);
        this.httpStatus = httpStatus;
        this.apiUrl = apiUrl;
    }
    public int getHttpStatus() { return httpStatus; }
    public String getApiUrl() { return apiUrl; }
} 