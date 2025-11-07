package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.request.AuthenticationRequestDto;
import com.pipemasters.server.dto.request.RegisterRequestDto;
import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.response.AuthenticationResponseDto;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.UserAccount;
import com.pipemasters.server.repository.UserAccountRepository;
import com.pipemasters.server.service.AuthenticationService;
import com.pipemasters.server.service.JwtService;
import com.pipemasters.server.service.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserService userService;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    public AuthenticationServiceImpl(UserService userService, UserAccountRepository userAccountRepository,
                                     PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, ModelMapper modelMapper) {
        this.userService = userService;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public AuthenticationResponseDto register(RegisterRequestDto request) {
        User savedUser = userService.createAndReturnUser(modelMapper.map(request, UserCreateDto.class));

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.getUsername());
        userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        userAccount.setUser(savedUser);
        savedUser.setUserAccount(userAccount);

        userAccountRepository.save(userAccount);

        String jwtToken = jwtService.generateToken(userAccount);

        return new AuthenticationResponseDto(jwtToken);
    }

    @Override
    @Transactional
    public void registerFromImport(RegisterRequestDto request) {
        UserCreateDto userCreateDto = modelMapper.map(request, UserCreateDto.class);

        User savedUser = userService.createAndReturnUser(userCreateDto);

        if (userAccountRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("User account with username '{}' already exists. Skipping.", request.getUsername());
            return;
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.getUsername());
        userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        userAccount.setUser(savedUser);
        savedUser.setUserAccount(userAccount);

        userAccountRepository.save(userAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponseDto login(AuthenticationRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserAccount userAccount = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        String jwtToken = jwtService.generateToken(userAccount);
        return new AuthenticationResponseDto(jwtToken);
    }
}
