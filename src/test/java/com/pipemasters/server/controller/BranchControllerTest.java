package com.pipemasters.server.controller;

import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.BranchResponseDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchControllerTest {

    @Mock
    private BranchService branchService;

    @InjectMocks
    private BranchController branchController;

    private BranchResponseDto mockBranchResponseDto1;
    private BranchResponseDto mockBranchResponseDto2;

    @BeforeEach
    void setUp() {
        mockBranchResponseDto1 = new BranchResponseDto();
        mockBranchResponseDto1.setId(1L);
        mockBranchResponseDto1.setName("Test Branch 1");

        mockBranchResponseDto2 = new BranchResponseDto();
        mockBranchResponseDto2.setId(2L);
        mockBranchResponseDto2.setName("Test Branch 2");
        mockBranchResponseDto2.setParentId(mockBranchResponseDto1.getId());
    }

    @Test
    void create_ReturnsCreatedStatusAndDto() {
        BranchRequestDto input = new BranchRequestDto();
        BranchResponseDto saved = new BranchResponseDto();

        when(branchService.createBranch(input)).thenReturn(saved);

        ResponseEntity<BranchResponseDto> response = branchController.create(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());
    }

    @Test
    void updateName_ReturnsOkStatusAndUpdatedDto() {
        Long id = 1L;
        String newName = "UpdatedName";
        BranchResponseDto updated = new BranchResponseDto();

        when(branchService.updateBranchName(id, newName)).thenReturn(updated);

        ResponseEntity<BranchResponseDto> response = branchController.rename(id, newName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
    }

    @Test
    void reassignParent_ReturnsOkStatusAndDto() {
        Long id = 2L;
        Long newParentId = 1L;
        BranchResponseDto updated = new BranchResponseDto();

        when(branchService.reassignParent(id, newParentId)).thenReturn(updated);

        ResponseEntity<BranchResponseDto> response = branchController.reassignParent(id, newParentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
    }

    @Test
    void getBranchById_ReturnsOkStatusAndDtoWhenFound_NoParent() {
        when(branchService.getBranchById(mockBranchResponseDto1.getId(), false)).thenReturn(mockBranchResponseDto1);

        ResponseEntity<BranchResponseDto> response = branchController.getBranchById(mockBranchResponseDto1.getId(), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBranchResponseDto1, response.getBody());
    }

    @Test
    void getBranchById_ReturnsOkStatusAndDtoWhenFound_WithParent() {
        BranchResponseDto childWithParentDto = new BranchResponseDto();
        childWithParentDto.setId(mockBranchResponseDto2.getId());
        childWithParentDto.setName(mockBranchResponseDto2.getName());
        childWithParentDto.setParentId(mockBranchResponseDto1.getId());

        when(branchService.getBranchById(mockBranchResponseDto2.getId(), true)).thenReturn(childWithParentDto);

        ResponseEntity<BranchResponseDto> response = branchController.getBranchById(mockBranchResponseDto2.getId(), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(childWithParentDto, response.getBody());
        assertNotNull(response.getBody().getParentId());
        assertEquals(mockBranchResponseDto1.getId(), response.getBody().getParentId());
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
        when(branchService.getBranchByName(mockBranchResponseDto1.getName(), false)).thenReturn(mockBranchResponseDto1);

        ResponseEntity<BranchResponseDto> response = branchController.getBranchByName(mockBranchResponseDto1.getName(), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBranchResponseDto1, response.getBody());
    }

    @Test
    void getBranchByName_ReturnsOkStatusAndDtoWhenFound_WithParent() {
        BranchResponseDto childWithParentDto = new BranchResponseDto();
        childWithParentDto.setId(mockBranchResponseDto2.getId());
        childWithParentDto.setName(mockBranchResponseDto2.getName());
        childWithParentDto.setParentId(mockBranchResponseDto1.getId());

        when(branchService.getBranchByName(mockBranchResponseDto2.getName(), true)).thenReturn(childWithParentDto);

        ResponseEntity<BranchResponseDto> response = branchController.getBranchByName(mockBranchResponseDto2.getName(), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(childWithParentDto, response.getBody());
        assertNotNull(response.getBody().getParentId());
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
        List<BranchResponseDto> expectedBranches = Arrays.asList(mockBranchResponseDto1, mockBranchResponseDto2);
        BranchResponseDto simpleBranchResponseDto1 = new BranchResponseDto();
        simpleBranchResponseDto1.setId(mockBranchResponseDto1.getId());
        simpleBranchResponseDto1.setName(mockBranchResponseDto1.getName());

        BranchResponseDto simpleBranchResponseDto2 = new BranchResponseDto();
        simpleBranchResponseDto2.setId(mockBranchResponseDto2.getId());
        simpleBranchResponseDto2.setName(mockBranchResponseDto2.getName());
        simpleBranchResponseDto2.setParentId(null);

        when(branchService.getAllBranches(false)).thenReturn(Arrays.asList(simpleBranchResponseDto1, simpleBranchResponseDto2));

        ResponseEntity<List<BranchResponseDto>> response = branchController.getAllBranches(false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(simpleBranchResponseDto1, response.getBody().get(0));
        assertEquals(simpleBranchResponseDto2, response.getBody().get(1));
        assertNull(response.getBody().get(0).getParentId());
        assertNull(response.getBody().get(1).getParentId());
    }

    @Test
    void getAllBranches_ReturnsOkStatusAndListOfDtos_WithParents() {
        List<BranchResponseDto> expectedBranches = Arrays.asList(mockBranchResponseDto1, mockBranchResponseDto2);
        when(branchService.getAllBranches(true)).thenReturn(expectedBranches);

        ResponseEntity<List<BranchResponseDto>> response = branchController.getAllBranches(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedBranches, response.getBody());
        assertNull(response.getBody().get(0).getParentId());
        assertNotNull(response.getBody().get(1).getParentId());
    }

    @Test
    void getAllBranches_ReturnsEmptyListWhenNoBranchesExist() {
        when(branchService.getAllBranches(anyBoolean())).thenReturn(Collections.emptyList());

        ResponseEntity<List<BranchResponseDto>> response = branchController.getAllBranches(false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getChildBranches_ReturnsOkStatusAndListOfDtosWhenParentIdGiven_NoParentsInResponse() {
        List<BranchResponseDto> children = Arrays.asList(mockBranchResponseDto2);
        BranchResponseDto childWithoutParentInResponse = new BranchResponseDto();
        childWithoutParentInResponse.setId(mockBranchResponseDto2.getId());
        childWithoutParentInResponse.setName(mockBranchResponseDto2.getName());
        childWithoutParentInResponse.setParentId(null);

        when(branchService.getChildBranches(mockBranchResponseDto1.getId(), false)).thenReturn(Arrays.asList(childWithoutParentInResponse));

        ResponseEntity<List<BranchResponseDto>> response = branchController.getChildBranches(mockBranchResponseDto1.getId(), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(childWithoutParentInResponse, response.getBody().get(0));
        assertNull(response.getBody().get(0).getParentId());
    }

    @Test
    void getChildBranches_ReturnsOkStatusAndListOfDtosWhenParentIdGiven_WithParentsInResponse() {
        List<BranchResponseDto> children = Arrays.asList(mockBranchResponseDto2);
        when(branchService.getChildBranches(mockBranchResponseDto1.getId(), true)).thenReturn(children);

        ResponseEntity<List<BranchResponseDto>> response = branchController.getChildBranches(mockBranchResponseDto1.getId(), true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(children, response.getBody());
        assertNotNull(response.getBody().get(0).getParentId());
        assertEquals(mockBranchResponseDto1.getId(), response.getBody().get(0).getParentId());
    }

    @Test
    void getChildBranches_ReturnsEmptyListWhenNoChildrenFound() {
        when(branchService.getChildBranches(eq(mockBranchResponseDto1.getId()), anyBoolean())).thenReturn(Collections.emptyList());

        ResponseEntity<List<BranchResponseDto>> response = branchController.getChildBranches(mockBranchResponseDto1.getId(), false);

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
        List<BranchResponseDto> rootBranches = Arrays.asList(mockBranchResponseDto1);
        when(branchService.getParentBranches()).thenReturn(rootBranches);

        ResponseEntity<List<BranchResponseDto>> response = branchController.getParentsBranches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockBranchResponseDto1, response.getBody().get(0));
        assertNull(response.getBody().get(0).getParentId());
    }

    @Test
    void getParentsBranches_ReturnsEmptyListWhenNoRootBranchesExist() {
        when(branchService.getParentBranches()).thenReturn(Collections.emptyList());

        ResponseEntity<List<BranchResponseDto>> response = branchController.getParentsBranches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}