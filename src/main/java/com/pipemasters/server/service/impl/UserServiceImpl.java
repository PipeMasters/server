package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.repository.UserRepository;
import com.pipemasters.server.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, BranchRepository branchRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @CacheEvict(cacheNames = "users", allEntries = true)
    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));

        User user = new User();
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setPatronymic(dto.getPatronymic());
        user.setBranch(branch);

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Set<Role> defaultRoles = new HashSet<>();
            defaultRoles.add(Role.USER);
            user.setRoles(defaultRoles);
        }

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    @Override
    @CacheEvict(cacheNames = "users", allEntries = true)
    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        modelMapper.map(dto, user);

         if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
             user.setRoles(dto.getRoles());
         }


        if (dto.getBranchId() != null) {
            Branch newBranch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));
            user.setBranch(newBranch);
        }

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findByIdWithBranch(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return modelMapper.map(user, UserResponseDto.class);
    }

    @Override
    @Cacheable("users")
    public List<UserResponseDto> getUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(u -> modelMapper.map(u, UserResponseDto.class)).toList();
    }
}