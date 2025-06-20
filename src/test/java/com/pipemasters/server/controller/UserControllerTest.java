package com.pipemasters.server.controller;

import com.pipemasters.server.dto.BranchDto; // Assuming this DTO exists and has id/name
import com.pipemasters.server.dto.UserDto;
import com.pipemasters.server.dto.UserCreateDto;
import com.pipemasters.server.dto.UserUpdateDto;
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

        UserDto expectedUserDto = new UserDto();
        expectedUserDto.setId(1L);
        expectedUserDto.setName("Test");
        expectedUserDto.setSurname("User");

        // FIX: Initialize BranchDto and set it on expectedUserDto
        BranchDto branchDto = new BranchDto();
        branchDto.setId(1L);
        // If BranchDto also has a 'name' field and your DTO mapping fills it, you might want to set it here too:
        // branchDto.setName("Test Branch");
        expectedUserDto.setBranch(branchDto); // Set the initialized BranchDto

        expectedUserDto.setRoles(Collections.singleton(Role.USER));

        when(userService.createUser(createDto)).thenReturn(expectedUserDto);

        ResponseEntity<UserDto> response = userController.createUser(createDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        // Verify that the branch in the returned DTO is not null and has the correct ID
        assertNotNull(response.getBody().getBranch());
        assertEquals(expectedUserDto.getBranch().getId(), response.getBody().getBranch().getId());
        assertEquals(expectedUserDto.getId(), response.getBody().getId()); // General check for other fields
        verify(userService, times(1)).createUser(createDto);
    }

    @Test
    void updateUserReturnsUpdatedUser() {
        Long userId = 1L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("UpdatedName");
        updateDto.setId(userId);

        UserDto expectedUserDto = new UserDto();
        expectedUserDto.setId(userId);
        expectedUserDto.setName("UpdatedName");
        // FIX: Initialize BranchDto and set it, assuming updates can change the branch
        BranchDto branchDto = new BranchDto();
        branchDto.setId(2L); // Assuming branch might change to ID 2
        expectedUserDto.setBranch(branchDto);


        when(userService.updateUser(userId, updateDto)).thenReturn(expectedUserDto);

        ResponseEntity<UserDto> response = userController.updateUser(userId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getBranch()); // Assert branch is not null
        assertEquals(expectedUserDto.getBranch().getId(), response.getBody().getBranch().getId());
        assertEquals(expectedUserDto.getId(), response.getBody().getId());
        verify(userService, times(1)).updateUser(userId, updateDto);
    }

    @Test
    void getUserByIdReturnsUser() {
        Long userId = 1L;
        UserDto expectedUserDto = new UserDto();
        expectedUserDto.setId(userId);
        expectedUserDto.setName("Retrieved");
        // FIX: Initialize BranchDto and set it
        BranchDto branchDto = new BranchDto();
        branchDto.setId(1L);
        branchDto.setName("Retrieved Branch Name"); // If your UserDto also populates branch name
        expectedUserDto.setBranch(branchDto);


        when(userService.getUserById(userId)).thenReturn(expectedUserDto);

        ResponseEntity<UserDto> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getBranch()); // Assert branch is not null
        assertEquals(expectedUserDto.getBranch().getId(), response.getBody().getBranch().getId());
        assertEquals(expectedUserDto.getId(), response.getBody().getId());
        verify(userService, times(1)).getUserById(userId);
    }
}