package com.jdt16.agenin.transaction.components.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {
    private final RedisConnectionFactory connectionFactory;

    @Override
    public Health health() {
        try {
            try (RedisConnection connection = connectionFactory.getConnection()) {
                String pong = connection.ping();
                return Health.up()
                        .withDetail("redis", "Available")
                        .withDetail("ping", pong)
                        .withDetail("service", "transactions")
                        .build();
            }
        } catch (Exception exception) {
            log.error("Redis health check failed", exception);
            return Health.down()
                    .withDetail("redis", "Unavailable")
                    .withDetail("error", exception.getMessage())
                    .build();
        }
    }
}
