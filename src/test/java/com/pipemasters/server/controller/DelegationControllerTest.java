package com.pipemasters.server.controller;

import com.pipemasters.server.dto.request.DelegationRequestDto;
import com.pipemasters.server.dto.response.DelegationResponseDto;
import com.pipemasters.server.service.DelegationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelegationControllerTest {

    @Mock
    private DelegationService delegationService;

    @InjectMocks
    private DelegationController delegationController;

    @Test
    void delegateReturnsCreatedStatusAndDelegationDto() {
        DelegationRequestDto inputDTO = new DelegationRequestDto();
        DelegationResponseDto resultDTO = new DelegationResponseDto();

        when(delegationService.delegate(inputDTO)).thenReturn(resultDTO);

        ResponseEntity<DelegationResponseDto> response = delegationController.delegate(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(resultDTO, response.getBody());
    }

    @Test
    void delegateThrowsExceptionPropagates() {
        DelegationRequestDto inputDTO = new DelegationRequestDto();

        when(delegationService.delegate(inputDTO)).thenThrow(new IllegalArgumentException("Invalid data"));

        assertThrows(IllegalArgumentException.class, () -> delegationController.delegate(inputDTO));
    }
}