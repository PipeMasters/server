package com.pipemasters.server.service;

import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.repository.TrainRepository;
import com.pipemasters.server.service.impl.TrainServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainServiceImplTest {
    @Mock
    private TrainRepository trainRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private TrainServiceImpl trainService;

    @BeforeEach
    void setUp() {
        trainRepository = mock(TrainRepository.class);
        modelMapper = new ModelMapper();
        trainService = new TrainServiceImpl(trainRepository, modelMapper);
    }

    @Test
    void save_ShouldSaveAndReturnDto() {
        TrainDto dto = new TrainDto(123L, "Москва-Сочи", 5, "Иванов");
        Train train = modelMapper.map(dto, Train.class);

        when(trainRepository.save(any(Train.class))).thenReturn(train);

        TrainDto result = trainService.save(dto);

        assertThat(result.getTrainNumber()).isEqualTo(dto.getTrainNumber());
        verify(trainRepository).save(any(Train.class));
    }

    @Test
    void getById_ShouldReturnDto_WhenFound() {
        Train train = new Train(123L, "Москва-Сочи", 5, "Иванов");
        train.setId(1L);

        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));

        TrainDto result = trainService.getById(1L);

        assertThat(result.getTrainNumber()).isEqualTo(123L);
        verify(trainRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        when(trainRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainService.getById(99L))
                .isInstanceOf(TrainNotFoundException.class)
                .hasMessageContaining("Train not found");
    }

    @Test
    void getAll_ShouldReturnListOfDtos() {
        List<Train> trains = Arrays.asList(
                new Train(123L, "Москва-Сочи", 5, "Иванов"),
                new Train(456L, "Питер-Казань", 3, "Петров")
        );
        when(trainRepository.findAll()).thenReturn(trains);

        List<TrainDto> result = trainService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTrainNumber()).isEqualTo(123L);
    }

    @Test
    void update_ShouldUpdateTrain() {
        Train existing = new Train(111L, "Старый", 2, "Старый");
        existing.setId(1L);

        TrainDto updatedDto = new TrainDto(123L, "Москва-Сочи", 5, "Иванов");

        when(trainRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(trainRepository.save(any(Train.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainDto result = trainService.update(1L, updatedDto);

        assertThat(result.getRouteMessage()).isEqualTo("Москва-Сочи");
    }

    @Test
    void delete_ShouldCallRepository() {
        trainService.delete(1L);
        verify(trainRepository).deleteById(1L);
    }

    @Test
    void getUniqueChiefs_ShouldReturnListOfUniqueChiefs() {
        List<String> distinctChiefsFromRepo = Arrays.asList("Иванов", "Петров", "Сидоров");
        when(trainRepository.findDistinctChiefs()).thenReturn(distinctChiefsFromRepo);

        List<String> result = trainService.getUniqueChiefs();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder("Иванов", "Петров", "Сидоров");
        verify(trainRepository).findDistinctChiefs();
    }

    @Test
    void getUniqueChiefs_ShouldReturnEmptyListWhenNoChiefsExist() {
        when(trainRepository.findDistinctChiefs()).thenReturn(Collections.emptyList());

        List<String> result = trainService.getUniqueChiefs();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(trainRepository).findDistinctChiefs();
    }
}
