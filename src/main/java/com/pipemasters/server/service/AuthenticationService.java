package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.AuthenticationRequestDto;
import com.pipemasters.server.dto.request.RegisterRequestDto;
import com.pipemasters.server.dto.response.AuthenticationResponseDto;
import org.springframework.transaction.annotation.Transactional;

public interface AuthenticationService {
    AuthenticationResponseDto register(RegisterRequestDto request);
    void registerFromImport(RegisterRequestDto request);
    AuthenticationResponseDto login(AuthenticationRequestDto request);
}
