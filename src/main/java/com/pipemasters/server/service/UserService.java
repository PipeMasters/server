package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserCreateDto dto);
    UserResponseDto updateUser(Long userId, UserUpdateDto dto);
    UserResponseDto getUserById(Long userId);
    List<UserResponseDto> getUsers();
    UserResponseDto assignUserToBranch(Long userId, Long branchId);
    List<UserResponseDto> getUsersByBranchId(Long branchId);
}
