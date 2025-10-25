package com.pipemasters.server.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pipemasters.server.config.serializer.PageDtoRedisSerializer;
import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.response.BranchResponseDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.dto.response.TrainScheduleResponseDto;
import com.pipemasters.server.dto.response.UserResponseDto;
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
import java.util.stream.Collectors;


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
        PageDtoRedisSerializer<UploadBatchDtoSmallResponse> pageDtoSerializer =
                new PageDtoRedisSerializer<>(redisObjectMapper, UploadBatchDtoSmallResponse.class);
        RedisSerializationContext.SerializationPair<PageDto<UploadBatchDtoSmallResponse>> pageDtoSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(pageDtoSerializer);

        PageDtoRedisSerializer<TrainScheduleResponseDto> trainSchedulePageSerializer =
                new PageDtoRedisSerializer<>(redisObjectMapper, TrainScheduleResponseDto.class);
        RedisSerializationContext.SerializationPair<PageDto<TrainScheduleResponseDto>> trainScheduleSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(trainSchedulePageSerializer);

        PageDtoRedisSerializer<BranchResponseDto> branchPageSerializer =
                new PageDtoRedisSerializer<>(redisObjectMapper, BranchResponseDto.class);
        RedisSerializationContext.SerializationPair<PageDto<BranchResponseDto>> branchSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(branchPageSerializer);

        PageDtoRedisSerializer<TrainResponseDto> trainPageSerializer =
                new PageDtoRedisSerializer<>(redisObjectMapper, TrainResponseDto.class);
        RedisSerializationContext.SerializationPair<PageDto<TrainResponseDto>> trainSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(trainPageSerializer);

        PageDtoRedisSerializer<UserResponseDto> userPageSerializer =
                new PageDtoRedisSerializer<>(redisObjectMapper, UserResponseDto.class);
        RedisSerializationContext.SerializationPair<PageDto<UserResponseDto>> userSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(userPageSerializer);

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

        RedisCacheConfiguration trainSchedulesCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(trainScheduleSerializationPair)
                .disableCachingNullValues();

        RedisCacheConfiguration branchesCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(branchSerializationPair)
                .disableCachingNullValues();

        RedisCacheConfiguration trainsCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(trainSerializationPair)
                .disableCachingNullValues();

        RedisCacheConfiguration usersPagesCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(userSerializationPair)
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration("filteredBatches", filteredBatchesCacheConfig)
                .withCacheConfiguration("trainSchedules", trainSchedulesCacheConfig)
                .withCacheConfiguration("branches_pages", branchesCacheConfig)
                .withCacheConfiguration("trains_pages", trainsCacheConfig)
                .withCacheConfiguration("users_pages", usersPagesCacheConfig)
                .build();
    }


    @Bean("uploadBatchFilterKeyGenerator")
    public KeyGenerator uploadBatchFilterKeyGenerator() {
        return (target, method, params) -> {
            UploadBatchFilter f = (UploadBatchFilter) params[0];
            Pageable p = (Pageable) params[1];
            String tagsPart = "";
            if (f.getTagIds() != null && !f.getTagIds().isEmpty()) {
                tagsPart = f.getTagIds().stream()
                        .sorted()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
            }
            return "uploadBatchFilter:" +
                    (f.getSpecificDate() != null ? f.getSpecificDate() : "") + ":" +
                    (f.getDepartureDateFrom() != null ? f.getDepartureDateFrom() : "") + ":" +
                    (f.getDepartureDateTo() != null ? f.getDepartureDateTo() : "") + ":" +
                    (f.getArrivalDateFrom() != null ? f.getArrivalDateFrom() : "") + ":" +
                    (f.getArrivalDateTo() != null ? f.getArrivalDateTo() : "") + ":" +
                    (f.getCreatedFrom() != null ? f.getCreatedFrom() : "") + ":" +
                    (f.getCreatedTo() != null ? f.getCreatedTo() : "") + ":" +
                    (f.getTrainId() != null ? f.getTrainId() : "") + ":" +
                    (f.getChiefId() != null ? f.getChiefId() : "") + ":" +
                    (f.getUploadedById() != null ? f.getUploadedById() : "") + ":" +
                    (f.getUploadedByName() != null ? f.getUploadedByName() : "") + ":" +
                    (f.getBranchId() != null ? f.getBranchId() : "") + ":" +
                    (f.getId() != null ? f.getId() : "") + ":" +
                    (f.getComment() != null ? f.getComment().trim().toLowerCase() : "") + ":" +
                    (f.getArchived() != null ? f.getArchived() : "") + ":" +
                    (f.getDeleted() != null ? f.getDeleted() : "") + ":" +
                    (f.getAbsenceCause() != null ? f.getAbsenceCause() : "") + ":" +
                    tagsPart + ":" +
                    p.getPageNumber() + ":" +
                    p.getPageSize() + ":" +
                    p.getSort();
        };
    }
}