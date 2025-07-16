package com.pipemasters.server.mapper;

import com.pipemasters.server.TestEnvInitializer;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
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
    void shouldMapUploadBatchToDtoResponseCorrectly() {
        Branch parentBranch = new Branch("Parent Branch", null);
        parentBranch.setId(1L);

        Branch branch = new Branch("Child Branch", parentBranch);
        branch.setId(2L);

        User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER), branch);
        user.setId(3L);

        User chief = new User("Петр", "Петров", "Петрович", Set.of(Role.USER), branch);
        chief.setId(4L);

        Train train = new Train(123L, "Route 123", 5, chief, branch);

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
        uploadBatch.setTrainArrived(LocalDate.of(2024, 1, 3));

        UploadBatchDtoSmallResponse dto = modelMapper.map(uploadBatch, UploadBatchDtoSmallResponse.class);

        assertEquals(uploadBatch.getId(), dto.getId());
        assertEquals(uploadBatch.getTrainDeparted(), dto.getDateDeparted());
        assertEquals(uploadBatch.getTrainArrived(), dto.getDateArrived());
        assertEquals(uploadBatch.getTrain().getTrainNumber(), dto.getTrainNumber());
        assertEquals(uploadBatch.getTrain().getChief().getFullName(), dto.getChiefName());
    }

    @Test
    void shouldMapDtoToUploadBatchCorrectly() {
        Long parentBranchId = 1L;
        Long branchId = 2L;
        Long userId = 3L;
        Long trainId = 123L;
        Long chiefId = 4L;

        BranchRequestDto parentBranchRequestDto = new BranchRequestDto();
        parentBranchRequestDto.setId(parentBranchId);
        parentBranchRequestDto.setName("Parent Branch");

        BranchRequestDto branchRequestDto = new BranchRequestDto();
        branchRequestDto.setId(branchId);
        branchRequestDto.setName("Child Branch");
        branchRequestDto.setParentId(parentBranchId);

        UserResponseDto userDto = new UserResponseDto("Иван", "Иванов", "Иванович", Set.of(Role.USER), branchId);
        userDto.setId(userId);
        TrainRequestDto trainDto = new TrainRequestDto();
        trainDto.setId(trainId);
        trainDto.setTrainNumber(123L);
        trainDto.setRouteMessage("Route 123");
        trainDto.setChiefId(chiefId);
        trainDto.setBranchId(branchId);

        UploadBatchRequestDto dto = new UploadBatchRequestDto(
                UUID.randomUUID().toString(),
                userDto.getId(),
                Instant.parse("2024-01-01T10:00:00Z"),
                LocalDate.of(2024, 1, 2),
                trainDto.getId(),
                "Комментарий",
                Set.of("ключевое", "видео"),
                branchRequestDto.getId(),
                false,
                Instant.parse("2025-01-01T10:00:00Z"),
                true,
                List.of(),
                null
        );

        UploadBatch uploadBatch = modelMapper.map(dto, UploadBatch.class);

        User userStub = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER),
                new Branch("Child Branch", new Branch("Parent Branch", null)));
        userStub.setId(userId);

        Branch branchStub = new Branch("Child Branch", new Branch("Parent Branch", null));
        branchStub.setId(branchId);

        User chiefStub = new User("Петр", "Петров", "Петрович", Set.of(Role.USER), branchStub);
        chiefStub.setId(chiefId);

        Train trainStub = new Train(123L, "Route 123", null, chiefStub, branchStub);
        trainStub.setId(trainId);

        uploadBatch.setUploadedBy(userStub);
        uploadBatch.setBranch(branchStub);
        uploadBatch.setTrain(trainStub);

        assertEquals(UUID.fromString(dto.getDirectory()), uploadBatch.getDirectory());
        assertEquals(userStub.getId(), uploadBatch.getUploadedBy().getId());
        assertEquals(userStub.getName(), uploadBatch.getUploadedBy().getName());
        assertEquals(dto.getTrainDeparted(), uploadBatch.getTrainDeparted());
        assertEquals(branchStub.getId(), uploadBatch.getBranch().getId());
        assertEquals(branchStub.getName(), uploadBatch.getBranch().getName());
        assertEquals(dto.getKeywords(), uploadBatch.getKeywords());
        assertTrue(uploadBatch.getFiles().isEmpty());
    }

    @Test
    void shouldAvoidRecursionInUploadBatchDtoResponse() {
        Branch branch = new Branch("Branch", null);
        User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER), branch);
        User chief = new User("Петр", "Петров", "Петрович", Set.of(Role.USER), branch);
        chief.setId(4L);
        Train train = new Train(123L, "Route", 4, chief, branch);
        UploadBatch uploadBatch = new UploadBatch(
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
        uploadBatch.setTrainArrived(LocalDate.now());

        UploadBatchDtoSmallResponse dto = modelMapper.map(uploadBatch, UploadBatchDtoSmallResponse.class);
        assertEquals(uploadBatch.getTrain().getChief().getFullName(), dto.getChiefName());
    }
}