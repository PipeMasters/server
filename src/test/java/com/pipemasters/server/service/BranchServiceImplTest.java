package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.BranchResponseDto;
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
    private BranchResponseDto mockBranchResponseDto1;
    private BranchResponseDto mockBranchResponseDto2;

    @BeforeEach
    void setUp() {
        mockBranch1 = new Branch("Root Branch", null);
        mockBranch1.setId(1L);

        mockBranch2 = new Branch("Child Branch", mockBranch1);
        mockBranch2.setId(2L);

        mockBranchResponseDto1 = new BranchResponseDto();
        mockBranchResponseDto1.setId(1L);
        mockBranchResponseDto1.setName("Root Branch");

        mockBranchResponseDto2 = new BranchResponseDto();
        mockBranchResponseDto2.setId(2L);
        mockBranchResponseDto2.setName("Child Branch");
        mockBranchResponseDto2.setParentId(mockBranchResponseDto1.getId());
    }

    @Test
    void createBranch_withParent() {
        BranchRequestDto inputDto = new BranchRequestDto();
        inputDto.setName("Child");
        BranchRequestDto parentDto = new BranchRequestDto(); // Assuming parentDto is just a placeholder here
        parentDto.setId(1L);
        inputDto.setParentId(parentDto.getId());

        Branch parent = new Branch("Parent", null);
        parent.setId(1L);

        Branch child = new Branch("Child", parent);
        child.setId(2L);

        BranchResponseDto expectedResponseDto = new BranchResponseDto();
        expectedResponseDto.setId(2L);
        expectedResponseDto.setName("Child");
        expectedResponseDto.setParentId(1L);

        when(branchRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(branchRepository.save(any(Branch.class))).thenReturn(child); // Ensure to save Branch entity
        when(modelMapper.map(any(Branch.class), eq(BranchResponseDto.class))).thenReturn(expectedResponseDto); // Map Branch to BranchResponseDto

        BranchResponseDto result = branchService.createBranch(inputDto);

        assertEquals("Child", result.getName());
        assertEquals(expectedResponseDto, result); // Compare with the full DTO
        verify(branchRepository).save(any(Branch.class)); // Verify saving Branch entity
    }


    @Test
    void updateBranchName_success() {
        Branch branch = new Branch("Old", null);
        branch.setId(1L);

        Branch updated = new Branch("New", null);
        updated.setId(1L);

        BranchResponseDto resultDto = new BranchResponseDto();
        resultDto.setId(1L);
        resultDto.setName("New");

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(branchRepository.save(any(Branch.class))).thenReturn(updated);
        when(modelMapper.map(any(Branch.class), eq(BranchResponseDto.class))).thenReturn(resultDto);

        BranchResponseDto result = branchService.updateBranchName(1L, "New");

        assertEquals("New", result.getName());
        assertEquals(resultDto, result);
    }

    @Test
    void reassignParent_success() {
        Branch child = new Branch("Child", null);
        child.setId(2L);

        Branch newParent = new Branch("Parent", null);
        newParent.setId(1L);

        Branch reassigned = new Branch("Child", newParent);
        reassigned.setId(2L);

        BranchResponseDto resultDto = new BranchResponseDto();
        resultDto.setId(2L);
        resultDto.setName("Child");
        resultDto.setParentId(1L); // Updated parent ID

        when(branchRepository.findById(2L)).thenReturn(Optional.of(child));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(newParent));
        when(branchRepository.save(any(Branch.class))).thenReturn(reassigned);
        when(modelMapper.map(any(Branch.class), eq(BranchResponseDto.class))).thenReturn(resultDto);

        BranchResponseDto result = branchService.reassignParent(2L, 1L);

        assertEquals(2L, result.getId());
        assertEquals(resultDto, result);
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void getBranchById_shouldReturnBranchDtoWhenFound_NoParent() {
        when(branchRepository.findById(mockBranch1.getId())).thenReturn(Optional.of(mockBranch1));
        when(modelMapper.map(mockBranch1, BranchResponseDto.class)).thenReturn(mockBranchResponseDto1);
        mockBranchResponseDto1.setParentId(null);

        BranchResponseDto result = branchService.getBranchById(mockBranch1.getId(), false);

        assertNotNull(result);
        assertEquals(mockBranchResponseDto1, result);
        assertNull(result.getParentId());
    }

    @Test
    void getBranchById_shouldReturnBranchDtoWhenFound_WithParent() {
        when(branchRepository.findById(mockBranch2.getId())).thenReturn(Optional.of(mockBranch2));
        when(modelMapper.map(mockBranch2, BranchResponseDto.class)).thenReturn(mockBranchResponseDto2);

        BranchResponseDto result = branchService.getBranchById(mockBranch2.getId(), true);

        assertNotNull(result);
        assertEquals(mockBranchResponseDto2, result);
        assertNotNull(result.getParentId());
        assertEquals(mockBranchResponseDto1.getId(), result.getParentId());
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
        when(modelMapper.map(mockBranch1, BranchResponseDto.class)).thenReturn(mockBranchResponseDto1);
        mockBranchResponseDto1.setParentId(null);

        BranchResponseDto result = branchService.getBranchByName(mockBranch1.getName(), false);

        assertNotNull(result);
        assertEquals(mockBranchResponseDto1, result);
        assertNull(result.getParentId());
    }

    @Test
    void getBranchByName_shouldReturnBranchDtoWhenFound_WithParent() {
        when(branchRepository.findByName(mockBranch2.getName())).thenReturn(Optional.of(mockBranch2));
        when(modelMapper.map(mockBranch2, BranchResponseDto.class)).thenReturn(mockBranchResponseDto2);

        BranchResponseDto result = branchService.getBranchByName(mockBranch2.getName(), true);

        assertNotNull(result);
        assertEquals(mockBranchResponseDto2, result);
        assertNotNull(result.getParentId());
        assertEquals(mockBranchResponseDto1.getId(), result.getParentId());
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

        BranchResponseDto simpleBranchResponseDto1 = new BranchResponseDto();
        simpleBranchResponseDto1.setId(mockBranch1.getId());
        simpleBranchResponseDto1.setName(mockBranch1.getName());
        simpleBranchResponseDto1.setParentId(null);

        BranchResponseDto simpleBranchResponseDto2 = new BranchResponseDto();
        simpleBranchResponseDto2.setId(mockBranch2.getId());
        simpleBranchResponseDto2.setName(mockBranch2.getName());
        simpleBranchResponseDto2.setParentId(null);

        when(modelMapper.map(mockBranch1, BranchResponseDto.class)).thenReturn(simpleBranchResponseDto1);
        when(modelMapper.map(mockBranch2, BranchResponseDto.class)).thenReturn(simpleBranchResponseDto2);

        List<BranchResponseDto> result = branchService.getAllBranches(false);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(simpleBranchResponseDto1, result.get(0));
        assertEquals(simpleBranchResponseDto2, result.get(1));
        assertNull(result.get(0).getParentId());
        assertNull(result.get(1).getParentId());
    }

    @Test
    void getAllBranches_shouldReturnListOfBranchDtos_WithParents() {
        List<Branch> allBranches = Arrays.asList(mockBranch1, mockBranch2);
        when(branchRepository.findAll()).thenReturn(allBranches);

        when(modelMapper.map(mockBranch1, BranchResponseDto.class)).thenReturn(mockBranchResponseDto1);
        when(modelMapper.map(mockBranch2, BranchResponseDto.class)).thenReturn(mockBranchResponseDto2);

        List<BranchResponseDto> result = branchService.getAllBranches(true);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockBranchResponseDto1, result.get(0));
        assertEquals(mockBranchResponseDto2, result.get(1));
        assertNull(result.get(0).getParentId());
        assertNotNull(result.get(1).getParentId());
        assertEquals(mockBranchResponseDto1.getId(), result.get(1).getParentId());
    }

    @Test
    void getAllBranches_shouldReturnEmptyListWhenNoBranchesExist() {
        when(branchRepository.findAll()).thenReturn(Collections.emptyList());

        List<BranchResponseDto> result = branchService.getAllBranches(false);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getChildBranches_shouldReturnChildrenWhenParentIdGiven_NoParentsInResponse() {
        List<Branch> childrenEntities = Arrays.asList(mockBranch2);
        when(branchRepository.existsById(mockBranch1.getId())).thenReturn(true);
        when(branchRepository.findByParentId(mockBranch1.getId())).thenReturn(childrenEntities);

        BranchResponseDto childWithoutParentInResponse = new BranchResponseDto();
        childWithoutParentInResponse.setId(mockBranch2.getId());
        childWithoutParentInResponse.setName(mockBranch2.getName());
        childWithoutParentInResponse.setParentId(null);

        when(modelMapper.map(mockBranch2, BranchResponseDto.class)).thenReturn(childWithoutParentInResponse);

        List<BranchResponseDto> result = branchService.getChildBranches(mockBranch1.getId(), false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(childWithoutParentInResponse, result.get(0));
        assertNull(result.get(0).getParentId());
    }

    @Test
    void getChildBranches_shouldReturnChildrenWhenParentIdGiven_WithParentsInResponse() {
        List<Branch> childrenEntities = Arrays.asList(mockBranch2);
        when(branchRepository.existsById(mockBranch1.getId())).thenReturn(true);
        when(branchRepository.findByParentId(mockBranch1.getId())).thenReturn(childrenEntities);

        when(modelMapper.map(mockBranch2, BranchResponseDto.class)).thenReturn(mockBranchResponseDto2);

        List<BranchResponseDto> result = branchService.getChildBranches(mockBranch1.getId(), true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockBranchResponseDto2, result.get(0));
        assertNotNull(result.get(0).getParentId());
        assertEquals(mockBranchResponseDto1.getId(), result.get(0).getParentId());
    }


    @Test
    void getChildBranches_shouldReturnEmptyListWhenNoChildrenFound() {
        when(branchRepository.existsById(mockBranch1.getId())).thenReturn(true);
        when(branchRepository.findByParentId(mockBranch1.getId())).thenReturn(Collections.emptyList());

        List<BranchResponseDto> result = branchService.getChildBranches(mockBranch1.getId(), anyBoolean());

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

    @Test
    void getRootBranches_shouldReturnListOfParentBranches() {
        List<Branch> rootEntities = Arrays.asList(mockBranch1);
        when(branchRepository.findByParentIsNull()).thenReturn(rootEntities);

        BranchResponseDto simpleRootDto = new BranchResponseDto();
        simpleRootDto.setId(mockBranch1.getId());
        simpleRootDto.setName(mockBranch1.getName());
        simpleRootDto.setParentId(null);

        when(modelMapper.map(mockBranch1, BranchResponseDto.class)).thenReturn(simpleRootDto);

        List<BranchResponseDto> result = branchService.getParentBranches();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(simpleRootDto, result.get(0));
        assertNull(result.get(0).getParentId());
    }

    @Test
    void getRootBranches_shouldReturnEmptyListWhenNoParentBranchesExist() {
        when(branchRepository.findByParentIsNull()).thenReturn(Collections.emptyList());

        List<BranchResponseDto> result = branchService.getParentBranches();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}