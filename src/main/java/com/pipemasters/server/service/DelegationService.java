package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.DelegationRequestDto;
import com.pipemasters.server.dto.response.DelegationResponseDto;

public interface DelegationService {
    DelegationResponseDto delegate(DelegationRequestDto delegationRequestDTO);
}
