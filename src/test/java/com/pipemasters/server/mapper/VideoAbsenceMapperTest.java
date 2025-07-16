package com.pipemasters.server.mapper;

import com.pipemasters.server.config.ModelMapperConfig;
import com.pipemasters.server.dto.VideoAbsenceDto;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.VideoAbsence;
import com.pipemasters.server.entity.enums.AbsenceCause;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class VideoAbsenceMapperTest {

    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapperConfig().modelMapper();
    }

    @Test
    void testVideoAbsenceToDtoMapping() {
        // Given
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.setId(10L);

        VideoAbsence absence = new VideoAbsence(
                uploadBatch,
                AbsenceCause.DEVICE_FAILURE,
                "Камера отсутствовала во время загрузки"
        );
        absence.setId(1L);

        // When
        VideoAbsenceDto dto = modelMapper.map(absence, VideoAbsenceDto.class);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getCause()).isEqualTo(AbsenceCause.DEVICE_FAILURE);
        assertThat(dto.getComment()).isEqualTo("Камера отсутствовала во время загрузки");
    }

    @Test
    void testDtoToVideoAbsenceMapping() {
        // Given
        VideoAbsenceDto dto = new VideoAbsenceDto(
                null,
                AbsenceCause.DEVICE_FAILURE,
                "Видео слишком низкого качества"
        );
        dto.setId(5L);

        // When
        VideoAbsence entity = modelMapper.map(dto, VideoAbsence.class);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getCause()).isEqualTo(AbsenceCause.DEVICE_FAILURE);
        assertThat(entity.getComment()).isEqualTo("Видео слишком низкого качества");
    }
}