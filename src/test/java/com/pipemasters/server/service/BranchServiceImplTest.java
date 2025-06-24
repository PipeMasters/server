package com.pipemasters.server.service;

import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.service.impl.BranchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BranchServiceImpl branchService;

    private Branch mockBranch1;
    private Branch mockBranch2;
    private BranchDto mockBranchDto1;
    private BranchDto mockBranchDto2;

    @BeforeEach
    void setUp() {
        mockBranch1 = new Branch("Root Branch", null);
        mockBranch1.setId(1L);

        mockBranch2 = new Branch("Child Branch", mockBranch1);
        mockBranch2.setId(2L);

        mockBranchDto1 = new BranchDto();
        mockBranchDto1.setId(1L);
        mockBranchDto1.setName("Root Branch");

        mockBranchDto2 = new BranchDto();
        mockBranchDto2.setId(2L);
        mockBranchDto2.setName("Child Branch");
        mockBranchDto2.setParent(mockBranchDto1);
    }

    @Test
    void createBranch_withParent() {
        BranchDto inputDto = new BranchDto();
        inputDto.setName("Child");
        BranchDto parentDto = new BranchDto();
        parentDto.setId(1L);
        inputDto.setParent(parentDto);

        Branch parent = new Branch("Parent", null);
        parent.setId(1L);

        Branch child = new Branch("Child", parent);
        child.setId(2L);

        when(branchRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(branchRepository.save(any())).thenReturn(child);
        when(modelMapper.map(any(Branch.class), eq(BranchDto.class))).thenReturn(inputDto);

        BranchDto result = branchService.createBranch(inputDto);

        assertEquals("Child", result.getName());
        verify(branchRepository).save(any());
    }

    @Test
    void updateBranchName_success() {
        Branch branch = new Branch("Old", null);
        branch.setId(1L);

        Branch updated = new Branch("New", null);
        updated.setId(1L);

        BranchDto resultDto = new BranchDto();
        resultDto.setId(1L);
        resultDto.setName("New");

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(branchRepository.save(any())).thenReturn(updated);
        when(modelMapper.map(any(Branch.class), eq(BranchDto.class))).thenReturn(resultDto);

        BranchDto result = branchService.updateBranchName(1L, "New");

        assertEquals("New", result.getName());
    }

    @Test
    void reassignParent_success() {
        Branch child = new Branch("Child", null);
        child.setId(2L);

        Branch newParent = new Branch("Parent", null);
        newParent.setId(1L);

        Branch reassigned = new Branch("Child", newParent);
        reassigned.setId(2L);

        BranchDto resultDto = new BranchDto();
        resultDto.setId(2L);
        resultDto.setName("Child");

        when(branchRepository.findById(2L)).thenReturn(Optional.of(child));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(newParent));
        when(branchRepository.save(any())).thenReturn(reassigned);
        when(modelMapper.map(any(Branch.class), eq(BranchDto.class))).thenReturn(resultDto);

        BranchDto result = branchService.reassignParent(2L, 1L);

        assertEquals(2L, result.getId());
        verify(branchRepository).save(any());
    }

    @Test
    void getBranchById_shouldReturnBranchDtoWhenFound_NoParent() {
        when(branchRepository.findById(mockBranch1.getId())).thenReturn(Optional.of(mockBranch1));
        when(modelMapper.map(mockBranch1, BranchDto.class)).thenReturn(mockBranchDto1);
        mockBranchDto1.setParent(null);

        BranchDto result = branchService.getBranchById(mockBranch1.getId(), false);

        assertNotNull(result);
        assertEquals(mockBranchDto1, result);
        assertNull(result.getParent());
    }

    @Test
    void getBranchById_shouldReturnBranchDtoWhenFound_WithParent() {
        when(branchRepository.findById(mockBranch2.getId())).thenReturn(Optional.of(mockBranch2));
        when(modelMapper.map(mockBranch2, BranchDto.class)).thenReturn(mockBranchDto2);

        BranchDto result = branchService.getBranchById(mockBranch2.getId(), true);

        assertNotNull(result);
        assertEquals(mockBranchDto2, result);
        assertNotNull(result.getParent());
        assertEquals(mockBranchDto1.getId(), result.getParent().getId());
    }

    @Test
    void getBranchById_shouldThrowExceptionWhenNotFound() {
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BranchNotFoundException.class, () ->
                branchService.getBranchById(999L, anyBoolean())
        );
    }

    @Test
    void getBranchByName_shouldReturnBranchDtoWhenFound_NoParent() {
        when(branchRepository.findByName(mockBranch1.getName())).thenReturn(Optional.of(mockBranch1));
        when(modelMapper.map(mockBranch1, BranchDto.class)).thenReturn(mockBranchDto1);
        mockBranchDto1.setParent(null);

        BranchDto result = branchService.getBranchByName(mockBranch1.getName(), false);

        assertNotNull(result);
        assertEquals(mockBranchDto1, result);
        assertNull(result.getParent());
    }

    @Test
    void getBranchByName_shouldReturnBranchDtoWhenFound_WithParent() {
        when(branchRepository.findByName(mockBranch2.getName())).thenReturn(Optional.of(mockBranch2));
        when(modelMapper.map(mockBranch2, BranchDto.class)).thenReturn(mockBranchDto2);

        BranchDto result = branchService.getBranchByName(mockBranch2.getName(), true);

        assertNotNull(result);
        assertEquals(mockBranchDto2, result);
        assertNotNull(result.getParent());
        assertEquals(mockBranchDto1.getId(), result.getParent().getId());
    }

    @Test
    void getBranchByName_shouldThrowExceptionWhenNotFound() {
        when(branchRepository.findByName(anyString())).thenReturn(Optional.empty());

        assertThrows(BranchNotFoundException.class, () ->
                branchService.getBranchByName("NonExistent", anyBoolean())
        );
    }

    @Test
    void getAllBranches_shouldReturnListOfBranchDtos_NoParents() {
        List<Branch> allBranches = Arrays.asList(mockBranch1, mockBranch2);
        when(branchRepository.findAll()).thenReturn(allBranches);

        BranchDto simpleBranchDto1 = new BranchDto();
        simpleBranchDto1.setId(mockBranch1.getId());
        simpleBranchDto1.setName(mockBranch1.getName());
        simpleBranchDto1.setParent(null);

        BranchDto simpleBranchDto2 = new BranchDto();
        simpleBranchDto2.setId(mockBranch2.getId());
        simpleBranchDto2.setName(mockBranch2.getName());
        simpleBranchDto2.setParent(null);

        when(modelMapper.map(mockBranch1, BranchDto.class)).thenReturn(simpleBranchDto1);
        when(modelMapper.map(mockBranch2, BranchDto.class)).thenReturn(simpleBranchDto2);

        List<BranchDto> result = branchService.getAllBranches(false);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(simpleBranchDto1, result.get(0));
        assertEquals(simpleBranchDto2, result.get(1));
        assertNull(result.get(0).getParent());
        assertNull(result.get(1).getParent());
    }

    @Test
    void getAllBranches_shouldReturnListOfBranchDtos_WithParents() {
        List<Branch> allBranches = Arrays.asList(mockBranch1, mockBranch2);
        when(branchRepository.findAll()).thenReturn(allBranches);

        when(modelMapper.map(mockBranch1, BranchDto.class)).thenReturn(mockBranchDto1);
        when(modelMapper.map(mockBranch2, BranchDto.class)).thenReturn(mockBranchDto2);

        List<BranchDto> result = branchService.getAllBranches(true);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockBranchDto1, result.get(0));
        assertEquals(mockBranchDto2, result.get(1));
        assertNull(result.get(0).getParent());
        assertNotNull(result.get(1).getParent());
        assertEquals(mockBranchDto1.getId(), result.get(1).getParent().getId());
    }

    @Test
    void getAllBranches_shouldReturnEmptyListWhenNoBranchesExist() {
        when(branchRepository.findAll()).thenReturn(Collections.emptyList());

        List<BranchDto> result = branchService.getAllBranches(false);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getChildBranches_shouldReturnChildrenWhenParentIdGiven_NoParentsInResponse() {
        List<Branch> childrenEntities = Arrays.asList(mockBranch2);
        when(branchRepository.existsById(mockBranch1.getId())).thenReturn(true);
        when(branchRepository.findByParentId(mockBranch1.getId())).thenReturn(childrenEntities);

        BranchDto childWithoutParentInResponse = new BranchDto();
        childWithoutParentInResponse.setId(mockBranch2.getId());
        childWithoutParentInResponse.setName(mockBranch2.getName());
        childWithoutParentInResponse.setParent(null);

        when(modelMapper.map(mockBranch2, BranchDto.class)).thenReturn(childWithoutParentInResponse);

        List<BranchDto> result = branchService.getChildBranches(mockBranch1.getId(), false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(childWithoutParentInResponse, result.get(0));
        assertNull(result.get(0).getParent());
    }

    @Test
    void getChildBranches_shouldReturnChildrenWhenParentIdGiven_WithParentsInResponse() {
        List<Branch> childrenEntities = Arrays.asList(mockBranch2);
        when(branchRepository.existsById(mockBranch1.getId())).thenReturn(true);
        when(branchRepository.findByParentId(mockBranch1.getId())).thenReturn(childrenEntities);

        when(modelMapper.map(mockBranch2, BranchDto.class)).thenReturn(mockBranchDto2);

        List<BranchDto> result = branchService.getChildBranches(mockBranch1.getId(), true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockBranchDto2, result.get(0));
        assertNotNull(result.get(0).getParent());
        assertEquals(mockBranchDto1.getId(), result.get(0).getParent().getId());
    }


    @Test
    void getChildBranches_shouldReturnEmptyListWhenNoChildrenFound() {
        when(branchRepository.existsById(mockBranch1.getId())).thenReturn(true);
        when(branchRepository.findByParentId(mockBranch1.getId())).thenReturn(Collections.emptyList());

        List<BranchDto> result = branchService.getChildBranches(mockBranch1.getId(), anyBoolean());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getChildBranches_shouldThrowExceptionWhenParentNotFound() {
        when(branchRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(BranchNotFoundException.class, () ->
                branchService.getChildBranches(999L, anyBoolean())
        );
    }
}