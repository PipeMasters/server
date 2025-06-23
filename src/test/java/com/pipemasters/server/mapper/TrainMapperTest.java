package com.pipemasters.server.mapper;
import com.pipemasters.server.config.ModelMapperConfig;
import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.entity.Train;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainMapperTest {

    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapperConfig().modelMapper();
    }

    @Test
    public void testTrainToTrainDtoMapping() {
        Train train = new Train(123L, "Москва — Сочи", 5, "Иванов Иван Иванович");

        TrainDto dto = modelMapper.map(train, TrainDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getTrainNumber()).isEqualTo(train.getTrainNumber());
        assertThat(dto.getRouteMessage()).isEqualTo(train.getRouteMessage());
        assertThat(dto.getConsistCount()).isEqualTo(train.getConsistCount());
        assertThat(dto.getChief()).isEqualTo(train.getChief());
    }

    @Test
    public void testTrainDtoToTrainMapping() {
        TrainDto dto = new TrainDto(456L, "Санкт-Петербург — Владивосток", 8, "Петров Петр Петрович");

        Train train = modelMapper.map(dto, Train.class);

        assertThat(train).isNotNull();
        assertThat(train.getTrainNumber()).isEqualTo(dto.getTrainNumber());
        assertThat(train.getRouteMessage()).isEqualTo(dto.getRouteMessage());
        assertThat(train.getConsistCount()).isEqualTo(dto.getConsistCount());
        assertThat(train.getChief()).isEqualTo(dto.getChief());
    }

    @Test
    public void testTrainToTrainDtoMappingWithNullFields() {
        // Чтобы проверить null-значения — создадим объект с параметрами, но передадим null туда
        Train train = new Train(null, null, null, null);

        TrainDto dto = modelMapper.map(train, TrainDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getTrainNumber()).isNull();
        assertThat(dto.getRouteMessage()).isNull();
        assertThat(dto.getConsistCount()).isNull();
        assertThat(dto.getChief()).isNull();
    }

    @Test
    public void testTrainDtoToTrainMappingWithNullFields() {
        TrainDto dto = new TrainDto(null, null, null, null);

        Train train = modelMapper.map(dto, Train.class);

        assertThat(train).isNotNull();
        assertThat(train.getTrainNumber()).isNull();
        assertThat(train.getRouteMessage()).isNull();
        assertThat(train.getConsistCount()).isNull();
        assertThat(train.getChief()).isNull();
    }
}
