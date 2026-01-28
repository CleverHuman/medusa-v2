package com.medusa.mall.exception;

/**
 * BTCPay Webhook处理异常
 */
public class BtcPayWebhookException extends BtcPayException {
    private static final long serialVersionUID = 1L;
    private final String eventType;
    private final String webhookBody;
    public BtcPayWebhookException(String message, String eventType, String webhookBody) {
        super(message, "BTCPAY_WEBHOOK_ERROR", "WEBHOOK_PROCESSING");
        this.eventType = eventType;
        this.webhookBody = webhookBody;
    }
    public BtcPayWebhookException(String message, String eventType, String webhookBody, String invoiceId) {
        super(message, "BTCPAY_WEBHOOK_ERROR", "WEBHOOK_PROCESSING", invoiceId);
        this.eventType = eventType;
        this.webhookBody = webhookBody;
    }
    public BtcPayWebhookException(String message, String eventType, String webhookBody, Throwable cause) {
        super(message, "BTCPAY_WEBHOOK_ERROR", "WEBHOOK_PROCESSING", null, cause);
        this.eventType = eventType;
        this.webhookBody = webhookBody;
    }
    public String getEventType() { return eventType; }
    public String getWebhookBody() { return webhookBody; }
} 