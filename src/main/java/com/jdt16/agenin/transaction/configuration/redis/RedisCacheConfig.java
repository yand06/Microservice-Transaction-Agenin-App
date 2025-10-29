package com.jdt16.agenin.transaction.configuration.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jdt16.agenin.transaction.dto.response.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Cache Configuration with Best Practices
 * - Custom serialization per cache type
 * - Graceful error handling
 * - Cache statistics enabled
 * - RedisInsight compatible key naming
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

    /**
     * ObjectMapper untuk Redis serialization
     * TANPA default typing untuk keamanan
     */
    @Bean
    @Primary
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();

        log.info("✅ Redis ObjectMapper configured WITHOUT default typing for security");
        return mapper;
    }

    /**
     * RedisTemplate untuk operasi manual Redis
     * Menggunakan String key dan JSON value serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);

        // Key serialization
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value serialization
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        log.info("✅ RedisTemplate configured with transaction support");
        return template;
    }

    /**
     * CacheManager - Override dari CachingConfigurer
     * HANYA SATU method cacheManager yang digunakan
     */
    @Bean
    @Override
    public CacheManager cacheManager() {
        // Akan dipanggil oleh Spring untuk mendapatkan CacheManager
        return null; // Placeholder, implementasi di bawah
    }

    /**
     * RedisCacheManager - Implementasi actual CacheManager
     * Method ini akan dipanggil oleh cacheManager() di atas
     */
    @Bean
    @Primary
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Cache untuk Transactions - TTL 30 menit
        cacheConfigurations.put("transactions",
                createRestApiResponseListCacheConfig(
                        TransactionResponse.class,
                        redisObjectMapper,
                        Duration.ofMinutes(30)
                )
        );

        // Cache untuk Transaction History - TTL 20 menit
        cacheConfigurations.put("transactionHistory",
                createRestApiResponseListCacheConfig(
                        CustomerOpenBankAccountResponse.class,
                        redisObjectMapper,
                        Duration.ofMinutes(20)
                )
        );

        // Cache untuk User Balance - TTL 15 menit
        cacheConfigurations.put("userBalance",
                createRestApiResponseListCacheConfig(
                        UserBalanceResponse.class,
                        redisObjectMapper,
                        Duration.ofMinutes(15)
                )
        );

        // Cache untuk Products - TTL 30 menit
        cacheConfigurations.put("products",
                createRestApiResponseListCacheConfig(
                        ProductsResponse.class,
                        redisObjectMapper,
                        Duration.ofMinutes(30)
                )
        );

        // Cache untuk Commissions - TTL 20 menit
        cacheConfigurations.put("commissions",
                createRestApiResponseListCacheConfig(
                        BigDecimal.class,
                        redisObjectMapper,
                        Duration.ofMinutes(20)
                )
        );

        // Default configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("agenin:transaction:")
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .enableStatistics()
                .transactionAware()
                .build();

        log.info("✅ RedisCacheManager configured with {} caches and statistics enabled",
                cacheConfigurations.size());

        return cacheManager;
    }

    /**
     * Helper method untuk membuat cache configuration
     */
    private <T> RedisCacheConfiguration createRestApiResponseListCacheConfig(
            Class<T> elementType,
            ObjectMapper objectMapper,
            Duration ttl) {

        // Buat JavaType untuk RestApiResponse<List<T>>
        JavaType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elementType);

        JavaType wrapperType = objectMapper.getTypeFactory()
                .constructParametricType(RestApiResponse.class, listType);

        Jackson2JsonRedisSerializer<RestApiResponse<List<T>>> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, wrapperType);

        return RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("agenin:transaction:")
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer)
                );
    }


    /**
     * Custom Cache Error Handler
     * Override dari CachingConfigurer
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                log.error("❌ Cache GET failed - cache: {}, key: {}, error: {}",
                        cache.getName(), key, exception.getMessage());
                log.debug("Cache GET error details", exception);
            }

            @Override
            public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key, Object value) {
                log.error("❌ Cache PUT failed - cache: {}, key: {}, error: {}",
                        cache.getName(), key, exception.getMessage());
                log.debug("Cache PUT error details", exception);
            }

            @Override
            public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                log.error("❌ Cache EVICT failed - name: {} cache: {}, key: {}",
                        cache.getName(), key, exception.getMessage());
                log.debug("Cache EVICT error details", exception);
            }

            @Override
            public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
                log.error("❌ Cache CLEAR failed - cache: {}, error: {}",
                        cache.getName(), exception.getMessage());
                log.debug("Cache CLEAR error details", exception);
            }
        };
    }
}
