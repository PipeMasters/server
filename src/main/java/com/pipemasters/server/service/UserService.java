package com.pipemasters.server.service;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import com.pipemasters.server.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserCreateDto dto);
    UserResponseDto updateUser(Long userId, UserUpdateDto dto);
    UserResponseDto getUserById(Long userId);
    List<UserResponseDto> getUsers();
    PageDto<UserResponseDto> getPaginatedUsers(Pageable pageable);
    UserResponseDto assignUserToBranch(Long userId, Long branchId);
    List<UserResponseDto> getUsersByBranchId(Long branchId);
    User createAndReturnUser(UserCreateDto dto);
    void delete(Long id);
}
