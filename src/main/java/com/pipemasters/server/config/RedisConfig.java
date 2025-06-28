package com.pipemasters.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pipemasters.server.dto.UploadBatchFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {
    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheConfiguration cacheConfig = defaultCacheConfig(Duration.ofMinutes(10)).disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration("trains", defaultCacheConfig(Duration.ofMinutes(10)))
                .withCacheConfiguration("users", defaultCacheConfig(Duration.ofMinutes(10)))
                .withCacheConfiguration("filteredBatches", defaultCacheConfig(Duration.ofMinutes(10)))
                .withCacheConfiguration("batches", defaultCacheConfig(Duration.ofMinutes(10)))
                .withCacheConfiguration("branches", defaultCacheConfig(Duration.ofMinutes(10)))
                .withCacheConfiguration("branches_parent", defaultCacheConfig(Duration.ofMinutes(10)))
                .withCacheConfiguration("branches_child", defaultCacheConfig(Duration.ofMinutes(10)))

                .build();
    }

    private RedisCacheConfiguration defaultCacheConfig(Duration duration) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(duration)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(mapper)));
    }

    @Bean("uploadBatchFilterKeyGenerator")
    public KeyGenerator uploadBatchFilterKeyGenerator() {
        return (target, method, params) -> {
            UploadBatchFilter filter = (UploadBatchFilter) params[0];
            Pageable pageable = (Pageable) params[1];

            return "uploadBatchFilter:" +
                    (filter.getSpecificDate() != null ? filter.getSpecificDate().toString() : "") + ":" +
                    (filter.getDateFrom() != null ? filter.getDateFrom().toString() : "") + ":" +
                    (filter.getDateTo() != null ? filter.getDateTo().toString() : "") + ":" +
                    (filter.getTrainNumber() != null ? filter.getTrainNumber() : "") + ":" +
                    (filter.getChiefName() != null ? filter.getChiefName() : "") + ":" +
                    (filter.getUploadedByName() != null ? filter.getUploadedByName() : "") + ":" +
                    (filter.getKeywords() != null ? String.join(",", filter.getKeywords()) : "") + ":" +
                    pageable.getPageNumber() + ":" +
                    pageable.getPageSize() + ":" +
                    pageable.getSort();
        };
    }
}