package com.pipemasters.server.controller;

import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.service.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchControllerTest {

    @Mock
    private BranchService branchService;

    @InjectMocks
    private BranchController branchController;

    private BranchDto mockBranchDto1;
    private BranchDto mockBranchDto2;

    @BeforeEach
    void setUp() {
        mockBranchDto1 = new BranchDto();
        mockBranchDto1.setId(1L);
        mockBranchDto1.setName("Test Branch 1");

        mockBranchDto2 = new BranchDto();
        mockBranchDto2.setId(2L);
        mockBranchDto2.setName("Test Branch 2");
        mockBranchDto2.setParent(mockBranchDto1);
    }

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

    @Test
    void getBranchById_ReturnsOkStatusAndDtoWhenFound() {
        when(branchService.getBranchById(mockBranchDto1.getId())).thenReturn(mockBranchDto1);

        ResponseEntity<BranchDto> response = branchController.getBranchById(mockBranchDto1.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBranchDto1, response.getBody());
    }

    @Test
    void getBranchById_ThrowsExceptionWhenNotFound() {
        when(branchService.getBranchById(anyLong())).thenThrow(new BranchNotFoundException("Branch not found"));

        assertThrows(BranchNotFoundException.class, () ->
                branchController.getBranchById(999L)
        );
    }

    @Test
    void getBranchByName_ReturnsOkStatusAndDtoWhenFound() {
        when(branchService.getBranchByName(mockBranchDto1.getName())).thenReturn(mockBranchDto1);

        ResponseEntity<BranchDto> response = branchController.getBranchByName(mockBranchDto1.getName());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBranchDto1, response.getBody());
    }

    @Test
    void getBranchByName_ThrowsExceptionWhenNotFound() {
        when(branchService.getBranchByName(anyString())).thenThrow(new BranchNotFoundException("Branch not found"));

        assertThrows(BranchNotFoundException.class, () ->
                branchController.getBranchByName("NonExistent")
        );
    }

    @Test
    void getAllBranches_ReturnsOkStatusAndListOfDtos() {
        List<BranchDto> expectedBranches = Arrays.asList(mockBranchDto1, mockBranchDto2);
        when(branchService.getAllBranches()).thenReturn(expectedBranches);

        ResponseEntity<List<BranchDto>> response = branchController.getAllBranches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedBranches, response.getBody());
    }

    @Test
    void getAllBranches_ReturnsEmptyListWhenNoBranchesExist() {
        when(branchService.getAllBranches()).thenReturn(Collections.emptyList());

        ResponseEntity<List<BranchDto>> response = branchController.getAllBranches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getChildBranches_ReturnsOkStatusAndListOfDtosWhenParentIdGiven() {
        List<BranchDto> children = Arrays.asList(mockBranchDto2);
        when(branchService.getChildBranches(mockBranchDto1.getId())).thenReturn(children);

        ResponseEntity<List<BranchDto>> response = branchController.getChildBranches(mockBranchDto1.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(children, response.getBody());
    }

    @Test
    void getChildBranches_ReturnsEmptyListWhenNoChildrenFound() {
        when(branchService.getChildBranches(mockBranchDto1.getId())).thenReturn(Collections.emptyList());

        ResponseEntity<List<BranchDto>> response = branchController.getChildBranches(mockBranchDto1.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getChildBranches_ThrowsExceptionWhenParentNotFound() {
        when(branchService.getChildBranches(anyLong())).thenThrow(new BranchNotFoundException("Parent branch not found"));

        assertThrows(BranchNotFoundException.class, () ->
                branchController.getChildBranches(999L)
        );
    }
}