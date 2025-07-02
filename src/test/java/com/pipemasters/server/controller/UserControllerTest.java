package com.pipemasters.server.controller;

import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserController userController;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void createUserReturnsCreatedUser() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("Test");
        createDto.setSurname("User");
        createDto.setBranchId(1L);

        UserResponseDto expectedUserResponseDto = new UserResponseDto();
        expectedUserResponseDto.setId(1L);
        expectedUserResponseDto.setName("Test");
        expectedUserResponseDto.setSurname("User");

        BranchRequestDto branchRequestDto = new BranchRequestDto();
        branchRequestDto.setId(1L);
        expectedUserResponseDto.setBranchId(branchRequestDto.getId());

        expectedUserResponseDto.setRoles(Collections.singleton(Role.USER));

        when(userService.createUser(createDto)).thenReturn(expectedUserResponseDto);

        ResponseEntity<UserResponseDto> response = userController.createUser(createDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getBranchId());
        assertEquals(expectedUserResponseDto.getBranchId(), response.getBody().getBranchId());
        assertEquals(expectedUserResponseDto.getId(), response.getBody().getId());
        verify(userService, times(1)).createUser(createDto);
    }

    @Test
    void updateUserReturnsUpdatedUser() {
        Long userId = 1L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("UpdatedName");

        UserResponseDto expectedUserResponseDto = new UserResponseDto();
        expectedUserResponseDto.setId(userId);
        expectedUserResponseDto.setName("UpdatedName");
        BranchRequestDto branchRequestDto = new BranchRequestDto();
        branchRequestDto.setId(2L);
        expectedUserResponseDto.setBranchId(branchRequestDto.getId());


        when(userService.updateUser(userId, updateDto)).thenReturn(expectedUserResponseDto);

        ResponseEntity<UserResponseDto> response = userController.updateUser(userId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getBranchId());
        assertEquals(expectedUserResponseDto.getBranchId(), response.getBody().getBranchId());
        assertEquals(expectedUserResponseDto.getId(), response.getBody().getId());
        verify(userService, times(1)).updateUser(userId, updateDto);
    }

    @Test
    void getUserByIdReturnsUser() {
        Long userId = 1L;
        UserResponseDto expectedUserResponseDto = new UserResponseDto();
        expectedUserResponseDto.setId(userId);
        expectedUserResponseDto.setName("Retrieved");
        BranchRequestDto branchRequestDto = new BranchRequestDto();
        branchRequestDto.setId(1L);
        branchRequestDto.setName("Retrieved Branch Name");
        expectedUserResponseDto.setBranchId(branchRequestDto.getId());


        when(userService.getUserById(userId)).thenReturn(expectedUserResponseDto);

        ResponseEntity<UserResponseDto> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getBranchId());
        assertEquals(expectedUserResponseDto.getBranchId(), response.getBody().getBranchId());
        assertEquals(expectedUserResponseDto.getId(), response.getBody().getId());
        verify(userService, times(1)).getUserById(userId);
    }
}