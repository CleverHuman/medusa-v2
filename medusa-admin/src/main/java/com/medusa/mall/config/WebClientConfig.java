package com.medusa.mall.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {
    
    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);
    
    @Value("${tor.proxy.enabled:false}")
    private boolean torProxyEnabled;
    
    @Value("${tor.proxy.host:tor}")
    private String torProxyHost;
    
    @Value("${tor.proxy.port:9050}")
    private int torProxyPort;
    
    @Value("${btcpay.proxy.enabled:false}")
    private boolean btcpayProxyEnabled;
    
    /**
     * 默认的 WebClient（不使用代理）
     * 用于 BTCPay 等内部服务
     */
    @Bean
    @Primary
    public WebClient webClient() {
        log.info("Creating default WebClient (no proxy)");
        return WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(16 * 1024 * 1024)) // 16MB
                .build())
            .build();
    }
    
    /**
     * 带 TOR 代理的 WebClient
     * 专门用于 NOWPayments API 调用
     */
    @Bean(name = "torWebClient")
    public WebClient torWebClient() {
        if (!torProxyEnabled) {
            log.info("TOR proxy is disabled, using default WebClient");
            return webClient();
        }
        
        log.info("Creating TOR proxy WebClient: {}:{}", torProxyHost, torProxyPort);
        
        try {
            // 配置 HttpClient 使用 SOCKS5 代理
            HttpClient httpClient = HttpClient.create()
                .proxy(proxy -> proxy
                    .type(ProxyProvider.Proxy.SOCKS5)
                    .host(torProxyHost)  // Docker 中是 'tor' 服务名
                    .port(torProxyPort)
                    .connectTimeoutMillis(30000)) // 30秒连接超时
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofSeconds(60)) // 60秒响应超时
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS));
                    log.debug("TOR proxy connection established");
                });
            
            log.info("✅ TOR WebClient created successfully with SOCKS5 proxy: {}:{}", 
                     torProxyHost, torProxyPort);
            
            return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                    .build())
                .build();
        } catch (Exception e) {
            log.error("❌ Failed to create TOR WebClient, falling back to default", e);
            return webClient();
        }
    }
    
    /**
     * BTCPay 专用 WebClient
     * 可通过配置选择是否使用 TOR 代理
     */
    @Bean(name = "btcpayWebClient")
    public WebClient btcpayWebClient() {
        if (btcpayProxyEnabled && torProxyEnabled) {
            log.info("Creating BTCPay WebClient with TOR proxy");
            // 复用 TOR 代理配置
            HttpClient httpClient = HttpClient.create()
                .proxy(proxy -> proxy
                    .type(ProxyProvider.Proxy.SOCKS5)
                    .host(torProxyHost)
                    .port(torProxyPort)
                    .connectTimeoutMillis(30000))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS));
                    log.debug("BTCPay TOR proxy connection established");
                });
            
            log.info("✅ BTCPay WebClient created with TOR proxy: {}:{}", torProxyHost, torProxyPort);
            
            return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                    .build())
                .build();
        } else {
            log.info("Creating BTCPay WebClient without proxy (btcpay.proxy.enabled={}, tor.proxy.enabled={})", 
                     btcpayProxyEnabled, torProxyEnabled);
            return webClient();  // 使用默认 WebClient
        }
    }
} 
