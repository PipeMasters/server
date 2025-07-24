package com.pipemasters.server.redis;

import com.pipemasters.server.dto.request.MediaFileRequestDto;
import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import com.pipemasters.server.service.impl.BranchServiceImpl;
import com.pipemasters.server.service.impl.TrainServiceImpl;
import com.pipemasters.server.service.impl.UploadBatchServiceImpl;
import com.pipemasters.server.service.impl.UserServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext
public class RedisTest {
    @Autowired
    private TrainServiceImpl trainService;
    @Autowired
    private BranchServiceImpl branchService;
    @Autowired
    private UploadBatchServiceImpl uploadBatchService;
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private RedisCacheManager cacheManager;
    @Autowired
    private LettuceConnectionFactory factory;



    @BeforeAll
    static void setUp() {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

    }

    @Test
    void testRedisPing() {
        var redisConnection = factory.getConnection();
        assertEquals("PONG", redisConnection.ping());
        assertNotNull(factory);
    }

    //BranchServiceImpl
    @Test
    void testCacheEvictOnBranch() {
        cleanCache();
        String cacheName = "branches";
        var branch = branchService.getAllBranches(false).getFirst();
        assertFalse(getAllKeys().isEmpty(), "Expected cached data after load");
        assertTrue(isKeyExists(cacheName));
        branchService.updateBranchName(branch.getId(), branch.getName());
        assertTrue(getAllKeys().isEmpty(),
                "Cache must be empty after @CacheEvict, but it still contains keys: " + getAllKeys());
    }
    //TrainServiceImpl
    @Test
    void testCacheEvictOnTrains() {
        cleanCache();
        String cacheName = "trains";
        var train = trainService.getAll().getFirst();
        assertFalse(getAllKeys().isEmpty(), "Expected cached data after load");
        assertTrue(isKeyExists(cacheName));
        trainService.update(train.getId(), new TrainRequestDto(
                train.getTrainNumber(),
                train.getRouteMessage(),
                train.getConsistCount(),
                train.getChiefId(),
                train.getBranchId()));
        assertTrue(getAllKeys().isEmpty(),
                "Cache must be empty after @CacheEvict, but it still contains keys: " + getAllKeys());
    }

    //UploadBatchServiceImpl
    @Test
    void testCacheEvictOnUploadBatch() {
        cleanCache();
        String cacheName = "batches";
        var uploadBatch = uploadBatchService.getById(uploadBatchService.getAll().getFirst().getId());
        assertFalse(getAllKeys().isEmpty(), "Expected cached data after load");
        assertTrue(isKeyExists(cacheName));
        UploadBatchRequestDto requestDto = new UploadBatchRequestDto(
                uploadBatch.getDirectory(),
                uploadBatch.getUploadedBy().getId(),
                uploadBatch.getCreatedAt(),
                uploadBatch.getTrainDeparted(),
                uploadBatch.getTrainId(),
                uploadBatch.getComment(),
                uploadBatch.getKeywords(),
                uploadBatch.getBranchId(),
                uploadBatch.isArchived(),
                uploadBatch.getDeletedAt(),
                uploadBatch.isDeleted(),
                null,
                null
        );

        uploadBatchService.update(uploadBatch.getId(), requestDto);
        assertTrue(getAllKeys().isEmpty(),
                "Cache must be empty after @CacheEvict, but it still contains keys: " + getAllKeys());
    }

    //UserServiceImpl
    @Test
    void testCacheEvictOnuser() {
        cleanCache();
        String cacheName = "users";
        var user = userService.getUsers().getFirst();
        assertFalse(getAllKeys().isEmpty(), "Expected cached data after load");
        assertTrue(isKeyExists(cacheName));
        userService.updateUser(user.getId(),new UserUpdateDto(
                user.getName(),
                user.getSurname(),
                user.getPatronymic(),
                user.getRoles(),
                user.getBranchId()
        ));
        assertTrue(getAllKeys().isEmpty(),
                "Cache must be empty after @CacheEvict, but it still contains keys: " + getAllKeys());
    }

    boolean isKeyExists(String pattern) {
        return getAllKeys().stream()
                .anyMatch(key -> key.contains(pattern));
    }

    Set<String> getAllKeys() {
        var connection = factory.getConnection();
        var rawKeys = connection.keyCommands().keys("*".getBytes(StandardCharsets.UTF_8));
        return rawKeys.stream()
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .collect(Collectors.toSet());
    }

    void cleanCache() {
        var cache = cacheManager.getCache("*");
        assert cache != null;
        cache.clear();
    }
}
