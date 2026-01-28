package com.medusa.mall.exception;

/**
 * BTCPay相关异常
 */
public class BtcPayException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String errorCode;
    private final String operation;
    private final String invoiceId;

    public BtcPayException(String message) {
        super(message);
        this.errorCode = "BTCPAY_ERROR";
        this.operation = "UNKNOWN";
        this.invoiceId = null;
    }

    public BtcPayException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.operation = "UNKNOWN";
        this.invoiceId = null;
    }

    public BtcPayException(String message, String errorCode, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.invoiceId = null;
    }

    public BtcPayException(String message, String errorCode, String operation, String invoiceId) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.invoiceId = invoiceId;
    }

    public BtcPayException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BTCPAY_ERROR";
        this.operation = "UNKNOWN";
        this.invoiceId = null;
    }

    public BtcPayException(String message, String errorCode, String operation, String invoiceId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
        this.invoiceId = invoiceId;
    }

    public String getErrorCode() { return errorCode; }
    public String getOperation() { return operation; }
    public String getInvoiceId() { return invoiceId; }
} 