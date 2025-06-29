package com.pipemasters.server.mapper;

import com.pipemasters.server.TestEnvInitializer;
import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.dto.UserDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.UserRepository;
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
    @Autowired
    private MediaFileRepository mediaFileRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private UserRepository userRepository;
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
                false,
                Instant.parse("2025-01-01T10:00:00Z"),
                true,
                List.of()
        );

        UploadBatchDto dto = modelMapper.map(UploadBatch, UploadBatchDto.class);

        assertEquals(UploadBatch.getDirectory().toString(), dto.getDirectory());
        assertEquals(
                UploadBatch.getUploadedBy().getName(),
                userRepository.findById(dto.getUploadedById())
                        .orElseThrow()
                        .getName()
        );
        assertEquals(UploadBatch.getTrainDeparted(), dto.getTrainDeparted());
        assertEquals(
                UploadBatch.getBranch().getName(),
                branchRepository.findById(dto.getBranchId())
                        .orElseThrow()
                        .getName()
        );
        assertEquals(UploadBatch.getKeywords(), dto.getKeywords());
        assertNull(dto.getAbsenceId()); // проверка отключенной рекурсии
        assertTrue(dto.getFiles().isEmpty());
    }

    @Test
    void shouldMapDtoToUploadBatchCorrectly() {
        BranchDto parentBranchDto = new BranchDto();
        parentBranchDto.setName("Parent Branch");

        BranchDto branchDto = new BranchDto();
        branchDto.setName("Child Branch");
        branchDto.setParentId(parentBranchDto.getId()); // это будет проигнорировано в маппинге

        UserDto userDto = new UserDto("Иван", "Иванов", "Иванович", Set.of(Role.USER), branchDto.getId());

        TrainDto trainDto = new TrainDto();
        trainDto.setTrainNumber(123L);
        trainDto.setRouteMessage("Route 123");

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
                null // отсутствие
        );

        UploadBatch UploadBatch = modelMapper.map(dto, UploadBatch.class);

        assertEquals(UUID.fromString(dto.getDirectory()), UploadBatch.getDirectory());
        assertEquals(
                UploadBatch.getUploadedBy().getName(),
                userRepository.findById(dto.getUploadedById())
                        .orElseThrow()
                        .getName()
        );
        assertEquals(dto.getTrainDeparted(), UploadBatch.getTrainDeparted());
        assertEquals(
                UploadBatch.getBranch().getName(),
                branchRepository.findById(dto.getBranchId())
                        .orElseThrow()
                        .getName()
        );
        assertTrue(UploadBatch.getFiles().isEmpty());
    }

//    @Test
    void shouldAvoidRecursionInBranchDto() {
        Branch parent = new Branch("Parent", null);
        Branch child = new Branch("Child", parent);

        BranchDto dto = modelMapper.map(child, BranchDto.class);

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

        UploadBatchDto dto = modelMapper.map(UploadBatch, UploadBatchDto.class);
        assertNull(dto.getAbsenceId(), "Поле absence должно быть null, чтобы избежать рекурсии");
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
                    false,
                    null,
                    false,
                    new ArrayList<>()
            );
            MediaFile file = new MediaFile("", FileType.VIDEO, batch);
            assertThrows(Exception.class, () -> mediaFileRepository.save(file));
        }
}