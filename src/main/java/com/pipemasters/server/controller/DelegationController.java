package com.pipemasters.server.controller;

import com.pipemasters.server.dto.DelegationDto;
import com.pipemasters.server.service.DelegationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/delegation")
public class DelegationController {
    private final DelegationService delegationService;

    public DelegationController(DelegationService delegationService) {
        this.delegationService = delegationService;
    }

    @PostMapping("/delegate")
    public ResponseEntity<DelegationDto> delegate(@RequestBody DelegationDto delegationDTO) {
        return new ResponseEntity<DelegationDto>(delegationService.delegate(delegationDTO), HttpStatus.CREATED);
    }
}
