package com.pipemasters.server.mapper;

import com.pipemasters.server.TestEnvInitializer;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest
@ContextConfiguration(initializers = TestEnvInitializer.class)
public class UploadBatchMappingTest {

    @Autowired
    private ModelMapper modelMapper;
    @Test
    void shouldMapUploadBatchToDtoCorrectly() {
        Branch parentBranch = new Branch("Parent Branch", null);
        parentBranch.setId(1L);

        Branch branch = new Branch("Child Branch", parentBranch);
        branch.setId(2L);

        User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER), branch);
        user.setId(3L);

        Train train = new Train(123L, "Route 123", 5, "Chief");

        UploadBatch uploadBatch = new UploadBatch(
                UUID.randomUUID(),
                user,
                Instant.parse("2024-01-01T10:00:00Z"),
                LocalDate.of(2024, 1, 2),
                train,
                "Комментарий",
                Set.of("ключевое", "видео"),
                branch,
                false,
                Instant.parse("2025-01-01T10:00:00Z"),
                true,
                List.of()
        );

        UploadBatchRequestDto dto = modelMapper.map(uploadBatch, UploadBatchRequestDto.class);

        assertEquals(uploadBatch.getDirectory().toString(), dto.getDirectory());
        assertEquals(uploadBatch.getUploadedBy().getId(), dto.getUploadedById());
        assertEquals(uploadBatch.getTrainDeparted(), dto.getTrainDeparted());
        assertEquals(uploadBatch.getBranch().getId(), dto.getBranchId());
        assertEquals(uploadBatch.getKeywords(), dto.getKeywords());
        assertNull(dto.getAbsenceId()); // проверка отключенной рекурсии
        assertTrue(dto.getFiles().isEmpty());
    }

    @Test
    void shouldMapDtoToUploadBatchCorrectly() {
        Long parentBranchId = 1L;
        Long branchId = 2L;
        Long userId = 3L;
        Long trainId = 123L;

        BranchRequestDto parentBranchRequestDto = new BranchRequestDto();
        parentBranchRequestDto.setId(parentBranchId);
        parentBranchRequestDto.setName("Parent Branch");

        BranchRequestDto branchRequestDto = new BranchRequestDto();
        branchRequestDto.setId(branchId);
        branchRequestDto.setName("Child Branch");
        branchRequestDto.setParentId(parentBranchId);

        UserResponseDto userResponseDto = new UserResponseDto("Иван", "Иванов", "Иванович", Set.of(Role.USER), branchId);

        TrainRequestDto trainRequestDto = new TrainRequestDto();
        trainRequestDto.setId(trainId);
        trainRequestDto.setTrainNumber(123L);
        trainRequestDto.setRouteMessage("Route 123");

        UploadBatchRequestDto dto = new UploadBatchRequestDto(
                UUID.randomUUID().toString(),
                userResponseDto.getId(),
                Instant.parse("2024-01-01T10:00:00Z"),
                LocalDate.of(2024, 1, 2),
                trainRequestDto.getId(),
                "Комментарий",
                Set.of("ключевое", "видео"),
                branchRequestDto.getId(),
                false,
                Instant.parse("2025-01-01T10:00:00Z"),
                true,
                List.of(),
                null // отсутствие
        );

        UploadBatch uploadBatch = modelMapper.map(dto, UploadBatch.class);

        User userStub = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER),
                new Branch("Child Branch", new Branch("Parent Branch", null)));
        userStub.setId(userId);

        Branch branchStub = new Branch("Child Branch", new Branch("Parent Branch", null));
        branchStub.setId(branchId);

        uploadBatch.setUploadedBy(userStub);
        uploadBatch.setBranch(branchStub);

        assertEquals(UUID.fromString(dto.getDirectory()), uploadBatch.getDirectory());
        assertEquals(userStub.getId(), uploadBatch.getUploadedBy().getId());
        assertEquals(userStub.getName(), uploadBatch.getUploadedBy().getName());
        assertEquals(dto.getTrainDeparted(), uploadBatch.getTrainDeparted());
        assertEquals(branchStub.getId(), uploadBatch.getBranch().getId());
        assertEquals(branchStub.getName(), uploadBatch.getBranch().getName());
        assertEquals(dto.getKeywords(), uploadBatch.getKeywords());
        assertTrue(uploadBatch.getFiles().isEmpty());
    }

//    @Test
    void shouldAvoidRecursionInBranchDto() {
        Branch parent = new Branch("Parent", null);
        Branch child = new Branch("Child", parent);

        BranchRequestDto dto = modelMapper.map(child, BranchRequestDto.class);

        assertNull(dto.getParentId(), "Рекурсивное поле должно быть null");
    }

    @Test
    void shouldAvoidRecursionInUploadBatchDto() {
        Branch branch = new Branch("Branch", null);
        User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER), branch);
        Train train = new Train(123L, "Route", 4, "Chief");
        UploadBatch UploadBatch = new UploadBatch(
                UUID.randomUUID(),
                user,
                Instant.now(),
                LocalDate.now(),
                train,
                "Комментарий",
                Set.of("тест"),
                branch,
                false,
                null,
                false,
                new ArrayList<>()
        );

        UploadBatchRequestDto dto = modelMapper.map(UploadBatch, UploadBatchRequestDto.class);
        assertNull(dto.getAbsenceId(), "Поле absence должно быть null, чтобы избежать рекурсии");
    }
}