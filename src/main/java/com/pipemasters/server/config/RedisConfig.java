package com.pipemasters.server.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pipemasters.server.config.serializer.PageDtoRedisSerializer;
import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
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
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
                                          ObjectMapper redisObjectMapper) {

        // Сериализатор по умолчанию для большинства кешей (обычные объекты, коллекции)
        // Новый рекомендуемый способ
        Jackson2JsonRedisSerializer<Object> defaultSerializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);

        RedisSerializationContext.SerializationPair<Object> defaultSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(defaultSerializer);

        // Специальный сериализатор для PageDto<UploadBatchResponseDto>
        PageDtoRedisSerializer<UploadBatchResponseDto> pageDtoSerializer =
                new PageDtoRedisSerializer<>(redisObjectMapper, UploadBatchResponseDto.class);

        RedisSerializationContext.SerializationPair<PageDto<UploadBatchResponseDto>> pageDtoSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(pageDtoSerializer);

        // Общий конфиг для остальных кешей
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(defaultSerializationPair)
                .disableCachingNullValues();

        // Отдельный конфиг для filteredBatches с кастомным сериализатором
        RedisCacheConfiguration filteredBatchesCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(pageDtoSerializationPair)
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration("filteredBatches", filteredBatchesCacheConfig)
                .build();
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