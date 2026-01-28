package com.medusa.mall.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * BTCPay重试工具类
 */
public class BtcPayRetryUtil {
    
    private static final Logger log = LoggerFactory.getLogger(BtcPayRetryUtil.class);
    
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    
    /**
     * 带重试机制的API调用
     */
    public static <T> Mono<T> retryWithBackoff(Mono<T> mono, String operation) {
        return mono.retryWhen(reactor.util.retry.Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                .filter(throwable -> {
                    log.warn("Retrying {} after error: {}", operation, throwable.getMessage());
                    return true;
                })
                .doBeforeRetry(retrySignal -> 
                    log.info("Retrying {} (attempt {}/{})", operation, 
                            retrySignal.totalRetries() + 1, MAX_RETRIES))
                .doAfterRetry(retrySignal -> 
                    log.info("Retry completed for {}", operation)));
    }
    
    /**
     * 带重试机制的API调用（自定义重试条件）
     */
    public static <T> Mono<T> retryWithBackoff(Mono<T> mono, String operation, 
                                               Predicate<Throwable> retryCondition) {
        return mono.retryWhen(reactor.util.retry.Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                .filter(retryCondition)
                .doBeforeRetry(retrySignal -> 
                    log.info("Retrying {} (attempt {}/{})", operation, 
                            retrySignal.totalRetries() + 1, MAX_RETRIES))
                .doAfterRetry(retrySignal -> 
                    log.info("Retry completed for {}", operation)));
    }
} 