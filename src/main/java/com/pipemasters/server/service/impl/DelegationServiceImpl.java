package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.DelegationDto;
import com.pipemasters.server.entity.Delegation;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.exceptions.delegation.DelegationDateValidationException;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
import com.pipemasters.server.repository.DelegationRepository;
import com.pipemasters.server.repository.UserRepository;
import com.pipemasters.server.service.DelegationService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DelegationServiceImpl implements DelegationService {
    private final UserRepository userRepository;
    private final DelegationRepository delegationRepository;
    private final ModelMapper modelMapper;

    public DelegationServiceImpl(UserRepository userRepository, DelegationRepository delegationRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.delegationRepository = delegationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public DelegationDto delegate(DelegationDto delegationDTO) {
        if (delegationDTO.getFromDate() == null || delegationDTO.getToDate() == null) {
            throw new DelegationDateValidationException("Start date or/and end date cannot be null");
        }
        if (delegationDTO.getFromDate().isAfter(delegationDTO.getToDate())) {
            throw new DelegationDateValidationException("Start date cannot be after end date");
        }
        User delegator = userRepository.findById(delegationDTO.getDelegatorId())
                .orElseThrow(() -> new UserNotFoundException("Delegator not found"));
        User substitute = userRepository.findById(delegationDTO.getSubstituteId())
                .orElseThrow(() -> new UserNotFoundException("Substitute not found"));
        Delegation delegation = new Delegation(delegator, substitute, delegationDTO.getFromDate(), delegationDTO.getToDate());
        delegationRepository.save(delegation);

        return modelMapper.map(delegation, DelegationDto.class);
    }
}
