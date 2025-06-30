package com.pipemasters.server.config.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.dto.PageDto;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;


public class PageDtoRedisSerializer<T> implements RedisSerializer<PageDto<T>> {

    private final ObjectMapper objectMapper;
    private final JavaType javaType;

    public PageDtoRedisSerializer(ObjectMapper objectMapper, Class<T> clazz) {
        this.objectMapper = objectMapper;
        this.javaType = objectMapper.getTypeFactory()
                .constructParametricType(PageDto.class, clazz);
    }

    @Override
    public byte[] serialize(PageDto<T> pageDto) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(pageDto);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Could not serialize PageDto", e);
        }
    }

    @Override
    public PageDto<T> deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) return null;
        try {
            return objectMapper.readValue(bytes, javaType);
        } catch (IOException e) {
            throw new SerializationException("Could not deserialize PageDto", e);
        }
    }
}