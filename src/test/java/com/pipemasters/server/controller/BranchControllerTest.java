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
import static org.mockito.ArgumentMatchers.*;
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
    void getBranchById_ReturnsOkStatusAndDtoWhenFound_NoParent() {
        when(branchService.getBranchById(mockBranchDto1.getId(), false)).thenReturn(mockBranchDto1);

        ResponseEntity<BranchDto> response = branchController.getBranchById(mockBranchDto1.getId(), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBranchDto1, response.getBody());
    }

    @Test
    void getBranchById_ReturnsOkStatusAndDtoWhenFound_WithParent() {
        BranchDto childWithParentDto = new BranchDto();
        childWithParentDto.setId(mockBranchDto2.getId());
        childWithParentDto.setName(mockBranchDto2.getName());
        childWithParentDto.setParent(mockBranchDto1);

        when(branchService.getBranchById(mockBranchDto2.getId(), true)).thenReturn(childWithParentDto);

        ResponseEntity<BranchDto> response = branchController.getBranchById(mockBranchDto2.getId(), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(childWithParentDto, response.getBody());
        assertNotNull(response.getBody().getParent());
        assertEquals(mockBranchDto1.getId(), response.getBody().getParent().getId());
    }

    @Test
    void getBranchById_ThrowsExceptionWhenNotFound() {
        when(branchService.getBranchById(anyLong(), anyBoolean())).thenThrow(new BranchNotFoundException("Branch not found"));

        assertThrows(BranchNotFoundException.class, () ->
                branchController.getBranchById(999L, false)
        );
    }

    @Test
    void getBranchByName_ReturnsOkStatusAndDtoWhenFound_NoParent() {
        when(branchService.getBranchByName(mockBranchDto1.getName(), false)).thenReturn(mockBranchDto1);

        ResponseEntity<BranchDto> response = branchController.getBranchByName(mockBranchDto1.getName(), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBranchDto1, response.getBody());
    }

    @Test
    void getBranchByName_ReturnsOkStatusAndDtoWhenFound_WithParent() {
        BranchDto childWithParentDto = new BranchDto();
        childWithParentDto.setId(mockBranchDto2.getId());
        childWithParentDto.setName(mockBranchDto2.getName());
        childWithParentDto.setParent(mockBranchDto1);

        when(branchService.getBranchByName(mockBranchDto2.getName(), true)).thenReturn(childWithParentDto);

        ResponseEntity<BranchDto> response = branchController.getBranchByName(mockBranchDto2.getName(), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(childWithParentDto, response.getBody());
        assertNotNull(response.getBody().getParent());
    }

    @Test
    void getBranchByName_ThrowsExceptionWhenNotFound() {
        when(branchService.getBranchByName(anyString(), anyBoolean())).thenThrow(new BranchNotFoundException("Branch not found"));

        assertThrows(BranchNotFoundException.class, () ->
                branchController.getBranchByName("NonExistent", false)
        );
    }

    @Test
    void getAllBranches_ReturnsOkStatusAndListOfDtos_NoParents() {
        List<BranchDto> expectedBranches = Arrays.asList(mockBranchDto1, mockBranchDto2);
        BranchDto simpleBranchDto1 = new BranchDto();
        simpleBranchDto1.setId(mockBranchDto1.getId());
        simpleBranchDto1.setName(mockBranchDto1.getName());

        BranchDto simpleBranchDto2 = new BranchDto();
        simpleBranchDto2.setId(mockBranchDto2.getId());
        simpleBranchDto2.setName(mockBranchDto2.getName());
        simpleBranchDto2.setParent(null);

        when(branchService.getAllBranches(false)).thenReturn(Arrays.asList(simpleBranchDto1, simpleBranchDto2));

        ResponseEntity<List<BranchDto>> response = branchController.getAllBranches(false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(simpleBranchDto1, response.getBody().get(0));
        assertEquals(simpleBranchDto2, response.getBody().get(1));
        assertNull(response.getBody().get(0).getParent());
        assertNull(response.getBody().get(1).getParent());
    }

    @Test
    void getAllBranches_ReturnsOkStatusAndListOfDtos_WithParents() {
        List<BranchDto> expectedBranches = Arrays.asList(mockBranchDto1, mockBranchDto2);
        when(branchService.getAllBranches(true)).thenReturn(expectedBranches);

        ResponseEntity<List<BranchDto>> response = branchController.getAllBranches(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedBranches, response.getBody());
        assertNull(response.getBody().get(0).getParent());
        assertNotNull(response.getBody().get(1).getParent());
    }

    @Test
    void getAllBranches_ReturnsEmptyListWhenNoBranchesExist() {
        when(branchService.getAllBranches(anyBoolean())).thenReturn(Collections.emptyList());

        ResponseEntity<List<BranchDto>> response = branchController.getAllBranches(false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getChildBranches_ReturnsOkStatusAndListOfDtosWhenParentIdGiven_NoParentsInResponse() {
        List<BranchDto> children = Arrays.asList(mockBranchDto2);
        BranchDto childWithoutParentInResponse = new BranchDto();
        childWithoutParentInResponse.setId(mockBranchDto2.getId());
        childWithoutParentInResponse.setName(mockBranchDto2.getName());
        childWithoutParentInResponse.setParent(null);

        when(branchService.getChildBranches(mockBranchDto1.getId(), false)).thenReturn(Arrays.asList(childWithoutParentInResponse));

        ResponseEntity<List<BranchDto>> response = branchController.getChildBranches(mockBranchDto1.getId(), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(childWithoutParentInResponse, response.getBody().get(0));
        assertNull(response.getBody().get(0).getParent());
    }

    @Test
    void getChildBranches_ReturnsOkStatusAndListOfDtosWhenParentIdGiven_WithParentsInResponse() {
        List<BranchDto> children = Arrays.asList(mockBranchDto2);
        when(branchService.getChildBranches(mockBranchDto1.getId(), true)).thenReturn(children);

        ResponseEntity<List<BranchDto>> response = branchController.getChildBranches(mockBranchDto1.getId(), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(children, response.getBody());
        assertNotNull(response.getBody().get(0).getParent());
        assertEquals(mockBranchDto1.getId(), response.getBody().get(0).getParent().getId());
    }

    @Test
    void getChildBranches_ReturnsEmptyListWhenNoChildrenFound() {
        when(branchService.getChildBranches(eq(mockBranchDto1.getId()), anyBoolean())).thenReturn(Collections.emptyList());

        ResponseEntity<List<BranchDto>> response = branchController.getChildBranches(mockBranchDto1.getId(), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getChildBranches_ThrowsExceptionWhenParentNotFound() {
        when(branchService.getChildBranches(anyLong(), anyBoolean())).thenThrow(new BranchNotFoundException("Parent branch not found"));

        assertThrows(BranchNotFoundException.class, () ->
                branchController.getChildBranches(999L, false)
        );
    }

    @Test
    void getParentsBranches_ReturnsOkStatusAndListOfRootBranches() {
        List<BranchDto> rootBranches = Arrays.asList(mockBranchDto1);
        when(branchService.getParentBranches()).thenReturn(rootBranches);

        ResponseEntity<List<BranchDto>> response = branchController.getParentsBranches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockBranchDto1, response.getBody().get(0));
        assertNull(response.getBody().get(0).getParent());
    }

    @Test
    void getParentsBranches_ReturnsEmptyListWhenNoRootBranchesExist() {
        when(branchService.getParentBranches()).thenReturn(Collections.emptyList());

        ResponseEntity<List<BranchDto>> response = branchController.getParentsBranches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}