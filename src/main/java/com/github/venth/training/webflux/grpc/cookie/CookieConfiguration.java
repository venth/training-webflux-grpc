package com.github.venth.training.webflux.grpc.cookie;

import java.util.concurrent.TimeUnit;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieConfiguration {

    @Bean
    public CircuitBreaker searchBreaker() {
        return new CircuitBreaker()
                .withFailureThreshold(3, 10)
                .withSuccessThreshold(5)
                .withTimeout(2, TimeUnit.SECONDS)
                .withDelay(1, TimeUnit.MINUTES);
    }

}
