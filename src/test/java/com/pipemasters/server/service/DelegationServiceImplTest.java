package com.pipemasters.server.service;

import com.pipemasters.server.dto.DelegationDto;
import com.pipemasters.server.entity.Delegation;
import com.pipemasters.server.entity.User;
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
        DelegationDto delegationDTO = new DelegationDto();
        delegationDTO.setFromDate(LocalDate.of(2023, 10, 10));
        delegationDTO.setToDate(LocalDate.of(2023, 10, 5));

        assertThrows(IllegalArgumentException.class, () -> delegationService.delegate(delegationDTO));
    }

    @Test
    void delegateShouldThrowExceptionWhenStartDateOrEndDateIsNull() {
        DelegationDto delegationDTO = new DelegationDto();
        delegationDTO.setFromDate(null);
        delegationDTO.setToDate(LocalDate.of(2023, 10, 10));

        assertThrows(IllegalArgumentException.class, () -> delegationService.delegate(delegationDTO));

        delegationDTO.setFromDate(LocalDate.of(2023, 10, 10));
        delegationDTO.setToDate(null);

        assertThrows(IllegalArgumentException.class, () -> delegationService.delegate(delegationDTO));
    }

    @Test
    void delegateShouldThrowExceptionWhenDelegatorNotFound() {
        DelegationDto delegationDTO = new DelegationDto();
        delegationDTO.setFromDate(LocalDate.of(2023, 10, 5));
        delegationDTO.setToDate(LocalDate.of(2023, 10, 10));
        delegationDTO.setDelegatorId(1L);
        delegationDTO.setSubstituteId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> delegationService.delegate(delegationDTO));
    }

    @Test
    void delegateShouldThrowExceptionWhenSubstituteNotFound() {
        DelegationDto delegationDTO = new DelegationDto();
        delegationDTO.setFromDate(LocalDate.of(2023, 10, 5));
        delegationDTO.setToDate(LocalDate.of(2023, 10, 10));
        delegationDTO.setDelegatorId(1L);
        delegationDTO.setSubstituteId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> delegationService.delegate(delegationDTO));
    }

    @Test
    void delegateShouldSaveDelegationAndReturnDelegationDto() {
        DelegationDto delegationDTO = new DelegationDto();
        delegationDTO.setFromDate(LocalDate.of(2023, 10, 5));
        delegationDTO.setToDate(LocalDate.of(2023, 10, 10));
        delegationDTO.setDelegatorId(1L);
        delegationDTO.setSubstituteId(2L);

        User delegator = new User();
        User substitute = new User();
        Delegation delegation = new Delegation(delegator, substitute, delegationDTO.getFromDate(), delegationDTO.getToDate());
        DelegationDto expectedDelegationDto = new DelegationDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(delegator));
        when(userRepository.findById(2L)).thenReturn(Optional.of(substitute));
        when(delegationRepository.save(any(Delegation.class))).thenReturn(delegation);
        when(modelMapper.map(any(Delegation.class), eq(DelegationDto.class))).thenReturn(expectedDelegationDto);

        DelegationDto result = delegationService.delegate(delegationDTO);

        assertEquals(expectedDelegationDto, result);
    }
}