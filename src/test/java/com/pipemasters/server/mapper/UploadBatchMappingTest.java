package com.pipemasters.server.mapper;

import com.pipemasters.server.TestEnvInitializer;
import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.dto.UserDto;
import com.pipemasters.server.dto.response.UploadBatchDtoResponse;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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

        Train train = new Train(123L, "Route 123", 5, chief);

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

        UploadBatchDtoResponse dto = modelMapper.map(uploadBatch, UploadBatchDtoResponse.class);

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

        BranchDto parentBranchDto = new BranchDto();
        parentBranchDto.setId(parentBranchId);
        parentBranchDto.setName("Parent Branch");

        BranchDto branchDto = new BranchDto();
        branchDto.setId(branchId);
        branchDto.setName("Child Branch");
        branchDto.setParentId(parentBranchId);

        UserDto userDto = new UserDto("Иван", "Иванов", "Иванович", Set.of(Role.USER), branchId);
        userDto.setId(userId);

        TrainDto trainDto = new TrainDto();
        trainDto.setId(trainId);
        trainDto.setTrainNumber(123L);
        trainDto.setRouteMessage("Route 123");
        trainDto.setChiefId(chiefId);

        UploadBatchDto dto = new UploadBatchDto(
                UUID.randomUUID().toString(),
                userDto.getId(),
                Instant.parse("2024-01-01T10:00:00Z"),
                LocalDate.of(2024, 1, 2),
                trainDto.getId(),
                "Комментарий",
                Set.of("ключевое", "видео"),
                branchDto.getId(),
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

        Train trainStub = new Train(123L, "Route 123", null, chiefStub);
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
        Train train = new Train(123L, "Route", 4, chief);
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

        UploadBatchDtoResponse dto = modelMapper.map(uploadBatch, UploadBatchDtoResponse.class);
        assertEquals(uploadBatch.getTrain().getChief().getFullName(), dto.getChiefName());
    }
}