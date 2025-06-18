package com.pipemasters.server.mapper;

import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.dto.RecordDto;
import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.dto.UserDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.Record;
import com.pipemasters.server.entity.enums.AbsenceCause;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest
public class RecordMappingTest {

    @Autowired
    private ModelMapper modelMapper;

    @Test
    void shouldMapRecordToDtoCorrectly() {
        Branch parentBranch = new Branch("Parent Branch", null);
        Branch branch = new Branch("Child Branch", parentBranch);
        User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER), branch);
        Train train = new Train(123L, "Route 123", 5, "Chief");

        Record record = new Record(
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

        RecordDto dto = modelMapper.map(record, RecordDto.class);

        assertEquals(record.getDirectory().toString(), dto.getDirectory());
        assertEquals(record.getUploadedBy().getName(), dto.getUploadedBy().getName());
        assertEquals(record.getTrainDeparted(), dto.getTrainDeparted());
        assertEquals(record.getBranch().getName(), dto.getBranch().getName());
        assertEquals(record.getKeywords(), dto.getKeywords());
        assertNull(dto.getAbsence()); // проверка отключенной рекурсии
        assertTrue(dto.getFiles().isEmpty());
    }

    @Test
    void shouldMapDtoToRecordCorrectly() {
        BranchDto parentBranchDto = new BranchDto();
        parentBranchDto.setName("Parent Branch");

        BranchDto branchDto = new BranchDto();
        branchDto.setName("Child Branch");
        branchDto.setParent(parentBranchDto); // это будет проигнорировано в маппинге

        UserDto userDto = new UserDto("Иван", "Иванов", "Иванович", Set.of(Role.USER), branchDto);

        TrainDto trainDto = new TrainDto();
        trainDto.setTrainNumber(123L);
        trainDto.setRouteMessage("Route 123");

        RecordDto dto = new RecordDto(
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

        Record record = modelMapper.map(dto, Record.class);

        assertEquals(UUID.fromString(dto.getDirectory()), record.getDirectory());
        assertEquals(dto.getUploadedBy().getName(), record.getUploadedBy().getName());
        assertEquals(dto.getTrainDeparted(), record.getTrainDeparted());
        assertEquals(dto.getBranch().getName(), record.getBranch().getName());
        assertTrue(record.getFiles().isEmpty());
    }

    @Test
    void shouldAvoidRecursionInBranchDto() {
        Branch parent = new Branch("Parent", null);
        Branch child = new Branch("Child", parent);

        BranchDto dto = modelMapper.map(child, BranchDto.class);

        assertNull(dto.getParent(), "Рекурсивное поле должно быть null");
    }

    @Test
    void shouldAvoidRecursionInRecordDto() {
        Branch branch = new Branch("Branch", null);
        User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER), branch);
        Train train = new Train(123L, "Route", 4, "Chief");
        Record record = new Record(
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

        RecordDto dto = modelMapper.map(record, RecordDto.class);
        assertNull(dto.getAbsence(), "Поле absence должно быть null, чтобы избежать рекурсии");
    }
}