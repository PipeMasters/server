package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserCreateDto dto);
    UserResponseDto updateUser(Long userId, UserUpdateDto dto);
    UserResponseDto getUserById(Long userId);
    List<UserResponseDto> getUsers();
}
