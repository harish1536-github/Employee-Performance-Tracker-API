package com.hr.performancetracker.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CYCLE_SUMMARY    = "cycleSummary";
    public static final String EMPLOYEE_REVIEWS = "employeeReviews";
    public static final String EMPLOYEE_FILTER  = "employeeFilter";

    /*
     * WHY EVERYTHING instead of NON_FINAL:
     *
     * NON_FINAL types: Object, List, Map, etc
     * But root-level List<ReviewResponse> is seen as Object
     * PROPERTY mode needs type at root → fails for arrays
     *
     * EVERYTHING includes ALL types including primitives wrappers
     * Ensures even root-level List gets type info written
     * So Jackson can deserialize back correctly
     *
     * WHY PROPERTY (back from WRAPPER_OBJECT):
     * PROPERTY stores @class inside the object
     * Works correctly when EVERYTHING is used
     * More compatible with nested structures
     */

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Handle LocalDate, LocalDateTime etc
        mapper.registerModule(new JavaTimeModule());

        // Store as "2025-01-01" not [2025, 1, 1]
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ EVERYTHING + PROPERTY = works for all types including Lists
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,  // ✅ Changed
                JsonTypeInfo.As.PROPERTY                // ✅ Back to PROPERTY
        );

        return mapper;
    }

    @Bean(name = "redisValueSerializer")
    public RedisSerializer<Object> redisValueSerializer(
            ObjectMapper redisObjectMapper) {

        return new RedisSerializer<>() {

            @Override
            public byte[] serialize(Object value)
                    throws SerializationException {

                if (value == null) {
                    return new byte[0];
                }

                try {
                    return redisObjectMapper.writeValueAsBytes(value);
                } catch (JsonProcessingException e) {
                    throw new SerializationException(
                            "Could not serialize object to JSON for Redis: "
                                    + e.getMessage(), e);
                }
            }

            @Override
            public Object deserialize(byte[] bytes)
                    throws SerializationException {

                if (bytes == null || bytes.length == 0) {
                    return null;
                }

                try {
                    return redisObjectMapper.readValue(bytes, Object.class);
                } catch (IOException e) {
                    throw new SerializationException(
                            "Could not deserialize JSON from Redis: "
                                    + e.getMessage(), e);
                }
            }
        };
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory,
            RedisSerializer<Object> redisValueSerializer) {

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .serializeKeysWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(redisValueSerializer)
                        )
                        .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        /*
         * cycleSummary → 5 minutes
         * Expensive aggregation query
         */
        cacheConfigs.put(
                CYCLE_SUMMARY,
                defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        /*
         * employeeReviews → 2 minutes
         * Evicted on new review for that employee
         */
        cacheConfigs.put(
                EMPLOYEE_REVIEWS,
                defaultConfig.entryTtl(Duration.ofMinutes(2))
        );

        /*
         * employeeFilter → 1 minute
         * Evicted on new employee or review created
         */
        cacheConfigs.put(
                EMPLOYEE_FILTER,
                defaultConfig.entryTtl(Duration.ofMinutes(1))
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}