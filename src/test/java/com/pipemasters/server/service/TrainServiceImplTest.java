package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
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

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private User anotherChief;
    private Branch anotherBranch;

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

    @Test
    void assignTrainToBranchSuccessfully() {
        Long trainId = 1L;
        Long branchId = 2L;

        Train existingTrain = new Train(123L, "Initial Route", 1, chief, null);
        existingTrain.setId(trainId);

        Branch newBranch = new Branch("New Branch", null);
        newBranch.setId(branchId);

        Train updatedTrainAfterSave = new Train(123L, "Initial Route", 1, chief, newBranch);
        updatedTrainAfterSave.setId(trainId);

        TrainResponseDto expectedResponseDto = new TrainResponseDto();
        expectedResponseDto.setId(trainId);
        expectedResponseDto.setTrainNumber(123L);
        expectedResponseDto.setBranchId(branchId);

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(existingTrain));
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(newBranch));
        when(trainRepository.save(any(Train.class))).thenReturn(updatedTrainAfterSave);
        when(modelMapper.map(updatedTrainAfterSave, TrainResponseDto.class)).thenReturn(expectedResponseDto);

        TrainResponseDto result = trainService.assignTrainToBranch(trainId, branchId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(trainId);
        assertThat(result.getBranchId()).isEqualTo(branchId);
        verify(trainRepository, times(1)).findById(trainId);
        verify(branchRepository, times(1)).findById(branchId);
        verify(trainRepository, times(1)).save(existingTrain);
        verify(modelMapper, times(1)).map(updatedTrainAfterSave, TrainResponseDto.class);
    }

    @Test
    void assignTrainToBranchThrowsExceptionIfTrainNotFound() {
        Long trainId = 99L;
        Long branchId = 1L;

        when(trainRepository.findById(trainId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainService.assignTrainToBranch(trainId, branchId))
                .isInstanceOf(TrainNotFoundException.class)
                .hasMessageContaining("Train not found");
        verify(trainRepository, times(1)).findById(trainId);
        verify(branchRepository, never()).findById(anyLong());
        verify(trainRepository, never()).save(any(Train.class));
    }

    @Test
    void assignTrainToBranchThrowsExceptionIfBranchNotFound() {
        Long trainId = 1L;
        Long branchId = 99L;

        Train existingTrain = new Train(1L, "Route", 1, chief, null);
        existingTrain.setId(trainId);

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(existingTrain));
        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainService.assignTrainToBranch(trainId, branchId))
                .isInstanceOf(BranchNotFoundException.class)
                .hasMessageContaining("Branch not found");
        verify(trainRepository, times(1)).findById(trainId);
        verify(branchRepository, times(1)).findById(branchId);
        verify(trainRepository, never()).save(any(Train.class));
    }

    @Test
    void updateTrainChiefSuccessfully() {
        Long trainId = 1L;
        Long newChiefId = 20L;

        User oldChief = new User("Old", "Chief", "Patronymic", new HashSet<>(), branch);
        oldChief.setId(10L);

        User newChief = new User("New", "Chief", "Patronymic", new HashSet<>(), branch);
        newChief.setId(newChiefId);

        Train existingTrain = new Train(123L, "Route", 1, oldChief, branch);
        existingTrain.setId(trainId);

        Train updatedTrainAfterSave = new Train(123L, "Route", 1, newChief, branch);
        updatedTrainAfterSave.setId(trainId);

        TrainResponseDto expectedResponseDto = new TrainResponseDto();
        expectedResponseDto.setId(trainId);
        expectedResponseDto.setTrainNumber(123L);
        expectedResponseDto.setChiefId(newChiefId);

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(existingTrain));
        when(userRepository.findById(newChiefId)).thenReturn(Optional.of(newChief));
        when(trainRepository.save(any(Train.class))).thenReturn(updatedTrainAfterSave);
        when(modelMapper.map(updatedTrainAfterSave, TrainResponseDto.class)).thenReturn(expectedResponseDto);

        TrainResponseDto result = trainService.updateTrainChief(trainId, newChiefId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(trainId);
        assertThat(result.getChiefId()).isEqualTo(newChiefId);
        verify(trainRepository, times(1)).findById(trainId);
        verify(userRepository, times(1)).findById(newChiefId);
        verify(trainRepository, times(1)).save(existingTrain);
        verify(modelMapper, times(1)).map(updatedTrainAfterSave, TrainResponseDto.class);
    }

    @Test
    void updateTrainChiefThrowsExceptionIfTrainNotFound() {
        Long trainId = 99L;
        Long newChiefId = 1L;

        when(trainRepository.findById(trainId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainService.updateTrainChief(trainId, newChiefId))
                .isInstanceOf(TrainNotFoundException.class)
                .hasMessageContaining("Train not found");
        verify(trainRepository, times(1)).findById(trainId);
        verify(userRepository, never()).findById(anyLong());
        verify(trainRepository, never()).save(any(Train.class));
    }

    @Test
    void updateTrainChiefThrowsExceptionIfNewChiefNotFound() {
        Long trainId = 1L;
        Long newChiefId = 99L;

        Train existingTrain = new Train(1L, "Route", 1, chief, branch);
        existingTrain.setId(trainId);

        when(trainRepository.findById(trainId)).thenReturn(Optional.of(existingTrain));
        when(userRepository.findById(newChiefId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainService.updateTrainChief(trainId, newChiefId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Chief user not found");
        verify(trainRepository, times(1)).findById(trainId);
        verify(userRepository, times(1)).findById(newChiefId);
        verify(trainRepository, never()).save(any(Train.class));
    }

    @Test
    void getTrainsByBranchId_ShouldReturnListOfTrains() {
        Train train1 = new Train(101L, "Route 1", 10, chief, branch);
        train1.setId(1L);
        Train train2 = new Train(102L, "Route 2", 12, chief, branch);
        train2.setId(2L);
        List<Train> trainsForBranch = Arrays.asList(train1, train2);

        TrainResponseDto dto1 = new TrainResponseDto(101L, "Route 1", 10, chief.getId(), branch.getId());
        TrainResponseDto dto2 = new TrainResponseDto(102L, "Route 2", 12, chief.getId(), branch.getId());

        when(branchRepository.existsById(branch.getId())).thenReturn(true);
        when(trainRepository.findByBranchId(branch.getId())).thenReturn(trainsForBranch);
        when(modelMapper.map(train1, TrainResponseDto.class)).thenReturn(dto1);
        when(modelMapper.map(train2, TrainResponseDto.class)).thenReturn(dto2);

        List<TrainResponseDto> result = trainService.getTrainsByBranchId(branch.getId());

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(dto1, dto2);
        verify(branchRepository).existsById(branch.getId());
        verify(trainRepository).findByBranchId(branch.getId());
    }

    @Test
    void getTrainsByBranchId_ShouldReturnEmptyList_WhenNoTrainsFoundForBranch() {
        when(branchRepository.existsById(branch.getId())).thenReturn(true);
        when(trainRepository.findByBranchId(branch.getId())).thenReturn(Collections.emptyList());

        List<TrainResponseDto> result = trainService.getTrainsByBranchId(branch.getId());

        assertThat(result).isEmpty();
        verify(branchRepository).existsById(branch.getId());
        verify(trainRepository).findByBranchId(branch.getId());
    }

    @Test
    void getTrainsByBranchId_ShouldThrowBranchNotFoundException_WhenBranchDoesNotExist() {
        when(branchRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(BranchNotFoundException.class, () -> trainService.getTrainsByBranchId(99L));
        verify(branchRepository).existsById(99L);
        verify(trainRepository, never()).findByBranchId(anyLong());
    }


    @Test
    void getChiefsByBranchId_ShouldReturnListOfChiefs() {
        User chief1 = new User("Chief1", "L", "M", Set.of(Role.USER), branch); chief1.setId(1L);
        User chief2 = new User("Chief2", "L", "M", Set.of(Role.USER), branch); chief2.setId(2L);
        List<User> chiefsForBranch = Arrays.asList(chief1, chief2);

        UserResponseDto dto1 = new UserResponseDto(); dto1.setId(1L); dto1.setName("Chief1");
        UserResponseDto dto2 = new UserResponseDto(); dto2.setId(2L); dto2.setName("Chief2");
        List<UserResponseDto> expectedDtos = Arrays.asList(dto1, dto2);

        when(branchRepository.existsById(branch.getId())).thenReturn(true);
        when(trainRepository.findDistinctChiefsByBranchId(branch.getId())).thenReturn(chiefsForBranch);
        when(modelMapper.map(chief1, UserResponseDto.class)).thenReturn(dto1);
        when(modelMapper.map(chief2, UserResponseDto.class)).thenReturn(dto2);

        List<UserResponseDto> result = trainService.getChiefsByBranchId(branch.getId());

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(dto1, dto2);
        verify(branchRepository).existsById(branch.getId());
        verify(trainRepository).findDistinctChiefsByBranchId(branch.getId());
    }

    @Test
    void getChiefsByBranchId_ShouldReturnEmptyList_WhenNoChiefsFoundForBranch() {
        when(branchRepository.existsById(branch.getId())).thenReturn(true);
        when(trainRepository.findDistinctChiefsByBranchId(branch.getId())).thenReturn(Collections.emptyList());

        List<UserResponseDto> result = trainService.getChiefsByBranchId(branch.getId());

        assertThat(result).isEmpty();
        verify(branchRepository).existsById(branch.getId());
        verify(trainRepository).findDistinctChiefsByBranchId(branch.getId());
    }

    @Test
    void getChiefsByBranchId_ShouldThrowBranchNotFoundException_WhenBranchDoesNotExist() {
        when(branchRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(BranchNotFoundException.class, () -> trainService.getChiefsByBranchId(99L));
        verify(branchRepository).existsById(99L);
        verify(trainRepository, never()).findDistinctChiefsByBranchId(anyLong());
    }
}