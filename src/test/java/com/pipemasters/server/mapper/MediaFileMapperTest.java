package com.pipemasters.server.mapper;
import com.pipemasters.server.config.ModelMapperConfig;
import com.pipemasters.server.dto.MediaFileDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;

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
        MediaFileDto dto = modelMapper.map(mediaFile, MediaFileDto.class);

        // Assert
        assertEquals(mediaFile.getFilename(), dto.getFilename());
        assertEquals(mediaFile.getFileType(), dto.getFileType());
        assertEquals(mediaFile.getUploadedAt(), dto.getUploadedAt());

        assertNotNull(dto.getSource());
        assertEquals(mediaFile.getSource().getFilename(), dto.getSource().getFilename());

        assertNotNull(dto.getUploadBatch());
        assertEquals(mediaFile.getUploadBatch().getId(), dto.getUploadBatch().getId());
    }

    @Test
    public void testDtoToEntityMapping() {
        // Arrange
        UploadBatchDto uploadBatchDto = new UploadBatchDto();
        uploadBatchDto.setId(101L);

        MediaFileDto sourceDto = new MediaFileDto(
                "source_video.mp4",
                FileType.VIDEO,
                Instant.parse("2024-01-01T10:00:00Z"),
                null,
                uploadBatchDto
        );

        MediaFileDto dto = new MediaFileDto(
                "audio.mp3",
                FileType.AUDIO,
                Instant.parse("2024-01-01T12:00:00Z"),
                sourceDto,
                uploadBatchDto
        );

        // Act
        MediaFile entity = modelMapper.map(dto, MediaFile.class);

        // Assert
        assertEquals(dto.getFilename(), entity.getFilename());
        assertEquals(dto.getFileType(), entity.getFileType());
        assertEquals(dto.getUploadedAt(), entity.getUploadedAt());

        assertNotNull(entity.getSource());
        assertEquals(dto.getSource().getFilename(), entity.getSource().getFilename());

        assertNotNull(entity.getUploadBatch());
        assertEquals(dto.getUploadBatch().getId(), entity.getUploadBatch().getId());
    }
}

