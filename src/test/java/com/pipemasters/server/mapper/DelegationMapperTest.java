package com.pipemasters.server.mapper;

import com.pipemasters.server.config.ModelMapperConfig;
import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.dto.DelegationDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.Delegation;
import com.pipemasters.server.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DelegationMapperTest {
    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapperConfig().modelMapper();
    }

    @Test
    void testDelegationToDtoMapping() {
        User delegator = new User();
        delegator.setId(1L);
        User substitute = new User();
        substitute.setId(2L);

        Delegation delegation = new Delegation(delegator, substitute,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10));

        DelegationDto dto = modelMapper.map(delegation, DelegationDto.class);

        assertEquals(1L, dto.getDelegatorId());
        assertEquals(2L, dto.getSubstituteId());
        assertEquals(LocalDate.of(2024, 1, 1), dto.getFromDate());
        assertEquals(LocalDate.of(2024, 1, 10), dto.getToDate());
    }

    @Test
    void testBranchToDtoMappingWithParentSkipped() {
        Branch parent = new Branch("Parent Branch", null);
        parent.setId(10L);

        Branch child = new Branch("Child Branch", parent);
        child.setId(20L);

        BranchDto dto = modelMapper.map(child, BranchDto.class);

        assertEquals("Child Branch", dto.getName());
        assertNull(dto.getParentId(), "Parent must be skipped in DTO to avoid recursion");
    }


    @Test
    void testBranchDtoToEntityMapping() {
        BranchDto dto = new BranchDto("Test Branch", null);
        dto.setId(30L);

        Branch entity = modelMapper.map(dto, Branch.class);

        assertEquals("Test Branch", entity.getName());
        assertNull(entity.getParent());
    }


    @Test
    void testDelegationMappingWithNulls() {
        Delegation delegation = new Delegation(null, null, null, null);

        DelegationDto dto = modelMapper.map(delegation, DelegationDto.class);

        assertNull(dto.getDelegatorId());
        assertNull(dto.getSubstituteId());
        assertNull(dto.getFromDate());
        assertNull(dto.getToDate());
    }
}
