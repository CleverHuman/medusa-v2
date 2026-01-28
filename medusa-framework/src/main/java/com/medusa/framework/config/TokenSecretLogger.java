package com.medusa.framework.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Temporary bean for debugging token secret source.
 * Remove after verifying the effective configuration.
 */
@Component
public class TokenSecretLogger {

    private static final Logger log = LoggerFactory.getLogger(TokenSecretLogger.class);

    private final Environment environment;

    public TokenSecretLogger(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void logTokenSecret() {
        String tokenSecret = environment.getProperty("token.secret");
        log.warn("Effective token.secret = {}", tokenSecret);
    }
}


