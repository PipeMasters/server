package com.pipemasters.server.mapper;

import com.pipemasters.server.config.ModelMapperConfig;
import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.User;
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
        User chief = new User();
        chief.setId(10L);
        chief.setName("Иван");
        chief.setSurname("Иванов");
        chief.setPatronymic("Иванович");

        Branch branch = new Branch("Branch", null);
        branch.setId(5L);

        Train train = new Train(123L, "Москва — Сочи", 5, chief, branch);

        TrainDto dto = modelMapper.map(train, TrainDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getTrainNumber()).isEqualTo(train.getTrainNumber());
        assertThat(dto.getRouteMessage()).isEqualTo(train.getRouteMessage());
        assertThat(dto.getConsistCount()).isEqualTo(train.getConsistCount());
        assertThat(dto.getChiefId()).isEqualTo(chief.getId());
        assertThat(dto.getBranchId()).isEqualTo(branch.getId());
    }

    @Test
    public void testTrainDtoToTrainMapping() {
        Long chiefId = 20L;
        Long branchId = 30L;
        TrainDto dto = new TrainDto(456L, "Санкт-Петербург — Владивосток", 8, chiefId, branchId);

        Train train = modelMapper.map(dto, Train.class);

        assertThat(train).isNotNull();
        assertThat(train.getTrainNumber()).isEqualTo(dto.getTrainNumber());
        assertThat(train.getRouteMessage()).isEqualTo(dto.getRouteMessage());
        assertThat(train.getConsistCount()).isEqualTo(dto.getConsistCount());

        if (train.getChief() != null) {
            assertThat(train.getChief().getId()).isEqualTo(dto.getChiefId());
        } else {
            assertThat(dto.getChiefId()).isNull();
        }

        if (train.getBranch() != null) {
            assertThat(train.getBranch().getId()).isEqualTo(dto.getBranchId());
        } else {
            assertThat(dto.getBranchId()).isNull();
        }
    }
    @Test
    public void testTrainToTrainDtoMappingWithNullFields() {
        Train train = new Train(null, null, null, null, null);

        TrainDto dto = modelMapper.map(train, TrainDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getTrainNumber()).isNull();
        assertThat(dto.getRouteMessage()).isNull();
        assertThat(dto.getConsistCount()).isNull();
        assertThat(dto.getChiefId()).isNull();
        assertThat(dto.getBranchId()).isNull();
    }

    @Test
    public void testTrainDtoToTrainMappingWithNullFields() {
        TrainDto dto = new TrainDto(null, null, null, null, null);

        Train train = modelMapper.map(dto, Train.class);

        assertThat(train).isNotNull();
        assertThat(train.getTrainNumber()).isNull();
        assertThat(train.getRouteMessage()).isNull();
        assertThat(train.getConsistCount()).isNull();
        assertThat(train.getChief()).isNull();
        assertThat(train.getBranch()).isNull();
    }
}