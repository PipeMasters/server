package com.pipemasters.server.mapper;
import com.pipemasters.server.config.ModelMapperConfig;
import com.pipemasters.server.dto.request.MediaFileRequestDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import javax.xml.datatype.DatatypeFactory;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;


public class MediaFileMapperTest {

    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapperConfig().modelMapper();
    }


    @Test
    public void testEntityToDtoMapping() {
        // Arrange
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.setId(100L);

        MediaFile sourceFile = new MediaFile("source_video.mp4", FileType.VIDEO, uploadBatch);
        sourceFile.setId(200L);

        MediaFile mediaFile = new MediaFile(
                "audio.mp3",
                FileType.AUDIO,
                Instant.parse("2024-01-01T12:00:00Z"),
                sourceFile,
                uploadBatch
        );
        mediaFile.setId(300L);

        // Act
        MediaFileRequestDto dto = modelMapper.map(mediaFile, MediaFileRequestDto.class);

        // Assert
        assertEquals(mediaFile.getFilename(), dto.getFilename());
        assertEquals(mediaFile.getFileType(), dto.getFileType());
        assertEquals(mediaFile.getUploadedAt(), dto.getUploadedAt());

        assertNotNull(dto.getSourceId());
        assertEquals(sourceFile.getId(), dto.getSourceId());

        assertNotNull(dto.getUploadBatchId());
        assertEquals(uploadBatch.getId(), dto.getUploadBatchId());
    }

    @Test
    public void testDtoToEntityMapping() {
        // Arrange
        UploadBatchRequestDto uploadBatchRequestDto = new UploadBatchRequestDto();
        uploadBatchRequestDto.setId(101L);

        MediaFileRequestDto sourceDto = new MediaFileRequestDto(
                "source_video.mp4",
                FileType.VIDEO,
                Instant.parse("2024-01-01T10:00:00Z"),
                null,
                uploadBatchRequestDto.getId(),
                Duration.ofMillis(10000L),
                1024L
        );
        sourceDto.setId(200L);

        MediaFileRequestDto dto = new MediaFileRequestDto(
                "audio.mp3",
                FileType.AUDIO,
                Instant.parse("2024-01-01T12:00:00Z"),
                sourceDto.getId(),
                uploadBatchRequestDto.getId(),
                Duration.ofMillis(10000L),
                1024L
        );

        // Act
        MediaFile entity = modelMapper.map(dto, MediaFile.class);

        MediaFile sourceEntity = new MediaFile();
        sourceEntity.setId(sourceDto.getId());
        sourceEntity.setFilename(sourceDto.getFilename());

        entity.setSource(sourceEntity);

        UploadBatch batch = new UploadBatch();
        batch.setId(uploadBatchRequestDto.getId());
        entity.setUploadBatch(batch);

        // Assert
        assertEquals(dto.getFilename(), entity.getFilename());
        assertEquals(dto.getFileType(), entity.getFileType());
        assertEquals(dto.getUploadedAt(), entity.getUploadedAt());

        assertNotNull(entity.getSource());
        assertEquals(sourceDto.getFilename(), entity.getSource().getFilename());

        assertNotNull(entity.getUploadBatch());
        assertEquals(dto.getUploadBatchId(), entity.getUploadBatch().getId());
    }
}

