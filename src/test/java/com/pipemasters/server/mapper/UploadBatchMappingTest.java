package com.pipemasters.server.mapper;

import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.dto.UserDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.repository.MediaFileRepository;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest
public class UploadBatchMappingTest {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private MediaFileRepository mediaFileRepository;
    @Test
    void shouldMapUploadBatchToDtoCorrectly() {
        Branch parentBranch = new Branch("Parent Branch", null);
        Branch branch = new Branch("Child Branch", parentBranch);
        User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER), branch);
        Train train = new Train(123L, "Route 123", 5, "Chief");

        UploadBatch UploadBatch = new UploadBatch(
                UUID.randomUUID(),
                user,
                Instant.parse("2024-01-01T10:00:00Z"),
                LocalDate.of(2024, 1, 2),
                train,
                "Комментарий",
                Set.of("ключевое", "видео"),
                branch,
                Instant.parse("2025-01-01T10:00:00Z"),
                true,
                List.of()
        );

        UploadBatchDto dto = modelMapper.map(UploadBatch, UploadBatchDto.class);

        assertEquals(UploadBatch.getDirectory().toString(), dto.getDirectory());
        assertEquals(UploadBatch.getUploadedBy().getName(), dto.getUploadedBy().getName());
        assertEquals(UploadBatch.getTrainDeparted(), dto.getTrainDeparted());
        assertEquals(UploadBatch.getBranch().getName(), dto.getBranch().getName());
        assertEquals(UploadBatch.getKeywords(), dto.getKeywords());
        assertNull(dto.getAbsence()); // проверка отключенной рекурсии
        assertTrue(dto.getFiles().isEmpty());
    }

    @Test
    void shouldMapDtoToUploadBatchCorrectly() {
        BranchDto parentBranchDto = new BranchDto();
        parentBranchDto.setName("Parent Branch");

        BranchDto branchDto = new BranchDto();
        branchDto.setName("Child Branch");
        branchDto.setParent(parentBranchDto); // это будет проигнорировано в маппинге

        UserDto userDto = new UserDto("Иван", "Иванов", "Иванович", Set.of(Role.USER), branchDto);

        TrainDto trainDto = new TrainDto();
        trainDto.setTrainNumber(123L);
        trainDto.setRouteMessage("Route 123");

        UploadBatchDto dto = new UploadBatchDto(
                UUID.randomUUID().toString(),
                userDto,
                Instant.parse("2024-01-01T10:00:00Z"),
                LocalDate.of(2024, 1, 2),
                trainDto,
                "Комментарий",
                Set.of("ключевое", "видео"),
                branchDto,
                Instant.parse("2025-01-01T10:00:00Z"),
                true,
                List.of(),
                null // отсутствие
        );

        UploadBatch UploadBatch = modelMapper.map(dto, UploadBatch.class);

        assertEquals(UUID.fromString(dto.getDirectory()), UploadBatch.getDirectory());
        assertEquals(dto.getUploadedBy().getName(), UploadBatch.getUploadedBy().getName());
        assertEquals(dto.getTrainDeparted(), UploadBatch.getTrainDeparted());
        assertEquals(dto.getBranch().getName(), UploadBatch.getBranch().getName());
        assertTrue(UploadBatch.getFiles().isEmpty());
    }

    @Test
    void shouldAvoidRecursionInBranchDto() {
        Branch parent = new Branch("Parent", null);
        Branch child = new Branch("Child", parent);

        BranchDto dto = modelMapper.map(child, BranchDto.class);

        assertNull(dto.getParent(), "Рекурсивное поле должно быть null");
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
                null,
                false,
                new ArrayList<>()
        );

        UploadBatchDto dto = modelMapper.map(UploadBatch, UploadBatchDto.class);
        assertNull(dto.getAbsence(), "Поле absence должно быть null, чтобы избежать рекурсии");
    }

        @Test
        void saveMediaFileWithEmptyFilenameThrowsException() {
            Branch branch = new Branch("Branch", null);
            User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER), branch);
            Train train = new Train(123L, "Route", 4, "Chief");
            UploadBatch batch = new UploadBatch(
                    UUID.randomUUID(),
                    user,
                    Instant.now(),
                    LocalDate.now(),
                    train,
                    "Комментарий",
                    Set.of("тест"),
                    branch,
                    null,
                    false,
                    new ArrayList<>()
            );
            MediaFile file = new MediaFile("", FileType.VIDEO, batch);
            assertThrows(Exception.class, () -> mediaFileRepository.save(file));
        }
}