package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.repository.BranchRepository;
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
    private BranchRepository branchRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private TrainServiceImpl trainService;

    private User chief;
    private Branch branch;

    @BeforeEach
    void setUp() {
        branch = new Branch("Branch", null);
        branch.setId(2L);
        chief = new User();
        chief.setId(10L);
        chief.setName("Иванов");
        chief.setBranch(branch);
    }

    @Test
    void save_ShouldSaveAndReturnDto() {
        TrainRequestDto dto = new TrainRequestDto(123L, "Москва-Сочи", 5, chief.getId(), branch.getId());
        Train train = new Train(123L, "Москва-Сочи", 5, chief, branch);
        TrainResponseDto responseDto = new TrainResponseDto(123L, "Москва-Сочи", 5, chief.getId(), branch.getId());

        when(modelMapper.map(dto, Train.class)).thenReturn(train);
        when(trainRepository.save(any(Train.class))).thenReturn(train);
        when(userRepository.findById(chief.getId())).thenReturn(Optional.of(chief));
        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(modelMapper.map(train, TrainResponseDto.class)).thenReturn(responseDto);

        TrainResponseDto result = trainService.save(dto);

        assertThat(result.getTrainNumber()).isEqualTo(dto.getTrainNumber());
        verify(trainRepository).save(any(Train.class));
    }

    @Test
    void getById_ShouldReturnDto_WhenFound() {
        Train train = new Train(123L, "Москва-Сочи", 5, chief, branch);
        train.setId(1L);
        TrainResponseDto dto = new TrainResponseDto(123L, "Москва-Сочи", 5, chief.getId(), branch.getId());
        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));
        when(modelMapper.map(train, TrainResponseDto.class)).thenReturn(dto);

        TrainResponseDto result = trainService.getById(1L);

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
        Train train1 = new Train(123L, "Москва-Сочи", 5, chief, branch);
        Train train2 = new Train(456L, "Питер-Казань", 3, chief, branch);
        TrainResponseDto dto1 = new TrainResponseDto(123L, "Москва-Сочи", 5, chief.getId(), branch.getId());
        TrainResponseDto dto2 = new TrainResponseDto(456L, "Питер-Казань", 3, chief.getId(), branch.getId());

        when(trainRepository.findAll()).thenReturn(Arrays.asList(train1, train2));
        when(modelMapper.map(train1, TrainResponseDto.class)).thenReturn(dto1);
        when(modelMapper.map(train2, TrainResponseDto.class)).thenReturn(dto2);

        List<TrainResponseDto> result = trainService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTrainNumber()).isEqualTo(123L);
    }

    @Test
    void update_ShouldUpdateTrain() {
        Train existing = new Train(111L, "Старый", 2, chief, branch);
        existing.setId(1L);
        TrainRequestDto updatedDto = new TrainRequestDto(123L, "Москва-Сочи", 5, chief.getId(), branch.getId());
        Train updatedTrain = new Train(123L, "Москва-Сочи", 5, chief, branch);
        TrainResponseDto responseDto = new TrainResponseDto(123L, "Москва-Сочи", 5, chief.getId(), branch.getId());

        when(trainRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(chief.getId())).thenReturn(Optional.of(chief));
        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(trainRepository.save(any(Train.class))).thenReturn(updatedTrain);
        when(modelMapper.map(updatedTrain, TrainResponseDto.class)).thenReturn(responseDto);

        TrainResponseDto result = trainService.update(1L, updatedDto);

        assertThat(result.getRouteMessage()).isEqualTo("Москва-Сочи");
    }

    @Test
    void delete_ShouldCallRepository() {
        trainService.delete(1L);
        verify(trainRepository).deleteById(1L);
    }

    @Test
    void getChiefs_ShouldReturnListOfUserResponseDto() {
        User chief2 = new User();
        chief2.setId(11L);
        chief2.setName("Петров");
        chief2.setBranch(branch);

        List<User> chiefs = Arrays.asList(chief, chief2);
        UserResponseDto dto1 = new UserResponseDto();
        UserResponseDto dto2 = new UserResponseDto();
        when(trainRepository.findDistinctChiefs()).thenReturn(chiefs);
        when(modelMapper.map(chief, UserResponseDto.class)).thenReturn(dto1);
        when(modelMapper.map(chief2, UserResponseDto.class)).thenReturn(dto2);

        List<UserResponseDto> result = trainService.getChiefs();

        assertThat(result).hasSize(2);
        verify(trainRepository).findDistinctChiefs();
        verify(modelMapper).map(chief, UserResponseDto.class);
        verify(modelMapper).map(chief2, UserResponseDto.class);
    }

    @Test
    void getChiefs_ShouldReturnEmptyListWhenNoChiefsExist() {
        when(trainRepository.findDistinctChiefs()).thenReturn(Collections.emptyList());

        List<UserResponseDto> result = trainService.getChiefs();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(trainRepository).findDistinctChiefs();
    }
}