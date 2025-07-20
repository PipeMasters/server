package com.pipemasters.server.mapper;

import com.pipemasters.server.TestEnvInitializer;
import com.pipemasters.server.config.ModelMapperConfig;
import com.pipemasters.server.dto.request.MediaFileRequestDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.VideoAbsenceDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.enums.AbsenceCause;
import com.pipemasters.server.entity.enums.FileType;
import org.junit.jupiter.api.BeforeEach;
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

//@SpringBootTest
//@ContextConfiguration(initializers = TestEnvInitializer.class)
public class MappingTests {
//    @Autowired
    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapperConfig().modelMapper();
    }

    private UploadBatch createTestUploadBatch() {
        UUID directory = UUID.randomUUID();
        User uploadedBy = new User(null,null,null,null,null);
        Instant createdAt = Instant.now();
        LocalDate trainDeparted = LocalDate.now();
        Train train = new Train(null,null,null,null, null);
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
        UploadBatch uploadBatch = createTestUploadBatch();
        VideoAbsence absence = new VideoAbsence(uploadBatch, AbsenceCause.OTHER, "No video");

        VideoAbsenceDto dto = modelMapper.map(absence, VideoAbsenceDto.class);

        assertEquals(AbsenceCause.OTHER, dto.getCause());
        assertEquals("No video", dto.getComment());
    }

    @Test
    void shouldMapVideoAbsenceDtoToEntityCorrectly() {
        VideoAbsenceDto dto = new VideoAbsenceDto(null, AbsenceCause.OTHER, "Пропущена запись");

        VideoAbsence entity = modelMapper.map(dto, VideoAbsence.class);

        assertEquals(AbsenceCause.OTHER, entity.getCause());
        assertEquals("Пропущена запись", entity.getComment());
    }

    @Test
    void shouldMapMediaFileToDtoCorrectly() {
        UploadBatch uploadBatch = createTestUploadBatch();
        MediaFile sourceFile = new MediaFile("source.mp4", FileType.VIDEO, Instant.now(), null, uploadBatch);
        sourceFile.setId(123L);
        MediaFile file = new MediaFile("video.mp4", FileType.VIDEO, Instant.now(), sourceFile, uploadBatch);
        file.setId(456L);

        MediaFileRequestDto dto = modelMapper.map(file, MediaFileRequestDto.class);

        assertEquals("video.mp4", dto.getFilename());
        assertEquals(FileType.VIDEO, dto.getFileType());
        assertNotNull(dto.getUploadedAt());

        assertEquals(123L, dto.getSourceId());
        assertEquals(uploadBatch.getId(), dto.getUploadBatchId());
    }

    @Test
    void shouldMapMediaFileDtoToEntityCorrectly() {
        UploadBatchRequestDto uploadBatchRequestDto = new UploadBatchRequestDto();
        uploadBatchRequestDto.setId(10L);
        Long sourceId = 123L;

        MediaFileRequestDto sourceDto = new MediaFileRequestDto("old.mp4", FileType.VIDEO, Instant.now(), null, uploadBatchRequestDto.getId());
        sourceDto.setId(sourceId);
        MediaFileRequestDto dto = new MediaFileRequestDto("new.mp4", FileType.VIDEO, Instant.now(), sourceDto.getId(), uploadBatchRequestDto.getId());

        MediaFile entity = modelMapper.map(dto, MediaFile.class);

        MediaFile source = new MediaFile();
        source.setId(sourceId);
        source.setFilename("old.mp4");
        entity.setSource(source);

        assertEquals("new.mp4", entity.getFilename());
        assertEquals(FileType.VIDEO, entity.getFileType());
        assertNotNull(entity.getUploadedAt());

        assertNotNull(entity.getSource());
        assertEquals("old.mp4", entity.getSource().getFilename());
    }
}