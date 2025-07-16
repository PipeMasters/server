package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.DelegationRequestDto;
import com.pipemasters.server.dto.response.DelegationResponseDto;
import com.pipemasters.server.entity.Delegation;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
import com.pipemasters.server.repository.DelegationRepository;
import com.pipemasters.server.repository.UserRepository;
import com.pipemasters.server.service.impl.DelegationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DelegationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DelegationRepository delegationRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private DelegationServiceImpl delegationService;

    @Test
    void delegateShouldThrowExceptionWhenStartDateIsAfterEndDate() {
        DelegationRequestDto delegationRequestDTO = new DelegationRequestDto();
        delegationRequestDTO.setFromDate(LocalDate.of(2023, 10, 10));
        delegationRequestDTO.setToDate(LocalDate.of(2023, 10, 5));

        assertThrows(IllegalArgumentException.class, () -> delegationService.delegate(delegationRequestDTO));
    }

    @Test
    void delegateShouldThrowExceptionWhenStartDateOrEndDateIsNull() {
        DelegationRequestDto delegationRequestDTO = new DelegationRequestDto();
        delegationRequestDTO.setFromDate(null);
        delegationRequestDTO.setToDate(LocalDate.of(2023, 10, 10));

        assertThrows(IllegalArgumentException.class, () -> delegationService.delegate(delegationRequestDTO));

        delegationRequestDTO.setFromDate(LocalDate.of(2023, 10, 10));
        delegationRequestDTO.setToDate(null);

        assertThrows(IllegalArgumentException.class, () -> delegationService.delegate(delegationRequestDTO));
    }

    @Test
    void delegateShouldThrowExceptionWhenDelegatorNotFound() {
        DelegationRequestDto delegationRequestDTO = new DelegationRequestDto();
        delegationRequestDTO.setFromDate(LocalDate.of(2023, 10, 5));
        delegationRequestDTO.setToDate(LocalDate.of(2023, 10, 10));
        delegationRequestDTO.setDelegatorId(1L);
        delegationRequestDTO.setSubstituteId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> delegationService.delegate(delegationRequestDTO));
    }

    @Test
    void delegateShouldThrowExceptionWhenSubstituteNotFound() {
        DelegationRequestDto delegationRequestDTO = new DelegationRequestDto();
        delegationRequestDTO.setFromDate(LocalDate.of(2023, 10, 5));
        delegationRequestDTO.setToDate(LocalDate.of(2023, 10, 10));
        delegationRequestDTO.setDelegatorId(1L);
        delegationRequestDTO.setSubstituteId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> delegationService.delegate(delegationRequestDTO));
    }

    @Test
    void delegateShouldSaveDelegationAndReturnDelegationDto() {
        DelegationRequestDto delegationRequestDTO = new DelegationRequestDto();
        delegationRequestDTO.setFromDate(LocalDate.of(2023, 10, 5));
        delegationRequestDTO.setToDate(LocalDate.of(2023, 10, 10));
        delegationRequestDTO.setDelegatorId(1L);
        delegationRequestDTO.setSubstituteId(2L);

        User delegator = new User();
        User substitute = new User();
        Delegation delegation = new Delegation(delegator, substitute, delegationRequestDTO.getFromDate(), delegationRequestDTO.getToDate());
        DelegationResponseDto expectedDelegationResponseDto = new DelegationResponseDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(delegator));
        when(userRepository.findById(2L)).thenReturn(Optional.of(substitute));
        when(delegationRepository.save(any(Delegation.class))).thenReturn(delegation);
        when(modelMapper.map(any(Delegation.class), eq(DelegationResponseDto.class))).thenReturn(expectedDelegationResponseDto);

        DelegationResponseDto result = delegationService.delegate(delegationRequestDTO);

        assertEquals(expectedDelegationResponseDto, result);
    }
}