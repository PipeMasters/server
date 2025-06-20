package com.pipemasters.server.controller;

import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.service.BranchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchControllerTest {

    @Mock
    private BranchService branchService;

    @InjectMocks
    private BranchController branchController;

    @Test
    void create_ReturnsCreatedStatusAndDto() {
        BranchDto input = new BranchDto();
        BranchDto saved = new BranchDto();

        when(branchService.createBranch(input)).thenReturn(saved);

        ResponseEntity<BranchDto> response = branchController.create(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());
    }

    @Test
    void updateName_ReturnsOkStatusAndUpdatedDto() {
        Long id = 1L;
        String newName = "UpdatedName";
        BranchDto updated = new BranchDto();

        when(branchService.updateBranchName(id, newName)).thenReturn(updated);

        ResponseEntity<BranchDto> response = branchController.rename(id, newName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
    }

    @Test
    void reassignParent_ReturnsOkStatusAndDto() {
        Long id = 2L;
        Long newParentId = 1L;
        BranchDto updated = new BranchDto();

        when(branchService.reassignParent(id, newParentId)).thenReturn(updated);

        ResponseEntity<BranchDto> response = branchController.reassignParent(id, newParentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
    }
}