package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.repository.UserRepository;
import com.pipemasters.server.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {
    private UserServiceImpl userService;
    private UserRepository userRepository;
    private BranchRepository branchRepository;
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        branchRepository = mock(BranchRepository.class);
        modelMapper = mock(ModelMapper.class);
        userService = new UserServiceImpl(userRepository, branchRepository, modelMapper);
    }

    @Test
    void createUserSuccessfully() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("Test");
        createDto.setSurname("User");
        createDto.setBranchId(1L);

        Branch mockBranch = new Branch("name", null);
        mockBranch.setId(1L);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Test");
        savedUser.setSurname("User");
        savedUser.setBranch(mockBranch);
        savedUser.setRoles(Collections.singleton(Role.USER));

        UserResponseDto expectedUserResponseDto = new UserResponseDto();
        expectedUserResponseDto.setId(1L);
        expectedUserResponseDto.setName("Test");
        expectedUserResponseDto.setSurname("User");

        BranchRequestDto expectedBranchRequestDto = new BranchRequestDto();
        expectedBranchRequestDto.setId(1L);
        expectedUserResponseDto.setBranchId(expectedBranchRequestDto.getId());

        expectedUserResponseDto.setRoles(Collections.singleton(Role.USER));

        when(branchRepository.findById(1L)).thenReturn(Optional.of(mockBranch));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(modelMapper.map(savedUser, UserResponseDto.class)).thenReturn(expectedUserResponseDto);

        UserResponseDto result = userService.createUser(createDto);

        assertNotNull(result);
        assertEquals(expectedUserResponseDto.getId(), result.getId());
        assertEquals(expectedUserResponseDto.getName(), result.getName());
        assertEquals(expectedUserResponseDto.getBranchId(), result.getBranchId());
        verify(branchRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(modelMapper, times(1)).map(any(User.class), eq(UserResponseDto.class));
    }

    @Test
    void createUserThrowsExceptionIfBranchNotFound() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setBranchId(99L);

        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.createUser(createDto));
        verify(branchRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserSuccessfully() {
        Long userId = 1L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("UpdatedName");
        updateDto.setBranchId(2L);
        updateDto.setRoles(new HashSet<>(Collections.singletonList(Role.ADMIN)));

        Branch existingBranch = new Branch("name2", null);
        existingBranch.setId(1L);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("OriginalName");
        existingUser.setBranch(existingBranch);

        Branch newBranch = new Branch("name3", null);
        newBranch.setId(2L);

        User updatedUserAfterSave = new User();
        updatedUserAfterSave.setId(userId);
        updatedUserAfterSave.setName("UpdatedName");
        updatedUserAfterSave.setBranch(newBranch);
        updatedUserAfterSave.setRoles(new HashSet<>(Collections.singletonList(Role.ADMIN)));


        UserResponseDto expectedUserResponseDto = new UserResponseDto();
        expectedUserResponseDto.setId(userId);
        expectedUserResponseDto.setName("UpdatedName");

        BranchRequestDto expectedBranchRequestDto = new BranchRequestDto();
        expectedBranchRequestDto.setId(2L);
        expectedUserResponseDto.setBranchId(expectedBranchRequestDto.getId());


        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(branchRepository.findById(2L)).thenReturn(Optional.of(newBranch));
        when(userRepository.save(any(User.class))).thenReturn(updatedUserAfterSave);
        when(modelMapper.map(any(User.class), eq(UserResponseDto.class))).thenReturn(expectedUserResponseDto);
        doNothing().when(modelMapper).map(updateDto, existingUser);


        UserResponseDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals(expectedUserResponseDto.getName(), result.getName());
        assertEquals(expectedUserResponseDto.getBranchId(), result.getBranchId());
        verify(userRepository, times(1)).findById(userId);
        verify(branchRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).save(existingUser);
        verify(modelMapper, times(1)).map(updateDto, existingUser);
    }

    @Test
    void updateUserThrowsExceptionIfUserNotFound() {
        Long userId = 99L;
        UserUpdateDto updateDto = new UserUpdateDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUser(userId, updateDto));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByIdSuccessfully() {
        Long userId = 1L;
        Branch branchForUser = new Branch("name4", null);
        branchForUser.setId(1L);
        branchForUser.setName("Main Branch");

        User foundUser = new User();
        foundUser.setId(userId);
        foundUser.setName("RetrievedUser");
        foundUser.setBranch(branchForUser);

        UserResponseDto expectedUserResponseDto = new UserResponseDto();
        expectedUserResponseDto.setId(userId);
        expectedUserResponseDto.setName("RetrievedUser");

        BranchRequestDto expectedBranchRequestDto = new BranchRequestDto();
        expectedBranchRequestDto.setId(1L);
        expectedBranchRequestDto.setName("Main Branch");
        expectedUserResponseDto.setBranchId(expectedBranchRequestDto.getId());


        when(userRepository.findByIdWithBranch(userId)).thenReturn(Optional.of(foundUser));
        when(modelMapper.map(foundUser, UserResponseDto.class)).thenReturn(expectedUserResponseDto);

        UserResponseDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(expectedUserResponseDto.getId(), result.getId());
        assertEquals(expectedUserResponseDto.getName(), result.getName());
        assertEquals(expectedUserResponseDto.getBranchId(), result.getBranchId());
        verify(userRepository, times(1)).findByIdWithBranch(userId);
        verify(modelMapper, times(1)).map(foundUser, UserResponseDto.class);
    }

    @Test
    void getUserByIdThrowsExceptionIfUserNotFound() {
        Long userId = 99L;

        when(userRepository.findByIdWithBranch(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findByIdWithBranch(userId);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getUsersSuccessfully() {
        Branch branch = new Branch("BranchName", null);
        branch.setId(1L);

        User user1 = new User("Alice", "Smith", "A.", Set.of(Role.USER), branch);
        user1.setId(1L);

        User user2 = new User("Bob", "Johnson", "B.", Set.of(Role.ADMIN), branch);
        user2.setId(2L);

        List<User> mockUsers = List.of(user1, user2);

        UserResponseDto userResponseDto1 = new UserResponseDto("Alice", "Smith", "A.", Set.of(Role.USER), null);
        userResponseDto1.setId(1L);
        UserResponseDto userResponseDto2 = new UserResponseDto("Bob", "Johnson", "B.", Set.of(Role.ADMIN), null);
        userResponseDto2.setId(2L);

        when(userRepository.findAll()).thenReturn(mockUsers);
        when(modelMapper.map(user1, UserResponseDto.class)).thenReturn(userResponseDto1);
        when(modelMapper.map(user2, UserResponseDto.class)).thenReturn(userResponseDto2);

        List<UserResponseDto> result = userService.getUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Bob", result.get(1).getName());

        verify(userRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(user1, UserResponseDto.class);
        verify(modelMapper, times(1)).map(user2, UserResponseDto.class);
    }

}