package com.pipemasters.server.service;

import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.repository.TrainRepository;
import com.pipemasters.server.repository.UserRepository;
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
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private TrainServiceImpl trainService;

    private User chief;

    @BeforeEach
    void setUp() {
        chief = new User();
        chief.setId(10L);
        chief.setName("Иванов");
    }

    @Test
    void save_ShouldSaveAndReturnDto() {
        TrainDto dto = new TrainDto(123L, "Москва-Сочи", 5, chief.getId());
        Train train = new Train(123L, "Москва-Сочи", 5, chief);

        when(modelMapper.map(dto, Train.class)).thenReturn(train);
        when(trainRepository.save(any(Train.class))).thenReturn(train);
        when(userRepository.findById(chief.getId())).thenReturn(Optional.of(chief));
        when(modelMapper.map(train, TrainDto.class)).thenReturn(dto);

        TrainDto result = trainService.save(dto);

        assertThat(result.getTrainNumber()).isEqualTo(dto.getTrainNumber());
        verify(trainRepository).save(any(Train.class));
    }

    @Test
    void getById_ShouldReturnDto_WhenFound() {
        Train train = new Train(123L, "Москва-Сочи", 5, chief);
        train.setId(1L);
        TrainDto dto = new TrainDto(123L, "Москва-Сочи", 5, chief.getId());

        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));
        when(modelMapper.map(train, TrainDto.class)).thenReturn(dto);

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
        Train train1 = new Train(123L, "Москва-Сочи", 5, chief);
        Train train2 = new Train(456L, "Питер-Казань", 3, chief);
        TrainDto dto1 = new TrainDto(123L, "Москва-Сочи", 5, chief.getId());
        TrainDto dto2 = new TrainDto(456L, "Питер-Казань", 3, chief.getId());

        when(trainRepository.findAll()).thenReturn(Arrays.asList(train1, train2));
        when(modelMapper.map(train1, TrainDto.class)).thenReturn(dto1);
        when(modelMapper.map(train2, TrainDto.class)).thenReturn(dto2);

        List<TrainDto> result = trainService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTrainNumber()).isEqualTo(123L);
    }

    @Test
    void update_ShouldUpdateTrain() {
        Train existing = new Train(111L, "Старый", 2, chief);
        existing.setId(1L);
        TrainDto updatedDto = new TrainDto(123L, "Москва-Сочи", 5, chief.getId());
        Train updatedTrain = new Train(123L, "Москва-Сочи", 5, chief);

        when(trainRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(chief.getId())).thenReturn(Optional.of(chief));
//        when(modelMapper.map(updatedDto, Train.class)).thenReturn(updatedTrain);
        when(trainRepository.save(any(Train.class))).thenReturn(updatedTrain);
        when(modelMapper.map(updatedTrain, TrainDto.class)).thenReturn(updatedDto);

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