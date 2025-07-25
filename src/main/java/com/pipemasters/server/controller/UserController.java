package com.pipemasters.server.controller;

import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import com.pipemasters.server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto createDTO) {
        UserResponseDto newUser = userService.createUser(createDTO);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto updateDTO) {
        UserResponseDto updatedUser = userService.updateUser(id, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getUsers() {
        List<UserResponseDto> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/assignBranch/{branchId}")
    public ResponseEntity<UserResponseDto> assignUserToBranch(@PathVariable Long userId, @PathVariable Long branchId) {
        UserResponseDto updatedUser = userService.assignUserToBranch(userId, branchId);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/by-branch/{branchId}")
    public ResponseEntity<List<UserResponseDto>> getUsersByBranchId(@PathVariable Long branchId) {
        List<UserResponseDto> users = userService.getUsersByBranchId(branchId);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
