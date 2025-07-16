package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.repository.UserRepository;
import com.pipemasters.server.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private ModelMapper modelMapper;

    private Branch mockBranch;
    private Branch anotherMockBranch;

    @BeforeEach
    void setUp() {

        mockBranch = new Branch("Main Branch", null);
        mockBranch.setId(1L);

        anotherMockBranch = new Branch("Another Branch", null);
        anotherMockBranch.setId(2L);
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

    @Test
    void assignUserToBranchSuccessfully() {
        Long userId = 1L;
        Long branchId = 2L;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Test User");
        existingUser.setBranch(null);

        Branch newBranch = new Branch("New Branch", null);
        newBranch.setId(branchId);

        User updatedUserAfterSave = new User();
        updatedUserAfterSave.setId(userId);
        updatedUserAfterSave.setName("Test User");
        updatedUserAfterSave.setBranch(newBranch);

        UserResponseDto expectedUserResponseDto = new UserResponseDto();
        expectedUserResponseDto.setId(userId);
        expectedUserResponseDto.setName("Test User");
        expectedUserResponseDto.setBranchId(branchId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(newBranch));
        when(userRepository.save(any(User.class))).thenReturn(updatedUserAfterSave);
        when(modelMapper.map(updatedUserAfterSave, UserResponseDto.class)).thenReturn(expectedUserResponseDto);

        UserResponseDto result = userService.assignUserToBranch(userId, branchId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(branchId, result.getBranchId());
        verify(userRepository, times(1)).findById(userId);
        verify(branchRepository, times(1)).findById(branchId);
        verify(userRepository, times(1)).save(existingUser);
        verify(modelMapper, times(1)).map(updatedUserAfterSave, UserResponseDto.class);
    }

    @Test
    void assignUserToBranchThrowsExceptionIfUserNotFound() {
        Long userId = 99L;
        Long branchId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.assignUserToBranch(userId, branchId));
        verify(userRepository, times(1)).findById(userId);
        verify(branchRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void assignUserToBranchThrowsExceptionIfBranchNotFound() {
        Long userId = 1L;
        Long branchId = 99L;

        User existingUser = new User();
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        assertThrows(BranchNotFoundException.class, () -> userService.assignUserToBranch(userId, branchId));
        verify(userRepository, times(1)).findById(userId);
        verify(branchRepository, times(1)).findById(branchId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUsersByBranchId_ShouldReturnListOfUsers() {
        User user1 = new User("User1", "S1", "P1", Collections.singleton(Role.USER), mockBranch); user1.setId(1L);
        User user2 = new User("User2", "S2", "P2", Collections.singleton(Role.USER), mockBranch); user2.setId(2L);
        List<User> usersForBranch = Arrays.asList(user1, user2);

        UserResponseDto dto1 = new UserResponseDto(); dto1.setId(1L); dto1.setName("User1"); dto1.setBranchId(mockBranch.getId());
        UserResponseDto dto2 = new UserResponseDto(); dto2.setId(2L); dto2.setName("User2"); dto2.setBranchId(mockBranch.getId());
        List<UserResponseDto> expectedDtos = Arrays.asList(dto1, dto2);

        when(branchRepository.existsById(mockBranch.getId())).thenReturn(true);
        when(userRepository.findByBranchId(mockBranch.getId())).thenReturn(usersForBranch);
        when(modelMapper.map(user1, UserResponseDto.class)).thenReturn(dto1);
        when(modelMapper.map(user2, UserResponseDto.class)).thenReturn(dto2);

        List<UserResponseDto> result = userService.getUsersByBranchId(mockBranch.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedDtos));
        verify(branchRepository, times(1)).existsById(mockBranch.getId());
        verify(userRepository, times(1)).findByBranchId(mockBranch.getId());
    }

    @Test
    void getUsersByBranchId_ShouldReturnEmptyList_WhenNoUsersFoundForBranch() {
        when(branchRepository.existsById(mockBranch.getId())).thenReturn(true);
        when(userRepository.findByBranchId(mockBranch.getId())).thenReturn(Collections.emptyList());

        List<UserResponseDto> result = userService.getUsersByBranchId(mockBranch.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(branchRepository, times(1)).existsById(mockBranch.getId());
        verify(userRepository, times(1)).findByBranchId(mockBranch.getId());
    }

    @Test
    void getUsersByBranchId_ShouldThrowBranchNotFoundException_WhenBranchDoesNotExist() {
        when(branchRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(BranchNotFoundException.class, () -> userService.getUsersByBranchId(99L));

        verify(branchRepository, times(1)).existsById(99L);
        verify(userRepository, never()).findByBranchId(anyLong());
    }
}