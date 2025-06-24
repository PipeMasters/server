package com.pipemasters.server.mapper;

import com.pipemasters.server.TestEnvInitializer;
import com.pipemasters.server.dto.MediaFileDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.VideoAbsenceDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.AbsenceCause;
import com.pipemasters.server.entity.enums.FileType;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ContextConfiguration(initializers = TestEnvInitializer.class)
public class MappingTests {

    @Autowired
    private ModelMapper modelMapper;

    private UploadBatch createTestUploadBatch() {
        UUID directory = UUID.randomUUID();
        User uploadedBy = new User(null,null,null,null,null);
        Instant createdAt = Instant.now();
        LocalDate trainDeparted = LocalDate.now();
        Train train = new Train(null,null,null,null);
        String comment = "Test comment";
        Set<String> keywords = Set.of("video", "absence");
        Branch branch = new Branch("Main", null);
        boolean archived = false;
        Instant deletedAt = null;
        boolean deleted = false;
        List<MediaFile> files = new ArrayList<>();

        return new UploadBatch(directory, uploadedBy, createdAt, trainDeparted, train,
                comment, keywords, branch, archived, deletedAt, deleted, files);
    }

    @Test
    void shouldMapVideoAbsenceToDtoCorrectly() {
        UploadBatch UploadBatch = createTestUploadBatch();
        VideoAbsence absence = new VideoAbsence(UploadBatch, AbsenceCause.OTHER, "No video");

        VideoAbsenceDto dto = modelMapper.map(absence, VideoAbsenceDto.class);

        assertEquals(AbsenceCause.OTHER, dto.getCause());
        assertEquals("No video", dto.getComment());
        assertNotNull(dto.getUploadBatch());
    }

    @Test
    void shouldMapVideoAbsenceDtoToEntityCorrectly() {
        UploadBatchDto UploadBatchDto = new UploadBatchDto();
        VideoAbsenceDto dto = new VideoAbsenceDto(UploadBatchDto, AbsenceCause.OTHER, "Пропущена запись");

        VideoAbsence entity = modelMapper.map(dto, VideoAbsence.class);

        assertEquals(AbsenceCause.OTHER, entity.getCause());
        assertEquals("Пропущена запись", entity.getComment());
        assertNotNull(entity.getUploadBatch());
    }

    @Test
    void shouldMapMediaFileToDtoCorrectly() {
        UploadBatch UploadBatch = createTestUploadBatch();
        MediaFile sourceFile = new MediaFile("source.mp4", FileType.VIDEO, Instant.now(), null, UploadBatch);
        MediaFile file = new MediaFile("video.mp4", FileType.VIDEO, Instant.now(), sourceFile, UploadBatch);

        MediaFileDto dto = modelMapper.map(file, MediaFileDto.class);

        assertEquals("video.mp4", dto.getFilename());
        assertEquals(FileType.VIDEO, dto.getFileType());
        assertNotNull(dto.getUploadedAt());

        if (dto.getSource() != null) {
            assertEquals("source.mp4", dto.getSource().getFilename());
        }
    }

    @Test
    void shouldMapMediaFileDtoToEntityCorrectly() {
        UploadBatchDto UploadBatchDto = new UploadBatchDto(); // можно расширить
        MediaFileDto sourceDto = new MediaFileDto("old.mp4", FileType.VIDEO, Instant.now(), null, UploadBatchDto);
        MediaFileDto dto = new MediaFileDto("new.mp4", FileType.VIDEO, Instant.now(), sourceDto, UploadBatchDto);

        MediaFile entity = modelMapper.map(dto, MediaFile.class);

        assertEquals("new.mp4", entity.getFilename());
        assertEquals(FileType.VIDEO, entity.getFileType());
        assertNotNull(entity.getUploadedAt());

        assertNotNull(entity.getSource());
        assertEquals("old.mp4", entity.getSource().getFilename());
    }
}