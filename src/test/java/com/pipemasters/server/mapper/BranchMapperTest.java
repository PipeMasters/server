package com.pipemasters.server.mapper;

import com.pipemasters.server.config.ModelMapperConfig;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.entity.Branch;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

public class BranchMapperTest {

    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapperConfig().modelMapper();
    }

    @Test
    public void testBranchToDto_basicMapping() {
        Branch branch = new Branch("Central", null);
        branch.setId(1L);

        BranchRequestDto dto = modelMapper.map(branch, BranchRequestDto.class);

        assertEquals(branch.getName(), dto.getName());
        assertEquals(branch.getId(), dto.getId());
        assertNull(dto.getParentId(), "Parent should be skipped in mapping");
    }

    @Test
    public void testBranchToDto_withParent() {
        Branch parent = new Branch("Root", null);
        parent.setId(2L);

        Branch branch = new Branch("Child", parent);
        branch.setId(3L);

        BranchRequestDto dto = modelMapper.map(branch, BranchRequestDto.class);

        assertEquals(branch.getName(), dto.getName());
        assertEquals(branch.getId(), dto.getId());
        assertNull(dto.getParentId(), "Parent should be skipped due to mapping configuration");
    }

    @Test
    public void testBranchDtoToEntity() {
        BranchRequestDto dto = new BranchRequestDto("Finance", null);
        dto.setId(10L);

        Branch entity = modelMapper.map(dto, Branch.class);

        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getId(), entity.getId());
        assertNull(entity.getParent(), "Parent should be null by default");
    }

    @Test
    public void testBranchRecursiveMapping() {
        Branch root = new Branch("Root", null);
        root.setId(1L);

        Branch child = new Branch("Child", root);
        child.setId(2L);

        root.setParent(child);

        BranchRequestDto dto = modelMapper.map(child, BranchRequestDto.class);

        assertNull(dto.getParentId(), "Cyclic parent mapping should be skipped");
    }
}
