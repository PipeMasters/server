package com.pipemasters.server.service;

import com.pipemasters.server.dto.UserDto;
import com.pipemasters.server.dto.UserCreateDto;
import com.pipemasters.server.dto.UserUpdateDto;

public interface UserService {
    UserDto createUser(UserCreateDto dto);
    UserDto updateUser(Long userId, UserUpdateDto dto);
    UserDto getUserById(Long userId);
}
